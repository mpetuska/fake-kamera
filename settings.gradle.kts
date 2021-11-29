plugins {
  id("de.fayard.refreshVersions") version "0.23.0"
  id("com.gradle.enterprise") version "3.7.2"
}

rootProject.name = "fake-kamera"

refreshVersions { extraArtifactVersionKeyRules(file("versions.rules")) }

include(":jni")
