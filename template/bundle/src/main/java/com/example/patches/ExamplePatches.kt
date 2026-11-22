package com.example.patches

import com.example.patches.misc.UnlockPro
import io.github.a092devs.nexfusion.AppPatchInfo
import io.github.a092devs.nexfusion.PatchBundle

/** Entry point of the bundle, named by `nexfusionBundle.entry`. */
object ExamplePatches : PatchBundle {
    override val apps = listOf(
        AppPatchInfo("Contoso", "com.contoso.app", arrayOf(UnlockPro)),
    )
}
