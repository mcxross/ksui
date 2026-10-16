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
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.GasConfig
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.model.TransactionData
import xyz.mcxross.ksui.core.ptb.ProgrammableTransaction

interface Transaction {
  @Throws(Exception::class)
  suspend fun getTransaction(
    request: GetTransactionRequest
  ): Result<GetTransactionResponse, SuiError>

  @Throws(Exception::class)
  suspend fun getTransaction(
    digest: String,
    readMask: FieldMask? = null,
  ): Result<GetTransactionResponse, SuiError>

  @Throws(Exception::class)
  suspend fun batchGetTransactions(
    digests: List<String>,
    readMask: FieldMask? = null,
  ): Result<BatchGetTransactionsResponse, SuiError>

  @Throws(Exception::class)
  suspend fun executeTransaction(
    request: ExecuteTransactionRequest
  ): Result<ExecuteTransactionResponse, SuiError>

  @Throws(Exception::class)
  suspend fun executeTransaction(
    transaction: GrpcTransaction,
    signatures: List<UserSignature>,
    readMask: FieldMask? = null,
  ): Result<ExecuteTransactionResponse, SuiError>

  @Throws(Exception::class)
  suspend fun executeTransaction(
    transactionBytes: ByteArray,
    signatures: List<ByteArray>,
    readMask: FieldMask? = null,
  ): Result<ExecuteTransactionResponse, SuiError>

  @Throws(Exception::class)
  suspend fun simulateTransaction(
    request: SimulateTransactionRequest
  ): Result<SimulateTransactionResponse, SuiError>

  @Throws(Exception::class)
  suspend fun simulateTransaction(
    transaction: GrpcTransaction,
    readMask: FieldMask? = null,
    checks: SimulateTransactionRequest.TransactionChecks? = null,
    doGasSelection: Boolean? = null,
  ): Result<SimulateTransactionResponse, SuiError>

  @Throws(Exception::class)
  suspend fun simulateTransaction(
    transactionBytes: ByteArray,
    readMask: FieldMask? = null,
    checks: SimulateTransactionRequest.TransactionChecks? = null,
    doGasSelection: Boolean? = null,
  ): Result<SimulateTransactionResponse, SuiError>

  @Throws(Exception::class)
  suspend fun buildTransaction(
    sender: AccountAddress,
    ptb: ProgrammableTransaction,
    gas: GasConfig = GasConfig(),
  ): Result<TransactionData, SuiError>
}
