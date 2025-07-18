<h1 align="center">Ksui - Multiplatform SDK for Sui</h1>

Ksui, /keɪˈsuːiː/ (pronounced as "kay-soo-ee"), is a Kotlin Multiplatform (KMP) SDK for integrating with the Sui
blockchain.

It is designed to be a type-safe, client-configurable, and multiplatform SDK that can be used across
different platforms such as Android, iOS, JS, and JVM. It is built on top of the KMM toolchain and is designed to be
extensible and easy to use.


[![Kotlin Version](https://img.shields.io/badge/Kotlin-v2.1.21-B125EA?logo=kotlin)](https://kotlinlang.org)
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
- [Quick start](#quick-start)
- [What's included](#whats-included)
- [Contribution](#contribution)
- [License](#license)

## Features

- Multiplatform (Android, iOS, JS, JVM)
- Type-safe intuitive API
- Client Configurable (Retries, Timeout, etc)
- Asynchronous client
- Coroutine based
- Expressive DSL for PTB construction
- Plus everything else you would expect from a Sui SDK

## Quick Start

### Installation

#### Multiplatform

Add the `Ksui` dependency to the common sourceSet

```kotlin
implementation("xyz.mcxross.ksui:ksui:<$ksui_version>")
```

#### Platform specific (Android, JS, Native, JVM)

Add the `Ksui` dependency to the Project's dependency block

Generic:

```kotlin
implementation("xyz.mcxross.ksui:<ksui-[platform]>:<$ksui_version>")
```

For example for Android and JS

Android:

```kotlin
implementation("xyz.mcxross.ksui:ksui-android:<$ksui_version>")
```

### Account Management

#### Generating a new account

To generate a new Sui account, simply call the static `create` method on the `Account` class as shown below:

```kotlin
val yourAccount = Account.create()
```

This generates a new account with a random mnemonic, public key, and address.

#### Importing an existing account

##### From a private key

There are a couple of ways to import an existing account. One way is to import an account from a private key as a
private key object:

```kotlin
val privateKey = PrivateKey.fromEncoded("suipri...8cpv0g")

val yourAccount = Account.import(privateKey)
```

or as a string:

```kotlin
val yourAccount = Account.import("suipri...8cpv0g")
```

> [!NOTE]
> **Ksui** adheres to the standard Sui private key format of encoding the private key in Bech32 format as
> proposed in [SIP-15](https://github.com/sui-foundation/sips/blob/main/sips/sip-15.md).

You can also import an account from a mnemonic:

```kotlin
val mnemonic = "abandon salad ..."
val yourAccount = Account.import(mnemonic)
```

### Initialization

To get started, create an instance of the Sui client. This single step also automatically configures a default, 
globally-accessible client that can be used by top-level functions like `ptb`.

```kotlin
val sui = Sui()
```

In case you want to configure the client, you can do so as shown below:

```kotlin
val config = SuiConfig(settings = SuiSettings(network = Network.MAINNET))
val sui = Sui(config)
```

Now you can use the `sui` instance to interact with the Sui chain for reading and writing.

### Reading from the chain

Once you have initialized the client, you can use it to read from the chain. For example, to get the balance of an
address:

```kotlin
val balance = sui.getBalance(AccountAddress("0x4afc81d797fd02bd7e923389677352eb592d55a00b65067fa582c05f62b4788b"))
```

### Writing to the chain (PTBs)

To write to the chain, you build a **Programmable Transaction Block (PTB)**. The SDK provides an expressive DSL that makes this 
process simple and intuitive. The top level `ptb` function automatically uses the default client you initialized to resolve object 
details, so you don't need to pass it explicitly.

For example, to construct a **PTB** that splits a coin and sends it to another address:
```kotlin

// Assuming you have an account object
val alice = Account.import("suipri...8cpv0g")

// Create a programmable transaction
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

// Sign and execute txn
val txn = sui.signAndExecuteTransactionBlock(alice, ptb)

```

For more information, please see the [documentation](https://suicookbook.com).

## What's included

| File/Folder      | Description                                                                                             |
|------------------|---------------------------------------------------------------------------------------------------------|
| [lib](lib)       | Library implementation folder. It contains the code for Ksui that can be used across multiple platforms |
| [sample](sample) | Samples on how to use the exported APIs                                                                 |

## Contribution

All contributions to Ksui are welcome. Before opening a PR, please submit an issue detailing the bug or feature. When
opening a PR, please ensure that your contribution builds on the KMM toolchain, has been linted
with `ktfmt <GOOGLE (INTERNAL)>`, and contains tests when applicable. For more information, please see
the [contribution guidelines](CONTRIBUTING.md).

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

