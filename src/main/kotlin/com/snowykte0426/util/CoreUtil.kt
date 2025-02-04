package com.snowykte0426.util

fun generateCoreRows(coreLoads: List<Double>): List<Pair<String, String>> {
    return if (coreLoads.isEmpty()) {
        List(3) { "N/A" to "N/A" }
    } else {
        if (coreLoads.size < 3) {
            coreLoads.mapIndexed { i, load -> "Core $i" to (if (load.isNaN()) "N/A" else "%.2f%%".format(load)) } +
                    List(3 - coreLoads.size) { "N/A" to "N/A" }
        } else {
            coreLoads.mapIndexed { i, load -> "Core $i" to (if (load.isNaN()) "N/A" else "%.2f%%".format(load)) }
        }
    }
}