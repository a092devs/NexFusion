package io.github.nexfusion.compat.callbacks;

import android.os.Bundle;

public abstract class XCallback implements Comparable<XCallback> {
    public final int priority;
    public static final int PRIORITY_DEFAULT = 50;
    public static final int PRIORITY_LOWEST = -10000;
    public static final int PRIORITY_HIGHEST = 10000;

    public XCallback() {
        this(PRIORITY_DEFAULT);
    }

    public XCallback(int priority) {
        this.priority = priority;
    }

    public static abstract class Param {
        public final Object[] args;
        private Bundle extra;

        protected Param() {
            this.args = null;
        }

        protected Param(Object[] args) {
            this.args = args;
        }

        public synchronized Bundle getExtra() {
            if (extra == null) {
                extra = new Bundle();
            }
            return extra;
        }

        public Object getObjectExtra(String key) {
            Bundle o = getExtra();
            return o.getSerializable(key);
        }

        public void setObjectExtra(String key, Object o) {
            getExtra().putSerializable(key, (java.io.Serializable) o);
        }
    }

    @Override
    public int compareTo(XCallback other) {
        if (this == other) return 0;
        if (other.priority != this.priority) return other.priority - this.priority;
        if (System.identityHashCode(this) < System.identityHashCode(other)) return -1;
        return 1;
    }
}
