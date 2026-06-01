plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // id("kotlin-kapt")  // 暂时禁用KAPT
    alias(libs.plugins.navigation.safeargs)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")  // 重新启用KSP
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20" // 添加 serialization 插件
}

android {
    namespace = "com.example.itemmanagement"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jiwanwu.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 添加Room模式导出目录
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        
        // 高德地图 API Key - Debug 版本（临时使用，等你申请新 Key 后替换）
        manifestPlaceholders["AMAP_KEY"] = "261b800a2472bcff727928a42854dd32"
    }

    signingConfigs {
        create("release") {
            storeFile = file("${rootProject.projectDir}/keystore/release-key.jks")
            storePassword = "jiwanwu2024"
            keyAlias = "jiwanwu-key"
            keyPassword = "jiwanwu2024"
        }
    }

    buildTypes {
        debug {
            // Debug 版本使用调试 Key（只绑定 debug.keystore 的 SHA1）
            manifestPlaceholders["AMAP_KEY"] = "261b800a2472bcff727928a42854dd32"
        }
        
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            
            // Release 版本使用发布 Key（只绑定 release-key.jks 的 SHA1）
            // 等你申请新 Key 后替换这里
            manifestPlaceholders["AMAP_KEY"] = "261b800a2472bcff727928a42854dd32"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
    
    // kapt {
    //     correctErrorTypes = true
    //     useBuildCache = true
    // }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    
    androidResources {
        additionalParameters += listOf("--warn-manifest-validation")
    }
    
    lint {
        // 添加lint配置
        abortOnError = false
        checkReleaseBuilds = false
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    // AndroidX核心库
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Lifecycle组件
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)

    // Navigation组件
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Jetpack Compose 依赖
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata") // LiveData integration
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // Coil for Compose Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Ktor for networking
    val ktorVersion = "2.3.8"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Room数据库
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)  // 重新启用KSP用于Room

    // Glide图片加载
    implementation(libs.glide)
    ksp(libs.glide.compiler)  // 使用KSP用于Glide

    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    // SmartRefreshLayout 3.0.0-alpha - AndroidX完美支持版本 🌟
    implementation("io.github.scwang90:refresh-layout-kernel:3.0.0-alpha")      // 核心必须依赖
    implementation("io.github.scwang90:refresh-header-material:3.0.0-alpha")     // Material Design头部
    implementation("io.github.scwang90:refresh-header-classics:3.0.0-alpha")     // 经典头部（备选）
    implementation("io.github.scwang90:refresh-header-radar:3.0.0-alpha")        // 雷达头部（炫酷）
    implementation("io.github.scwang90:refresh-footer-classics:3.0.0-alpha")     // 经典底部加载
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)

    // Google Flexbox
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    
    // PhotoView for zoomable images
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    
    // 高德3D地图SDK - 指定版本 9.7.0（与搜索SDK版本匹配）
    implementation("com.amap.api:3dmap:9.7.0")
    // 高德地图搜索服务SDK - 指定版本 9.7.0
    implementation("com.amap.api:search:9.7.0")
    
    // AAInfographics (AAChartCore-Kotlin) for charts - 本地依赖
    implementation(project(":charts"))
} 
