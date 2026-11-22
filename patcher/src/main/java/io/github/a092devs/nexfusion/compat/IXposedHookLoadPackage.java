package io.github.a092devs.nexfusion.compat;

import io.github.a092devs.nexfusion.compat.callbacks.XC_LoadPackage;

public interface IXposedHookLoadPackage extends IXposedMod {
    void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;
}
