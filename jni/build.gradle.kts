plugins { `cpp-library` }

library {
  targetMachines.set(listOf(machines.linux.x86_64))
  linkage.set(listOf(Linkage.SHARED))
  baseName.set("fakecam")
}

tasks {
  withType<CppCompile> {
    val javaHome = File(System.getProperty("java.home"))
    includes.setFrom(
        javaHome.resolve("include"),
        javaHome.resolve("include/linux"),
    )
  }
}
