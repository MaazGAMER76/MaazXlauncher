package com.maaz.xlauncher.engine

import android.content.Context
import com.maaz.xlauncher.data.DeviceAnalyzer
import com.maaz.xlauncher.data.GameVersion
import java.io.File

/**
 * LaunchHelper.kt - Handles Minecraft Java process launching via ProcessBuilder
 * Uses bundled OpenJDK 21 ARM64 and LWJGL 3.3.2
 * Applies device-specific optimizations (RAM, render distance, ANGLE driver)
 */
object LaunchHelper {
    
    private const val JAVA_BINARY_PATH = "/data/data/com.maaz.xlauncher/java/bin/java"
    private const val MINECRAFT_JAR_PATH = "/data/data/com.maaz.xlauncher/minecraft/minecraft.jar"
    private const val LWJGL_PATH = "/data/data/com.maaz.xlauncher/lwjgl/"
    private const val ANGLE_DRIVER_PATH = "/data/data/com.maaz.xlauncher/angle/"
    
    /**
     * Launches Minecraft Java with optimized JVM arguments
     * Returns the Process object for monitoring
     */
    fun launchMinecraft(
        context: Context,
        gameVersion: GameVersion,
        username: String,
        uuid: String = ""
    ): Process? {
        return try {
            val deviceSpecs = DeviceAnalyzer.analyzeDevice(context)
            
            // Build Java arguments
            val javaArgs = buildJavaArgs(gameVersion, deviceSpecs)
            
            // Build game arguments
            val gameArgs = buildGameArgs(username, uuid, gameVersion, deviceSpecs)
            
            // Combine all arguments
            val commands = mutableListOf(JAVA_BINARY_PATH)
            commands.addAll(javaArgs)
            commands.add("-jar")
            commands.add(MINECRAFT_JAR_PATH)
            commands.addAll(gameArgs)
            
            // Set environment variables
            val environment = mapOf(
                "LD_LIBRARY_PATH" to LWJGL_PATH,
                "JAVA_HOME" to "/data/data/com.maaz.xlauncher/java",
                "PATH" to "/data/data/com.maaz.xlauncher/java/bin:/system/bin:/system/xbin"
            )
            
            // Launch process
            val processBuilder = ProcessBuilder(commands)
            processBuilder.environment().putAll(environment)
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            
            // Log output
            process.inputStream.bufferedReader().forEachLine { line ->
                android.util.Log.d("Minecraft", line)
            }
            
            process
        } catch (e: Exception) {
            android.util.Log.e("LaunchHelper", "Failed to launch Minecraft", e)
            null
        }
    }
    
    /**
     * Builds JVM arguments with memory and optimization flags
     */
    private fun buildJavaArgs(gameVersion: GameVersion, deviceSpecs: com.maaz.xlauncher.data.DeviceSpecs): List<String> {
        val ramMB = gameVersion.ramMB.coerceAtMost(deviceSpecs.ramMB - 512)
        
        return listOf(
            "-Xmx${ramMB}m",
            "-Xms512m",
            "-XX:+UseG1GC",
            "-XX:G1HeapRegionSize=4M",
            "-XX:G1ReservePercent=20",
            "-XX:G1NewCollectionThreads=8",
            "-XX:G1MaxNewGenPercent=30",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:G1MixedGCLiveThresholdPercent=90",
            "-XX:InitiatingHeapOccupancyPercent=35",
            "-Dorg.lwjgl.util.Debug=false",
            "-Dorg.lwjgl.opengl.Display.fullscreen=true"
        ).toMutableList().apply {
            // Add ANGLE driver if recommended
            if (deviceSpecs.useANGLE) {
                add("-Dorg.lwjgl.opengl.Display.allowSoftwareOpenGL=true")
                add("-Dangle.enabled=true")
            }
        }
    }
    
    /**
     * Builds Minecraft game launch arguments
     */
    private fun buildGameArgs(
        username: String,
        uuid: String,
        gameVersion: GameVersion,
        deviceSpecs: com.maaz.xlauncher.data.DeviceSpecs
    ): List<String> {
        return listOf(
            "--username=$username",
            "--uuid=$uuid",
            "--version=${gameVersion.versionId}",
            "--gameDir=/sdcard/.minecraft",
            "--assetsDir=/sdcard/.minecraft/assets",
            "--assetIndex=${gameVersion.versionId}",
            "-Dfml.queryResult=confirm",
            "-Dorg.lwjgl.librarypath=$LWJGL_PATH",
            "-Dlogback.configurationFile=/data/data/com.maaz.xlauncher/logback.xml",
            "-Dcom.sun.management.jmxremote=false",
            "-Dcom.sun.management.jmxremote.port=9010"
        ).toMutableList().apply {
            // Add render distance optimization
            add("-Dminecraft.renderDistance=${gameVersion.renderDistance}")
            
            // Add Unisoc T620 specific tweaks
            if (deviceSpecs.isUnisocT620) {
                add("-XX:+UseStringDeduplication")
                add("-XX:StringDeduplicationAgeThreshold=3")
            }
        }
    }
    
    /**
     * Kills the Minecraft process gracefully
     */
    fun stopMinecraft(process: Process?) {
        try {
            process?.destroy()
            // Wait up to 5 seconds for graceful shutdown
            process?.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
            // Force kill if still running
            if (process?.isAlive == true) {
                process.destroyForcibly()
            }
        } catch (e: Exception) {
            android.util.Log.e("LaunchHelper", "Error stopping Minecraft", e)
        }
    }
    
    /**
     * Checks if Minecraft process is running
     */
    fun isMinecraftRunning(process: Process?): Boolean {
        return process?.isAlive == true
    }
}
