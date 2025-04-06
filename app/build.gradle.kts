import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    kotlin("kapt")
    id ("kotlin-parcelize")
}
val localProperties = Properties().apply {
    // Load properties from local.properties file
    load(project.rootProject.file("local.properties").inputStream())
}


android {
    namespace = "com.mithilakshar.learnsource"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.mithilakshar.learnsource"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }




    buildTypes {
        debug {
            buildConfigField("boolean", "FIREBASE_ANALYTICS_ENABLED", "false")
            buildConfigField("String", "sUrl",  localProperties.getProperty("sUrl"))
            buildConfigField("String", "sK",  localProperties.getProperty("sK"))
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("boolean", "FIREBASE_ANALYTICS_ENABLED", "true")
            buildConfigField("String", "sUrl",  localProperties.getProperty("sUrl"))
            buildConfigField("String", "sK",  localProperties.getProperty("sK"))
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
        viewBinding = true
        buildConfig = true
    }
}


dependencies {

    implementation("io.ktor:ktor-client-cio:2.3.4")
    implementation (libs.android.pdf.viewer)

    implementation("io.github.jan-tennert.supabase:storage-kt:1.3.2")
    implementation (libs.glide)
    implementation (libs.android.lottie)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.okhttp)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.common)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.ads.lite)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)


    annotationProcessor(libs.androidx.room.room.compiler)
    kapt(libs.androidx.room.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.app.update.ktx)
    implementation(libs.review.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}