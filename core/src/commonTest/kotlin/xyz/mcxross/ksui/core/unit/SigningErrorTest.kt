package xyz.mcxross.ksui.core.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import xyz.mcxross.ksui.core.PRIVATE_KEY_DATA
import xyz.mcxross.ksui.core.account.Account
import xyz.mcxross.ksui.core.crypto.PublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.exception.E
import xyz.mcxross.ksui.core.model.AccountAddress
import xyz.mcxross.ksui.core.model.GasData
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.model.TransactionData
import xyz.mcxross.ksui.core.model.TransactionExpiration
import xyz.mcxross.ksui.core.model.sign
import xyz.mcxross.ksui.core.ptb.ProgrammableTransactionBuilder
import xyz.mcxross.ksui.core.ptb.TransactionKind

class SigningErrorTest :
  StringSpec({
    "TransactionData.sign returns the signer's original exception" {
      val failure = IllegalStateException("hardware wallet disconnected")
      val real = Account.import(PRIVATE_KEY_DATA)
      val failing =
        object : Account() {
          override val publicKey: PublicKey = real.publicKey
          override val address: AccountAddress = real.address
          override val scheme: SignatureScheme = real.scheme

          override suspend fun sign(message: ByteArray): Result<ByteArray, E> = Result.Err(failure)

          override suspend fun verify(message: ByteArray, signature: ByteArray): Result<Boolean, E> =
            Result.Ok(false)
        }
      val tx =
        TransactionData.V1(
          kind = TransactionKind.ProgrammableTransaction(ProgrammableTransactionBuilder().build()),
          sender = real.address,
          gasData = GasData(emptyList(), real.address, 1000UL, 1_000_000UL),
          expiration = TransactionExpiration.None,
        )
      val result = tx.sign(failing)
      result.shouldBeInstanceOf<Result.Err<Exception>>().error shouldBeSameInstanceAs failure
    }
  })
