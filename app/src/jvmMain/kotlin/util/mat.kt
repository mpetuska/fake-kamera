@file:Suppress("TooManyFunctions")
package dev.petuska.fake.kamera.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toPainter
import org.jetbrains.kotlinx.multik.api.d2array
import org.jetbrains.kotlinx.multik.api.d3array
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.D3
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.operations.forEachMultiIndexed
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
  put(x, y, *value)
}

operator fun Mat.get(y: Int, x: Int, z: Int): Double = this[y, x][z]

operator fun Mat.set(y: Int, x: Int, z: Int, value: Double) {
//  val arr = this[x, y] ?: DoubleArray(channels())
//  arr[z] = value
  this[y, x][z] = value
}

fun Mat.clone(builder: (mat: Mat) -> Unit): Mat = clone().apply(builder)
fun Mat(builder: (mat: Mat) -> Unit): Mat = Mat().apply(builder)

fun Mat.toD3Array(): NDArray<Double, D3> {
  /*
  * i = x + WIDTH * (y + HEIGHT * z);
  * z = Math.round(i / (WIDTH * HEIGHT));
  * y = Math.round((i - z * WIDTH * HEIGHT) / WIDTH);
  * x = i - WIDTH * (y + HEIGHT * z);
  */
  val width = width()
  val height = height()
  val channels = channels()
  return mk.d3array(height, width, channels) { i ->
    val z: Int = i / (width * height)
    val y: Int = (i - z * width * height) / width
    val x: Int = i - width * (y + height * z)
    this[y, x, z]
  }
}

fun Mat.toD2Array(): NDArray<DoubleArray, D2> {
  val w = width()
  val h = height()
  return mk.d2array(h, w) { i ->
    val y = i / w
    val x = i % w
    this[y, x]
  }
}

@JvmName("toMatDoubleD3")
fun NDArray<Double, D3>.toMat(type: Int) = Mat.zeros(shape[0], shape[1], type).also { mat ->
  forEachMultiIndexed { (y, x, z), value ->
    mat[y, x, z] = value
  }
}

@JvmName("toMatDoubleD2")
fun NDArray<DoubleArray, D2>.toMat(type: Int) = Mat.zeros(shape, type).also { mat ->
  forEachMultiIndexed { (y, x), value ->
    mat[x, y] = value
  }
}
