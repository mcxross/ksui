---
name: ksui-ptb-crypto
description: "Workflow for Ksui programmable transaction blocks, accounts, signatures, private keys, BCS serialization, passkeys, sponsored transactions, gRPC transaction schemas, and platform crypto. Use when editing ptb, account, core/crypto, dsl/SponsoredPtb, transaction signing/execution, serializers for transaction data, or tests around PTB building and signing."
---

# Ksui PTB and Crypto

## Overview

Use this skill for the parts of Ksui that construct transaction data, sign messages, encode BCS payloads, or bridge platform crypto/passkey implementations.

Read [references/ptb-crypto-map.md](references/ptb-crypto-map.md) for the transaction, account, crypto, gRPC proto, platform, and test file map.

## Workflow

1. Determine whether the change is in PTB construction, transaction data modeling, signing, key handling, passkeys, or platform crypto.
2. Keep portable transaction, account, serializer, and model logic in `commonMain` unless a platform API is required.
3. Use `expect`/`actual` only for platform-dependent crypto/client behavior already modeled by the repo.
4. Preserve Sui wire formats: BCS encoding, intent hashing, signature flag prefixes, Bech32 private keys, and transaction response option mappings.
5. Check Sui RPC v2 beta proto shapes before changing transaction JSON, simulate/execute transaction payloads, user signatures, or signature verification behavior.
6. Add focused unit tests before e2e tests for builder shape, serialization, key import/export, signature verification, and option/filter conversion.
7. Run `./gradlew :ksui:jvmTest` after local changes; add platform compile tasks when touching non-JVM actual implementations.

## Guardrails

- Do not silently change serialized transaction layouts or signature byte composition.
- Keep `Account.import` behavior aligned with `PrivateKey.fromEncoded` and SIP-15 Bech32 private key handling.
- Preserve `SignatureScheme` flag usage when composing signatures.
- Treat passkey signatures specially where the code already does; do not prepend regular scheme/public key bytes unless required by the passkey model.
- Keep proto `optional` presence semantics in mind when mapping transaction JSON or future generated gRPC models.
- Keep the top-level `ptb { ... }` DSL ergonomic and validation errors explicit with `require`.
- Prefer extending existing builders/scopes over creating parallel transaction construction APIs.

## Validation Targets

Use existing tests as anchors:

- `unit/CommandTest.kt`, `unit/TransactionDataTest.kt`, `unit/SponsoredPtbTest.kt`, and `unit/SerializationTest.kt` for PTB and transaction models.
- `unit/AccountTest.kt`, `unit/PrivateKeyTest.kt`, `unit/CryptoModelTest.kt`, `unit/AccountAddressPublicKeyTest.kt`, and `unit/DigestTest.kt` for crypto and accounts.
- `e2e/TransactionTest.kt` and `e2e/DryRunTest.kt` only when behavior requires live Sui interaction.

```bash
./gradlew :ksui:jvmTest
```

For platform-specific edits, also run the closest compile task available for that target when the local environment supports it.
