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

import java.math.BigInteger
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.crypto.ec.CustomNamedCurves

internal fun decodeDerEcdsaSignature(derSignature: ByteArray): Pair<BigInteger, BigInteger> {
  try {
    ASN1InputStream(derSignature).use { stream ->
      val root =
        stream.readObject() ?: throw IllegalArgumentException("DER signature must not be empty")
      require(stream.readObject() == null) { "DER signature contains trailing bytes" }
      val seq =
        root as? ASN1Sequence
          ?: throw IllegalArgumentException("DER signature must be an ASN.1 sequence")
      require(seq.size() == 2) { "DER signature sequence must contain exactly 2 integers" }
      val r =
        (seq.getObjectAt(0) as? ASN1Integer)?.value
          ?: throw IllegalArgumentException("Invalid ECDSA r integer")
      val s =
        (seq.getObjectAt(1) as? ASN1Integer)?.value
          ?: throw IllegalArgumentException("Invalid ECDSA s integer")
      val canonical = DERSequence(arrayOf(ASN1Integer(r), ASN1Integer(s))).encoded
      require(canonical.contentEquals(derSignature)) { "Signature is not canonical DER" }
      return r to s
    }
  } catch (e: IllegalArgumentException) {
    throw e
  } catch (e: Exception) {
    throw IllegalArgumentException("Malformed DER signature", e)
  }
}

fun normalizeSignature(derSignature: ByteArray): ByteArray {
  val (r, rawS) = decodeDerEcdsaSignature(derSignature)
  val curveParams = CustomNamedCurves.getByName("secp256r1")
  val n = curveParams.n
  require(r > BigInteger.ZERO && r < n) { "ECDSA r value out of range" }
  require(rawS > BigInteger.ZERO && rawS < n) { "ECDSA s value out of range" }

  val halfCurveOrder = n.shiftRight(1)
  val s = if (rawS > halfCurveOrder) n.subtract(rawS) else rawS
  return toFixedUnsigned32Bytes(r) + toFixedUnsigned32Bytes(s)
}

private fun toFixedUnsigned32Bytes(value: BigInteger): ByteArray {
  val raw = value.toByteArray()
  val unsigned = if (raw.size == 33 && raw[0] == 0.toByte()) raw.copyOfRange(1, 33) else raw
  require(unsigned.size <= 32) { "ECDSA scalar exceeds 32 bytes" }
  return if (unsigned.size == 32) unsigned else ByteArray(32 - unsigned.size) + unsigned
}
