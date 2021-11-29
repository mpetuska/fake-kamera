import org.jetbrains.kotlin.gradle.tasks.KotlinCompile as KotlinCompileJvm
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinTest

plugins {
  id("com.diffplug.spotless")
  idea
}

repositories {
  mavenCentral()
  maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  maven("https://oss.sonatype.org/content/repositories/snapshots")
  google()
}

idea {
  module {
    isDownloadSources = true
    isDownloadJavadoc = true
  }
}

spotless {
  kotlin { ktfmt() }
  kotlinGradle { ktfmt() }
}

afterEvaluate {
  tasks {
    project.properties["org.gradle.targetCompatibility"]?.toString()?.let {
      withType<KotlinCompileJvm> { kotlinOptions { jvmTarget = it } }
      withType<AbstractCompile> { targetCompatibility = it }
    }
    withType<Test> { useJUnitPlatform() }
    if (tasks.findByName("allCompile") == null) {
      register("allCompile") {
        dependsOn(withType<KotlinCompile<*>>())
        group = "build"
        description = "Compiles all kotlin sourceSets"
      }
    }
    if (tasks.findByName("allTests") == null) {
      register("allTests") {
        dependsOn(withType<KotlinTest>())
        group = "verification"
        description = "Runs all kotlin tests"
      }
    }
  }
}
