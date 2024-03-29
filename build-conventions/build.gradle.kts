@file:Suppress("OPT_IN_IS_NOT_ENABLED")

import de.fayard.refreshVersions.core.RefreshVersionsCorePlugin
import de.fayard.refreshVersions.core.internal.InternalRefreshVersionsApi

plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
  mavenCentral()
  google()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:_")
  implementation("org.jetbrains.compose:compose-gradle-plugin:_")
  implementation("org.jetbrains.kotlin:kotlin-serialization:_")
  implementation("com.github.jakemarsden:git-hooks-gradle-plugin:_")
  implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:_")
  implementation("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:_")
  @OptIn(InternalRefreshVersionsApi::class)
  implementation("de.fayard.refreshVersions:refreshVersions-core:${RefreshVersionsCorePlugin.currentVersion}")
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}
