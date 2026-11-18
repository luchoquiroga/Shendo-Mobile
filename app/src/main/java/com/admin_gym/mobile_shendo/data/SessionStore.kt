package com.admin_gym.mobile_shendo.data

import android.content.Context
import androidx.core.content.edit

/** Guarda la sesión para no tener que loguearse cada vez que se abre la app. */
class SessionStore(context: Context) {

    private val prefs = context.getSharedPreferences("session_cliente", Context.MODE_PRIVATE)

    fun load(): Session? {
        val token = prefs.getString("token", null) ?: return null
        return Session(
            token = token,
            refresh = prefs.getString("refresh", null),
            nombre = prefs.getString("nombre", "").orEmpty(),
            email = prefs.getString("email", "").orEmpty(),
            telefono = prefs.getString("telefono", "").orEmpty(),
            direccion = prefs.getString("direccion", "").orEmpty(),
            esRepartidor = prefs.getBoolean("es_repartidor", false),
        )
    }

    fun save(session: Session) = prefs.edit {
        putString("token", session.token)
        putString("refresh", session.refresh)
        putString("nombre", session.nombre)
        putString("email", session.email)
        putString("telefono", session.telefono)
        putString("direccion", session.direccion)
        putBoolean("es_repartidor", session.esRepartidor)
    }

    fun clear() = prefs.edit { clear() }
}
