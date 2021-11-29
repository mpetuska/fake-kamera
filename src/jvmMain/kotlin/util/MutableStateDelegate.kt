package dev.petuska.fake.kamera.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class MutableStateDelegate {}

@Composable fun <T> rememberMutableStateOf(initial: () -> T) = remember { mutableStateOf(initial()) }
