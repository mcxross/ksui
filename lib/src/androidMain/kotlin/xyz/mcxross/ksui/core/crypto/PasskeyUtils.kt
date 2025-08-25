package xyz.mcxross.ksui.core.crypto

import java.math.BigInteger
import java.security.MessageDigest
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DLSequence
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.math.ec.ECPoint

object PasskeyUtils {

  private val params = CustomNamedCurves.getByName("secp256r1")

  private val curveParams = ECDomainParameters(params.curve, params.g, params.n, params.h)
  private val n = curveParams.n
  private val G = curveParams.g

  fun parseDERsignature(signature: ByteArray): Pair<BigInteger, BigInteger> {
    val asn1InputStream = ASN1InputStream(signature)
    val sequence = asn1InputStream.readObject() as DLSequence
    val r = (sequence.getObjectAt(0) as ASN1Integer).value
    val s = (sequence.getObjectAt(1) as ASN1Integer).value
    return Pair(r, s)
  }

  fun recoverPublicKey(
    r: BigInteger,
    s: BigInteger,
    messageHash: ByteArray,
    recoveryId: Int,
  ): ECPoint? {
    val i = BigInteger.valueOf(recoveryId.toLong() / 2)
    val x = r.add(i.multiply(n))

    if (x >= curveParams.curve.field.characteristic) return null

    val R =
      curveParams.curve.decodePoint(x.toByteArray() + byteArrayOf((recoveryId % 2 + 2).toByte()))
    val rInv = r.modInverse(n)
    val e = BigInteger(1, messageHash)
    val s1 = s.multiply(rInv).mod(n)
    val s2 = e.multiply(rInv).mod(n)

    val q = G.multiply(s1).subtract(R.multiply(s2))
    return q
  }

  fun getSignedMessage(authData: ByteArray, clientDataJson: ByteArray): ByteArray {
    val clientDataJsonHash = MessageDigest.getInstance("SHA-256").digest(clientDataJson)
    return authData + clientDataJsonHash
  }
}
