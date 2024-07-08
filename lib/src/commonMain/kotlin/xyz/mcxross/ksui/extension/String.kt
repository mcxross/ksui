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
package xyz.mcxross.ksui.extension

import xyz.mcxross.ksui.model.AccountAddress
import xyz.mcxross.ksui.model.Digest
import xyz.mcxross.ksui.model.ObjectDigest
import xyz.mcxross.ksui.model.Reference
import xyz.mcxross.ksui.model.SuiAddress
import xyz.mcxross.ksui.util.formatSuiDomain
import xyz.mcxross.ksui.util.idToParts

fun String.toReference() = Reference(AccountAddress(this))

fun String.toObjectDigest() = ObjectDigest(Digest(this))

/** Extension function to create a [SuiAddress] from a [String]. */
fun String.toSuiAddress() = SuiAddress(this)

fun String.formatAsSuiDomain() = formatSuiDomain(this)

fun String.asIdParts() = idToParts(this)

data class IdParts(val packageId: String, val module: String, val function: String)

fun Triple<String, String, String>.toId() = IdParts(first, second, third)
