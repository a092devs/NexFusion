package io.github.a092devs.nexfusion.core

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import io.github.libxposed.api.XposedInterface

/**
 * Universal anti-detection cloak that hides the NexFusion module package from target applications
 * and Google Play Services client libraries running within target processes.
 */
object StealthCloak {

    private val HIDDEN_PACKAGES = setOf(
        "io.github.a092devs.nexfusion",
        "io.github.a092devs.nexfusion.patches",
        "io.github.a092devs.nexfusion",
        "io.github.a092devs.nexfusion.patches",
    )

    fun install(app: Application, modulePackage: String, xposed: XposedInterface) {
        runCatching {
            val allHidden = HIDDEN_PACKAGES + modulePackage
            val pmClass = Class.forName("android.app.ApplicationPackageManager", false, app.classLoader)

            // Intercept getPackageInfo to throw NameNotFoundException if module is queried
            pmClass.declaredMethods.filter { it.name == "getPackageInfo" }.forEach { method ->
                xposed.hook(method).intercept { chain ->
                    val pkg = chain.args.getOrNull(0) as? String
                    if (pkg in allHidden) {
                        throw PackageManager.NameNotFoundException(pkg)
                    }
                    chain.proceed()
                }
            }

            // Intercept getApplicationInfo to throw NameNotFoundException if module is queried
            pmClass.declaredMethods.filter { it.name == "getApplicationInfo" }.forEach { method ->
                xposed.hook(method).intercept { chain ->
                    val pkg = chain.args.getOrNull(0) as? String
                    if (pkg in allHidden) {
                        throw PackageManager.NameNotFoundException(pkg)
                    }
                    chain.proceed()
                }
            }

            // Intercept getPackageUid to throw NameNotFoundException if module is queried
            pmClass.declaredMethods.filter { it.name == "getPackageUid" }.forEach { method ->
                xposed.hook(method).intercept { chain ->
                    val pkg = chain.args.getOrNull(0) as? String
                    if (pkg in allHidden) {
                        throw PackageManager.NameNotFoundException(pkg)
                    }
                    chain.proceed()
                }
            }

            // Intercept getPackageGids to throw NameNotFoundException if module is queried
            pmClass.declaredMethods.filter { it.name == "getPackageGids" }.forEach { method ->
                xposed.hook(method).intercept { chain ->
                    val pkg = chain.args.getOrNull(0) as? String
                    if (pkg in allHidden) {
                        throw PackageManager.NameNotFoundException(pkg)
                    }
                    chain.proceed()
                }
            }

            // Strip module package from getInstalledPackages queries
            pmClass.declaredMethods.filter { it.name == "getInstalledPackages" }.forEach { method ->
                xposed.hook(method).intercept { chain ->
                    val result = chain.proceed()
                    val list = (result as? List<*>)?.toMutableList() ?: return@intercept result
                    var modified = false
                    val iterator = list.iterator()
                    while (iterator.hasNext()) {
                        val item = iterator.next() as? PackageInfo
                        if (item?.packageName in allHidden) {
                            iterator.remove()
                            modified = true
                        }
                    }
                    if (modified) list else result
                }
            }

            // Strip module package from getInstalledApplications queries
            pmClass.declaredMethods.filter { it.name == "getInstalledApplications" }.forEach { method ->
                xposed.hook(method).intercept { chain ->
                    val result = chain.proceed()
                    val list = (result as? List<*>)?.toMutableList() ?: return@intercept result
                    var modified = false
                    val iterator = list.iterator()
                    while (iterator.hasNext()) {
                        val item = iterator.next() as? ApplicationInfo
                        if (item?.packageName in allHidden) {
                            iterator.remove()
                            modified = true
                        }
                    }
                    if (modified) list else result
                }
            }

            // Strip module from queryIntentActivities
            pmClass.declaredMethods.filter { it.name == "queryIntentActivities" }.forEach { method ->
                xposed.hook(method).intercept { chain ->
                    val result = chain.proceed()
                    val list = (result as? List<*>)?.toMutableList() ?: return@intercept result
                    var modified = false
                    val iterator = list.iterator()
                    while (iterator.hasNext()) {
                        val item = iterator.next() as? ResolveInfo
                        if (item?.activityInfo?.packageName in allHidden) {
                            iterator.remove()
                            modified = true
                        }
                    }
                    if (modified) list else result
                }
            }

            // Strip module from getPackagesForUid
            pmClass.declaredMethods.filter { it.name == "getPackagesForUid" }.forEach { method ->
                xposed.hook(method).intercept { chain ->
                    val result = chain.proceed()
                    val pkgs = result as? Array<*> ?: return@intercept result
                    val hasMatch = pkgs.any { it in allHidden }
                    if (hasMatch) {
                        @Suppress("UNCHECKED_CAST")
                        (pkgs as Array<String>).filter { it !in allHidden }.toTypedArray()
                    } else {
                        result
                    }
                }
            }
        }
    }
}
