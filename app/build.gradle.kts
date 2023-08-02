import com.android.build.api.variant.BuildConfigField
import java.io.FileInputStream
//import org.gradle.initialization.Environment.Properties
import java.util.Properties



plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {


    namespace = "com.example.brandtest"
    compileSdk = 33
    packagingOptions.resources.excludes += "META-INF/DEPENDENCIES"

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {

        applicationId = "com.example.brandtest"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val properties = Properties()
        if (File(rootProject.projectDir, "auth.properties").exists()) {
            FileInputStream(File(rootProject.projectDir, "auth.properties")).use {
                properties.load(it)
            }
        }

        val apiKey = properties.getProperty("API_KEY")

        buildConfigField("String", "API_KEY", "\"$apiKey\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))

    //Barcode ML
    implementation ("com.google.mlkit:barcode-scanning:17.1.0")

    //Text
    implementation ("com.google.mlkit:text-recognition:16.0.0")

    //HTTP API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation("com.google.android.material:material:1.9.0")
    implementation ("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    //implementation ("com.google.cloud:google-cloud-vision:3.20.0")
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.6.1")
    //implementation("androidx.concurrent:concurrent-futures:1.1.0")
    //implementation("com.google.guava:guava:32.1.2-android")

    implementation("com.google.apis:google-api-services-vision:v1-rev16-1.22.0")

    implementation("com.google.api-client:google-api-client-android:1.22.0") {
        exclude(module= "httpclient")
    }
    implementation ("com.google.http-client:google-http-client-gson:1.20.0") {
        exclude(module= "httpclient")
    }




    var camerax_version = "1.2.3"
    // CameraX core library using camera2 implementation
    implementation ("androidx.camera:camera-camera2:$camerax_version")
    // CameraX Lifecycle Library
    implementation ("androidx.camera:camera-lifecycle:$camerax_version")
    // CameraX View class
    implementation ("androidx.camera:camera-view:$camerax_version")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}