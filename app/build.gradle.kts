plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id ("kotlin-parcelize")
    //id ("com.osacky.doctor") version "0.7.3"

}

android {
    namespace = "com.example.a123"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.a123"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("debug")
    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("org.osmdroid:osmdroid-android:6.1.15")
    implementation("org.osmdroid:osmdroid-wms:6.1.15")
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.itextpdf:itext7-core:7.1.15")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    kapt ("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation ("androidx.activity:activity-ktx:1.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("androidx.room:room-runtime:2.5.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    kapt ("androidx.room:room-compiler:2.5.1")
    implementation ("androidx.room:room-ktx:2.5.1")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("com.drewnoakes:metadata-extractor:2.18.0")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("org.tensorflow:tensorflow-lite:2.10.0")
    implementation ("org.tensorflow:tensorflow-lite-select-tf-ops:2.13.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.3")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation ("com.microsoft.onnxruntime:onnxruntime-android:1.13.1")
    implementation ("org.tensorflow:tensorflow-lite:2.6.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-simplexml:2.9.0")


}

