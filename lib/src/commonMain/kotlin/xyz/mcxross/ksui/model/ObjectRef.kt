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

import xyz.mcxross.ksui.generated.type.ObjectRef

/**
 * @param address ID of the object.
 * @param version Version or sequence number of the object.
 * @param digest Digest of the object.
 */
public data class ObjectRef(
  /** ID of the object. */
  public val address: Any,
  /** Version or sequence number of the object. */
  public val version: Any,
  /** Digest of the object. */
  public val digest: String,
) {
  fun toGenerated(): ObjectRef {
    return ObjectRef(address, version, digest)
  }
}
