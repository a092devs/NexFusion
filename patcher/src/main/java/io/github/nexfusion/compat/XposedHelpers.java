package io.github.nexfusion.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class XposedHelpers {
    private XposedHelpers() {}

    public static Class<?> findClass(String className, ClassLoader classLoader) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.findClass(className, classLoader);
    }

    public static Class<?> findClassIfExists(String className, ClassLoader classLoader) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.findClassIfExists(className, classLoader);
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.findField(clazz, fieldName);
    }

    public static Field findFieldIfExists(Class<?> clazz, String fieldName) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.findFieldIfExists(clazz, fieldName);
    }

    public static Method findMethodExact(Class<?> clazz, String methodName, Object... parameterTypes) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.findMethodExact(clazz, methodName, parameterTypes);
    }

    public static Method findMethodExactIfExists(Class<?> clazz, String methodName, Object... parameterTypes) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.findMethodExactIfExists(clazz, methodName, parameterTypes);
    }

    public static Constructor<?> findConstructorExact(Class<?> clazz, Object... parameterTypes) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.findConstructorExact(clazz, parameterTypes);
    }

    public static Constructor<?> findConstructorExactIfExists(Class<?> clazz, Object... parameterTypes) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.findConstructorExactIfExists(clazz, parameterTypes);
    }

    public static Object getObjectField(Object obj, String fieldName) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.getObjectField(obj, fieldName);
    }

    public static void setObjectField(Object obj, String fieldName, Object val) {
        io.github.a092devs.nexfusion.compat.XposedHelpers.setObjectField(obj, fieldName, val);
    }

    public static boolean getBooleanField(Object obj, String fieldName) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.getBooleanField(obj, fieldName);
    }

    public static void setBooleanField(Object obj, String fieldName, boolean val) {
        io.github.a092devs.nexfusion.compat.XposedHelpers.setBooleanField(obj, fieldName, val);
    }

    public static int getIntField(Object obj, String fieldName) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.getIntField(obj, fieldName);
    }

    public static void setIntField(Object obj, String fieldName, int val) {
        io.github.a092devs.nexfusion.compat.XposedHelpers.setIntField(obj, fieldName, val);
    }

    public static long getLongField(Object obj, String fieldName) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.getLongField(obj, fieldName);
    }

    public static void setLongField(Object obj, String fieldName, long val) {
        io.github.a092devs.nexfusion.compat.XposedHelpers.setLongField(obj, fieldName, val);
    }

    public static Object getStaticObjectField(Class<?> clazz, String fieldName) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.getStaticObjectField(clazz, fieldName);
    }

    public static void setStaticObjectField(Class<?> clazz, String fieldName, Object val) {
        io.github.a092devs.nexfusion.compat.XposedHelpers.setStaticObjectField(clazz, fieldName, val);
    }

    public static Object callMethod(Object obj, String methodName, Object... args) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.callMethod(obj, methodName, args);
    }

    public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.callStaticMethod(clazz, methodName, args);
    }

    public static Object newInstance(Class<?> clazz, Object... args) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.newInstance(clazz, args);
    }

    public static Field findFirstFieldByExactType(Class<?> clazz, Class<?> type) {
        return io.github.a092devs.nexfusion.compat.XposedHelpers.findFirstFieldByExactType(clazz, type);
    }

    public static final class InvocationTargetError extends Error {
        private static final long serialVersionUID = -7647334720970189445L;

        public InvocationTargetError(Throwable cause) {
            super(cause);
        }

        public InvocationTargetError(String detailMessage, Throwable cause) {
            super(detailMessage, cause);
        }
    }
}
