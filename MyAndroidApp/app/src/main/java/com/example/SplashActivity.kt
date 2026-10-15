package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

class SplashActivity : ComponentActivity() {

    private val isNavigated = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make full screen by hiding status bar and navigation bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            SplashScreenContent(
                onVideoFinished = { navigateToMainActivity() }
            )
        }
    }

    private fun navigateToMainActivity() {
        if (!isNavigated.compareAndSet(false, true)) return

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish() // Removes SplashActivity from back stack

        // Smooth fade in / fade out transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.fade_in, R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}

@Composable
fun SplashScreenContent(
    onVideoFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val splashBgColor = remember { Color(0xFFF8F7F4) }
    val darkTeal = remember { Color(0xFF0F5F53) }

    // Failsafe auto-advance after 2.5 seconds in case video hardware decode stalls
    LaunchedEffect(Unit) {
        delay(2500L)
        onVideoFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(splashBgColor),
        contentAlignment = Alignment.Center
    ) {
        // Fallback/Underlay SpendSense brand badge so screen is never blank
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(pulseScale)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(darkTeal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₹",
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SpendSense",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = darkTeal
            )
        }

        // Fullscreen video overlay
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    val videoUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.intro_video}")
                    setVideoURI(videoUri)

                    // Auto-play muted as requested
                    setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.setVolume(0f, 0f)
                        start()
                    }

                    // Transition exactly when video finishes
                    setOnCompletionListener {
                        onVideoFinished()
                    }

                    // Fail-safe: if decoding fails, navigate immediately
                    setOnErrorListener { _, _, _ ->
                        onVideoFinished()
                        true
                    }
                }
            }
        )
    }
}
