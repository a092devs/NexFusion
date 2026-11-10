package io.github.nexfusion.compat;

import android.app.Application;
import android.content.pm.ApplicationInfo;

/**
 * Compatibility helper for accessing current application and process information
 * across modern Android runtime environments targeting libxposed API 102.
 */
public final class AndroidAppHelper {
    private AndroidAppHelper() {}

    public static Application currentApplication() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            return (Application) activityThreadClass.getMethod("currentApplication").invoke(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static String currentPackageName() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            String pkg = (String) activityThreadClass.getMethod("currentPackageName").invoke(null);
            if (pkg != null) return pkg;
        } catch (Throwable ignored) {}
        Application app = currentApplication();
        return (app != null) ? app.getPackageName() : "android";
    }

    public static String currentProcessName() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            String proc = (String) activityThreadClass.getMethod("currentProcessName").invoke(null);
            if (proc != null) return proc;
        } catch (Throwable ignored) {}
        return currentPackageName();
    }

    public static ApplicationInfo currentApplicationInfo() {
        Application app = currentApplication();
        return (app != null) ? app.getApplicationInfo() : null;
    }
}
