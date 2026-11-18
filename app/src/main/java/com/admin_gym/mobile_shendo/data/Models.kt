package com.admin_gym.mobile_shendo.data

data class Session(
    val token: String,
    /** Para pedir un access nuevo cuando vence (null en sesiones guardadas antes de tener refresh). */
    val refresh: String?,
    val nombre: String,
    val email: String,
    val telefono: String,
    val direccion: String,
    /** true = la app muestra la cara de repartidor (RepartidorApp); false = la de cliente. */
    val esRepartidor: Boolean = false,
)

/** Datos de la cuenta de repartidor (GET/PATCH /api/repartidor/estado/). */
data class EstadoRepartidor(
    val vehiculo: String,
    val patente: String,
    val disponible: Boolean,
)

/** Quien lleva el pedido (lo ve el cliente). */
data class RepartidorDelPedido(
    val nombre: String,
    val telefono: String?,
    val vehiculo: String?,
)

/** A quién entregarle (lo ve el repartidor una vez que tomó el pedido). */
data class ClienteDelPedido(
    val nombre: String,
    val telefono: String?,
)

data class Restaurante(
    val id: Long,
    val nombre: String,
    val direccion: String?,
    /** Rubros del restaurante (tabla categoria), ordenados. Con ellos se arman los filtros de la Home. */
    val categorias: List<String>,
    val abierto: Boolean,
    /** Precio del producto más barato del menú; null si no tiene productos disponibles. */
    val precioDesde: Double?,
    /** 0 = envío gratis. */
    val costoEnvio: Double = 0.0,
    /** 0 = sin pedido mínimo. */
    val pedidoMinimo: Double = 0.0,
    /** Ej. "09:00 a 13:00 y 20:00 a 23:30" o "Cerrado hoy"; null si el back no lo informa. */
    val horarioHoy: String? = null,
)

data class Producto(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    /** null = el restaurante no controla stock de este producto. */
    val stock: Int?,
    /** URL absoluta de la foto, o null. */
    val imagenUrl: String? = null,
)

data class ItemCarrito(
    val producto: Producto,
    val cantidad: Int,
) {
    val subtotal get() = producto.precio * cantidad
}

data class Pedido(
    val id: Long,
    val fecha: String?,
    val estado: String,
    val total: Double,
    val direccionEntrega: String?,
    val restaurante: String?,
    val metodoPago: String?,
    val pagado: Boolean,
    val tipoEntrega: TipoEntrega = TipoEntrega.DOMICILIO,
    val costoEnvio: Double = 0.0,
    val items: List<ItemPedido>,
    /** null mientras nadie lo tomó. */
    val repartidor: RepartidorDelPedido? = null,
    // Solo en los endpoints de repartidor:
    val restauranteDireccion: String? = null,
    val restauranteTelefono: String? = null,
    /** null en "disponibles" (el repartidor ve al cliente recién cuando toma el pedido). */
    val cliente: ClienteDelPedido? = null,
) {
    /** El estado es texto libre del back; se compara sin mayúsculas. */
    val estadoNormalizado get() = estado.trim().lowercase()
    val enCamino get() = estadoNormalizado == ESTADO_EN_CAMINO
    val entregado get() = estadoNormalizado == ESTADO_ENTREGADO
    val terminado get() = estadoNormalizado in setOf(ESTADO_ENTREGADO, "cancelado", "rechazado")

    companion object {
        const val ESTADO_EN_CAMINO = "en camino"
        const val ESTADO_ENTREGADO = "entregado"
    }
}

data class ItemPedido(
    val nombre: String,
    val cantidad: Int,
    val subtotal: Double,
)

/** Las claves son las que espera el backend en "tipo_entrega". */
enum class TipoEntrega(val clave: String, val texto: String) {
    DOMICILIO("domicilio", "Envío a domicilio"),
    RETIRO("retiro", "Retiro en el local");

    companion object {
        fun deClave(clave: String?) = entries.firstOrNull { it.clave == clave } ?: DOMICILIO
    }
}

/** Las claves son las que espera el backend en "metodo_pago". */
enum class MetodoPago(val clave: String, val texto: String) {
    EFECTIVO("efectivo", "Efectivo"),
    TARJETA("tarjeta", "Tarjeta (al recibir)"),
    TRANSFERENCIA("transferencia", "Transferencia"),
}
