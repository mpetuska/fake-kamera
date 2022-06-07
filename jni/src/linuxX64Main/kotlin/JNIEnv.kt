@file:Suppress("FunctionName")

import jni.*
import kotlinx.cinterop.*

fun JNIEnvVar.GetArrayLength(array: jarray?) =
  pointed?.GetArrayLength?.invoke(ptr, array)
fun JNIEnvVar.GetByteArrayElements(array: jbyteArray?, p3: CPointer<jbooleanVar>? = null) =
  pointed?.GetByteArrayElements?.invoke(ptr, array, p3)
fun JNIEnvVar.ReleaseByteArrayElements(array: jbyteArray?, elements: CPointer<jbyteVar>?, p4: jint) =
  pointed?.ReleaseByteArrayElements?.invoke(ptr, array, elements, p4)
fun JNIEnvVar.GetStringUTFChars(string: jstring?, p3: CPointer<jbooleanVar>?) = pointed?.GetStringUTFChars?.invoke(ptr, string, p3)
fun JNIEnvVar.ReleaseStringUTFChars(string: jstring?, p3: CPointer<ByteVar>?) = pointed?.ReleaseStringUTFChars?.invoke(ptr, string, p3)
// fun JNIEnvVar.GetArrayLength(frame: jarray?) = pointed?.GetArrayLength?.invoke(ptr, frame)
