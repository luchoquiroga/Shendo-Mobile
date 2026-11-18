package com.shendo.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shendo.app.data.*
import com.shendo.app.ui.theme.Organic

/**
 * Home / browse — the "1c" layout.
 * Profile nav → search → craving circles → places near you → bottom nav.
 *
 * Image areas are placeholder tint blocks (same as the mock). Swap
 * PlaceThumb's Box for an AsyncImage (Coil 3 / kamel) once you have photos.
 */
@Composable
fun HomeScreen() {
    var craving by remember { mutableStateOf(Category.All) }
    var favourites by remember { mutableStateOf(setOf("r3")) }
    var tab by remember { mutableStateOf(HomeTab.Home) }

    val places = remember(craving) {
        samplePlaces.filter { craving == Category.All || it.category == craving }
    }

    Column(Modifier.fillMaxSize().background(Organic.bg)) {
        ProfileBar(name = "Hola, Lucho", address = "Av. Lavalle 1240")
        SearchPill(Modifier.padding(horizontal = 20.dp, vertical = 14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { CravingRow(selected = craving, onSelect = { craving = it }) }
            item {
                Text(
                    text = if (craving == Category.All) "Places near you" else "${craving.label} near you",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Organic.text,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
                )
            }
            items(places, key = { it.id }) { place ->
                PlaceCard(
                    place = place,
                    isFavourite = place.id in favourites,
                    onToggleFavourite = {
                        favourites = if (place.id in favourites) favourites - place.id else favourites + place.id
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                )
            }
        }

        BottomNav(selected = tab, onSelect = { tab = it })
    }
}

/* ── nav ─────────────────────────────────────────────────────────────── */

@Composable
private fun ProfileBar(name: String, address: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp),
    ) {
        // Avatar placeholder — replace with the user's photo.
        Box(Modifier.size(44.dp).clip(CircleShape).background(Organic.sage300), Alignment.BottomCenter) {
            Box(Modifier.size(17.dp).offset(y = (-19).dp).clip(CircleShape).background(Organic.sage600))
            Box(Modifier.size(34.dp).offset(y = 8.dp).clip(CircleShape).background(Organic.sage500))
        }
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleMedium, color = Organic.text)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Outlined.LocationOn, null, Modifier.size(13.dp), tint = Organic.accent)
                Text(
                    address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Organic.text.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SearchPill(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(Organic.pill)
            .background(Organic.surface)
            .clickable { /* navigate to search */ }
            .padding(horizontal = 16.dp),
    ) {
        Icon(Icons.Outlined.Search, null, Modifier.size(18.dp), tint = Organic.text.copy(alpha = 0.5f))
        Text(
            "Search dishes or places",
            style = MaterialTheme.typography.bodyMedium,
            color = Organic.text.copy(alpha = 0.5f),
        )
    }
}

/* ── cravings ────────────────────────────────────────────────────────── */

@Composable
private fun CravingRow(selected: Category, onSelect: (Category) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp),
    ) {
        items(Category.entries.toList()) { c ->
            val on = c == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.width(74.dp).clickable { onSelect(c) },
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(if (on) Organic.accent100 else Organic.surface)
                        .then(if (on) Modifier.ring(Organic.accent) else Modifier),
                ) {
                    Box(Modifier.size(30.dp).clip(CircleShape).background(c.dot))
                }
                Text(
                    c.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (on) Organic.accent800 else Organic.text.copy(alpha = 0.6f),
                )
            }
        }
    }
}

/** 2.5dp accent ring, the selected state of the craving circles. */
private fun Modifier.ring(color: Color) = this.then(
    Modifier.border(width = 2.5.dp, color = color, shape = CircleShape)
)

/* ── place card ──────────────────────────────────────────────────────── */

@Composable
private fun PlaceCard(
    place: Place,
    isFavourite: Boolean,
    onToggleFavourite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Organic.radiusLg,
        color = Organic.surface,
    ) {
        Row(
            modifier = Modifier.padding(12.dp).height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PlaceThumb(
                tint = place.tint,
                modifier = Modifier.width(104.dp).fillMaxHeight().defaultMinSize(minHeight = 104.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        place.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Organic.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                        tint = if (isFavourite) Organic.accent else Organic.text.copy(alpha = 0.35f),
                        modifier = Modifier.size(22.dp).clip(CircleShape).clickable(onClick = onToggleFavourite),
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    place.kind,
                    style = MaterialTheme.typography.bodySmall,
                    color = Organic.text.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Filled.Star, null, Modifier.size(13.dp), tint = Organic.accent)
                    Text(place.rating, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Organic.text)
                    Text("·", style = MaterialTheme.typography.bodySmall, color = Organic.text.copy(alpha = 0.35f))
                    Text(place.eta, style = MaterialTheme.typography.bodySmall, color = Organic.text.copy(alpha = 0.7f))
                }
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Chip("From ${money(place.priceFrom)}", Organic.accent100, Organic.accent800)
                    val free = place.deliveryFee == 0.0
                    Chip(
                        label = if (free) "Free delivery" else "${money(place.deliveryFee)} delivery",
                        bg = if (free) Organic.sage100 else Organic.neutral200,
                        fg = if (free) Organic.sage800 else Organic.neutral800,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceThumb(tint: Color, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(22.dp)).background(tint)) {
        Box(
            Modifier.size(74.dp).offset(x = (-14).dp, y = 20.dp)
                .align(Alignment.BottomStart).clip(CircleShape)
                .background(Color.White.copy(alpha = 0.30f))
        )
    }
}

@Composable
private fun Chip(label: String, bg: Color, fg: Color) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        maxLines = 1,
        modifier = Modifier.clip(Organic.pill).background(bg).padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/* ── bottom nav ──────────────────────────────────────────────────────── */

enum class HomeTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Outlined.Home),
    Orders("Orders", Icons.Outlined.Receipt),
    You("You", Icons.Outlined.Person),
}

@Composable
private fun BottomNav(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Organic.divider))
        Row(
            Modifier.fillMaxWidth().background(Organic.surface).padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            HomeTab.entries.forEach { t ->
                val on = t == selected
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f).clickable { onSelect(t) }.padding(vertical = 6.dp),
                ) {
                    Icon(
                        t.icon, t.label, Modifier.size(22.dp),
                        tint = if (on) Organic.accent else Organic.text.copy(alpha = 0.45f),
                    )
                    Text(
                        t.label,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (on) Organic.accent else Organic.text.copy(alpha = 0.45f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
