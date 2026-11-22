package io.github.nexfusion

import io.github.a092devs.nexfusion.AppPatchInfo as ModernAppPatchInfo
import io.github.a092devs.nexfusion.AppTarget as ModernAppTarget
import io.github.a092devs.nexfusion.Patch as ModernPatch
import io.github.a092devs.nexfusion.PatchBundle as ModernPatchBundle

typealias Patch = ModernPatch
typealias PatchSession = io.github.a092devs.nexfusion.PatchSession
typealias BundleInfo = io.github.a092devs.nexfusion.BundleInfo
typealias PatcherLog = io.github.a092devs.nexfusion.PatcherLog

open class AppTarget @JvmOverloads constructor(
    version: String,
    experimental: Boolean = false,
) : ModernAppTarget(version, experimental)

open class AppPatchInfo @JvmOverloads constructor(
    appName: String,
    packageName: String,
    patches: Array<ModernPatch>,
    targets: List<ModernAppTarget> = emptyList(),
    appIconColor: Int? = null,
) : ModernAppPatchInfo(appName, packageName, patches, targets, appIconColor)

interface PatchBundle : ModernPatchBundle
