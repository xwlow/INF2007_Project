plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.inf2007_project"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.inf2007_project"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.generativeai)
    implementation(libs.androidx.espresso.core)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

//    plugins {
//        alias(libs.plugins.android.application)
//        alias(libs.plugins.kotlin.android)
//        alias(libs.plugins.kotlin.compose)
//        alias(libs.plugins.google.gms.google.services)
//    }

    android {
        namespace = "com.example.inf2007_project"
        compileSdk = 35

        defaultConfig {
            applicationId = "com.example.inf2007_project"
            minSdk = 26
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        kotlinOptions {
            jvmTarget = "11"
        }
        buildFeatures {
            compose = true
        }
    }

    dependencies {

        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation(libs.androidx.navigation.compose)
        implementation(libs.firebase.auth)
        implementation(libs.androidx.runtime.livedata)
        implementation(libs.firebase.database)
        implementation(libs.firebase.firestore)
        implementation("com.google.android.gms:play-services-location:21.3.0")
        implementation(libs.accompanist.permissions)
        implementation("androidx.compose.material3:material3:1.3.1")
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
        implementation("com.google.firebase:firebase-messaging-ktx:24.1.1")


        // Retrofit dependencies (Kotlin DSL syntax)
        implementation("com.squareup.retrofit2:retrofit:2.9.0") // Retrofit library
        implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Gson converter for Retrofit

        // Debugging for nearbysearch API
        implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

        implementation ("androidx.health.connect:connect-client:1.1.0-alpha12")
        implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
        implementation ("io.coil-kt:coil-compose:2.2.2")
    }
}