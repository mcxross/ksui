---
name: ksui-graphql-api
description: "Workflow for adding or updating Sui GraphQL-backed APIs in Ksui. Use when editing lib/src/commonMain/graphql, generated Apollo query usage, protocol/api/internal API layers, SuiError result handling, SuiConfig endpoints or headers, or read/query methods for coin, object, events, move, governance, general, transaction, sns, or faucet."
---

# Ksui GraphQL API

## Overview

Use this skill when work touches Ksui's Sui API surface backed by Apollo GraphQL files and the `protocol` -> `api` -> `internal` layering.

Read [references/api-layering.md](references/api-layering.md) for the domain file map, common implementation pattern, and validation commands.

## Layering

For a public API change, update each layer deliberately:

1. `lib/src/commonMain/graphql/*.graphql` contains Apollo operations.
2. `lib/src/commonMain/kotlin/xyz/mcxross/ksui/internal/<Domain>.kt` adapts generated Apollo operations to `Result<..., SuiError>`.
3. `lib/src/commonMain/kotlin/xyz/mcxross/ksui/protocol/<Domain>.kt` declares the public interface.
4. `lib/src/commonMain/kotlin/xyz/mcxross/ksui/api/<Domain>.kt` implements the interface and delegates to `internal`.
5. `lib/src/commonMain/kotlin/xyz/mcxross/ksui/Sui.kt` composes domain interfaces into the `Sui` entry point when adding a new domain.
6. `lib/src/commonTest/kotlin/xyz/mcxross/ksui` covers behavior with unit or e2e tests.

## Implementation Pattern

- Use `getGraphqlClient(config)` from `client/Core.kt`.
- Use `handleQuery { ... }.toResult()` for Apollo query/mutation errors unless the surrounding code has an established exception path.
- Use `Optional.presentIfNotNull(value)` for nullable GraphQL variables.
- Keep public parameters typed with Ksui model types such as `AccountAddress`, `TransactionBlockFilter`, and response option classes.
- Keep generated response types from `xyz.mcxross.ksui.generated` in return values unless introducing an explicit SDK model is already part of the surrounding domain.
- Preserve defaults in the protocol interface, not only in the implementation.

## Validation

Run these from the repo root when GraphQL operations change:

```bash
./gradlew generateApolloSources
./gradlew :ksui:jvmTest
```

For pure Kotlin API changes without new GraphQL documents, `./gradlew :ksui:jvmTest` is the main gate. Add focused tests for option mapping, filters, error handling, pagination, or public defaults when those behaviors change.
