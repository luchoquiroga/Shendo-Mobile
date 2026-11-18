package com.admin_gym.mobile_shendo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.admin_gym.mobile_shendo.data.Producto
import com.admin_gym.mobile_shendo.data.Restaurante
import com.admin_gym.mobile_shendo.ui.theme.Organic

/** Menú de un restaurante: cabecera, productos y selector de cantidad por producto. */
@Composable
fun MenuScreen(vm: MainViewModel, restaurante: Restaurante) {
    LaunchedEffect(restaurante.id) { vm.cargarMenu(restaurante) }

    val delCarrito = vm.carritoRestaurante?.id == restaurante.id

    Column(Modifier.fillMaxSize()) {
        Encabezado(restaurante.nombre, onVolver = { vm.volver() })
        ListaRefrescable(
            loading = vm.loading,
            onRefresh = { vm.cargarMenu(restaurante) },
            modifier = Modifier.weight(1f),
        ) {
            item { CabeceraRestaurante(restaurante) }
            item {
                Text(
                    "Menú",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Organic.text,
                )
            }
            listaOVacio(
                items = vm.menu,
                loading = vm.loading,
                textoVacio = "Este restaurante todavía no cargó productos",
                key = { it.id },
            ) { producto ->
                ProductoCard(
                    producto = producto,
                    cantidad = if (delCarrito) vm.cantidadEnCarrito(producto) else 0,
                    habilitado = restaurante.abierto && producto.stock != 0,
                    onAgregar = { vm.agregar(restaurante, producto) },
                    onQuitar = { vm.quitar(producto) },
                )
            }
        }
    }
}

@Composable
private fun CabeceraRestaurante(restaurante: Restaurante) {
    Column(verticalArrangement = Arrangement.spacedBy(Organic.space2)) {
        BloqueFoto(
            semilla = restaurante.id,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
        )
        if (restaurante.categorias.isNotEmpty()) {
            Text(
                restaurante.categorias.joinToString(" · ") { it.replaceFirstChar(Char::uppercaseChar) },
                style = MaterialTheme.typography.bodyMedium,
                color = Organic.textMuted,
            )
        }
        if (!restaurante.direccion.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = Organic.textFaint,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    restaurante.direccion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Organic.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = Organic.space1),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Organic.space2)) {
            if (restaurante.abierto) ChipSalvia("Abierto") else ChipNeutro("Cerrado")
            if (restaurante.precioDesde != null) {
                ChipAcento("Desde ${precio(restaurante.precioDesde)}")
            }
        }
        Text(
            textoEnvio(restaurante) + restaurante.horarioHoy?.let { " · $it" }.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = Organic.textMuted,
        )
        if (restaurante.pedidoMinimo > 0) {
            Text(
                "Pedido mínimo ${precio(restaurante.pedidoMinimo)}",
                style = MaterialTheme.typography.bodySmall,
                color = Organic.textMuted,
            )
        }
        if (!restaurante.abierto) {
            Aviso("Este restaurante está cerrado. Podés ver el menú, pero no hacer pedidos.")
        }
    }
}

@Composable
private fun ProductoCard(
    producto: Producto,
    cantidad: Int,
    habilitado: Boolean,
    onAgregar: () -> Unit,
    onQuitar: () -> Unit,
) {
    val stock = producto.stock
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FotoProducto(producto.imagenUrl, producto.id, Modifier.size(72.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(Organic.space1),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Organic.space3),
            ) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = Organic.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!producto.descripcion.isNullOrBlank()) {
                    Text(
                        producto.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Organic.textMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    precio(producto.precio),
                    style = MaterialTheme.typography.titleSmall,
                    color = Organic.accent800,
                )
                when {
                    stock == 0 -> ChipNeutro("Sin stock")
                    stock != null && stock <= 5 -> ChipAcento("Quedan $stock")
                }
            }
            SelectorCantidad(
                cantidad = cantidad,
                habilitado = habilitado,
                onAgregar = onAgregar,
                onQuitar = onQuitar,
            )
        }
    }
}
