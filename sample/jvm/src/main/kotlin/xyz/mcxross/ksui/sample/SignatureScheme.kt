package xyz.mcxross.ksui.sample

enum class SignatureScheme(
  /**
   * Gets scheme.
   *
   * @return the scheme
   */
  val scheme: Byte
) {
  /** Ed25519 signature scheme. */
  ED25519(0x00.toByte()),
  /** Secp256k1 signature scheme. */
  Secp256k1(0x01.toByte()),
  /** Secp256r1 signature scheme. */
  Secp256r1(0x02.toByte()),
  /** BLS12381 signature scheme. */
  BLS12381(0xff.toByte());

  companion object {
    private val BY_SCHEME: MutableMap<Byte, SignatureScheme> = HashMap()

    init {
      for (e in entries) {
        BY_SCHEME[e.scheme] = e
      }
    }

    /**
     * Value of signature scheme.
     *
     * @param scheme the scheme
     * @return the signature scheme
     */
    fun valueOf(scheme: Byte): SignatureScheme? {
      return BY_SCHEME[scheme]
    }
  }
}
