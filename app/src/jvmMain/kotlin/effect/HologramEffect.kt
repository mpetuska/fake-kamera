package dev.petuska.fake.kamera.effect

import dev.petuska.fake.kamera.util.*
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

    val segmentLogits = Mat.zeros(frame.size(), frame.type()).apply {}
  }

  override fun invoke(frame: Mat): Mat {
    val holo = frame.clone()
      .dilate()
      .blur()
      .tint()
      .halftone()
      .ghosting()
    return Mat { Core.addWeighted(frame, 0.5, holo, 0.6, 0.0, it) }
  }

  private fun Mat.tint() = also { mat ->
    Imgproc.applyColorMap(this, mat, Imgproc.COLORMAP_WINTER)
  }

  private fun Mat.halftone() = also { mat ->
    val bandLength = 2
    val bandGap = 3
    val h = height()
    val w = width()
    for (y in 0 until h) {
      if (y % (bandLength + bandGap) < bandLength) {
        // holo[y, :, :] = holo[y, :, :] * np.random.uniform(0.1, 0.3)
        val darken = Random.nextDouble(0.1, 0.5)
        for (x in 0 until w) {
          mat[y, x] = mat[y, x].map { it * darken }.toDoubleArray()
        }
      }
    }
  }

  private fun Mat.ghosting() = also { mat ->
    Core.addWeighted(this, 0.2, clone().shift(5, 5), 0.8, 0.0, mat)
    Core.addWeighted(mat, 0.4, clone().shift(-5, -5), 0.6, 0.0, mat)
  }

  private fun Mat.shift(dx: Int, dy: Int): Mat = also { mat ->
    val warp: Mat = Mat.zeros(2, 3, CvType.CV_64FC1)
    warp.put(0, 0, 1.0)
    warp.put(1, 1, 1.0)
    warp.put(0, 2, dx.toDouble())
    warp.put(1, 2, dy.toDouble())
    Imgproc.warpAffine(mat, mat, warp, mat.size())
  }

  private fun Mat.blur() = clone { mat ->
    Imgproc.blur(this, mat, Size(30.0, 30.0))
  }

  private fun Mat.dilate() = clone { mat ->
    val dKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(10.0, 10.0))
    Imgproc.dilate(this, mat, dKernel, Point(-1.0, -1.0), 1)
  }
}
