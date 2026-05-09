package com.personalbiography.ui.settings

internal fun uninstallUriForPackage(packageName: String): String {
    require(packageName.isNotBlank()) { "packageName must not be blank" }
    return "package:$packageName"
}
