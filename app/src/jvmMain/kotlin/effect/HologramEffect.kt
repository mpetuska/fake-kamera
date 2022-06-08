package dev.petuska.fake.kamera.effect

import dev.petuska.fake.kamera.util.*
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.opencv.core.*
import org.opencv.dnn.Dnn
import org.opencv.imgproc.Imgproc
import kotlin.random.Random

@Suppress("UnusedPrivateMember")
object HologramEffect : FrameEffect {
  private val cvNet by lazy { Dnn.readNetFromTensorflow("frozen_inference_graph.pb", "graph.pbtxt") }

  private fun getMask(frame: Mat) = run {
    val ratio = 640 / frame.height()
    val scaleFactor = (1.0 / 255) * ratio
    val size = Size(277.0, 277.0)
    val swapRB = true
    val crop = true
    val mean = Scalar(1.0, 1.0, 1.0)
    val blob = Dnn.blobFromImage(frame, scaleFactor, size, mean, swapRB, crop)
    cvNet.setInput(blob)
    val results = cvNet.forward().get(0, 0)

    val segmentLogits = Mat.zeros(frame.size(), frame.type()).apply {
    }
  }

  override fun invoke(frame: Mat): Mat {
    val holo = frame
//      .dilate()
//      .blur()
//      .tint()
      .halftone()
//      .ghosting()
//    return Mat { Core.addWeighted(frame, 0.5, holo, 0.6, 0.0, it) }
    return holo
  }

  private fun Mat.tint() = clone { mat ->
    Imgproc.cvtColor(this, mat, Imgproc.COLORMAP_WINTER)
  }

  private fun Mat.halftone() = clone { mat ->
    val bandLength = 4
    val bandGap = 6
    val h = mat.height()
    val w = mat.width()
    val c = mat.channels()
    for (y in 0 until h) {
      if (y % (bandLength + bandGap) < bandLength) {
        // holo[y, :, :] = holo[y, :, :] * np.random.uniform(0.1, 0.3)
        val darken = Random.nextDouble(0.1, 0.3)
        for (x in 0 until w) {
          for (z in 0 until c) {
            mat[y, x, z] *= darken
          }
        }
      }
    }
  }

  private fun Mat.ghosting() = clone { mat ->
    Core.addWeighted(this, 0.2, shift(5, 5), 0.8, 0.0, mat)
    Core.addWeighted(mat, 0.4, shift(-5, -5), 0.6, 0.0, mat)
  }

  private fun Mat.shift(dx: Int, dy: Int): Mat = toD2Array().also { mat ->
    // shift_img from: https://stackoverflow.com/a/53140617
    //    img = np.roll(img, dy, axis=0)
    //    img = np.roll(img, dx, axis=1)
    val w = width()
    val h = height()
    val zero = DoubleArray(channels()) { 0.0 }
    if (dy > 0) {
      // img[:dy, :] = 0
      for (y in 0 until dy) {
        for (x in 0 until w) {
          mat[y, x] = zero
        }
      }
    } else if (dy < 0) {
      // img[dy:, :] = 0
      for (y in (h - dy) until h) {
        for (x in 0 until w) {
          mat[y, x] = zero
        }
      }
    }
    if (dx > 0) {
      // img[:, :dx] = 0
      for (y in 0 until h) {
        for (x in 0 until dx) {
          mat[y, x] = zero
        }
      }
    } else if (dx < 0) {
      // img[:, dx:] = 0
      for (y in 0 until h) {
        for (x in (w - dx) until w) {
          mat[y, x] = zero
        }
      }
    }
  }.toMat(type())

  private fun Mat.blur() = clone { mat ->
    Imgproc.blur(this, mat, Size(30.0, 30.0))
  }

  private fun Mat.dilate() = clone { mat ->
    val dKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(10.0, 10.0))
    Imgproc.dilate(this, mat, dKernel, Point(-1.0, -1.0), 1)
  }
}
