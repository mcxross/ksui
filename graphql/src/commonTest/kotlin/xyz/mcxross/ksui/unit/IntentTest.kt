package xyz.mcxross.ksui.unit

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import xyz.mcxross.ksui.model.AppId
import xyz.mcxross.ksui.model.Intent
import xyz.mcxross.ksui.model.IntentScope
import xyz.mcxross.ksui.model.IntentType
import xyz.mcxross.ksui.model.IntentVersion

class IntentTest :
  StringSpec({
    "Intent helpers use expected defaults" {
      val intent = Intent.suiTransaction()
      intent.scope shouldBe IntentScope.TRANSACTIONDATA
      intent.version shouldBe IntentVersion.V0
      intent.appId shouldBe AppId.SUI
    }

    "IntentType mapping produces expected intent" {
      val intent = IntentType.SUI_TX.intent()
      intent.scope shouldBe IntentScope.TRANSACTIONDATA
      intent.version shouldBe IntentVersion.V0
      intent.appId shouldBe AppId.SUI
    }

    "Intent.personalMessage uses expected scope" {
      val intent = Intent.personalMessage()
      intent.scope shouldBe IntentScope.PERSONALMESSAGE
    }
  })
