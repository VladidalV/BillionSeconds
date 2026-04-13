plugins { id("kmp-feature-convention") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreDomain)
            implementation(projects.core.coreUi)
        }
    }
}

android { namespace = "com.example.billionseconds.feature.countdown" }
