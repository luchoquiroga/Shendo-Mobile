package com.admin_gym.mobile_shendo.data

import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.catch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

/** Avisos en tiempo real del back ("cambió el pedido N"). Solo avisan: los datos se recargan por REST. */
object Avisos {

    private const val NO_AUTORIZADO = 4401
    private val http = OkHttpClient()

    // http://10.0.2.2:8000/api/ -> ws://10.0.2.2:8000/ws/
    private val BASE_WS = ApiClient.BASE_URL.replaceFirst("http", "ws").replace("/api/", "/ws/")

    private class Cerrado(val code: Int) : Exception("WebSocket cerrado ($code)")

    /**
     * Emite el id de cada pedido que cambió. [ruta] = "cliente/" o "repartidor/".
     * Si se corta, reintenta con espera creciente (1 s, 2 s, 4 s... hasta 30 s). Si el back lo rechaza
     * por token (4401), renueva el access; si no se puede, deja de intentar (la API hará el logout).
     */
    fun avisos(ruta: String): Flow<Long> = flow {
        var espera = 1_000L
        while (true) {
            val token = ApiClient.accessActual ?: return@flow
            var codigo = 0
            emitAll(conexion(ruta, token).catch { e ->
                codigo = (e as? Cerrado)?.code ?: 0
                Log.d("Avisos", "socket $ruta cortado: ${e.message}")
            })
            // Sin red, renovarAccess tira ApiException: se trata como "reintentar más tarde".
            if (codigo == NO_AUTORIZADO && runCatching { ApiClient.renovarAccess() }.getOrDefault("") == null) return@flow
            delay(espera)
            espera = (espera * 2).coerceAtMost(30_000L)
        }
    }

    /** Una conexión: emite avisos hasta que se cierra (termina con [Cerrado] o con la falla de red). */
    private fun conexion(ruta: String, token: String): Flow<Long> = callbackFlow {
        val socket = http.newWebSocket(
            Request.Builder().url("$BASE_WS$ruta?token=$token").build(),
            object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    val json = runCatching { JSONObject(text) }.getOrNull() ?: return
                    if (json.optString("tipo") == "pedido") trySendBlocking(json.optLong("id_pedido"))
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(code, null)
                    close(Cerrado(code))
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    close(t)
                }
            },
        )
        awaitClose { socket.close(1000, null) }
    }
}
