plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.loc.hexis.platform"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(projects.shared.core)
    implementation(projects.shared.ui)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.room.runtime)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines)

    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
}

fun execute(vararg command: String): String =
    providers.exec { commandLine(*command) }.standardOutput.asText.get().trim()

val generateChangelogJson by
    tasks.registering {
        description = "Assembling Changelog"
        val inputFile = rootProject.file("CHANGELOG.md")
        val outputDir = file("$projectDir/src/main/assets/")
        val outputFile = File(outputDir, "changelog.json")

        inputs.file(inputFile)
        outputs.file(outputFile)

        doLast {
            if (!outputDir.exists()) outputDir.mkdirs()

            val lines = inputFile.readLines()

            val map = mutableMapOf<String, MutableList<String>>()
            var currentVersion: String? = null

            for (line in lines) {
                when {
                    line.startsWith("## ") -> {
                        currentVersion = line.removePrefix("## ").substringBefore(":").trim()
                        map[currentVersion] = mutableListOf()
                    }

                    line.startsWith("- ") && currentVersion != null -> {
                        map[currentVersion]?.add(line.removePrefix("- ").trim())
                    }
                }
            }

            val json = buildString {
                append("[\n")

                val entriesToKeep = map.entries.take(10)
                entriesToKeep.forEachIndexed { index, entry ->
                    append("  {\n")
                    append("    \"version\": \"${entry.key}\",\n")
                    append("    \"changes\": [\n")

                    entry.value.forEachIndexed { i, item ->
                        append("      \"${item.replace("\"", "\\\"")}\"")
                        if (i != entry.value.lastIndex) append(",")
                        append("\n")
                    }

                    append("    ]\n")
                    append("  }")

                    if (index != entriesToKeep.lastIndex) append(",")
                    append("\n")
                }

                append("]")
            }

            outputFile.writeText(json)
        }
    }

tasks.named("preBuild") { dependsOn(generateChangelogJson) }
