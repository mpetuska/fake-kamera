package dev.petuska.fake.kamera.util

import androidx.compose.runtime.*
import org.reduxkotlin.Store

@PublishedApi
internal val LocalStore: ProvidableCompositionLocal<Store<*>?> = compositionLocalOf { null }

@Composable
@PublishedApi
@Suppress("UNCHECKED_CAST")
internal inline fun <reified TState> getStore(): Store<TState> =
  LocalStore.current.runCatching { (this as Store<TState>) }.getOrElse {
    error("Store<${TState::class.simpleName}> not found in current composition scope")
  }

@Composable
inline operator fun <T> Store<T>.invoke(crossinline content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalStore provides this) { content() }
}

@Composable
fun rememberDispatcher() = getStore<Any>().dispatch

@Composable
inline fun <reified TState, TSlice> selectState(
  crossinline selector: @DisallowComposableCalls TState.() -> TSlice
): State<TSlice> {
  return getStore<TState>().selectState(selector)
}

@Composable
inline fun <TState, TSlice> Store<TState>.selectState(
  crossinline selector: @DisallowComposableCalls TState.() -> TSlice
): State<TSlice> {
  val result = remember { mutableStateOf(state.selector()) }
  DisposableEffect(result) {
    val unsubscribe = subscribe { result.value = state.selector() }
    onDispose(unsubscribe)
  }
  return result
}
