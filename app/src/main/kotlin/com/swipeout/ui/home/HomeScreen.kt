package com.swipeout.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.swipeout.data.db.dao.ImageDao
import com.swipeout.data.db.entity.MonthlyMenuEntity
import com.swipeout.ui.common.ProgressRing
import com.swipeout.ui.strings.AppStrings
import com.swipeout.ui.strings.LocalStrings
import com.swipeout.ui.strings.formatBytes
import com.swipeout.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMonthClick: (String) -> Unit,
    onAlbumClick: (bucketId: Long, albumName: String) -> Unit,
    onSettingsClick: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
) {
    val months          by vm.months.collectAsStateWithLifecycle()
    val albums          by vm.albums.collectAsStateWithLifecycle()
    val isSyncing       by vm.isSyncing.collectAsStateWithLifecycle()
    val totalBytesFreed by vm.totalBytesFreed.collectAsStateWithLifecycle()
    val strings         = LocalStrings.current
    val context         = LocalContext.current

    // Tab selection — months is the default
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Meses, 1 = Álbuns

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    else
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    val hasPermission by remember {
        derivedStateOf {
            permissions.all { p ->
                ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    val retryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) vm.sync()
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) vm.sync()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding()
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column {
                Text(strings.appName, color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(strings.tagline, color = TextMuted, fontSize = 14.sp)
            }

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (totalBytesFreed > 0) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Keep.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(formatBytes(totalBytesFreed), color = Keep, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(strings.totalFreedLabel, color = TextMuted, fontSize = 11.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceHigh)
                        .clickable { onSettingsClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("⚙", fontSize = 20.sp)
                }
            }
        }

        // ── Tab switcher ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Surface),
        ) {
            listOf(strings.monthsHeader, strings.albumsHeader).forEachIndexed { idx, label ->
                val selected = selectedTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Accent else Surface)
                        .clickable { selectedTab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = label,
                        color      = if (selected) androidx.compose.ui.graphics.Color.White else TextMuted,
                        fontSize   = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }

        // ── Content — only the selected tab is rendered ───────────────────────
        PullToRefreshBox(
            isRefreshing = isSyncing,
            onRefresh    = { vm.sync() },
            modifier     = Modifier.fillMaxSize(),
        ) {
            when {
                !hasPermission -> {
                    PermissionDeniedState(
                        strings  = strings,
                        onRetry  = { retryLauncher.launch(permissions) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                selectedTab == 0 -> {
                    // ── MESES ─────────────────────────────────────────────────
                    if (months.isEmpty() && !isSyncing) {
                        EmptyState(strings = strings, modifier = Modifier.fillMaxSize())
                    } else {
                        LazyColumn(
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier            = Modifier.fillMaxSize(),
                        ) {
                            items(months, key = { it.key }) { menu ->
                                MonthRow(
                                    menu    = menu,
                                    strings = strings,
                                    onClick = { onMonthClick(menu.key) },
                                )
                            }
                        }
                    }
                }
                else -> {
                    // ── ÁLBUNS ────────────────────────────────────────────────
                    if (albums.isEmpty() && !isSyncing) {
                        EmptyState(strings = strings, modifier = Modifier.fillMaxSize())
                    } else {
                        LazyColumn(
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier            = Modifier.fillMaxSize(),
                        ) {
                            items(albums, key = { it.bucketId }) { album ->
                                AlbumRow(
                                    album   = album,
                                    onClick = { onAlbumClick(album.bucketId, album.bucketName) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Month row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MonthRow(
    menu: MonthlyMenuEntity,
    strings: AppStrings,
    onClick: () -> Unit,
) {
    val completed = menu.isCompleted
    val progress  = if (menu.totalCount > 0) menu.reviewedCount.toFloat() / menu.totalCount else 0f

    // Format month title from key at display time so it respects the current language.
    val title = remember(menu.key, strings.monthNames) {
        val parts = menu.key.split("-")
        if (parts.size == 2) {
            val monthIdx  = (parts[1].toIntOrNull() ?: 1) - 1
            val monthName = strings.monthNames.getOrElse(monthIdx) { parts[1] }
            "$monthName ${parts[0]}"
        } else menu.key
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (completed) 0.55f else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (!completed && menu.coverUri.isNotEmpty()) {
            AsyncImage(
                model              = menu.coverUri,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceHigh),
            )
        } else {
            ProgressRing(
                progress   = if (completed) 1f else progress,
                isComplete = completed,
                modifier   = Modifier.size(40.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text           = title,
                    color          = if (completed) TextMuted else TextPrimary,
                    fontSize       = 16.sp,
                    fontWeight     = FontWeight.SemiBold,
                    textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
                )
                if (completed) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Keep),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✓", color = androidx.compose.ui.graphics.Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text     = if (completed) "${strings.reviewed} · ${menu.totalCount} ${strings.files}"
                           else "${menu.reviewedCount} de ${menu.totalCount} ${strings.files}",
                color    = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Text("›", color = TextMuted, fontSize = 22.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Album row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AlbumRow(
    album: ImageDao.AlbumInfo,
    onClick: () -> Unit,
    strings: AppStrings = LocalStrings.current,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Cover thumbnail — always available (guaranteed by the DAO query)
        AsyncImage(
            model              = album.coverUri,
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceHigh),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = album.bucketName,
                color      = TextPrimary,
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text     = "${album.pendingCount} ${strings.pending}",
                color    = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Text("›", color = TextMuted, fontSize = 22.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty states
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PermissionDeniedState(
    strings: AppStrings,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("📷", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(strings.noPhotos, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(strings.grantAccess, color = TextMuted, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick  = onRetry,
            colors   = ButtonDefaults.buttonColors(containerColor = Accent),
            shape    = RoundedCornerShape(12.dp),
        ) {
            Text(strings.grantAccess, color = androidx.compose.ui.graphics.Color.White,
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
        }
    }
}

@Composable
private fun EmptyState(strings: AppStrings, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("📷", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(strings.noPhotos, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(strings.grantAccess, color = TextMuted, fontSize = 14.sp)
    }
}
