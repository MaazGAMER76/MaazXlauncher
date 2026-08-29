package com.maaz.xlauncher.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * AccountData.kt - Stores account information for both Microsoft and Offline modes
 */
@Serializable
@Immutable
data class GameAccount(
    val id: String,
    val username: String,
    val uuid: String = "",
    val isMicrosoft: Boolean = false,
    val accessToken: String = "",
    val skinPath: String = "",
    val isActive: Boolean = false
)

@Serializable
@Immutable
data class GameVersion(
    val versionId: String,
    val releaseType: String, // "release", "snapshot", "old_alpha", "old_beta"
    val installed: Boolean = false,
    val installPath: String = "",
    val javaArgs: String = "",
    val ramMB: Int = 2048,
    val renderDistance: Int = 10
)

@Serializable
@Immutable
data class CrashLog(
    val timestamp: Long,
    val errorType: String,
    val errorMessage: String,
    val stackTrace: String,
    val canAutoFix: Boolean = false
)

/**
 * Represents the launcher's global configuration state
 */
@Serializable
@Immutable
data class LauncherConfig(
    val selectedAccount: GameAccount? = null,
    val selectedVersion: String = "1.21.1",
    val ramAllocationMB: Int = 2048,
    val autoDetectSettings: Boolean = true,
    val useANGLEDriver: Boolean = false,
    val lastLaunchTime: Long = 0L
)
