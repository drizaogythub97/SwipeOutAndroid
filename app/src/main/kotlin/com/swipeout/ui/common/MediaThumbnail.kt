package com.swipeout.ui.common

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.exoplayer.ExoPlayer
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.swipeout.data.db.entity.ImageEntity
import com.swipeout.ui.theme.SurfaceHigh

/**
 * Smart thumbnail: AsyncImage para fotos, VideoThumbnail (primeiro frame) para vídeos.
 */
@Composable
fun MediaThumbnail(
    image: ImageEntity,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (image.isVideo) {
        VideoThumbnail(uri = Uri.parse(image.contentUri), modifier = modifier)
    } else {
        AsyncImage(
            model              = ImageRequest.Builder(LocalContext.current).data(image.contentUri).build(),
            contentDescription = null,
            contentScale       = contentScale,
            modifier           = modifier,
        )
    }
}

/**
 * Thumbnail estático de vídeo — primeiro frame decodificado pelo VideoFrameDecoder
 * registrado no singleton ImageLoader. Isso reaproveita o cache de memória/disco do
 * Coil (antes eram chamadas cruas a MediaMetadataRetriever em Dispatchers.IO, sem
 * cache, o que reabria o decoder a cada recomposição do thumbnail).
 */
@Composable
fun VideoThumbnail(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(
        modifier         = modifier.background(SurfaceHigh),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                // Arbitrary cache-key suffix so this request doesn't collide with a
                // full-resolution image of the same URI cached elsewhere.
                .memoryCacheKey("video-frame:$uri")
                .diskCacheKey("video-frame:$uri")
                .build(),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Text("▶", color = Color.White, fontSize = 18.sp)
        }
    }
}

/**
 * Player de vídeo completo via TextureView + ExoPlayer.
 * TextureView (não SurfaceView) para funcionar com graphicsLayer alpha/rotation do Compose.
 *
 * [snapBackKey] deve ser incrementado pelo pai após cada snap-back (arrasto incompleto).
 * Isso aciona a reconexão do player à TextureView, desbloqueando o frame congelado.
 *
 * Observa o lifecycle do owner para pausar em ON_STOP (evita áudio tocando quando o
 * usuário vai pro launcher ou abre outro app) e retomar em ON_START.
 */
@Composable
fun TextureVideoPlayer(uri: Uri, snapBackKey: Int = 0, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val player = remember(uri) {
        ExoPlayer.Builder(context).build().also { p ->
            p.setMediaItem(Media3Item.fromUri(uri))
            p.prepare()
            p.playWhenReady = true
            p.repeatMode    = ExoPlayer.REPEAT_MODE_ONE
            p.volume        = 1f
        }
    }

    // Keeps a reference to the active TextureView so reconnection can target it
    val currentTv = remember { mutableStateOf<android.view.TextureView?>(null) }

    // After each snap-back, re-attach the TextureView to unfreeze the video frame
    LaunchedEffect(snapBackKey) {
        if (snapBackKey > 0) {
            currentTv.value?.let { tv ->
                player.clearVideoSurface()
                player.setVideoTextureView(tv)
            }
        }
    }

    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP  -> player.playWhenReady = false
                Lifecycle.Event.ON_START -> player.playWhenReady = true
                else                      -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.stop()
            player.clearVideoSurface()
            player.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            android.view.TextureView(ctx).also { tv ->
                currentTv.value = tv
                player.setVideoTextureView(tv)
            }
        },
        update = { tv ->
            currentTv.value = tv
        },
        modifier = modifier,
    )
}
