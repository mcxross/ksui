---
name: ksui-kmp-development
description: "Kotlin Multiplatform development workflow for the Ksui repository. Use when changing repo structure, Gradle builds, source sets, CI, docs, samples, dependencies, publishing, protobuf or gRPC client setup, or when asked to build, test, troubleshoot, or make a broad change in this Ksui SDK repo."
---

# Ksui KMP Development

## Overview

Use this skill to work in the Ksui Kotlin Multiplatform SDK without rediscovering the repo layout, source set boundaries, or validation commands.

Read [references/repo-map.md](references/repo-map.md) when you need the module map, source set map, Gradle tasks, CI contract, gRPC/protobuf context, or release/doc commands.

## Workflow

1. Start from the repo root that contains `settings.gradle.kts`.
2. Identify the smallest affected module and source set before editing.
3. Prefer `core/src/commonMain` and `core/src/commonTest` for transport-independent models, serialization, crypto, and PTB behavior.
4. Prefer `graphql/src/commonMain` and `graphql/src/commonTest` for Apollo operations, GraphQL adapters, portable GraphQL unit tests, and the `Sui` entry point.
5. Prefer `grpc/src/commonMain` and `grpc/src/jvmTest` for proto/gRPC client behavior.
6. Keep public package names under `xyz.mcxross.ksui` and preserve the existing split between `protocol`, `api`, `internal`, `model`, `serializer`, `ptb`, `account`, `core/crypto`, and `client`.
7. Add or update tests close to the touched behavior. Use core tests for shared types/builders/config, GraphQL common tests for Apollo/generated mapping, GraphQL JVM tests for network-facing GraphQL behavior, and gRPC JVM tests for gRPC endpoint/header/runtime behavior.
8. Run the narrowest Gradle task that proves the change, then run the main gate when practical.

## Editing Rules

- Use the Gradle version catalog in `gradle/libs.versions.toml` for dependencies and plugin versions.
- Keep Gradle changes in Kotlin DSL and follow the existing two-space indentation style.
- Do not move generated Apollo or future generated gRPC/protobuf sources into hand-written source sets.
- Do not introduce JVM-only APIs into `commonMain`.
- Keep generated protobuf/gRPC sources in generated source directories and proto files under `grpc/src/commonMain/proto`.
- Keep documentation updates near the user-facing API surface: README for quick-start behavior, Dokka/KDoc for public SDK functions, and module docs such as `graphql/Module.md`.
- Treat `sample/jvm` and `sample/android` as usage examples, not as the source of SDK behavior.

## Validation

Use these commands from the repo root:

```bash
./gradlew :ksui-core:jvmTest
./gradlew :ksui:jvmTest
./gradlew :ksui-grpc:jvmTest
./gradlew generateApolloSources
./gradlew dokkaGenerate
./gradlew :sample:jvm:run
./gradlew :sample:android:assembleDebug
```

The CI build gate is `./gradlew :ksui-core:jvmTest :ksui:jvmTest :ksui-grpc:jvmTest` on macOS with JDK 17. Run platform-specific compilation only when the touched source set requires it.

If a command fails because it needs network, Android SDK, Xcode/Konan, or browser tooling, report the exact missing requirement and continue with the strongest local validation available.
