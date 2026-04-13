plugins {
    id("kmp-core-convention")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreDomain)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

android {
    namespace = "com.example.billionseconds.core.data"
}
