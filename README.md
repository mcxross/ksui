<h1 align="center">Ksui - Multiplatform SDK for Sui</h1>

Ksui, /keɪˈsuːiː/ (pronounced as "kay-soo-ee"), is a Kotlin Multiplatform (KMP) SDK for integrating with the Sui
blockchain.

It is designed to be a type-safe, client-configurable, and multiplatform SDK that can be used across
different platforms such as Android, iOS, JS, and JVM. It is built on top of the KMM toolchain and is designed to be
extensible and easy to use.

![Build](https://img.shields.io/badge/Build-v2.0.1-blue.svg)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-v1.9.23-B125EA?logo=kotlin)](https://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/xyz.mcxross.ksui/ksui)](https://search.maven.org/artifact/xyz.mcxross.ksui/ksui)
![Snapshot](https://img.shields.io/nexus/s/xyz.mcxross.ksui/ksui?server=https%3A%2F%2Fs01.oss.sonatype.org&label=Snapshot)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

![badge-android](http://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android)
![badge-ios](http://img.shields.io/badge/Platform-iOS-orange.svg?logo=apple)
![badge-js](http://img.shields.io/badge/Platform-NodeJS-yellow.svg?logo=javascript)
![badge-jvm](http://img.shields.io/badge/Platform-JVM-red.svg?logo=openjdk)
![badge-linux](http://img.shields.io/badge/Platform-Linux-lightgrey.svg?logo=linux)
![badge-macos](http://img.shields.io/badge/Platform-macOS-orange.svg?logo=apple)
![badge-windows](http://img.shields.io/badge/Platform-Windows-blue.svg?logo=windows)

# Table of contents

- [Features](#features)
- [Quick start](#quick-start)
- [What's included](#whats-included)
- [Projects using Ksui](#projects-using-Ksui)
- [Contribution](#contribution)

## Features

- Implements all functions
- Type-safe
- Client Configurable
- Multiplatform

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

JS:

```kotlin
implementation("xyz.mcxross.ksui:ksui-js:<$ksui_version>")
```

### Initialization

First, you need to create a new instance of the Sui RPC HTTP Client as shown below:

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
val balance = sui.getBalance(SuiAddress("0x4afc81d797fd02bd7e923389677352eb592d55a00b65067fa582c05f62b4788b"))
```

### Writing to the chain (PTBs)

To write to the chain, you need to create a `Transaction` and sign it with the private key of the sender. A transaction,
among its metadata,
is made up of PTBs which are a chain of commands that are executed by the chain. For example, to construct a PTB that
*splits* a
coin and *sends* it to another address, you can do so as shown below:

```kotlin
val ptb = programmableTx {
    command {
        // Split the coin
        val splitCoins = splitCoins {
            coin = Argument.GasCoin
            into = listOf(input(1_000UL))
        }

        // Send the split coin to the receiver
        transferObjects {
            objects = listOf(splitCoins)
            to = input(SuiAddress("0xad4c...aeb4"))
        }
    }
}

// Now, we can create the transaction data
val txData = TransactionData.programmable(sender, listOf(senderCoins pick 0), ptb, 5_000_000UL, gasPrice)
```

For more information, please see the [documentation](https://mcxross.github.io/ksui/).

## What's included

| File/Folder      | Description                                                                                             |
|------------------|---------------------------------------------------------------------------------------------------------|
| [lib](lib)       | Library implementation folder. It contains the code for Ksui that can be used across multiple platforms |
| [sample](sample) | Samples on how to use the exported APIs                                                                 |

## Projects using Ksui

- [Sui Cohesive](https://github.com/mcxross/sui-cohesive)

## Contribution

All contributions to Ksui are welcome. Before opening a PR, please submit an issue detailing the bug or feature. When
opening a PR, please ensure that your contribution builds on the KMM toolchain, has been linted
with `ktfmt <GOOGLE (INTERNAL)>`, and contains tests when applicable. For more information, please see
the [contribution guidelines](CONTRIBUTING.md).
