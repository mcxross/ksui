package xyz.mcxross.ksui.e2e

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.TestResources
import xyz.mcxross.ksui.model.EventFilter
import xyz.mcxross.ksui.model.Result
import xyz.mcxross.ksui.util.runBlocking

class EventsTest :
  StringSpec({
    val sui = TestResources.sui
    val alice = TestResources.alice

    "Query events by sender returns a response on testnet" {
      runBlocking {
        val filter = EventFilter(sender = alice.address.toString())
        when (val result = sui.queryEvents(filter = filter, first = 1)) {
          is Result.Ok -> {
            val data = requireNotNull(result.value)
            val events = requireNotNull(data.events)
            events.nodes.isNotEmpty() shouldBe true
          }
          is Result.Err -> {
            fail("Failed to query events")
          }
        }
      }
    }
  })
