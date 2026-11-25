package io.github.a092devs.nexfusion

import android.util.Log
import io.github.a092devs.nexfusion.compat.XposedBridge

/** Logging for the patcher itself. Bundles usually log through their own extension code. */
object PatcherLog {
    private const val TAG = "NexFusion"

    /** Whether [info] messages are logged. Set by NexFusion for debug builds. */
    @Volatile
    var verbose = false

    fun d(msg: String) {
        if (verbose) Log.d(TAG, msg)
    }

    fun i(msg: String) {
        Log.i(TAG, msg)
    }

    fun w(msg: String) {
        Log.w(TAG, msg)
    }

    fun e(msg: String, throwable: Throwable? = null) {
        Log.e(TAG, msg, throwable)
    }

    fun info(throwable: Throwable? = null, message: () -> String) {
        if (verbose) Log.i(TAG, message(), throwable)
    }

    /**
     * State diagnostics that release builds log too: the per-bundle cache decision, the applied
     * summary and patch failures all show up in both logcat and Xposed/LSPosed module logs,
     * so a device can be audited without a debug build.
     */
    fun state(throwable: Throwable? = null, message: () -> String) {
        val text = message()
        Log.i(TAG, text, throwable)
        runCatching {
            XposedBridge.log("$TAG: $text")
            if (throwable != null) XposedBridge.log(throwable)
        }
    }

    fun error(throwable: Throwable? = null, message: () -> String) {
        val text = message()
        Log.e(TAG, text, throwable)
        runCatching {
            XposedBridge.log("$TAG [ERROR]: $text")
            if (throwable != null) XposedBridge.log(throwable)
        }
    }
}
