package xyz.mcxross.ksui.core.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.core.ptb.Argument
import xyz.mcxross.ksui.core.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.core.ptb.ProgrammableTransactionBuilder
import xyz.mcxross.ksui.core.ptb.TransactionKind

// suikit/swift-smoke builds the same transaction through SuiKit and expects these bytes.
private val SAMPLE_TRANSACTION_KIND_HEX =
  "00" + // TransactionKind::ProgrammableTransaction
    "01" + "00" + "11" + "02" + "0100000000000000" + "0200000000000000" + // Pure(vector<u64>[1, 2])
    "01" + "00" + "00".repeat(31) + "42" + // MoveCall to package 0x42
    "07" + "636f756e746572" + "09" + "62617463685f736574" + // counter::batch_set
    "00" + "02" + "00" + "010000" // no type arguments; GasCoin, Input(0)

private fun sampleTransaction(): ProgrammableTransaction {
  val builder = ProgrammableTransactionBuilder()
  val values = byteArrayOf(2, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0)
  builder.moveCall(
    "0x42::counter::batch_set",
    args = listOf(Argument.GasCoin, builder.pure(values)),
  )
  return builder.build()
}

private fun ByteArray.hex(): String =
  joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }

class TransactionBytesTest :
  StringSpec({
    "toBcs encodes the programmable transaction" {
      val transaction = sampleTransaction()
      transaction.toBcs().hex() shouldBe Bcs.encodeToByteArray(transaction).hex()
    }

    "toTransactionKindBcs encodes the transaction as a TransactionKind" {
      val transaction = sampleTransaction()
      val expected =
        Bcs.encodeToByteArray<TransactionKind>(TransactionKind.ProgrammableTransaction(transaction))
      transaction.toTransactionKindBcs().hex() shouldBe expected.hex()
      transaction.toTransactionKindBcs().hex() shouldBe SAMPLE_TRANSACTION_KIND_HEX
    }
  })
