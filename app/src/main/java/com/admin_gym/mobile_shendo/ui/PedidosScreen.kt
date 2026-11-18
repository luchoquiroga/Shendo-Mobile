package com.admin_gym.mobile_shendo.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.admin_gym.mobile_shendo.data.Pedido
import com.admin_gym.mobile_shendo.data.TipoEntrega
import com.admin_gym.mobile_shendo.ui.theme.Organic
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Historial de pedidos del cliente. */
@Composable
fun MisPedidosScreen(vm: MainViewModel) {
    LaunchedEffect(Unit) { vm.cargarMisPedidos() }
    DisposableEffect(Unit) {
        val avisos = vm.escucharAvisos("cliente/") { vm.cargarMisPedidos(silencioso = true) }
        onDispose { avisos.cancel() }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)  // respaldo: los cambios llegan al instante por el WebSocket
            vm.cargarMisPedidos(silencioso = true)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Encabezado("Mis pedidos")
        ListaRefrescable(loading = vm.loading, onRefresh = vm::cargarMisPedidos, modifier = Modifier.weight(1f)) {
            listaOVacio(
                items = vm.misPedidos,
                loading = vm.loading,
                textoVacio = "Todavía no hiciste ningún pedido",
                key = { it.id },
            ) { PedidoCard(it) }
        }
    }
}

@Composable
private fun PedidoCard(pedido: Pedido) {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BloqueFoto(pedido.id, Modifier.size(44.dp))
            Spacer(Modifier.width(Organic.space3))
            Column(Modifier.weight(1f)) {
                Text(
                    pedido.restaurante ?: "Pedido #${pedido.id}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Organic.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val fecha = pedido.fecha?.let { " · ${fechaLocal(it)}" }.orEmpty()
                Text(
                    "Pedido #${pedido.id}$fecha",
                    style = MaterialTheme.typography.bodySmall,
                    color = Organic.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(Organic.space2))
            ChipEstado(pedido.estado)
        }

        Spacer(Modifier.height(Organic.space3))
        HorizontalDivider(color = Organic.divider)
        Spacer(Modifier.height(Organic.space2))

        Column(verticalArrangement = Arrangement.spacedBy(Organic.space1)) {
            pedido.items.forEach { item ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${item.cantidad} × ${item.nombre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Organic.text,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(Organic.space2))
                    Text(precio(item.subtotal), style = MaterialTheme.typography.bodyMedium, color = Organic.text)
                }
            }
        }

        Spacer(Modifier.height(Organic.space2))
        HorizontalDivider(color = Organic.divider)
        Spacer(Modifier.height(Organic.space2))

        if (pedido.costoEnvio > 0) {
            Row(Modifier.fillMaxWidth().padding(bottom = Organic.space1)) {
                Text("Envío", style = MaterialTheme.typography.bodyMedium, color = Organic.textMuted, modifier = Modifier.weight(1f))
                Text(precio(pedido.costoEnvio), style = MaterialTheme.typography.bodyMedium, color = Organic.textMuted)
            }
        }
        Row(Modifier.fillMaxWidth()) {
            Text("Total", style = MaterialTheme.typography.titleSmall, color = Organic.text, modifier = Modifier.weight(1f))
            Text(precio(pedido.total), style = MaterialTheme.typography.titleSmall, color = Organic.text)
        }

        Spacer(Modifier.height(Organic.space3))
        if (pedido.tipoEntrega == TipoEntrega.RETIRO) ChipSalvia("Retiro en el local")
        else ChipAcento("Envío a domicilio")

        pedido.direccionEntrega?.takeIf { it.isNotBlank() && pedido.tipoEntrega == TipoEntrega.DOMICILIO }?.let {
            Spacer(Modifier.height(Organic.space2))
            Spacer(Modifier.height(Organic.space3))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = Organic.textMuted,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(Organic.space1))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Organic.textMuted)
            }
        }
        pedido.metodoPago?.takeIf { it.isNotBlank() }?.let {
            val estadoPago = if (pedido.pagado) " · Pagado" else " · Se paga al recibir"
            Text(
                "Pago: $it$estadoPago",
                style = MaterialTheme.typography.bodySmall,
                color = Organic.textMuted,
                modifier = Modifier.padding(top = Organic.space1),
            )
        }

        pedido.repartidor?.let { rep ->
            Spacer(Modifier.height(Organic.space2))
            if (pedido.terminado) {
                Text(
                    if (pedido.entregado) "Entregado por ${rep.nombre}" else "Repartidor: ${rep.nombre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Organic.textMuted,
                )
            } else {
                val context = LocalContext.current
                val tel = rep.telefono?.takeIf { it.isNotBlank() }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Organic.radiusMd)
                        .background(Organic.accent100)
                        .padding(start = Organic.space3, top = Organic.space1, bottom = Organic.space1),
                ) {
                    Column(Modifier.weight(1f)) {
                        if (pedido.enCamino) {
                            Text("¡Está en camino!", style = MaterialTheme.typography.titleSmall, color = Organic.accent800)
                        }
                        Text(
                            "Te lo lleva ${rep.nombre}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Organic.accent800,
                        )
                        rep.vehiculo?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = Organic.accent800)
                        }
                    }
                    if (tel != null) {
                        IconButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))) }) {
                            Icon(
                                Icons.Outlined.Phone,
                                contentDescription = "Llamar a ${rep.nombre}",
                                tint = Organic.accent800,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipEstado(estado: String) {
    val texto = estado.trim().replaceFirstChar { it.uppercase() }
    when (estado.trim().lowercase()) {
        "pendiente", "en camino" -> ChipAcento(texto)
        "entregado", "listo", "completado" -> ChipSalvia(texto)
        else -> ChipNeutro(texto)
    }
}

/** ISO en UTC ("2026-10-01T22:33:00.123Z") a hora local "01/10 19:33"; si no se puede parsear, el texto crudo. */
private fun fechaLocal(iso: String): String = try {
    val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val fecha = entrada.parse(iso.take(19))
    if (fecha == null) iso else SimpleDateFormat("dd/MM HH:mm", Locale.US).format(fecha)
} catch (e: Exception) {
    iso
}
