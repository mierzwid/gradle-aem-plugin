package com.cognifide.gradle.aem.environment.tasks

import com.cognifide.gradle.aem.environment.EnvironmentException
import com.cognifide.gradle.aem.environment.ServiceAwait
import com.cognifide.gradle.aem.environment.docker.DockerTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class EnvUp : DockerTask() {

    init {
        description = "Starts additional services for local environment " +
                "- based on provided docker compose file."
    }

    @Internal
    private val serviceAwait = ServiceAwait(aem)

    @TaskAction
    fun up() {
        stack.deploy(config.composeFilePath)
        val unavailableServices = serviceAwait.checkForUnavailableServices()
        if (unavailableServices.isNotEmpty()) {
            throw EnvironmentException("Failed to initialized all services! Following URLs are still unavailable " +
                    "or returned different response than expected:\n${unavailableServices.joinToString("\n")}")
        }
    }

    companion object {
        const val NAME = "aemEnvUp"
    }
}