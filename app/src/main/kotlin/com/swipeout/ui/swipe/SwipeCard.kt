package com.swipeout.ui.swipe

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.swipeout.data.db.entity.ImageEntity
import com.swipeout.ui.common.TextureVideoPlayer
import com.swipeout.ui.common.VideoThumbnail
import com.swipeout.ui.strings.LocalStrings
import com.swipeout.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SwipeCard(
    image: ImageEntity,
    stackPosition: Int,     // 0 = front (active), 1 = next, 2 = back
    onSwiped: (String) -> Unit,
    onWillSwipe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density        = LocalDensity.current
    val screenWidthPx  = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val swipeThresholdX  = screenWidthPx * 0.28f
    val swipeThresholdY  = screenHeightPx * 0.18f  // Upward swipe = BOOKMARK
    val velocityThreshold = 700f

    // All drag state keyed to image.id — fresh for every card that reaches this slot
    var offsetX       by remember(image.id) { mutableFloatStateOf(0f) }
    var offsetY       by remember(image.id) { mutableFloatStateOf(0f) }
    var isFlying      by remember(image.id) { mutableStateOf(false) }
    // Incremented after each snap-back to reconnect the TextureView to ExoPlayer
    var snapBackKey   by remember(image.id) { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    // ── Animated stack visuals ───────────────────────────────────────────────
    val targetScale   = when (stackPosition) { 0 -> 1.00f; 1 -> 0.93f; else -> 0.87f }
    val targetOffsetY = when (stackPosition) { 0 -> 0f;    1 -> -14f;  else -> -28f  }
    val targetAlpha   = when (stackPosition) { 0 -> 1.00f; 1 -> 0.80f; else -> 0.55f }
    val stackSpec     = spring<Float>(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
    val animScale     by animateFloatAsState(targetScale,   animationSpec = stackSpec, label = "scale")
    val animOffsetY   by animateFloatAsState(targetOffsetY, animationSpec = stackSpec, label = "offsetY")
    val animAlpha     by animateFloatAsState(targetAlpha,   animationSpec = stackSpec, label = "alpha")

    val strings = LocalStrings.current

    // Overlay selection: the dominant axis wins. Horizontal → KEEP/DELETE;
    // upward vertical → BOOKMARK. Downward drag has no meaning so we clamp it
    // visually further down.
    val horizontalDominant = abs(offsetX) >= abs(offsetY)
    val verticalUpDominant = !horizontalDominant && offsetY < 0f
    val horizontalOverlayAlpha =
        if (horizontalDominant) (abs(offsetX) / swipeThresholdX).coerceIn(0f, 1f) else 0f
    val bookmarkOverlayAlpha =
        if (verticalUpDominant) (abs(offsetY) / swipeThresholdY).coerceIn(0f, 1f) else 0f
    val rotation     = (offsetX / screenWidthPx) * 16f
    val swipingRight = offsetX > 0f

    // ── Gesture — front card only ─────────────────────────────────────────────
    val gestureModifier = if (stackPosition == 0 && !isFlying) {
        Modifier.pointerInput(image.id) {
            val tracker = VelocityTracker()
            detectDragGestures(
                onDragStart = { tracker.resetTracking() },
                onDrag = { change, drag ->
                    change.consume()
                    offsetX += drag.x
                    // Track Y at 1:1 so upward drag can hit the bookmark threshold, but
                    // clamp positive (downward) values to zero — downward has no action.
                    offsetY = (offsetY + drag.y).coerceAtMost(0f)
                    tracker.addPosition(change.uptimeMillis, change.position)
                },
                onDragEnd = {
                    val vel = tracker.calculateVelocity()
                    tracker.resetTracking()

                    // BOOKMARK wins when the upward vertical motion is both beyond its
                    // threshold AND stronger than any horizontal movement. This keeps
                    // diagonal flicks from getting misread.
                    val bookmarkByDisplacement =
                        offsetY <= -swipeThresholdY && abs(offsetY) > abs(offsetX)
                    val bookmarkByVelocity =
                        vel.y <= -velocityThreshold && abs(vel.y) > abs(vel.x)
                    val shouldBookmark = bookmarkByDisplacement || bookmarkByVelocity

                    val shouldSwipeHoriz = !shouldBookmark &&
                        (abs(offsetX) > swipeThresholdX || abs(vel.x) > velocityThreshold)

                    when {
                        shouldBookmark -> {
                            val targetY   = -screenHeightPx * 1.2f
                            val launchVel = if (abs(vel.y) > velocityThreshold) vel.y else -1800f
                            onWillSwipe()
                            isFlying = true
                            scope.launch {
                                animate(
                                    initialValue    = offsetY,
                                    targetValue     = targetY,
                                    initialVelocity = launchVel,
                                    animationSpec   = spring(
                                        stiffness    = Spring.StiffnessMedium,
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                    ),
                                ) { v, _ -> offsetY = v }
                                onSwiped(ImageEntity.BOOKMARK)
                            }
                        }
                        shouldSwipeHoriz -> {
                            // Position decides direction when card moved ≥30px;
                            // velocity decides for quick flicks with minimal displacement.
                            val isRight: Boolean = when {
                                abs(offsetX) >= 30f -> offsetX > 0f
                                else                -> vel.x > 0f
                            }
                            val decision  = if (isRight) ImageEntity.KEEP else ImageEntity.DELETE
                            val targetX   = if (isRight) screenWidthPx * 1.6f else -screenWidthPx * 1.6f
                            val launchVel = when {
                                abs(vel.x) > velocityThreshold -> vel.x
                                isRight  -> maxOf(vel.x, 1500f)
                                else     -> minOf(vel.x, -1500f)
                            }
                            onWillSwipe()
                            isFlying = true
                            scope.launch {
                                animate(
                                    initialValue    = offsetX,
                                    targetValue     = targetX,
                                    initialVelocity = launchVel,
                                    animationSpec   = spring(
                                        stiffness    = Spring.StiffnessMedium,
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                    ),
                                ) { v, _ -> offsetX = v }
                                onSwiped(decision)
                            }
                        }
                        else -> {
                            scope.launch {
                                val j1 = launch {
                                    animate(offsetX, 0f, animationSpec = spring(
                                        stiffness    = Spring.StiffnessMedium,
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                    )) { v, _ -> offsetX = v }
                                }
                                val j2 = launch {
                                    animate(offsetY, 0f, animationSpec = spring(
                                        stiffness    = Spring.StiffnessMedium,
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                    )) { v, _ -> offsetY = v }
                                }
                                j1.join(); j2.join()
                                // Reconnect TextureView to ExoPlayer after snap-back to unfreeze video
                                if (image.isVideo) snapBackKey++
                            }
                        }
                    }
                },
                onDragCancel = {
                    tracker.resetTracking()
                    scope.launch {
                        val j1 = launch { animate(offsetX, 0f) { v, _ -> offsetX = v } }
                        val j2 = launch { animate(offsetY, 0f) { v, _ -> offsetY = v } }
                        j1.join(); j2.join()
                        if (image.isVideo) snapBackKey++
                    }
                }
            )
        }
    } else Modifier

    // ── Card box ──────────────────────────────────────────────────────────────
    // Single graphicsLayer (clip merged in) avoids double hardware-layer caching
    // that freezes TextureView frames during drag/snap-back animations.
    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = if (stackPosition == 0) offsetX else 0f
                translationY = if (stackPosition == 0) offsetY
                               else with(density) { animOffsetY.dp.toPx() }
                rotationZ    = if (stackPosition == 0) rotation else 0f
                scaleX       = animScale
                scaleY       = animScale
                alpha        = animAlpha
                clip         = true
                shape        = RoundedCornerShape(20.dp)
            }
            .background(Surface)
            .then(gestureModifier),
    ) {
        // ── Media content ─────────────────────────────────────────────────────
        if (image.isVideo) {
            if (stackPosition == 0) {
                // Front card: full TextureView-based player
                TextureVideoPlayer(
                    uri          = Uri.parse(image.contentUri),
                    snapBackKey  = snapBackKey,
                    modifier     = Modifier.fillMaxSize(),
                )
            } else {
                // Back/next cards: static thumbnail — no ExoPlayer allocated
                VideoThumbnail(
                    uri      = Uri.parse(image.contentUri),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            AsyncImage(
                model              = ImageRequest.Builder(LocalContext.current).data(image.contentUri).build(),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
        }

        // ── Decision overlays (front card only) ───────────────────────────────
        if (stackPosition == 0) {
            if (horizontalOverlayAlpha > 0.03f) {
                val decisionColor = if (swipingRight) Keep else Delete
                Box(modifier = Modifier.fillMaxSize().background(decisionColor.copy(alpha = horizontalOverlayAlpha * 0.25f)))
                Box(
                    modifier = Modifier
                        .align(if (swipingRight) Alignment.TopStart else Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(decisionColor.copy(alpha = (horizontalOverlayAlpha * 1.2f).coerceAtMost(1f)))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text          = if (swipingRight) strings.keep.uppercase() else strings.delete.uppercase(),
                        color         = Color.White,
                        fontSize      = 15.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                }
            } else if (bookmarkOverlayAlpha > 0.03f) {
                Box(modifier = Modifier.fillMaxSize().background(Bookmark.copy(alpha = bookmarkOverlayAlpha * 0.25f)))
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Bookmark.copy(alpha = (bookmarkOverlayAlpha * 1.2f).coerceAtMost(1f)))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text          = "🔖 ${strings.bookmark.uppercase()}",
                        color         = Color.White,
                        fontSize      = 15.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                }
            }
        }

        // ── Video badge ───────────────────────────────────────────────────────
        if (image.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(strings.videoLabel, color = Color.White, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}
