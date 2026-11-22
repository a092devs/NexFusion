package io.github.nexfusion

import android.app.Application
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface
import org.luckypray.dexkit.DexKitCacheBridge
import io.github.a092devs.nexfusion.AppPatchInfo as ModernAppPatchInfo
import io.github.a092devs.nexfusion.AppTarget as ModernAppTarget
import io.github.a092devs.nexfusion.BundleInfo as ModernBundleInfo
import io.github.a092devs.nexfusion.Patch as ModernPatch
import io.github.a092devs.nexfusion.PatchBundle as ModernPatchBundle
import io.github.a092devs.nexfusion.PatchExecutor as ModernPatchExecutor
import io.github.a092devs.nexfusion.PatchSession as ModernPatchSession

open class AppTarget @JvmOverloads constructor(
    version: String,
    experimental: Boolean = false,
) : ModernAppTarget(version, experimental)

open class AppPatchInfo @JvmOverloads constructor(
    appName: String,
    packageName: String,
    patches: Array<Patch>,
    targets: List<AppTarget> = emptyList(),
    appIconColor: Int? = null,
) : ModernAppPatchInfo(
    appName,
    packageName,
    Array(patches.size) { patches[it] },
    targets,
    appIconColor,
)

open class Patch @JvmOverloads constructor(
    name: String,
    description: String,
    use: Boolean = true,
    run: (ModernPatchExecutor.() -> Unit)? = null,
) : ModernPatch(name, description, use, run ?: {})

interface PatchBundle : ModernPatchBundle

open class BundleInfo(
    id: String,
    name: String,
    version: String,
    apkPath: String,
    classLoader: ClassLoader,
) : ModernBundleInfo(id, name, version, apkPath, classLoader)

open class PatchSession(
    app: Application,
    lpparam: XposedModuleInterface.PackageReadyParam,
    xposed: XposedInterface,
    coreVersion: String,
    debug: Boolean,
) : ModernPatchSession(app, lpparam, xposed, coreVersion, debug)

object PatcherLog {
    @JvmStatic fun d(msg: String) = io.github.a092devs.nexfusion.PatcherLog.d(msg)
    @JvmStatic fun i(msg: String) = io.github.a092devs.nexfusion.PatcherLog.i(msg)
    @JvmStatic fun w(msg: String) = io.github.a092devs.nexfusion.PatcherLog.w(msg)
    @JvmStatic fun e(msg: String, tr: Throwable? = null) = io.github.a092devs.nexfusion.PatcherLog.e(msg, tr)
}

open class PatchExecutor(
    appContext: Application,
    lpparam: XposedModuleInterface.PackageReadyParam,
    xposed: XposedInterface,
    bundle: ModernBundleInfo,
    dexkit: DexKitCacheBridge.RecyclableBridge,
    sessionAppliedPatches: MutableSet<String>? = null,
) : ModernPatchExecutor(appContext, lpparam, xposed, bundle, dexkit, sessionAppliedPatches)
