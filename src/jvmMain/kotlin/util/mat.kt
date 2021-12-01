package dev.petuska.fake.kamera.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toPainter
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import kotlin.math.ceil
import kotlin.math.floor
import org.opencv.core.Mat

fun Mat.to2dBytes(): ByteArray {
  //  val buffer =
  //      Array(height()) { y ->
  //        ByteArray(width() * 2).also { ba ->
  //          for (x in 0 until ceil(ba.size / 2.0).toInt()) {
  //            ba[x * 2] = this[x, y, 0].toInt().toByte()
  //          }
  //          for (x in 0 until ceil(ba.size / 4.0).toInt()) {
  //            ba[x * 4 + 1] = this[x * 2, y, 1].toInt().toByte()
  //          }
  //          for (x in 0 until floor(ba.size / 4.0).toInt()) {
  //            ba[x * 4 + 3] = this[x * 2, y, 2].toInt().toByte()
  //          }
  //        }
  //      }
  //  return buffer.flatMap { it.toList() }.toByteArray()

  val barr = ByteArray(height() * width() * 2)
  for (y in 0 until height()) {
    for (x in 0 until ceil(width() * 2 / 2.0).toInt()) {
      barr[(y * width() * 2) + x * 2] = this[x, y, 0].toInt().toByte()
    }
    for (x in 0 until ceil(width() * 2 / 4.0).toInt()) {
      barr[(y * width() * 2) + x * 4 + 1] = this[x * 2, y, 1].toInt().toByte()
    }
    for (x in 0 until floor(width() * 2 / 4.0).toInt()) {
      barr[(y * width() * 2) + x * 4 + 3] = this[x * 2, y, 2].toInt().toByte()
    }
  }
  return barr
}

fun Mat.toBufferedImage(): BufferedImage {
  val image = BufferedImage(width(), height(), BufferedImage.TYPE_3BYTE_BGR)
  val data = (image.raster.dataBuffer as DataBufferByte).data
  get(0, 0, data)
  return image
}

fun Mat.toImageBitmap(): ImageBitmap = toBufferedImage().toComposeImageBitmap()

fun Mat.toPainter(): Painter = toBufferedImage().toPainter()

operator fun Mat.get(x: Int, y: Int, c: Int) = this[y, x][c]

operator fun Mat.set(x: Int, y: Int, c: Int, value: Double) {
  this[y, x][c] = value
}
