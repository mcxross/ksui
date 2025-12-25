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
package xyz.mcxross.ksui.sample

import kotlinx.serialization.Serializable

const val GAS_STATION_URL = "http://0.0.0.0:8080/gas"
const val GAS_STATION_API_KEY = "zk_xPFGtrE1ZclSKQN1TRYBfqQ9-B5QJKVrpUu5K_0IhMA"

@Serializable
data class GasRequest(val txBytes: String, val sender: String)

@Serializable
data class SponsoredResponse(val txBytes: String, val sponsorSignature: String)
