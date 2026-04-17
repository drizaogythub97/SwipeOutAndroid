package com.swipeout.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swipeout.ui.strings.LocalStrings
import com.swipeout.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    vm: OnboardingViewModel = hiltViewModel(),
) {
    val strings    = LocalStrings.current
    val pagerState = rememberPagerState { 3 }
    val scope      = rememberCoroutineScope()

    // Permission launcher — fired immediately on first composition.
    // API 34+ includes READ_MEDIA_VISUAL_USER_SELECTED so the system offers the
    // "selected items" option on the permission sheet.
    val permissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        else ->
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) vm.onPermissionGranted()
    }

    // Ask for permission as soon as onboarding opens — sync starts in background
    LaunchedEffect(Unit) { permLauncher.launch(permissions) }

    val pages = listOf(
        OnboardingPage(emoji = "📷", title = strings.onboardingPage1Title, body = strings.onboardingPage1Body, accentColor = Accent),
        OnboardingPage(emoji = "👆", title = strings.onboardingPage2Title, body = strings.onboardingPage2Body, accentColor = Keep),
        OnboardingPage(emoji = "✨", title = strings.onboardingPage3Title, body = strings.onboardingPage3Body, accentColor = Bookmark),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
    ) {
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex], pageIndex = pageIndex)
        }

        // Dot indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) { i ->
                val selected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .size(if (selected) 20.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (selected) pages[pagerState.currentPage].accentColor
                            else TextMuted.copy(alpha = 0.4f)
                        )
                )
            }
        }

        // CTA button
        Button(
            onClick = {
                if (pagerState.currentPage < 2) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    vm.completeOnboarding()
                    onDone()
                }
            },
            colors   = ButtonDefaults.buttonColors(containerColor = pages[pagerState.currentPage].accentColor),
            shape    = RoundedCornerShape(14.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 32.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text       = if (pagerState.currentPage < 2) strings.next else strings.getStarted,
                color      = Color.White,
                fontSize   = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, pageIndex: Int) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier            = Modifier.offset(y = (-40).dp),
        ) {
            IllustrationArea(page = page, pageIndex = pageIndex)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text       = page.title,
                    color      = TextPrimary,
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                )
                Text(
                    text       = page.body,
                    color      = TextSecondary,
                    fontSize   = 16.sp,
                    textAlign  = TextAlign.Center,
                    lineHeight = 24.sp,
                )
            }
        }
    }
}

@Composable
private fun IllustrationArea(page: OnboardingPage, pageIndex: Int) {
    when (pageIndex) {
        0    -> Page0Illustration(accentColor = page.accentColor)
        1    -> Page1Illustration()
        else -> Page2Illustration(accentColor = page.accentColor)
    }
}

@Composable
private fun Page0Illustration(accentColor: Color) {
    val inf = rememberInfiniteTransition(label = "pulse")
    val scale by inf.animateFloat(
        initialValue  = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOut), RepeatMode.Reverse),
        label = "scale",
    )
    val glowAlpha by inf.animateFloat(
        initialValue  = 0.2f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOut), RepeatMode.Reverse),
        label = "glow",
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = glowAlpha))
        )
        Text("📷", fontSize = 72.sp, modifier = Modifier.scale(scale))
    }
}

@Composable
private fun Page1Illustration() {
    val inf = rememberInfiniteTransition(label = "swipe")
    val keepAlpha by inf.animateFloat(
        initialValue  = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0f at 0; 1f at 500; 1f at 1200; 0f at 1500; 0f at 3000
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "keepAlpha",
    )
    val deleteAlpha by inf.animateFloat(
        initialValue  = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0f at 1500; 1f at 2000; 1f at 2700; 0f at 3000
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "deleteAlpha",
    )
    val cardOffset by inf.animateFloat(
        initialValue  = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0f at 0; 60f at 1200; 0f at 1500; -60f at 2700; 0f at 3000
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "cardOffset",
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier.size(200.dp, 160.dp),
    ) {
        Box(
            modifier = Modifier
                .size(120.dp, 140.dp)
                .offset(x = cardOffset.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Surface),
            contentAlignment = Alignment.Center,
        ) {
            Text("🖼", fontSize = 48.sp)
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .alpha(keepAlpha)
                .clip(RoundedCornerShape(8.dp))
                .background(Keep.copy(alpha = 0.9f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text("✓ MANTER", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .alpha(deleteAlpha)
                .clip(RoundedCornerShape(8.dp))
                .background(Delete.copy(alpha = 0.9f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text("✕ DELETAR", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Page2Illustration(accentColor: Color) {
    val inf = rememberInfiniteTransition(label = "counter")
    val progress by inf.animateFloat(
        initialValue  = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOut), RepeatMode.Reverse),
        label = "progress",
    )
    val displayBytes = (progress * 4_294_967_296f).toLong()
    val displayText = when {
        displayBytes < 1_048_576     -> "${displayBytes / 1_024} KB"
        displayBytes < 1_073_741_824 -> "${"%.1f".format(displayBytes / 1_048_576f)} MB"
        else                         -> "${"%.2f".format(displayBytes / 1_073_741_824f)} GB"
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("✨", fontSize = 56.sp)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(
                    listOf(accentColor.copy(alpha = 0.15f), accentColor.copy(alpha = 0.3f))
                ))
                .padding(horizontal = 28.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(displayText, color = accentColor, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text("liberados", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val body: String,
    val accentColor: Color,
)
