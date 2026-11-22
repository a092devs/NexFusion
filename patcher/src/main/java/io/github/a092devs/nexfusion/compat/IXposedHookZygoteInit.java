package io.github.a092devs.nexfusion.compat;

public interface IXposedHookZygoteInit extends IXposedMod {
    void initZygote(StartupParam startupParam) throws Throwable;

    class StartupParam {
        public String modulePath;
        public boolean startsSystemServer;
    }
}
