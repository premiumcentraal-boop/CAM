plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cam.xposed"
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
    implementation(project(":cam-meta"))
    // LSPosed API — added in Run 3 when hooks begin; kept out now to stay honest about scope.
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.20")
}
