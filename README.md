<h1 align="center">Ksui - KMP Library for Sui</h1>

Ksui, /keɪˈsuːiː/ (pronounced as "kay-soo-ee"), is a collection of Kotlin Multiplatform JSON-RPC wrapper and crypto
utilities for interacting with a Sui Full node.

This library is intended to be the highest quality publicly available library for interacting with Sui on any
Kotlin-supported platform by epitomizing expressiveness, conciseness and aesthetics

![Version](https://img.shields.io/badge/Version-1.3.2-blue.svg)
[![Maven Central](https://img.shields.io/maven-central/v/xyz.mcxross.ksui/ksui)](https://search.maven.org/artifact/xyz.mcxross.ksui/ksui)
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

##### RPC HTTP Client

Create a new instance of the Sui RPC HTTP Client. The client supports both DSL and command-query styles for client
creation and RPC calls respectively as shown below:

```kotlin
val sui = Sui()

val balance = sui.getBalance(SuiAddress("0x4afc81d797fd02bd7e923389677352eb592d55a00b65067fa582c05f62b4788b"))
```

In case you want to configure the client, you can do so as shown below:

```kotlin
val config = SuiConfig(settings = SuiSettings(network = Network.MAINNET))
val sui = Sui(config)
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
