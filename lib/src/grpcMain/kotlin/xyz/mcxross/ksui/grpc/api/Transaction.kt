package xyz.mcxross.ksui.grpc.api

import com.google.protobuf.kotlin.FieldMask
import sui.rpc.v2.BatchGetTransactionsResponse
import sui.rpc.v2.ExecuteTransactionRequest
import sui.rpc.v2.ExecuteTransactionResponse
import sui.rpc.v2.GetTransactionRequest
import sui.rpc.v2.GetTransactionResponse
import sui.rpc.v2.SimulateTransactionRequest
import sui.rpc.v2.SimulateTransactionResponse
import sui.rpc.v2.Transaction as GrpcTransaction
import sui.rpc.v2.UserSignature
import xyz.mcxross.ksui.grpc.internal.GrpcRuntime
import xyz.mcxross.ksui.grpc.internal.batchGetTransactions as internalBatchGetTransactions
import xyz.mcxross.ksui.grpc.internal.executeTransaction as internalExecuteTransaction
import xyz.mcxross.ksui.grpc.internal.getTransaction as internalGetTransaction
import xyz.mcxross.ksui.grpc.internal.simulateTransaction as internalSimulateTransaction
import xyz.mcxross.ksui.grpc.protocol.Transaction as TransactionProtocol

internal class Transaction(private val runtime: GrpcRuntime) : TransactionProtocol {
  override suspend fun getTransaction(request: GetTransactionRequest): GetTransactionResponse =
    internalGetTransaction(runtime, request)

  override suspend fun getTransaction(
    digest: String,
    readMask: FieldMask?,
  ): GetTransactionResponse = internalGetTransaction(runtime, digest, readMask)

  override suspend fun batchGetTransactions(
    digests: List<String>,
    readMask: FieldMask?,
  ): BatchGetTransactionsResponse = internalBatchGetTransactions(runtime, digests, readMask)

  override suspend fun executeTransaction(
    request: ExecuteTransactionRequest
  ): ExecuteTransactionResponse = internalExecuteTransaction(runtime, request)

  override suspend fun executeTransaction(
    transaction: GrpcTransaction,
    signatures: List<UserSignature>,
    readMask: FieldMask?,
  ): ExecuteTransactionResponse =
    internalExecuteTransaction(runtime, transaction, signatures, readMask)

  override suspend fun executeTransaction(
    transactionBytes: ByteArray,
    signatures: List<ByteArray>,
    readMask: FieldMask?,
  ): ExecuteTransactionResponse =
    internalExecuteTransaction(runtime, transactionBytes, signatures, readMask)

  override suspend fun simulateTransaction(
    request: SimulateTransactionRequest
  ): SimulateTransactionResponse = internalSimulateTransaction(runtime, request)

  override suspend fun simulateTransaction(
    transaction: GrpcTransaction,
    readMask: FieldMask?,
    checks: SimulateTransactionRequest.TransactionChecks?,
    doGasSelection: Boolean?,
  ): SimulateTransactionResponse =
    internalSimulateTransaction(runtime, transaction, readMask, checks, doGasSelection)

  override suspend fun simulateTransaction(
    transactionBytes: ByteArray,
    readMask: FieldMask?,
    checks: SimulateTransactionRequest.TransactionChecks?,
    doGasSelection: Boolean?,
  ): SimulateTransactionResponse =
    internalSimulateTransaction(runtime, transactionBytes, readMask, checks, doGasSelection)
}
