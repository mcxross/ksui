package xyz.mcxross.ksui.grpc.internal

import com.google.protobuf.kotlin.FieldMask
import sui.rpc.v2.BatchGetTransactionsRequestInternal
import sui.rpc.v2.BatchGetTransactionsResponse
import sui.rpc.v2.ExecuteTransactionRequest
import sui.rpc.v2.ExecuteTransactionRequestInternal
import sui.rpc.v2.ExecuteTransactionResponse
import sui.rpc.v2.GetTransactionRequest
import sui.rpc.v2.GetTransactionRequestInternal
import sui.rpc.v2.GetTransactionResponse
import sui.rpc.v2.SimulateTransactionRequest
import sui.rpc.v2.SimulateTransactionRequestInternal
import sui.rpc.v2.SimulateTransactionResponse
import sui.rpc.v2.Transaction as GrpcTransaction
import sui.rpc.v2.UserSignature

internal suspend fun getTransaction(
  runtime: GrpcRuntime,
  request: GetTransactionRequest,
): GetTransactionResponse = runtime.ledgerService.GetTransaction(request)

internal suspend fun getTransaction(
  runtime: GrpcRuntime,
  digest: String,
  readMask: FieldMask? = null,
): GetTransactionResponse =
  getTransaction(
    runtime,
    GetTransactionRequestInternal().apply {
      this.digest = digest
      readMask?.let { this.readMask = it }
    },
  )

internal suspend fun batchGetTransactions(
  runtime: GrpcRuntime,
  digests: List<String>,
  readMask: FieldMask? = null,
): BatchGetTransactionsResponse =
  runtime.ledgerService.BatchGetTransactions(
    BatchGetTransactionsRequestInternal().apply {
      this.digests = digests
      readMask?.let { this.readMask = it }
    }
  )

internal suspend fun executeTransaction(
  runtime: GrpcRuntime,
  request: ExecuteTransactionRequest,
): ExecuteTransactionResponse = runtime.transactionExecutionService.ExecuteTransaction(request)

internal suspend fun executeTransaction(
  runtime: GrpcRuntime,
  transaction: GrpcTransaction,
  signatures: List<UserSignature>,
  readMask: FieldMask? = null,
): ExecuteTransactionResponse =
  executeTransaction(
    runtime,
    ExecuteTransactionRequestInternal().apply {
      this.transaction = transaction
      this.signatures = signatures
      readMask?.let { this.readMask = it }
    },
  )

internal suspend fun executeTransaction(
  runtime: GrpcRuntime,
  transactionBytes: ByteArray,
  signatures: List<ByteArray>,
  readMask: FieldMask? = null,
): ExecuteTransactionResponse =
  executeTransaction(
    runtime,
    transaction = transactionBytes.asGrpcTransaction(),
    signatures = signatures.map { it.asGrpcSignature() },
    readMask = readMask,
  )

internal suspend fun simulateTransaction(
  runtime: GrpcRuntime,
  request: SimulateTransactionRequest,
): SimulateTransactionResponse = runtime.transactionExecutionService.SimulateTransaction(request)

internal suspend fun simulateTransaction(
  runtime: GrpcRuntime,
  transaction: GrpcTransaction,
  readMask: FieldMask? = null,
  checks: SimulateTransactionRequest.TransactionChecks? = null,
  doGasSelection: Boolean? = null,
): SimulateTransactionResponse =
  simulateTransaction(
    runtime,
    SimulateTransactionRequestInternal().apply {
      this.transaction = transaction
      readMask?.let { this.readMask = it }
      checks?.let { this.checks = it }
      doGasSelection?.let { this.doGasSelection = it }
    },
  )

internal suspend fun simulateTransaction(
  runtime: GrpcRuntime,
  transactionBytes: ByteArray,
  readMask: FieldMask? = null,
  checks: SimulateTransactionRequest.TransactionChecks? = null,
  doGasSelection: Boolean? = null,
): SimulateTransactionResponse =
  simulateTransaction(
    runtime,
    transaction = transactionBytes.asGrpcTransaction(),
    readMask = readMask,
    checks = checks,
    doGasSelection = doGasSelection,
  )
