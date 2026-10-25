package dev.cyberdeck.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager


internal fun loadLaunchableApps(
    context: Context
): List<LaunchableApp> {
    val packageManager = context.packageManager

    val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    return packageManager
        .queryIntentActivities(launcherIntent, 0)
        .mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo
                ?: return@mapNotNull null

            val appInfo = activityInfo.applicationInfo
                ?: return@mapNotNull null

            val name = packageManager
                .getApplicationLabel(appInfo)
                .toString()
                .trim()

            if (name.isBlank()) {
                null
            } else {
                LaunchableApp(
                    name = name,
                    packageName = activityInfo.packageName
                )
            }
        }
        .distinctBy { it.packageName }
        .sortedBy { it.name.lowercase() }
}

internal fun launchApp(
    context: Context,
    app: LaunchableApp
): Boolean {
    val launchIntent = context.packageManager
        .getLaunchIntentForPackage(app.packageName)
        ?: return false

    return try {
        context.startActivity(launchIntent)
        true
    } catch (_: android.content.ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}