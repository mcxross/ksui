package xyz.mcxross.ksui.sample

class SignatureSchemeNotSupportedException  /** Instantiates a new Signature scheme not supported exception.  */
: RuntimeException("only ed25519 and secp256k1 signature scheme supported.")