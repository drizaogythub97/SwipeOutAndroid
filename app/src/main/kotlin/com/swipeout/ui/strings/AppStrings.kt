package com.swipeout.ui.strings

import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

data class AppStrings(
    // App
    val appName: String,
    val tagline: String,
    // Onboarding
    val onboardingPage1Title: String,
    val onboardingPage1Body: String,
    val onboardingPage2Title: String,
    val onboardingPage2Body: String,
    val onboardingPage3Title: String,
    val onboardingPage3Body: String,
    val next: String,
    val getStarted: String,
    // Home
    val monthsHeader: String,
    val noPhotos: String,
    val grantAccess: String,
    val reviewed: String,
    val files: String,
    val totalFreedLabel: String,
    // Review
    val reviewChoices: String,
    val keep: String,
    val delete: String,
    val later: String,
    val confirmDeletion: String,
    val confirmReview: String,
    val toFree: String,
    // Swipe
    val monthReviewed: String,
    val checkChoices: String,
)

private val PT = AppStrings(
    appName              = "SwipeOut",
    tagline              = "Limpe sua galeria",
    onboardingPage1Title = "Bem-vindo ao SwipeOut",
    onboardingPage1Body  = "O jeito mais rápido de organizar e limpar sua galeria de fotos.",
    onboardingPage2Title = "Deslize para decidir",
    onboardingPage2Body  = "Deslize para a direita para manter, para a esquerda para deletar. Simples assim.",
    onboardingPage3Title = "Veja o impacto",
    onboardingPage3Body  = "Acompanhe quanto espaço você libera no seu dispositivo a cada revisão.",
    next                 = "Próximo",
    getStarted           = "Começar",
    monthsHeader         = "MESES",
    noPhotos             = "Nenhuma foto encontrada",
    grantAccess          = "Conceda acesso à galeria para começar",
    reviewed             = "Revisado",
    files                = "arquivos",
    totalFreedLabel      = "liberados",
    reviewChoices        = "Revise suas escolhas",
    keep                 = "Manter",
    delete               = "Deletar",
    later                = "Depois",
    confirmDeletion      = "Confirmar exclusão",
    confirmReview        = "Confirmar revisão",
    toFree               = "a liberar",
    monthReviewed        = "Mês revisado!",
    checkChoices         = "Confira suas escolhas antes de deletar",
)

private val EN = AppStrings(
    appName              = "SwipeOut",
    tagline              = "Clean your gallery",
    onboardingPage1Title = "Welcome to SwipeOut",
    onboardingPage1Body  = "The fastest way to organize and clean your photo gallery.",
    onboardingPage2Title = "Swipe to decide",
    onboardingPage2Body  = "Swipe right to keep, left to delete. That simple.",
    onboardingPage3Title = "See the impact",
    onboardingPage3Body  = "Track how much space you free on your device with each review session.",
    next                 = "Next",
    getStarted           = "Get Started",
    monthsHeader         = "MONTHS",
    noPhotos             = "No photos found",
    grantAccess          = "Grant gallery access to get started",
    reviewed             = "Reviewed",
    files                = "files",
    totalFreedLabel      = "freed",
    reviewChoices        = "Review your choices",
    keep                 = "Keep",
    delete               = "Delete",
    later                = "Later",
    confirmDeletion      = "Confirm deletion",
    confirmReview        = "Confirm review",
    toFree               = "to free",
    monthReviewed        = "Month reviewed!",
    checkChoices         = "Review your choices before deleting",
)

fun resolveStrings(languagePref: String = "auto"): AppStrings {
    val lang = when (languagePref) {
        "pt"   -> "pt"
        "en"   -> "en"
        else   -> Locale.getDefault().language
    }
    return if (lang == "pt") PT else EN
}

val LocalStrings = staticCompositionLocalOf { PT }

fun formatBytes(bytes: Long): String = when {
    bytes <= 0               -> "0 B"
    bytes < 1_024            -> "$bytes B"
    bytes < 1_048_576        -> "${bytes / 1_024} KB"
    bytes < 1_073_741_824    -> "${"%.1f".format(bytes / 1_048_576f)} MB"
    else                     -> "${"%.2f".format(bytes / 1_073_741_824f)} GB"
}
