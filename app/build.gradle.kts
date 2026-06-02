plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.guangyinrusuo.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.guangyinrusuo.app"
        minSdk = 29  // Android 10 — 覆盖绝大多数小米15用户
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

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

    // Java 17编译兼容 — 配合AGP 8.5+要求
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true  // 启用ViewBinding，简化findViewById
    }
}

dependencies {
    // ─── AndroidX 核心 ───
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.7.1")

    // ─── Material 3 组件库 ───
    implementation("com.google.android.material:material:1.12.0")

    // ─── 约束布局 + RecyclerView + CardView ───
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // ─── Navigation ───
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // ─── Lifecycle (ViewModel + LiveData) ───
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.8.4")

    // ─── Room 数据库 ───
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ─── WorkManager (定时后台任务) ───
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ─── 图表库 (APP使用时间统计可视化) ───
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // ─── Coroutines ───
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ─── 测试 ───
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
