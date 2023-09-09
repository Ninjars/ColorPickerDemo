package com.example.tutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.ViewModelProvider
import com.example.tutorial.ui.theme.TutorialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelProvider = ViewModelProvider(this)
        val viewModel = viewModelProvider[ColorPickerViewModel::class.java]

        setContent {
            val state = viewModel.uiState.collectAsState()
            TutorialTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ColorCircle(
                            sat = state.value.sat,
                            markerPosition = state.value.normalisedWheelOffset,
                            eventHandler = { event -> viewModel.accept(event) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                        Slider(
                            value = state.value.hue,
                            onValueChange = { value -> viewModel.accept(Event.HueSet(value)) })
                        Slider(
                            value = state.value.sat,
                            onValueChange = { value -> viewModel.accept(Event.SatSet(value)) })
                        Slider(
                            value = state.value.lum,
                            onValueChange = { value -> viewModel.accept(Event.LumSet(value)) })

                        Readout(
                            color = state.value.color,
                            text = state.value.hexCode,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Readout(
    color: Color,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(IntrinsicSize.Min),
    ) {
        Box(
            modifier = Modifier
                .size(100.dp, 50.dp)
                .background(color)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(16.dp),
                )
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun ColorCircle(
    sat: Float,
    markerPosition: Offset,
    eventHandler: (Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                val floatSize = size.toSize()
                detectDragGestures(
                    onDragStart = { startOffset ->
                        eventHandler(
                            Event.ColorPickerSet(
                                floatSize.center,
                                floatSize.maxDimension,
                                startOffset
                            )
                        )
                    },
                    onDrag = { change, _ ->
                        eventHandler(
                            Event.ColorPickerSet(
                                floatSize.center,
                                floatSize.maxDimension,
                                change.position
                            )
                        )
                    },
                )
            }
    ) {
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Green,
                    Color.Cyan,
                    Color.Blue,
                    Color.Magenta,
                    Color.Red
                )
            ),
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(sat) })
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.Transparent, Color.Black)
            ),
        )
        val markerCenter = size.center + markerPosition.times(size.maxDimension / 2f)
        drawCircle(
            color = Color.Black,
            radius = 24f,
            center = markerCenter,
        )
        drawCircle(
            color = Color.White,
            radius = 18f,
            center = markerCenter,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TutorialTheme {
    }
}