package com.admin_gym.mobile_shendo.ui

import android.app.Application
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.admin_gym.mobile_shendo.data.ApiClient
import com.admin_gym.mobile_shendo.data.ApiException
import com.admin_gym.mobile_shendo.data.FavoritosStore
import com.admin_gym.mobile_shendo.data.ItemCarrito
import com.admin_gym.mobile_shendo.data.MetodoPago
import com.admin_gym.mobile_shendo.data.Pedido
import com.admin_gym.mobile_shendo.data.Producto
import com.admin_gym.mobile_shendo.data.Restaurante
import com.admin_gym.mobile_shendo.data.Session
import com.admin_gym.mobile_shendo.data.SessionStore
import com.admin_gym.mobile_shendo.data.TipoEntrega
import com.admin_gym.mobile_shendo.data.EstadoRepartidor
import com.admin_gym.mobile_shendo.data.Avisos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed interface Pantalla {
    data object Restaurantes : Pantalla
    data object MisPedidos : Pantalla
    data object Perfil : Pantalla
    data class Menu(val restaurante: Restaurante) : Pantalla
    data object Carrito : Pantalla
}

data class CambioDeRestaurante(
    val actual: Restaurante,
    val nuevo: Restaurante,
    val producto: Producto,
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SessionStore(app)

    var session by mutableStateOf(store.load())
        private set

    init {
        session?.let(::usarTokens)
        // ApiClient renovó el access (llega desde un hilo de IO): se guarda para la próxima vez.
        ApiClient.onAccessRenovado = { nuevo ->
            viewModelScope.launch(Dispatchers.Main) {
                session = session?.copy(token = nuevo)?.also(store::save)
            }
        }
    }

    private fun usarTokens(s: Session?) {
        ApiClient.accessActual = s?.token
        ApiClient.refreshActual = s?.refresh
    }

    override fun onCleared() {
        ApiClient.onAccessRenovado = null
    }

    private val favoritosStore = FavoritosStore(app)

    /** Ids de restaurantes favoritos de la cuenta actual (se guardan en el teléfono). */
    var favoritos by mutableStateOf(session?.let { favoritosStore.load(it.email) }.orEmpty())
        private set

    var authLoading by mutableStateOf(false)
        private set
    var authError by mutableStateOf<String?>(null)
        private set

    /** Pila de navegación: el último elemento es la pantalla visible. */
    var pila by mutableStateOf<List<Pantalla>>(listOf(Pantalla.Restaurantes))
        private set
    val pantalla get() = pila.last()

    var restaurantes by mutableStateOf<List<Restaurante>>(emptyList())
        private set
    var menu by mutableStateOf<List<Producto>>(emptyList())
        private set
    var misPedidos by mutableStateOf<List<Pedido>>(emptyList())
        private set

    /** El carrito es de un solo restaurante a la vez. */
    var carritoRestaurante by mutableStateOf<Restaurante?>(null)
        private set
    var carrito by mutableStateOf<List<ItemCarrito>>(emptyList())
        private set
    /** Producto de otro restaurante esperando que el cliente confirme vaciar el carrito actual. */
    var cambioPendiente by mutableStateOf<CambioDeRestaurante?>(null)
        private set
    val totalCarrito get() = carrito.sumOf { it.subtotal }
    val cantidadCarrito get() = carrito.sumOf { it.cantidad }

    /** Envío según el tipo de entrega: con retiro no se cobra. */
    fun costoEnvio(tipo: TipoEntrega) =
        if (tipo == TipoEntrega.DOMICILIO) carritoRestaurante?.costoEnvio ?: 0.0 else 0.0

    fun totalConEnvio(tipo: TipoEntrega) = totalCarrito + costoEnvio(tipo)

    /** Cuánto le falta al subtotal para llegar al pedido mínimo (0 si ya llega o no hay mínimo). */
    val faltaParaMinimo get() = ((carritoRestaurante?.pedidoMinimo ?: 0.0) - totalCarrito).coerceAtLeast(0.0)

    var loading by mutableStateOf(false)
        private set
    var enviandoPedido by mutableStateOf(false)
        private set

    /** Mensaje para mostrar en un Snackbar; la pantalla lo limpia con [mensajeMostrado]. */
    var mensaje by mutableStateOf<String?>(null)
        private set

    // ---------- Sesión ----------

    fun login(email: String, contrasena: String) {
        if (email.isBlank() || contrasena.isBlank()) {
            authError = "Completá email y contraseña"
            return
        }
        autenticar { ApiClient.login(email.trim(), contrasena) }
    }

    /** Con [vehiculo] distinto de null la cuenta se crea como repartidor. */
    fun registro(
        nombre: String,
        email: String,
        telefono: String,
        contrasena: String,
        direccion: String,
        vehiculo: String? = null,
    ) {
        val digitos = telefono.count { it.isDigit() }
        authError = when {
            nombre.isBlank() || email.isBlank() || telefono.isBlank() || contrasena.isBlank() ->
                "Completá nombre, email, teléfono y contraseña"
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "El email no es válido"
            digitos !in 6..15 -> "El teléfono no es válido"
            contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            vehiculo != null && vehiculo.isBlank() -> "Contanos en qué vehículo repartís"
            else -> null
        }
        if (authError != null) return
        autenticar {
            ApiClient.registro(nombre.trim(), email.trim(), telefono.trim(), contrasena, direccion.trim(), vehiculo?.trim())
        }
    }

    fun limpiarErrorAuth() {
        authError = null
    }

    private fun autenticar(llamada: suspend () -> Session) {
        viewModelScope.launch {
            authLoading = true
            authError = null
            try {
                val nueva = llamada()
                store.save(nueva)
                usarTokens(nueva)
                session = nueva
                favoritos = favoritosStore.load(nueva.email)
            } catch (e: ApiException) {
                authError = e.message
            } finally {
                authLoading = false
            }
        }
    }

    fun logout(motivo: String? = null) {
        store.clear()
        usarTokens(null)
        session = null
        pila = listOf(Pantalla.Restaurantes)
        misPedidos = emptyList()
        estadoRepartidor = null
        disponibles = emptyList()
        misEntregas = emptyList()
        favoritos = emptySet()
        vaciarCarrito()
        authError = motivo
    }

    fun mensajeMostrado() {
        mensaje = null
    }

    // ---------- Navegación ----------

    fun irA(pantalla: Pantalla) {
        pila = pila + pantalla
    }

    /** Cambia de pestaña (Restaurantes / Mis pedidos / Perfil) descartando lo que había apilado. */
    fun irAPestania(pantalla: Pantalla) {
        pila = listOf(pantalla)
    }

    /** Devuelve false si no hay a dónde volver (y entonces se cierra la app). */
    fun volver(): Boolean {
        if (pila.size <= 1) return false
        pila = pila.dropLast(1)
        return true
    }

    // ---------- Datos ----------

    fun cargarRestaurantes() = ejecutar { restaurantes = ApiClient.getRestaurantes() }

    fun cargarMenu(restaurante: Restaurante) = ejecutar {
        menu = emptyList()
        menu = ApiClient.getMenu(restaurante.id)
    }

    /** Escucha los avisos del back ("cambió un pedido") y llama a [alRecibir] por cada uno. Cancelar el Job al salir. */
    fun escucharAvisos(ruta: String, alRecibir: () -> Unit): Job =
        viewModelScope.launch { Avisos.avisos(ruta).collect { alRecibir() } }

    /** [silencioso] = sin spinner ni mensaje si falla la red (para el refresco periódico de la pantalla). */
    fun cargarMisPedidos(silencioso: Boolean = false) =
        ejecutar(conToken = true, silencioso = silencioso) { misPedidos = ApiClient.getMisPedidos(it) }

    // ---------- Perfil ----------

    var guardandoPerfil by mutableStateOf(false)
        private set

    /** Guarda nombre, teléfono y dirección. Devuelve por [alTerminar] si salió bien (para cerrar el formulario). */
    fun guardarPerfil(nombre: String, telefono: String, direccion: String, alTerminar: (Boolean) -> Unit = {}) {
        val digitos = telefono.count { it.isDigit() }
        when {
            nombre.isBlank() -> { mensaje = "El nombre no puede quedar vacío"; return }
            digitos !in 6..15 -> { mensaje = "El teléfono no es válido"; return }
        }
        actualizarPerfil("Perfil actualizado", alTerminar) {
            ApiClient.actualizarPerfil(it, nombre = nombre.trim(), telefono = telefono.trim(), direccion = direccion.trim())
        }
    }

    fun cambiarContrasena(actual: String, nueva: String, alTerminar: (Boolean) -> Unit = {}) {
        when {
            actual.isBlank() -> { mensaje = "Ingresá tu contraseña actual"; return }
            nueva.length < 6 -> { mensaje = "La contraseña nueva debe tener al menos 6 caracteres"; return }
        }
        actualizarPerfil("Contraseña cambiada", alTerminar) {
            ApiClient.actualizarPerfil(it, contrasenaActual = actual, contrasenaNueva = nueva)
        }
    }

    private fun actualizarPerfil(ok: String, alTerminar: (Boolean) -> Unit, llamada: suspend (Session) -> Session) {
        val actual = session ?: return
        viewModelScope.launch {
            guardandoPerfil = true
            try {
                val respuesta = llamada(actual)
                // Si el back mandó tokens nuevos (cambio de contraseña) se usan; si no, el access pudo
                // haberse renovado durante la llamada y vale el que tiene la sesión ahora.
                val nueva = if (respuesta.token != actual.token) respuesta
                else respuesta.copy(token = session?.token ?: actual.token)
                store.save(nueva)
                session = nueva
                mensaje = ok
                alTerminar(true)
            } catch (e: ApiException) {
                manejarError(e)
                alTerminar(false)
            } finally {
                guardandoPerfil = false
            }
        }
    }

    // ---------- Repartidor ----------

    var estadoRepartidor by mutableStateOf<EstadoRepartidor?>(null)
        private set
    /** Pedidos para tomar. */
    var disponibles by mutableStateOf<List<Pedido>>(emptyList())
        private set
    /** Pedidos que tomé: activos primero, después el historial. */
    var misEntregas by mutableStateOf<List<Pedido>>(emptyList())
        private set
    /** Id del pedido sobre el que hay una acción en curso (para deshabilitar sus botones). */
    var accionEnPedido by mutableStateOf<Long?>(null)
        private set

    val entregasActivas get() = misEntregas.filter { !it.terminado }

    /** Estado + disponibles + mis entregas. [silencioso] para el refresco periódico. */
    fun cargarRepartidor(silencioso: Boolean = false) = ejecutar(conToken = true, silencioso = silencioso) { token ->
        estadoRepartidor = ApiClient.getEstadoRepartidor(token)
        disponibles = ApiClient.getDisponibles(token)
        misEntregas = ApiClient.getMisEntregas(token)
    }

    fun alternarDisponible() {
        val actual = estadoRepartidor ?: return
        accionRepartidor(null) { token ->
            estadoRepartidor = ApiClient.actualizarEstadoRepartidor(token, disponible = !actual.disponible)
            mensaje = if (estadoRepartidor?.disponible == true) "Estás disponible para tomar pedidos" else "Pausaste los pedidos"
        }
    }

    fun cambiarVehiculo(vehiculo: String) {
        if (vehiculo.isBlank()) { mensaje = "Ingresá tu vehículo"; return }
        accionRepartidor(null) { token ->
            estadoRepartidor = ApiClient.actualizarEstadoRepartidor(token, vehiculo = vehiculo.trim())
            mensaje = "Vehículo actualizado"
        }
    }

    fun tomarPedido(pedido: Pedido) = accionRepartidor(pedido.id, recargarSi409 = true) { token ->
        ApiClient.tomarPedido(token, pedido.id)
        mensaje = "Tomaste el pedido #${pedido.id}"
        disponibles = ApiClient.getDisponibles(token)
        misEntregas = ApiClient.getMisEntregas(token)
    }

    fun soltarPedido(pedido: Pedido) = accionRepartidor(pedido.id, recargarSi409 = true) { token ->
        ApiClient.soltarPedido(token, pedido.id)
        mensaje = "Devolviste el pedido #${pedido.id}"
        disponibles = ApiClient.getDisponibles(token)
        misEntregas = ApiClient.getMisEntregas(token)
    }

    fun marcarEnCamino(pedido: Pedido) = cambiarEstadoEntrega(pedido, Pedido.ESTADO_EN_CAMINO, "Pedido #${pedido.id} en camino")

    fun marcarEntregado(pedido: Pedido) = cambiarEstadoEntrega(pedido, Pedido.ESTADO_ENTREGADO, "¡Pedido #${pedido.id} entregado!")

    private fun cambiarEstadoEntrega(pedido: Pedido, estado: String, ok: String) =
        accionRepartidor(pedido.id, recargarSi409 = true) { token ->
            val actualizado = ApiClient.cambiarEstadoEntrega(token, pedido.id, estado)
            misEntregas = misEntregas.map { if (it.id == actualizado.id) actualizado else it }
            mensaje = ok
        }

    private fun accionRepartidor(idPedido: Long?, recargarSi409: Boolean = false, block: suspend (String) -> Unit) {
        val token = session?.token ?: return
        if (accionEnPedido != null) return
        viewModelScope.launch {
            accionEnPedido = idPedido ?: -1L
            try {
                block(token)
            } catch (e: ApiException) {
                manejarError(e)
                // 409 = el pedido cambió (otro lo tomó, ya salió...): se recarga para mostrar lo real.
                if (e.code == 409 && recargarSi409) cargarRepartidor(silencioso = true)
            } finally {
                accionEnPedido = null
            }
        }
    }

    fun alternarFavorito(restaurante: Restaurante) {
        val email = session?.email ?: return
        favoritos = if (restaurante.id in favoritos) favoritos - restaurante.id else favoritos + restaurante.id
        favoritosStore.save(email, favoritos)
    }

    // ---------- Carrito ----------

    fun cantidadEnCarrito(producto: Producto) =
        carrito.firstOrNull { it.producto.id == producto.id }?.cantidad ?: 0

    fun agregar(restaurante: Restaurante, producto: Producto) {
        if (carritoRestaurante?.id != restaurante.id) {
            val actual = carritoRestaurante
            if (actual != null && carrito.isNotEmpty()) {
                // El carrito es de un solo restaurante: antes de vaciarlo, se pregunta (ver ClienteApp).
                cambioPendiente = CambioDeRestaurante(actual, restaurante, producto)
                return
            }
            carrito = emptyList()
            carritoRestaurante = restaurante
        }
        val nueva = cantidadEnCarrito(producto) + 1
        if (producto.stock != null && nueva > producto.stock) {
            mensaje = if (producto.stock == 0) "${producto.nombre} no tiene stock"
            else "Solo quedan ${producto.stock} de ${producto.nombre}"
            return
        }
        cambiarCantidad(producto, nueva)
    }

    /** Vacía el carrito del otro restaurante y agrega el producto que se quiso sumar. */
    fun confirmarCambioDeRestaurante() {
        val cambio = cambioPendiente ?: return
        cambioPendiente = null
        vaciarCarrito()
        agregar(cambio.nuevo, cambio.producto)
    }

    fun cancelarCambioDeRestaurante() {
        cambioPendiente = null
    }

    fun quitar(producto: Producto) = cambiarCantidad(producto, cantidadEnCarrito(producto) - 1)

    private fun cambiarCantidad(producto: Producto, cantidad: Int) {
        val sinEste = carrito.filter { it.producto.id != producto.id }
        carrito = if (cantidad <= 0) {
            sinEste
        } else if (sinEste.size == carrito.size) {
            carrito + ItemCarrito(producto, cantidad)
        } else {
            carrito.map { if (it.producto.id == producto.id) it.copy(cantidad = cantidad) else it }
        }
        if (carrito.isEmpty()) carritoRestaurante = null
    }

    private fun vaciarCarrito() {
        carrito = emptyList()
        carritoRestaurante = null
        cambioPendiente = null
    }

    fun confirmarPedido(tipoEntrega: TipoEntrega, direccion: String, metodoPago: MetodoPago?) {
        val restaurante = carritoRestaurante ?: return
        val token = session?.token ?: return
        if (tipoEntrega == TipoEntrega.DOMICILIO && direccion.isBlank()) {
            mensaje = "Ingresá la dirección de entrega"
            return
        }
        if (faltaParaMinimo > 0) {
            mensaje = "El pedido mínimo es ${precio(restaurante.pedidoMinimo)}"
            return
        }
        if (metodoPago == null) {
            mensaje = "Elegí cómo vas a pagar"
            return
        }
        viewModelScope.launch {
            enviandoPedido = true
            try {
                val pedido = ApiClient.crearPedido(token, restaurante.id, tipoEntrega, direccion.trim(), metodoPago, carrito)
                vaciarCarrito()
                mensaje = "¡Pedido #${pedido.id} realizado!"
                pila = listOf(Pantalla.MisPedidos)
            } catch (e: ApiException) {
                manejarError(e)
            } finally {
                enviandoPedido = false
            }
        }
    }

    private fun ejecutar(conToken: Boolean = false, silencioso: Boolean = false, block: suspend (token: String) -> Unit) {
        val token = session?.token.orEmpty()
        if (conToken && token.isEmpty()) return
        viewModelScope.launch {
            if (!silencioso) loading = true
            try {
                block(token)
            } catch (e: ApiException) {
                // En el refresco periódico, un corte de red no se muestra; la sesión vencida sí.
                if (!silencioso || e.code == 401) manejarError(e)
            } finally {
                if (!silencioso) loading = false
            }
        }
    }

    private fun manejarError(e: ApiException) {
        if (e.code == 401) logout("Tu sesión expiró, ingresá de nuevo")
        else mensaje = e.message
    }
}
