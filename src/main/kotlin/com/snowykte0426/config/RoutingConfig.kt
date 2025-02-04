package com.snowykte0426.config

import com.snowykte0426.service.fetchSystemStatus
import com.snowykte0426.ui.generateHtmlPage
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        staticResources("/hw-sence/status/static", "static")
        route("") {
            get("") {
                val status = fetchSystemStatus()
                call.respondHtml {
                    generateHtmlPage(status)
                }
            }
        }
    }
}