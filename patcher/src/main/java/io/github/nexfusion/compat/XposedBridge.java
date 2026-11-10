package io.github.nexfusion.compat;

import android.util.Log;
import io.github.libxposed.api.XposedInterface;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public final class XposedBridge {
    public static final int XPOSED_MIN_API_VERSION = 102;
    public static final ClassLoader BOOTCLASSLOADER = ClassLoader.getSystemClassLoader();
    private static volatile XposedInterface sXposedInterface;

    private XposedBridge() {}

    public static void init(XposedInterface xposedInterface) {
        sXposedInterface = xposedInterface;
    }

    public static XposedInterface getXposedInterface() {
        return sXposedInterface;
    }

    public static int getXposedVersion() {
        XposedInterface xi = sXposedInterface;
        return xi != null ? xi.getApiVersion() : XPOSED_MIN_API_VERSION;
    }

    public static void log(String text) {
        XposedInterface xi = sXposedInterface;
        if (xi != null) {
            xi.log(Log.INFO, "NexFusion", text != null ? text : "null");
        } else {
            Log.i("NexFusion", text != null ? text : "null");
        }
    }

    public static void log(Throwable t) {
        XposedInterface xi = sXposedInterface;
        String msg = t != null ? (t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName()) : "null";
        if (xi != null) {
            xi.log(Log.ERROR, "NexFusion", msg, t);
        } else {
            Log.e("NexFusion", msg, t);
        }
    }

    public static XC_MethodHook.Unhook hookMethod(Member hookMethod, XC_MethodHook callback) {
        if (!(hookMethod instanceof Executable)) {
            throw new IllegalArgumentException("Only methods and constructors can be hooked: " + hookMethod);
        }
        XposedInterface xi = sXposedInterface;
        if (xi == null) {
            throw new IllegalStateException("XposedBridge has not been initialized with an XposedInterface");
        }
        Executable executable = (Executable) hookMethod;
        XposedInterface.HookHandle handle = xi.hook(executable)
            .setPriority(callback.priority)
            .intercept(chain -> {
                XC_MethodHook.MethodHookParam param = new XC_MethodHook.MethodHookParam();
                param.method = hookMethod;
                param.thisObject = chain.getThisObject();
                List<Object> chainArgs = chain.getArgs();
                param.args = chainArgs != null ? chainArgs.toArray() : new Object[0];

                try {
                    callback.beforeHookedMethod(param);
                } catch (Throwable t) {
                    XposedBridge.log(t);
                    param.setResult(null);
                    param.returnEarly = false;
                }

                if (param.returnEarly) {
                    if (param.hasThrowable()) {
                        throw param.getThrowable();
                    }
                    return param.getResult();
                }

                Object result = null;
                try {
                    if (param.args != null && param.args.length > 0) {
                        result = chain.proceed(param.args);
                    } else {
                        result = chain.proceed();
                    }
                    param.setResult(result);
                    param.returnEarly = false;
                } catch (Throwable t) {
                    param.setThrowable(t);
                }

                if (param.thisObject == null) {
                    param.thisObject = chain.getThisObject();
                }

                Object lastResult = param.getResult();
                Throwable lastThrowable = param.getThrowable();

                try {
                    callback.afterHookedMethod(param);
                } catch (Throwable t) {
                    XposedBridge.log(t);
                    if (lastThrowable == null) {
                        param.setResult(lastResult);
                    } else {
                        param.setThrowable(lastThrowable);
                    }
                }

                if (param.hasThrowable()) {
                    throw param.getThrowable();
                }
                return param.getResult();
            });

        return callback.new Unhook(hookMethod, handle::unhook);
    }

    public static void unhookMethod(Member hookMethod, XC_MethodHook callback) {
        // Handled via XC_MethodHook.Unhook
    }

    public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
        Set<XC_MethodHook.Unhook> unhooks = new HashSet<>();
        for (Method method : hookClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                unhooks.add(hookMethod(method, callback));
            }
        }
        return unhooks;
    }

    public static Set<XC_MethodHook.Unhook> hookAllConstructors(Class<?> hookClass, XC_MethodHook callback) {
        Set<XC_MethodHook.Unhook> unhooks = new HashSet<>();
        for (Constructor<?> constructor : hookClass.getDeclaredConstructors()) {
            unhooks.add(hookMethod(constructor, callback));
        }
        return unhooks;
    }

    public static Object invokeOriginalMethod(Member method, Object thisObject, Object[] args) throws Throwable {
        XposedInterface xi = sXposedInterface;
        if (xi == null) {
            throw new IllegalStateException("XposedBridge has not been initialized with an XposedInterface");
        }
        if (method instanceof Method) {
            @SuppressWarnings("unchecked")
            XposedInterface.Invoker<?, Method> invoker = (XposedInterface.Invoker<?, Method>) xi.getInvoker((Method) method);
            invoker.setType(XposedInterface.Invoker.Type.ORIGIN);
            return invoker.invoke(thisObject, args != null ? args : new Object[0]);
        } else if (method instanceof Constructor<?>) {
            @SuppressWarnings("unchecked")
            XposedInterface.CtorInvoker<?> invoker = (XposedInterface.CtorInvoker<?>) xi.getInvoker((Constructor<?>) method);
            invoker.setType(XposedInterface.Invoker.Type.ORIGIN);
            return invoker.newInstance(args != null ? args : new Object[0]);
        } else {
            throw new IllegalArgumentException("Cannot invoke non-executable: " + method);
        }
    }

    public static class CopyOnWriteSortedSet<E> {
        private final CopyOnWriteArrayList<E> elements = new CopyOnWriteArrayList<>();
        public synchronized boolean add(E e) { return elements.add(e); }
        public synchronized boolean remove(E e) { return elements.remove(e); }
        public Object[] getSnapshot() { return elements.toArray(); }
    }
}
