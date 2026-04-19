package xyz.mcxross.ksui.core.model

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
