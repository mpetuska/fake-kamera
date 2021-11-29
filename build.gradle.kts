plugins {
  if (System.getenv("CI") == null) {
    id("plugin.git-hooks")
  }
  id("plugin.application-compose")
  `cpp-library`
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
  }
}

library {
  targetMachines.set(listOf(machines.linux.x86_64))
  linkage.set(listOf(Linkage.SHARED))
  baseName.set("fakecam")
  source.setFrom("src/cppMain/cpp")
  privateHeaders.setFrom("src/cppMain/headers")
  publicHeaders.setFrom("src/cppMain/public")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation("org.reduxkotlin:redux-kotlin-threadsafe:_")
        implementation("org.reduxkotlin:redux-kotlin-thunk:_")
        implementation("org.kodein.di:kodein-di-framework-compose:_")
        implementation(compose.runtime)
      }
    }
    named("jvmMain") {
      dependencies {
        implementation("org.openpnp:opencv:_")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:_")
      }
    }
  }
}

compose { desktop { application { mainClass = "dev.petuska.fake.kamera.MainKt" } } }

tasks {
  withType<CppCompile> {
    val javaHome = File(System.getProperty("java.home"))
    includes.setFrom(
        javaHome.resolve("include"),
        javaHome.resolve("include/linux"),
    )
  }
  named<Copy>("jvmProcessResources") {
    val linkRelease by getting(LinkSharedLibrary::class)
    dependsOn(linkRelease)
    from(linkRelease.outputs) {
      duplicatesStrategy = DuplicatesStrategy.WARN
      into("META-INF/native/linux-64")
    }
  }
}
