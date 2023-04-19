@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-gradle-plugin`
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.taskTree)
}

multiJvm {
    jvmVersionForCompilation.set(11)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.bundles.kotlin.testing)
    api(project(":utils"))
    api(project(":jakta-bdi"))
    api(libs.tuprolog.dsl.theory)
    api(libs.tuprolog.dsl.core)
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
                freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.detekt {
    onlyIf {
        project.hasProperty("runDetect")
    }
}