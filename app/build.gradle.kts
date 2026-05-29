plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.dilanne.bypass"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.dilanne.bypass"
        minSdk = 30
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = true
        ignoreWarnings = false
        checkDependencies = true
        xmlReport = true
        htmlReport = true
        
        // Sécurité : Activer les vérifications spécifiques
        enable.add("HardcodedText")
        enable.add("InsecureStorage")
        enable.add("TrustAllX509TrustManager")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Security
    implementation(libs.security.crypto)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Password Security & Analysis
    implementation(libs.nbvcxz)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.okhttp)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)

    // Image Loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // WorkManager
    implementation(libs.work.runtime)
    androidTestImplementation(libs.work.testing)

    testImplementation(libs.junit)
    testImplementation(libs.mockito)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.mockito.android)
}
