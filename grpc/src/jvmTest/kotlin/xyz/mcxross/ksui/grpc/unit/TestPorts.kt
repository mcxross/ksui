package xyz.mcxross.ksui.grpc.unit

import java.net.ServerSocket

internal fun unusedTcpPort(): Int = ServerSocket(0).use { it.localPort }
