package io.github.nexfusion.compat.callbacks;

public abstract class XC_LoadPackage extends io.github.a092devs.nexfusion.compat.callbacks.XC_LoadPackage {
    public XC_LoadPackage() {
        super();
    }

    public XC_LoadPackage(int priority) {
        super(priority);
    }

    public static class LoadPackageParam extends io.github.a092devs.nexfusion.compat.callbacks.XC_LoadPackage.LoadPackageParam {
        public LoadPackageParam(Object[] args) {
            super(args);
        }

        public LoadPackageParam() {
            super(new Object[0]);
        }
    }
}
