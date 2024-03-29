@file:Suppress("TooManyFunctions")

package dev.petuska.fake.kamera.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toPainter
import org.jetbrains.kotlinx.multik.api.d3arrayIndices
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.data.D3
import org.jetbrains.kotlinx.multik.ndarray.data.D3Array
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.toDoubleArray
import org.opencv.core.Mat
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import kotlin.math.ceil
import kotlin.math.floor

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
      barr[(y * width() * 2) + x * 2] = this[y, x, 0].toInt().toByte()
    }
    for (x in 0 until ceil(width() * 2 / 4.0).toInt()) {
      barr[(y * width() * 2) + x * 4 + 1] = this[y, x * 2, 1].toInt().toByte()
    }
    for (x in 0 until floor(width() * 2 / 4.0).toInt()) {
      barr[(y * width() * 2) + x * 4 + 3] = this[y, x * 2, 2].toInt().toByte()
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

operator fun Mat.set(y: Int, x: Int, value: DoubleArray) {
  @Suppress("SpreadOperator")
  put(y, x, *value)
}

operator fun Mat.get(y: Int, x: Int, z: Int): Double = this[y, x][z]

fun Mat.clone(builder: (mat: Mat) -> Unit): Mat = clone().apply(builder)
fun Mat(builder: (mat: Mat) -> Unit): Mat = Mat().apply(builder)
fun Mat.print(name: String = "mat") = println("[$name] size=${size()} $this")

fun Mat.toD3Array(): NDArray<Double, D3> {
  /*
  * i = x + WIDTH * (y + HEIGHT * z)
  * z = i / (WIDTH * HEIGHT)
  * y = (i - z * WIDTH * HEIGHT) / WIDTH;
  * x = i - WIDTH * (y + HEIGHT * z);
  */
  val height = height()
  val width = width()
  val channels = channels()
  return mk.d3arrayIndices(height, width, channels) { y, x, z ->
    this[y, x, z]
  }
}

fun D3Array<Double>.toMat(type: Int) = Mat.ones(shape[0], shape[1], type).also { mat ->
  for (y in 0 until shape[0]) {
    for (x in 0 until shape[1]) {
      mat[y, x] = this[y, x].toDoubleArray()
    }
  }
}
