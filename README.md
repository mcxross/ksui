<h1 align="center">Ksui - Multiplatform SDK for Sui</h1>

Ksui, /keɪˈsuːiː/ (pronounced "kay-soo-ee"), is a Kotlin Multiplatform SDK for
integrating with the Sui blockchain.

It is designed to be type-safe, client-configurable, coroutine based, and usable
across Android, iOS, JS, JVM, and native Kotlin targets.

[![Kotlin Version](https://img.shields.io/badge/Kotlin-v2.3.0-B125EA?logo=kotlin)](https://kotlinlang.org)
[![Docs Publish](https://github.com/mcxross/ksui/actions/workflows/docs-publish.yml/badge.svg)](https://github.com/mcxross/ksui/actions/workflows/docs-publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/xyz.mcxross.ksui/ksui)](https://search.maven.org/artifact/xyz.mcxross.ksui/ksui)
![Snapshot](https://img.shields.io/nexus/s/xyz.mcxross.ksui/ksui?server=https%3A%2F%2Fs01.oss.sonatype.org&label=Snapshot)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

![badge-android](http://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android)
![badge-ios](http://img.shields.io/badge/Platform-iOS-orange.svg?logo=apple)
![badge-js](http://img.shields.io/badge/Platform-NodeJS-yellow.svg?logo=javascript)
![badge-jvm](http://img.shields.io/badge/Platform-JVM-red.svg?logo=openjdk)
![badge-linux](http://img.shields.io/badge/Platform-Linux-lightgrey.svg?logo=linux)
![badge-macos](http://img.shields.io/badge/Platform-macOS-orange.svg?logo=apple)

# Table of contents

- [Features](#features)
- [Modules](#modules)
- [Installation](#installation)
- [Quick start](#quick-start)
- [What's included](#whats-included)
- [Contribution](#contribution)
- [License](#license)

## Features

- Multiplatform Kotlin API
- gRPC and GraphQL clients
- Type-safe Sui models
- Account and key management
- BCS serialization helpers
- Programmable Transaction Block (PTB) builder
- Coroutine based async APIs

## Modules

Ksui is split into transport-independent core code and transport-specific client
modules.

| Module | Artifact | What it contains                                                                                                                  | Use it when                                                                                        |
|---|---|-----------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| Core | `xyz.mcxross.ksui:ksui-core` | Models, errors, config, accounts, cryptography, BCS helpers, PTB construction, transaction data building, and signing primitives. | You need to build/sign transactions or share Sui types without taking a network client dependency. |
| GraphQL SDK | `xyz.mcxross.ksui:ksui` | GraphQL Client                                                                                                                    | You want to use the Sui GraphQL client.                                                            |
| gRPC SDK | `xyz.mcxross.ksui:ksui-grpc` | gRPC Client                                                                                                                       | You want to use the Sui gRPC client.                                                               |

This split keeps the dependency graph explicit so you can add only what you need:

- `ksui-core` is the stable foundation. It has no GraphQL or gRPC transport
  dependency, so it can be used from tests, shared libraries, Android apps, and
  other SDK layers without dragging in client stacks.
- `ksui` is the GraphQL distribution. It keeps `Sui` as the main entry point and
  wires core transaction building to GraphQL object resolution.
- `ksui-grpc` is the gRPC client module.

## Installation

The current snapshot version is:

```kotlin
val ksuiVersion = "2.2.8-SNAPSHOT"
```

For snapshots, add Sonatype's snapshot repository:

```kotlin
repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots")
}
```

For released versions, `mavenCentral()` is enough.

### Kotlin Multiplatform

Add the module you need to the appropriate source set. In most applications,
`ksui` is the right starting point.

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("xyz.mcxross.ksui:ksui:2.2.8-SNAPSHOT")
        }
    }
}
```

Use `ksui-core` when you only need models, accounts, crypto, serialization, PTB
building, and transaction signing primitives:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("xyz.mcxross.ksui:ksui-core:2.2.8-SNAPSHOT")
        }
    }
}
```

Use `ksui-grpc` when you want the gRPC client:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("xyz.mcxross.ksui:ksui-grpc:2.2.8-SNAPSHOT")
        }
    }
}
```

You can depend on more than one module when needed:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("xyz.mcxross.ksui:ksui:2.2.8-SNAPSHOT")
            implementation("xyz.mcxross.ksui:ksui-grpc:2.2.8-SNAPSHOT")
        }
    }
}
```

### Platform-specific Gradle projects

For a non-KMP Android app:

```kotlin
dependencies {
    implementation("xyz.mcxross.ksui:ksui-android:2.2.8-SNAPSHOT")
}
```

For a JVM-only project:

```kotlin
dependencies {
    implementation("xyz.mcxross.ksui:ksui-jvm:2.2.8-SNAPSHOT")
}
```

Platform artifacts are also published for the split modules. For example:

```kotlin
dependencies {
    implementation("xyz.mcxross.ksui:ksui-core-android:2.2.8-SNAPSHOT")
    implementation("xyz.mcxross.ksui:ksui-grpc-android:2.2.8-SNAPSHOT")
}
```

```kotlin
dependencies {
    implementation("xyz.mcxross.ksui:ksui-core-jvm:2.2.8-SNAPSHOT")
    implementation("xyz.mcxross.ksui:ksui-grpc-jvm:2.2.8-SNAPSHOT")
}
```

The GraphQL module supports Android, iOS, JS, JVM, macOS, tvOS, and watchOS
targets. The gRPC module is available on the targets supported by kotlinx-rpc
gRPC in this repo: Android, JVM, iOS, macOS, tvOS, and watchOS. JS is not
currently a gRPC target.

## Quick start

### Account management

Generate a new Sui account:

```kotlin
val account = Account.create()
```

Import an account from a private key:

```kotlin
val privateKey = PrivateKey.fromEncoded("suipri...8cpv0g")
val account = Account.import(privateKey)
```

Or import directly from an encoded private key:

```kotlin
val account = Account.import("suipri...8cpv0g")
```

Ksui follows the standard Sui private key Bech32 format proposed in
[SIP-15](https://github.com/sui-foundation/sips/blob/main/sips/sip-15.md).

You can also import an account from a mnemonic:

```kotlin
val mnemonic = "abandon salad ..."
val account = Account.import(mnemonic)
```

### GraphQL client

The GraphQL SDK keeps `Sui` as the high-level entry point.

```kotlin
val sui = Sui()
```

Configure the network:

```kotlin
val config = SuiConfig(settings = SuiSettings(network = Network.MAINNET))
val sui = Sui(config)
```

Read from the chain:

```kotlin
val balance =
    sui.getBalance(
        AccountAddress("0x4afc81d797fd02bd7e923389677352eb592d55a00b65067fa582c05f62b4788b")
    )
```

Build, sign, and execute a PTB:

```kotlin
val alice = Account.import("suipri...8cpv0g")

val ptb = ptb {
    val coins = splitCoins {
        coin = Argument.GasCoin
        into = listOf(pure(100_000_000UL))
    }

    transferObjects {
        objects = coins
        to = address("0xbf...cde")
    }
}

val transaction = sui.signAndExecuteTransactionBlock(alice, ptb)
```

When using the GraphQL `ptb` helper, object-string inputs are resolved through
the configured `Sui` client before the transaction is built.

### Core-only transaction building

`ksui-core` can construct and sign transactions without depending on GraphQL or
gRPC. If a PTB uses unresolved object IDs, resolve them with a transport-specific
resolver before calling the strict `build()` path.

```kotlin
val tx = xyz.mcxross.ksui.core.ptb.ptb {
    transferObjects {
        objects = listOf(`object`(objectReference))
        to = address("0xbf...cde")
    }
}
```

### gRPC client

Use `SuiGrpcClient` from `ksui-grpc` when you want to use Sui's gRPC API while
sharing the same core models, config, account, crypto, and transaction-building
types.

```kotlin
val config =
    SuiConfig(
        settings = SuiSettings(network = Network.TESTNET)
    )

val client = SuiGrpcClient.fromConfig(config)
val balance = client.getBalance(AccountAddress("0x..."))
```

## What's included

| Path | Description |
|---|---|
| [core](core) | Transport-independent core module: models, errors, config, accounts, crypto, BCS, helpers, PTB construction, and transaction signing primitives. |
| [graphql](graphql) | GraphQL SDK module published as `ksui`; contains the `Sui` entry point and GraphQL-backed APIs. |
| [grpc](grpc) | gRPC SDK module published as `ksui-grpc`; contains `SuiGrpcClient`, protobuf definitions, and gRPC APIs. |
| [sample](sample) | Sample projects showing SDK usage. |
| [skills](skills) | Development notes for working on Ksui with agent tooling. |

For more information, see the [documentation](https://suicookbook.com).

## Contribution

All contributions to Ksui are welcome. Before opening a PR, please submit an
issue detailing the bug or feature. When opening a PR, ensure that your
contribution builds on the KMP toolchain, has been formatted with `ktfmt`, and
contains tests when applicable. For more information, see the
[contribution guidelines](CONTRIBUTING.md).

## License

    Copyright 2024 McXross

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
