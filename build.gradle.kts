import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.koin.compiler) apply false
}

allprojects {
    apply { plugin(rootProject.libs.plugins.spotless.get().pluginId) }
    configure<SpotlessExtension> {
        kotlin {
            ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
            target("src/**/*.kt")
            targetExclude("${layout.buildDirectory}/**/*.kt")
            licenseHeaderFile(rootProject.file("spotless/copyright.txt"))
        }
        kotlinGradle {
            ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
            target("*.kts")
            targetExclude("${layout.buildDirectory}/**/*.kts")
            licenseHeaderFile(rootProject.file("spotless/copyright.txt"), "(^(?![\\/ ]\\*).*$)")
            toggleOffOn()
        }
        format("xml") {
            target("src/**/*.xml")
            targetExclude("**/build/", ".idea/")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
