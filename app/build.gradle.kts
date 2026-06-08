plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("kotlin-kapt")
	id("com.google.dagger.hilt.android")
	id("com.google.devtools.ksp")
}

android {
	namespace = "com.taibao.app"
	compileSdk = 35
	defaultConfig {
		applicationId = "com.taibao.app"
		minSdk = 26
		targetSdk = 33
		versionCode = 1
		versionName = "1.0"
	}

	signingConfigs {
		create("release"){
			storeFile = file("${rootDir.absolutePath}/keystore/GoGoGo.jks") // 存储keystore或者是jks文件的路径
			keyAlias = "GoGoKey" // 别名
			keyPassword = "GoGoGo" // 密码
			storePassword = "GoGoGo" // 存储密码
		}
	}

	buildTypes {
		debug {
			isMinifyEnabled = false
			buildConfigField("String", "VERSION_TYPE", "\"VERSION_STATUS_ALPHA\"")
			signingConfig = signingConfigs.getByName("release")
		}
		release {
			isMinifyEnabled = true
			buildConfigField("String", "VERSION_TYPE", "\"VERSION_STATUS_RELEASE\"")
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
			)
			signingConfig = signingConfigs.getByName("release")

		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
	buildFeatures {
		viewBinding = true
		buildConfig = true
	}

}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.material)

	implementation(libs.androidx.navigation.fragment.ktx)
	implementation(libs.androidx.navigation.ui.ktx)

	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.lifecycle.livedata.ktx)
	implementation(libs.androidx.lifecycle.viewmodel.ktx)

	implementation(libs.androidx.preference.ktx)

	implementation(libs.androidx.paging)

	implementation(libs.androidx.room.runtime)
	kapt(libs.androidx.room.compiler)
	implementation(libs.androidx.room.ktx)
	implementation(libs.androidx.room.paging)

	implementation(libs.hilt)
	kapt(libs.hilt.compiler)

	implementation(libs.gson)

	implementation(libs.androidx.worker)

	implementation("com.google.auto.service:auto-service:1.0")
	kapt("com.google.auto.service:auto-service-annotations:1.0")

	implementation(libs.androidx.annotation)
	implementation(libs.coil)
	implementation(libs.customactivityoncrash)
	implementation(libs.android.logger)

	implementation("com.tencent:mmkv:2.4.0")
//	debugImplementation("com.guolindev.glance:glance:1.1.0")

	implementation(libs.permissionx)
	implementation(libs.utilcodex)

	implementation(libs.shapeview)
//	implementation("io.github.jeremyliao:live-event-bus-x:1.8.0")
	implementation(libs.xpopup)

	implementation("com.baidu.lbsyun:BaiduMapSDK_Map:8.1.0")
	implementation("com.baidu.lbsyun:BaiduMapSDK_Util:8.1.0")
	implementation("com.baidu.lbsyun:BaiduMapSDK_Search:8.1.0")
	implementation("com.baidu.lbsyun:BaiduMapSDK_Location:9.6.8")
}
kapt {
	correctErrorTypes = true
}
