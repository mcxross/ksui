package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import xyz.mcxross.bcs.Bcs
import xyz.mcxross.ksui.TestResources.sui
import xyz.mcxross.ksui.core.model.CallArg
import xyz.mcxross.ksui.core.ptb.Argument
import xyz.mcxross.ksui.core.ptb.Command
import xyz.mcxross.ksui.core.ptb.ProgrammableTransaction
import xyz.mcxross.ksui.ptb.ptb
import xyz.mcxross.ksui.randomString

class GraphqlPtbResolutionTest :
  StringSpec({
    val bcs = Bcs

    "GraphQL PTB builder resolves ObjectStr inputs before serialization" {
      val ptb =
        ptb(sui) {
          val ramp = `object`("0x67a60352909987c0a3777d15444e883599ee8799c742aa97b6a23205da29867a")

          val auth = moveCall {
            target =
              "0x9c09daf59b0630762a712a9dd043eb35cec87d5ddbb77452497bdd87392b9b50::p2p_ramp::authenticate"
            arguments = listOf(ramp)
          }

          val params = moveCall {
            target =
              "0x10c87c29ea5d5674458652ababa246742a763f9deafed11608b7f0baea296484::intents::new_params"
            arguments =
              listOf(
                pure(randomString(10)),
                pure("description"),
                pure(listOf(0UL)),
                pure(900000UL),
                `object`("0x6"),
              )
          }

          val outcome = moveCall {
            target =
              "0x9c09daf59b0630762a712a9dd043eb35cec87d5ddbb77452497bdd87392b9b50::p2p_ramp::empty_approved_outcome"
          }

          val members =
            pure(
              listOf(
                "0x7aaec1a24ced4f34d49c27f00b21f5e3c7a9b20f25e57a1fd2863b15abe3a904",
                "0x7aaec1a24ced4f34d49c27f00b21f5e3c7a9b20f25e57a1fd2863b15abe3a902",
              )
            )

          moveCall {
            target =
              "0x9c09daf59b0630762a712a9dd043eb35cec87d5ddbb77452497bdd87392b9b50::config::request_config_p2p_ramp"
            arguments = listOf(auth, params, outcome, ramp, members)
          }
        }

      val bytes = bcs.encodeToByteArray<ProgrammableTransaction>(ptb)
      val deserialized = bcs.decodeFromByteArray<ProgrammableTransaction>(bytes)

      deserialized.inputs.size shouldBe 7
      deserialized.inputs[0].shouldBeInstanceOf<CallArg.Object>()
      deserialized.inputs[1].shouldBeInstanceOf<CallArg.Pure>()

      deserialized.commands.size shouldBe 4

      val cmd0 = deserialized.commands[0]
      cmd0.shouldBeInstanceOf<Command.MoveCall>()
      cmd0.moveCall.function shouldBe "authenticate"
      cmd0.moveCall.arguments[0] shouldBe Argument.Input(0u)

      val cmd4 = deserialized.commands[3]
      cmd4.shouldBeInstanceOf<Command.MoveCall>()
      cmd4.moveCall.function shouldBe "request_config_p2p_ramp"

      val args = cmd4.moveCall.arguments
      args.size shouldBe 5

      args[0].shouldBeInstanceOf<Argument.Result>()
      (args[0] as Argument.Result).commandResult shouldBe 0u

      args[1].shouldBeInstanceOf<Argument.Result>()
      (args[1] as Argument.Result).commandResult shouldBe 1u

      args[2].shouldBeInstanceOf<Argument.Result>()
      (args[2] as Argument.Result).commandResult shouldBe 2u

      args[3] shouldBe Argument.Input(0u)

      args[4] shouldBe Argument.Input(6u)
    }
  })
