package com.cognifide.gradle.aem.instance.tail.io

import com.cognifide.gradle.aem.common.AemExtension
import com.cognifide.gradle.aem.common.AemTask
import com.cognifide.gradle.aem.common.Formats
import com.cognifide.gradle.aem.instance.tail.TailOptions
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.net.URI
import org.apache.commons.io.FileUtils

class LogFiles(
    private val taskName: String,
    private val aem: AemExtension,
    private val options: TailOptions
) {

    fun mainUri(instanceName: String) = uri(main(instanceName))

    fun clearMain(instanceName: String) = main(instanceName).bufferedWriter().use { it.write("") }

    fun clearIncidents(instanceName: String) =
        FileUtils.deleteDirectory(AemTask.temporaryDir(aem.project, taskName, "$instanceName/$INCIDENT_DIR"))

    fun writeToIncident(instanceName: String, writerBlock: (BufferedWriter) -> Unit): URI {
        val incidentFile = incident(instanceName)
        incidentFile.bufferedWriter().use(writerBlock)
        return uri(incidentFile)
    }

    fun writeToMain(instanceName: String, writerBlock: (FileWriter) -> Unit) =
        FileWriter(main(instanceName).path, true).use(writerBlock)

    fun lock() {
        lock(lockFile())
    }

    fun isLocked(): Boolean {
        val lockFile = lockFile()
        return lockFile.exists() && lockFile.lastModified() + options.lockInterval > System.currentTimeMillis()
    }

    private fun main(instanceName: String) =
        AemTask.temporaryFile(aem.project, "$taskName/$instanceName", options.logFile())

    private fun incident(instanceName: String) =
        AemTask.temporaryFile(
            aem.project,
            "$taskName/$instanceName/$INCIDENT_DIR",
            "${Formats.dateFileName()}-${options.logFile()}"
        )

    private fun lock(file: File) {
        if (!file.exists()) {
            file.bufferedWriter().use { it.write("") }
        } else {
            file.setLastModified(System.currentTimeMillis())
        }
    }

    private fun lockFile(): File {
        return AemTask.temporaryFile(
            aem.project,
            taskName,
            "tailer.lock"
        )
    }

    private fun uri(file: File): URI = file.toURI()

    companion object {
        const val INCIDENT_DIR = "incidents"
    }
}