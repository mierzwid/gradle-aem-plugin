package com.cognifide.gradle.aem.test

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.TempDirectory

@Disabled
@ExtendWith(TempDirectory::class)
class ComposeTaskTest : AemTest() {

    @Test
    fun shouldComposePackageWithBundleAndContent() {
        buildTask("compose/bundle-and-content", ":aemCompose") {
            val pkg = file("build/distributions/example-1.0.0-SNAPSHOT.zip")

            assertPackage(pkg)
            assertPackageFile(pkg, "jcr_root/apps/example/.content.xml")
            assertPackageFile(pkg, "jcr_root/apps/example/install/example-1.0.0-SNAPSHOT.jar")
            assertPackageFile(pkg, "META-INF/vault/hooks/hook.jar")
            assertPackageFile(pkg, "META-INF/vault/nodetypes.cnd")
        }
    }

    @Test
    fun shouldComposePackageAssemblyAndSingles() {
        buildTasks("compose/assembly", "aemCompose") {
            val assemblyPkg = file("build/distributions/example-1.0.0-SNAPSHOT.zip")
            assertPackage(assemblyPkg)
            assertPackageFile(assemblyPkg, "jcr_root/apps/example/core/.content.xml")
            assertPackageBundle(assemblyPkg, "jcr_root/apps/example/core/install/example.core-1.0.0-SNAPSHOT.jar")
            assertPackageFile(assemblyPkg, "jcr_root/apps/example/common/.content.xml")
            assertPackageBundle(assemblyPkg, "jcr_root/apps/example/common/install/example.common-1.0.0-SNAPSHOT.jar")
            assertPackageBundle(assemblyPkg, "jcr_root/apps/example/common/install/kotlin-osgi-bundle-1.2.21.jar")
            assertPackageFile(assemblyPkg, "jcr_root/etc/designs/example/.content.xml")
            assertPackageFile(assemblyPkg, "META-INF/vault/hooks/hook1.jar")
            assertPackageFile(assemblyPkg, "META-INF/vault/hooks/hook2.jar")

            val corePkg = file("core/build/distributions/example.core-1.0.0-SNAPSHOT.zip")
            assertPackage(corePkg)
            assertPackageFile(corePkg, "jcr_root/apps/example/core/.content.xml")
            assertPackageBundle(corePkg, "jcr_root/apps/example/core/install/example.core-1.0.0-SNAPSHOT.jar")

            val commonPkg = file("common/build/distributions/example.common-1.0.0-SNAPSHOT.zip")
            assertPackage(commonPkg)
            assertPackageFile(commonPkg, "jcr_root/apps/example/common/.content.xml")
            assertPackageBundle(commonPkg, "jcr_root/apps/example/common/install/example.common-1.0.0-SNAPSHOT.jar")
            assertPackageFile(commonPkg, "jcr_root/apps/example/common/install/kotlin-osgi-bundle-1.2.21.jar")

            val designPkg = file("design/build/distributions/example.design-1.0.0-SNAPSHOT.zip")
            assertPackage(designPkg)
            assertPackageFile(designPkg, "jcr_root/etc/designs/example/.content.xml")
        }
    }

}