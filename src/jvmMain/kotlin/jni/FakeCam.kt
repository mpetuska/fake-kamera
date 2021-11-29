package dev.petuska.fake.kamera.jni

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.io.IOException
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class FakeCam(device: String, val width: Int, val height: Int) : AutoCloseable {
  private val fd: Int
  constructor(device: Int, width: Int, height: Int) : this("/dev/video$device", width, height)
  init {
    fd = open(device, width, height)
    println("FD: $fd")
  }

  private external fun open(device: String, width: Int, height: Int): Int
  private external fun writeFrame(fd: Int, frame: ByteArray): Boolean
  private external fun close(fd: Int): Int

  fun writeFrame(frame: ByteArray): Boolean = writeFrame(fd, frame)

  fun writeMat(mat: Mat) {
    val frame = ByteArray(width * height * 3)
    frame.drawImage(mat.toBufferedImage())
    writeFrame(frame)
  }

  private fun Mat.toBufferedImage(): BufferedImage {
    val resizedMat = Mat()
    Imgproc.resize(this, resizedMat, Size(width.toDouble(), height.toDouble()))
    val image = BufferedImage(resizedMat.width(), resizedMat.height(), BufferedImage.TYPE_3BYTE_BGR)
    val data = (image.raster.dataBuffer as DataBufferByte).data
    resizedMat.get(0, 0, data)
    return image
  }

  private fun ByteArray.drawImage(image: BufferedImage) {
    // Draw at center
    for (y in 0 until image.height) {
      for (x in 0 until image.width) {
        val i: Int = (y * width + x) * 3

        // Flip Horizontally
        val color = Color(image.getRGB(width - 1 - x, y))
        //         val color = Color(image.getRGB(x, y));
        val r: Int = color.red
        val g: Int = color.green
        val b: Int = color.blue
        putPixel(i, r, g, b)
      }
    }
  }

  private fun ByteArray.putPixel(i: Int, r: Int, g: Int, b: Int) {
    this[i] = r.toByte()
    this[i + 1] = g.toByte()
    this[i + 2] = b.toByte()
  }

  override fun close() {
    close(fd)
  }

  companion object {
    private fun isArm(): Boolean {
      return System.getProperty("os.arch") == "arm"
    }
    private fun getFloat(): String {
      return if (isArm()) {
        if (File("/lib/arm-linux-gnueabihf").isDirectory) "hf" else "el"
      } else ""
    }
    private fun getArch(): String {
      val arch = System.getProperty("os.arch")
      val dataModel =
          System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode"))
      val bits: String
      return if (isArm()) {
        "arm" + getFloat()
      } else {
        bits =
            if ("32" == dataModel) {
              "32"
            } else if ("64" == dataModel) {
              "64"
            } else {
              if (arch.contains("64") || arch.equals("sparcv9", ignoreCase = true)) "64" else "32"
            }
        bits
      }
    }
    init {
      val arch = getArch()
      try {
        NativeUtils.loadLibraryFromJar("/META-INF/native/linux-$arch", arrayOf("fakecam"))
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }
}
