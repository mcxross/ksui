package xyz.mcxross.ksui

import xyz.mcxross.ksui.model.SuiConfig
import xyz.mcxross.ksui.protocol.Coin
import xyz.mcxross.ksui.protocol.General
import xyz.mcxross.ksui.protocol.Governance

class Sui(config: SuiConfig = SuiConfig()) :
  Coin by xyz.mcxross.ksui.api.Coin(config),
  Governance by xyz.mcxross.ksui.api.Governance(config),
  General by xyz.mcxross.ksui.api.General(config)
