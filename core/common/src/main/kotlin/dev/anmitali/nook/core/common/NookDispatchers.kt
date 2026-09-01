package dev.anmitali.nook.core.common

import kotlinx.coroutines.CoroutineDispatcher

data class NookDispatchers(
    val main: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val io: CoroutineDispatcher,
)
