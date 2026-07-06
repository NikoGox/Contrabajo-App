plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.movil.contrabajo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.movil.contrabajo"
        minSdk = 24
        targetSdk = 36
        versionCode = 20
        versionName = "0.16.2-Alpha"

        // ─────────────────────────────────────────────────────────────────────
        // CONFIGURACIÓN DE ENTORNO (URLs de los microservicios)
        //
        // Existen dos perfiles de conexión. Mantén descomentado UNO solo:
        //
        //   • DOCKER (local): para desarrollo con el backend corriendo en
        //     docker-compose en esta misma PC. El emulador Android alcanza el
        //     host mediante la IP especial 10.0.2.2 (loopback emulador → host).
        //     Es el perfil por defecto durante el desarrollo.
        //
        //   • NUBE (producción): apunta al despliegue en la nube. La IP puede
        //     CAMBIAR entre despliegues; actualízala aquí cuando rote. Se
        //     mantiene COMENTADO y solo se activa al compilar para distribución.
        //
        // Para alternar: comenta el bloque activo y descomenta el otro.
        // ─────────────────────────────────────────────────────────────────────

        // ── DOCKER (local) — ACTIVO ──
        // ── DOCKER (local) — ACTIVO ──
        // 1. Define la IP Base aquí en una variable string de Kotlin
        val dockerHost = "http://10.0.2.2" // Cambia aquí la IP del host si compilas fuera del emulador

        // 2. Usando las comillas escapadas y la interpolación de Kotlin
        //buildConfigField("String", "USUARIOS_BASE_URL", "\"$dockerHost:8081/\"")
        //buildConfigField("String", "SERVICIOS_BASE_URL", "\"$dockerHost:8082/\"")
        //buildConfigField("String", "COMUNICACIONES_BASE_URL", "\"$dockerHost:8083/\"")
        //buildConfigField("String", "FOTOS_BASE_URL", "\"$dockerHost:8084/\"")

        // ── NUBE (producción) — INACTIVO (IP puede variar, actualizar al rotar) ──
        buildConfigField("String", "USUARIOS_BASE_URL", "\"http://20.114.137.86:8081/\"")
        buildConfigField("String", "SERVICIOS_BASE_URL", "\"http://20.114.137.86:8082/\"")
        buildConfigField("String", "COMUNICACIONES_BASE_URL", "\"http://20.114.137.86:8083/\"")
        buildConfigField("String", "FOTOS_BASE_URL", "\"http://20.114.137.86:8084/\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Play Console valida 16 KB page size en libs nativas para Android 15+.
        // Empaquetamos solo ABIs ARM para distribucion (dispositivos reales).
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // CameraX (actualizado para compatibilidad moderna de NDK/page-size)
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    // ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.1")
    // WorkManager — tareas en segundo plano (polling de mensajes)
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
