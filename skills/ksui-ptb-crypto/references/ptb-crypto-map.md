# Ksui PTB And Crypto Map

## Transaction Construction

- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/ptb/PtbDsl.kt` exposes the `ptb { ... }` receiver methods.
- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/ptb/ProgrammableTransaction.kt` contains builder and transaction structures.
- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/ptb/Command.kt` models PTB commands and arguments.
- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/ptb/Extended.kt` contains top-level helpers such as `ptb`.
- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/dsl/SponsoredPtb.kt` builds gasless transaction requests for gas stations.

## Transaction Data And Serialization

- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/model/Transaction.kt` contains transaction data, gas data, expiration, effects/options, and composers.
- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/model/Intent.kt` contains Sui intent message types.
- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/serializer` contains serializers for transaction data, commands, arguments, filters, type tags, gas data, and options.
- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/internal/Transaction.kt` signs, executes, sponsors, and polls transaction blocks.

## gRPC Transaction Context

- Sui RPC v2 beta protobuf definitions live in `lib/src/jvmTest/proto/sui/rpc/v2beta2`.
- `transaction.proto` defines the gRPC `Transaction` shape used by GraphQL transaction JSON fields.
- `transaction_execution_service.proto` defines `TransactionExecutionService.ExecuteTransaction`, which accepts `Transaction`, repeated `UserSignature`, and optional `read_mask`.
- `live_data_service.proto` defines `LiveDataService.SimulateTransaction`, which accepts `Transaction`, optional `read_mask`, `checks`, and `do_gas_selection`.
- `signature_verification_service.proto` defines `SignatureVerificationService.VerifySignature`, which verifies a `Bcs` message and `UserSignature`, optionally against an address and JWK set.
- `executed_transaction.proto`, `effects.proto`, `balance_change.proto`, and `event.proto` define executed transaction response shapes.
- Proto fields are proto3 `optional` by convention in this API, so future model mappings must preserve absence versus default values where it affects behavior.
- Field masks use `read_mask`; pagination uses `page_size`, `page_token`, and `next_page_token`.

## Accounts And Keys

- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/account/Account.kt` is the public account abstraction and import/create entry point.
- `Ed25519Account.kt`, `Secp256k1Account.kt`, `Secp256r1Account.kt`, and `PasskeyAccount.kt` provide concrete account behavior.
- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/core/crypto/PrivateKey.kt` implements SIP-15 Bech32 private key export/import dispatch by signature scheme flag.
- `PublicKey.kt`, `KeyPair.kt`, `SignatureScheme.kt`, `Hash.kt`, and scheme-specific files hold crypto primitives.

## Platform Boundaries

- `core/crypto/Platform.kt` and platform-specific `Platform.*.kt` files contain expect/actual crypto behavior.
- `core/crypto/PasskeyProvider.kt` has platform-specific implementations in Android, JS, JVM, and native source sets.
- `androidJvmMain` contains Android/JVM passkey verification helpers and ED25519 derivation code.

## Signing Flow

`signAndSubmitTransaction` in `internal/Transaction.kt`:

1. Fetches reference gas price and payment coins.
2. Builds `TransactionData` from sender, gas payment, PTB, budget, and gas price.
3. Wraps data in `IntentMessage(Intent.suiTransaction(), txData)`.
4. BCS-encodes and BLAKE2b256-hashes the intent message.
5. Signs with the account.
6. Serializes regular signatures as `scheme flag + signature + public key`.
7. Leaves passkey signatures in their specialized serialized form.
8. Submits `ExecuteTransactionBlockMutation`.

When aligning this flow with gRPC, compare the serialized signature shape to `signature.proto` and the transaction payload shape to `transaction.proto`. GraphQL `executeTransaction` still uses base64 BCS plus signatures today, while GraphQL `simulateTransaction` can accept either gRPC-shaped JSON or BCS wrapper input.

## Tests To Reuse

- PTB and transaction model tests: `CommandTest.kt`, `TransactionDataTest.kt`, `SponsoredPtbTest.kt`, `SerializationTest.kt`.
- Crypto and account tests: `AccountTest.kt`, `PrivateKeyTest.kt`, `CryptoModelTest.kt`, `AccountAddressPublicKeyTest.kt`, `DigestTest.kt`.
- E2E transaction tests: `TransactionTest.kt`, `DryRunTest.kt`.
