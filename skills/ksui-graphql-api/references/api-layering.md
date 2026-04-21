# Ksui GraphQL API Layering

## Domain Files

Each domain generally has a matching trio:

- `graphql/src/commonMain/kotlin/xyz/mcxross/ksui/protocol/<Domain>.kt`
- `graphql/src/commonMain/kotlin/xyz/mcxross/ksui/api/<Domain>.kt`
- `graphql/src/commonMain/kotlin/xyz/mcxross/ksui/internal/<Domain>.kt`

Current domains include `Coin`, `Events`, `Faucet`, `General`, `Governance`, `Move`, `Object`, `Sns`, and `Transaction`.

## GraphQL Files

- Operations live in `graphql/src/commonMain/graphql/*.graphql`.
- Schema lives in `graphql/src/commonMain/graphql/schema.graphqls`.
- Apollo generates Kotlin types under package `xyz.mcxross.ksui.generated`.
- Regenerate generated types with `./gradlew generateApolloSources`.

## GraphQL And gRPC Shape

- The GraphQL schema explicitly ties some transaction JSON fields to the Sui RPC v2 gRPC proto schema.
- `simulateTransaction(transaction: JSON!, checksEnabled: Boolean, doGasSelection: Boolean)` accepts JSON matching the Sui RPC v2 `Transaction` schema, or BCS input as `{"bcs": {"value": "<base64>"}}`.
- `Transaction.transactionJson`, `TransactionEffects.effectsJson`, and `TransactionEffects.balanceChangesJson` are documented as matching the gRPC proto format, excluding BCS where noted.
- The reference proto files live in `grpc/src/commonMain/proto/sui/rpc/v2`. Check `transaction.proto`, `executed_transaction.proto`, `effects.proto`, `balance_change.proto`, and `transaction_execution_service.proto` when mapping transaction JSON through GraphQL.
- The proto API conventions in `grpc/src/commonMain/proto/sui/rpc/v2/README.md` apply when mirroring gRPC-shaped data.

## Implementation Pattern

Internal functions accept `SuiConfig`, convert SDK model values into GraphQL variables, call Apollo, and return `xyz.mcxross.ksui.core.model.Result`.

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
- Use `Result.Ok` and `Result.Err` from `xyz.mcxross.ksui.core.model.Result` at the public boundary.
- Do not assume GraphQL JSON and BCS forms are interchangeable. The schema documents separate BCS wrappers and gRPC-shaped JSON forms.

## Tests

- Unit tests live in `graphql/src/commonTest/kotlin/xyz/mcxross/ksui/unit`.
- Network-facing e2e tests live in `graphql/src/jvmTest/kotlin/xyz/mcxross/ksui/e2e`.
- Add tests for filters, option defaults, pagination cursors, and GraphQL model conversions when those mappings change. Shared config/model/PTB behavior belongs in `core/src/commonTest`.
