/*
 * Copyright 2024 McXross
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.mcxross.ksui.model

import kotlinx.serialization.Serializable
import xyz.mcxross.ksui.util.decodeBase58

@Serializable data class ObjectId(val hash: AccountAddress)

@Serializable
data class ObjectReference(val reference: Reference, val version: Long, val digest: ObjectDigest) {
  companion object {
    fun from(id: AccountAddress, version: Int, digest: String): ObjectReference {
      return ObjectReference(Reference(id), version.toLong(), ObjectDigest(Digest(digest)))
    }
  }
}

@Serializable data class Reference(val accountAddress: AccountAddress)

@Serializable data class ObjectDigest(val digest: Digest)

@Serializable
data class Digest(val data: ByteArray) {

  constructor(data: String) : this(fromString(data).data)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as Digest

    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    return data.contentHashCode()
  }

  override fun toString(): String {
    return data.toString()
  }

  companion object {
    fun fromString(s: String): Digest {
      val buffer = s.decodeBase58()

      if (buffer.size != 32) {
        throw IllegalArgumentException("Digest must be 32 bytes long, but was ${buffer.size}")
      }

      return Digest(buffer)
    }
  }
}

@Serializable
data class TransferredGasObject(val amount: Long, val id: String, val transferTxDigest: String)
