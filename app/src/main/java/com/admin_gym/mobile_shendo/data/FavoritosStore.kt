package com.admin_gym.mobile_shendo.data

import android.content.Context
import androidx.core.content.edit

/**
 * Restaurantes favoritos, guardados en el teléfono (el back no tiene dónde guardarlos).
 * Se separan por email para que dos cuentas en el mismo teléfono no compartan favoritos.
 */
class FavoritosStore(context: Context) {

    private val prefs = context.getSharedPreferences("favoritos", Context.MODE_PRIVATE)

    fun load(email: String): Set<Long> =
        prefs.getStringSet(email, emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }.toSet()

    fun save(email: String, ids: Set<Long>) = prefs.edit {
        putStringSet(email, ids.map { it.toString() }.toSet())
    }
}
