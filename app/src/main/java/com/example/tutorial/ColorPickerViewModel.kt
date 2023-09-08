package com.example.tutorial

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.core.util.Consumer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

sealed class Event {
    data class HueSet(val value: Float) : Event()
    data class SatSet(val value: Float) : Event()
    data class LumSet(val value: Float) : Event()
    data class ColorPickerSet(val center: Offset, val size: Float, val touchPos: Offset) : Event()
}

data class UiState(
    val hexCode: String,
    val color: Color,
    val normalisedWheelOffset: Offset,
    val hue: Float,
    val sat: Float,
    val lum: Float,
)

class ColorPickerViewModel : ViewModel(), Consumer<Event> {

    private val state = MutableStateFlow(
        State(
            hue = 0.5f,
            sat = 0.5f,
            lum = 0.5f,
        )
    )
    val uiState = state.asStateFlow().map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value.toUiState())

    override fun accept(event: Event) {
        state.value = when (event) {
            is Event.ColorPickerSet -> handleColorPickerUpdate(state.value, event)
            is Event.HueSet -> state.value.copy(hue = event.value)
            is Event.LumSet -> state.value.copy(lum = event.value)
            is Event.SatSet -> state.value.copy(sat = event.value)
        }
    }

    private fun handleColorPickerUpdate(state: State, event: Event.ColorPickerSet): State {
        val relativeTouch = (event.touchPos - event.center)
        val distance = relativeTouch.getDistance() / (event.size / 2f)
        val angle = atan2(relativeTouch.y, relativeTouch.x)
        val hue = ((angle / PI).toFloat() / 2f).mod(1f)
        return state.copy(
            hue = hue,
            lum = 1 - distance,
        )
    }

    private fun State.toUiState(): UiState {
        val colorValue = android.graphics.Color.HSVToColor(floatArrayOf(hue * 360, sat, lum))
        return UiState(
            hue = hue,
            sat = sat,
            lum = lum,
            normalisedWheelOffset = this.toWheelOffset(),
            hexCode = "#${Integer.toHexString(colorValue)}",
            color = Color(colorValue)
        )
    }

    private fun State.toWheelOffset(): Offset =
        Offset(x = cos(hue * PI).toFloat(), y = sin(hue * PI).toFloat())


    private data class State(
        val hue: Float,
        val sat: Float,
        val lum: Float,
    )
}
