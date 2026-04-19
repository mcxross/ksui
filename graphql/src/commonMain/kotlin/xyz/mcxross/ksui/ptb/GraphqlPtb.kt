package xyz.mcxross.ksui.ptb

import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.SuiKit
import xyz.mcxross.ksui.core.model.ObjectDataOptions
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.ptb.Argument
import xyz.mcxross.ksui.core.ptb.Command
import xyz.mcxross.ksui.core.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.core.ptb.ProgrammableTransactionBuilder
import xyz.mcxross.ksui.core.ptb.ProgrammableTransactionResolver
import xyz.mcxross.ksui.core.ptb.PtbDsl
import xyz.mcxross.ksui.core.ptb.ResolvedObject
import xyz.mcxross.ksui.core.ptb.ResolvedObjectOwner
import xyz.mcxross.ksui.generated.GetNormalizedMoveFunctionQuery
import xyz.mcxross.ksui.generated.fragment.RPC_MOVE_FUNCTION_FIELDS

suspend fun ptb(client: Sui = SuiKit.client, block: PtbDsl.() -> Unit): ProgrammableTransaction {
  val builder = ProgrammableTransactionBuilder()
  val dsl = PtbDsl(builder)
  dsl.block()
  return builder.build(GraphqlProgrammableTransactionResolver(client))
}

private class GraphqlProgrammableTransactionResolver(private val sui: Sui) :
  ProgrammableTransactionResolver {

  override suspend fun mutableInputIndexes(commands: List<Command>): Set<Int> {
    val mutableInputs = mutableSetOf<Int>()
    val functionSignatureCache = mutableMapOf<String, GetNormalizedMoveFunctionQuery.Data?>()

    for (command in commands) {
      if (command !is Command.MoveCall) continue

      val callDetails = command.moveCall
      val target = "${callDetails.pakage.hash}::${callDetails.module}::${callDetails.function}"
      val signatureResponse =
        functionSignatureCache.getOrPut(target) {
          when (val result = sui.getNormalizedMoveFunction(target)) {
            is Result.Ok -> result.value
            is Result.Err ->
              throw IllegalStateException("Failed to get function signature for $target")
          }
        }
      val params: List<RPC_MOVE_FUNCTION_FIELDS.Parameter> =
        signatureResponse
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
    val clean = id.removePrefix("0x")
    val padded = clean.padStart(64, '0')
    return "0x$padded"
  }
}
