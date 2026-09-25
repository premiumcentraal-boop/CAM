plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cam.meta"
    compileSdk = 35
    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":cam-core"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.20")
    testImplementation("junit:junit:4.13.2")
}
