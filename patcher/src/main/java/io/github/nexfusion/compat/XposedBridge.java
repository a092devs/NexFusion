package io.github.nexfusion.compat;

import io.github.libxposed.api.XposedInterface;
import java.lang.reflect.Member;

public final class XposedBridge {
    public static final int XPOSED_MIN_API_VERSION = 101;
    public static final ClassLoader BOOTCLASSLOADER = ClassLoader.getSystemClassLoader();

    private XposedBridge() {}

    public static void init(XposedInterface xposedInterface) {
        io.github.a092devs.nexfusion.compat.XposedBridge.init(xposedInterface);
    }

    public static XposedInterface getXposedInterface() {
        return io.github.a092devs.nexfusion.compat.XposedBridge.getXposedInterface();
    }

    public static int getXposedVersion() {
        return io.github.a092devs.nexfusion.compat.XposedBridge.getXposedVersion();
    }

    public static void log(String text) {
        io.github.a092devs.nexfusion.compat.XposedBridge.log(text);
    }

    public static void log(Throwable t) {
        io.github.a092devs.nexfusion.compat.XposedBridge.log(t);
    }

    public static io.github.a092devs.nexfusion.compat.XC_MethodHook.Unhook hookMethod(Member hookMethod, io.github.a092devs.nexfusion.compat.XC_MethodHook callback) {
        return io.github.a092devs.nexfusion.compat.XposedBridge.hookMethod(hookMethod, callback);
    }

    public static Object invokeOriginalMethod(Member method, Object thisObject, Object[] args) throws Throwable {
        return io.github.a092devs.nexfusion.compat.XposedBridge.invokeOriginalMethod(method, thisObject, args);
    }
}
