package dev.petuska.fake.kamera.jni

import java.io.*
import java.lang.reflect.Field

/**
 * Simple library class for working with JNI (Java Native Interface)
 *
 * @see http://frommyplayground.com/how-to-load-native-jni-library-from-jar
 * @see https://github.com/sarxos/v4l4j/blob/master/src/main/java/com/github/sarxos/v4l4j/NativeUtils.java
 *
 * @author Adam Heirnich <adam></adam>@adamh.cz>, http://www.adamh.cz
 */
object NativeUtils {
  /**
   * Loads library from current JAR archive
   *
   * The file from JAR is copied into system temporary directory and then loaded. The temporary file
   * is deleted after exiting. Method uses String as filename because the pathname is "abstract",
   * not system-dependent.
   *
   * @param jarpath The filename inside JAR as absolute path (beginning with '/'), e.g. /package/File.ext
   * @param libs to load
   * @throws IOException If temporary file creation or read/write operation fails
   * @throws IllegalArgumentException If source file (param path) does not exist
   * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than
   * three characters (restriction of {@see File#createTempFile(java.lang.String,
   * * java.lang.String)}).
   */
  @Suppress("ThrowsCount")
  fun loadLibraryFromJar(jarpath: String, libs: Array<String>) {
    val libspath: File = File.createTempFile("libs", "")
    if (!libspath.delete()) {
      throw IOException("Cannot clean $libspath")
    }
    if (!libspath.exists()) {
      if (!libspath.mkdirs()) {
        throw IOException("Cannot create directory $libspath")
      }
    }
    libspath.deleteOnExit()
//    try {
//      addLibraryPath(libspath.absolutePath)
//    } catch (e: Exception) {
//      throw IOException(e)
//    }
    for (lib in libs) {
      val libfile = "lib$lib.so"
      val path = "$jarpath/$libfile"
      require(path.startsWith("/")) { "The path to be absolute (start with '/')." }
      val file = File(libspath, libfile)
      file.createNewFile()
      file.deleteOnExit()
      val buffer = ByteArray(1024)
      var readBytes: Int
      val `is` =
        NativeUtils::class.java.getResourceAsStream(path)
          ?: throw FileNotFoundException("File $path was not found inside JAR.")
      val os: OutputStream = FileOutputStream(file)
      try {
        while (`is`.read(buffer).also { readBytes = it } != -1) {
          os.write(buffer, 0, readBytes)
        }
      } finally {
        os.close()
        `is`.close()
      }
      System.load(file.absolutePath)
    }
  }

  /**
   * Adds the specified path to the java library path
   *
   * @param pathToAdd the path to add
   * @throws Exception
   */
  @Throws(Exception::class)
  fun addLibraryPath(pathToAdd: String) {
    val usrPathsField: Field = ClassLoader::class.java.getDeclaredField("usr_paths")
    usrPathsField.isAccessible = true

    // get array of paths
    @Suppress("UNCHECKED_CAST")
    val paths = usrPathsField.get(null) as Array<String>

    // check if the path to add is already present
    for (path in paths) {
      if (path == pathToAdd) {
        return
      }
    }

    // add the new path
    val newPaths: Array<String?> = paths.copyOf(paths.size + 1)
    newPaths[newPaths.size - 1] = pathToAdd
    usrPathsField.set(null, newPaths)
  }
}
