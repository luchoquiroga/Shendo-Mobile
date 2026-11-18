package com.admin_gym.mobile_shendo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.admin_gym.mobile_shendo.ui.theme.Organic
import java.util.Locale

// Piezas comunes del diseño "Organic" que usan todas las pantallas.
// Colores, radios y espaciados salen de Organic (ui/theme/Color.kt), nunca a mano.

private val pesos = Locale.forLanguageTag("es-AR")

fun precio(valor: Double): String = String.format(pesos, "$ %,.2f", valor)

/** "Envío gratis" / "Envío $ 500,00". */
fun textoEnvio(restaurante: com.admin_gym.mobile_shendo.data.Restaurante): String =
    if (restaurante.costoEnvio > 0) "Envío ${precio(restaurante.costoEnvio)}" else "Envío gratis"

/** Padding de las listas: margen lateral del mock y aire abajo. */
val paddingLista = PaddingValues(start = Organic.gutter, end = Organic.gutter, top = 8.dp, bottom = 24.dp)

/** Encabezado de pantalla secundaria: flecha para volver + título en Caprasimo. */
@Composable
fun Encabezado(titulo: String, onVolver: (() -> Unit)? = null, acciones: @Composable () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (onVolver == null) Organic.gutter else 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
            .height(52.dp),
    ) {
        if (onVolver != null) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Organic.text)
            }
        }
        Text(
            titulo,
            style = MaterialTheme.typography.headlineSmall,
            color = Organic.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        acciones()
    }
}

/** Tarjeta del mock: fondo surface, radio grande, sin sombra. */
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val forma = Organic.radiusLg
    Surface(
        shape = forma,
        color = Organic.surface,
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Column(Modifier.padding(Organic.space3), content = contenido)
    }
}

/** Etiqueta tipo pill ("Desde $ 900", "Abierto", "Pendiente"). */
@Composable
fun Chip(texto: String, fondo: Color, color: Color, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
        modifier = modifier
            .clip(Organic.pill)
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Variantes de Chip con los colores del mock. */
@Composable
fun ChipAcento(texto: String) = Chip(texto, Organic.accent100, Organic.accent800)

@Composable
fun ChipSalvia(texto: String) = Chip(texto, Organic.sage100, Organic.sage800)

@Composable
fun ChipNeutro(texto: String) = Chip(texto, Organic.neutral200, Organic.neutral800)

/** Botón principal: pill terracota a todo el ancho. Con [cargando] muestra un spinner y no se puede tocar. */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = Organic.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = Organic.accent,
            contentColor = Organic.bg,
            disabledContainerColor = Organic.neutral300,
            disabledContentColor = Organic.neutral700,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Organic.bg)
        } else {
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Campo de texto con los colores del tema (fondo surface, borde terracota al enfocar). */
@Composable
fun CampoTexto(
    valor: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    // Pide mayúsculas al teclado: transformar el texto en onChange pierde letras mientras el IME compone.
    mayusculas: Boolean = false,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        shape = Organic.radiusMd,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            capitalization = if (mayusculas) KeyboardCapitalization.Characters else KeyboardCapitalization.Unspecified,
            keyboardType = teclado,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Organic.surface,
            unfocusedContainerColor = Organic.surface,
            focusedBorderColor = Organic.accent,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = Organic.accent700,
            cursorColor = Organic.accent,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Bloque de color que ocupa el lugar de la foto (no hay imágenes de restaurantes ni productos).
 * [semilla] elige el tinte, para que cada restaurante tenga siempre el mismo.
 */
@Composable
fun BloqueFoto(semilla: Long, modifier: Modifier = Modifier, contenido: @Composable BoxScope.() -> Unit = {}) {
    val tinte = Organic.tintes[(semilla % Organic.tintes.size).toInt().let { if (it < 0) -it else it }]
    Box(modifier.clip(Organic.radiusLg).background(tinte)) {
        Box(
            Modifier
                .size(74.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-14).dp, y = 20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.30f))
        )
        contenido()
    }
}

/**
 * Foto de un producto. Si hay [url] se carga encima del [BloqueFoto]; mientras carga, o si falla,
 * se ve el bloque de color (el mismo que cuando no hay foto).
 */
@Composable
fun FotoProducto(url: String?, semilla: Long, modifier: Modifier = Modifier) {
    BloqueFoto(semilla, modifier) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** − cantidad + (en el menú y el carrito). */
@Composable
fun SelectorCantidad(
    cantidad: Int,
    habilitado: Boolean,
    onAgregar: () -> Unit,
    onQuitar: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (cantidad > 0) {
            BotonRedondo(onClick = onQuitar, habilitado = true, descripcion = "Quitar") {
                Text("−", style = MaterialTheme.typography.titleMedium, color = Organic.accent800)
            }
            Text(
                "$cantidad",
                style = MaterialTheme.typography.titleSmall,
                color = Organic.text,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(30.dp),
            )
        }
        BotonRedondo(onClick = onAgregar, habilitado = habilitado, descripcion = "Agregar") {
            Icon(Icons.Filled.Add, contentDescription = null, tint = if (habilitado) Organic.accent800 else Organic.neutral500)
        }
    }
}

@Composable
private fun BotonRedondo(
    onClick: () -> Unit,
    habilitado: Boolean,
    descripcion: String,
    tamanio: Dp = 36.dp,
    contenido: @Composable BoxScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(tamanio)
            .clip(CircleShape)
            .background(if (habilitado) Organic.accent100 else Organic.neutral200)
            .clickable(enabled = habilitado, onClickLabel = descripcion, onClick = onClick),
        content = contenido,
    )
}

// ---------- Listas ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaRefrescable(
    loading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = paddingLista,
    contenido: LazyListScope.() -> Unit,
) {
    PullToRefreshBox(isRefreshing = loading, onRefresh = onRefresh, modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = contenido,
        )
    }
}

/** Los items, o un texto si la lista está vacía y no está cargando. */
fun <T> LazyListScope.listaOVacio(
    items: List<T>,
    loading: Boolean,
    textoVacio: String,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit,
) {
    if (items.isEmpty() && !loading) {
        item { TextoVacio(textoVacio) }
    } else {
        items(items, key = key) { itemContent(it) }
    }
}

@Composable
fun TextoVacio(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = Organic.textMuted,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
    )
}

/** Aviso destacado (restaurante cerrado, etc.). */
@Composable
fun Aviso(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = Organic.accent800,
        modifier = modifier
            .fillMaxWidth()
            .clip(Organic.radiusMd)
            .background(Organic.accent100)
            .padding(Organic.space3),
    )
}
