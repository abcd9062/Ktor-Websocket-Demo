package com.heysitam.plugins

import com.heysitam.Connection
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import java.util.*
import kotlin.collections.LinkedHashSet

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/chat") {
            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection//
            try {
                send("You are connected! There are ${connections.count()} users here.")// websocketSession
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val receivedText = frame.readText()
                        val textWithUsername = "[${thisConnection.name}]: $receivedText"
                        connections.forEach {
                            it.session.send(textWithUsername)
                        }
                        if (receivedText.equals("bye", ignoreCase = true)) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        }
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            } finally {
                println("Removing $thisConnection!")
                connections -= thisConnection
            }

        }
    }
}
