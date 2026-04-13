plugins {
    id("kmp-core-convention")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreDomain)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
    }
}

android {
    namespace = "com.example.billionseconds.core.navigation"
}
