package xyz.mcxross.ksui.ptb

import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.SuiKit

suspend fun ptb(client: Sui = SuiKit.client, block: PtbDsl.() -> Unit): ProgrammableTransaction {
  val builder = ProgrammableTransactionBuilder()
  val dsl = PtbDsl(builder)
  dsl.block()
  return builder.build()
}
