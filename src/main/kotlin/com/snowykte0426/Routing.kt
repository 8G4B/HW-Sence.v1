package com.snowykte0426

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import oshi.SystemInfo
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
fun Application.configureRouting() {
    routing {
        route("/hw-sence") {
            get("/status") {
                val si = SystemInfo()
                val hw = si.hardware
                val p = hw.processor
                val prevTicks = p.systemCpuLoadTicks
                Thread.sleep(1000)
                val rawCpuLoad = p.getSystemCpuLoadBetweenTicks(prevTicks)
                val cpuLoad = if (rawCpuLoad.isNaN()) Double.NaN else rawCpuLoad * 100.0
                val mem = hw.memory
                val memUsed = mem.total - mem.available
                val memPercent = if (mem.total > 0) (memUsed.toDouble() / mem.total) * 100.0 else Double.NaN
                val prevProcTicks = p.processorCpuLoadTicks
                Thread.sleep(1000)
                val coreLoadValues = p.getProcessorCpuLoadBetweenTicks(prevProcTicks)
                val coreLoads =
                    if (coreLoadValues.any { it.isNaN() }) emptyList<Double>() else coreLoadValues.map { it * 100 }
                val os = si.operatingSystem
                val pc = 5
                val processes = os.processes
                val topMemProcesses =
                    if (processes.isEmpty()) emptyList<Pair<String, Long>>() else processes.sortedByDescending { it.residentSetSize }
                        .take(pc).map { it.name to it.residentSetSize }
                val pn = topMemProcesses.map { it.first }
                val pv = topMemProcesses.map { it.second.toDouble() / (1024 * 1024) }
                val pnJson = pn.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
                val pvJson = pv.joinToString(prefix = "[", postfix = "]")
                val s = hw.sensors
                val t = if (s.cpuTemperature.isNaN()) Double.NaN else if (s.cpuTemperature > 20) s.cpuTemperature else "N/A"
                val v = if (s.cpuVoltage.isNaN()) Double.NaN else if(s.cpuVoltage > 1) s.cpuVoltage else "N/A"
                val maxHz = p.maxFreq
                val maxGhz = if (maxHz > 0) maxHz / 1_000_000_000.0 else Double.NaN
                val freqs = p.currentFreq
                val coreFreqGhz =
                    if (freqs.isEmpty()) emptyList<Double>() else freqs.map { if (it > 0) it / 1_000_000_000.0 else Double.NaN }
                val upSec = os.systemUptime.toLong()
                val upH = upSec / 3600
                val upM = (upSec % 3600) / 60
                val upStr = if (upSec > 0) "${upH}h ${upM}m" else "N/A"
                val disks = hw.diskStores
                val devName = "snowykte0426"
                val devUrl = "https://www.github.com/snowykte0426"
                val repoUrl = "https://github.com/8G4B/HW-Sence"
                val releasesUrl = "https://github.com/8G4B/HW-Sence.v1/releases/tag/v1.1.3"
                val qrUrl =
                    "https://raw.githubusercontent.com/8G4B/HW-Sence/main/src/main/resources/static/image/repo_qr.png"
                val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                val dateNow = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val containerId = System.getenv("HOSTNAME") ?: "Unknown"

                val memLimitFile = File("/sys/fs/cgroup/memory/memory.limit_in_bytes")
                val dockerMemLimit = if (memLimitFile.exists()) {
                    val limitStr = memLimitFile.readText().trim()
                    if (limitStr == "max" || limitStr.toLongOrNull() == null) "N/A" else {
                        val bytes = limitStr.toLong()
                        "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
                    }
                } else "N/A"

                fun coreRows(coreLoads: List<Double>): List<Pair<String, String>> {
                    return if (coreLoads.isEmpty()) {
                        List(3) { "N/A" to "N/A" }
                    } else {
                        if (coreLoads.size < 3) {
                            coreLoads.mapIndexed { i, l -> "Core $i" to (if (l.isNaN()) "N/A" else "%.2f%%".format(l)) } +
                                    List(3 - coreLoads.size) { "N/A" to "N/A" }
                        } else {
                            coreLoads.mapIndexed { i, l -> "Core $i" to (if (l.isNaN()) "N/A" else "%.2f%%".format(l)) }
                        }
                    }
                }
                call.respondHtml {
                    head {
                        title("HW-Sence | System Status")
                        link(rel = "icon", type = "image/png", href = "/static/image/favicon.png")
                        style {
                            +"""
@keyframes fadeInUp{0%{opacity:0;transform:translateY(20px);}100%{opacity:1;transform:translateY(0);}}
body {
    font-family:Arial,sans-serif;
    background:#f9f9f9;
    display:flex;
    flex-direction:column;
    min-height:100vh;
    animation:fadeInUp 1s ease forwards;
    opacity:0;
    color:#000 !important;
    padding:20px; 
    margin:0;
    box-sizing:border-box;
}
.refresh-container {
    display:flex;
    justify-content:center;
    margin:20px 0; 
    gap:10px;
}
.last-updated {
    text-align:center;
    margin-top:16px; /* Add some extra margin to create padding */
    font-size:14px;
    padding-bottom:20px;
    color:#666;
}

.dashboard {
    display:grid;
    grid-template-columns:repeat(auto-fit,minmax(280px,1fr));
    gap:20px;
    align-items:start;
    width:100%;
    box-sizing:border-box;
}

.panel {
    background:#fff !important;
    box-shadow:0 2px 8px rgba(0,0,0,0.1);
    border-radius:5px;
    padding:20px;
    color:#000 !important;
    box-sizing:border-box;
    display:flex;
    flex-direction:column;
    align-items:stretch;
    width:100%;
}

h1 { text-align:center; margin:20px 0; color:#000 !important; font-size:32px; /* Larger size for HW-Sence */ }
h3 {
    margin-top:0;
    color:#000 !important;
    text-align:center;
    margin-bottom:10px;
    font-size:16px; /* keep version smaller */
}
h2 {
    margin-top:0;
    margin-bottom:20px;
    font-size:18px;
    text-align:center;
    color:#000 !important;
    width:100%;
    box-sizing:border-box;
}

table {
    width:100%;
    margin:20px 0;
    border-collapse:collapse;
    background:#fff;
    box-shadow:0 2px 8px rgba(0,0,0,0.1);
    border-radius:5px;
    overflow:hidden;
    box-sizing:border-box;
    word-wrap:break-word;
    table-layout:auto;
    max-width:100%;
}
th,td {
    border-bottom:1px solid #eee;
    padding:10px;
    text-align:center;
    font-size:14px;
    white-space: normal;
    color:#000 !important;
    box-sizing:border-box;
}
th {
    background:#4CAF50 !important;
    color:#fff !important;
    font-weight:600;
}
tr:hover {
    background:#f7f7f7 !important;
}

canvas {
    max-width:200px;
    margin:20px auto;
    display:block;
    width:auto;
    height:auto;
    box-sizing:border-box;
}

.disk-scroll-wrapper {
    overflow-x:auto;
    width:100%;
    box-sizing:border-box;
}

.dark-mode {
    background:#222;
    color:#eee !important;
}
.dark-mode h1, .dark-mode h2, .dark-mode h3, .dark-mode p, .dark-mode a, .dark-mode .last-updated {
    color:#eee !important;
}
.dark-mode .panel {
    background:#333 !important;
    color:#eee !important;
}
.dark-mode table {
    background:#444 !important;
}
.dark-mode table th {
    background:#4CAF50 !important;
    color:#fff !important;
}
.dark-mode table td, .dark-mode table th {
    color:#eee !important;
}
.dark-mode tr:hover {
    background:#555 !important;
}
.dark-mode .refresh-button, .dark-mode .dark-mode-button {
    background:#4CAF50 !important;
    color:#fff !important;
}
.dark-mode footer {
    background:#333;
    border-top:1px solid #555;
    color:#eee !important;
}

h1,h2,h3,table,canvas,footer,.refresh-container,.last-updated {
    opacity:0;animation:fadeInUp 0.8s ease forwards;
}
.refresh-container { animation-delay:0.4s; }
h1 { animation-delay:0.2s;color:#000 !important; }
h3 { animation-delay:0.25s;color:#555 !important; }
.last-updated { animation-delay:0.3s;color:#666 !important; }
h2 { animation-delay:0.4s;color:#000 !important; }
canvas { animation-delay:0.8s; }
footer {
    animation-delay:1.0s;
    margin-top:8vh;
    text-align:center;
    padding:20px;
    font-size:14px;
    background:#f0f2f5;
    border-top:1px solid #ddd;
    color:#555 !important;
}

.refresh-button,.dark-mode-button {
    background:#4CAF50 !important;
    color:#fff !important;
    padding:10px 20px;
    border:none;
    border-radius:5px;
    cursor:pointer;
    font-size:14px;
    transition:background-color 0.3s ease;
}
.refresh-button:hover,.dark-mode-button:hover {
    background:#45a049 !important;
}

footer .qr-container {
    margin-top:10px;
}
a {
    color:inherit;
    text-decoration:none;
}
a:hover {
    text-decoration:underline;
}
"""
                        }
                        script(src = "https://cdn.jsdelivr.net/npm/chart.js") {}
                    }
                    body {
                        h1 { +"HW-Sence" }
                        h3 {
                            a(href = releasesUrl, target = "_blank") {
                                +"v1.1.3"
                            }
                        }
                        p("now-time") {
                            style = "text-align:center; margin-bottom:20px;"
                            +"$dateNow $now"
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
                        div("last-updated") { +"Last updated: $now" }

                        div("dashboard") {
                            div("panel") {
                                h2 {
                                    title = "Shows overall CPU and Memory usage"
                                    +"CPU & Memory Usage"
                                }
                                table {
                                    tr {
                                        th { title = "Name of the metric";+"Metric" }
                                        th { title = "Current measured value";+"Value" }
                                    }
                                    tr {
                                        td { title = "Total CPU usage";+"Total CPU Usage" }
                                        td {
                                            title =
                                                "Percentage of CPU in use";+if (cpuLoad.isNaN()) "N/A" else "%.2f%%".format(
                                            cpuLoad
                                        )
                                        }
                                    }
                                    tr {
                                        td { title = "Memory usage metric";+"Memory Usage" }
                                        td {
                                            title =
                                                "Percentage of memory in use";+if (memPercent.isNaN()) "N/A" else "%.2f%%".format(
                                            memPercent
                                        ).toString()
                                        }
                                    }
                                }
                                canvas {
                                    id = "cpuGauge"
                                    width = "200"; height = "200"
                                    attributes["data-cpu-used"] = if (cpuLoad.isNaN()) "0.0" else cpuLoad.toString()
                                    title = "CPU Usage Doughnut Chart"
                                }
                                canvas {
                                    id = "memGauge"
                                    width = "200"; height = "200"
                                    attributes["data-mem-used"] =
                                        if (memPercent.isNaN()) "0.0" else memPercent.toString()
                                    title = "Memory Usage Doughnut Chart"
                                }
                            }

                            div("panel") {
                                h2 {
                                    title = "Shows per-core CPU usage"
                                    +"Per-Core CPU Usage"
                                }
                                table {
                                    tr {
                                        th { title = "Logical core number";+"Core Index" }
                                        th { title = "CPU usage on this core";+"Usage (%)" }
                                    }
                                    val rows = coreRows(coreLoads)
                                    rows.forEach { (coreName, usage) ->
                                        tr {
                                            td {
                                                title =
                                                    if (coreName == "N/A") "No data" else "Core identifier";+coreName
                                            }
                                            td {
                                                title =
                                                    if (usage == "N/A") "No data" else "Core usage percentage";+usage
                                            }
                                        }
                                    }
                                }
                            }

                            div("panel") {
                                h2 {
                                    title = "Various CPU and hardware-related metrics"
                                    +"CPU & Hardware Info"
                                }
                                table {
                                    tr {
                                        th { title = "Metric name";+"Item" }
                                        th { title = "Measured value";+"Value" }
                                    }
                                    tr {
                                        td { title = "CPU Temperature";+"CPU Temperature" }
                                        td {
                                            title =
                                                "Current CPU temperature in °C";+if (t == "N/A") "N/A" else "%.2f °C".format(
                                            t
                                        )
                                        }
                                    }
                                    tr {
                                        td { title = "CPU Voltage";+"CPU Voltage" }
                                        td {
                                            title = "Current CPU voltage";+if (v == "N/A") "N/A" else "%.2f V".format(v)
                                        }
                                    }
                                    tr {
                                        td { title = "Max CPU Frequency";+"Max CPU Frequency" }
                                        td {
                                            title =
                                                "Maximum CPU frequency in GHz";+if (maxGhz.isNaN()) "N/A" else "%.2f GHz".format(
                                            maxGhz
                                        )
                                        }
                                    }
                                    if (coreFreqGhz.isEmpty()) {
                                        tr {
                                            td { title = "Core frequencies data not available";+"Core Frequencies" }
                                            td { title = "N/A";+"N/A" }
                                        }
                                    } else {
                                        coreFreqGhz.forEachIndexed { i, f ->
                                            tr {
                                                td { title = "Frequency of core $i";+"Core $i Frequency" }
                                                td {
                                                    title =
                                                        "Frequency in GHz";+if (f.isNaN()) "N/A" else "%.2f GHz".format(
                                                    f
                                                )
                                                }
                                            }
                                        }
                                    }
                                    tr {
                                        td { title = "System uptime";+"System Uptime" }
                                        td { title = "Uptime in hours and minutes";+upStr }
                                    }
                                }
                            }

                            div("panel") {
                                h2 { title = "Top memory consuming processes";+"Top $pc Memory-Consuming Processes" }
                                if (topMemProcesses.isEmpty()) {
                                    table {
                                        tr {
                                            th { title = "Process Name";+"Process" }
                                            th { title = "Memory(MB) used";+"Memory(MB)" }
                                        }
                                        tr {
                                            td { title = "No process data";+"N/A" }
                                            td { title = "No process data";+"N/A" }
                                        }
                                    }
                                } else {
                                    canvas {
                                        id = "memChart"
                                        width = "600"
                                        height = "400"
                                        title = "Memory Usage Bar Chart for Top Processes"
                                        attributes["data-process-names"] = pnJson
                                        attributes["data-process-values"] = pvJson
                                    }
                                }
                            }

                            div("panel") {
                                h2 {
                                    title = "Information about disk storage"
                                    +"Disk Information"
                                }
                                div("disk-scroll-wrapper") {
                                    style = "overflow-x:auto;"
                                    table {
                                        tr {
                                            th { title = "Disk name";+"Name" }
                                            th { title = "Disk model";+"Model" }
                                            th { title = "Disk serial number";+"Serial" }
                                            th { title = "Disk size in GB";+"Size(GB)" }
                                        }
                                        if (disks.isEmpty()) {
                                            tr {
                                                td { title = "No disk data";+"N/A" }
                                                td { title = "No disk data";+"N/A" }
                                                td { title = "No disk data";+"N/A" }
                                                td { title = "No disk data";+"N/A" }
                                            }
                                        } else {
                                            disks.forEach { d ->
                                                val diskName = if (d.name.isBlank()) "N/A" else d.name
                                                val diskModel = if (d.model.isBlank()) "N/A" else d.model
                                                val diskSerial = if (d.serial.isNullOrBlank()) "N/A" else d.serial!!
                                                val diskSizeGB =
                                                    if (d.size > 0) "%.2f".format(d.size / (1024.0 * 1024.0 * 1024.0)) else "N/A"
                                                tr {
                                                    td { title = "Disk name";+diskName }
                                                    td { title = "Disk model";+diskModel }
                                                    td { title = "Disk serial";+diskSerial }
                                                    td { title = "Disk size in GB";+diskSizeGB }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            div("panel") {
                                h2 {
                                    title = "Docker environment metrics"
                                    +"Docker Environment"
                                }
                                table {
                                    tr {
                                        th { title = "Item name";+"Item" }
                                        th { title = "Value of the item";+"Value" }
                                    }
                                    tr {
                                        td { title = "Container HOSTNAME";+"Container HOSTNAME" }
                                        td { title = "Container hostname value";+containerId }
                                    }
                                    tr {
                                        td { title = "CGroup Memory Limit";+"CGroup Memory Limit" }
                                        td { title = "CGroup memory limit in GB";+dockerMemLimit }
                                    }
                                }
                            }
                        }

                        script {
                            unsafe {
                                +"""
const cpuCtx=document.getElementById('cpuGauge');
const timeCtx=document.querySelector('.now-time');
function updateTime() {
  const now = new Date();
  const dateNow = now.toISOString().split('T')[0];
  const timeNow = now.toTimeString().split(' ')[0];

  timeCtx.textContent = dateNow + ' ' + timeNow;
}
setInterval(updateTime, 1000);
if(cpuCtx){
  const cpuUsed=parseFloat(cpuCtx.getAttribute('data-cpu-used'))||0.0;
  new Chart(cpuCtx.getContext('2d'),{
    type:'doughnut',
    data:{
      labels:['Used','Free'],
      datasets:[{
        data:[cpuUsed,100-cpuUsed],
        backgroundColor:['rgba(255,99,132,0.7)','rgba(201,203,207,0.3)'],
        borderColor:['#fff','#fff'],
        borderWidth:1
      }]
    },
    options:{
      cutout:'70%',
      plugins:{
        legend:{display:false},
        title:{
          display:true,
          text:'CPU',
          color:'#000',
          position:'bottom',
          font:{size:16,weight:'bold'}
        },
        tooltip:{
          callbacks:{
            label:function(ctx){return ctx.label+': '+ctx.parsed+'%';}
          }
        }
      }
    }
  });
}

const memG=document.getElementById('memGauge');
if(memG){
  const memUsed=parseFloat(memG.getAttribute('data-mem-used'))||0.0;
  new Chart(memG.getContext('2d'),{
    type:'doughnut',
    data:{
      labels:['Used','Free'],
      datasets:[{
        data:[memUsed,100-memUsed],
        backgroundColor:['rgba(54,162,235,0.7)','rgba(201,203,207,0.3)'],
        borderColor:['#fff','#fff'],
        borderWidth:1
      }]
    },
    options:{
      cutout:'70%',
      plugins:{
        legend:{display:false},
        title:{
          display:true,
          text:'Memory',
          color:'#000',
          position:'bottom',
          font:{size:16,weight:'bold'}
        },
        tooltip:{
          callbacks:{
            label:function(ctx){return ctx.label+': '+ctx.parsed+'%';}
          }
        }
      }
    }
  });
}

const memC=document.getElementById('memChart');
if(memC){
  new Chart(memC.getContext('2d'),{
    type:'bar',
    data:{
      labels:$pnJson,
      datasets:[{
        label:'Memory Usage (MB)',
        data:$pvJson,
        backgroundColor:'rgba(75,192,192,0.2)',
        borderColor:'rgba(75,192,192,1)',
        borderWidth:1
      }]
    },
    options:{
      scales:{y:{beginAtZero:true}}
    }
  });
}
"""
                            }
                        }
                        footer {
                            p {
                                +"Developed by "
                                a(href = devUrl, target = "_blank") { +devName }
                            }
                            p { +"GitHub Repository:" }
                            div("qr-container") {
                                img(src = qrUrl, alt = "QR Code for Repository") { style = "width:120px;height:120px;" }
                            }
                            p {
                                a(href = repoUrl, target = "_blank") { +repoUrl }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun coreRows(coreLoads: List<Double>): List<Pair<String, String>> {
    return if (coreLoads.isEmpty()) {
        List(3) { "N/A" to "N/A" }
    } else {
        if (coreLoads.size < 3) {
            coreLoads.mapIndexed { i, l -> "Core $i" to (if (l.isNaN()) "N/A" else "%.2f%%".format(l)) } +
                    List(3 - coreLoads.size) { "N/A" to "N/A" }
        } else {
            coreLoads.mapIndexed { i, l -> "Core $i" to (if (l.isNaN()) "N/A" else "%.2f%%".format(l)) }
        }
    }
}