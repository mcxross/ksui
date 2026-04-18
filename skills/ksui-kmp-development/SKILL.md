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
3. Prefer `lib/src/commonMain` and `lib/src/commonTest` for portable SDK behavior. Use platform source sets only for engine, crypto, passkey, or platform-specific interop.
4. Keep public package names under `xyz.mcxross.ksui` and preserve the existing split between `protocol`, `api`, `internal`, `model`, `serializer`, `ptb`, `account`, `core/crypto`, and `client`.
5. Add or update tests close to the touched behavior. Use existing unit tests for local models/serializers/builders and e2e tests only for network-facing behavior.
6. Run the narrowest Gradle task that proves the change, then run the main gate when practical.

## Editing Rules

- Use the Gradle version catalog in `gradle/libs.versions.toml` for dependencies and plugin versions.
- Keep Gradle changes in Kotlin DSL and follow the existing two-space indentation style.
- Do not move generated Apollo or future generated gRPC/protobuf sources into hand-written source sets.
- Do not introduce JVM-only APIs into `commonMain`.
- Treat the existing Sui RPC v2 beta proto files under `lib/src/jvmTest/proto` as reference/test fixtures until a production gRPC client generator/runtime is wired into Gradle.
- Keep documentation updates near the user-facing API surface: README for quick-start behavior, Dokka/KDoc for public SDK functions, and `lib/Module.md` for module docs.
- Treat `sample/jvm` and `sample/android` as usage examples, not as the source of SDK behavior.

## Validation

Use these commands from the repo root:

```bash
./gradlew :ksui:jvmTest
./gradlew generateApolloSources
./gradlew dokkaGenerate
./gradlew :sample:jvm:run
./gradlew :sample:android:assembleDebug
```

The CI build gate is `./gradlew :ksui:jvmTest` on macOS with JDK 17. Run platform-specific compilation only when the touched source set requires it.

If a command fails because it needs network, Android SDK, Xcode/Konan, or browser tooling, report the exact missing requirement and continue with the strongest local validation available.
