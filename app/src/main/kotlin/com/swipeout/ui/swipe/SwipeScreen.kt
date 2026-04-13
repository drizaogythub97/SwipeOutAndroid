package com.swipeout.ui.swipe

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.swipeout.data.db.entity.ImageEntity
import com.swipeout.ui.common.VideoThumbnail
import com.swipeout.ui.theme.*

@Composable
fun SwipeScreen(
    monthKey: String,
    onBack: () -> Unit,
    onReview: () -> Unit,
    vm: SwipeViewModel = hiltViewModel(),
) {
    val pending    by vm.pendingImages.collectAsStateWithLifecycle()   // null until Room loads
    val allDone    by vm.isAllReviewed.collectAsStateWithLifecycle()
    val totalCount by vm.totalCount.collectAsStateWithLifecycle()
    val lastSwipe  by vm.lastSwipe.collectAsStateWithLifecycle()
    val canUndo    by vm.canUndo.collectAsStateWithLifecycle()
    val context    = LocalContext.current
    val density    = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    // Navigate to ReviewScreen when all images are reviewed.
    // SwipeScreen is popped (NavGraph) so Back from Review goes to Home, not here.
    LaunchedEffect(allDone) {
        if (allDone) onReview()
    }

    // Prefetch next cards when top changes
    val topId = pending?.firstOrNull()?.id
    LaunchedEffect(topId) {
        pending?.drop(1)?.take(5)?.forEach { img ->
            ImageLoader(context).enqueue(
                ImageRequest.Builder(context).data(img.contentUri).build()
            )
        }
    }

    // Undo: tracks the card currently flying back (pure UI animation state)
    var returningCard by remember { mutableStateOf<LastSwipe?>(null) }

    fun onUndoTap() {
        val last = lastSwipe ?: return
        returningCard = last   // trigger animation overlay
        vm.undo()              // restore to PENDING in DB (canUndo becomes false immediately)
    }

    val safeList    = pending ?: emptyList()
    val reviewedCount = (totalCount - safeList.size).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding(),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Text("‹", color = TextPrimary, fontSize = 28.sp, lineHeight = 32.sp)
            }
            Text(
                text       = monthKey.toMonthTitle(),
                color      = TextPrimary,
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
            )
            // Progress counter
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(56.dp).padding(end = 8.dp),
            ) {
                Text("$reviewedCount", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("/ $totalCount",  color = TextMuted, fontSize = 11.sp)
            }
        }

        // ── Card stack ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val cards = safeList.take(3)
            cards.reversed().forEachIndexed { reversedIdx, image ->
                val stackPos = cards.size - 1 - reversedIdx
                key(image.id) {
                    SwipeCard(
                        image         = image,
                        stackPosition = stackPos,
                        onSwiped      = { decision -> vm.swipe(image.id, decision) },
                        onWillSwipe   = {
                            pending?.drop(2)?.take(5)?.forEach { img ->
                                ImageLoader(context).enqueue(
                                    ImageRequest.Builder(context).data(img.contentUri).build()
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.74f)
                            .zIndex((3 - stackPos).toFloat()),
                    )
                }
            }

            // Returning card animation overlay (undo visual feedback)
            returningCard?.let { rc ->
                key(rc.image.id) {
                    AnimatedReturningCard(
                        image         = rc.image,
                        fromRight     = rc.fromRight,
                        screenWidthPx = screenWidthPx,
                        modifier      = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.74f)
                            .zIndex(10f),
                        onFinished    = { returningCard = null },
                    )
                }
            }
        }

        // ── Undo button — between cards and action buttons ────────────────────
        val undoAlpha by animateFloatAsState(if (canUndo) 1f else 0f, label = "undoAlpha")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .alpha(undoAlpha),
            contentAlignment = Alignment.Center,
        ) {
            TextButton(
                onClick = { if (canUndo) onUndoTap() },
                enabled = canUndo,
            ) {
                Text(
                    "↩  Desfazer",
                    color      = TextPrimary.copy(alpha = 0.75f),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // ── Action buttons ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            val top = safeList.firstOrNull()
            RoundActionButton(label = "✕", color = Delete,   bgAlpha = 0.15f, size = 64.dp) {
                top?.let { vm.swipe(it.id, ImageEntity.DELETE) }
            }
            RoundActionButton(label = "🔖", color = Bookmark, bgAlpha = 0.15f, size = 52.dp) {
                top?.let { vm.swipe(it.id, ImageEntity.BOOKMARK) }
            }
            RoundActionButton(label = "✓", color = Keep,     bgAlpha = 0.15f, size = 64.dp) {
                top?.let { vm.swipe(it.id, ImageEntity.KEEP) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Returning card animation — shows the undone card flying back from off-screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnimatedReturningCard(
    image: ImageEntity,
    fromRight: Boolean,
    screenWidthPx: Float,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit,
) {
    val startX  = if (fromRight) screenWidthPx * 1.5f else -screenWidthPx * 1.5f
    val offsetX = remember { Animatable(startX) }

    LaunchedEffect(Unit) {
        offsetX.animateTo(
            targetValue   = 0f,
            animationSpec = spring(
                stiffness    = Spring.StiffnessMediumLow,
                dampingRatio = Spring.DampingRatioNoBouncy,
            ),
        )
        onFinished()
    }

    Box(
        modifier = modifier
            .graphicsLayer { translationX = offsetX.value }
            .clip(RoundedCornerShape(20.dp))
            .background(Surface),
    ) {
        if (image.isVideo) {
            VideoThumbnail(
                uri      = Uri.parse(image.contentUri),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            AsyncImage(
                model              = ImageRequest.Builder(LocalContext.current).data(image.contentUri).build(),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Action button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RoundActionButton(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    bgAlpha: Float,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick  = onClick,
        colors   = IconButtonDefaults.filledIconButtonColors(containerColor = color.copy(alpha = bgAlpha)),
        modifier = Modifier.size(size).clip(CircleShape),
    ) {
        Text(label, color = color, fontSize = if (size > 60.dp) 24.sp else 20.sp)
    }
}

private fun String.toMonthTitle(): String {
    val parts = split("-")
    if (parts.size != 2) return this
    val monthNames = listOf(
        "Janeiro","Fevereiro","Março","Abril","Maio","Junho",
        "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro",
    )
    val month = parts[1].toIntOrNull() ?: return this
    return "${monthNames.getOrElse(month - 1) { this }} ${parts[0]}"
}
