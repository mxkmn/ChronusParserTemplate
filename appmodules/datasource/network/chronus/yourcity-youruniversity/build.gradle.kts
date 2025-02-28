import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.serialization)
}

kotlin {
	applyDefaultHierarchyTemplate()
	jvm()
	iosArm64() // iOS
	iosSimulatorArm64() // macOS Apple Silicon
	iosX64() // macOS Intel

	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.target.get()))
		}
	}

	sourceSets {
		commonMain.dependencies {
			implementation(libs.ktor.client.core)
			implementation(libs.ktor.serialization)
			implementation(libs.ksoup)
			implementation(libs.charsets)

			implementation(projects.appmodules.library.logger)
			implementation(projects.appmodules.model.common)
			implementation(projects.appmodules.model.chronus)
		}
	}
}

android {
	namespace = "datasource.network.chronusparsers.yourcity.youruniversity"
	compileSdk = libs.versions.androidsdk.target.get().toInt()

	defaultConfig {
		minSdk = libs.versions.androidsdk.min.get().toInt()
	}

	compileOptions {
		sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
		targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.target.get())
	}
}
