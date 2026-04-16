# Ksui GraphQL API Layering

## Domain Files

Each domain generally has a matching trio:

- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/protocol/<Domain>.kt`
- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/api/<Domain>.kt`
- `lib/src/commonMain/kotlin/xyz/mcxross/ksui/internal/<Domain>.kt`

Current domains include `Coin`, `Events`, `Faucet`, `General`, `Governance`, `Move`, `Object`, `Sns`, and `Transaction`.

## GraphQL Files

- Operations live in `lib/src/commonMain/graphql/*.graphql`.
- Schema lives in `lib/src/commonMain/graphql/schema.graphqls`.
- Apollo generates Kotlin types under package `xyz.mcxross.ksui.generated`.
- Regenerate generated types with `./gradlew generateApolloSources`.

## Implementation Pattern

Internal functions accept `SuiConfig`, convert SDK model values into GraphQL variables, call Apollo, and return `xyz.mcxross.ksui.model.Result`.

Use this shape when adding simple query methods:

```kotlin
internal suspend fun getSomething(
  config: SuiConfig,
  value: String?,
): Result<GetSomethingQuery.Data?, SuiError> =
  handleQuery {
      getGraphqlClient(config)
        .query(GetSomethingQuery(value = Optional.presentIfNotNull(value)))
    }
    .toResult()
```

Then expose it through:

- A `suspend fun` in `protocol/<Domain>.kt`, with defaults there.
- An override in `api/<Domain>.kt` delegating to the internal function.
- `Sui.kt` only if a new domain interface is being added.

## Config And Errors

- `SuiConfig.getRequestUrl(SuiApiType.INDEXER)` provides the GraphQL endpoint through `getGraphqlClient`.
- Headers for the GraphQL client come from `SuiConfig.getHeaders(SuiApiType.INDEXER)`.
- `handleQuery` maps Apollo errors to `SuiError`; keep that path for normal GraphQL queries and mutations.
- Use `Result.Ok` and `Result.Err` from `xyz.mcxross.ksui.model.Result` at the public boundary.

## Tests

- Unit tests live in `lib/src/commonTest/kotlin/xyz/mcxross/ksui/unit`.
- Network-facing e2e tests live in `lib/src/commonTest/kotlin/xyz/mcxross/ksui/e2e`.
- Add tests for filters, option defaults, pagination cursors, and model conversions when those mappings change.
