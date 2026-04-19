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

enum class TransactionBlockKindInput(public val rawValue: String) {
  SYSTEM_TX("SYSTEM_TX"),

  /** A user submitted transaction block. */
  PROGRAMMABLE_TX("PROGRAMMABLE_TX"),

  /** Auto generated constant for unknown enum values */
  UNKNOWN__("UNKNOWN__");

  fun toGenerated(): xyz.mcxross.ksui.generated.type.TransactionKindInput {
    return xyz.mcxross.ksui.generated.type.TransactionKindInput.safeValueOf(this.rawValue)
  }

  companion object {
    fun fromGenerated(
      value: xyz.mcxross.ksui.generated.type.TransactionKindInput
    ): TransactionBlockKindInput {
      return entries.find { it.rawValue == value.rawValue } ?: UNKNOWN__
    }
  }
}
