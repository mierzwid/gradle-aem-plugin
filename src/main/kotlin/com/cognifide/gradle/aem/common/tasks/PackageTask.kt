package com.cognifide.gradle.aem.common.tasks

import com.cognifide.gradle.aem.common.AemDefaultTask
import com.cognifide.gradle.aem.instance.Instance
import java.io.File
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles

open class PackageTask : AemDefaultTask() {

    @Input
    var instances: List<Instance> = listOf()

    @InputFiles
    var packages: List<File> = listOf()

    override fun projectsEvaluated() {
        if (instances.isEmpty()) {
            instances = aem.instances
        }

        if (packages.isEmpty()) {
            packages = aem.packagesDependent(this)
        }
    }

    fun instance(urlOrName: String) {
        instances += aem.instance(urlOrName)
    }

    fun pkg(path: String) {
        packages += project.file(path)
    }
}