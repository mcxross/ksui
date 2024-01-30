package xyz.mcxross.ksui.extension

import xyz.mcxross.ksui.model.CoinData
import xyz.mcxross.ksui.model.ObjectReference

fun CoinData.asObjectReference() = ObjectReference(this.coinObjectId, this.version, this.digest)
