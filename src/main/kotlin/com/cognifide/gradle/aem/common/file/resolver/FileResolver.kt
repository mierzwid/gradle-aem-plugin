package com.cognifide.gradle.aem.common.file.resolver

import com.cognifide.gradle.aem.common.AemExtension
import java.io.File

class FileResolver(aem: AemExtension, downloadDir: File) : Resolver<FileGroup>(aem, downloadDir) {

    override fun createGroup(name: String): FileGroup {
        return FileGroup(downloadDir, name)
    }
}