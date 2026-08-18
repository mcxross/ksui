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
import xyz.mcxross.ksui.core.model.GasLessTransactionData
import xyz.mcxross.ksui.core.model.Network
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.model.SuiConfig
import xyz.mcxross.ksui.core.model.SuiSettings
import xyz.mcxross.ksui.core.model.TransactionData
import xyz.mcxross.ksui.core.model.sign
import xyz.mcxross.ksui.core.model.validateSponsoredTransaction
import xyz.mcxross.ksui.core.ptb.ptb
import xyz.mcxross.ksui.core.util.bcsDecode
import xyz.mcxross.ksui.core.util.runBlocking

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
      header("X-API-Key", GAS_STATION_API_KEY)
      setBody(GasRequestManual(txBytes = base64, sender = ALICE_ACCOUNT.address.toString()))
    }

  if (res.status != HttpStatusCode.OK) {
    throw Exception("HTTP error ${res.status}: ${res.bodyAsText()}")
  }

  val sponsoredResponse = res.body<SponsoredResponseManual>()

  val txDataBytes = Base64.decode(sponsoredResponse.txBytes)
  val txData = bcsDecode<TransactionData>(txDataBytes)
  gasLess.validateSponsoredTransaction(txData, SAMPLE_SPONSOR_POLICY)

  val userSignature =
    when (val sig = txData.sign(ALICE_ACCOUNT)) {
      is Result.Ok -> sig.value
      is Result.Err -> throw sig.error
    }

  val response =
    sui.executeTransactionBlock(
      sponsoredResponse.txBytes,
      listOf(userSignature, sponsoredResponse.sponsorSignature),
    )

  println(response)
}

@Serializable data class GasRequestManual(val txBytes: String, val sender: String)
