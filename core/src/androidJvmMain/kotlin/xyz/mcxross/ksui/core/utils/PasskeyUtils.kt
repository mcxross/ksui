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
package xyz.mcxross.ksui.core.utils

import java.math.BigInteger
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.custom.sec.SecP256R1Curve

object PasskeyUtils {

  fun recoverPublicKeyPoint(
    recId: Int,
    r: BigInteger,
    s: BigInteger,
    message: BigInteger,
    params: ECDomainParameters,
  ): ECPoint? {
    val n = params.n
    val i = BigInteger.valueOf(recId.toLong() / 2)
    val x = r.add(i.multiply(n))
    val curve = params.curve
    val prime = (curve as SecP256R1Curve).q

    if (x >= prime) {
      return null
    }

    val R =
      curve.decodePoint(
        BigInteger.valueOf(2 + (recId % 2).toLong()).toByteArray() +
          x.toByteArray().let {
            if (it.size > 32) it.copyOfRange(it.size - 32, it.size)
            else ByteArray(32 - it.size) + it
          }
      )

    if (!R.multiply(n).isInfinity) {
      return null
    }

    val eInv = n.subtract(message).mod(n)
    val rInv = r.modInverse(n)

    val srInv = s.multiply(rInv).mod(n)
    val eInvRInv = eInv.multiply(rInv).mod(n)

    val q = params.g.multiply(eInvRInv).add(R.multiply(srInv))

    return q
  }
}
