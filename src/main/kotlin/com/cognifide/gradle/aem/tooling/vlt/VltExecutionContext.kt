package com.cognifide.gradle.aem.tooling.vlt

import org.apache.commons.cli2.CommandLine
import org.apache.jackrabbit.vault.cli.VltExecutionContext as BaseExecutionContext
import org.apache.jackrabbit.vault.util.console.CliCommand

class VltExecutionContext(app: VltApp) : BaseExecutionContext(app) {

    @Suppress("TooGenericExceptionCaught")
    override fun execute(commandLine: CommandLine): Boolean {
        commands.filterIsInstance<CliCommand>().forEach { command ->
            try {
                if (doExecute(command, commandLine)) {
                    return true
                }
            } catch (e: Exception) {
                throw VltException("Error while executing command: $command")
            }
        }

        return false
    }
}
