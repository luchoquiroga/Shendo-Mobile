package com.admin_gym.mobile_shendo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design system "Organic" (de Claude Design, "Shendo Home").
 * Las pantallas no escriben colores ni radios a mano: los toman de acá o de MaterialTheme.
 */
@Immutable
object Organic {
    // roles
    val bg = Color(0xFFF5EAD8)
    val surface = Color(0xFFEBDDC5)
    val text = Color(0xFF201E1D)
    val accent = Color(0xFFC67139)
    val accent2 = Color(0xFF7A8A5E)
    val divider = Color(0x29201E1D) // text al 16%

    // texto atenuado (el mock usa text con alpha)
    val textMuted = text.copy(alpha = 0.65f)
    val textFaint = text.copy(alpha = 0.45f)

    // neutral
    val neutral100 = Color(0xFFF9F4ED)
    val neutral200 = Color(0xFFEEE7DB)
    val neutral300 = Color(0xFFDCD3C4)
    val neutral400 = Color(0xFFC0B6A5)
    val neutral500 = Color(0xFFA19786)
    val neutral600 = Color(0xFF82796A)
    val neutral700 = Color(0xFF645C50)
    val neutral800 = Color(0xFF474238)
    val neutral900 = Color(0xFF2E2B25)

    // accent (terracota)
    val accent100 = Color(0xFFFFF2EB)
    val accent200 = Color(0xFFFFE1D0)
    val accent300 = Color(0xFFFFC6A5)
    val accent400 = Color(0xFFF6A06B)
    val accent500 = Color(0xFFD67F48)
    val accent600 = Color(0xFFB2622D)
    val accent700 = Color(0xFF8C491A)
    val accent800 = Color(0xFF643312)
    val accent900 = Color(0xFF402310)

    // accent-2 (salvia)
    val sage100 = Color(0xFFF0FAE1)
    val sage200 = Color(0xFFE1EECC)
    val sage300 = Color(0xFFCCDBB2)
    val sage400 = Color(0xFFAEBF92)
    val sage500 = Color(0xFF8FA073)
    val sage600 = Color(0xFF728157)
    val sage700 = Color(0xFF56633F)
    val sage800 = Color(0xFF3D472B)
    val sage900 = Color(0xFF272E1B)

    val error = Color(0xFFB3261E)
    val errorContainer = Color(0xFFF9DEDC)
    val onErrorContainer = Color(0xFF410E0B)

    // espaciado (escala 1.10x)
    val space1 = 4.dp
    val space2 = 9.dp
    val space3 = 13.dp
    val space4 = 18.dp
    val space6 = 26.dp
    val space8 = 35.dp

    /** Margen lateral de las pantallas en el mock. */
    val gutter = 20.dp

    // radios: el sistema redondea de más; las pills son 50%
    val radiusSm = RoundedCornerShape(8.dp)
    val radiusMd = RoundedCornerShape(16.dp)
    val radiusLg = RoundedCornerShape(28.dp)
    val pill = RoundedCornerShape(percent = 50)

    /** Tintes para los bloques que reemplazan a las fotos (no hay imágenes de restaurantes). */
    val tintes = listOf(accent300, sage300, neutral300, accent400, sage400, neutral400)

    /** Colores del punto de cada categoría en los círculos "antojos". */
    val puntos = listOf(accent500, sage500, neutral600, accent600, sage600, accent400, sage700)
}
