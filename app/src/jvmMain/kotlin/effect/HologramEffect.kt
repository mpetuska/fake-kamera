package dev.petuska.fake.kamera.effect

import dev.petuska.fake.kamera.util.*
import org.opencv.core.*
import org.opencv.core.CvType.CV_32F
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.random.Random

@Suppress("UnusedPrivateMember")
object HologramEffect : FrameEffect {
  private val cvNet: Net by lazy {
    val file = File.createTempFile("fake-kamera", "bodypix_graph.pb")
    file.deleteOnExit()
    HologramEffect::class.java.getResourceAsStream("/bodypix_graph.pb")?.use { ins ->
      file.outputStream().use { os ->
        ins.copyTo(os)
      }
    }
    Dnn.readNetFromTensorflow(file.absolutePath).apply {
      setPreferableBackend(Dnn.DNN_BACKEND_CUDA)
      setPreferableTarget(Dnn.DNN_TARGET_CUDA)
    }
  }

  private fun getMask(frame: Mat): Mat = run {
    val ratio = 640.0 / frame.height()
    val scaleFactor = (1.0 / 255) * ratio
    val size = Size(227.0, 227.0)
//    val size = frame.size()
    val swapRB = true
    val crop = true
    val mean = Scalar(1.0, 1.0, 1.0)
    val blob = Dnn.blobFromImage(frame, scaleFactor, size, mean, swapRB, crop)
    cvNet.setInput(blob)
    // the net's output is a list of [id,conf,...pixels]
    val detections = cvNet.forward()
    val segmentLogits = detections.reshape(1, detections.size(2))
    frame.print("frame")
    detections.print("detections")
    segmentLogits.print("segmentLogits")
    val scaler = BodypixScaler(height = frame.height(), width = frame.width(), type = segmentLogits.type())
    val scaledSegmentScores = scaler.scaleAndCropToInputTensorShape(segmentLogits, false)
    val mask = Mat { Imgproc.threshold(scaledSegmentScores, it, 0.8, 1.0, Imgproc.THRESH_BINARY) }
    mask.dilate().blur()
  }

  override fun invoke(frame: Mat): Mat {
    println("=========================================")
//    val background = frame.clone()//Mat { GaussianBlur(frame, it, Size(61.0, 61.0), 20.0, 20.0) }
//    val holo = frame.clone()
//      .tint()
//      .halftone()
//      .ghosting()
//    val effect = Mat { Core.addWeighted(frame, 0.5, holo, 0.6, 0.0, it) }
//    val ones = Mat.ones(frame.rows(), frame.cols(), mask.type())
//    val invMask = Mat { Core.subtract(ones, mask, it) }
//    val invMaskMult = Mat { Core.multiply(background, invMask, it, 1.0, CV_32F) }

    val mask = getMask(frame)
    mask.print("mask")
    Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2BGR)
    val maskMult = Mat { Core.multiply(frame, mask, it, 1.0, CV_32F) }

    return mask // Mat { Core.add(maskMult, invMaskMult, it) }
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
    val d = 5
    Core.addWeighted(this, 0.2, clone().shift(d, d), 0.8, 0.0, mat)
    Core.addWeighted(mat, 0.4, clone().shift(-d, -d), 0.6, 0.0, mat)
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
