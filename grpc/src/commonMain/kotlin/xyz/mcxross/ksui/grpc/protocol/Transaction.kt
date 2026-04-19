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
import xyz.mcxross.ksui.core.exception.SuiError
import xyz.mcxross.ksui.core.model.Result

interface Transaction {
  suspend fun getTransaction(
    request: GetTransactionRequest
  ): Result<GetTransactionResponse, SuiError>

  suspend fun getTransaction(
    digest: String,
    readMask: FieldMask? = null,
  ): Result<GetTransactionResponse, SuiError>

  suspend fun batchGetTransactions(
    digests: List<String>,
    readMask: FieldMask? = null,
  ): Result<BatchGetTransactionsResponse, SuiError>

  suspend fun executeTransaction(
    request: ExecuteTransactionRequest
  ): Result<ExecuteTransactionResponse, SuiError>

  suspend fun executeTransaction(
    transaction: GrpcTransaction,
    signatures: List<UserSignature>,
    readMask: FieldMask? = null,
  ): Result<ExecuteTransactionResponse, SuiError>

  suspend fun executeTransaction(
    transactionBytes: ByteArray,
    signatures: List<ByteArray>,
    readMask: FieldMask? = null,
  ): Result<ExecuteTransactionResponse, SuiError>

  suspend fun simulateTransaction(
    request: SimulateTransactionRequest
  ): Result<SimulateTransactionResponse, SuiError>

  suspend fun simulateTransaction(
    transaction: GrpcTransaction,
    readMask: FieldMask? = null,
    checks: SimulateTransactionRequest.TransactionChecks? = null,
    doGasSelection: Boolean? = null,
  ): Result<SimulateTransactionResponse, SuiError>

  suspend fun simulateTransaction(
    transactionBytes: ByteArray,
    readMask: FieldMask? = null,
    checks: SimulateTransactionRequest.TransactionChecks? = null,
    doGasSelection: Boolean? = null,
  ): Result<SimulateTransactionResponse, SuiError>
}
