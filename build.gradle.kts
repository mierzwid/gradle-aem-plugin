import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    kotlin("jvm") version "1.3.10"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC11"
    id("com.jfrog.bintray") version "1.8.4"
}

group = "com.cognifide.gradle"
version = "6.0.0-beta"
description = "Gradle AEM Plugin"
defaultTasks = listOf("build", "publishToMavenLocal")

repositories {
    jcenter()
}

dependencies {
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    implementation("org.apache.commons:commons-lang3:3.4")
    implementation("commons-io:commons-io:2.4")
    implementation("commons-validator:commons-validator:1.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.8.8")
    implementation("org.reflections:reflections:0.9.9")
    implementation("org.apache.jackrabbit.vault:vault-cli:3.2.4")
    implementation("org.jsoup:jsoup:1.10.3")
    implementation("org.samba.jcifs:jcifs:1.3.18-kohsuke-1")
    implementation("biz.aQute.bnd:biz.aQute.bnd.gradle:4.0.0")
    implementation("org.zeroturnaround:zt-zip:1.11")
    implementation("com.hierynomus:sshj:0.21.1")
    implementation("org.apache.httpcomponents:httpclient:4.5.4")
    implementation("org.apache.httpcomponents:httpmime:4.5.4")
    implementation("org.osgi:org.osgi.core:6.0.0")
    implementation("io.pebbletemplates:pebble:3.0.4")
    implementation("com.dorkbox:Notify:3.7")
    implementation("com.jayway.jsonpath:json-path:2.4.0")
    implementation("de.gesellix:docker-client:2018-12-30T15-32-58")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.3.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testImplementation(gradleTestKit())
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("org.junit-pioneer:junit-pioneer:0.2.2")

    "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:1.0.0-RC11")
}

tasks {
    register<Jar>("sourcesJar") {
        classifier = "sources"
        dependsOn("classes")
        from(sourceSets["main"].allSource)
    }

    named<ProcessResources>("processResources") {
        doLast {
            file("$buildDir/resources/main/build.json").printWriter().use {
                it.print("""{
                    "pluginVersion": "${project.version}",
                    "gradleVersion": "${project.gradle.gradleVersion}"
            }""".trimIndent())
            }
        }
    }

    named<Test>("test") {
        testLogging {
            events = setOf(TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.SHORT
        }

        useJUnitPlatform()
        dependsOn(named("publishToMavenLocal"))
    }
}

gradlePlugin {
    plugins {
        create("config") {
            id = "com.cognifide.aem.config"
            implementationClass = "com.cognifide.gradle.aem.config.ConfigPlugin"
        }
        create("tooling") {
            id = "com.cognifide.aem.tooling"
            implementationClass = "com.cognifide.gradle.aem.tooling.ToolingPlugin"
        }
        create("package") {
            id = "com.cognifide.aem.package"
            implementationClass = "com.cognifide.gradle.aem.pkg.PackagePlugin"
        }
        create("bundle") {
            id = "com.cognifide.aem.bundle"
            implementationClass = "com.cognifide.gradle.aem.bundle.BundlePlugin"
        }
        create("instance") {
            id = "com.cognifide.aem.instance"
            implementationClass = "com.cognifide.gradle.aem.instance.InstancePlugin"
        }
        create("environment") {
            id = "com.cognifide.aem.environment"
            implementationClass = "com.cognifide.gradle.aem.environment.EnvironmentPlugin"
        }
    }
}

detekt {
    config.from(file("detekt.yml"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
}

bintray {
    user = (project.findProperty("bintray.user") ?: System.getenv("BINTRAY_USER"))?.toString()
    key = (project.findProperty("bintray.key") ?: System.getenv("BINTRAY_KEY"))?.toString()
    setPublications("mavenJava")
    with(pkg) {
        repo = "maven-public"
        name = "gradle-aem-plugin"
        userOrg = "cognifide"
        setLicenses("Apache-2.0")
        vcsUrl = "https://github.com/Cognifide/gradle-aem-plugin.git"
        setLabels("aem", "cq", "vault", "scr")
        with(version) {
            name = project.version.toString()
            desc = "${project.description} ${project.version}"
            vcsTag = project.version.toString()
        }
    }
    publish = (project.findProperty("bintray.publish") ?: "true").toString().toBoolean()
    override = (project.findProperty("bintray.override") ?: "false").toString().toBoolean()
}