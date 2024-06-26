package xyz.mcxross.ksui.sample

import java.io.File
import java.security.KeyFactory
import java.security.KeyPair
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun saveKeysToFile(file: File, keyPair: KeyPair): Boolean {
  val privateKey = keyPair.private.encoded
  val publicKey = keyPair.public.encoded

  file.writeText("PRIVATE KEY=${Base64.encode(privateKey)}\n")
  file.appendText("PUBLIC KEY=${Base64.encode(publicKey)}\n")

  return true
}

@OptIn(ExperimentalEncodingApi::class)
fun loadKeysFromFile(file: File): KeyPair {
  val lines = file.readLines()
  val privateKeyLine =
    lines.find { it.startsWith("PRIVATE KEY=") }
      ?: throw IllegalArgumentException("Private key not found")
  val publicKeyLine =
    lines.find { it.startsWith("PUBLIC KEY=") }
      ?: throw IllegalArgumentException("Public key not found")

  println("Private Key: ${privateKeyLine.substringAfter("PRIVATE KEY)")}")
  println("Public Key: ${publicKeyLine.substringAfter("PUBLIC KEY)")}")

  val privateKeyData = Base64.decode(privateKeyLine.substringAfter("PRIVATE KEY="))
  val publicKeyData = Base64.decode(publicKeyLine.substringAfter("PUBLIC KEY="))

  val keyFactory = KeyFactory.getInstance("Ed25519", "BC")
  val privateKeySpec = PKCS8EncodedKeySpec(privateKeyData)
  val publicKeySpec = X509EncodedKeySpec(publicKeyData)

  val privateKey = keyFactory.generatePrivate(privateKeySpec)
  val publicKey = keyFactory.generatePublic(publicKeySpec)

  return KeyPair(publicKey, privateKey)
}
