package com.bl4ckswordsman.cerberustiles

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.bl4ckswordsman.cerberustiles.Constants.UNKNOWN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.URL

/**
 * The version manager of the app.
 */
class VersionManager {
    private val repoOwner = "bl4ckswordsman"
    private val repoName = "CerberusTiles"
    private val repoApiUrl = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"

    /**
     * Returns the app version.
     * @param context The context of the app.
     * @return The app version.
     */
    fun getCurrentAppVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            UNKNOWN
        }
    }

    /**
     * Returns the latest release version of the app from the GitHub repo.
     * @return The latest release version.
     */
    suspend fun getLatestReleaseVersion(): String {
        return withContext(Dispatchers.IO) {
            try {
                val json = URL(repoApiUrl).readText()
                val jsonObj = JSONObject(json)
                jsonObj.getString("tag_name")
            } catch (e: Exception) {
                e.printStackTrace()
                UNKNOWN
            }
        }
    }

    /**
     * Returns the release notes of the latest release from the GitHub repo.
     * @return The release notes of the latest release.
     */
    suspend fun getLatestReleaseNotes(): String {
        return withContext(Dispatchers.IO) {
            try {
                val json = URL(repoApiUrl).readText()
                val jsonObj = JSONObject(json)
                jsonObj.getString("body")
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                UNKNOWN
            } catch (e: JSONException) {
                e.printStackTrace()
                UNKNOWN
            }
        }
    }

    /**
     * Returns the APK download URL of the latest release from the GitHub repo.
     * @return The APK download URL of the latest release.
     */
    suspend fun getLatestReleaseApkUrl(): String {
        return withContext(Dispatchers.IO) {
            var apkUrl = ""
            try {
                val json = URL(repoApiUrl).readText()
                val jsonObj = JSONObject(json)
                val assets = jsonObj.getJSONArray("assets")
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name").endsWith(".apk")) {
                        apkUrl = asset.getString("browser_download_url")
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            apkUrl
        }
    }

    /**
     * Checks if the version string is valid.
     */
    private fun isValidVersion(version: String): Boolean {
        return version.isNotEmpty() && version.all { it.isDigit() || it == '.' || it == 'v' } && version.contains(".")
    }

    /**
     * Parses the version string into a list of integers.
     */
    fun parseVersion(version: String): List<Int> {
        return if (isValidVersion(version)) {
            val versionWithoutPrefix = if (version.startsWith("v")) version.removePrefix("v") else version
            if (versionWithoutPrefix.contains(".")) {
                versionWithoutPrefix.split(".").map { it.toInt() }
            } else {
                listOf(versionWithoutPrefix.toInt())
            }
        } else {
            Log.d("Parse Error", "Invalid version: $version")
            listOf(0)
        }
    }

    /**
     * Fetches and parses the latest release info from the GitHub repo.
     * @param context The context of the app.
     * @return The latest release info.
     */
    suspend fun fetchAndParseVersionInfo(context: Context, versionManager: VersionManager): Pair<Boolean, ReleaseInfo> {
        val releaseInfo = fetchLatestReleaseInfo(context)

        // Parse the version numbers
        val currentVersionNumbers = versionManager.parseVersion(releaseInfo.currentVersion)
        val latestVersionNumbers = versionManager.parseVersion(releaseInfo.latestVersion)

        val maxLength = maxOf(currentVersionNumbers.size, latestVersionNumbers.size)
        val paddedCurrentVersionNumbers = currentVersionNumbers.padEnd(maxLength, 0)
        val paddedLatestVersionNumbers = latestVersionNumbers.padEnd(maxLength, 0)
        val isUpdateAvailable = paddedCurrentVersionNumbers.zip(paddedLatestVersionNumbers)
            .any { (current: Int, latest: Int) -> current < latest }

        return Pair(isUpdateAvailable, releaseInfo)
    }

}