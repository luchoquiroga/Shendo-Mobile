package com.admin_gym.mobile_shendo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.admin_gym.mobile_shendo.R

// Caprasimo (títulos) sobre Figtree (texto). Figtree es variable: un Font por peso.
private val Caprasimo = FontFamily(Font(R.font.caprasimo_regular))

@OptIn(ExperimentalTextApi::class)
private fun figtree(weight: FontWeight) = Font(
    R.font.figtree_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val Figtree = FontFamily(
    figtree(FontWeight.Normal),
    figtree(FontWeight.Medium),
    figtree(FontWeight.SemiBold),
    figtree(FontWeight.Bold),
)

val Typography = Typography(
    // títulos (Caprasimo)
    displaySmall = TextStyle(fontFamily = Caprasimo, fontSize = 34.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = Caprasimo, fontSize = 21.sp, lineHeight = 25.sp),
    titleLarge = TextStyle(fontFamily = Caprasimo, fontSize = 19.sp, lineHeight = 23.sp),
    titleMedium = TextStyle(fontFamily = Caprasimo, fontSize = 17.sp, lineHeight = 20.sp),
    // texto (Figtree)
    titleSmall = TextStyle(fontFamily = Figtree, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontFamily = Figtree, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Figtree, fontSize = 15.sp, lineHeight = 23.sp),
    bodySmall = TextStyle(fontFamily = Figtree, fontSize = 12.5.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = Figtree, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontFamily = Figtree, fontSize = 12.5.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold),
    labelSmall = TextStyle(fontFamily = Figtree, fontSize = 11.5.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold),
)
