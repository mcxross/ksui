package xyz.mcxross.ksui.extension

import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.CoinData
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.model.ObjectDigest
import xyz.mcxross.ksui.model.ObjectReference
import xyz.mcxross.ksui.model.Reference

fun CoinData.asObjectReference() =
  ObjectReference(
    Reference(AccountAddress(this.coinObjectId)),
    this.version,
    ObjectDigest(Digest(this.digest)),
  )
