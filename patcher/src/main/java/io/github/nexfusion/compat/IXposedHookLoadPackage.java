package io.github.nexfusion.compat;

import io.github.nexfusion.compat.callbacks.XC_LoadPackage;

public interface IXposedHookLoadPackage extends IXposedMod {
    void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;
}
