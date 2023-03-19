<h1 align="center">KSui - KMP Library for Sui</h1>

KSui is a collection of Multiplatform Kotlin language JSON-RPC wrapper and crypto utilities for interacting with the Sui Devnet and Sui Full node.

![SUI JSON-RPC version](https://img.shields.io/badge/Sui%20JSON--RPC-0.29.0-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android%20|%20JVM%20|%20Web%20|%20Native-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/xyz.mcxross.ksui/ksui)](https://search.maven.org/artifact/xyz.mcxross.ksui/ksui)

# Table of contents
- [Features](#features)
- [Quick start](#quick-start)
- [What's included](#whats-included)
- [Contribution](#contribution)

## Features
- Implements all functions
- Pub/Sub and Req/Res
- Crypto wrappers
- Client Configurable
- Multiplatform

## Quick Start

##### Installation

```kotlin
implementation("xyz.mcxross.ksui:ksui:$ksui_version")
```

##### RPC HTTP Client
Create a new instance of the Sui RPC HTTP Client. The client can be configured with the following options:
- `endpoint`: The Sui endpoint to connect to. Defaults to `EndPoint.DEVNET`
- `agentName`: The name of the agent making the request. Defaults to `KSUI/0.0.1`
- `maxRetries`: The maximum number of times to retry a request. Defaults to `5`
- and many more. Check documentation for more details.

After the client is configured, it can be used to invoke remote procedures e.g. `getBalance`, `getCheckpoints` and `getCoins`. Check the [Sui JSON-RPC documentation](https://docs.sui.io/sui-jsonrpc) for a list of available RPCs.

The client supports both DSL and command-query styles for client creation and RPC calls respectively as shown below:
```kotlin
//Configure Client, DSL Style
val suiRpcClient = createSuiHttpClient {
    endpoint = EndPoint.DEVNET
    agentName = "KSUI/0.0.1"
    maxRetries = 10
}
//Invoke remote procedure, command-query style
val balance = suiRpcClient.getBalance(SuiAddress("0x3b1db4d4ea331281835e2b450312f82fc4ab880a"))
val coinMetadata = suiRpcClient.getCoinMetadata("0x2::sui::SUI")
```

<img src="asset/print.png" alt="KSui output" />

## What's included
| File/Folder      | Description                                                                                             |
|------------------|---------------------------------------------------------------------------------------------------------|
| [lib](lib)       | Library implementation folder. It contains the code for KSui that can be used across multiple platforms |
| [sample](sample) | Samples on how to use the exported APIs                                                                 |

## Contribution

All contributions to KSui are welcome. Before opening a PR, please submit an issue detailing the bug or feature. When opening a PR, please ensure that your contribution builds on the KMM toolchain, has been linted with `ktfmt <GOOGLE (INTERNAL)>`, and contains tests when applicable. For more information, please see the [contribution guidelines](CONTRIBUTING.md).