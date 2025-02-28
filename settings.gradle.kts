@file:Suppress("UnstableApiUsage")

rootProject.name = "ChronusParserTemplate"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
	repositories {
		google {
			mavenContent {
				includeGroupAndSubgroups("androidx")
				includeGroupAndSubgroups("com.android")
				includeGroupAndSubgroups("com.google")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

dependencyResolutionManagement {
	repositories {
		google {
			mavenContent {
				includeGroupAndSubgroups("androidx")
				includeGroupAndSubgroups("com.android")
				includeGroupAndSubgroups("com.google")
			}
		}
		mavenCentral()
	}
}

include(":appmodules:library:logger")
include(":appmodules:library:navigation")
include(":appmodules:library:ui")

include(":appmodules:model:common")
include(":appmodules:model:chronus")

include(":appmodules:datasource:network:chronus:irkutsk-irnitu")
include(":appmodules:datasource:network:chronus:irkutsk-igu-imit")
include(":appmodules:datasource:network:chronus:yourcity-youruniversity")
include(":appmodules:datasource:network:chronus:main") // collecting parsers in one class

// include(":appmodules:datarepository:chronus") // FIXME: use repo instead of source

include(":appmodules:feature:chronus:contributor")
include(":appmodules:feature:chronus:places")
include(":appmodules:feature:chronus:schedule")
include(":appmodules:feature:chronus:search")

include(":appmodules:shared:chronusparsers")

include(":appmodules:app:chronusparsers:android")
include(":appmodules:app:chronusparsers:desktop")
