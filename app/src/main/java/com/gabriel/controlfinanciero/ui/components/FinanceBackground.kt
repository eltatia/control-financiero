package com.gabriel.controlfinanciero.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import com.gabriel.controlfinanciero.R

@Composable
fun FinanceBackground(
    modifier: Modifier = Modifier,
    showIllustration: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "finance-bg")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "finance-bg-shift"
    )

    val backgroundImages = listOf(
        R.drawable.bg_finance_wave_1,
        R.drawable.bg_finance_wave_2,
        R.drawable.bg_finance_wave_3
    )
    val selectedImage = remember { backgroundImages.random() }

    val gradient = Brush.linearGradient(
        colors = listOf(
            colorScheme.primaryContainer.copy(alpha = 0.6f),
            colorScheme.secondaryContainer.copy(alpha = 0.35f),
            colorScheme.background
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 200f * shift),
        end = androidx.compose.ui.geometry.Offset(800f, 900f)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        if (showIllustration) {
            Image(
                painter = painterResource(selectedImage),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.18f),
                contentScale = ContentScale.Crop
            )
        }
    }
}
