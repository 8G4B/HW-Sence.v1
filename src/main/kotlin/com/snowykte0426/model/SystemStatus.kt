package com.snowykte0426.model

data class SystemStatus(
    val cpuLoad: Double,
    val memPercent: Double,
    val coreLoads: List<Double>,
    val processNames: List<String>,
    val processMemoryMB: List<Double>,
    val cpuTemperature: String,
    val cpuVoltage: String,
    val maxGhz: Double,
    val coreFreqGhz: List<Double>,
    val systemUptime: String,
    val disks: List<DiskInfo>,
    val containerId: String,
    val dockerMemLimit: String,
    val currentTime: String,
    val currentDate: String,
    val totalMemory: String,
    val usedMemory: String,
    val freeMemory: String,
    val osName: String
)

data class DiskInfo(
    val name: String,
    val model: String,
    val serial: String,
    val sizeGB: String
)