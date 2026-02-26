plugins {
    id("buildsrc.convention.kotlin-multiplatform")
    alias(libs.plugins.kotlinPluginSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinxDatetime)
            implementation(libs.obor)
            implementation(libs.kotlinxSerializationCore)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotestProperty)
        }
    }
}
