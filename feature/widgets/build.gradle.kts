plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.loc.hexis.widgets"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures { compose = true }

    sourceSets {
        getByName("main") {
            res.directories.addAll(
                listOf(
                    "src/main/res",
                    "${project(":shared:ui").projectDir}/src/commonMain/composeResources",
                )
            )
        }
    }
}

dependencies {
    implementation(projects.shared.core)
    implementation(projects.shared.ui)
    implementation(projects.core.database)
    implementation(projects.core.platform)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.glance.appwidget.preview)
    implementation(libs.androidx.glance.preview)

    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.materialkolor)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines)

    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
}

kotlin {
    compilerOptions {
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
    }
}