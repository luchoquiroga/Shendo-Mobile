package com.admin_gym.mobile_shendo.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** code = 0 cuando no se pudo llegar al servidor. */
class ApiException(val code: Int, message: String) : Exception(message)

object ApiClient {

    // 10.0.2.2 es la PC vista desde el emulador de Android.
    // Para un celular físico, poné la IP local de la PC (ej. "http://192.168.0.10:8000/api/")
    // y levantá Django con: python manage.py runserver 0.0.0.0:8000
    const val BASE_URL = "http://10.0.2.2:8000/api/"

    // ---------- Renovación del token ----------
    // El ViewModel carga acá los tokens de la sesión. Si una llamada con token da 401, se pide un
    // access nuevo con el refresh, se avisa por onAccessRenovado (para guardarlo) y se reintenta una vez.

    @Volatile var accessActual: String? = null
    @Volatile var refreshActual: String? = null
    var onAccessRenovado: ((String) -> Unit)? = null
    private val renovando = Mutex()

    /** Access nuevo, o null si no hay refresh o el refresh venció (entonces hay que loguearse). */
    private suspend fun renovar(tokenVencido: String): String? = renovando.withLock {
        // Si otra llamada ya lo renovó mientras esperábamos, usamos ese.
        accessActual?.takeIf { it != tokenVencido }?.let { return@withLock it }
        val refresh = refreshActual ?: return@withLock null
        val nuevo = try {
            JSONObject(enviar("POST", "cliente/token/refresh/", null, JSONObject().put("refresh", refresh)))
                .getString("access")
        } catch (e: ApiException) {
            if (e.code == 0) throw e  // sin conexión: no es que haya vencido
            return@withLock null
        }
        accessActual = nuevo
        onAccessRenovado?.invoke(nuevo)
        nuevo
    }

    /** Para el WebSocket: access nuevo a partir del actual, o null si la sesión venció. */
    suspend fun renovarAccess(): String? = accessActual?.let { renovar(it) }

    // ---------- Cuenta (cliente y repartidor) ----------

    suspend fun login(email: String, contrasena: String): Session {
        val body = JSONObject().put("email", email).put("contrasena", contrasena)
        return sesion(JSONObject(request("POST", "cliente/login/", body = body)))
    }

    /** Con [vehiculo] distinto de null se registra como repartidor. */
    suspend fun registro(
        nombre: String,
        email: String,
        telefono: String,
        contrasena: String,
        direccion: String,
        vehiculo: String? = null,
    ): Session {
        val body = JSONObject()
            .put("nombre", nombre)
            .put("email", email)
            .put("telefono", telefono)
            .put("contrasena", contrasena)
            .put("direccion", direccion)
            .apply { if (vehiculo != null) put("repartidor", true).put("vehiculo", vehiculo) }
        return sesion(JSONObject(request("POST", "cliente/registro/", body = body)))
    }

    /** Solo se mandan los campos no nulos. Devuelve la sesión con los datos actualizados. */
    suspend fun actualizarPerfil(
        sesion: Session,
        nombre: String? = null,
        telefono: String? = null,
        direccion: String? = null,
        contrasenaActual: String? = null,
        contrasenaNueva: String? = null,
    ): Session {
        val body = JSONObject().apply {
            nombre?.let { put("nombre", it) }
            telefono?.let { put("telefono", it) }
            direccion?.let { put("direccion", it) }
            if (contrasenaNueva != null) put("contrasena_actual", contrasenaActual).put("contrasena_nueva", contrasenaNueva)
        }
        val json = JSONObject(request("PATCH", "cliente/perfil/", sesion.token, body))
        // Al cambiar la contraseña el back invalida los tokens viejos y manda unos nuevos.
        val nuevoAccess = json.str("access")
        val nuevoRefresh = json.str("refresh")
        if (nuevoAccess != null) {
            accessActual = nuevoAccess
            refreshActual = nuevoRefresh
        }
        return sesion.copy(
            token = nuevoAccess ?: sesion.token,
            refresh = nuevoRefresh ?: sesion.refresh,
            nombre = json.getString("nombre"),
            telefono = json.str("telefono").orEmpty(),
            direccion = json.str("direccion").orEmpty(),
        )
    }

    // ---------- Cliente ----------

    suspend fun getRestaurantes(): List<Restaurante> =
        JSONArray(request("GET", "cliente/restaurantes/")).map {
            Restaurante(
                id = it.getLong("id_restaurante"),
                nombre = it.getString("nombre"),
                direccion = it.str("direccion"),
                categorias = it.optJSONArray("categorias")
                    ?.let { lista -> (0 until lista.length()).map(lista::getString) }
                    .orEmpty(),
                abierto = it.optBoolean("abierto", true),
                precioDesde = if (it.isNull("precio_desde")) null else it.optDouble("precio_desde"),
                costoEnvio = it.numero("costo_envio"),
                pedidoMinimo = it.numero("pedido_minimo"),
                horarioHoy = it.str("horario_hoy")?.takeIf { h -> h.isNotBlank() },
            )
        }

    suspend fun getMenu(idRestaurante: Long): List<Producto> =
        JSONArray(request("GET", "cliente/restaurantes/$idRestaurante/productos/")).map {
            Producto(
                id = it.getLong("id_producto"),
                nombre = it.getString("nombre"),
                descripcion = it.str("descripcion"),
                precio = it.optDouble("precio", 0.0),
                stock = if (it.isNull("stock")) null else it.getInt("stock"),
                imagenUrl = it.str("imagen_url")?.takeIf { u -> u.isNotBlank() },
            )
        }

    suspend fun getMisPedidos(token: String): List<Pedido> =
        JSONArray(request("GET", "cliente/pedidos/", token)).map(::pedido)

    suspend fun crearPedido(
        token: String,
        idRestaurante: Long,
        tipoEntrega: TipoEntrega,
        direccion: String,
        metodoPago: MetodoPago,
        items: List<ItemCarrito>,
    ): Pedido {
        val body = JSONObject()
            .put("id_restaurante", idRestaurante)
            .put("tipo_entrega", tipoEntrega.clave)
            .apply { if (tipoEntrega == TipoEntrega.DOMICILIO) put("direccion_entrega", direccion) }
            .put("metodo_pago", metodoPago.clave)
            .put("items", JSONArray(items.map {
                JSONObject().put("id_producto", it.producto.id).put("cantidad", it.cantidad)
            }))
        return pedido(JSONObject(request("POST", "cliente/pedidos/", token, body)))
    }

    // ---------- Repartidor ----------

    suspend fun getEstadoRepartidor(token: String): EstadoRepartidor =
        estadoRepartidor(JSONObject(request("GET", "repartidor/estado/", token)))

    suspend fun actualizarEstadoRepartidor(token: String, disponible: Boolean? = null, vehiculo: String? = null): EstadoRepartidor {
        val body = JSONObject().apply {
            disponible?.let { put("disponible", it) }
            vehiculo?.let { put("vehiculo", it) }
        }
        return estadoRepartidor(JSONObject(request("PATCH", "repartidor/estado/", token, body)))
    }

    /** Pedidos a domicilio que nadie tomó (sin datos del cliente). */
    suspend fun getDisponibles(token: String): List<Pedido> =
        JSONArray(request("GET", "repartidor/disponibles/", token)).map(::pedido)

    /** Mis entregas: activas primero, después el historial. */
    suspend fun getMisEntregas(token: String): List<Pedido> =
        JSONArray(request("GET", "repartidor/pedidos/", token)).map(::pedido)

    /** 409 si otro lo tomó antes o no estoy disponible. */
    suspend fun tomarPedido(token: String, idPedido: Long): Pedido =
        pedido(JSONObject(request("POST", "repartidor/pedidos/$idPedido/tomar/", token)))

    suspend fun soltarPedido(token: String, idPedido: Long) {
        request("POST", "repartidor/pedidos/$idPedido/soltar/", token)
    }

    /** [estado] = [Pedido.ESTADO_EN_CAMINO] o [Pedido.ESTADO_ENTREGADO]. */
    suspend fun cambiarEstadoEntrega(token: String, idPedido: Long, estado: String): Pedido =
        pedido(JSONObject(request("POST", "repartidor/pedidos/$idPedido/estado/", token, JSONObject().put("estado", estado))))

    // ---------- JSON -> modelos ----------

    private fun sesion(json: JSONObject) = Session(
        token = json.getString("access"),
        refresh = json.str("refresh"),
        nombre = json.getString("nombre"),
        email = json.str("email").orEmpty(),
        telefono = json.str("telefono").orEmpty(),
        direccion = json.str("direccion").orEmpty(),
        esRepartidor = json.str("perfil") == "repartidor",
    )

    private fun estadoRepartidor(json: JSONObject) = EstadoRepartidor(
        vehiculo = json.str("vehiculo").orEmpty(),
        disponible = json.optBoolean("disponible", false),
    )

    private fun pedido(json: JSONObject) = Pedido(
        id = json.getLong("id_pedido"),
        fecha = json.str("fecha"),
        estado = json.str("estado") ?: "pendiente",
        total = json.optDouble("total", 0.0),
        direccionEntrega = json.str("direccion_entrega"),
        restaurante = json.str("restaurante"),
        metodoPago = json.str("metodo_pago"),
        pagado = json.optBoolean("pagado", false),
        tipoEntrega = TipoEntrega.deClave(json.str("tipo_entrega")),
        costoEnvio = json.numero("costo_envio"),
        items = json.getJSONArray("items").map {
            ItemPedido(
                nombre = it.getString("nombre"),
                cantidad = it.getInt("cantidad"),
                subtotal = it.optDouble("subtotal", 0.0),
            )
        },
        repartidor = json.optJSONObject("repartidor")?.let {
            RepartidorDelPedido(it.getString("nombre"), it.str("telefono"), it.str("vehiculo"))
        },
        restauranteDireccion = json.str("restaurante_direccion"),
        restauranteTelefono = json.str("restaurante_telefono"),
        cliente = json.optJSONObject("cliente")?.let { ClienteDelPedido(it.getString("nombre"), it.str("telefono")) },
    )

    // ---------- HTTP ----------

    /** Como [enviar], pero si da 401 con token, renueva el access y reintenta una vez. */
    private suspend fun request(
        method: String,
        path: String,
        token: String? = null,
        body: JSONObject? = null,
    ): String = try {
        enviar(method, path, token, body)
    } catch (e: ApiException) {
        if (e.code != 401 || token == null) throw e
        val nuevo = renovar(token) ?: throw e
        enviar(method, path, nuevo, body)
    }

    private suspend fun enviar(
        method: String,
        path: String,
        token: String?,
        body: JSONObject?,
    ): String = withContext(Dispatchers.IO) {
        val conn = URL(BASE_URL + path).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000
            conn.setRequestProperty("Accept", "application/json")
            if (token != null) conn.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.outputStream.use { it.write(body.toString().toByteArray()) }
            }

            val code = conn.responseCode
            val ok = code in 200..299
            val text = (if (ok) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }
                .orEmpty()
            if (!ok) throw ApiException(code, mensajeDeError(code, text))
            text
        } catch (e: IOException) {
            Log.w("ApiClient", "$method $path fallo", e)
            throw ApiException(0, "No se pudo conectar con el servidor. Revisá tu conexión.")
        } finally {
            conn.disconnect()
        }
    }

    private fun mensajeDeError(code: Int, text: String): String {
        if (code == 429) return "Demasiados intentos. Esperá un minuto y probá de nuevo."
        val error = try {
            JSONObject(text).optString("error").takeIf { it.isNotBlank() }
        } catch (e: JSONException) {
            null
        }
        return error ?: "Error del servidor ($code)"
    }

    /** org.json devuelve "null" como texto con optString, esto devuelve null de verdad. */
    private fun JSONObject.str(key: String): String? = if (!has(key) || isNull(key)) null else get(key).toString()

    /** Número tolerante: si falta, es null o no se puede leer, 0. */
    private fun JSONObject.numero(key: String): Double =
        if (isNull(key)) 0.0 else get(key).toString().toDoubleOrNull() ?: 0.0

    private fun <T> JSONArray.map(transform: (JSONObject) -> T): List<T> =
        (0 until length()).map { transform(getJSONObject(it)) }
}
