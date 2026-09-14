package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.ui.theme.TwitterBackground
import com.example.ui.theme.TwitterBlue
import com.example.ui.theme.TwitterBorder
import com.example.ui.theme.TwitterGreen
import com.example.ui.theme.TwitterTextPrimary
import com.example.ui.theme.TwitterTextSecondary
import com.example.ui.theme.TwitterWhite

data class PathPoint(val x: Float, val y: Float)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignaturePad(
    title: String,
    subtitle: String,
    onSignatureCaptured: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val paths = remember { mutableStateListOf<List<Offset>>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var hasSigned by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TwitterBackground)
            .border(1.dp, TwitterBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TwitterTextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TwitterTextSecondary
                )
            }
            if (hasSigned) {
                Text(
                    text = "✓ Signé",
                    style = MaterialTheme.typography.labelMedium,
                    color = TwitterGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TwitterWhite)
                .border(1.dp, TwitterBorder, RoundedCornerShape(8.dp))
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            currentPath = listOf(Offset(motionEvent.x, motionEvent.y))
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            currentPath = currentPath + Offset(motionEvent.x, motionEvent.y)
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            if (currentPath.isNotEmpty()) {
                                paths.add(currentPath)
                                currentPath = emptyList()
                            }
                            true
                        }
                        else -> false
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background subtle dashed signature guideline
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(20f, size.height - 30f),
                    end = Offset(size.width - 20f, size.height - 30f),
                    strokeWidth = 2f
                )

                // Draw completed strokes
                for (stroke in paths) {
                    if (stroke.size > 1) {
                        val composePath = Path().apply {
                            moveTo(stroke[0].x, stroke[0].y)
                            for (i in 1 until stroke.size) {
                                lineTo(stroke[i].x, stroke[i].y)
                            }
                        }
                        drawPath(
                            path = composePath,
                            color = TwitterTextPrimary,
                            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                // Draw active stroke
                if (currentPath.size > 1) {
                    val activePath = Path().apply {
                        moveTo(currentPath[0].x, currentPath[0].y)
                        for (i in 1 until currentPath.size) {
                            lineTo(currentPath[i].x, currentPath[i].y)
                        }
                    }
                    drawPath(
                        path = activePath,
                        color = TwitterTextPrimary,
                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            if (paths.isEmpty() && currentPath.isEmpty()) {
                Text(
                    text = "Signez ici du doigt",
                    style = MaterialTheme.typography.bodySmall,
                    color = TwitterTextSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    paths.clear()
                    currentPath = emptyList()
                    hasSigned = false
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TwitterTextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, TwitterBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("clear_signature_btn")
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = TwitterTextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Effacer", color = TwitterTextSecondary)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (paths.isNotEmpty()) {
                        val bitmap = createBitmapFromPaths(paths, 400, 160)
                        onSignatureCaptured(bitmap)
                        hasSigned = true
                    }
                },
                enabled = paths.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = TwitterBlue),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("validate_signature_btn")
            ) {
                Icon(Icons.Default.Done, contentDescription = "Valider", tint = TwitterWhite, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Valider la signature", color = TwitterWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun createBitmapFromPaths(paths: List<List<Offset>>, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(15, 23, 42)
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    for (stroke in paths) {
        if (stroke.size > 1) {
            val composePath = Path().apply {
                moveTo(stroke[0].x, stroke[0].y)
                for (i in 1 until stroke.size) {
                    lineTo(stroke[i].x, stroke[i].y)
                }
            }
            canvas.drawPath(composePath.asAndroidPath(), paint)
        }
    }
    return bitmap
}
