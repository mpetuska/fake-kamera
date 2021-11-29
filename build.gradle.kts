plugins {
  if (System.getenv("CI") == null) {
    id("plugin.git-hooks")
  }
  id("plugin.application-compose")
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

kotlin {
  sourceSets { named("jvmMain") { dependencies { implementation("org.openpnp:opencv:_") } } }
}

compose { desktop { application { mainClass = "dev.petuska.fake.kamera.MainKt" } } }

tasks {
  named<Copy>("jvmProcessResources") {
    val jniRelease = project(":jni").tasks.named<LinkSharedLibrary>("linkRelease").get()
    dependsOn(jniRelease)
    from(jniRelease.outputs) {
      duplicatesStrategy = DuplicatesStrategy.WARN
      into("META-INF/native/linux-64")
    }
  }
}
