package com.mail2dev.upperdot.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object MiuiPermissionUtils {
    private const val TAG = "MiuiPermissionUtils"

    fun isMiui(): Boolean {
        return try {
            val p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.name")
            val input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            val line = input.readLine()
            input.close()
            !line.isNullOrBlank()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking MIUI property", e)
            Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)
        }
    }

    fun getMiuiPermissionEditorIntent(context: Context): Intent {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
        intent.putExtra("extra_pkgname", context.packageName)
        
        return if (isIntentAvailable(context, intent)) {
            intent
        } else {
            // Fallback to MIUI settings main page or standard app details
            val fallback = Intent("miui.intent.action.APP_PERM_EDITOR")
            fallback.putExtra("extra_pkgname", context.packageName)
            if (isIntentAvailable(context, fallback)) {
                fallback
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            }
        }
    }

    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        return try {
            val packageManager = context.packageManager
            val list = packageManager.queryIntentActivities(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
            list.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }
}
