plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.bk.bk1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bk.bk1"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //Maps
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.maps.android:maps-compose:4.3.0")
    implementation ("com.google.maps.android:android-maps-utils:3.8.2")

    //SpeedDial
    implementation("com.leinardi.android:speed-dial.compose:2.0.0-alpha01")

    //Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    //Livedata
    implementation("androidx.compose.runtime:runtime-livedata:1.6.2")

    //Movesense + rxjava
    implementation(files("./libs/mdslib-3.15.0(1)-release.aar"))
    implementation("com.polidea.rxandroidble2:rxandroidble:1.10.2")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.8")

    //Room
    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    //Json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    //Leak canary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:3.0-alpha-1")

    //Event Bus
    implementation("com.squareup:otto:1.3.8")

    //Dagger hilt
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //Dropdown menu
    implementation("me.saket.cascade:cascade:2.3.0")
    implementation("me.saket.cascade:cascade-compose:2.3.0")

    //Screenshot
    implementation("com.github.SmartToolFactory:Compose-Screenshot:1.0.3")
//    implementation("com.github.lucasxvirtual:compose-screen-capture:1.0.0")

    //Charts
    implementation("com.patrykandpatrick.vico:compose:2.0.0-alpha.12")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.12")
    implementation("com.patrykandpatrick.vico:core:2.0.0-alpha.12")

    //Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")

}