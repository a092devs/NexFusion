package io.github.nexfusion

import android.util.Log

/** Logging for the patcher itself. Bundles usually log through their own extension code. */
object PatcherLog {
    private const val TAG = "NexFusion"

    /** Whether [info] messages are logged. Set by NexFusion for debug builds. */
    @Volatile
    var verbose = false

    fun info(throwable: Throwable? = null, message: () -> String) {
        if (verbose) Log.i(TAG, message(), throwable)
    }

    /**
     * State diagnostics that release builds log too: the per-bundle cache decision, the applied
     * summary and patch failures all show up in logcat, so a device can be audited without a
     * debug build.
     */
    fun state(throwable: Throwable? = null, message: () -> String) {
        Log.i(TAG, message(), throwable)
    }

    fun error(throwable: Throwable? = null, message: () -> String) {
        Log.e(TAG, message(), throwable)
    }
}
