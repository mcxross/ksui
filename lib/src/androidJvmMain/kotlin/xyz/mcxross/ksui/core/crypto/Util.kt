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
package xyz.mcxross.ksui.core.crypto

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.crypto.ec.CustomNamedCurves

/**
 * Parses a DER-encoded signature, normalizes its 'S' value to prevent malleability, and returns it
 * in a compact 64-byte (r || s) format.
 *
 * @param derSignature The raw signature bytes from the passkey response.
 * @return A 64-byte normalized signature.
 */
fun normalizeSignature(derSignature: ByteArray): ByteArray {
  val asn1InputStream = ASN1InputStream(derSignature)
  val asn1Sequence = asn1InputStream.readObject() as ASN1Sequence

  val r = (asn1Sequence.getObjectAt(0) as ASN1Integer).value
  var s = (asn1Sequence.getObjectAt(1) as ASN1Integer).value

  val curveParams = CustomNamedCurves.getByName("secp256r1")
  val halfCurveOrder = curveParams.n.shiftRight(1)

  if (s > halfCurveOrder) {
    s = curveParams.n.subtract(s)
  }

  // Convert r and s to 32-byte arrays, padding if necessary.
  val rBytes =
    r.toByteArray().let {
      if (it.size > 32) it.copyOfRange(it.size - 32, it.size) else ByteArray(32 - it.size) + it
    }
  val sBytes =
    s.toByteArray().let {
      if (it.size > 32) it.copyOfRange(it.size - 32, it.size) else ByteArray(32 - it.size) + it
    }

  return rBytes + sBytes
}
