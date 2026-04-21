# Ksui Repo Map

## Modules

- `:ksui-core` is the transport-independent core module in `core`, published as `ksui-core`.
- `:ksui` is the GraphQL SDK module in `graphql`, published as `ksui`.
- `:ksui-grpc` is the gRPC SDK module in `grpc`, published as `ksui-grpc`.
- `:sample:jvm` is a JVM sample app that depends on `:ksui`.
- `:sample:android` is an Android Compose sample app that depends on `:ksui`.

## Build Files

- `settings.gradle.kts` configures plugin repositories, dependency repositories, module includes, and the project rename.
- `build.gradle.kts` declares shared plugin aliases.
- `core/build.gradle.kts` configures the core Kotlin Multiplatform module, Android library settings, source sets, Dokka, Kotest, and Maven publishing.
- `graphql/build.gradle.kts` configures the GraphQL Kotlin Multiplatform module, Apollo, platform HTTP engines, Dokka, Kotest, and Maven publishing.
- `grpc/build.gradle.kts` configures the gRPC Kotlin Multiplatform module, proto generation, kotlinx-rpc, Dokka, Kotest, and Maven publishing.
- `gradle/libs.versions.toml` is the source of truth for plugin and dependency versions.
- `.github/workflows/build.yml` runs `./gradlew :ksui-core:jvmTest :ksui:jvmTest :ksui-grpc:jvmTest`.
- `.github/workflows/docs-publish.yml` runs `./gradlew generateApolloSources` and `./gradlew dokkaGenerate`.
- `.github/workflows/release.yml` runs `./scripts/release` with Sonatype signing credentials.

## Source Sets

- `core/src/commonMain/kotlin` holds transport-independent SDK types: models, config, accounts, crypto, serializers, PTB builders, and utility helpers.
- `core/src/commonTest/kotlin` holds tests for shared model, config, crypto, serialization, and PTB behavior.
- `graphql/src/commonMain/kotlin` holds the GraphQL `Sui` client, protocol/api/internal layers, GraphQL-specific filters, GraphQL PTB resolver, and sponsored PTB DSL.
- `graphql/src/commonMain/graphql` holds Apollo GraphQL operation files and `schema.graphqls`.
- `graphql/src/commonTest/kotlin` holds portable GraphQL unit tests.
- `graphql/src/jvmTest/kotlin` holds network-facing GraphQL e2e tests and JVM-only GraphQL fixtures.
- `grpc/src/commonMain/kotlin` holds `SuiGrpcClient`, gRPC protocol/api/internal layers, and runtime support.
- `grpc/src/commonMain/proto` holds Sui RPC v2 protobuf definitions and google protobuf/rpc dependencies used by the gRPC module.
- `grpc/src/jvmTest/kotlin` holds gRPC unit and e2e tests.

## Main Package Areas

- `core/model`, `core/serializer`, `core/ptb`, `core/account`, `core/crypto`, and `core/util` are the transport-independent foundation.
- `graphql/Sui.kt` composes the public GraphQL `Sui` client from domain protocols and implementations.
- `graphql/client` creates platform HTTP clients and Apollo clients.
- `graphql/protocol`, `graphql/api`, and `graphql/internal` contain GraphQL public interfaces, implementations, Apollo calls, and conversions.
- `graphql/model` contains GraphQL-generated type adapters such as filters and enum wrappers.
- `graphql/ptb` contains the GraphQL-backed object/function resolver for PTB building.
- `graphql/dsl` contains higher-level GraphQL-backed DSLs such as sponsored PTBs.
- `grpc/SuiGrpcClient.kt` composes the public gRPC client from domain protocols and implementations.
- `grpc/protocol`, `grpc/api`, and `grpc/internal` contain gRPC public interfaces, implementations, proto calls, and runtime support.

## gRPC And Protobuf Context

- The repo has Sui RPC v2 proto definitions under `grpc/src/commonMain/proto/sui/rpc/v2`.
- The gRPC module uses kotlinx-rpc protobuf/gRPC generation from `grpc/build.gradle.kts`.
- Do not leak JVM-only stubs into `commonMain`.
- Keep generated protobuf/gRPC sources in generated source directories, not hand-written Kotlin directories.
- Use `grpc/src/commonMain/proto/sui/rpc/v2/README.md` for shared API conventions.
- Main Sui RPC v2 services in the proto set include `LedgerService`, `MovePackageService`, `NameService`, `StateService`, `TransactionExecutionService`, `SignatureVerificationService`, and `SubscriptionService`.

## Common Commands

```bash
./gradlew :ksui-core:jvmTest
./gradlew :ksui:jvmTest
./gradlew :ksui-grpc:jvmTest
./gradlew generateApolloSources
./gradlew dokkaGenerate
./gradlew :sample:jvm:run
./gradlew :sample:android:assembleDebug
./gradlew publishToMavenLocal
```

Prefer the narrowest command that exercises the touched module, then run all three library JVM test tasks when practical.
