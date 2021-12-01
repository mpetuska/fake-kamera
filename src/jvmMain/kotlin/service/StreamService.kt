package dev.petuska.fake.kamera.service

import dev.petuska.fake.kamera.effect.FrameEffect
import dev.petuska.fake.kamera.util.VideoDeviceInput
import dev.petuska.fake.kamera.util.VideoDeviceOutput
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import org.opencv.core.Mat

class StreamService(
    private val input: VideoDeviceInput,
    private val output: VideoDeviceOutput,
    fps: UInt
) : AutoCloseable {
  private val frameDelay = 1_000L / fps.toInt()

  suspend fun stream(vararg effects: FrameEffect) = stream(effects.toSet())
  
  suspend fun stream(effects: Set<FrameEffect>) {
    input.open()
    output.open()
    input
        .stream()
        .filterNot(Mat::empty)
        .buffer(1)
        .let { effects.fold(it) { flow, effect -> flow.map(effect::invoke) } }
        .collect {
          delay(frameDelay)
          output.write(it)
        }
  }

  override fun close() {
    input.close()
    output.close()
  }
}
