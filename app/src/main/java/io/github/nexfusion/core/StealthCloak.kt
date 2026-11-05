package io.github.nexfusion.core

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

/**
 * Universal anti-detection cloak that hides the NexFusion module package from target applications
 * and Google Play Services client libraries running within target processes.
 */
object StealthCloak {

    fun install(app: Application, modulePackage: String) {
        runCatching {
            val pmClass = Class.forName("android.app.ApplicationPackageManager", false, app.classLoader)

            // Intercept getPackageInfo to throw NameNotFoundException if module is queried
            XposedBridge.hookAllMethods(pmClass, "getPackageInfo", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val pkg = param.args.getOrNull(0) as? String
                    if (pkg == modulePackage) {
                        param.throwable = PackageManager.NameNotFoundException(modulePackage)
                    }
                }
            })

            // Intercept getApplicationInfo to throw NameNotFoundException if module is queried
            XposedBridge.hookAllMethods(pmClass, "getApplicationInfo", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val pkg = param.args.getOrNull(0) as? String
                    if (pkg == modulePackage) {
                        param.throwable = PackageManager.NameNotFoundException(modulePackage)
                    }
                }
            })

            // Strip module package from getInstalledPackages queries
            XposedBridge.hookAllMethods(pmClass, "getInstalledPackages", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val list = (param.result as? List<*>)?.toMutableList() ?: return
                    var modified = false
                    val iterator = list.iterator()
                    while (iterator.hasNext()) {
                        val item = iterator.next() as? PackageInfo
                        if (item?.packageName == modulePackage) {
                            iterator.remove()
                            modified = true
                        }
                    }
                    if (modified) param.result = list
                }
            })

            // Strip module package from getInstalledApplications queries
            XposedBridge.hookAllMethods(pmClass, "getInstalledApplications", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val list = (param.result as? List<*>)?.toMutableList() ?: return
                    var modified = false
                    val iterator = list.iterator()
                    while (iterator.hasNext()) {
                        val item = iterator.next() as? ApplicationInfo
                        if (item?.packageName == modulePackage) {
                            iterator.remove()
                            modified = true
                        }
                    }
                    if (modified) param.result = list
                }
            })

            // Strip module from queryIntentActivities
            XposedBridge.hookAllMethods(pmClass, "queryIntentActivities", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val list = (param.result as? List<*>)?.toMutableList() ?: return
                    var modified = false
                    val iterator = list.iterator()
                    while (iterator.hasNext()) {
                        val item = iterator.next() as? ResolveInfo
                        if (item?.activityInfo?.packageName == modulePackage) {
                            iterator.remove()
                            modified = true
                        }
                    }
                    if (modified) param.result = list
                }
            })

            // Strip module from getPackagesForUid
            XposedBridge.hookAllMethods(pmClass, "getPackagesForUid", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val pkgs = param.result as? Array<*> ?: return
                    if (pkgs.contains(modulePackage)) {
                        @Suppress("UNCHECKED_CAST")
                        param.result = (pkgs as Array<String>).filter { it != modulePackage }.toTypedArray()
                    }
                }
            })
        }
    }
}

