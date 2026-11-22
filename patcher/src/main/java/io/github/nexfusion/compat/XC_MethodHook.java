package io.github.nexfusion.compat;

import io.github.nexfusion.compat.callbacks.IXUnhook;
import io.github.nexfusion.compat.callbacks.XCallback;
import java.lang.reflect.Member;

public abstract class XC_MethodHook extends XCallback {
    public XC_MethodHook() {
        super();
    }

    public XC_MethodHook(int priority) {
        super(priority);
    }

    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {}

    public static class MethodHookParam extends io.github.a092devs.nexfusion.compat.XC_MethodHook.MethodHookParam {
        public MethodHookParam() {
            super();
        }
    }

    public class Unhook implements IXUnhook<XC_MethodHook> {
        private final Member hookMethod;

        public Unhook(Member hookMethod) {
            this.hookMethod = hookMethod;
        }

        public Member getHookedMethod() {
            return hookMethod;
        }

        @Override
        public XC_MethodHook getCallback() {
            return XC_MethodHook.this;
        }

        @Override
        public void unhook() {}
    }
}
