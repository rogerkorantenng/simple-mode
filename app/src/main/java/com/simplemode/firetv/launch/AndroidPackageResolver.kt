package com.simplemode.firetv.launch

import android.content.Context

/** The real [PackageResolver], backed by the device's own PackageManager. */
class AndroidPackageResolver(private val context: Context) : PackageResolver {
    override fun isInstalled(packageName: String): Boolean =
        context.packageManager.getLaunchIntentForPackage(packageName) != null
}
