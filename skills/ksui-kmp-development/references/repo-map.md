# Ksui Repo Map

## Modules

- `:ksui` is the library module. Its directory is `lib`, but `settings.gradle.kts` renames the project to `ksui`.
- `:sample:jvm` is a JVM sample app that depends on `:ksui`.
- `:sample:android` is an Android Compose sample app that depends on `:ksui`.

## Build Files

- `settings.gradle.kts` configures plugin repositories, dependency repositories, module includes, and the project rename.
- `build.gradle.kts` declares shared plugin aliases.
- `lib/build.gradle.kts` configures Kotlin Multiplatform, Android library settings, source sets, Apollo, Dokka, Kotest, and Maven publishing.
- `gradle/libs.versions.toml` is the source of truth for plugin and dependency versions.
- `.github/workflows/build.yml` runs `./gradlew :ksui:jvmTest`.
- `.github/workflows/docs-publish.yml` runs `./gradlew generateApolloSources` and `./gradlew dokkaGenerate`.
- `.github/workflows/release.yml` runs `./scripts/release` with Sonatype signing credentials.

## Source Sets

- `lib/src/commonMain/kotlin` holds portable SDK code.
- `lib/src/commonMain/graphql` holds Apollo GraphQL operation files and `schema.graphqls`.
- `lib/src/commonTest/kotlin` holds unit and e2e tests for the library.
- `lib/src/jvmMain`, `androidMain`, `androidJvmMain`, `jsMain`, `appleMain`, `nativeMain`, and target-specific directories hold platform actuals and engine/crypto/passkey implementations.
- `lib/src/jvmTest/proto` holds Sui RPC v2 beta protobuf definitions and google protobuf/rpc dependencies used as test/reference material.

## Main Package Areas

- `Sui.kt` composes the public `Sui` client from domain protocols and implementations.
- `client` creates platform HTTP clients and Apollo clients.
- `protocol` contains public interfaces.
- `api` contains public interface implementations.
- `internal` contains GraphQL calls, transaction signing/execution helpers, and conversions.
- `model` contains public data types, options, filters, transaction data, and config.
- `serializer` contains kotlinx serialization support for model and transaction types.
- `ptb` contains programmable transaction builder and DSL types.
- `dsl` contains higher-level DSLs such as sponsored PTBs.
- `account` and `core/crypto` contain account abstractions, key types, signatures, passkeys, and platform crypto.
- `util` contains endpoints, constants, encoders, logging, and helpers.

## gRPC And Protobuf Context

- The repo currently has Sui RPC v2 beta proto definitions under `lib/src/jvmTest/proto/sui/rpc/v2beta2`, but no production gRPC generator/runtime dependency in `gradle/libs.versions.toml` or `lib/build.gradle.kts`.
- Treat those proto files as the source of truth for gRPC service and message shapes until a real client is wired in.
- If adding a production gRPC client, first decide whether it is JVM-only or KMP-capable. Do not leak JVM-only stubs into `commonMain`.
- Keep generated protobuf/gRPC sources in generated source directories, not hand-written Kotlin directories.
- Use `lib/src/jvmTest/proto/sui/rpc/v2beta2/README.md` for shared API conventions: human-readable address/object/digest/type encodings, `read_mask` field masks, `page_size` and `page_token` pagination, proto3 optional presence, richer errors through `grpc-status-details-bin`, and Sui response headers.
- Main Sui RPC v2 beta services in the proto set are `LedgerService`, `LiveDataService`, `MovePackageService`, `TransactionExecutionService`, `SignatureVerificationService`, and `SubscriptionService`.

## Common Commands

```bash
./gradlew :ksui:jvmTest
./gradlew generateApolloSources
./gradlew dokkaGenerate
./gradlew :sample:jvm:run
./gradlew :sample:android:assembleDebug
./gradlew publishToMavenLocal
```

Prefer the narrowest command that exercises the touched module, then run `:ksui:jvmTest` when practical.
