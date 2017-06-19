package com.cognifide.gradle.aem

import com.cognifide.gradle.aem.pkg.ComposeTask
import org.gradle.api.DefaultTask
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.io.File
import java.io.Serializable
import java.util.*

/**
 * Aggregated collection of AEM related configuration.
 *
 * Notice that this whole object is serializable and marked ad input of tasks, so there is no need to mark
 * each property as input.
 *
 * What is more, content paths, which are used to compose a CRX package are being processed to copy task,
 * which automatically mark them as inputs, so package is being rebuild on any content changes.
 */
data class AemConfig(

    /**
     * List of AEM instances on which packages could be deployed.
     */
    var instances: MutableList<AemInstance> = mutableListOf(),

    /**
     * Defines maximum time after which initializing connection to AEM will be aborted (e.g on upload, install).
     */
    var deployConnectionTimeout: Int = 5000,

    /**
     * Perform deploy action (upload, install or activate) in parallel to multiple instances at once.
     */
    @Incubating
    var deployParallel: Boolean = false,

    /**
     * Force upload CRX package regardless if it was previously uploaded.
     */
    var uploadForce: Boolean = true,

    /**
     * Determines if when on package install, sub-packages included in CRX package content should be also installed.
     */
    var recursiveInstall: Boolean = true,

    /**
     * Defines behavior for access control handling included in rep:policy nodes being a part of CRX package content.
     *
     * @see <https://jackrabbit.apache.org/filevault/apidocs/org/apache/jackrabbit/vault/fs/io/AccessControlHandling.html>
     */
    var acHandling: String = "merge_preserve",

    /**
     * Absolute path to JCR content to be included in CRX package.
     *
     * Default: "${project.projectDir.path}/src/main/content"
     */
    var contentPath: String = "",

    /**
     * Content path for bundle jars being placed in CRX package.
     * Default: "/apps/${project.rootProject.name}/install".
     */
    var bundlePath: String = "",

    /**
     * Custom path to composed CRX package being uploaded.
     *
     * Default: "${project.buildDir.path}/distributions/${project.name}-${project.version}.zip"
     */
    var localPackagePath: String = "",

    /**
     * Custom path to CRX package that is uploaded on AEM instance.
     *
     * Default: [automatically determined]
     */
    var remotePackagePath: String = "",

    /**
     * Exclude files being a part of CRX package.
     */
    var filesExcluded: MutableList<String> = mutableListOf(
            "**/.git",
            "**/.git/**",
            "**/.gitattributes",
            "**/.gitignore",
            "**/.gitmodules",
            "**/.vlt",
            "**/node_modules/**",
            "jcr_root/.vlt-sync-config.properties"
    ),

    /**
     * Define here custom properties that can be used in CRX package files like 'META-INF/vault/properties.xml'.
     * Could override predefined properties provided by plugin itself.
     */
    var fileProperties: MutableMap<String, Any> = mutableMapOf(),

    /**
     * Freely customize files being copied to CRX package.
     *
     * Default: exclude files defined in 'filesExcluded'.
     */
    var fileFilter: ((CopySpec, ComposeTask) -> Unit) = { spec, _ ->
        spec.exclude(filesExcluded)
    },

    /**
     * Used to generate unique "buildCount" and "created" predefined file properties.
     */
    var buildDate: Date = Date(),

    /**
     * Ensures that for directory 'META-INF/vault' default files will be generated when missing:
     * 'config.xml', 'filter.xml', 'properties.xml' and 'settings.xml'.
     */
    var vaultCopyMissingFiles : Boolean = true,

    /**
     * Custom path to Vault files that will be used to build CRX package.
     * Useful to share same files for all packages, like package thumbnail.
     */
    var vaultFilesPath: String = "",

    /**
     * Wildcard file name filter expression that is used to filter in which Vault files properties can be injected.
     * This also could be done 'by fileFilter', but due to performance optimization it is done separately.
     */
    var vaultFilesExpanded: MutableList<String> = mutableListOf("*.xml"),

    /**
     * Define here properties that will be skipped when pulling JCR content from AEM instance.
     */
    var vaultSkipProperties : MutableList<String> = mutableListOf(
            "jcr:lastModified",
            "jcr:created",
            "cq:lastModified",
            "cq:lastReplicat*",
            "jcr:uuid"
    ),

    /**
     * Filter file used when Vault files are being checked out from running AEM instance.
     *
     * Default: "src/main/content/META-INF/vault/filter.xml"
     */
    var vaultFilterPath:String = "",

    /**
     * Extra parameters passed to VLT application while executing 'checkout' command.
     */
    var vaultCheckoutArgs : MutableList<String> = mutableListOf("--force"),

    /**
     * Specify characters to be used as line endings when cleaning up checked out JCR content.
     */
    var vaultLineSeparator : String = System.lineSeparator(),

    /**
     * Configure default task dependency assignments while including dependant project bundles.
     * Simplifies multi-module project configuration.
     */
    var dependBundlesTaskNames: (Project) -> Set<String> = { setOf(
            LifecycleBasePlugin.ASSEMBLE_TASK_NAME,
            LifecycleBasePlugin.CHECK_TASK_NAME
    )},

    /**
     * Configure default task dependency assignments while including dependant project content.
     * Simplifies multi-module project configuration.
     */
    var dependContentTaskNames: (Project) -> Set<String> = { project ->
        val task = project.tasks.getByName(ComposeTask.NAME)

        task.taskDependencies.getDependencies(task).map { it.name }.toSet()
    }

) : Serializable {
    companion object {

        /**
         * Generally it is recommended to configure only extension config instead of config of concrete task, because
         * there are combined tasks like `aemDeploy` which are using multiple properties at once.
         *
         * Copying properties and considering them as separate config is intentional, just to ensure that specific task
         * configuration does not affect another.
         */
        fun create(task: DefaultTask): AemConfig {
            val global =  task.project.extensions.getByType(AemExtension::class.java).config
            val extended = global.copy()

            extended.configure(task)

            return extended
        }

        /**
         * Shorthand getter for configuration related with specified project.
         * Especially useful when including one project in another (composing assembly packages).
         */
        fun of(project: Project): AemConfig {
            return (project.tasks.getByName(ComposeTask.NAME) as AemTask).config
        }
    }

    /**
     * Initialize defaults that depends on concrete type of project.
     */
    fun configure(task: DefaultTask) {

        // Default values
        val project = task.project

        if (project.path == project.rootProject.path) {
            bundlePath = "/apps/${project.name}/install"
        } else {
            bundlePath = "/apps/${project.rootProject.name}/${project.name}/install"
        }

        contentPath =  "${project.projectDir.path}/src/main/content"
        vaultFilesPath ="${project.rootProject.projectDir.path}/src/main/resources/${AemPlugin.VLT_PATH}"
        vaultFilterPath = "${project.projectDir.path}/${AemPlugin.VLT_PATH}/filter.xml"

        // Build caching
        val inputs = task.inputs

        vaultFilesDirs.forEach { inputs.dir(it) }
    }

    /**
     * Declare new deployment target (AEM instance).
     */
    fun instance(url: String, user: String = "admin", password: String = "admin", type: String = "default") {
        instances.add(AemInstance(url, user, password, type))
    }

    /**
     * Following checks will be performed during configuration phase.
     */
    fun validate() {
        if (bundlePath.isBlank()) {
            throw AemException("Bundle path cannot be blank")
        }

        if (contentPath.isBlank()) {
            throw AemException("Content path cannot be blank")
        }

        instances.forEach { it.validate() }
    }

    /**
     * CRX package Vault files will be composed from given sources.
     * If both will be non-existing, then files will be auto-generated.
     */
    val vaultFilesDirs : List<File>
        get() {
            val paths = listOf(
                    vaultFilesPath,
                    "$contentPath/${AemPlugin.VLT_PATH}"
            )

            return paths.filter { !it.isNullOrBlank() }.map { File(it) }
        }
}


