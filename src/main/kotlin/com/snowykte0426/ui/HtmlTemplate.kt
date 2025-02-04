package com.snowykte0426.ui

import com.snowykte0426.model.SystemStatus
import com.snowykte0426.util.generateCoreRows
import kotlinx.html.*

fun HTML.generateHtmlPage(status: SystemStatus) {
    head {
        title("HW-Sence | System Status")
        link(rel = "icon", type = "image/png", href = "/hw-sence/status/static/image/favicon.ico")
        link(rel = "stylesheet", href = "/hw-sence/status/static/css/styles.css")
        script(src = "https://cdn.jsdelivr.net/npm/chart.js") {}
    }
    body {
        renderHeader(status)
        renderDashboard(status)
        renderScripts()
        renderFooter()
    }
}

fun FlowContent.renderHeader(status: SystemStatus) {
    h1 { +"HW-Sence" }
    h3 {
        a(href = "https://github.com/8G4B/HW-Sence.v1/releases/tag/v1.2.2", target = "_blank") { +"v1.2.2" }
    }
    p("now-time") {
        style = "text-align:center; margin-bottom:20px;"
        +"${status.currentDate} ${status.currentTime}"
    }
    h1 { +"System Status" }
    div("refresh-container") {
        button(type = ButtonType.button, classes = "refresh-button") {
            onClick = "location.reload();"
            +"Refresh"
        }
        button(type = ButtonType.button, classes = "dark-mode-button") {
            onClick = "document.body.classList.toggle('dark-mode');"
            +"Dark Mode"
        }
    }
    div("last-updated") { +"Last updated: ${status.currentTime}" }
    div("os-info") {
        +"OS: ${status.osName}"
    }
}

fun FlowContent.renderDashboard(status: SystemStatus) {
    div("dashboard") {
        renderCpuMemoryPanel(status)
        renderPerCorePanel(status)
        renderCpuHardwarePanel(status)
        renderTopProcessesPanel(status)
        renderDiskInfoPanel(status)
        renderDockerInfoPanel(status)
    }
}

fun FlowContent.renderCpuMemoryPanel(status: SystemStatus) {
    div("panel") {
        h2 {
            title = "Shows overall CPU and Memory usage"
            +"CPU & Memory Usage"
        }
        table {
            tr {
                th { title = "Name of the metric"; +"Metric" }
                th { title = "Current measured value"; +"Value" }
            }
            tr {
                td { title = "Total CPU usage"; +"Total CPU Usage" }
                td {
                    title = "Percentage of CPU in use"
                    +if (status.cpuLoad.isNaN()) "N/A" else "%.2f%%".format(status.cpuLoad)
                }
            }
            tr {
                td { title = "Memory usage metric"; +"Memory Usage" }
                td {
                    title = "Percentage of memory in use"
                    +if (status.memPercent.isNaN()) "N/A" else "%.2f%%".format(status.memPercent)
                }
            }
        }
        canvas {
            id = "cpuGauge"
            width = "200"
            height = "200"
            attributes["data-cpu-used"] = if (status.cpuLoad.isNaN()) "0.0" else status.cpuLoad.toString()
            title = "CPU Usage Doughnut Chart"
        }
        canvas {
            id = "memGauge"
            width = "200"
            height = "200"
            attributes["data-mem-used"] = if (status.memPercent.isNaN()) "0.0" else status.memPercent.toString()
            attributes["data-mem-total"] = status.totalMemory
            attributes["data-mem-used-physical"] = status.usedMemory
            title = "Memory Usage Doughnut Chart"
        }
        div{
            id="memUsageDetails"
        }
    }
}

fun FlowContent.renderPerCorePanel(status: SystemStatus) {
    div("panel") {
        h2 {
            title = "Shows per-core CPU usage"
            +"Per-Core CPU Usage"
        }
        table {
            tr {
                th { title = "Logical core number"; +"Core Index" }
                th { title = "CPU usage on this core"; +"Usage (%)" }
            }
            val rows = generateCoreRows(status.coreLoads)
            rows.forEach { (coreName, usage) ->
                tr {
                    td { title = if (coreName == "N/A") "No data" else "Core identifier"; +coreName }
                    td { title = if (usage == "N/A") "No data" else "Core usage percentage"; +usage }
                }
            }
        }
    }
}

fun FlowContent.renderCpuHardwarePanel(status: SystemStatus) {
    div("panel") {
        h2 {
            title = "Various CPU and hardware-related metrics"
            +"CPU & Hardware Info"
        }
        table {
            tr {
                th { title = "Metric name"; +"Item" }
                th { title = "Measured value"; +"Value" }
            }
            tr {
                td { title = "CPU Temperature"; +"CPU Temperature" }
                td {
                    title = "Current CPU temperature in °C"
                    +if (status.cpuTemperature == "N/A") "N/A" else "${status.cpuTemperature} °C"
                }
            }
            tr {
                td { title = "CPU Voltage"; +"CPU Voltage" }
                td {
                    title = "Current CPU voltage"
                    +if (status.cpuVoltage == "N/A") "N/A" else "${status.cpuVoltage} V"
                }
            }
            tr {
                td { title = "Max CPU Frequency"; +"Max CPU Frequency" }
                td {
                    title = "Maximum CPU frequency in GHz"
                    +if (status.maxGhz.isNaN()) "N/A" else "%.2f GHz".format(status.maxGhz)
                }
            }
            if (status.coreFreqGhz.isEmpty()) {
                tr {
                    td { title = "Core frequencies data not available"; +"Core Frequencies" }
                    td { title = "N/A"; +"N/A" }
                }
            } else {
                status.coreFreqGhz.forEachIndexed { i, f ->
                    tr {
                        td { title = "Frequency of core $i"; +"Core $i Frequency" }
                        td {
                            title = "Frequency in GHz"
                            +if (f.isNaN()) "N/A" else "%.2f GHz".format(f)
                        }
                    }
                }
            }
            tr {
                td { title = "System uptime"; +"System Uptime" }
                td { title = "Uptime in hours and minutes"; +status.systemUptime }
            }
        }
    }
}

fun FlowContent.renderTopProcessesPanel(status: SystemStatus) {
    div("panel") {
        h2 { title = "Top memory consuming processes"; +"Top ${status.processNames.size} Memory-Consuming Processes" }
        if (status.processNames.isEmpty()) {
            table {
                tr {
                    th { title = "Process Name"; +"Process" }
                    th { title = "Memory(MB) used"; +"Memory(MB)" }
                }
                tr {
                    td { title = "No process data"; +"N/A" }
                    td { title = "No process data"; +"N/A" }
                }
            }
        } else {
            canvas {
                id = "memChart"
                width = "600"
                height = "400"
                title = "Memory Usage Bar Chart for Top Processes"
                val pnJson = status.processNames.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
                val pvJson = status.processMemoryMB.joinToString(prefix = "[", postfix = "]")
                attributes["data-process-names"] = pnJson
                attributes["data-process-values"] = pvJson
            }
        }
    }
}

fun FlowContent.renderDiskInfoPanel(status: SystemStatus) {
    div("panel") {
        h2 { title = "Information about disk storage"; +"Disk Information" }
        div("disk-scroll-wrapper") {
            style = "overflow-x:auto;"
            table {
                tr {
                    th { title = "Disk name"; +"Name" }
                    th { title = "Disk model"; +"Model" }
                    th { title = "Disk serial number"; +"Serial" }
                    th { title = "Disk size in GB"; +"Size(GB)" }
                }
                if (status.disks.isEmpty()) {
                    tr {
                        td { title = "No disk data"; +"N/A" }
                        td { title = "No disk data"; +"N/A" }
                        td { title = "No disk data"; +"N/A" }
                        td { title = "No disk data"; +"N/A" }
                    }
                } else {
                    status.disks.forEach { disk ->
                        tr {
                            td { title = "Disk name"; +disk.name }
                            td { title = "Disk model"; +disk.model }
                            td { title = "Disk serial"; +disk.serial }
                            td { title = "Disk size in GB"; +disk.sizeGB }
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.renderDockerInfoPanel(status: SystemStatus) {
    div("panel") {
        h2 { title = "Docker environment metrics"; +"Docker Environment" }
        table {
            tr {
                th { title = "Item name"; +"Item" }
                th { title = "Value of the item"; +"Value" }
            }
            tr {
                td { title = "Container HOSTNAME"; +"Container HOSTNAME" }
                td { title = "Container hostname value"; +status.containerId }
            }
            tr {
                td { title = "CGroup Memory Limit"; +"CGroup Memory Limit" }
                td { title = "CGroup memory limit in GB"; +status.dockerMemLimit }
            }
        }
    }
}

fun FlowContent.renderScripts() {
    script(src = "/hw-sence/status/static/js/main.js") {}
}

fun FlowContent.renderFooter() {
    footer {
        p {
            +"Developed by "
            a(href = "https://www.github.com/snowykte0426", target = "_blank") { +"snowykte0426" }
        }
        p { +"GitHub Repository:" }
        div("qr-container") {
            img(src = "/hw-sence/status/static/image/repo_qr.png", alt = "QR Code for Repository") {
                style = "width:120px;height:120px;"
            }
        }
        p {
            a(href = "https://github.com/8G4B/HW-Sence", target = "_blank") { +"https://github.com/8G4B/HW-Sence" }
        }
    }
}