package com.cognifide.gradle.aem.vlt

import com.cognifide.gradle.aem.AemConfig
import com.cognifide.gradle.aem.AemInstance
import org.apache.commons.cli2.CommandLine
import org.apache.jackrabbit.vault.cli.VaultFsApp
import org.apache.jackrabbit.vault.util.console.ExecutionContext
import org.apache.jackrabbit.vault.util.console.commands.CmdConsole
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File

class VltApp(val instance: AemInstance, val logger: Logger) : VaultFsApp() {

    companion object {

        fun checkout(project: Project, config: AemConfig) {
            val instance = AemInstance.filter(project, config, AemInstance.FILTER_AUTHOR).first()
            val vltApp = VltApp(instance, project.logger)
            val cmdArgs = config.vaultCheckoutArgs + listOf(instance.url, "/", config.determineContentPath(project))

            val filter = File(config.vaultFilterPath)
            if (filter.exists()) {
                vltApp.executeCommand(listOf(CheckoutTask.COMMAND, "-f", filter.absolutePath) + cmdArgs)
            } else {
                vltApp.executeCommand(listOf(CheckoutTask.COMMAND + cmdArgs))
            }
        }

    }

    override fun getDefaultContext(): ExecutionContext {
        return defaultContext
    }

    private val defaultContext by lazy {
        val ctx = VltExecutionContext(this)
        ctx.installCommand(CmdConsole()); ctx
    }

    fun executeCommand(args: List<String> = listOf()): Unit {
        val allArgs = mutableListOf<String>()
        allArgs.addAll(listOf("--credentials", instance.user + ":" + instance.password))
        allArgs.addAll(args)

        run(allArgs.toTypedArray())
    }

    override fun prepare(cl: CommandLine) {
        logger.info("Executing Vault application: $cl")

        super.prepare(cl)
    }
}
