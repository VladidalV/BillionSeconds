import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Convention plugin для фича-модулей.
 * Применяет KMP + Compose и настраивает все таргеты (Android, iOS, JS, WASM).
 *
 * Использование в фича-модуле:
 *   plugins { id("kmp-feature-convention") }
 *   android { namespace = "com.example.billionseconds.xxx" }
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
}

apply(plugin = "org.jetbrains.kotlin.plugin.compose")

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            isStatic = true
        }
    }

    js { browser() }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        val jsAndWasmSharedMain by creating { dependsOn(commonMain.get()) }
        jsMain.get().dependsOn(jsAndWasmSharedMain)
        wasmJsMain.get().dependsOn(jsAndWasmSharedMain)

        val iosMain by creating { dependsOn(commonMain.get()) }
        iosArm64Main.get().dependsOn(iosMain)
        iosSimulatorArm64Main.get().dependsOn(iosMain)

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
        }
        commonTest.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test:2.3.0")
        }
    }
}

android {
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
