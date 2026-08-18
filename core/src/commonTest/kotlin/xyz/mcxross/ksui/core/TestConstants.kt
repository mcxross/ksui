package xyz.mcxross.ksui.core

import xyz.mcxross.ksui.core.crypto.Ed25519PrivateKey

/** Deterministic local-only unit-test key. It is never used by network-connected tests. */
val PRIVATE_KEY_DATA = Ed25519PrivateKey(ByteArray(32) { (it + 1).toByte() }).export()
