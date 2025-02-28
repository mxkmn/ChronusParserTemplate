@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.jetbrains.compose)
}

kotlin {
	jvm {
		withJava()
	}

	sourceSets {
		jvmMain.dependencies {
			implementation(compose.desktop.currentOs)
			implementation(libs.decompose.core)
			implementation(libs.decompose.composeext) // needed by LifecycleController

			implementation(projects.appmodules.library.logger)
			implementation(projects.appmodules.model.common)
			implementation(projects.appmodules.model.chronus)
			implementation(projects.appmodules.shared.chronusparsers)
		}
	}
}

compose.desktop {
	application {
		mainClass = "app.chronusparsers.desktop.MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
			packageName = "chronusparsers parsers"
			packageVersion = "1.0.0"
		}
	}
}
