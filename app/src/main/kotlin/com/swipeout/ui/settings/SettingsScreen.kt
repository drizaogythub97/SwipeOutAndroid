package com.swipeout.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swipeout.ui.strings.LocalStrings
import com.swipeout.ui.strings.formatBytes
import com.swipeout.ui.theme.*

private data class LangOption(val code: String, val label: String, val flag: String)

private val LANG_OPTIONS = listOf(
    LangOption("auto", "Automático / Auto", "🌐"),
    LangOption("pt",   "Português (Brasil)", "🇧🇷"),
    LangOption("en",   "English",             "🇺🇸"),
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = hiltViewModel(),
) {
    val strings         = LocalStrings.current
    val currentLang     by vm.currentLanguage.collectAsStateWithLifecycle()
    val totalBytesFreed by vm.totalBytesFreed.collectAsStateWithLifecycle()
    val stats30         by vm.stats30.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .systemBarsPadding(),
    ) {
        // Header
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.IconButton(onClick = onBack) {
                Text("‹", color = TextPrimary, fontSize = 28.sp)
            }
            Text(
                strings.settingsTitle,
                color      = TextPrimary,
                fontSize   = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Language section ──────────────────────────────────────────────
            SectionHeader(title = "Idioma / Language")  // bilingual by design
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface),
            ) {
                LANG_OPTIONS.forEachIndexed { idx, option ->
                    val selected = currentLang == option.code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.setLanguage(option.code) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(option.flag, fontSize = 22.sp)
                        Text(
                            text       = option.label,
                            color      = if (selected) Accent else TextPrimary,
                            fontSize   = 15.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier   = Modifier.weight(1f),
                        )
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Accent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("✓", color = androidx.compose.ui.graphics.Color.White,
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (idx < LANG_OPTIONS.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .padding(horizontal = 16.dp)
                                .background(Border)
                        )
                    }
                }
            }
            Text(
                text     = strings.settingsLangNote,
                color    = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            // ── Stats — últimos 30 dias ────────────────────────────────────────
            SectionHeader(title = strings.settingsStats30Title)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    emoji    = "🗑",
                    value    = "${stats30.filesDeleted}",
                    label    = strings.settingsFilesDeleted,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    emoji    = "✨",
                    value    = formatBytes(stats30.bytesFreed),
                    label    = strings.settingsStorageFreed,
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Total geral ────────────────────────────────────────────────────
            SectionHeader(title = strings.settingsTotalTitle)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Keep.copy(alpha = 0.10f))
                    .padding(20.dp),
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("🏆", fontSize = 32.sp)
                    Column {
                        Text(
                            text       = formatBytes(totalBytesFreed),
                            color      = Keep,
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text     = strings.settingsFreedSinceStart,
                            color    = TextSecondary,
                            fontSize = 13.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text          = title.uppercase(),
        color         = TextMuted,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier      = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun StatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(emoji, fontSize = 28.sp)
        Text(value, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, fontSize = 12.sp)
    }
}
