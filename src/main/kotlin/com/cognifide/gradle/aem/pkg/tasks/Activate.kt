package com.cognifide.gradle.aem.pkg.tasks

import com.cognifide.gradle.aem.common.fileNames
import com.cognifide.gradle.aem.common.tasks.PackageTask
import com.cognifide.gradle.aem.instance.names
import org.gradle.api.tasks.TaskAction

open class Activate : PackageTask() {

    init {
        description = "Activates CRX package on instance(s)."
    }

    @TaskAction
    fun activate() {
        aem.progress(instances.size * packages.size) {
            aem.syncPackages(instances, packages) { pkg ->
                increment("${pkg.name} -> ${instance.name}") {
                    activatePackage(determineRemotePackagePath(pkg))
                }
            }
        }
        aem.notifier.notify("Package activated", "${packages.fileNames} on ${instances.names}")
    }

    companion object {
        const val NAME = "aemActivate"
    }
}