package io.github.nexfusion.compat;

import android.content.pm.ApplicationInfo;

public final class AndroidAppHelper {
    private AndroidAppHelper() {}

    public static String currentPackageName() {
        return io.github.a092devs.nexfusion.compat.AndroidAppHelper.currentPackageName();
    }

    public static String currentProcessName() {
        return io.github.a092devs.nexfusion.compat.AndroidAppHelper.currentProcessName();
    }

    public static ApplicationInfo currentApplicationInfo() {
        return io.github.a092devs.nexfusion.compat.AndroidAppHelper.currentApplicationInfo();
    }
}
