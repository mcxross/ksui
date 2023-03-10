<h1 align="center">KSui - KMP Library for Sui</h1>

KSui is a collection of Multiplatform Kotlin language JSON-RPC wrapper and crypto utilities for interacting with the Sui Devnet and Sui Full node.

# Table of contents
- [Features](#features)
- [Quick start](#quick-start)
- [What's included](#whats-included)
- [Contribution](#contribution)

## Features

- Pub/Sub and Req/Res
- Crypto wrappers
- Client Configurable
- Multiplatform

## Quick Start

##### Installation

```kotlin
implementation("xyz.mcxross.ksui:ksui:$ksui_version")
```

##### RPC
```kotlin
//Configure Client, DSL Style
val suiRpcClient = createSuiRpcClient { setEndPoint(EndPoint.DEVNET) }
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