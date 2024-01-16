package com.bl4ckswordsman.cerberustiles

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
            "Unknown"
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
                "Unknown"
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
            } catch (e: Exception) {
                e.printStackTrace()
                "Unknown"
            }
        }
    }

    /**
     * Returns the APK download URL of the latest release from the GitHub repo.
     * @return The APK download URL of the latest release.
     */
    suspend fun getLatestReleaseApkUrl(): String {
        return withContext(Dispatchers.IO) {
            try {
                val json = URL(repoApiUrl).readText()
                val jsonObj = JSONObject(json)
                val assets = jsonObj.getJSONArray("assets")
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name").endsWith(".apk")) {
                        return@withContext asset.getString("browser_download_url")
                    }
                }
                ""
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

}