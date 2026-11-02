package com.example.patches.misc

import de.robv.android.xposed.XC_MethodReplacement.returnConstant
import io.github.nexfusion.patch

val UnlockPro = patch(
    name = "Unlock Pro",
    description = "Unlocks the Pro features.",
) {
    IsProFingerprint.hookMethod(returnConstant(true))
}
