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

/**
 * Object data options
 *
 * This class represents the options for object data
 *
 * @property showBcs Show the BCS
 * @property showContent Show the content
 * @property showDisplay Show the display
 * @property showType Show the type
 * @property showOwner Show the owner
 * @property showPreviousTransaction Show the previous transaction
 * @property showStorageRebate Show the storage rebate
 */
data class ObjectDataOptions(
  val showBcs: Boolean = true,
  val showContent: Boolean = true,
  val showDisplay: Boolean = true,
  val showType: Boolean = true,
  val showOwner: Boolean = false,
  val showPreviousTransaction: Boolean = true,
  val showStorageRebate: Boolean = true,
)
