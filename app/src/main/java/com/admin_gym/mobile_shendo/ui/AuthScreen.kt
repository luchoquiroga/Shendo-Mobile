package com.admin_gym.mobile_shendo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.admin_gym.mobile_shendo.ui.theme.Organic

/** Login y registro de clientes en una misma pantalla. */
@Composable
fun AuthScreen(vm: MainViewModel) {
    var registrando by rememberSaveable { mutableStateOf(false) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }

    var repartidor by rememberSaveable { mutableStateOf(false) }
    var vehiculo by rememberSaveable { mutableStateOf("") }

    val enviar = {
        if (registrando) vm.registro(nombre, email, telefono, contrasena, direccion, if (repartidor) vehiculo else null)
        else vm.login(email, contrasena)
    }

    Scaffold(containerColor = Organic.bg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Organic.space6, vertical = Organic.gutter),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BloqueFoto(7, Modifier.size(96.dp))
            Spacer(Modifier.height(Organic.space4))
            Text("Shendo", style = MaterialTheme.typography.displaySmall, color = Organic.accent)
            Text(
                if (registrando) "Creá tu cuenta" else "Pedí comida a domicilio",
                style = MaterialTheme.typography.bodyMedium,
                color = Organic.textMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Organic.space8))

            if (registrando) {
                CampoTexto(nombre, { nombre = it }, "Nombre")
                Spacer(Modifier.height(12.dp))
            }
            CampoTexto(email, { email = it }, "Email", teclado = KeyboardType.Email)
            Spacer(Modifier.height(12.dp))
            if (registrando) {
                CampoTexto(telefono, { telefono = it }, "Teléfono", teclado = KeyboardType.Phone)
                Spacer(Modifier.height(12.dp))
            }
            CampoTexto(
                valor = contrasena,
                onChange = { contrasena = it },
                label = "Contraseña",
                teclado = KeyboardType.Password,
                imeAction = if (registrando) ImeAction.Next else ImeAction.Done,
                onDone = { enviar() },
                visualTransformation = PasswordVisualTransformation(),
            )
            if (registrando) {
                Spacer(Modifier.height(12.dp))
                CampoTexto(
                    valor = direccion,
                    onChange = { direccion = it },
                    label = "Dirección de entrega (opcional)",
                    imeAction = if (repartidor) ImeAction.Next else ImeAction.Done,
                    onDone = { enviar() },
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    // Toda la fila (texto incluido) activa el switch.
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .toggleable(value = repartidor, role = Role.Switch, onValueChange = { repartidor = it }),
                ) {
                    Text(
                        "Quiero ser repartidor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Organic.text,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(checked = repartidor, onCheckedChange = null)
                }
                if (repartidor) {
                    Spacer(Modifier.height(4.dp))
                    CampoTexto(
                        valor = vehiculo,
                        onChange = { vehiculo = it },
                        label = "Vehículo (ej. moto, bici)",
                        imeAction = ImeAction.Done,
                        onDone = { enviar() },
                    )
                }
            }

            vm.authError?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Organic.error)
            }

            Spacer(Modifier.height(Organic.space6))
            BotonPrincipal(
                texto = if (registrando) "Crear cuenta" else "Ingresar",
                onClick = { enviar() },
                cargando = vm.authLoading,
            )
            TextButton(onClick = {
                registrando = !registrando
                vm.limpiarErrorAuth()
            }) {
                Text(
                    if (registrando) "Ya tengo cuenta, ingresar" else "No tengo cuenta, registrarme",
                    style = MaterialTheme.typography.labelLarge,
                    color = Organic.accent800,
                )
            }
        }
    }
}
