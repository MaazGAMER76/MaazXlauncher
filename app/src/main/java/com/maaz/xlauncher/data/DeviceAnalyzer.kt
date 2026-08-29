package com.maaz.xlauncher.data

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import kotlin.math.roundToInt

/**
 * DeviceInfo.kt - Auto-detects device hardware specs (CPU, GPU, RAM)
 * Used by CrashAnalyzer and LaunchHelper to optimize game settings
 */
data class DeviceSpecs(
    val cpuModel: String,
    val gpuModel: String,
    val ramMB: Int,
    val androidVersion: Int,
    val isUnisocT620: Boolean,
    val recommendedRamMB: Int,
    val recommendedRenderDistance: Int,
    val useANGLE: Boolean
)

object DeviceAnalyzer {
    
    /**
     * Analyzes device hardware and returns optimization recommendations
     */
    fun analyzeDevice(context: Context): DeviceSpecs {
        val cpuModel = getCpuModel()
        val gpuModel = getGpuModel()
        val ramMB = getTotalRamMB(context)
        val androidVersion = Build.VERSION.SDK_INT
        
        val isUnisocT620 = cpuModel.contains("T620", ignoreCase = true)
        
        // Auto-optimize for weak chips
        val (recommendedRam, renderDistance, useAngle) = when {
            isUnisocT620 -> Triple(2048, 8, true)  // Unisoc T620: 2GB RAM, render distance 8
            ramMB <= 2048 -> Triple(1536, 6, true)  // Low RAM: 1.5GB, render distance 6
            ramMB <= 4096 -> Triple(2048, 10, false) // Medium RAM: 2GB, render distance 10
            else -> Triple(3072, 16, false)          // High RAM: 3GB, render distance 16
        }
        
        return DeviceSpecs(
            cpuModel = cpuModel,
            gpuModel = gpuModel,
            ramMB = ramMB,
            androidVersion = androidVersion,
            isUnisocT620 = isUnisocT620,
            recommendedRamMB = recommendedRam,
            recommendedRenderDistance = renderDistance,
            useANGLE = useAngle
        )
    }
    
    /**
     * Extracts CPU model from system properties
     */
    private fun getCpuModel(): String {
        return try {
            val process = Runtime.getRuntime().exec("cat /proc/cpuinfo")
            val bufferedReader = process.inputStream.bufferedReader()
            var line: String?
            var model = "Unknown"
            
            while (bufferedReader.readLine().also { line = it } != null) {
                if (line?.contains("Hardware", ignoreCase = true) == true) {
                    model = line?.substringAfter(":")?.trim() ?: "Unknown"
                    break
                }
            }
            bufferedReader.close()
            model
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Extracts GPU model from Build properties
     */
    private fun getGpuModel(): String {
        return try {
            val glRenderer = android.opengl.GLES20.glGetString(android.opengl.GLES20.GL_RENDERER)
            glRenderer ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Gets total device RAM in MB
     */
    private fun getTotalRamMB(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return (memInfo.totalMemory / (1024 * 1024)).toInt()
    }
}
