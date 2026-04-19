/*
 * Copyright 2025 McXross
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

@Serializable
data class PasskeyAuthenticator(
  val authenticatorData: ByteArray,
  val clientDataJson: String,
  val userSignature: ByteArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as PasskeyAuthenticator

    if (!authenticatorData.contentEquals(other.authenticatorData)) return false
    if (clientDataJson != other.clientDataJson) return false
    if (!userSignature.contentEquals(other.userSignature)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = authenticatorData.contentHashCode()
    result = 31 * result + clientDataJson.hashCode()
    result = 31 * result + userSignature.contentHashCode()
    return result
  }
}
