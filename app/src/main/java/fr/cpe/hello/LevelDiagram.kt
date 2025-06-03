package fr.cpe.hello

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.cpe.hello.ui.theme.Green200
import fr.cpe.hello.ui.theme.Green500
import fr.cpe.hello.ui.theme.GreenGradient
import fr.cpe.hello.ui.theme.LightColor
import kotlin.math.floor
import fr.cpe.hello.model.LevelState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import fr.cpe.hello.ui.theme.DarkGradient

@Composable
fun LevelScreen(state: LevelState, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier,
//            .fillMaxWidth(),
//            .padding(bottom = 20.dp),
//            verticalArrangement = Arrangement.SpaceBetween,

    ) {

        LevelIndicator(state = state, onClick = onClick)
        Text(
            text = "${state.unitName}\n${state.value}${state.unit} ",
            textAlign = TextAlign.Center,
//            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.width(150.dp)
        )


    }
}

@Composable
fun LevelIndicator(state: LevelState, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .width(145.dp)
            .aspectRatio(1f)
    ) {
        CircularLevelIndicator(state.arcValue, 240f)
    }
}


@Composable
private fun CircularLevelIndicator(value: Float, angle: Float){
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
//        drawLines(value, angle)
//        Color(0xFFFFFFFF)
        drawArcs(1f, angle, null, null, DarkGradient)
        drawArcs(value, angle, Green200, Green500, GreenGradient)

    }
}

fun DrawScope.drawArcs(progress: Float, maxValue: Float, blurColor: Color?, borderColor: Color?, gradient: Brush) {
    val startAngle = 270 - maxValue / 2
    val sweepAngle = maxValue * progress

    val topLeft = Offset(50f, 50f)
    val size = Size(size.width - 100f, size.height - 100f)

    fun drawBlur() {
        for (i in 0..20) {
            if (blurColor != null) {
                drawArc(
                    color = blurColor.copy(alpha = i / 900f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = size,
                    style = Stroke(width = 80f + (20 - i) * 20, cap = StrokeCap.Round)
                )
            }
        }
    }

    fun drawStroke() {
        if (borderColor != null) {
            drawArc(
                color = borderColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = 86f, cap = StrokeCap.Round)
            )
        }
    }

    fun drawGradient() {
        drawArc(
            brush = gradient,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(width = 80f, cap = StrokeCap.Round)
        )
    }

    drawBlur()
    drawStroke()
    drawGradient()
}

fun DrawScope.drawLines(progress: Float, maxValue: Float, numberOfLines: Int = 40) {
    val oneRotation = maxValue / numberOfLines
    val startValue = if (progress == 0f) 0 else floor(progress * numberOfLines).toInt() + 1

    for (i in startValue..numberOfLines) {
        rotate(i * oneRotation + (180 - maxValue) / 2) {
            drawLine(
                LightColor,
                Offset(if (i % 5 == 0) 80f else 0f, size.height / 2),
                Offset(0f, size.height / 2),
                8f,
                StrokeCap.Round
            )
        }
    }
}