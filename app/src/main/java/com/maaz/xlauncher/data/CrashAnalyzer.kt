package com.maaz.xlauncher.data

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * CrashAnalyzer.kt - Reads latest.log from .minecraft and diagnoses crashes
 * Provides auto-fix suggestions for common issues (OutOfMemory, driver errors)
 */
object CrashAnalyzer {
    
    private const val MINECRAFT_LOG_PATH = "/.minecraft/logs/latest.log"
    
    /**
     * Reads the latest Minecraft crash log and analyzes it
     */
    fun analyzeCrashLog(context: Context): CrashLog? {
        val logFile = File(context.filesDir.parent, MINECRAFT_LOG_PATH)
        
        return if (logFile.exists()) {
            try {
                val logContent = logFile.readText()
                parseCrashLog(logContent)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Parses crash log content and identifies error type
     */
    private fun parseCrashLog(logContent: String): CrashLog {
        val lines = logContent.lines()
        val timestamp = System.currentTimeMillis()
        
        var errorType = "Unknown Error"
        var errorMessage = "Check logs for details"
        var canAutoFix = false
        val stackTraceLines = mutableListOf<String>()
        
        // Scan for known errors
        for (line in lines) {
            when {
                line.contains("OutOfMemoryError", ignoreCase = true) -> {
                    errorType = "OUT_OF_MEMORY"
                    errorMessage = "Minecraft ran out of memory. Increase RAM allocation."
                    canAutoFix = true
                }
                line.contains("java.lang.NullPointerException", ignoreCase = true) -> {
                    errorType = "NULL_POINTER_EXCEPTION"
                    errorMessage = "Null pointer exception detected"
                }
                line.contains("Shader compilation failed", ignoreCase = true) -> {
                    errorType = "SHADER_ERROR"
                    errorMessage = "GPU shader compilation failed. Try ANGLE driver."
                    canAutoFix = true
                }
                line.contains("ANGLE", ignoreCase = true) && line.contains("failed", ignoreCase = true) -> {
                    errorType = "DRIVER_ERROR"
                    errorMessage = "Graphics driver error. Disable ANGLE."
                }
                line.startsWith("\tat ") -> {
                    stackTraceLines.add(line)
                }
            }
        }
        
        return CrashLog(
            timestamp = timestamp,
            errorType = errorType,
            errorMessage = errorMessage,
            stackTrace = stackTraceLines.joinToString("\n"),
            canAutoFix = canAutoFix
        )
    }
    
    /**
     * Applies auto-fix based on crash type
     * Returns true if fix was applied successfully
     */
    fun applyAutoFix(crashLog: CrashLog, gameConfig: GameVersion): GameVersion {
        return when (crashLog.errorType) {
            "OUT_OF_MEMORY" -> {
                // Increase RAM from current to 3GB
                gameConfig.copy(
                    ramMB = 3072,
                    javaArgs = "-Xmx3072m -Xms512m -XX:+UseG1GC"
                )
            }
            "SHADER_ERROR" -> {
                // Enable ANGLE driver
                gameConfig.copy(
                    javaArgs = gameConfig.javaArgs + " -Dorg.lwjgl.opengl.Display.allowSoftwareOpenGL=true"
                )
            }
            "DRIVER_ERROR" -> {
                // Disable ANGLE, reduce render distance
                gameConfig.copy(
                    renderDistance = 8,
                    javaArgs = gameConfig.javaArgs.replace("-Dangle", "")
                )
            }
            else -> gameConfig
        }
    }
    
    /**
     * Clears the crash log file
     */
    fun clearCrashLog(context: Context) {
        val logFile = File(context.filesDir.parent, MINECRAFT_LOG_PATH)
        if (logFile.exists()) {
            logFile.delete()
        }
    }
}
