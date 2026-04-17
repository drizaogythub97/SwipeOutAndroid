package com.swipeout.ui.review

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.swipeout.data.db.entity.ImageEntity
import com.swipeout.ui.common.MediaThumbnail
import com.swipeout.ui.common.TextureVideoPlayer
import com.swipeout.ui.strings.LocalStrings
import com.swipeout.ui.strings.formatBytes
import com.swipeout.ui.theme.*

@Composable
fun ReviewScreen(
    monthKey: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
    vm: ReviewViewModel = hiltViewModel(),
) {
    val state   = vm.state.collectAsStateWithLifecycle().value
    val strings = LocalStrings.current
    val context = LocalContext.current
    var previewImage by remember { mutableStateOf<ImageEntity?>(null) }

    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) vm.onSystemDeleteConfirmed()
        else vm.clearPendingSender()
    }

    LaunchedEffect(state.pendingDeleteSender) {
        state.pendingDeleteSender?.let { sender ->
            deleteLauncher.launch(IntentSenderRequest.Builder(sender).build())
        }
    }
    LaunchedEffect(state.failedDeleteCount) {
        if (state.failedDeleteCount > 0) {
            android.widget.Toast.makeText(
                context,
                strings.deleteFailedMessage(state.failedDeleteCount),
                android.widget.Toast.LENGTH_LONG,
            ).show()
        }
    }
    LaunchedEffect(state.isDone) {
        if (state.isDone) onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding(),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Text("‹", color = TextPrimary, fontSize = 28.sp)
            }
            Text(
                strings.reviewChoices,
                color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.width(48.dp))
        }

        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCard(
                label    = strings.keep,
                count    = state.keptImages.size,
                color    = Keep,
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                label    = strings.delete,
                count    = state.deletedImages.size,
                color    = Delete,
                subtitle = if (state.deletedBytes > 0) formatBytes(state.deletedBytes) else null,
                modifier = Modifier.weight(1f),
            )
        }

        // Scrollable photo sections — manual grid to avoid LazyVerticalGrid/verticalScroll conflict
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (state.keptImages.isNotEmpty()) {
                PhotoSection(
                    title  = "✅ ${strings.keep} (${state.keptImages.size})",
                    photos = state.keptImages,
                    border = Keep,
                    onTap  = { previewImage = it },
                )
            }
            if (state.deletedImages.isNotEmpty()) {
                PhotoSection(
                    title    = "🗑 ${strings.delete} (${state.deletedImages.size})",
                    subtitle = if (state.deletedBytes > 0) "${formatBytes(state.deletedBytes)} ${strings.toFree}" else null,
                    photos   = state.deletedImages,
                    border   = Delete,
                    onTap    = { previewImage = it },
                )
            }
            if (state.bookmarkedImages.isNotEmpty()) {
                PhotoSection(
                    title  = "🔖 ${strings.later} (${state.bookmarkedImages.size})",
                    photos = state.bookmarkedImages,
                    border = Bookmark,
                    onTap  = { previewImage = it },
                )
            }
            Spacer(Modifier.height(80.dp))
        }

        // Footer — botão sempre ativo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Background)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
        ) {
            Button(
                onClick = { (context as? android.app.Activity)?.let { vm.confirmReview(it) } },
                enabled = !state.isLoading,
                colors  = ButtonDefaults.buttonColors(
                    containerColor         = if (state.deletedImages.isNotEmpty()) Delete else Accent,
                    disabledContainerColor = Surface,
                ),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    val label = if (state.deletedImages.isEmpty()) {
                        strings.confirmReview
                    } else {
                        buildString {
                            append("${strings.confirmReview} (${state.deletedImages.size})")
                            if (state.deletedBytes > 0) append(" · ${formatBytes(state.deletedBytes)}")
                        }
                    }
                    Text(
                        label,
                        color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }
    }

    // Full-screen preview
    previewImage?.let { img ->
        Dialog(
            onDismissRequest = { previewImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center,
            ) {
                if (img.isVideo) {
                    TextureVideoPlayer(
                        uri      = Uri.parse(img.contentUri),
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    AsyncImage(
                        model              = img.contentUri,
                        contentDescription = null,
                        contentScale       = ContentScale.Fit,
                        modifier           = Modifier.fillMaxSize(),
                    )
                }
                IconButton(
                    onClick  = { previewImage = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                ) {
                    Text("✕", color = TextPrimary, fontSize = 18.sp)
                }
                // Preview revert buttons — DELETE toggles to KEEP, other decisions (KEEP
                // and BOOKMARK) revert to PENDING so the user can re-review them.
                val (revertLabel, revertColor) = when (img.decision) {
                    ImageEntity.DELETE   -> "✓ ${strings.keep}"   to Keep
                    ImageEntity.KEEP     -> "✕ ${strings.delete}" to Delete
                    ImageEntity.BOOKMARK -> "↩ ${strings.undo}"   to Accent
                    else                 -> null                  to Accent
                }
                if (revertLabel != null) {
                    Button(
                        onClick  = { vm.revert(img); previewImage = null },
                        colors   = ButtonDefaults.buttonColors(containerColor = revertColor),
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
                    ) {
                        Text(
                            revertLabel,
                            color = TextPrimary, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SummaryCard(
    label: String,
    count: Int,
    color: Color,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("$count", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 13.sp)
        if (subtitle != null) {
            Text(subtitle, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun PhotoSection(
    title: String,
    subtitle: String? = null,
    photos: List<ImageEntity>,
    border: Color,
    onTap: (ImageEntity) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        if (subtitle != null) {
            Text(subtitle, color = border, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }

        // Grid manual de 3 colunas — evita conflito de scroll com LazyVerticalGrid
        val rows = (photos.size + 2) / 3
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) { rowIdx ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    for (colIdx in 0 until 3) {
                        val photoIdx = rowIdx * 3 + colIdx
                        if (photoIdx < photos.size) {
                            val photo = photos[photoIdx]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceHigh)
                                    .clickable { onTap(photo) },
                            ) {
                                MediaThumbnail(
                                    image    = photo,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
