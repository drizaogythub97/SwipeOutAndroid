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

/**
 * Review screen for the album-based swipe flow.
 * Visually identical to [ReviewScreen]; backed by [AlbumReviewViewModel] which
 * skips markMonthCompleted — months update automatically via sync/rebuildMenus.
 */
@Composable
fun AlbumReviewScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    vm: AlbumReviewViewModel = hiltViewModel(),
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
                vm.albumName.ifBlank { strings.reviewChoices },
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

        // Footer button
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
                    TextureVideoPlayer(uri = Uri.parse(img.contentUri), modifier = Modifier.fillMaxSize())
                } else {
                    AsyncImage(
                        model = img.contentUri, contentDescription = null,
                        contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize(),
                    )
                }
                IconButton(
                    onClick  = { previewImage = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                ) {
                    Text("✕", color = TextPrimary, fontSize = 18.sp)
                }
                if (img.decision == ImageEntity.DELETE || img.decision == ImageEntity.KEEP) {
                    val isDeleted = img.decision == ImageEntity.DELETE
                    Button(
                        onClick  = { vm.revert(img); previewImage = null },
                        colors   = ButtonDefaults.buttonColors(containerColor = if (isDeleted) Keep else Delete),
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
                    ) {
                        Text(
                            if (isDeleted) "✓ ${strings.keep}" else "✕ ${strings.delete}",
                            color = TextPrimary, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
