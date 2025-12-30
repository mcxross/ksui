package xyz.mcxross.ksui.sample

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlin.io.encoding.Base64
import kotlinx.serialization.Serializable
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.Sui
import xyz.mcxross.ksui.model.GasLessTransactionData
import xyz.mcxross.ksui.model.Network
import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.model.SuiSettings
import xyz.mcxross.ksui.model.TransactionData
import xyz.mcxross.ksui.model.sign
import xyz.mcxross.ksui.ptb.ptb
import xyz.mcxross.ksui.util.runBlocking

@Serializable
data class SponsoredResponseManual(
  val txBytes: String,
  val sponsorSignature: String,
  val status: String,
)

fun main() = runBlocking {
  val sui = Sui(config = SuiConfig(SuiSettings(Network.TESTNET)))

  val ptb = ptb {
    moveCall {
      target = HELLO_WORLD
      arguments = +pure(0UL)
    }
  }

  val gasLess = GasLessTransactionData.new(ptb, ALICE_ACCOUNT.address)
  val base64 = Base64.encode(Bcs.encodeToByteArray(gasLess))

  val httpClient =
    HttpClient(CIO) {
      install(ContentNegotiation) { json() }
      followRedirects = true
    }

  val res =
    httpClient.post("http://0.0.0.0:8080/gas") {
      contentType(ContentType.Application.Json)
      header("X-API-Key", "zk_xPFGtrE1ZclSKQN1TRYBfqQ9-B5QJKVrpUu5K_0IhMA")
      setBody(GasRequestManual(txBytes = base64, sender = ALICE_ACCOUNT.address.toString()))
    }

  if (res.status != HttpStatusCode.OK) {
    throw Exception("HTTP error ${res.status}: ${res.bodyAsText()}")
  }

  val sponsoredResponse = res.body<SponsoredResponseManual>()

  val txDataBytes = Base64.decode(sponsoredResponse.txBytes)
  val txData = xyz.mcxross.ksui.util.bcsDecode<TransactionData>(txDataBytes)

  val userSignature =
    when (val sig = txData.sign(ALICE_ACCOUNT)) {
      is xyz.mcxross.ksui.model.Result.Ok -> sig.value
      is xyz.mcxross.ksui.model.Result.Err -> throw sig.error
    }

  val response =
    sui.executeTransactionBlock(
      sponsoredResponse.txBytes,
      listOf(userSignature, sponsoredResponse.sponsorSignature),
    )

  println(response)
}

@Serializable data class GasRequestManual(val txBytes: String, val sender: String)
