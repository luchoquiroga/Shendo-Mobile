package com.admin_gym.mobile_shendo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.admin_gym.mobile_shendo.ui.theme.Organic

@Composable
fun PerfilScreen(vm: MainViewModel) {
    val sesion = vm.session
    val nombre = sesion?.nombre.orEmpty().trim()
    var editando by rememberSaveable { mutableStateOf(false) }
    var nombreEd by rememberSaveable { mutableStateOf("") }
    var telefonoEd by rememberSaveable { mutableStateOf("") }
    var direccionEd by rememberSaveable { mutableStateOf("") }
    var cambiandoClave by rememberSaveable { mutableStateOf(false) }
    var claveActual by rememberSaveable { mutableStateOf("") }
    var claveNueva by rememberSaveable { mutableStateOf("") }
    var editandoVehiculo by rememberSaveable { mutableStateOf(false) }
    var vehiculoEd by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        Encabezado("Tu perfil")
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Organic.gutter, vertical = 8.dp),
        ) {
            AvatarPlaceholder(88.dp)
            Spacer(Modifier.height(Organic.space3))
            Text(
                nombre.ifEmpty { "Tu cuenta" },
                style = MaterialTheme.typography.headlineSmall,
                color = Organic.text,
                textAlign = TextAlign.Center,
            )
            Text(
                sesion?.email.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = Organic.textMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Organic.space6))

            Tarjeta {
                if (editando) {
                    CampoTexto(nombreEd, { nombreEd = it }, "Nombre")
                    Spacer(Modifier.height(12.dp))
                    CampoTexto(telefonoEd, { telefonoEd = it }, "Teléfono", teclado = KeyboardType.Phone)
                    Spacer(Modifier.height(12.dp))
                    CampoTexto(direccionEd, { direccionEd = it }, "Dirección", imeAction = ImeAction.Done)
                    Text(
                        "El email no se puede cambiar",
                        style = MaterialTheme.typography.labelSmall,
                        color = Organic.textMuted,
                        modifier = Modifier.padding(top = Organic.space2),
                    )
                    Spacer(Modifier.height(Organic.space3))
                    BotonPrincipal(
                        "Guardar",
                        onClick = {
                            vm.guardarPerfil(nombreEd, telefonoEd, direccionEd) { ok -> if (ok) editando = false }
                        },
                        cargando = vm.guardandoPerfil,
                    )
                    TextButton(onClick = { editando = false }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Cancelar", color = Organic.accent800)
                    }
                } else {
                    FilaDato(Icons.Outlined.Email, "Email", sesion?.email)
                    HorizontalDivider(color = Organic.divider, modifier = Modifier.padding(vertical = Organic.space2))
                    FilaDato(Icons.Outlined.Phone, "Teléfono", sesion?.telefono)
                    HorizontalDivider(color = Organic.divider, modifier = Modifier.padding(vertical = Organic.space2))
                    FilaDato(Icons.Outlined.LocationOn, "Dirección", sesion?.direccion)
                    TextButton(
                        onClick = {
                            nombreEd = sesion?.nombre.orEmpty()
                            telefonoEd = sesion?.telefono.orEmpty()
                            direccionEd = sesion?.direccion.orEmpty()
                            editando = true
                        },
                        modifier = Modifier.align(Alignment.End),
                    ) { Text("Editar datos", color = Organic.accent800) }
                }
            }

            if (sesion?.esRepartidor == true) {
                Spacer(Modifier.height(Organic.space3))
                Tarjeta {
                    Text("Repartidor", style = MaterialTheme.typography.titleSmall, color = Organic.text)
                    Spacer(Modifier.height(Organic.space2))
                    if (editandoVehiculo) {
                        CampoTexto(vehiculoEd, { vehiculoEd = it }, "Vehículo (ej. moto, bici)", imeAction = ImeAction.Done)
                        Row(Modifier.align(Alignment.End)) {
                            TextButton(onClick = { editandoVehiculo = false }) { Text("Cancelar", color = Organic.accent800) }
                            TextButton(
                                enabled = vehiculoEd.isNotBlank(),
                                onClick = { vm.cambiarVehiculo(vehiculoEd); editandoVehiculo = false },
                            ) { Text("Guardar", color = Organic.accent800) }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Vehículo", style = MaterialTheme.typography.labelSmall, color = Organic.textMuted)
                                Text(
                                    vm.estadoRepartidor?.vehiculo?.ifBlank { null } ?: "—",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Organic.text,
                                )
                            }
                            TextButton(onClick = {
                                vehiculoEd = vm.estadoRepartidor?.vehiculo.orEmpty()
                                editandoVehiculo = true
                            }) { Text("Editar", color = Organic.accent800) }
                        }
                        vm.estadoRepartidor?.let {
                            if (it.disponible) ChipSalvia("Disponible") else ChipNeutro("En pausa")
                        }
                    }
                }
            }

            Spacer(Modifier.height(Organic.space3))
            Tarjeta {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { cambiandoClave = !cambiandoClave },
                ) {
                    Text(
                        "Cambiar contraseña",
                        style = MaterialTheme.typography.titleSmall,
                        color = Organic.text,
                        modifier = Modifier.weight(1f).padding(vertical = Organic.space2),
                    )
                    Text(if (cambiandoClave) "−" else "+", style = MaterialTheme.typography.titleMedium, color = Organic.accent800)
                }
                if (cambiandoClave) {
                    Spacer(Modifier.height(Organic.space2))
                    CampoTexto(
                        claveActual, { claveActual = it }, "Contraseña actual",
                        teclado = KeyboardType.Password, visualTransformation = PasswordVisualTransformation(),
                    )
                    Spacer(Modifier.height(12.dp))
                    CampoTexto(
                        claveNueva, { claveNueva = it }, "Contraseña nueva",
                        teclado = KeyboardType.Password, imeAction = ImeAction.Done,
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    Spacer(Modifier.height(Organic.space3))
                    BotonPrincipal(
                        "Cambiar contraseña",
                        onClick = {
                            vm.cambiarContrasena(claveActual, claveNueva) { ok ->
                                if (ok) { claveActual = ""; claveNueva = ""; cambiandoClave = false }
                            }
                        },
                        habilitado = claveActual.isNotEmpty() && claveNueva.isNotEmpty(),
                        cargando = vm.guardandoPerfil,
                    )
                }
            }

            Spacer(Modifier.height(Organic.space6))
            BotonCerrarSesion(onClick = { vm.logout() })
        }
    }
}

@Composable
private fun FilaDato(icono: ImageVector, etiqueta: String, valor: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(icono, contentDescription = null, tint = Organic.accent, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = Organic.textMuted)
            Text(
                valor?.trim().orEmpty().ifEmpty { "—" },
                style = MaterialTheme.typography.bodyMedium,
                color = Organic.text,
            )
        }
    }
}

@Composable
private fun BotonCerrarSesion(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(Organic.pill)
            .background(Organic.neutral200)
            .border(1.dp, Organic.neutral300, Organic.pill)
            .clickable(onClick = onClick),
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.ExitToApp,
            contentDescription = null,
            tint = Organic.accent800,
            modifier = Modifier.size(20.dp),
        )
        Text(
            "Cerrar sesión",
            style = MaterialTheme.typography.labelLarge,
            color = Organic.accent800,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
