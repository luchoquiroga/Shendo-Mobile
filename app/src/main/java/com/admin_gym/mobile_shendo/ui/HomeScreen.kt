package com.admin_gym.mobile_shendo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.admin_gym.mobile_shendo.data.Restaurante
import com.admin_gym.mobile_shendo.ui.theme.Organic

/**
 * Home: perfil + buscador fijos arriba; antojos (categorías), título y restaurantes scrollean.
 * Layout del mock "1c" de Claude Design.
 */
@Composable
fun HomeScreen(vm: MainViewModel) {
    LaunchedEffect(Unit) { vm.cargarRestaurantes() }

    var busqueda by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf<String?>(null) }
    var soloFavoritos by rememberSaveable { mutableStateOf(false) }

    // Filtros: los rubros de todos los restaurantes, sin repetir ("Pizza" y "pizza" cuentan como uno).
    val categorias = remember(vm.restaurantes) {
        vm.restaurantes
            .flatMap { it.categorias }
            .map { it.trim().replaceFirstChar(Char::uppercaseChar) }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
    }

    // Si la categoría elegida desaparece tras recargar, volvemos a "Todos"
    LaunchedEffect(categorias) {
        if (categorias.isNotEmpty() && categoria != null && categoria !in categorias) categoria = null
    }

    val texto = busqueda.trim()
    val filtrados = remember(vm.restaurantes, vm.favoritos, busqueda, categoria, soloFavoritos) {
        vm.restaurantes.filter { r ->
            (!soloFavoritos || r.id in vm.favoritos) &&
                (categoria == null || r.categorias.any { it.trim().equals(categoria, ignoreCase = true) }) &&
                (texto.isEmpty() ||
                    r.nombre.contains(texto, ignoreCase = true) ||
                    r.categorias.any { it.contains(texto, ignoreCase = true) })
        }
    }

    val titulo = when {
        soloFavoritos -> "Tus favoritos"
        categoria != null -> categoria.orEmpty()
        else -> "Cerca tuyo"
    }
    val hayFiltro = soloFavoritos || categoria != null || texto.isNotEmpty()

    Column(Modifier.fillMaxSize()) {
        BarraPerfil(vm)
        Buscador(
            valor = busqueda,
            onChange = { busqueda = it },
            modifier = Modifier.padding(horizontal = Organic.gutter, vertical = 14.dp),
        )

        ListaRefrescable(
            loading = vm.loading,
            onRefresh = vm::cargarRestaurantes,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                FilaAntojos(
                    categorias = categorias,
                    categoria = categoria,
                    soloFavoritos = soloFavoritos,
                    onTodos = {
                        categoria = null
                        soloFavoritos = false
                    },
                    onCategoria = {
                        categoria = it
                        soloFavoritos = false
                    },
                    onFavoritos = {
                        categoria = null
                        soloFavoritos = true
                    },
                )
            }
            item {
                Text(
                    titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Organic.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = Organic.gutter, end = Organic.gutter, top = 8.dp),
                )
            }
            listaOVacio(
                items = filtrados,
                loading = vm.loading,
                textoVacio = if (vm.restaurantes.isEmpty() && !hayFiltro) {
                    "No hay restaurantes disponibles"
                } else {
                    "No encontramos restaurantes con ese filtro"
                },
                key = { it.id },
            ) { r ->
                TarjetaRestaurante(
                    restaurante = r,
                    favorito = r.id in vm.favoritos,
                    onFavorito = { vm.alternarFavorito(r) },
                    onClick = { vm.irA(Pantalla.Menu(r)) },
                    modifier = Modifier.padding(horizontal = Organic.gutter),
                )
            }
        }
    }
}

/* ── perfil ──────────────────────────────────────────────────────────── */

/** Avatar placeholder del mock (tres círculos salvia). Lo usa también el Perfil. */
@Composable
internal fun AvatarPlaceholder(tamano: Dp, modifier: Modifier = Modifier) {
    val k = tamano.value / 44f
    Box(
        modifier
            .size(tamano)
            .clip(CircleShape)
            .background(Organic.sage300),
        Alignment.BottomCenter,
    ) {
        Box(
            Modifier
                .size((17 * k).dp)
                .offset(y = (-19 * k).dp)
                .clip(CircleShape)
                .background(Organic.sage600)
        )
        Box(
            Modifier
                .size((34 * k).dp)
                .offset(y = (8 * k).dp)
                .clip(CircleShape)
                .background(Organic.sage500)
        )
    }
}

@Composable
private fun BarraPerfil(vm: MainViewModel) {
    val sesion = vm.session
    val primerNombre = sesion?.nombre.orEmpty().trim().substringBefore(' ')
    val direccion = sesion?.direccion.orEmpty().trim()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Organic.gutter, end = Organic.gutter, top = 14.dp),
    ) {
        AvatarPlaceholder(44.dp)
        Column(Modifier.weight(1f)) {
            Text(
                if (primerNombre.isEmpty()) "Hola" else "Hola, $primerNombre",
                style = MaterialTheme.typography.titleMedium,
                color = Organic.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Outlined.LocationOn, null, Modifier.size(13.dp), tint = Organic.accent)
                Text(
                    direccion.ifEmpty { "Agregá tu dirección" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Organic.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (vm.cantidadCarrito > 0) {
            IconButton(onClick = { vm.irA(Pantalla.Carrito) }) {
                BadgedBox(badge = {
                    Badge(containerColor = Organic.accent, contentColor = Organic.bg) {
                        Text("${vm.cantidadCarrito}")
                    }
                }) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito", tint = Organic.text)
                }
            }
        }
    }
}

/* ── buscador ────────────────────────────────────────────────────────── */

@Composable
private fun Buscador(valor: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(Organic.pill)
            .background(Organic.surface)
            .padding(horizontal = 16.dp),
    ) {
        Icon(Icons.Outlined.Search, null, Modifier.size(18.dp), tint = Organic.textFaint)
        BasicTextField(
            value = valor,
            onValueChange = onChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Organic.text),
            cursorBrush = SolidColor(Organic.accent),
            modifier = Modifier.weight(1f),
            decorationBox = { campo ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (valor.isEmpty()) {
                        Text(
                            "Buscá restaurantes o comidas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Organic.textFaint,
                            maxLines = 1,
                        )
                    }
                    campo()
                }
            },
        )
        if (valor.isNotEmpty()) {
            Icon(
                Icons.Filled.Clear,
                contentDescription = "Borrar búsqueda",
                tint = Organic.textFaint,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable { onChange("") },
            )
        }
    }
}

/* ── antojos ─────────────────────────────────────────────────────────── */

@Composable
private fun FilaAntojos(
    categorias: List<String>,
    categoria: String?,
    soloFavoritos: Boolean,
    onTodos: () -> Unit,
    onCategoria: (String) -> Unit,
    onFavoritos: () -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(start = Organic.gutter, end = Organic.gutter, top = 6.dp, bottom = 6.dp),
    ) {
        item {
            Antojo("Todos", Organic.neutral500, categoria == null && !soloFavoritos, onTodos)
        }
        items(categorias.size) { i ->
            val c = categorias[i]
            Antojo(c, Organic.puntos[i % Organic.puntos.size], categoria == c && !soloFavoritos) { onCategoria(c) }
        }
        item {
            Antojo("Favoritos", Organic.accent, soloFavoritos, onFavoritos)
        }
    }
}

@Composable
private fun Antojo(etiqueta: String, punto: Color, seleccionado: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .width(74.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(if (seleccionado) Organic.accent100 else Organic.surface)
                .then(if (seleccionado) Modifier.border(2.5.dp, Organic.accent, CircleShape) else Modifier),
        ) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(punto)
            )
        }
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = if (seleccionado) Organic.accent800 else Organic.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/* ── tarjeta de restaurante ──────────────────────────────────────────── */

@Composable
private fun TarjetaRestaurante(
    restaurante: Restaurante,
    favorito: Boolean,
    onFavorito: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoria = restaurante.categorias.joinToString(" · ") { it.replaceFirstChar(Char::uppercaseChar) }
    val direccion = restaurante.direccion?.trim().orEmpty()

    Tarjeta(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .alpha(if (restaurante.abierto) 1f else 0.6f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BloqueFoto(
                restaurante.id,
                Modifier
                    .width(104.dp)
                    .fillMaxHeight()
                    .defaultMinSize(minHeight = 104.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        restaurante.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = Organic.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = if (favorito) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (favorito) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (favorito) Organic.accent else Organic.textFaint,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onFavorito),
                    )
                }
                if (categoria.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = Organic.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (direccion.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(Icons.Outlined.LocationOn, null, Modifier.size(13.dp), tint = Organic.accent)
                        Text(
                            direccion,
                            style = MaterialTheme.typography.bodySmall,
                            color = Organic.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                val detalle = listOfNotNull(
                    textoEnvio(restaurante),
                    restaurante.pedidoMinimo.takeIf { it > 0 }?.let { "Mín. ${precio(it)}" },
                    restaurante.horarioHoy,
                ).joinToString(" · ")
                Text(
                    detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Organic.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    restaurante.precioDesde?.let { ChipAcento("Desde ${precio(it)}") }
                    if (restaurante.abierto) ChipSalvia("Abierto") else ChipNeutro("Cerrado")
                }
            }
        }
    }
}
