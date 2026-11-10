package io.github.nexfusion.compat.callbacks;

public interface IXUnhook<T> {
    void unhook();
    T getCallback();
}
