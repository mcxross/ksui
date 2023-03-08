<h1 align="center">KSui - KMP Library for Sui</h1>

KSui is a collection of Multiplatform Kotlin language JSON-RPC wrapper and crypto utilities for interacting with the Sui Devnet and Sui Full node.

# Table of contents
- [Quick start](#quick-start)
- [What's included](#whats-included)
- [Features](#features)
- [Contribution](#contribution)

## Quick Start

##### Installation

```kotlin
implementation("xyz.mcxross.ksui:ksui:$ksui_version")
```

##### RPC
```kotlin
// Configure Client, DSL Style
val suiRpcClient = createSuiRpcClient {
    setEndPoint(EndPoint.DEVNET)
  }
// Invoke remote procedure, command-query style
  suiRpcClient.getValidators().list
```