# Module Ksui

Ksui, `/kei'su:i:/` (pronounced as "kay-soo-ee"), is a collection of Multiplatform Kotlin language JSON-RPC wrapper and
crypto utilities for interacting with Sui Full node.

# Table of Contents

- [Installation](#installation)
    * [Multiplatform](#multiplatform)
    * [Android](#android)
    * [JS](#js)
    * [JVM](#jvm)

- [Client](#client)
    * [Types of Clients](#types-of-clients)
    * [Create Client](#create-client)

- [Transactions](#transactions)
    * [Create Transaction](#create-transaction)
    * [Sign Transaction](#sign-transaction)
    * [Send Transaction](#send-transaction)
    * [Get Transaction](#get-transaction)

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

Ksui provides two configurable clients for interacting with Sui Full node.
The encouraged way to create a client is to use the DSL-style builder functions. Both clients share some common
configurations, but also have their own unique configurations that are ignored by the other client if specified.

## Types of Clients

* Http Client

This client uses the JSON-RPC over HTTP protocol to communicate with the Sui Full nodes specified by the `endpoint`
property in the `suiHttpClient` builder function.

* Websocket Client

This client uses the JSON-RPC over Websocket protocol to communicate with the Sui Full node specified by
the `endpoint` property in the `suiWebsocketClient` builder function.

It main serves as a subscription client for listening to events on the Sui Full node.

## Create Client

To create a Http Client, use the `suiHttpClient` builder function.
It takes a lambda with a receiver of type `SuiHttpClientConfig` as an argument for customizing the client.
The builder function returns an instance of `SuiHttpClient`.

```kotlin
val suiHttpClient = suiHttpClient {
    endpoint = EndPoint.MAINNET
    maxRetries = 10
    connectionTimeout = 100000
}
```

# Transactions

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
        listOfTnxDigests(
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