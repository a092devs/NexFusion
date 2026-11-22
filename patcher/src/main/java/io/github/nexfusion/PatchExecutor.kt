package io.github.nexfusion

import io.github.a092devs.nexfusion.PatchExecutor as ModernPatchExecutor

fun patch(
    name: String = "",
    description: String = "",
    use: Boolean = true,
    func: PatchExecutor.() -> Unit
) = Patch(name, description, use) {
    (this as? PatchExecutor)?.func()
}
