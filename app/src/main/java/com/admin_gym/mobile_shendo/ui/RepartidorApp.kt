package com.admin_gym.mobile_shendo.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.admin_gym.mobile_shendo.data.Pedido
import com.admin_gym.mobile_shendo.ui.theme.Organic
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Cara del repartidor: pedidos para tomar, mis entregas y perfil. */
@Composable
fun RepartidorApp(vm: MainViewModel) {
    var pestania by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.cargarRepartidor() }
    DisposableEffect(Unit) {
        val avisos = vm.escucharAvisos("repartidor/") { vm.cargarRepartidor(silencioso = true) }
        onDispose { avisos.cancel() }
    }
    // Los pedidos nuevos aparecen solos mientras la app está abierta
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)  // respaldo: los cambios llegan al instante por el WebSocket
            vm.cargarRepartidor(silencioso = true)
        }
    }
    LaunchedEffect(vm.mensaje) {
        vm.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.mensajeMostrado()
        }
    }

    Scaffold(
        containerColor = Organic.bg,
        bottomBar = { BottomNavRepartidor(pestania) { pestania = it } },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (pestania) {
                0 -> DisponiblesScreen(vm)
                1 -> MisEntregasScreen(vm)
                else -> PerfilScreen(vm)
            }
        }
    }
}

/* ── Disponibles ────────────────────────────────────────────────────── */

@Composable
private fun DisponiblesScreen(vm: MainViewModel) {
    val disponible = vm.estadoRepartidor?.disponible == true
    val ocupado = vm.accionEnPedido != null

    Column(Modifier.fillMaxSize()) {
        Encabezado("Disponibles")
        ListaRefrescable(loading = vm.loading, onRefresh = { vm.cargarRepartidor() }, modifier = Modifier.weight(1f)) {
            item {
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (disponible) "Disponible" else "En pausa",
                                style = MaterialTheme.typography.titleMedium,
                                color = Organic.text,
                            )
                            vm.estadoRepartidor?.vehiculo?.takeIf { it.isNotBlank() }?.let {
                                Text("Vehículo: $it", style = MaterialTheme.typography.bodySmall, color = Organic.textMuted)
                            }
                        }
                        Switch(
                            checked = disponible,
                            onCheckedChange = { vm.alternarDisponible() },
                            enabled = vm.estadoRepartidor != null && !ocupado,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Organic.accent,
                                checkedThumbColor = Organic.bg,
                            ),
                        )
                    }
                    if (!disponible) {
                        Spacer(Modifier.height(Organic.space3))
                        Aviso("Activá tu disponibilidad para tomar pedidos")
                    }
                }
            }
            listaOVacio(
                items = vm.disponibles,
                loading = vm.loading,
                textoVacio = "No hay pedidos para tomar ahora",
                key = { it.id },
            ) { pedido ->
                PedidoDisponible(pedido, puedeTomar = disponible && !ocupado, onTomar = { vm.tomarPedido(pedido) })
            }
        }
    }
}

@Composable
private fun PedidoDisponible(pedido: Pedido, puedeTomar: Boolean, onTomar: () -> Unit) {
    Tarjeta {
        CabeceraPedido(pedido)
        Spacer(Modifier.height(Organic.space3))
        Lugar("Retirás en", pedido.restaurante, pedido.restauranteDireccion)
        Spacer(Modifier.height(Organic.space2))
        // Antes de tomarlo el back manda solo la calle; la dirección exacta aparece en Mis entregas.
        Lugar("Zona de entrega", null, pedido.direccionEntrega)
        Spacer(Modifier.height(Organic.space3))
        ResumenItems(pedido)
        Spacer(Modifier.height(Organic.space3))
        BotonPrincipal("Tomar pedido", onClick = onTomar, habilitado = puedeTomar)
    }
}

/* ── Mis entregas ───────────────────────────────────────────────────── */

@Composable
private fun MisEntregasScreen(vm: MainViewModel) {
    Column(Modifier.fillMaxSize()) {
        Encabezado("Mis entregas")
        ListaRefrescable(loading = vm.loading, onRefresh = { vm.cargarRepartidor() }, modifier = Modifier.weight(1f)) {
            listaOVacio(
                items = vm.misEntregas,
                loading = vm.loading,
                textoVacio = "Todavía no tomaste pedidos",
                key = { it.id },
            ) { pedido ->
                EntregaCard(
                    pedido = pedido,
                    ocupado = vm.accionEnPedido != null,
                    cargando = vm.accionEnPedido == pedido.id,
                    onRetirado = { vm.marcarEnCamino(pedido) },
                    onSoltar = { vm.soltarPedido(pedido) },
                    onEntregado = { vm.marcarEntregado(pedido) },
                )
            }
        }
    }
}

@Composable
private fun EntregaCard(
    pedido: Pedido,
    ocupado: Boolean,
    cargando: Boolean,
    onRetirado: () -> Unit,
    onSoltar: () -> Unit,
    onEntregado: () -> Unit,
) {
    val context = LocalContext.current
    Tarjeta {
        CabeceraPedido(pedido)
        Spacer(Modifier.height(Organic.space3))

        Fila(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { Lugar("Retirás en", pedido.restaurante, pedido.restauranteDireccion) }
            pedido.restauranteTelefono?.takeIf { it.isNotBlank() }?.let {
                BotonLlamar("Llamar al restaurante") { llamar(context, it) }
            }
        }
        pedido.cliente?.let { cliente ->
            Spacer(Modifier.height(Organic.space2))
            Fila(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Cliente", style = MaterialTheme.typography.labelSmall, color = Organic.textMuted)
                    Text(cliente.nombre, style = MaterialTheme.typography.bodyMedium, color = Organic.text)
                }
                cliente.telefono?.takeIf { it.isNotBlank() }?.let {
                    BotonLlamar("Llamar al cliente") { llamar(context, it) }
                }
            }
        }
        Spacer(Modifier.height(Organic.space2))
        Fila(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { Lugar("Entregás en", null, pedido.direccionEntrega) }
            pedido.direccionEntrega?.takeIf { it.isNotBlank() }?.let {
                OutlinedButton(onClick = { abrirMapa(context, it) }, shape = Organic.pill) {
                    Text("Cómo llegar", style = MaterialTheme.typography.labelLarge, color = Organic.accent800)
                }
            }
        }

        Spacer(Modifier.height(Organic.space3))
        ResumenItems(pedido)

        if (!pedido.terminado) {
            Spacer(Modifier.height(Organic.space3))
            if (pedido.enCamino) {
                BotonPrincipal("Entregado", onClick = onEntregado, habilitado = !ocupado, cargando = cargando)
            } else {
                BotonPrincipal("Ya lo retiré", onClick = onRetirado, habilitado = !ocupado, cargando = cargando)
                Spacer(Modifier.height(Organic.space2))
                OutlinedButton(
                    onClick = onSoltar,
                    enabled = !ocupado,
                    shape = Organic.pill,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text("Soltar pedido", style = MaterialTheme.typography.labelLarge, color = Organic.textMuted)
                }
            }
        }
    }
}

/* ── piezas compartidas de las tarjetas ─────────────────────────────── */

@Composable
private fun Fila(modifier: Modifier = Modifier, contenido: @Composable RowScope.() -> Unit) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, content = contenido)
}

@Composable
private fun CabeceraPedido(pedido: Pedido) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Pedido #${pedido.id}", style = MaterialTheme.typography.titleMedium, color = Organic.text)
            pedido.fecha?.let {
                Text(fechaLocalRepartidor(it), style = MaterialTheme.typography.bodySmall, color = Organic.textMuted)
            }
        }
        Spacer(Modifier.width(Organic.space2))
        when {
            pedido.entregado -> ChipSalvia("Entregado")
            pedido.terminado -> ChipNeutro(pedido.estado.trim().replaceFirstChar { it.uppercase() })
            pedido.enCamino -> ChipAcento("En camino")
            else -> ChipNeutro("Por retirar")
        }
    }
}

/** "Retirás en" + nombre y dirección del lugar. */
@Composable
private fun Lugar(titulo: String, nombre: String?, direccion: String?) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = Organic.textMuted,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp),
        )
        Spacer(Modifier.width(Organic.space1))
        Column {
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = Organic.textMuted)
            val texto = listOfNotNull(nombre, direccion).filter { it.isNotBlank() }.joinToString(" · ")
            Text(
                texto.ifEmpty { "Sin dirección" },
                style = MaterialTheme.typography.bodyMedium,
                color = Organic.text,
            )
        }
    }
}

@Composable
private fun ResumenItems(pedido: Pedido) {
    HorizontalDivider(color = Organic.divider)
    Spacer(Modifier.height(Organic.space2))
    Column(verticalArrangement = Arrangement.spacedBy(Organic.space1)) {
        pedido.items.forEach {
            Text("${it.cantidad} × ${it.nombre}", style = MaterialTheme.typography.bodyMedium, color = Organic.text)
        }
    }
    Spacer(Modifier.height(Organic.space2))
    Row(Modifier.fillMaxWidth()) {
        Text("Total", style = MaterialTheme.typography.titleSmall, color = Organic.text, modifier = Modifier.weight(1f))
        Text(precio(pedido.total), style = MaterialTheme.typography.titleSmall, color = Organic.text)
    }
    pedido.metodoPago?.takeIf { it.isNotBlank() }?.let {
        val estadoPago = if (pedido.pagado) "Pagado" else "Se cobra al entregar"
        Text(
            "Pago: $it · $estadoPago",
            style = MaterialTheme.typography.bodySmall,
            color = Organic.textMuted,
            modifier = Modifier.padding(top = Organic.space1),
        )
    }
}

@Composable
private fun BotonLlamar(descripcion: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Outlined.Phone, contentDescription = descripcion, tint = Organic.accent)
    }
}

/* ── intents ────────────────────────────────────────────────────────── */

// ACTION_DIAL abre el marcador sin pedir permiso
private fun llamar(context: Context, telefono: String) =
    abrir(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono")), "No hay app para llamar")

private fun abrirMapa(context: Context, direccion: String) =
    abrir(context, Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(direccion))), "No hay app de mapas")

private fun abrir(context: Context, intent: Intent, sinApp: String) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, sinApp, Toast.LENGTH_SHORT).show()
    }
}

/** ISO en UTC a hora local "01/10 19:33"; si no se puede parsear, el texto crudo. */
private fun fechaLocalRepartidor(iso: String): String = try {
    val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val fecha = entrada.parse(iso.take(19))
    if (fecha == null) iso else SimpleDateFormat("dd/MM HH:mm", Locale.US).format(fecha)
} catch (e: Exception) {
    iso
}

/* ── navegación inferior (mismo look que ClienteApp) ────────────────── */

private data class PestaniaRepartidor(val etiqueta: String, val icono: ImageVector)

private val pestaniasRepartidor = listOf(
    PestaniaRepartidor("Disponibles", Icons.Filled.Home),
    PestaniaRepartidor("Mis entregas", Icons.AutoMirrored.Filled.List),
    PestaniaRepartidor("Perfil", Icons.Filled.Person),
)

@Composable
private fun BottomNavRepartidor(seleccionada: Int, onSelect: (Int) -> Unit) {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Organic.divider)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(Organic.surface)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            pestaniasRepartidor.forEachIndexed { i, p ->
                val color = if (i == seleccionada) Organic.accent else Organic.textFaint
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(i) }
                        .padding(vertical = 6.dp),
                ) {
                    Icon(p.icono, contentDescription = p.etiqueta, modifier = Modifier.size(22.dp), tint = color)
                    Text(
                        p.etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                    )
                }
            }
        }
    }
}
