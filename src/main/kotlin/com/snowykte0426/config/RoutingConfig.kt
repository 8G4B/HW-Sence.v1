package com.snowykte0426.config

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.html.respondHtml
import com.snowykte0426.service.fetchSystemStatus
import com.snowykte0426.ui.generateHtmlPage

fun Application.configureRouting() {
    routing {
        route("/hw-sence") {
            staticResources("/static", "static")
            get("/status") {
                val status = fetchSystemStatus()
                call.respondHtml {
                    generateHtmlPage(status)
                }
            }
        }
    }
}
