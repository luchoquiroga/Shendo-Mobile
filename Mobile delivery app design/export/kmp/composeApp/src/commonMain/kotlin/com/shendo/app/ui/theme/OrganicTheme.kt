package com.shendo.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Organic design system — tokens transcribed from styles.css.
 * Never hard-code a colour or size in a screen; take it from here.
 */
@Immutable
object Organic {
    // roles
    val bg = Color(0xFFF5EAD8)
    val surface = Color(0xFFEBDDC5)
    val text = Color(0xFF201E1D)
    val accent = Color(0xFFC67139)
    val accent2 = Color(0xFF7A8A5E)
    val divider = Color(0x29201E1D) // text @ 16%

    // neutral ramp
    val neutral100 = Color(0xFFF9F4ED)
    val neutral200 = Color(0xFFEEE7DB)
    val neutral300 = Color(0xFFDCD3C4)
    val neutral400 = Color(0xFFC0B6A5)
    val neutral500 = Color(0xFFA19786)
    val neutral600 = Color(0xFF82796A)
    val neutral700 = Color(0xFF645C50)
    val neutral800 = Color(0xFF474238)
    val neutral900 = Color(0xFF2E2B25)

    // accent ramp (terracotta)
    val accent100 = Color(0xFFFFF2EB)
    val accent200 = Color(0xFFFFE1D0)
    val accent300 = Color(0xFFFFC6A5)
    val accent400 = Color(0xFFF6A06B)
    val accent500 = Color(0xFFD67F48)
    val accent600 = Color(0xFFB2622D)
    val accent700 = Color(0xFF8C491A)
    val accent800 = Color(0xFF643312)
    val accent900 = Color(0xFF402310)

    // accent-2 ramp (sage)
    val sage100 = Color(0xFFF0FAE1)
    val sage200 = Color(0xFFE1EECC)
    val sage300 = Color(0xFFCCDBB2)
    val sage400 = Color(0xFFAEBF92)
    val sage500 = Color(0xFF8FA073)
    val sage600 = Color(0xFF728157)
    val sage700 = Color(0xFF56633F)
    val sage800 = Color(0xFF3D472B)
    val sage900 = Color(0xFF272E1B)

    // spacing (1.10× scale)
    val space1 = 4.dp
    val space2 = 9.dp
    val space3 = 13.dp
    val space4 = 18.dp
    val space6 = 26.dp
    val space8 = 35.dp

    // radii — the system over-rounds; pills are RoundedCornerShape(50)
    val radiusSm = RoundedCornerShape(8.dp)
    val radiusMd = RoundedCornerShape(16.dp)
    val radiusLg = RoundedCornerShape(28.dp)
    val pill = RoundedCornerShape(percent = 50)
}

/**
 * Caprasimo (display) over Figtree (body).
 * Put the .ttf files in composeResources/font/ and load them with
 * org.jetbrains.compose.resources.Font — the expect/actual below keeps
 * the call site identical on Android, iOS, desktop and wasm.
 */
expect fun organicHeadingFamily(): FontFamily
expect fun organicBodyFamily(): FontFamily

@Composable
fun organicTypography(): Typography {
    val heading = organicHeadingFamily()
    val body = organicBodyFamily()
    return Typography(
        displaySmall = TextStyle(fontFamily = heading, fontSize = 34.sp, lineHeight = 36.sp),
        headlineSmall = TextStyle(fontFamily = heading, fontSize = 21.sp, lineHeight = 25.sp),
        titleMedium = TextStyle(fontFamily = heading, fontSize = 17.sp, lineHeight = 20.sp),
        bodyMedium = TextStyle(fontFamily = body, fontSize = 15.sp, lineHeight = 23.sp),
        bodySmall = TextStyle(fontFamily = body, fontSize = 12.5f.sp, lineHeight = 17.sp),
        labelSmall = TextStyle(fontFamily = body, fontSize = 11.5f.sp, fontWeight = FontWeight.Bold),
    )
}

@Composable
fun ShendoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Organic.accent,
            onPrimary = Organic.bg,
            secondary = Organic.accent2,
            background = Organic.bg,
            onBackground = Organic.text,
            surface = Organic.surface,
            onSurface = Organic.text,
        ),
        typography = organicTypography(),
        content = content,
    )
}
