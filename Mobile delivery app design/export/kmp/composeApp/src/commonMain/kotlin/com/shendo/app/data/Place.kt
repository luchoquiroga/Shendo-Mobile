package com.shendo.app.data

import androidx.compose.ui.graphics.Color
import com.shendo.app.ui.theme.Organic

data class Place(
    val id: String,
    val name: String,
    val kind: String,
    val rating: String,
    val eta: String,
    val deliveryFee: Double,     // 0.0 == free
    val priceFrom: Double,
    val category: Category,
    val tint: Color,
)

enum class Category(val label: String, val dot: Color) {
    All("All", Organic.neutral500),
    Pizza("Pizza", Organic.accent500),
    Ramen("Ramen", Organic.sage500),
    Sushi("Sushi", Organic.neutral600),
    Burgers("Burgers", Organic.accent600),
    Sweet("Sweet", Organic.sage600),
}

val samplePlaces = listOf(
    Place("r1", "Casa Bruma", "Wood-fired pizza · Palermo", "4.8", "25–35 min", 0.0, 12.40, Category.Pizza, Organic.accent300),
    Place("r2", "Ochenta Ramen", "Ramen & gyoza · Almagro", "4.7", "30–40 min", 1.20, 14.00, Category.Ramen, Organic.sage300),
    Place("r3", "El Patio Sushi", "Nigiri, rolls · Recoleta", "4.9", "35–45 min", 0.0, 16.80, Category.Sushi, Organic.neutral300),
    Place("r4", "Doble Cuarto", "Smash burgers · Villa Crespo", "4.6", "20–30 min", 0.90, 11.20, Category.Burgers, Organic.accent400),
    Place("r5", "Verde Mesa", "Grain bowls, salads · Centro", "4.5", "25–35 min", 0.0, 10.60, Category.Ramen, Organic.sage400),
    Place("r6", "Miel & Sal", "Pastry, gelato · San Telmo", "4.8", "15–25 min", 1.50, 6.90, Category.Sweet, Organic.neutral400),
)

fun money(v: Double): String {
    val cents = kotlin.math.round(v * 100).toInt()
    return "$" + (cents / 100) + "." + (cents % 100).toString().padStart(2, '0')
}
