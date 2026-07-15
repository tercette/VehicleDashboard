plugins {
    alias(libs.plugins.android.application)
    // Hilt gera o grafo de DI; KSP é o processador de anotações que ele usa.
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.volks.vehicledashboard"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    // ViewBinding: gera uma classe de binding por layout XML, dando acesso type-safe
    // às views (sem findViewById e sem risco de cast errado). Padrão da ESPEC.
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.volks.vehicledashboard"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Coroutines/Flow: o modelo reativo do app.
    // -core é usado no `domain` (Kotlin puro, sem Android); -android fornece o
    // Dispatcher da Main thread, necessário na `presentation`.
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // API padrão de DI (@Inject). Usada já no `domain` (use case) — Kotlin/Java puro.
    implementation(libs.javax.inject)

    // Hilt: runtime (implementation) + gerador de código (ksp).
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Lifecycle: ViewModel + coleta de Flow ciente do ciclo de vida (repeatOnLifecycle).
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}