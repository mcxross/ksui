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
import xyz.mcxross.ksui.core.Hex
import xyz.mcxross.ksui.core.crypto.Hash
import xyz.mcxross.ksui.core.crypto.PublicKey
import xyz.mcxross.ksui.core.crypto.SignatureScheme
import xyz.mcxross.ksui.core.crypto.hash
import xyz.mcxross.ksui.serializer.AccountAddressSerializer

/** Represents an account address. */
@Serializable(with = AccountAddressSerializer::class)
data class AccountAddress(val data: ByteArray) {

  constructor(s: String) : this(fromString(s).data)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as AccountAddress

    return data.contentEquals(other.data)
  }

  override fun hashCode(): Int {
    return data.contentHashCode()
  }

  override fun toString(): String {
    return Hex(data).toString()
  }

  companion object {

    private const val LENGTH: Int = 32

    val EMPTY = AccountAddress(ByteArray(LENGTH))

    fun from(data: ByteArray): AccountAddress {
      require(data.size == LENGTH + 1) {
        "Address must be $LENGTH bytes long, but was ${data.size}"
      }
      return AccountAddress(data)
    }

    fun fromString(s: String): AccountAddress {
      return try {
        fromHexLiteral(s)
      } catch (e: Exception) {
        fromHex(s)
      }
    }

    fun fromPublicKey(publicKey: PublicKey): AccountAddress {
      when (publicKey.scheme()) {
        SignatureScheme.ED25519 ->
          require(publicKey.data.size == 32) {
            "Ed25519 public key must be 32 bytes long, but was ${publicKey.data.size}"
          }
        SignatureScheme.Secp256k1 ->
          require(publicKey.data.size == 33) {
            "Secp256k1 public key must be 33 bytes long, but was ${publicKey.data.size}"
          }
        SignatureScheme.Secp256r1 -> require(publicKey.data.size == 33) {
          "Secp256r1 public key must be 33 bytes long, but was ${publicKey.data.size}"
        }
        else ->
          throw IllegalArgumentException("Unsupported public key scheme: ${publicKey.scheme()}")
      }
      return AccountAddress(
        Hex(hash(Hash.BLAKE2B256, byteArrayOf(publicKey.scheme().scheme) + publicKey.data))
          .toByteArray()
      )
    }

    private fun fromHexLiteral(literal: String): AccountAddress {
      require(literal.startsWith("0x")) { "Address must start with 0x" }

      val hexLen = literal.length - 2

      // If the string is too short, pad it
      return if (hexLen < LENGTH * 2) {
        val hexStr = StringBuilder(LENGTH * 2)
        for (i in 0 until LENGTH * 2 - hexLen) {
          hexStr.append('0')
        }
        hexStr.append(literal.substring(2))
        fromHex(hexStr.toString())
      } else {
        fromHex(literal.substring(2))
      }
    }

    private fun fromHex(hex: String): AccountAddress {
      val bytes = ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
      require(bytes.size == LENGTH) { "Address must be $LENGTH bytes long, but was ${bytes.size}" }
      return AccountAddress(bytes)
    }
  }
}
