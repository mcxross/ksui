# Module Ksui

Ksui is a Kotlin Multiplatform library for interacting with Sui Full node. It maintains parity with Sui data types and
exposes a neat DSL-style API for interacting with Sui Full node.

# Table of Contents

- [Installation]()
    * [Multiplatform]()
    * [Android]()
    * [JS]()
    * [JVM]()
- [Client]()
    * [Types of Clients]()
    * [Create Client]()
- [Transactions]()
    * [Create Transaction]()
    * [Sign Transaction]()
    * [Send Transaction]()
    * [Get Transaction]()

# Installation

Ksui uses Gradle as its build tool.
The artifacts are published to Maven Central, so you must set `mavenCentral()` as one of your repositories.

## Multiplatform

Add the `Ksui` dependency to the common sourceSet in your `build.gradle.kts` file.

```kotlin
implementation("xyz.mcxross.ksui:ksui:<$latest_ksui_version>")

```

## Android

```kotlin
implementation("xyz.mcxross.ksui:ksui-android:<$latest_ksui_version>")
```

## JS

```kotlin
implementation("xyz.mcxross.ksui:ksui-js:<$latest_ksui_version>")
```

## JVM

```kotlin
implementation("xyz.mcxross.ksui:ksui-jvm:<$latest_ksui_version>")
```

# Client

`Sui` is the GraphQL module's entry point. Its `SuiConfig` selects the network or a custom indexer endpoint.
Queries and mutations use HTTP. Subscriptions use the same endpoint over a WebSocket with the
`graphql-transport-ws` protocol.

## Create Client

```kotlin
val sui = Sui(SuiConfig(SuiSettings(network = Network.MAINNET)))
```

## Subscriptions

`subscribeCheckpoints`, `subscribeTransactionBlocks`, and `subscribeEvents` return cold flows of
`Result` values. A subscription starts when its flow is collected and its WebSocket closes when
collection is cancelled. Each delivered edge contains a cursor. Persist a cursor after processing
its edge and pass it as `after` when starting a new stream to backfill missed data.

Sui currently provides GraphQL subscriptions on Mainnet and Testnet.

# Transactions

## Create Transaction

### Programmable Transaction

The primitives for creating a programmable transaction are **inputs** and **commands**.
**inputs** are, well, the inputs to a **command**. They can be either a literal value or a reference to the result of a
**command**.
**commands** are the building blocks of a programmable transaction. They are the operations that are performed on the
inputs.

Ksui provides a DSL-style API for creating programmable transactions. It models the primitives as constructs i.e.
**input** and **command** are constructs exposed as `input` and `command` respectively. DSL constructs are structured,
expressive, and easy to read. The top most DSL construct is the `programmableTx`. It creates a transaction block. Here,
you have access to
the `command` construct, which is the only required
construct for creating a programmable transaction block. In the `command` construct, you have access to the various
command creator functions. Every function returns a reference to the result of the command. This reference can be passed
as an `input` to other command creator functions.

For example, to create a transaction block that splits a coin into three coins of different amounts and then transfer
these to a given address, you can do the following:

```kotlin
val ptb = programmableTx {
  command {
    val splitCoins = splitCoins {
      coin = Argument.GasCoin
      into = inputs(1_000_000, 500_000, 5_000_000)
    }

    transferObjects {
      objects = inputs(splitCoins)
      to = input("0x4afc81d797fd02bd7e923389677352eb592d55a00b65067fa582c05f62b4788b")
    }
  }
}

```

The coin we are splitting is `Argument.GasCoin`, which is a reference to the `GAS` coin, the coin used to pay for gas.
We're splitting it into three coins of amounts `1_000_000`, `500_000`, and `5_000_000`. The `splitCoins` command returns
a reference to the result of the command, which we pass as an `input` to the `transferObjects` command.

## Get Transaction

Get a transaction by its digest.

```kotlin
val transactionBlockResponse = suiHttpClient.getTransactionBlock(
    digest = "D8KMrY8z83yprmEkWFHEgZkjVW48ctP8VvAUMqpB9fRC",
    options =
    TransactionBlockResponseOptions(
        showInput = true,
        showRawInput = false,
    )
)
```

Get a list of transactions for a specified query criteria.

```kotlin
val transactionBlocksPage = suiHttpClient.queryTransactionBlocks(
    query =
    TransactionBlockResponseQuery(
        options =
        TransactionBlockResponseOptions(
            showInput = true,
            showRawInput = false,
        )
    ),
    limit = 10,
    descendingOrder = true,
)
```

Get an ordered list of transaction responses

```kotlin
val txnBlockResponseList =
    suiHttpClient.getMultiTransactionBlocks(
        query =
        listOfTxDigests(
            "D8KMrY8z83yprmEkWFHEgZkjVW48ctP8VvAUMqpB9fRC",
            "AcppBEeCQfFWUDrKgLfAgmmMw48ooMjmRsHmhv565bfR",
            "Fmyh1FxxAX2WUdoMU22dHJByvt6Z1NN7TRtdofXC5oSN",
        ),
        options =
        TransactionBlockResponseOptions(
            showInput = true,
        )
    )
```

# Package xyz.mcxross.ksui.client

This package configures the HTTP and WebSocket transports used by the GraphQL client.

# Package xyz.mcxross.ksui.exception

This package contains all the custom exceptions thrown by Ksui. These are mostly Sui wrapper exceptions.

# Package xyz.mcxross.ksui.model

This package contains all the data classes used by Ksui. These are mostly Sui type wrappers.

# Package xyz.mcxross.ksui.util

This package contains all the utility functions used by Ksui.
