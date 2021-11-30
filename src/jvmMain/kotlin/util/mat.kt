package dev.petuska.fake.kamera.util

import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import kotlin.math.ceil
import kotlin.math.floor
import org.opencv.core.Mat

fun Mat.to2dBytes(): ByteArray {
  val width = width()
  val height = height()
  val channels = channels()

  val yuv =
      Array(height) { y ->
        Array(width) { x -> ByteArray(channels) { c -> this[y, x][c].toInt().toByte() } }
      }
  val buffer =
      Array(height) { y ->
        ByteArray(2 * width).apply {
          for (x in 0 until ceil(size / 2.0).toInt()) {
            this[x * 2] = yuv[y][x][0]
          }
          for (x in 0 until ceil(size / 4.0).toInt()) {
            this[x * 4 + 1] = yuv[y][x * 2][1]
          }
          for (x in 0 until floor(size / 4.0).toInt()) {
            this[4 * x + 3] = yuv[y][2 * x][2]
          }
        }
      }
  return buffer.flatMap { it.toList() }.toByteArray()
}

fun Mat.toBufferedImage(): BufferedImage {
  val image = BufferedImage(width(), height(), BufferedImage.TYPE_3BYTE_BGR)
  val data = (image.raster.dataBuffer as DataBufferByte).data
  get(0, 0, data)
  return image
}
