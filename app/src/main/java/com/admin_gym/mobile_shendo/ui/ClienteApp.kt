package com.admin_gym.mobile_shendo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.admin_gym.mobile_shendo.ui.theme.Organic

@Composable
fun ClienteApp(vm: MainViewModel) {
    val pantalla = vm.pantalla
    val snackbar = remember { SnackbarHostState() }
    val esPestania = pantalla == Pantalla.Restaurantes ||
        pantalla == Pantalla.MisPedidos ||
        pantalla == Pantalla.Perfil

    BackHandler(enabled = vm.pila.size > 1) { vm.volver() }

    LaunchedEffect(vm.mensaje) {
        vm.mensaje?.let {
            snackbar.showSnackbar(it)
            vm.mensajeMostrado()
        }
    }

    Scaffold(
        containerColor = Organic.bg,
        bottomBar = {
            when {
                esPestania -> BottomNav(
                    pantalla = pantalla,
                    onSelect = { vm.irAPestania(it) },
                )
                // En el menú, si hay cosas en el carrito de este restaurante, botón para ir a pagar
                pantalla is Pantalla.Menu && vm.cantidadCarrito > 0 &&
                    vm.carritoRestaurante?.id == pantalla.restaurante.id -> Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Organic.bg)
                        .navigationBarsPadding()
                        .padding(horizontal = Organic.gutter, vertical = 12.dp),
                ) {
                    BotonPrincipal(
                        texto = "Ver pedido (${vm.cantidadCarrito}) · ${precio(vm.totalCarrito)}",
                        onClick = { vm.irA(Pantalla.Carrito) },
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (pantalla) {
                Pantalla.Restaurantes -> HomeScreen(vm)
                Pantalla.MisPedidos -> MisPedidosScreen(vm)
                Pantalla.Perfil -> PerfilScreen(vm)
                is Pantalla.Menu -> MenuScreen(vm, pantalla.restaurante)
                Pantalla.Carrito -> CarritoScreen(vm)
            }
        }
    }

    vm.cambioPendiente?.let { cambio ->
        DialogoCambioDeRestaurante(
            cambio = cambio,
            onConfirmar = { vm.confirmarCambioDeRestaurante() },
            onCancelar = { vm.cancelarCambioDeRestaurante() },
        )
    }
}

/** El carrito es de un solo restaurante: antes de vaciarlo para sumar algo de otro, se pregunta. */
@Composable
private fun DialogoCambioDeRestaurante(
    cambio: CambioDeRestaurante,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = Organic.bg,
        shape = Organic.radiusLg,
        title = {
            Text("¿Empezar un pedido nuevo?", style = MaterialTheme.typography.headlineSmall, color = Organic.text)
        },
        text = {
            Text(
                "Tenés un pedido en ${cambio.actual.nombre}. Si seguís, lo vaciamos y empezamos uno en " +
                    "${cambio.nuevo.nombre} con ${cambio.producto.nombre}.",
                style = MaterialTheme.typography.bodyMedium,
                color = Organic.textMuted,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Vaciar y agregar", style = MaterialTheme.typography.labelLarge, color = Organic.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", style = MaterialTheme.typography.labelLarge, color = Organic.textMuted)
            }
        },
    )
}

/* ── navegación inferior (como el mock) ─────────────────────────────── */

private data class Pestania(val etiqueta: String, val icono: ImageVector, val destino: Pantalla)

private val pestanias = listOf(
    Pestania("Inicio", Icons.Filled.Home, Pantalla.Restaurantes),
    Pestania("Pedidos", Icons.AutoMirrored.Filled.List, Pantalla.MisPedidos),
    Pestania("Perfil", Icons.Filled.Person, Pantalla.Perfil),
)

@Composable
private fun BottomNav(pantalla: Pantalla, onSelect: (Pantalla) -> Unit) {
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
            pestanias.forEach { p ->
                val activa = p.destino == pantalla
                val color = if (activa) Organic.accent else Organic.textFaint
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(p.destino) }
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
