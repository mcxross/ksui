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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationResponse(
  @SerialName("rawId") val rawId: String,
  @SerialName("type") val type: String,
  @SerialName("id") val id: String,
  @SerialName("response") val response: AuthenticationCredentialResponse,

  // Represents the clientExtensionResults, often an empty object {}.
  @SerialName("clientExtensionResults") val clientExtensionResults: Map<String, String>,
)

@Serializable
data class AuthenticationCredentialResponse(
  @SerialName("clientDataJSON") val clientDataJSON: String,
  val signature: String,
  @SerialName("authenticatorData") val authenticatorData: String,
)
