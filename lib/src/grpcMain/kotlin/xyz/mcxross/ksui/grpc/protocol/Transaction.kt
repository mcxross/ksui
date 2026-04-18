package xyz.mcxross.ksui.grpc.protocol

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

interface Transaction {
  suspend fun getTransaction(request: GetTransactionRequest): GetTransactionResponse

  suspend fun getTransaction(digest: String, readMask: FieldMask? = null): GetTransactionResponse

  suspend fun batchGetTransactions(
    digests: List<String>,
    readMask: FieldMask? = null,
  ): BatchGetTransactionsResponse

  suspend fun executeTransaction(request: ExecuteTransactionRequest): ExecuteTransactionResponse

  suspend fun executeTransaction(
    transaction: GrpcTransaction,
    signatures: List<UserSignature>,
    readMask: FieldMask? = null,
  ): ExecuteTransactionResponse

  suspend fun executeTransaction(
    transactionBytes: ByteArray,
    signatures: List<ByteArray>,
    readMask: FieldMask? = null,
  ): ExecuteTransactionResponse

  suspend fun simulateTransaction(request: SimulateTransactionRequest): SimulateTransactionResponse

  suspend fun simulateTransaction(
    transaction: GrpcTransaction,
    readMask: FieldMask? = null,
    checks: SimulateTransactionRequest.TransactionChecks? = null,
    doGasSelection: Boolean? = null,
  ): SimulateTransactionResponse

  suspend fun simulateTransaction(
    transactionBytes: ByteArray,
    readMask: FieldMask? = null,
    checks: SimulateTransactionRequest.TransactionChecks? = null,
    doGasSelection: Boolean? = null,
  ): SimulateTransactionResponse
}
