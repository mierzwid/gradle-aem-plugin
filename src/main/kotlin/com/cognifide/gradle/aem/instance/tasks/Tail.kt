package com.cognifide.gradle.aem.instance.tasks

import com.cognifide.gradle.aem.common.AemDefaultTask
import com.cognifide.gradle.aem.instance.tail.*
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

open class Tail : AemDefaultTask() {

    init {
        description = "Tails logs from all configured instances (local & remote) and notifies developer about unknown errors."
    }

    @Nested
    private val options = TailOptions(aem)

    fun config(configurer: TailOptions.() -> Unit) {
        options.apply(configurer)
    }

    private val tailers = Tailers(name, aem, options)

    @TaskAction
    fun tail() = tailers.tail()

    fun startInBackground() = tailers.backgroundTail()

    fun stop() = tailers.stop()

    companion object {
        const val NAME = "aemTail"
    }
}
