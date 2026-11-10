package io.github.nexfusion.compat.callbacks;

import android.content.pm.ApplicationInfo;

public abstract class XC_LoadPackage extends XCallback {
    public XC_LoadPackage() {
        super();
    }

    public XC_LoadPackage(int priority) {
        super(priority);
    }

    public static class LoadPackageParam extends XCallback.Param {
        public String packageName;
        public String processName;
        public ClassLoader classLoader;
        public ApplicationInfo appInfo;
        public boolean isFirstApplication;

        public LoadPackageParam(Object[] args) {
            super(args);
        }
    }

    public abstract void handleLoadPackage(LoadPackageParam lpparam) throws Throwable;
}
