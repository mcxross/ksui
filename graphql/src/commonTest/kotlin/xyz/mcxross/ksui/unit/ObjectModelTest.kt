package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.ObjectId

class ObjectModelTest :
  StringSpec({
    "ObjectId uses AccountAddress string form" {
      val address = AccountAddress.fromString("0x1")
      val objectId = ObjectId(address)
      objectId.toString() shouldBe address.toString()
    }
  })
