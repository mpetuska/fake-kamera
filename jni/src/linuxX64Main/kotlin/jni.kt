@file:Suppress(
  "Unused",
  "LongParameterList",
  "FunctionParameterNaming",
  "UNUSED_PARAMETER",
)

import jni.JNIEnvVar
import jni.jboolean
import jni.jbyteArray
import jni.jint
import jni.jobject
import jni.jstring
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toByte
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.value
import platform.posix.O_RDWR
import platform.posix.close
import platform.posix.fprintf
import platform.posix.ioctl
import platform.posix.memset
import platform.posix.open
import platform.posix.stderr
import platform.posix.write
import videodev2.V4L2_BUF_TYPE_VIDEO_OUTPUT
import videodev2.V4L2_FIELD_NONE
import videodev2.V4L2_PIX_FMT_YUYV
import videodev2.VIDIOC_G_FMT
import videodev2.VIDIOC_S_FMT
import videodev2.v4l2_format

private fun printError(error: String) {
  fprintf(stderr, "%s\n", error)
}

@Suppress("ReturnCount")
@CName("Java_dev_petuska_fake_kamera_jni_FakeCam_open")
fun fakeCamOpen(
  env: JNIEnvVar,
  obj: jobject,
  device: jstring,
  width: jint,
  height: jint,
  channels: jint,
): jint {
  val out = memScoped {
    val zero: UByteVar = alloc {
      value = 0.convert()
    }
    env.GetStringUTFChars(device, zero.ptr)
  }
  val output = out?.toKStringFromUtf8()
  val devFd = open(output, O_RDWR)
  env.ReleaseStringUTFChars(device, out)
  if (devFd < 0) {
    printError("ERROR: could not open output device! $output")
    return -2
  }

  memScoped {
    val format = alloc<v4l2_format>()
    memset(format.ptr, 0, sizeOf<v4l2_format>().convert())
    format.type = V4L2_BUF_TYPE_VIDEO_OUTPUT
    if (ioctl(devFd, VIDIOC_G_FMT, format.ptr) < 0) {
      printError("ERROR: unable to get video format!")
      return -1
    }

    val frameSize = width * height * channels
    format.fmt.pix.width = width.convert()
    format.fmt.pix.height = height.convert()
    format.fmt.pix.pixelformat = V4L2_PIX_FMT_YUYV
    format.fmt.pix.sizeimage = frameSize.convert()
    format.fmt.pix.field = V4L2_FIELD_NONE.convert()

    if (ioctl(devFd, VIDIOC_S_FMT, format.ptr) < 0) {
      printError("ERROR: unable to set video format!")
      return -1
    }
  }
  return devFd
}

@CName("Java_dev_petuska_fake_kamera_jni_FakeCam_writeFrame")
fun fakeCamWriteFrame(
  env: JNIEnvVar,
  obj: jobject,
  dev_fd: jint,
  frame: jbyteArray,
  height: jint,
  channels: jint,
): jboolean {
  val frameSize = env.GetArrayLength(frame)!!
  val elements = env.GetByteArrayElements(frame)

  val written = write(dev_fd, elements, frameSize.convert())
  env.ReleaseByteArrayElements(frame, elements, 0)
  return if (written < 0) {
    printError("ERROR: could not write to output device!")
    close(dev_fd)
    false
  } else {
    written == frameSize.toLong()
  }.toByte().toUByte()
}

@CName("Java_dev_petuska_fake_kamera_jni_FakeCam_close")
fun fakeCamClose(
  env: JNIEnvVar,
  obj: jobject,
  dev_fd: jint,
): jint {
  return close(dev_fd)
}
