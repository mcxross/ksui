package xyz.mcxross.ksui.e2e

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.TestResources
import xyz.mcxross.ksui.core.model.Result
import xyz.mcxross.ksui.core.util.runBlocking
import xyz.mcxross.ksui.model.EventFilter

class EventsTest :
  StringSpec({
    val sui = TestResources.sui
    "Query events by sender returns a response on testnet" {
      runBlocking {
        val activeSender =
          when (val result = sui.queryEvents(filter = EventFilter(), first = 1)) {
            is Result.Ok -> {
              val data = requireNotNull(result.value)
              val events = requireNotNull(data.events)
              events.nodes.firstOrNull()?.rPC_EVENTS_FIELDS?.sender?.address?.toString()
            }
            is Result.Err -> fail("Failed to obtain an active event sender")
          }

        val sender = activeSender ?: TestResources.alice.address.toString()
        val filter = EventFilter(sender = sender)
        when (val result = sui.queryEvents(filter = filter, first = 1)) {
          is Result.Ok -> {
            val data = requireNotNull(result.value)
            val events = requireNotNull(data.events)
            if (activeSender != null) events.nodes.isNotEmpty() shouldBe true
            events.nodes.all {
              it.rPC_EVENTS_FIELDS.sender?.address?.toString() == sender
            } shouldBe true
          }
          is Result.Err -> {
            fail("Failed to query events")
          }
        }
      }
    }
  })
