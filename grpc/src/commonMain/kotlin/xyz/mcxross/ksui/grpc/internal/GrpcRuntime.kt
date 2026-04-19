package xyz.mcxross.ksui.grpc.internal

import kotlin.time.Duration
import kotlinx.rpc.grpc.client.GrpcClient
import kotlinx.rpc.withService
import sui.rpc.v2.LedgerService
import sui.rpc.v2.MovePackageService
import sui.rpc.v2.NameService
import sui.rpc.v2.SignatureVerificationService
import sui.rpc.v2.StateService
import sui.rpc.v2.SubscriptionService
import sui.rpc.v2.TransactionExecutionService

internal class GrpcRuntime(private val grpcClient: GrpcClient) {
  internal val ledgerService: LedgerService by lazy { grpcClient.withService() }
  internal val movePackageService: MovePackageService by lazy { grpcClient.withService() }
  internal val nameService: NameService by lazy { grpcClient.withService() }
  internal val signatureVerificationService: SignatureVerificationService by lazy {
    grpcClient.withService()
  }
  internal val stateService: StateService by lazy { grpcClient.withService() }
  internal val subscriptionService: SubscriptionService by lazy { grpcClient.withService() }
  internal val transactionExecutionService: TransactionExecutionService by lazy {
    grpcClient.withService()
  }

  internal fun close() {
    grpcClient.shutdown()
  }

  internal fun shutdownNow() {
    grpcClient.shutdownNow()
  }

  internal suspend fun awaitTermination(timeout: Duration) {
    grpcClient.awaitTermination(timeout)
  }
}
