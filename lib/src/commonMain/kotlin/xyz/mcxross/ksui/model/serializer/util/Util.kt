package xyz.mcxross.ksui.model.serializer.util

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import xyz.mcxross.ksui.model.Owner

fun whichOwner(jsonObject: JsonObject?) : Owner {
  if (jsonObject == null) return Owner.AddressOwner("default")
  return if (jsonObject.containsKey("AddressOwner")) {
    Owner.AddressOwner(jsonObject["AddressOwner"]!!.jsonPrimitive.content)
  } else if (jsonObject.containsKey("Shared")) {
    Owner.SharedOwner(Owner.SharedOwner.Shared(jsonObject["Shared"]!!.jsonObject["initial_shared_version"]!!.jsonPrimitive.long))
  } else if (jsonObject.containsKey("ObjectOwner")) {
    Owner.ObjectOwner(jsonObject["ObjectOwner"]!!.jsonPrimitive.content)
  } else {
    Owner.AddressOwner("default")
  }
}
