package xyz.mcxross.ksui.ptb

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.SuiKit
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.ObjectDataOptions
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.ptb.Argument
import xyz.mcxross.ksui.core.ptb.Command
import xyz.mcxross.ksui.core.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.core.ptb.ProgrammableTransactionBuilder
import xyz.mcxross.ksui.core.ptb.ProgrammableTransactionResolver
import xyz.mcxross.ksui.core.ptb.ResolvedObject
import xyz.mcxross.ksui.core.ptb.ResolvedObjectOwner
import xyz.mcxross.ksui.generated.fragment.RPC_MOVE_FUNCTION_FIELDS

@Throws(Exception::class)
suspend fun ptb(
  client: Sui,
  block: suspend NetworkPtbDsl.() -> Unit,
): ProgrammableTransaction = ptb(client, null, block)

@Throws(Exception::class)
suspend fun ptb(
  client: Sui = SuiKit.client,
  sender: AccountAddress? = null,
  block: suspend NetworkPtbDsl.() -> Unit,
): ProgrammableTransaction {
  val builder = ProgrammableTransactionBuilder()
  val dsl = NetworkPtbDsl(builder, client, sender)
  dsl.block()
  return builder.build(GraphqlProgrammableTransactionResolver(client))
}

private class GraphqlProgrammableTransactionResolver(private val sui: Sui) :
  ProgrammableTransactionResolver {

  override suspend fun mutableInputIndexes(commands: List<Command>): Set<Int> {
    val moveCalls = commands.filterIsInstance<Command.MoveCall>()
    if (moveCalls.isEmpty()) return emptySet()

    val distinctTargets =
      moveCalls
        .map { "${it.moveCall.pakage.hash}::${it.moveCall.module}::${it.moveCall.function}" }
        .distinct()

    val signaturesByTarget = coroutineScope {
      distinctTargets
        .associateWith { target ->
          async {
            when (val result = sui.getNormalizedMoveFunction(target)) {
              is Result.Ok -> result.value
              is Result.Err ->
                throw IllegalStateException("Failed to get function signature for $target")
            }
          }
        }
        .mapValues { (_, deferred) -> deferred.await() }
    }

    val mutableInputs = mutableSetOf<Int>()
    for (command in moveCalls) {
      val callDetails = command.moveCall
      val target = "${callDetails.pakage.hash}::${callDetails.module}::${callDetails.function}"
      val params: List<RPC_MOVE_FUNCTION_FIELDS.Parameter> =
        signaturesByTarget[target]
          ?.`object`
          ?.asMovePackage
          ?.module
          ?.function
          ?.rPC_MOVE_FUNCTION_FIELDS
          ?.parameters ?: continue

      command.moveCall.arguments.zip(params).forEach { (argument, parameter) ->
        if (argument is Argument.Input && parameter.isMutableReference()) {
          mutableInputs += argument.index.toInt()
        }
      }
    }

    return mutableInputs
  }

  override suspend fun resolveObjects(objectIds: List<String>): Map<String, ResolvedObject> {
    return when (
      val result = sui.multiGetObjects(objectIds, options = ObjectDataOptions(showOwner = true))
    ) {
      is Result.Ok ->
        result.value?.multiGetObjects?.filterNotNull()?.associate { suiObject ->
          val fields = suiObject.rPC_OBJECT_FIELDS
          val owner = fields.owner?.rPC_OBJECT_OWNER_FIELDS
          val normalizedId = normalize(fields.objectId.toString())
          val resolvedOwner =
            when (owner?.__typename) {
              "Shared" ->
                ResolvedObjectOwner.Shared(
                  owner.onShared?.initialSharedVersion?.toString()?.toLong() ?: 0L
                )
              "AddressOwner" -> ResolvedObjectOwner.AddressOwner
              "Immutable" -> ResolvedObjectOwner.Immutable
              else ->
                throw IllegalStateException("Unsupported object owner type: ${owner?.__typename}")
            }
          normalizedId to
            ResolvedObject(
              objectId = fields.objectId.toString(),
              version = fields.version.toString().toLong(),
              digest = fields.digest,
              owner = resolvedOwner,
            )
        } ?: emptyMap()
      is Result.Err -> throw IllegalStateException("Failed to resolve objects: ${result.error}")
    }
  }

  private fun RPC_MOVE_FUNCTION_FIELDS.Parameter.isMutableReference(): Boolean {
    val signatureMap = signature as? Map<*, *>
    return signatureMap?.get("ref") == "&mut"
  }

  private fun normalize(id: String): String {
    val clean = id.trim().removePrefix("0x").removePrefix("0X").lowercase()
    return "0x${clean.padStart(64, '0')}"
  }
}
