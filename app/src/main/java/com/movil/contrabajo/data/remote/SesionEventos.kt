package com.movil.contrabajo.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Canal global para señalizar sesion invalida detectada desde los interceptores HTTP.
 * Usa un AtomicBoolean para evitar que varias llamadas 401 simultaneas disparen
 * el cierre de sesion multiples veces.
 */
object SesionEventos {
    private val _flujoSesionInvalida = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val flujoSesionInvalida: SharedFlow<Unit> = _flujoSesionInvalida.asSharedFlow()

    private val procesandoCierre = AtomicBoolean(false)

    /**
     * Emitir exactamente una vez por ciclo de cierre.
     * El interceptor de cada cliente llama a este metodo cuando recibe HTTP 401
     * en una solicitud autenticada con Bearer token.
     */
    fun emitirSesionInvalida() {
        if (procesandoCierre.compareAndSet(false, true)) {
            _flujoSesionInvalida.tryEmit(Unit)
        }
    }

    /** Llamar tras completar el flujo de cierre de sesion para permitir detecciones futuras. */
    fun resetear() {
        procesandoCierre.set(false)
    }
}
