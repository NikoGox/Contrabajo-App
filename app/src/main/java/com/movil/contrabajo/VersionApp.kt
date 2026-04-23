package com.movil.contrabajo

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

fun obtenerVersionApp(context: Context): String {
    return runCatching {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }

        packageInfo.versionName?.takeIf { it.isNotBlank() } ?: "0.0.0"
    }.getOrElse { "0.0.0" }
}

@Composable
fun recordarVersionApp(): String {
    val context = LocalContext.current
    return remember(context) { obtenerVersionApp(context) }
}
