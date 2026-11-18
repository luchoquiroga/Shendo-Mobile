package com.admin_gym.mobile_shendo.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.admin_gym.mobile_shendo.data.ItemCarrito
import com.admin_gym.mobile_shendo.data.MetodoPago
import com.admin_gym.mobile_shendo.data.TipoEntrega
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.admin_gym.mobile_shendo.ui.theme.Organic

/** Carrito: items, dirección de entrega, forma de pago y confirmación. */
@Composable
fun CarritoScreen(vm: MainViewModel) {
    val restaurante = vm.carritoRestaurante

    Column(Modifier.fillMaxSize()) {
        Encabezado("Tu pedido", onVolver = { vm.volver() })

        if (restaurante == null || vm.carrito.isEmpty()) {
            TextoVacio("Tu carrito está vacío.\nElegí un restaurante para empezar.")
            return@Column
        }

        var direccion by rememberSaveable { mutableStateOf(vm.session?.direccion.orEmpty()) }
        // Se guarda el name del enum; null = todavía no eligió.
        var metodoElegido by rememberSaveable { mutableStateOf<String?>(null) }
        val metodoPago = MetodoPago.entries.firstOrNull { it.name == metodoElegido }
        var tipoElegido by rememberSaveable { mutableStateOf(TipoEntrega.DOMICILIO.name) }
        val tipoEntrega = TipoEntrega.valueOf(tipoElegido)

        LazyColumn(
            contentPadding = paddingLista,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding(),
        ) {
            item {
                Text(
                    restaurante.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    color = Organic.text,
                )
            }
            item {
                Tarjeta {
                    vm.carrito.forEachIndexed { i, item ->
                        if (i > 0) HorizontalDivider(color = Organic.divider, modifier = Modifier.padding(vertical = Organic.space2))
                        FilaItem(
                            item = item,
                            onAgregar = { vm.agregar(restaurante, item.producto) },
                            onQuitar = { vm.quitar(item.producto) },
                        )
                    }
                    HorizontalDivider(color = Organic.divider, modifier = Modifier.padding(vertical = Organic.space2))
                    FilaMonto("Subtotal", precio(vm.totalCarrito), MaterialTheme.typography.bodyMedium, Organic.textMuted)
                    if (tipoEntrega == TipoEntrega.DOMICILIO) {
                        val envio = vm.costoEnvio(tipoEntrega)
                        FilaMonto(
                            "Envío",
                            if (envio > 0) precio(envio) else "Gratis",
                            MaterialTheme.typography.bodyMedium,
                            Organic.textMuted,
                        )
                    }
                    Spacer(Modifier.height(Organic.space2))
                    FilaMonto("Total", precio(vm.totalConEnvio(tipoEntrega)), MaterialTheme.typography.titleLarge, Organic.text)
                }
            }
            if (vm.faltaParaMinimo > 0) {
                item {
                    Aviso("El pedido mínimo es ${precio(restaurante.pedidoMinimo)}. Te faltan ${precio(vm.faltaParaMinimo)} para poder confirmar.")
                }
            }
            item {
                Column {
                    Text(
                        "¿Cómo lo querés recibir?",
                        style = MaterialTheme.typography.titleMedium,
                        color = Organic.text,
                        modifier = Modifier.padding(bottom = Organic.space2),
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Organic.space2),
                        modifier = Modifier.selectableGroup(),
                    ) {
                        TipoEntrega.entries.forEach { tipo ->
                            OpcionPago(
                                texto = tipo.texto,
                                seleccionado = tipo == tipoEntrega,
                                onClick = { tipoElegido = tipo.name },
                            )
                        }
                    }
                }
            }
            if (tipoEntrega == TipoEntrega.DOMICILIO) {
                item {
                    Text(
                        "Dirección de entrega",
                        style = MaterialTheme.typography.titleMedium,
                        color = Organic.text,
                        modifier = Modifier.padding(bottom = Organic.space2),
                    )
                    CampoTexto(
                        valor = direccion,
                        onChange = { direccion = it },
                        label = "Dirección",
                    )
                }
            }
            item {
                Column {
                    Text(
                        "¿Cómo vas a pagar?",
                        style = MaterialTheme.typography.titleMedium,
                        color = Organic.text,
                        modifier = Modifier.padding(bottom = Organic.space2),
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Organic.space2),
                        modifier = Modifier.selectableGroup(),
                    ) {
                        MetodoPago.entries.forEach { metodo ->
                            OpcionPago(
                                texto = metodo.texto,
                                seleccionado = metodo == metodoPago,
                                onClick = { metodoElegido = metodo.name },
                            )
                        }
                    }
                    Text(
                        "El pago se hace al recibir el pedido.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Organic.textMuted,
                        modifier = Modifier.padding(top = Organic.space2),
                    )
                }
            }
            item {
                BotonPrincipal(
                    texto = "Confirmar pedido · ${precio(vm.totalConEnvio(tipoEntrega))}",
                    onClick = { vm.confirmarPedido(tipoEntrega, direccion, metodoPago) },
                    habilitado = vm.faltaParaMinimo <= 0,
                    cargando = vm.enviandoPedido,
                )
            }
        }
    }
}

@Composable
private fun FilaItem(item: ItemCarrito, onAgregar: () -> Unit, onQuitar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f).padding(end = Organic.space2)) {
            Text(
                item.producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                color = Organic.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                precio(item.subtotal),
                style = MaterialTheme.typography.bodySmall,
                color = Organic.textMuted,
            )
        }
        SelectorCantidad(
            cantidad = item.cantidad,
            habilitado = true,
            onAgregar = onAgregar,
            onQuitar = onQuitar,
        )
    }
}

@Composable
private fun FilaMonto(
    etiqueta: String,
    monto: String,
    estilo: androidx.compose.ui.text.TextStyle,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, style = estilo, color = color, modifier = Modifier.weight(1f))
        Text(monto, style = estilo, color = color)
    }
}

/** Opción tipo radio en pastilla; la usan el selector de entrega y el de método de pago. */
@Composable
private fun OpcionPago(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = if (seleccionado) Organic.accent800 else Organic.text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(Organic.pill)
            .background(if (seleccionado) Organic.accent100 else Organic.surface)
            .border(2.dp, if (seleccionado) Organic.accent else Color.Transparent, Organic.pill)
            .selectable(selected = seleccionado, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = Organic.space4, vertical = Organic.space3),
    )
}
