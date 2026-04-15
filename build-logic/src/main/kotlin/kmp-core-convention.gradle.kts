import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Convention plugin для core-модулей без Compose (чистая Kotlin/domain логика).
 *
 * Использование:
 *   plugins { id("kmp-core-convention") }
 *   android { namespace = "com.example.billionseconds.core.xxx" }
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

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
        // jsAndWasmSharedMain — стандартное KMP-имя для кода, общего для JS и WASM JS.
        // IDE ассоциирует этот сет с browser-таргетами → kotlinx.browser резолвится корректно.
        val jsAndWasmSharedMain by creating { dependsOn(commonMain.get()) }
        jsMain.get().dependsOn(jsAndWasmSharedMain)
        wasmJsMain.get().dependsOn(jsAndWasmSharedMain)

        // При ручной настройке intermediate source sets дефолтная иерархия KMP отключается,
        // поэтому iosMain нужно явно объявить как промежуточный сет для iOS таргетов.
        val iosMain by creating { dependsOn(commonMain.get()) }
        iosArm64Main.get().dependsOn(iosMain)
        iosSimulatorArm64Main.get().dependsOn(iosMain)

        commonMain.dependencies {
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
