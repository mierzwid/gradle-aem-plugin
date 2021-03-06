package com.cognifide.gradle.aem.tooling.vlt

import com.cognifide.gradle.aem.common.AemExtension
import com.cognifide.gradle.aem.pkg.Package
import java.io.File

class VltRunner(val aem: AemExtension) {

    private val app = VltApp(aem.project)

    var command: String = aem.props.string("aem.vlt.command") ?: ""

    var commandProperties: Map<String, Any> = mapOf("config" to aem.config)

    val commandEffective: String
        get() = aem.props.expand(command, commandProperties)

    var contentPath: String = aem.config.packageRoot

    var contentRelativePath: String = aem.props.string("aem.vlt.path") ?: ""

    val contentDirEffective: File
        get() {
            var workingPath = "$contentPath/${Package.JCR_ROOT}"
            if (contentRelativePath.isNotBlank()) {
                workingPath = "$workingPath/$contentRelativePath"
            }

            return File(workingPath)
        }

    fun run() {
        if (commandEffective.isBlank()) {
            throw VltException("Vault command cannot be blank.")
        }

        aem.logger.lifecycle("Working directory: $contentDirEffective")
        aem.logger.lifecycle("Executing command: vlt $commandEffective")

        app.execute(commandEffective, contentDirEffective)
    }
}