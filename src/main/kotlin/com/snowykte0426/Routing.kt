package com.example

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import oshi.SystemInfo
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun Application.configureRouting() {
    routing {
        route("/hw-sence") {
            get("/status") {
                val systemInfo = SystemInfo()
                val hardware = systemInfo.hardware
                val processor = hardware.processor
                val prevTicks = processor.systemCpuLoadTicks
                Thread.sleep(1000)
                val totalCpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100.0
                val prevProcTicks = processor.processorCpuLoadTicks
                Thread.sleep(1000)
                val coreLoads = processor.getProcessorCpuLoadBetweenTicks(prevProcTicks).map { it * 100 }
                val memory = hardware.memory
                val usedMemory = memory.total - memory.available
                val memoryUsagePercent = (usedMemory.toDouble() / memory.total) * 100.0
                val os = systemInfo.operatingSystem
                val processCount = 5
                val topMemProcesses = os.processes
                    .sortedByDescending { it.residentSetSize }
                    .take(processCount)
                    .map { p -> p.name to p.residentSetSize }
                val processNames = topMemProcesses.map { it.first }
                val processMemValues = topMemProcesses.map { it.second.toDouble() / (1024 * 1024) }
                val processNamesJson = processNames.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
                val processMemValuesJson = processMemValues.joinToString(prefix = "[", postfix = "]")

                val sensors = hardware.sensors
                val cpuTemperature = sensors.cpuTemperature
                val cpuVoltage = sensors.cpuVoltage
                val maxFreqHz = processor.maxFreq
                val maxFreqGhz = maxFreqHz / 1_000_000_000.0
                val currentFreqsGhz = processor.currentFreq.map { it / 1_000_000_000.0 }

                val diskStores = hardware.diskStores

                val developerName = "snowykte0426"
                val developerUrl = "https://www.github.com/snowykte0426"
                val repoUrl = "https://github.com/8G4B/HW-Sence"
                val qrCodeUrl = "https://raw.githubusercontent.com/8G4B/HW-Sence/main/src/main/resources/static/image/repo_qr.png"

                val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))

                call.respondHtml {
                    head {
                        title("HW-Sence | System Status")
                        style {
                            +"""
                        @keyframes fadeInUp {
                            0% {
                                opacity: 0;
                                transform: translateY(20px);
                            }
                            100% {
                                opacity: 1;
                                transform: translateY(0);
                            }
                        }

                        body {
                            font-family: Arial, sans-serif;
                            margin: 20px;
                            background-color: #f9f9f9;
                            display: flex;
                            flex-direction: column;
                            min-height: 100vh;
                            opacity: 0;
                            animation: fadeInUp 1s ease forwards;
                        }

                        .dark-mode {
                            background-color: #222;
                            color: #eee;
                        }

                        h1, h2, h3, table, canvas, footer, .refresh-container, .last-updated {
                            opacity: 0;
                            animation: fadeInUp 0.8s ease forwards;
                        }

                        .refresh-container {
                            animation-delay: 0.4s;
                        }
                        h1 {
                            animation-delay: 0.2s;
                        }
                        .last-updated {
                            animation-delay: 0.3s;
                        }
                        h2 {
                            animation-delay: 0.4s;
                        }
                        table {
                            animation-delay: 0.6s;
                        }
                        canvas {
                            animation-delay: 0.8s;
                        }
                        footer {
                            animation-delay: 1.0s;
                        }
                        h3 {
                            animation-delay: 0.5s;
                        }

                        canvas {
                            max-width: 300px;
                            margin: 20px auto;
                            display: block;
                        }

                        table {
                            width: 60%;
                            margin: 20px auto;
                            border-collapse: collapse;
                            background-color: #fff;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                            border-radius: 5px;
                            overflow: hidden;
                        }

                        th, td {
                            border-bottom: 1px solid #eee;
                            padding: 10px;
                            text-align: center;
                            font-size: 14px;
                        }

                        th {
                            background-color: #4CAF50;
                            color: white;
                            font-weight: 600;
                        }

                        tr:hover {
                            background-color: #f7f7f7;
                        }

                        h3 {
                            text-align: center;
                            font-weight: 500;
                            margin: 30px 0 10px 0;
                            color: #555;
                        }

                        footer {
                            margin-top: auto;
                            text-align: center;
                            padding: 20px;
                            font-size: 14px;
                            color: #555;
                            background-color: #f0f2f5;
                            border-top: 1px solid #ddd;
                        }

                        footer .qr-container {
                            margin-top: 10px;
                        }

                        a {
                            color: inherit;
                            text-decoration: none;
                        }

                        a:hover {
                            text-decoration: underline;
                        }

                        .refresh-container {
                            display: flex;
                            justify-content: center;
                            margin-top: 20px;
                            gap: 10px;
                        }

                        .refresh-button, .dark-mode-button {
                            background-color: #4CAF50;
                            color: white;
                            padding: 10px 20px;
                            border: none;
                            border-radius: 5px;
                            cursor: pointer;
                            font-size: 14px;
                            transition: background-color 0.3s ease;
                        }

                        .refresh-button:hover, .dark-mode-button:hover {
                            background-color: #45a049;
                        }

                        .last-updated {
                            text-align: center;
                            margin-top: 10px;
                            font-size: 14px;
                            color: #666;
                        }
                        """
                        }
                        script(src = "https://cdn.jsdelivr.net/npm/chart.js") {}
                    }
                    body {
                        div("refresh-container") {
                            button(type = ButtonType.button, classes = "refresh-button") {
                                attributes["onclick"] = "location.reload();"
                                +"Refresh"
                            }
                            button(type = ButtonType.button, classes = "dark-mode-button") {
                                attributes["onclick"] = "document.body.classList.toggle('dark-mode');"
                                +"Dark Mode"
                            }
                        }

                        h1 { +"System Status" }

                        div("last-updated") {
                            +"Last updated: $currentTime"
                        }

                        h2 { +"CPU & Memory Usage" }
                        table {
                            tr {
                                th { +"Metric" }
                                th { +"Value" }
                            }
                            tr {
                                td { +"Total CPU Usage" }
                                td { +"%.2f%%".format(totalCpuLoad) }
                            }
                            tr {
                                td { +"Memory Usage" }
                                td { +"%.2f%%".format(memoryUsagePercent) }
                            }
                        }

                        h3 { +"Per-Core CPU Usage:" }
                        table {
                            tr {
                                th { +"Core Index" }
                                th { +"Usage (%)" }
                            }
                            coreLoads.forEachIndexed { index, load ->
                                tr {
                                    td { +"Core $index" }
                                    td { +"%.2f%%".format(load) }
                                }
                            }
                        }

                        h2 { +"CPU & Hardware Info" }
                        table {
                            tr {
                                th { +"Item" }
                                th { +"Value" }
                            }
                            tr {
                                td { +"CPU Temperature" }
                                td { +"${"%.2f".format(cpuTemperature)} °C" }
                            }
                            tr {
                                td { +"CPU Voltage" }
                                td { +"$cpuVoltage V" }
                            }
                            tr {
                                td { +"Max CPU Frequency" }
                                td { +"%.2f GHz".format(maxFreqGhz) }
                            }
                            currentFreqsGhz.forEachIndexed { idx, freq ->
                                tr {
                                    td { +"Core $idx Frequency" }
                                    td { +"%.2f GHz".format(freq) }
                                }
                            }
                        }

                        h2 { +"Top $processCount Memory-Consuming Processes" }
                        canvas {
                            id = "memChart"
                            width = "600"
                            height = "400"
                        }

                        script {
                            unsafe {
                                +"""
                            const ctx = document.getElementById('memChart').getContext('2d');
                            const memChart = new Chart(ctx, {
                                type: 'bar',
                                data: {
                                    labels: $processNamesJson,
                                    datasets: [{
                                        label: 'Memory Usage (MB)',
                                        data: $processMemValuesJson,
                                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                                        borderColor: 'rgba(75, 192, 192, 1)',
                                        borderWidth: 1
                                    }]
                                },
                                options: {
                                    scales: {
                                        y: {
                                            beginAtZero: true
                                        }
                                    }
                                }
                            });
                            """
                            }
                        }

                        h2 { +"Disk Information" }
                        table {
                            tr {
                                th { +"Name" }
                                th { +"Model" }
                                th { +"Serial" }
                                th { +"Size(GB)" }
                            }
                            diskStores.forEach { disk ->
                                tr {
                                    td { +disk.name }
                                    td { +disk.model }
                                    td { +(disk.serial ?: "") }
                                    td { +"%.2f".format(disk.size / (1024.0 * 1024.0 * 1024.0)) }
                                }
                            }
                        }

                        footer {
                            p {
                                +"Developed by "
                                a(href = developerUrl, target = "_blank") {
                                    +developerName
                                }
                            }
                            p {
                                +"GitHub Repository:"
                            }
                            div("qr-container") {
                                img(src = qrCodeUrl, alt = "QR Code for Repository") {
                                    style = "width: 120px; height: 120px;"
                                }
                            }
                            p {
                                a(href = repoUrl, target = "_blank") {
                                    +repoUrl
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}