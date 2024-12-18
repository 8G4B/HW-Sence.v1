package com.example

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import oshi.SystemInfo
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

fun Application.configureRouting() {
    routing {
        route("/hw-sence") {
            get("/status") {
                val si = SystemInfo()
                val hw = si.hardware
                val p = hw.processor
                val prevTicks = p.systemCpuLoadTicks
                Thread.sleep(1000)
                val cpuLoad = p.getSystemCpuLoadBetweenTicks(prevTicks)*100.0
                val prevProcTicks = p.processorCpuLoadTicks
                Thread.sleep(1000)
                val coreLoads = p.getProcessorCpuLoadBetweenTicks(prevProcTicks).map{it*100}
                val mem = hw.memory
                val memUsed = mem.total - mem.available
                val memPercent = (memUsed.toDouble()/mem.total)*100.0
                val os = si.operatingSystem
                val pc=5
                val topMemProcesses = os.processes.sortedByDescending{it.residentSetSize}.take(pc).map{it.name to it.residentSetSize}
                val pn = topMemProcesses.map{it.first}
                val pv = topMemProcesses.map{it.second.toDouble()/(1024*1024)}
                val pnJson = pn.joinToString(prefix="[",postfix="]"){ "\"$it\"" }
                val pvJson = pv.joinToString(prefix="[",postfix="]")
                val s = hw.sensors
                val t = s.cpuTemperature
                val v = s.cpuVoltage
                val maxHz = p.maxFreq
                val maxGhz = maxHz/1_000_000_000.0
                val freqs = p.currentFreq.map{it/1_000_000_000.0}
                val upSec = os.systemUptime.toLong()
                val upH=upSec/3600
                val upM=(upSec%3600)/60
                val upStr = "${upH}h ${upM}m"
                val la = p.getSystemLoadAverage(3)
                val l1 = la.getOrElse(0){Double.NaN}
                val l5 = la.getOrElse(1){Double.NaN}
                val l15 = la.getOrElse(2){Double.NaN}
                val disks = hw.diskStores
                val devName = "snowykte0426"
                val devUrl = "https://www.github.com/snowykte0426"
                val repoUrl = "https://github.com/8G4B/HW-Sence"
                val qrUrl = "https://raw.githubusercontent.com/8G4B/HW-Sence/main/src/main/resources/static/image/repo_qr.png"
                val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                val containerId = System.getenv("HOSTNAME") ?: "Unknown"
                val memLimitFile = File("/sys/fs/cgroup/memory/memory.limit_in_bytes")
                val dockerMemLimit = if(memLimitFile.exists()) {
                    val limitStr = memLimitFile.readText().trim()
                    if(limitStr == "max" || limitStr.toLongOrNull() == null) "N/A"
                    else {
                        val bytes = limitStr.toLong()
                        val gb = bytes/(1024.0*1024.0*1024.0)
                        "%.2f GB".format(gb)
                    }
                } else "N/A"

                call.respondHtml {
                    head {
                        title("HW-Sence | System Status")
                        style {
                            +"""
@keyframes fadeInUp{0%{opacity:0;transform:translateY(20px);}100%{opacity:1;transform:translateY(0);}}
body {
    font-family:Arial,sans-serif;
    margin:20px;
    background:#f9f9f9;
    display:flex;
    flex-direction:column;
    min-height:100vh;
    opacity:0;animation:fadeInUp 1s ease forwards;
    color:#333;
}
.dark-mode {
    background:#222;
    color:#eee;
}
.dark-mode .panel {
    background:#333;
    color:#eee;
}
.dark-mode table {
    background:#444;
}
.dark-mode table th {
    background:#4CAF50;
    color:#fff;
}
.dark-mode table td, .dark-mode table th {
    color:#eee;
}
.dark-mode tr:hover {
    background:#555;
}
.dark-mode .refresh-button, .dark-mode .dark-mode-button {
    background:#4CAF50;
    color:#fff;
}
h1,h2,h3,table,canvas,footer,.refresh-container,.last-updated,.dashboard {
    opacity:0;animation:fadeInUp 0.8s ease forwards;
}
.refresh-container {
    animation-delay:0.4s;
}
h1 {
    animation-delay:0.2s;
}
.last-updated {
    animation-delay:0.3s;
}
h2 {
    animation-delay:0.4s;
}
.dashboard {
    animation-delay:0.5s;
}
canvas {
    animation-delay:0.8s;
}
footer {
    animation-delay:1.0s;
}
h3 {
    animation-delay:0.5s;
}
canvas {
    max-width:300px;
    margin:20px auto;
    display:block;
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
    color:#333;
}
th,td {
    border-bottom:1px solid #eee;
    padding:10px;
    text-align:center;
    font-size:14px;
    white-space: normal;
    color:#333;
}
th {
    background:#4CAF50;
    color:#fff;
    font-weight:600;
}
tr:hover {
    background:#f7f7f7;
}
h3 {
    text-align:center;
    font-weight:500;
    margin:20px 0 10px 0;
    color:#555;
}
footer {
    margin-top:auto;
    text-align:center;
    padding:20px;
    font-size:14px;
    color:#555;
    background:#f0f2f5;
    border-top:1px solid #ddd;
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
.refresh-container {
    display:flex;
    justify-content:center;
    margin-top:20px;
    gap:10px;
}
.refresh-button,.dark-mode-button {
    background:#4CAF50;
    color:#fff;
    padding:10px 20px;
    border:none;
    border-radius:5px;
    cursor:pointer;
    font-size:14px;
    transition:background-color 0.3s ease;
}
.refresh-button:hover,.dark-mode-button:hover {
    background:#45a049;
}
.last-updated {
    text-align:center;
    margin-top:10px;
    font-size:14px;
    color:#666;
}
.dashboard {
    display:flex;
    flex-wrap:wrap;
    gap:20px;
    margin-top:30px;
    justify-content:center;
    align-items:flex-start;
    box-sizing:border-box;
}
.panel {
    background:#fff;
    box-shadow:0 2px 8px rgba(0,0,0,0.1);
    border-radius:5px;
    padding:20px;
    flex:1 1 300px;
    max-width:500px;
    display:flex;
    flex-direction:column;
    box-sizing:border-box;
    overflow:auto;
    color:#333;
}
.panel h2 {
    margin-top:0;
    margin-bottom:20px;
    font-size:18px;
    color:#333;
    text-align:center;
}
.panel h3 {
    margin-top:0;
}
"""
                        }
                        script(src="https://cdn.jsdelivr.net/npm/chart.js"){}
                    }
                    body {
                        div("refresh-container") {
                            button(type=ButtonType.button,classes="refresh-button") {
                                attributes["onclick"]="location.reload();"
                                +"Refresh"
                            }
                            button(type=ButtonType.button,classes="dark-mode-button") {
                                attributes["onclick"]="document.body.classList.toggle('dark-mode');"
                                +"Dark Mode"
                            }
                        }
                        h1 { +"System Status" }
                        div("last-updated") { +"Last updated: $now" }
                        div("dashboard") {
                            div("panel") {
                                h2 {
                                    attributes["title"]="Overall CPU and Memory usage metrics"
                                    +"CPU & Memory Usage"
                                }
                                table {
                                    tr {
                                        th {
                                            attributes["title"]="Name of the metric"
                                            +"Metric"
                                        }
                                        th {
                                            attributes["title"]="Current measured value"
                                            +"Value"
                                        }
                                    }
                                    tr {
                                        td { +"Total CPU Usage" }
                                        td { "%.2f%%".format(cpuLoad) }
                                    }
                                    tr {
                                        td { +"Memory Usage" }
                                        td { "%.2f%%".format(memPercent) }
                                    }
                                }
                                canvas { id="cpuGauge"; width="250"; height="250" }
                            }
                            div("panel") {
                                h2 {
                                    attributes["title"]="CPU usage per logical core"
                                    +"Per-Core CPU Usage"
                                }
                                table {
                                    tr {
                                        th {
                                            attributes["title"]="Logical core number"
                                            +"Core Index"
                                        }
                                        th {
                                            attributes["title"]="CPU usage on this core"
                                            +"Usage (%)"
                                        }
                                    }
                                    coreLoads.forEachIndexed {i,l->tr{td{"Core $i"};td{"%.2f%%".format(l)}}}
                                }
                            }
                            div("panel") {
                                h2 {
                                    attributes["title"]="Various CPU and hardware-related metrics"
                                    +"CPU & Hardware Info"
                                }
                                table {
                                    tr {
                                        th {
                                            attributes["title"]="Metric name"
                                            +"Item"
                                        }
                                        th {
                                            attributes["title"]="Measured value"
                                            +"Value"
                                        }
                                    }
                                    tr {
                                        td {+"CPU Temperature"}
                                        td {"%.2f °C".format(t)}
                                    }
                                    tr {
                                        td {+"CPU Voltage"}
                                        td {"$v V"}
                                    }
                                    tr {
                                        td {+"Max CPU Frequency"}
                                        td {"%.2f GHz".format(maxGhz)}
                                    }
                                    freqs.forEachIndexed{i,f->tr{td{"Core $i Frequency"};td{"%.2f GHz".format(f)}}}
                                    tr {
                                        td {
                                            attributes["title"]="Time since the system started"
                                            +"System Uptime"
                                        }
                                        td {+upStr}
                                    }
                                    tr {
                                        td {
                                            attributes["title"]="Average load over last 1,5,15 minutes"
                                            +"Load Average (1/5/15min)"
                                        }
                                        td {"${l1.roundToInt()} / ${l5.roundToInt()} / ${l15.roundToInt()}"}
                                    }
                                }
                            }
                            div("panel") {
                                h2 {
                                    attributes["title"]="Top $pc memory consuming processes"
                                    +"Top $pc Memory-Consuming Processes"
                                }
                                canvas { id="memChart"; width="600"; height="400" }
                            }
                            div("panel") {
                                h2 {
                                    attributes["title"]="Information about disk storage"
                                    +"Disk Information"
                                }
                                table {
                                    tr {
                                        th {
                                            attributes["title"]="Disk name"
                                            +"Name"
                                        }
                                        th {
                                            attributes["title"]="Disk model"
                                            +"Model"
                                        }
                                        th {
                                            attributes["title"]="Disk serial number"
                                            +"Serial"
                                        }
                                        th {
                                            attributes["title"]="Disk size in GB"
                                            +"Size(GB)"
                                        }
                                    }
                                    disks.forEach{d->
                                        val diskName = d.name.ifBlank { "/dev/vda" }
                                        val diskModel = if(d.model.isBlank()) "unknown" else d.model
                                        val diskSerial = d.serial ?: "unknown"
                                        val diskSizeGB = if(d.size > 0) "%.2f".format(d.size/(1024.0*1024.0*1024.0)) else "unknown"
                                        tr {
                                            td { +diskName }
                                            td { +diskModel }
                                            td { +diskSerial }
                                            td { +diskSizeGB }
                                        }
                                    }
                                }
                            }
                            div("panel") {
                                h2 {
                                    attributes["title"]="Docker environment metrics"
                                    +"Docker Environment"
                                }
                                table {
                                    tr {
                                        th {+"Item"}
                                        th {+"Value"}
                                    }
                                    tr {
                                        td {+"Container HOSTNAME"}
                                        td {+containerId}
                                    }
                                    tr {
                                        td {+"CGroup Memory Limit"}
                                        td {+dockerMemLimit}
                                    }
                                }
                            }
                        }
                        script {
                            unsafe {
                                +"""
const cpuCtx=document.getElementById('cpuGauge').getContext('2d');
new Chart(cpuCtx,{
  type:'doughnut',
  data:{
    labels:['Used','Free'],
    datasets:[{
      data:[${cpuLoad},${100.0-cpuLoad}],
      backgroundColor:['rgba(255,99,132,0.7)','rgba(201,203,207,0.3)'],
      borderColor:['#fff','#fff'],
      borderWidth:1
    }]
  },
  options:{
    cutout:'70%',
    plugins:{
      legend:{display:false},
      tooltip:{
        callbacks:{
          label:function(ctx){return ctx.label+': '+ctx.parsed+'%';}
        }
      }
    }
  }
});
const memCtx=document.getElementById('memChart').getContext('2d');
new Chart(memCtx,{
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
"""
                            }
                        }
                        footer {
                            p {
                                +"Developed by "
                                a(href=devUrl,target="_blank"){+devName}
                            }
                            p { +"GitHub Repository:" }
                            div("qr-container") {
                                img(src=qrUrl,alt="QR Code for Repository"){style="width:120px;height:120px;"}
                            }
                            p {
                                a(href=repoUrl,target="_blank"){+repoUrl}
                            }
                        }
                    }
                }
            }
        }
    }
}