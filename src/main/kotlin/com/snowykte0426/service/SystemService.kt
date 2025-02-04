package com.snowykte0426.service

import com.snowykte0426.model.DiskInfo
import com.snowykte0426.model.SystemStatus
import oshi.SystemInfo
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun fetchSystemStatus(): SystemStatus {
    val si = SystemInfo()
    val hw = si.hardware
    val processor = hw.processor

    val prevTicks = processor.systemCpuLoadTicks
    Thread.sleep(1000)
    val rawCpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks)
    val cpuLoad = if (rawCpuLoad.isNaN()) Double.NaN else rawCpuLoad * 100.0

    val memory = hw.memory
    val memUsed = memory.total - memory.available
    val memPercent = if (memory.total > 0) (memUsed.toDouble() / memory.total) * 100.0 else Double.NaN

    val prevProcTicks = processor.processorCpuLoadTicks
    Thread.sleep(1000)
    val coreLoadValues = processor.getProcessorCpuLoadBetweenTicks(prevProcTicks)
    val coreLoads = if (coreLoadValues.any { it.isNaN() }) emptyList<Double>() else coreLoadValues.map { it * 100 }

    val os = si.operatingSystem
    val topProcCount = 5
    val processes = os.processes
    val topMemProcesses = if (processes.isEmpty()) emptyList<Pair<String, Long>>() else
        processes.sortedByDescending { it.residentSetSize }
            .take(topProcCount)
            .map { it.name to it.residentSetSize }
    val processNames = topMemProcesses.map { it.first }
    val processMemoryMB = topMemProcesses.map { it.second.toDouble() / (1024 * 1024) }

    val sensors = hw.sensors
    val cpuTemperature = if (sensors.cpuTemperature.isNaN() || sensors.cpuTemperature <= 20) "N/A" else "%.2f".format(sensors.cpuTemperature)
    val cpuVoltage = if (sensors.cpuVoltage.isNaN() || sensors.cpuVoltage <= 1) "N/A" else "%.2f".format(sensors.cpuVoltage)

    val maxHz = processor.maxFreq
    val maxGhz = if (maxHz > 0) maxHz / 1_000_000_000.0 else Double.NaN
    val freqs = processor.currentFreq
    val coreFreqGhz = if (freqs.isEmpty()) emptyList<Double>() else freqs.map { if (it > 0) it / 1_000_000_000.0 else Double.NaN }

    val upSec = os.systemUptime.toLong()
    val upH = upSec / 3600
    val upM = (upSec % 3600) / 60
    val systemUptime = if (upSec > 0) "${upH}h ${upM}m" else "N/A"

    val diskStores = hw.diskStores
    val disks = if (diskStores.isEmpty()) emptyList<DiskInfo>() else diskStores.map { d ->
        val name = if (d.name.isBlank()) "N/A" else d.name
        val model = if (d.model.isBlank()) "N/A" else d.model
        val serial = if (d.serial.isNullOrBlank()) "N/A" else d.serial!!
        val sizeGB = if (d.size > 0) "%.2f".format(d.size / (1024.0 * 1024.0 * 1024.0)) else "N/A"
        DiskInfo(name, model, serial, sizeGB)
    }

    val containerId = System.getenv("HOSTNAME") ?: "Unknown"
    val memLimitFile = File("/sys/fs/cgroup/memory/memory.limit_in_bytes")
    val dockerMemLimit = if (memLimitFile.exists()) {
        val limitStr = memLimitFile.readText().trim()
        if (limitStr == "max" || limitStr.toLongOrNull() == null) "N/A" else {
            val bytes = limitStr.toLong()
            "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
        }
    } else "N/A"
    val totalMemory = "%.2f GB".format(memory.total / (1024.0 * 1024.0 * 1024.0))
    val usedMemory = "%.2f GB".format(memUsed / (1024.0 * 1024.0 * 1024.0))
    val freeMemory = "%.2f GB".format(memory.available / (1024.0 * 1024.0 * 1024.0))
    val osName = os.family

    val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val dateNow = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    return SystemStatus(
        cpuLoad = cpuLoad,
        memPercent = memPercent,
        coreLoads = coreLoads,
        processNames = processNames,
        processMemoryMB = processMemoryMB,
        cpuTemperature = cpuTemperature,
        cpuVoltage = cpuVoltage,
        maxGhz = maxGhz,
        coreFreqGhz = coreFreqGhz,
        systemUptime = systemUptime,
        disks = disks,
        containerId = containerId,
        dockerMemLimit = dockerMemLimit,
        currentTime = now,
        currentDate = dateNow,
        totalMemory = totalMemory,
        usedMemory = usedMemory,
        freeMemory = freeMemory,
        osName = osName
    )
}