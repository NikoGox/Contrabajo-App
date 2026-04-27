package com.movil.contrabajo.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.movil.contrabajo.domain.model.CategoriaServicio
import com.movil.contrabajo.domain.model.CitaServicio
import com.movil.contrabajo.domain.model.EstadoCodigo
import com.movil.contrabajo.domain.model.EstadoCita
import com.movil.contrabajo.domain.model.ChatCita
import com.movil.contrabajo.domain.model.FiltroMarketplaceConfig
import com.movil.contrabajo.domain.model.FotoServicioLocal
import com.movil.contrabajo.domain.model.FormularioServicio
import com.movil.contrabajo.domain.model.MensajeChat
import com.movil.contrabajo.domain.model.NotificacionMensajePendiente
import com.movil.contrabajo.domain.model.OfertaServicio
import com.movil.contrabajo.domain.model.PrecioUtils
import com.movil.contrabajo.domain.model.PreguntaSeguridadConfig
import com.movil.contrabajo.domain.model.TipoPerfil
import com.movil.contrabajo.domain.model.TipoPrecio
import com.movil.contrabajo.domain.model.UbicacionAjustesConfig
import com.movil.contrabajo.domain.model.Usuario
import com.movil.contrabajo.domain.model.Valoracion
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class ContrabajoSQLiteHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE usuarios (" +
                "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT," +
                "run TEXT NOT NULL," +
                "dv TEXT NOT NULL," +
                "username TEXT NOT NULL UNIQUE," +
                "nombre TEXT NOT NULL," +
                "apellido_paterno TEXT NOT NULL," +
                "apellido_materno TEXT NOT NULL," +
                "telefono TEXT NOT NULL," +
                "correo TEXT NOT NULL UNIQUE," +
                "contrasena_hash TEXT NOT NULL," +
                "fecha_registro TEXT NOT NULL," +
                "fecha_nacimiento TEXT NOT NULL," +
                "verificado INTEGER NOT NULL DEFAULT 0," +
                "tipo_perfil INTEGER NOT NULL DEFAULT 1," +
                "numero_documento_identidad TEXT," +
                "pregunta_recuperacion TEXT NOT NULL DEFAULT ''," +
                "respuesta_recuperacion TEXT NOT NULL DEFAULT ''," +
                "verificacion_trabajador_pendiente INTEGER NOT NULL DEFAULT 0," +
                "fecha_solicitud_verificacion_ms INTEGER," +
                "foto_perfil TEXT," +
                "UNIQUE(run, dv))"
        )
        db.execSQL(
            "CREATE TABLE categorias_servicio (" +
                "id_categoria_servicio INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE estados (" +
                "id_estado INTEGER PRIMARY KEY AUTOINCREMENT," +
                "codigo TEXT NOT NULL," +
                "nombre TEXT NOT NULL," +
                "descripcion TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE coordenadas (" +
                "id_coordenadas INTEGER PRIMARY KEY AUTOINCREMENT," +
                "latitud REAL NOT NULL," +
                "longitud REAL NOT NULL," +
                "detalle TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE direcciones (" +
                "id_direccion INTEGER PRIMARY KEY AUTOINCREMENT," +
                "calle TEXT NOT NULL," +
                "numero TEXT NOT NULL," +
                "villa TEXT NOT NULL," +
                "id_coordenadas INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE fotos (" +
                "id_foto INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fecha_subida TEXT NOT NULL," +
                "enlace TEXT NOT NULL," +
                "detalle TEXT NOT NULL," +
                "nombre_archivo TEXT NOT NULL DEFAULT ''," +
                "mime_type TEXT NOT NULL DEFAULT ''," +
                "estado_sincronizacion TEXT NOT NULL DEFAULT 'pendiente'," +
                "url_remota TEXT)"
        )
        db.execSQL(
            "CREATE TABLE ofertas_servicio (" +
                "id_oferta_servicio INTEGER PRIMARY KEY AUTOINCREMENT," +
                "titulo TEXT NOT NULL," +
                "descripcion TEXT NOT NULL," +
                "detalle TEXT NOT NULL," +
                "precio_texto TEXT NOT NULL," +
                "tipo_precio INTEGER NOT NULL DEFAULT 0," +
                "monto_base INTEGER NOT NULL DEFAULT 0," +
                "disponible INTEGER NOT NULL DEFAULT 1," +
                "eliminada INTEGER NOT NULL DEFAULT 0," +
                "fecha_eliminacion TEXT," +
                "fecha_publicacion TEXT NOT NULL," +
                "id_categoria_servicio INTEGER NOT NULL," +
                "id_trabajador INTEGER NOT NULL," +
                "id_cliente INTEGER," +
                "id_foto_portada INTEGER)"
        )
        db.execSQL(
            "CREATE TABLE chats_cita (" +
                "id_chat_cita INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fecha_creacion TEXT NOT NULL," +
                "id_trabajador INTEGER NOT NULL," +
                "id_cliente INTEGER NOT NULL," +
                "id_oferta_servicio INTEGER," +
                "cerrado INTEGER NOT NULL DEFAULT 0," +
                "bloqueado_hasta_ms INTEGER," +
                "id_cita INTEGER)"
        )
        db.execSQL(
            "CREATE TABLE citas_servicio (" +
                "id_cita INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_chat_cita INTEGER NOT NULL UNIQUE," +
                "fecha_creacion TEXT NOT NULL," +
                "fecha_programada TEXT NOT NULL," +
                "comentario TEXT NOT NULL," +
                "precio_acordado INTEGER NOT NULL DEFAULT 0," +
                "fecha_inicio_trabajo TEXT," +
                "fecha_fin_trabajo TEXT," +
                "estado_cita INTEGER NOT NULL DEFAULT 401," +
                "fecha_actualizacion TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE mensajes_chat (" +
                "id_mensaje_chat INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fecha_envio TEXT NOT NULL," +
                "fecha_recibido TEXT," +
                "fecha_leido TEXT," +
                "id_emisor INTEGER NOT NULL," +
                "id_receptor INTEGER NOT NULL," +
                "id_chat_cita INTEGER NOT NULL," +
                "id_estado INTEGER NOT NULL," +
                "notificado_local INTEGER NOT NULL DEFAULT 0," +
                "contenido TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE valoraciones (" +
                "id_valoracion INTEGER PRIMARY KEY AUTOINCREMENT," +
                "voto INTEGER NOT NULL," +
                "fecha_voto TEXT NOT NULL," +
                "comentario TEXT NOT NULL," +
                "id_trabajador INTEGER NOT NULL," +
                "id_cliente INTEGER NOT NULL," +
                "id_chat_cita INTEGER NOT NULL," +
                "id_oferta_servicio INTEGER NOT NULL," +
                "UNIQUE(id_chat_cita, id_cliente))"
        )
        db.execSQL(
            "CREATE TABLE sesiones_locales (" +
                "id_sesion_local INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_usuario INTEGER NOT NULL," +
                "token_local TEXT NOT NULL," +
                "fecha_inicio TEXT NOT NULL," +
                "fecha_ultimo_acceso TEXT NOT NULL," +
                "fecha_inicio_ms INTEGER NOT NULL," +
                "fecha_ultimo_acceso_ms INTEGER NOT NULL," +
                "recordarme INTEGER NOT NULL DEFAULT 0," +
                "activa INTEGER NOT NULL DEFAULT 1)"
        )
        db.execSQL(
            "CREATE TABLE configuraciones_app (" +
                "id_configuracion_app INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_usuario INTEGER," +
                "tema TEXT NOT NULL," +
                "notificaciones_activas INTEGER NOT NULL DEFAULT 1," +
                "primera_ejecucion INTEGER NOT NULL DEFAULT 1," +
                "ultima_pantalla TEXT NOT NULL," +
                "fecha_actualizacion TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE filtros_marketplace (" +
                "id_filtro_marketplace INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_usuario INTEGER NOT NULL UNIQUE," +
                "filtro_categoria_id INTEGER," +
                "filtro_tipo_precio INTEGER," +
                "solo_trabajador_verificado INTEGER NOT NULL DEFAULT 0," +
                "orden_marketplace TEXT NOT NULL DEFAULT 'FECHA_RECIENTES'," +
                "filtro_zona_comuna_activo INTEGER NOT NULL DEFAULT 0," +
                "comuna_filtro TEXT NOT NULL DEFAULT ''," +
                "fecha_actualizacion TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE preguntas_seguridad (" +
                "id_pregunta_seguridad INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_usuario INTEGER NOT NULL," +
                "indice INTEGER NOT NULL," +
                "pregunta TEXT NOT NULL," +
                "respuesta TEXT NOT NULL," +
                "fecha_actualizacion TEXT NOT NULL," +
                "UNIQUE(id_usuario, indice))"
        )
        db.execSQL(
            "CREATE TABLE ubicaciones_usuario (" +
                "id_ubicacion_usuario INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_usuario INTEGER NOT NULL UNIQUE," +
                "region TEXT NOT NULL DEFAULT 'Region Metropolitana'," +
                "comuna TEXT NOT NULL DEFAULT 'Santiago'," +
                "calle TEXT NOT NULL DEFAULT 'Sin calle'," +
                "numero TEXT NOT NULL DEFAULT 'Sin numero'," +
                "detalle TEXT NOT NULL DEFAULT 'Sin detalle'," +
                "latitud REAL," +
                "longitud REAL," +
                "rango_disponibilidad_m INTEGER NOT NULL DEFAULT 20000," +
                "rango_busqueda_m INTEGER NOT NULL DEFAULT 20000," +
                "fecha_actualizacion TEXT NOT NULL)"
        )
        sembrarDatosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        listOf(
            "filtros_marketplace", "ubicaciones_usuario", "preguntas_seguridad", "configuraciones_app", "sesiones_locales", "valoraciones", "mensajes_chat",
            "citas_servicio", "chats_cita", "ofertas_servicio", "fotos", "direcciones", "coordenadas",
            "estados", "categorias_servicio", "usuarios"
        ).forEach { db.execSQL("DROP TABLE IF EXISTS $it") }
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        aplicarDescripcionAmpliaDemo(db)
    }

    fun obtenerUsuarioPorCorreoOCuenta(identificador: String, contrasena: String): Usuario? {
        readableDatabase.rawQuery(
            "SELECT * FROM usuarios WHERE (correo = ? OR username = ?) AND contrasena_hash = ? LIMIT 1",
            arrayOf(identificador, identificador, contrasena)
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toUsuario() else null
        }
    }

    fun obtenerUsuarioPorIdentificador(identificador: String): Usuario? {
        readableDatabase.rawQuery(
            "SELECT * FROM usuarios WHERE correo = ? OR username = ? LIMIT 1",
            arrayOf(identificador, identificador)
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toUsuario() else null
        }
    }

    fun actualizarContrasenaUsuario(idUsuario: Long, contrasenaNueva: String) {
        writableDatabase.update(
            "usuarios",
            ContentValues().apply {
                put("contrasena_hash", contrasenaNueva.trim())
            },
            "id_usuario = ?",
            arrayOf(idUsuario.toString())
        )
    }

    fun insertarUsuario(usuario: Usuario): Long {
        return writableDatabase.insert("usuarios", null, ContentValues().apply {
            put("run", usuario.run)
            put("dv", usuario.dv)
            put("username", usuario.username)
            put("nombre", usuario.nombre)
            put("apellido_paterno", usuario.apellidoPaterno)
            put("apellido_materno", usuario.apellidoMaterno)
            put("telefono", usuario.telefono)
            put("correo", usuario.correo)
            put("contrasena_hash", usuario.contrasenaHash)
            put("fecha_registro", usuario.fechaRegistro)
            put("fecha_nacimiento", usuario.fechaNacimiento)
            put("verificado", usuario.verificado.toInt())
            put("tipo_perfil", usuario.tipoPerfil)
            put("numero_documento_identidad", usuario.numeroDocumentoIdentidad)
            put("pregunta_recuperacion", usuario.preguntaRecuperacion)
            put("respuesta_recuperacion", usuario.respuestaRecuperacion)
            put("verificacion_trabajador_pendiente", usuario.verificacionTrabajadorPendiente.toInt())
            put("fecha_solicitud_verificacion_ms", usuario.fechaSolicitudVerificacionMs)
            put("foto_perfil", usuario.fotoPerfilUrl)
        })
    }

    fun actualizarFotoPerfilUsuario(idUsuario: Long, fotoPerfil: String) {
        writableDatabase.update(
            "usuarios",
            ContentValues().apply {
                put("foto_perfil", fotoPerfil.trim())
            },
            "id_usuario = ?",
            arrayOf(idUsuario.toString())
        )
    }

    fun existeUsuario(correo: String, username: String): Boolean {
        readableDatabase.rawQuery(
            "SELECT id_usuario FROM usuarios WHERE correo = ? OR username = ? LIMIT 1",
            arrayOf(correo, username)
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    fun existeRun(run: String, dv: String): Boolean {
        readableDatabase.rawQuery(
            "SELECT id_usuario FROM usuarios WHERE run = ? AND dv = ? LIMIT 1",
            arrayOf(run, dv)
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    fun obtenerUsuarioPorId(idUsuario: Long): Usuario? {
        readableDatabase.rawQuery(
            "SELECT * FROM usuarios WHERE id_usuario = ? LIMIT 1",
            arrayOf(idUsuario.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toUsuario() else null
        }
    }

    fun guardarSesion(idUsuario: Long, recordarme: Boolean) {
        writableDatabase.execSQL("UPDATE sesiones_locales SET activa = 0")
        val ahoraMs = System.currentTimeMillis()
        writableDatabase.insert("sesiones_locales", null, ContentValues().apply {
            put("id_usuario", idUsuario)
            put("token_local", "sesion-$idUsuario-${System.currentTimeMillis()}")
            put("fecha_inicio", ahora())
            put("fecha_ultimo_acceso", ahora())
            put("fecha_inicio_ms", ahoraMs)
            put("fecha_ultimo_acceso_ms", ahoraMs)
            put("recordarme", recordarme.toInt())
            put("activa", 1)
        })
    }

    fun cerrarSesion() {
        writableDatabase.execSQL("UPDATE sesiones_locales SET activa = 0")
    }

    fun obtenerUsuarioSesionActiva(): Usuario? {
        readableDatabase.rawQuery(
            "SELECT u.*, s.id_sesion_local, s.recordarme, s.fecha_ultimo_acceso_ms FROM sesiones_locales s " +
                "INNER JOIN usuarios u ON u.id_usuario = s.id_usuario " +
                "WHERE s.activa = 1 ORDER BY s.id_sesion_local DESC LIMIT 1",
            emptyArray()
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null

            val idSesion = cursor.getLong(cursor.getColumnIndexOrThrow("id_sesion_local"))
            val idUsuario = cursor.getLong(cursor.getColumnIndexOrThrow("id_usuario"))
            val recordarme = cursor.getInt(cursor.getColumnIndexOrThrow("recordarme")) == 1
            val ultimoAcceso = cursor.getLong(cursor.getColumnIndexOrThrow("fecha_ultimo_acceso_ms"))

            if (!recordarme && System.currentTimeMillis() - ultimoAcceso > SESION_EXPIRACION_MS) {
                writableDatabase.update(
                    "sesiones_locales",
                    ContentValues().apply { put("activa", 0) },
                    "id_sesion_local = ?",
                    arrayOf(idSesion.toString())
                )
                return null
            }

            writableDatabase.update(
                "sesiones_locales",
                ContentValues().apply {
                    put("fecha_ultimo_acceso", ahora())
                    put("fecha_ultimo_acceso_ms", System.currentTimeMillis())
                },
                "id_sesion_local = ?",
                arrayOf(idSesion.toString())
            )

            procesarVerificacionTrabajadorPendiente(idUsuario)
            return obtenerUsuarioPorId(idUsuario)
        }
    }

    fun solicitarVerificacionTrabajador(
        idUsuario: Long,
        run: String,
        dv: String,
        numeroDocumento: String
    ): Result<Unit> {
        val usuario = obtenerUsuarioPorId(idUsuario)
            ?: return Result.failure(IllegalStateException("No existe el usuario activo"))
        if (usuario.run != run || usuario.dv.lowercase() != dv.lowercase()) {
            return Result.failure(IllegalArgumentException("El RUN ingresado no coincide con tu registro"))
        }
        val documentoNormalizado = numeroDocumento.filter { it.isDigit() }.take(9)
        if (documentoNormalizado.length != 9) {
            return Result.failure(IllegalArgumentException("El numero de documento debe tener 9 digitos"))
        }

        val actualizado = writableDatabase.update(
            "usuarios",
            ContentValues().apply {
                put("numero_documento_identidad", documentoNormalizado)
                put("verificacion_trabajador_pendiente", 1)
                put("fecha_solicitud_verificacion_ms", System.currentTimeMillis())
            },
            "id_usuario = ?",
            arrayOf(idUsuario.toString())
        )
        return if (actualizado > 0) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("No se pudo iniciar la verificacion"))
        }
    }

    private fun procesarVerificacionTrabajadorPendiente(idUsuario: Long) {
        readableDatabase.rawQuery(
            "SELECT verificacion_trabajador_pendiente, fecha_solicitud_verificacion_ms FROM usuarios WHERE id_usuario = ? LIMIT 1",
            arrayOf(idUsuario.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return
            val pendiente = cursor.getInt(cursor.getColumnIndexOrThrow("verificacion_trabajador_pendiente")) == 1
            if (!pendiente) return

            val indexMs = cursor.getColumnIndex("fecha_solicitud_verificacion_ms")
            val solicitudMs = if (indexMs >= 0 && !cursor.isNull(indexMs)) cursor.getLong(indexMs) else 0L
            if (solicitudMs <= 0L) return

            if (System.currentTimeMillis() - solicitudMs >= VERIFICACION_TRABAJADOR_MS) {
                writableDatabase.update(
                    "usuarios",
                    ContentValues().apply {
                        put("tipo_perfil", TipoPerfil.TRABAJADOR)
                        put("verificado", 1)
                        put("verificacion_trabajador_pendiente", 0)
                    },
                    "id_usuario = ?",
                    arrayOf(idUsuario.toString())
                )
            }
        }
    }

    fun obtenerOfertaPrincipal(): OfertaServicio? {
        return obtenerOfertasMarketplace().firstOrNull()
    }

    fun obtenerOfertasMarketplace(busqueda: String = ""): List<OfertaServicio> {
        val filtros = busqueda.trim()
        val where = if (filtros.isBlank()) {
            " WHERE o.disponible = 1 AND o.eliminada = 0"
        } else {
            " WHERE o.disponible = 1 AND o.eliminada = 0 AND (" +
                "o.titulo LIKE ? OR " +
                "o.descripcion LIKE ? OR " +
                "o.detalle LIKE ? OR " +
                "cat.nombre LIKE ? OR " +
                "(u.nombre || ' ' || u.apellido_paterno) LIKE ?)"
        }
        val args = if (filtros.isBlank()) {
            emptyArray()
        } else {
            Array(5) { "%$filtros%" }
        }

        readableDatabase.rawQuery(
            consultaOfertaSelect + consultaOfertaJoins + where + consultaOfertaGroupBy + " ORDER BY o.id_oferta_servicio DESC",
            args
        ).use { cursor ->
            val ofertas = mutableListOf<OfertaServicio>()
            while (cursor.moveToNext()) ofertas += cursor.toOfertaServicio()
            return ofertas
        }
    }

    fun obtenerOfertaPorId(idOfertaServicio: Long, incluirEliminadas: Boolean = false): OfertaServicio? {
        val filtroEliminadas = if (incluirEliminadas) "" else " AND o.eliminada = 0 "
        readableDatabase.rawQuery(
            consultaOfertaSelect + consultaOfertaJoins +
                " WHERE o.id_oferta_servicio = ? $filtroEliminadas " +
                consultaOfertaGroupBy + " LIMIT 1",
            arrayOf(idOfertaServicio.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toOfertaServicio() else null
        }
    }

    fun obtenerOfertaPorTrabajador(idTrabajador: Long): OfertaServicio? {
        readableDatabase.rawQuery(
            consultaOfertaSelect + consultaOfertaJoins +
                " WHERE o.id_trabajador = ? AND o.eliminada = 0 " +
                consultaOfertaGroupBy + " ORDER BY o.id_oferta_servicio DESC LIMIT 1",
            arrayOf(idTrabajador.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toOfertaServicio() else null
        }
    }

    fun obtenerOfertasPorTrabajador(idTrabajador: Long, incluirEliminadas: Boolean = false): List<OfertaServicio> {
        val filtroEliminadas = if (incluirEliminadas) "" else " AND o.eliminada = 0 "
        readableDatabase.rawQuery(
            consultaOfertaSelect + consultaOfertaJoins +
                " WHERE o.id_trabajador = ? $filtroEliminadas " +
                consultaOfertaGroupBy + " ORDER BY o.id_oferta_servicio DESC",
            arrayOf(idTrabajador.toString())
        ).use { cursor ->
            val ofertas = mutableListOf<OfertaServicio>()
            while (cursor.moveToNext()) ofertas += cursor.toOfertaServicio()
            return ofertas
        }
    }

    fun contarOfertasPorTrabajador(idTrabajador: Long): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) AS total FROM ofertas_servicio WHERE id_trabajador = ? AND eliminada = 0",
            arrayOf(idTrabajador.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return 0
            return cursor.getInt(cursor.getColumnIndexOrThrow("total"))
        }
    }

    fun contarOfertasActivasPorTrabajador(idTrabajador: Long): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) AS total FROM ofertas_servicio WHERE id_trabajador = ? AND disponible = 1 AND eliminada = 0",
            arrayOf(idTrabajador.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return 0
            return cursor.getInt(cursor.getColumnIndexOrThrow("total"))
        }
    }

    fun contarOfertasActivasPorTrabajadorExcluyendo(
        idTrabajador: Long,
        idOfertaExcluir: Long
    ): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) AS total FROM ofertas_servicio WHERE id_trabajador = ? AND disponible = 1 AND eliminada = 0 AND id_oferta_servicio <> ?",
            arrayOf(idTrabajador.toString(), idOfertaExcluir.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return 0
            return cursor.getInt(cursor.getColumnIndexOrThrow("total"))
        }
    }

    fun obtenerIdsOfertasConTrabajoEnCursoPorTrabajador(idTrabajador: Long): List<Long> {
        readableDatabase.rawQuery(
            "SELECT DISTINCT c.id_oferta_servicio AS id_oferta_servicio " +
                "FROM citas_servicio cs " +
                "INNER JOIN chats_cita c ON c.id_chat_cita = cs.id_chat_cita " +
                "WHERE c.id_trabajador = ? AND cs.estado_cita = ? AND c.id_oferta_servicio IS NOT NULL",
            arrayOf(idTrabajador.toString(), EstadoCita.EN_PROCESO.toString())
        ).use { cursor ->
            val ids = mutableListOf<Long>()
            while (cursor.moveToNext()) {
                ids += cursor.getLong(cursor.getColumnIndexOrThrow("id_oferta_servicio"))
            }
            return ids
        }
    }

    fun existeTrabajoEnCursoPorOferta(idOfertaServicio: Long): Boolean {
        readableDatabase.rawQuery(
            "SELECT 1 FROM citas_servicio cs " +
                "INNER JOIN chats_cita c ON c.id_chat_cita = cs.id_chat_cita " +
                "WHERE c.id_oferta_servicio = ? AND cs.estado_cita = ? LIMIT 1",
            arrayOf(idOfertaServicio.toString(), EstadoCita.EN_PROCESO.toString())
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    fun obtenerPreguntasSeguridad(idUsuario: Long): List<PreguntaSeguridadConfig> {
        val configuradas = mutableMapOf<Int, PreguntaSeguridadConfig>()
        readableDatabase.rawQuery(
            "SELECT indice, pregunta, respuesta FROM preguntas_seguridad WHERE id_usuario = ? ORDER BY indice ASC",
            arrayOf(idUsuario.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val indice = cursor.getInt(cursor.getColumnIndexOrThrow("indice"))
                configuradas[indice] = PreguntaSeguridadConfig(
                    indice = indice,
                    pregunta = cursor.getString(cursor.getColumnIndexOrThrow("pregunta")),
                    respuesta = cursor.getString(cursor.getColumnIndexOrThrow("respuesta"))
                )
            }
        }

        return (1..2).map { indice ->
            configuradas[indice] ?: PreguntaSeguridadConfig(indice = indice)
        }
    }

    fun guardarPreguntaSeguridad(idUsuario: Long, indice: Int, pregunta: String, respuesta: String) {
        val values = ContentValues().apply {
            put("id_usuario", idUsuario)
            put("indice", indice)
            put("pregunta", pregunta)
            put("respuesta", respuesta)
            put("fecha_actualizacion", ahora())
        }

        val actualizadas = writableDatabase.update(
            "preguntas_seguridad",
            values,
            "id_usuario = ? AND indice = ?",
            arrayOf(idUsuario.toString(), indice.toString())
        )
        if (actualizadas == 0) {
            writableDatabase.insert("preguntas_seguridad", null, values)
        }
    }

    fun obtenerUbicacionUsuario(idUsuario: Long): UbicacionAjustesConfig {
        readableDatabase.rawQuery(
            "SELECT region, comuna, calle, numero, detalle, latitud, longitud, rango_disponibilidad_m, rango_busqueda_m " +
                "FROM ubicaciones_usuario WHERE id_usuario = ? LIMIT 1",
            arrayOf(idUsuario.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                return UbicacionAjustesConfig()
            }
            return UbicacionAjustesConfig(
                region = cursor.getString(cursor.getColumnIndexOrThrow("region")),
                comuna = cursor.getString(cursor.getColumnIndexOrThrow("comuna")),
                calle = cursor.getString(cursor.getColumnIndexOrThrow("calle")),
                numero = cursor.getString(cursor.getColumnIndexOrThrow("numero")),
                detalle = cursor.getString(cursor.getColumnIndexOrThrow("detalle")),
                latitud = cursor.getDoubleNullable("latitud"),
                longitud = cursor.getDoubleNullable("longitud"),
                rangoDisponibilidadM = cursor.getInt(cursor.getColumnIndexOrThrow("rango_disponibilidad_m")).coerceAtLeast(20),
                rangoBusquedaM = cursor.getInt(cursor.getColumnIndexOrThrow("rango_busqueda_m")).coerceAtLeast(20)
            )
        }
    }

    fun guardarUbicacionUsuario(idUsuario: Long, config: UbicacionAjustesConfig) {
        val values = ContentValues().apply {
            put("id_usuario", idUsuario)
            put("region", config.region)
            put("comuna", config.comuna)
            put("calle", config.calle)
            put("numero", config.numero)
            put("detalle", config.detalle)
            put("latitud", config.latitud)
            put("longitud", config.longitud)
            put("rango_disponibilidad_m", config.rangoDisponibilidadM.coerceAtLeast(20))
            put("rango_busqueda_m", config.rangoBusquedaM.coerceAtLeast(20))
            put("fecha_actualizacion", ahora())
        }
        val actualizadas = writableDatabase.update(
            "ubicaciones_usuario",
            values,
            "id_usuario = ?",
            arrayOf(idUsuario.toString())
        )
        if (actualizadas == 0) {
            writableDatabase.insert("ubicaciones_usuario", null, values)
        }
    }

    fun obtenerFiltrosMarketplace(idUsuario: Long): FiltroMarketplaceConfig {
        readableDatabase.rawQuery(
            "SELECT filtro_categoria_id, filtro_tipo_precio, solo_trabajador_verificado, orden_marketplace, filtro_zona_comuna_activo, comuna_filtro " +
                "FROM filtros_marketplace WHERE id_usuario = ? LIMIT 1",
            arrayOf(idUsuario.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return FiltroMarketplaceConfig()
            return FiltroMarketplaceConfig(
                categoriaId = cursor.getLongNullable("filtro_categoria_id"),
                tipoPrecio = cursor.getIntNullable("filtro_tipo_precio"),
                soloTrabajadorVerificado = cursor.getInt(cursor.getColumnIndexOrThrow("solo_trabajador_verificado")) == 1,
                ordenMarketplace = cursor.getStringNullable("orden_marketplace").orEmpty().ifBlank { "FECHA_RECIENTES" },
                filtroZonaComunaActivo = cursor.getInt(cursor.getColumnIndexOrThrow("filtro_zona_comuna_activo")) == 1,
                comunaFiltro = cursor.getStringNullable("comuna_filtro").orEmpty()
            )
        }
    }

    fun guardarFiltrosMarketplace(idUsuario: Long, config: FiltroMarketplaceConfig) {
        val values = ContentValues().apply {
            put("id_usuario", idUsuario)
            if (config.categoriaId == null) putNull("filtro_categoria_id") else put("filtro_categoria_id", config.categoriaId)
            if (config.tipoPrecio == null) putNull("filtro_tipo_precio") else put("filtro_tipo_precio", config.tipoPrecio)
            put("solo_trabajador_verificado", config.soloTrabajadorVerificado.toInt())
            put("orden_marketplace", config.ordenMarketplace.ifBlank { "FECHA_RECIENTES" })
            put("filtro_zona_comuna_activo", config.filtroZonaComunaActivo.toInt())
            put("comuna_filtro", config.comunaFiltro)
            put("fecha_actualizacion", ahora())
        }
        writableDatabase.insertWithOnConflict(
            "filtros_marketplace",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun limpiarFiltrosMarketplace(idUsuario: Long) {
        writableDatabase.delete(
            "filtros_marketplace",
            "id_usuario = ?",
            arrayOf(idUsuario.toString())
        )
    }

    fun obtenerCategoriasServicio(): List<CategoriaServicio> {
        readableDatabase.rawQuery(
            "SELECT * FROM categorias_servicio ORDER BY nombre ASC",
            emptyArray()
        ).use { cursor ->
            val categorias = mutableListOf<CategoriaServicio>()
            while (cursor.moveToNext()) {
                categorias += CategoriaServicio(
                    idCategoriaServicio = cursor.getLong(cursor.getColumnIndexOrThrow("id_categoria_servicio")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                )
            }
            return categorias
        }
    }

    fun insertarFotoServicio(foto: FotoServicioLocal): Long {
        return writableDatabase.insert("fotos", null, ContentValues().apply {
            put("fecha_subida", ahora())
            put("enlace", foto.uriLocal)
            put("detalle", "Foto local lista para futura sincronizacion con backend")
            put("nombre_archivo", foto.nombreArchivo)
            put("mime_type", foto.mimeType)
            put("estado_sincronizacion", if (foto.pendienteSincronizacion) "pendiente" else "sincronizada")
            put("url_remota", foto.urlRemota)
        })
    }

    fun actualizarFotoServicio(idFoto: Long, foto: FotoServicioLocal) {
        writableDatabase.update(
            "fotos",
            ContentValues().apply {
                put("enlace", foto.uriLocal)
                put("detalle", "Foto local lista para futura sincronizacion con backend")
                put("nombre_archivo", foto.nombreArchivo)
                put("mime_type", foto.mimeType)
                put("estado_sincronizacion", if (foto.pendienteSincronizacion) "pendiente" else "sincronizada")
                put("url_remota", foto.urlRemota)
            },
            "id_foto = ?",
            arrayOf(idFoto.toString())
        )
    }

    fun insertarOfertaServicio(idTrabajador: Long, formulario: FormularioServicio, idFotoPortada: Long?): Long {
        val tipoPrecio = formulario.tipoPrecio
        val montoNormalizado = PrecioUtils.normalizarMonto(tipoPrecio, formulario.montoBase)
        return writableDatabase.insert("ofertas_servicio", null, ContentValues().apply {
            put("titulo", formulario.titulo.trim())
            put("descripcion", formulario.descripcion.trim())
            put("detalle", formulario.descripcion.trim())
            put("precio_texto", PrecioUtils.construirPrecioTexto(tipoPrecio, montoNormalizado))
            put("tipo_precio", tipoPrecio)
            put("monto_base", montoNormalizado)
            put("disponible", formulario.disponible.toInt())
            put("eliminada", 0)
            putNull("fecha_eliminacion")
            put("fecha_publicacion", ahora())
            put("id_categoria_servicio", formulario.idCategoriaServicio)
            put("id_trabajador", idTrabajador)
            put("id_foto_portada", idFotoPortada)
        })
    }

    fun actualizarOfertaServicio(idOfertaServicio: Long, formulario: FormularioServicio, idFotoPortada: Long?) {
        val tipoPrecio = formulario.tipoPrecio
        val montoNormalizado = PrecioUtils.normalizarMonto(tipoPrecio, formulario.montoBase)
        writableDatabase.update(
            "ofertas_servicio",
            ContentValues().apply {
                put("titulo", formulario.titulo.trim())
                put("descripcion", formulario.descripcion.trim())
                put("detalle", formulario.descripcion.trim())
                put("precio_texto", PrecioUtils.construirPrecioTexto(tipoPrecio, montoNormalizado))
                put("tipo_precio", tipoPrecio)
                put("monto_base", montoNormalizado)
                put("disponible", formulario.disponible.toInt())
                put("id_categoria_servicio", formulario.idCategoriaServicio)
                put("id_foto_portada", idFotoPortada)
            },
            "id_oferta_servicio = ?",
            arrayOf(idOfertaServicio.toString())
        )
    }

    fun actualizarDisponibilidadOferta(idOfertaServicio: Long, disponible: Boolean) {
        writableDatabase.update(
            "ofertas_servicio",
            ContentValues().apply {
                put("disponible", disponible.toInt())
            },
            "id_oferta_servicio = ?",
            arrayOf(idOfertaServicio.toString())
        )
    }

    fun eliminarOfertaServicio(idOfertaServicio: Long) {
        writableDatabase.update(
            "ofertas_servicio",
            ContentValues().apply {
                put("eliminada", 1)
                put("disponible", 0)
                put("fecha_eliminacion", ahora())
            },
            "id_oferta_servicio = ?",
            arrayOf(idOfertaServicio.toString())
        )
    }

    fun obtenerChatsParaUsuario(idUsuario: Long): List<ChatCita> {
        readableDatabase.rawQuery(
            "SELECT c.id_chat_cita, c.fecha_creacion, c.id_trabajador, c.id_cliente, c.id_oferta_servicio, c.id_cita, c.cerrado, c.bloqueado_hasta_ms, " +
                "CASE WHEN c.id_cliente = ? THEN ut.nombre || ' ' || ut.apellido_paterno " +
                "ELSE uc.nombre || ' ' || uc.apellido_paterno END AS nombre_contacto, " +
                "CASE WHEN c.id_cliente = ? THEN ut.username ELSE uc.username END AS username_contacto, " +
                "COALESCE(os.titulo, 'Servicio') AS titulo_servicio, " +
                "COALESCE(cat.nombre, '') AS categoria_servicio, " +
                "COALESCE(m.contenido, '') AS ultimo_mensaje, " +
                "COALESCE(m.fecha_envio, c.fecha_creacion) AS hora_ultimo_mensaje, " +
                "COALESCE(cs.estado_cita, NULL) AS estado_cita, " +
                "(SELECT COUNT(1) FROM mensajes_chat mx " +
                "WHERE mx.id_chat_cita = c.id_chat_cita AND mx.id_receptor = ? AND mx.fecha_leido IS NULL) AS mensajes_no_leidos " +
                "FROM chats_cita c " +
                "INNER JOIN usuarios ut ON ut.id_usuario = c.id_trabajador " +
                "INNER JOIN usuarios uc ON uc.id_usuario = c.id_cliente " +
                "LEFT JOIN mensajes_chat m ON m.id_mensaje_chat = (" +
                "SELECT id_mensaje_chat FROM mensajes_chat WHERE id_chat_cita = c.id_chat_cita " +
                "ORDER BY id_mensaje_chat DESC LIMIT 1) " +
                "LEFT JOIN ofertas_servicio os ON os.id_oferta_servicio = c.id_oferta_servicio " +
                "LEFT JOIN categorias_servicio cat ON cat.id_categoria_servicio = os.id_categoria_servicio " +
                "LEFT JOIN citas_servicio cs ON cs.id_chat_cita = c.id_chat_cita " +
                "WHERE c.id_trabajador = ? OR c.id_cliente = ? " +
                "ORDER BY hora_ultimo_mensaje DESC",
            arrayOf(
                idUsuario.toString(),
                idUsuario.toString(),
                idUsuario.toString(),
                idUsuario.toString(),
                idUsuario.toString()
            )
        ).use { cursor ->
            val chats = mutableListOf<ChatCita>()
            while (cursor.moveToNext()) chats += cursor.toChatCita()
            return chats
        }
    }

    fun obtenerMensajesPorChat(idChatCita: Long): List<MensajeChat> {
        readableDatabase.rawQuery(
            "SELECT * FROM mensajes_chat WHERE id_chat_cita = ? ORDER BY id_mensaje_chat ASC",
            arrayOf(idChatCita.toString())
        ).use { cursor ->
            val mensajes = mutableListOf<MensajeChat>()
            while (cursor.moveToNext()) mensajes += cursor.toMensajeChat()
            return mensajes
        }
    }

    fun obtenerChatPorId(idChatCita: Long, idUsuario: Long): ChatCita? {
        readableDatabase.rawQuery(
            "SELECT c.id_chat_cita, c.fecha_creacion, c.id_trabajador, c.id_cliente, c.id_oferta_servicio, c.id_cita, c.cerrado, c.bloqueado_hasta_ms, " +
                "CASE WHEN c.id_cliente = ? THEN ut.nombre || ' ' || ut.apellido_paterno " +
                "ELSE uc.nombre || ' ' || uc.apellido_paterno END AS nombre_contacto, " +
                "CASE WHEN c.id_cliente = ? THEN ut.username ELSE uc.username END AS username_contacto, " +
                "COALESCE(os.titulo, 'Servicio') AS titulo_servicio, " +
                "COALESCE(cat.nombre, '') AS categoria_servicio, " +
                "COALESCE(m.contenido, '') AS ultimo_mensaje, " +
                "COALESCE(m.fecha_envio, c.fecha_creacion) AS hora_ultimo_mensaje, " +
                "COALESCE(cs.estado_cita, NULL) AS estado_cita, " +
                "(SELECT COUNT(1) FROM mensajes_chat mx " +
                "WHERE mx.id_chat_cita = c.id_chat_cita AND mx.id_receptor = ? AND mx.fecha_leido IS NULL) AS mensajes_no_leidos " +
                "FROM chats_cita c " +
                "INNER JOIN usuarios ut ON ut.id_usuario = c.id_trabajador " +
                "INNER JOIN usuarios uc ON uc.id_usuario = c.id_cliente " +
                "LEFT JOIN mensajes_chat m ON m.id_mensaje_chat = (" +
                "SELECT id_mensaje_chat FROM mensajes_chat WHERE id_chat_cita = c.id_chat_cita " +
                "ORDER BY id_mensaje_chat DESC LIMIT 1) " +
                "LEFT JOIN ofertas_servicio os ON os.id_oferta_servicio = c.id_oferta_servicio " +
                "LEFT JOIN categorias_servicio cat ON cat.id_categoria_servicio = os.id_categoria_servicio " +
                "LEFT JOIN citas_servicio cs ON cs.id_chat_cita = c.id_chat_cita " +
                "WHERE c.id_chat_cita = ? LIMIT 1",
            arrayOf(idUsuario.toString(), idUsuario.toString(), idUsuario.toString(), idChatCita.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toChatCita() else null
        }
    }

    fun obtenerOfertaParaContacto(idOfertaServicio: Long): Pair<Long, Long>? {
        readableDatabase.rawQuery(
            "SELECT id_trabajador, id_cliente FROM ofertas_servicio WHERE id_oferta_servicio = ? AND eliminada = 0 LIMIT 1",
            arrayOf(idOfertaServicio.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val idTrabajador = cursor.getLong(cursor.getColumnIndexOrThrow("id_trabajador"))
            val idCliente = cursor.getLongNullable("id_cliente") ?: 0L
            return idTrabajador to idCliente
        }
    }

    fun obtenerChatEntreUsuarios(idTrabajador: Long, idCliente: Long, idOfertaServicio: Long): Long? {
        readableDatabase.rawQuery(
            "SELECT id_chat_cita FROM chats_cita " +
                "WHERE id_trabajador = ? AND id_cliente = ? AND id_oferta_servicio = ? " +
                "ORDER BY id_chat_cita DESC LIMIT 1",
            arrayOf(idTrabajador.toString(), idCliente.toString(), idOfertaServicio.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getLong(cursor.getColumnIndexOrThrow("id_chat_cita")) else null
        }
    }

    fun crearChatCita(idTrabajador: Long, idCliente: Long, idOfertaServicio: Long): Long {
        return writableDatabase.insert("chats_cita", null, ContentValues().apply {
            put("fecha_creacion", ahora())
            put("id_trabajador", idTrabajador)
            put("id_cliente", idCliente)
            put("id_oferta_servicio", idOfertaServicio)
            put("cerrado", 0)
            putNull("bloqueado_hasta_ms")
        })
    }

    fun insertarMensajeChat(
        idChatCita: Long,
        idEmisor: Long,
        idReceptor: Long,
        contenido: String
    ): Long {
        return writableDatabase.insert("mensajes_chat", null, ContentValues().apply {
            put("fecha_envio", ahora())
            putNull("fecha_recibido")
            putNull("fecha_leido")
            put("id_emisor", idEmisor)
            put("id_receptor", idReceptor)
            put("id_chat_cita", idChatCita)
            put("id_estado", EstadoCodigo.MSG_ENVIADO)
            put("notificado_local", 0)
            put("contenido", contenido.trim())
        })
    }

    fun obtenerMensajePorId(idMensajeChat: Long): MensajeChat? {
        readableDatabase.rawQuery(
            "SELECT * FROM mensajes_chat WHERE id_mensaje_chat = ? LIMIT 1",
            arrayOf(idMensajeChat.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toMensajeChat() else null
        }
    }

    fun marcarMensajesRecibidos(idReceptor: Long) {
        writableDatabase.update(
            "mensajes_chat",
            ContentValues().apply {
                put("fecha_recibido", ahora())
                put("id_estado", EstadoCodigo.MSG_ENTREGADO)
            },
            "id_receptor = ? AND fecha_recibido IS NULL",
            arrayOf(idReceptor.toString())
        )
    }

    fun marcarMensajesLeidos(idChatCita: Long, idReceptor: Long) {
        writableDatabase.update(
            "mensajes_chat",
            ContentValues().apply {
                val fechaActual = ahora()
                put("fecha_leido", fechaActual)
                put("fecha_recibido", fechaActual)
                put("id_estado", EstadoCodigo.MSG_LEIDO)
                put("notificado_local", 1)
            },
            "id_chat_cita = ? AND id_receptor = ? AND fecha_leido IS NULL",
            arrayOf(idChatCita.toString(), idReceptor.toString())
        )
    }

    fun obtenerMensajesPendientesNotificacion(idReceptor: Long): List<NotificacionMensajePendiente> {
        readableDatabase.rawQuery(
            "SELECT m.id_mensaje_chat, m.id_chat_cita, " +
                "(COALESCE(os.titulo, 'Servicio') || ' - ' || COALESCE(u.username, 'usuario')) AS titulo_notificacion, " +
                "m.contenido " +
                "FROM mensajes_chat m " +
                "LEFT JOIN chats_cita c ON c.id_chat_cita = m.id_chat_cita " +
                "LEFT JOIN ofertas_servicio os ON os.id_oferta_servicio = c.id_oferta_servicio " +
                "LEFT JOIN usuarios u ON u.id_usuario = m.id_emisor " +
                "WHERE m.id_receptor = ? AND m.fecha_leido IS NULL AND m.notificado_local = 0 " +
                "ORDER BY m.id_mensaje_chat ASC",
            arrayOf(idReceptor.toString())
        ).use { cursor ->
            val lista = mutableListOf<NotificacionMensajePendiente>()
            while (cursor.moveToNext()) {
                lista += cursor.toNotificacionMensajePendiente()
            }
            return lista
        }
    }

    fun marcarMensajesNotificados(idsMensaje: List<Long>) {
        if (idsMensaje.isEmpty()) return
        val marcadores = idsMensaje.joinToString(",") { "?" }
        writableDatabase.execSQL(
            "UPDATE mensajes_chat SET notificado_local = 1 WHERE id_mensaje_chat IN ($marcadores)",
            idsMensaje.map { it.toString() }.toTypedArray()
        )
    }

    fun crearCitaServicio(
        idChatCita: Long,
        fechaProgramada: String,
        comentario: String,
        precioAcordado: Int
    ): Long {
        val idCita = writableDatabase.insert("citas_servicio", null, ContentValues().apply {
            put("id_chat_cita", idChatCita)
            put("fecha_creacion", ahora())
            put("fecha_programada", fechaProgramada)
            put("comentario", comentario.trim())
            put("precio_acordado", precioAcordado)
            putNull("fecha_inicio_trabajo")
            putNull("fecha_fin_trabajo")
            put("estado_cita", EstadoCita.PENDIENTE)
            put("fecha_actualizacion", ahora())
        })
        if (idCita > 0) {
            writableDatabase.update(
                "chats_cita",
                ContentValues().apply { put("id_cita", idCita) },
                "id_chat_cita = ?",
                arrayOf(idChatCita.toString())
            )
        }
        return idCita
    }

    fun obtenerCitaPorChat(idChatCita: Long): CitaServicio? {
        readableDatabase.rawQuery(
            "SELECT * FROM citas_servicio WHERE id_chat_cita = ? LIMIT 1",
            arrayOf(idChatCita.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toCitaServicio() else null
        }
    }

    fun actualizarEstadoCita(idCita: Long, nuevoEstado: Int): Boolean {
        return actualizarEstadoCita(
            idCita = idCita,
            nuevoEstado = nuevoEstado,
            fechaInicioTrabajo = null,
            fechaFinTrabajo = null
        )
    }

    fun actualizarEstadoCita(
        idCita: Long,
        nuevoEstado: Int,
        fechaInicioTrabajo: String?,
        fechaFinTrabajo: String?
    ): Boolean {
        val actualizadas = writableDatabase.update(
            "citas_servicio",
            ContentValues().apply {
                put("estado_cita", nuevoEstado)
                if (fechaInicioTrabajo != null) put("fecha_inicio_trabajo", fechaInicioTrabajo)
                if (fechaFinTrabajo != null) put("fecha_fin_trabajo", fechaFinTrabajo)
                put("fecha_actualizacion", ahora())
            },
            "id_cita = ?",
            arrayOf(idCita.toString())
        )
        return actualizadas > 0
    }

    fun actualizarChatCerrado(idChatCita: Long, cerrado: Boolean, bloqueadoHastaMs: Long?): Boolean {
        val actualizadas = writableDatabase.update(
            "chats_cita",
            ContentValues().apply {
                put("cerrado", if (cerrado) 1 else 0)
                if (bloqueadoHastaMs != null) put("bloqueado_hasta_ms", bloqueadoHastaMs) else putNull("bloqueado_hasta_ms")
            },
            "id_chat_cita = ?",
            arrayOf(idChatCita.toString())
        )
        return actualizadas > 0
    }

    fun existeCitaEnProcesoTrabajador(idTrabajador: Long, idCitaExcluir: Long): Boolean {
        readableDatabase.rawQuery(
            "SELECT 1 FROM citas_servicio cs " +
                "INNER JOIN chats_cita c ON c.id_chat_cita = cs.id_chat_cita " +
                "WHERE c.id_trabajador = ? AND cs.estado_cita = ? AND cs.id_cita <> ? LIMIT 1",
            arrayOf(idTrabajador.toString(), EstadoCita.EN_PROCESO.toString(), idCitaExcluir.toString())
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    fun existeValoracionPorChatCliente(idChatCita: Long, idCliente: Long): Boolean {
        readableDatabase.rawQuery(
            "SELECT 1 FROM valoraciones WHERE id_chat_cita = ? AND id_cliente = ? LIMIT 1",
            arrayOf(idChatCita.toString(), idCliente.toString())
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    fun obtenerValoracionPorChat(idChatCita: Long, idCliente: Long): Valoracion? {
        readableDatabase.rawQuery(
            "SELECT * FROM valoraciones WHERE id_chat_cita = ? AND id_cliente = ? LIMIT 1",
            arrayOf(idChatCita.toString(), idCliente.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toValoracion() else null
        }
    }

    fun obtenerValoracionPorId(idValoracion: Long): Valoracion? {
        readableDatabase.rawQuery(
            "SELECT * FROM valoraciones WHERE id_valoracion = ? LIMIT 1",
            arrayOf(idValoracion.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toValoracion() else null
        }
    }

    fun obtenerValoracionesPorOferta(idOfertaServicio: Long): List<Valoracion> {
        readableDatabase.rawQuery(
            "SELECT v.*, " +
                "uc.username AS username_cliente, " +
                "cs.fecha_fin_trabajo AS fecha_finalizacion_cita " +
                "FROM valoraciones v " +
                "LEFT JOIN usuarios uc ON uc.id_usuario = v.id_cliente " +
                "LEFT JOIN citas_servicio cs ON cs.id_chat_cita = v.id_chat_cita " +
                "WHERE v.id_oferta_servicio = ? " +
                "ORDER BY v.id_valoracion DESC",
            arrayOf(idOfertaServicio.toString())
        ).use { cursor ->
            val valoraciones = mutableListOf<Valoracion>()
            while (cursor.moveToNext()) valoraciones += cursor.toValoracion()
            return valoraciones
        }
    }

    fun insertarValoracion(
        voto: Int,
        comentario: String,
        idTrabajador: Long,
        idCliente: Long,
        idChatCita: Long,
        idOfertaServicio: Long
    ): Long {
        return writableDatabase.insert("valoraciones", null, ContentValues().apply {
            put("voto", voto.coerceIn(1, 5))
            put("fecha_voto", ahora())
            put("comentario", comentario.ifBlank { "Sin comentarios" })
            put("id_trabajador", idTrabajador)
            put("id_cliente", idCliente)
            put("id_chat_cita", idChatCita)
            put("id_oferta_servicio", idOfertaServicio)
        })
    }

    private fun sembrarDatosIniciales(db: SQLiteDatabase) {
        listOf(
            listOf(101, "USR_ACTIVO", "Usuario activo", "Usuario habilitado para operar en la plataforma."),
            listOf(102, "USR_SUSPENDIDO", "Usuario suspendido", "Usuario suspendido temporalmente."),
            listOf(103, "USR_BANEADO", "Usuario baneado", "Usuario bloqueado por incumplimiento."),
            listOf(104, "USR_VERIF_PEND", "Verificacion pendiente", "Solicitud de verificacion de trabajador en revision."),
            listOf(105, "USR_VERIFICADO", "Usuario verificado", "Usuario validado oficialmente."),

            listOf(201, "SRV_PUBLICADO", "Servicio publicado", "Servicio visible para clientes."),
            listOf(202, "SRV_PAUSADO", "Servicio pausado", "Servicio temporalmente inactivo."),
            listOf(203, "SRV_OCULTO", "Servicio oculto", "Servicio no visible en exploracion."),
            listOf(204, "SRV_RESERVADO", "Servicio reservado", "Servicio con gestion activa de cita."),

            listOf(301, "MSG_ENVIADO", "Mensaje enviado", "Mensaje enviado por emisor."),
            listOf(302, "MSG_ENTREGADO", "Mensaje entregado", "Mensaje recibido en el dispositivo del receptor."),
            listOf(303, "MSG_LEIDO", "Mensaje leido", "Mensaje abierto y leido por el receptor."),
            listOf(304, "CHAT_ABIERTO", "Chat abierto", "Chat habilitado para escritura."),
            listOf(305, "CHAT_CERRADO", "Chat cerrado", "Chat cerrado para escritura, solo lectura."),
            listOf(306, "CHAT_BLOQUEADO", "Chat bloqueado", "Chat temporalmente bloqueado para nuevo contacto."),

            listOf(401, "CITA_PENDIENTE", "Cita pendiente", "El cliente genero la cita."),
            listOf(402, "CITA_HANDSHAKE", "Handshake", "El trabajador acepto la cita y condiciones."),
            listOf(403, "CITA_COMENZANDO", "Comenzando", "Trabajador solicita iniciar trabajo; falta confirmacion cliente."),
            listOf(404, "CITA_EN_PROCESO", "En proceso", "Cliente confirma inicio de trabajo."),
            listOf(405, "CITA_FINALIZANDO", "Finalizando", "Trabajador solicita finalizar; falta confirmacion cliente."),
            listOf(406, "CITA_FINALIZADO", "Finalizado", "Cliente confirma finalizacion del trabajo."),
            listOf(407, "CITA_CANCELADO", "Cancelado", "Cita cancelada por rechazo o cierre anticipado."),
            listOf(408, "CITA_CERRADO", "Cerrado", "Cita/Chat cerrados para escritura."),
            listOf(409, "CITA_RECHAZADA", "Rechazada", "Trabajador rechaza propuesta y la cita queda negociable para reenvio.")
        ).forEach { estado ->
            val idEstado = estado[0] as Int
            val codigo = estado[1] as String
            val nombre = estado[2] as String
            val descripcion = estado[3] as String
            db.insert("estados", null, ContentValues().apply {
                put("id_estado", idEstado)
                put("codigo", codigo)
                put("nombre", nombre)
                put("descripcion", descripcion)
            })
        }

        val categoriasBase = listOf(
            "Mecanica a domicilio",
            "Arquitectura y planos",
            "Tecnico general",
            "Electricidad",
            "Gasfiteria",
            "Carpinteria",
            "Pintura",
            "Soldadura",
            "Jardineria",
            "Computacion",
            "Redes e internet",
            "Aseo y mantencion"
        )
        categoriasBase.forEach { nombre ->
            db.insert("categorias_servicio", null, ContentValues().apply { put("nombre", nombre) })
        }

        db.insert("coordenadas", null, ContentValues().apply {
            put("latitud", -33.4489)
            put("longitud", -70.6693)
            put("detalle", "Region Metropolitana, cobertura centro y comunas cercanas")
        })

        db.insert("direcciones", null, ContentValues().apply {
            put("calle", "Av. Libertador Bernardo O'Higgins")
            put("numero", "1449")
            put("villa", "Santiago Centro")
            put("id_coordenadas", 1)
        })

        db.insert("fotos", null, ContentValues().apply {
            put("fecha_subida", ahora())
            put("enlace", "local://perfil/jose-perez")
            put("detalle", "Foto demo del prestador")
            put("nombre_archivo", "jose-perez-demo.jpg")
            put("mime_type", "image/jpeg")
            put("estado_sincronizacion", "sincronizada")
            put("url_remota", "https://backend.contrabajo.dev/fotos/jose-perez-demo.jpg")
        })

        val moderadorId = db.insert("usuarios", null, ContentValues().apply {
            put("run", "11111111")
            put("dv", "1")
            put("username", "moderador_ct")
            put("nombre", "Moderador")
            put("apellido_paterno", "Contrabajo")
            put("apellido_materno", "Sistema")
            put("telefono", "+56900000000")
            put("correo", "moderador@contrabajo.cl")
            put("contrasena_hash", "123456")
            put("fecha_registro", ahora())
            put("fecha_nacimiento", "1990-01-01")
            put("verificado", 1)
            put("tipo_perfil", TipoPerfil.MODERADOR)
            put("numero_documento_identidad", "MOD-CT-0001")
            put("pregunta_recuperacion", "Color favorito")
            put("respuesta_recuperacion", "turquesa")
            put("verificacion_trabajador_pendiente", 0)
            putNull("fecha_solicitud_verificacion_ms")
        })
        db.insert("ubicaciones_usuario", null, ContentValues().apply {
            put("id_usuario", moderadorId)
            put("region", "Region Metropolitana")
            put("comuna", "Santiago")
            put("calle", "Alameda")
            put("numero", "100")
            put("detalle", "Centro")
            put("latitud", -33.4468)
            put("longitud", -70.6693)
            put("rango_disponibilidad_m", 12_000)
            put("rango_busqueda_m", 20_000)
            put("fecha_actualizacion", ahora())
        })

        val trabajadorId = db.insert("usuarios", null, ContentValues().apply {
            put("run", "12345678")
            put("dv", "9")
            put("username", "JosePer")
            put("nombre", "Jose")
            put("apellido_paterno", "Perez")
            put("apellido_materno", "Soto")
            put("telefono", "+56911112222")
            put("correo", "jose@contrabajo.cl")
            put("contrasena_hash", "123456")
            put("fecha_registro", ahora())
            put("fecha_nacimiento", "1994-06-14")
            put("verificado", 1)
            put("tipo_perfil", TipoPerfil.TRABAJADOR)
            put("numero_documento_identidad", "44556677")
            put("pregunta_recuperacion", "Mascota")
            put("respuesta_recuperacion", "firulais")
            put("verificacion_trabajador_pendiente", 0)
            putNull("fecha_solicitud_verificacion_ms")
        })
        db.insert("ubicaciones_usuario", null, ContentValues().apply {
            put("id_usuario", trabajadorId)
            put("region", "Region Metropolitana")
            put("comuna", "Providencia")
            put("calle", "Av. Providencia")
            put("numero", "1200")
            put("detalle", "Cobertura urbana")
            put("latitud", -33.4302)
            put("longitud", -70.6188)
            put("rango_disponibilidad_m", 26_000)
            put("rango_busqueda_m", 20_000)
            put("fecha_actualizacion", ahora())
        })

        val clienteId = db.insert("usuarios", null, ContentValues().apply {
            put("run", "18765432")
            put("dv", "1")
            put("username", "ClienteDemo")
            put("nombre", "Valentina")
            put("apellido_paterno", "Rojas")
            put("apellido_materno", "Diaz")
            put("telefono", "+56933334444")
            put("correo", "vale@contrabajo.cl")
            put("contrasena_hash", "123456")
            put("fecha_registro", ahora())
            put("fecha_nacimiento", "1998-03-20")
            put("verificado", 0)
            put("tipo_perfil", TipoPerfil.USUARIO_BASE)
            putNull("numero_documento_identidad")
            put("pregunta_recuperacion", "Comida favorita")
            put("respuesta_recuperacion", "lasagna")
            put("verificacion_trabajador_pendiente", 0)
            putNull("fecha_solicitud_verificacion_ms")
        })
        db.insert("ubicaciones_usuario", null, ContentValues().apply {
            put("id_usuario", clienteId)
            put("region", "Region Metropolitana")
            put("comuna", "Santiago")
            put("calle", "Sin calle")
            put("numero", "Sin numero")
            put("detalle", "Sin detalle")
            putNull("latitud")
            putNull("longitud")
            put("rango_disponibilidad_m", 20_000)
            put("rango_busqueda_m", 20_000)
            put("fecha_actualizacion", ahora())
        })

        val ofertaPrincipalId = db.insert("ofertas_servicio", null, ContentValues().apply {
            put("titulo", "Mecanico a domicilio")
            put("descripcion", "Diagnostico y mantencion ligera en terreno")
            put("detalle", "Ofrezco servicio de mecanica automotriz a domicilio en la region metropolitana, con visita rapida, diagnostico inicial y presupuesto transparente.")
            put("precio_texto", PrecioUtils.construirPrecioTexto(TipoPrecio.DESDE, 25_000))
            put("tipo_precio", TipoPrecio.DESDE)
            put("monto_base", 25_000)
            put("disponible", 1)
            put("eliminada", 0)
            putNull("fecha_eliminacion")
            put("fecha_publicacion", ahora())
            put("id_categoria_servicio", 1)
            put("id_trabajador", trabajadorId)
            put("id_foto_portada", 1)
        })

        val trabajadoresDemo = listOf(
            listOf("Pedro", "Fuentes", "Lara", "PedroFx", "22334455", "7", "+56921110001", "pedro.fx@contrabajo.cl"),
            listOf("Marcela", "Vera", "Mora", "MarceVm", "23334455", "6", "+56921110002", "marcela.vm@contrabajo.cl"),
            listOf("Ramon", "Silva", "Pinto", "RamonSp", "24334455", "5", "+56921110003", "ramon.sp@contrabajo.cl"),
            listOf("Claudio", "Nunez", "Saez", "ClaudioNs", "25334455", "4", "+56921110004", "claudio.ns@contrabajo.cl"),
            listOf("Daniela", "Araya", "Rios", "DaniAr", "26334455", "3", "+56921110005", "daniela.ar@contrabajo.cl"),
            listOf("Ignacio", "Mella", "Toro", "IgnaMt", "27334455", "2", "+56921110006", "ignacio.mt@contrabajo.cl"),
            listOf("Paula", "Soto", "Guzman", "PaulaSg", "28334455", "1", "+56921110007", "paula.sg@contrabajo.cl"),
            listOf("Victor", "Cid", "Tapia", "VictorCt", "29334455", "0", "+56921110008", "victor.ct@contrabajo.cl"),
            listOf("Camila", "Lopez", "Reyes", "CamiLr", "30334455", "9", "+56921110009", "camila.lr@contrabajo.cl")
        )
        val publicacionesDemo = listOf(
            listOf(
                "Gasfiter urgente",
                "Reparo fugas, llaves y WC en menos de 24 horas. Incluye revision completa de filtraciones visibles, ajuste de sellos, cambio de flexibles y pruebas de presion para dejar el sistema estable. Si detecto piezas criticas, te explico opciones y costo antes de ejecutar.",
                "Desde 18.000 por visita"
            ),
            listOf(
                "Arquitecta para planos",
                "Planos municipales y regularizacion de ampliaciones. Trabajo levantamiento en terreno, propuestas de distribucion y set final para ingreso en municipalidad. Tambien apoyo con observaciones y correcciones posteriores para agilizar aprobaciones.",
                "Desde 120.000 por proyecto"
            ),
            listOf(
                "Tecnico en computadores",
                "Formateo, limpieza interna y optimizacion de equipos. Realizo respaldo previo, reinstalacion segura, actualizaciones, control de temperatura y ajuste de inicio para mejorar rendimiento real en uso diario de trabajo, estudio o gaming.",
                "Desde 22.000 por equipo"
            ),
            listOf(
                "Electricista domiciliario",
                "Cambio de enchufes, tableros y luminarias. Incluye diagnostico inicial, mediciones de seguridad, reemplazo de componentes y pruebas finales de funcionamiento para evitar sobrecargas o cortes inesperados.",
                "Desde 20.000 segun trabajo"
            ),
            listOf("Maestra pintora", "Pintura interior y exterior con terminacion fina.", "Desde 55.000 por jornada"),
            listOf("Soldador a domicilio", "Rejas, portones y refuerzos metalicos.", "Desde 35.000 por trabajo"),
            listOf("Carpintera muebles", "Fabricacion y reparacion de muebles a medida.", "Desde 48.000 segun mueble"),
            listOf("Tecnico en redes wifi", "Mejoro cobertura y estabilidad en hogar u oficina.", "Desde 25.000 por instalacion"),
            listOf("Jardinero por mantencion", "Poda, limpieza y mantencion semanal de jardines.", "Desde 16.000 por visita")
        )
        val fotosDemoRemotas = listOf(
            "https://images.unsplash.com/photo-1621905252507-b35492cc74b4?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1523413651479-597eb2da0ad6?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1555963966-b7ae5404b6ed?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1562259949-e8e7689d7828?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1581093458791-9d09f5c0b651?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1489515217757-5fd1be406fef?auto=format&fit=crop&w=1000&q=80",
            "https://images.unsplash.com/photo-1501004318641-b39e6451bec6?auto=format&fit=crop&w=1000&q=80"
        )
        val random = Random(20260418)
        trabajadoresDemo.forEachIndexed { index, trabajador ->
            val trabajadorDemoId = db.insert("usuarios", null, ContentValues().apply {
                put("run", trabajador[4])
                put("dv", trabajador[5])
                put("username", trabajador[3])
                put("nombre", trabajador[0])
                put("apellido_paterno", trabajador[1])
                put("apellido_materno", trabajador[2])
                put("telefono", trabajador[6])
                put("correo", trabajador[7])
                put("contrasena_hash", "123456")
                put("fecha_registro", ahora(index.toLong()))
                put("fecha_nacimiento", "1992-01-15")
                put("verificado", 1)
                put("tipo_perfil", if (index == 0) TipoPerfil.PREMIUM else TipoPerfil.TRABAJADOR)
                put("numero_documento_identidad", "DOC-${index + 1000}")
                put("pregunta_recuperacion", "Ciudad natal")
                put("respuesta_recuperacion", "santiago")
                put("verificacion_trabajador_pendiente", 0)
                putNull("fecha_solicitud_verificacion_ms")
            })
            db.insert("ubicaciones_usuario", null, ContentValues().apply {
                put("id_usuario", trabajadorDemoId)
                put("region", "Region Metropolitana")
                put("comuna", comunasRegionMetropolitana[index % comunasRegionMetropolitana.size])
                put("calle", "Sin calle")
                put("numero", "Sin numero")
                put("detalle", "Cobertura local")
                put("latitud", -33.65 + random.nextDouble() * 0.35)
                put("longitud", -70.90 + random.nextDouble() * 0.45)
                put("rango_disponibilidad_m", (10 + (index * 3 % 40)) * 1_000)
                put("rango_busqueda_m", 20_000)
                put("fecha_actualizacion", ahora(index.toLong()))
            })

            val publicacion = publicacionesDemo[index]
            val fotoDemoId = db.insert("fotos", null, ContentValues().apply {
                put("fecha_subida", ahora(index.toLong()))
                put("enlace", fotosDemoRemotas[index % fotosDemoRemotas.size])
                put("detalle", "Foto remota demo para tarjetas de exploracion")
                put("nombre_archivo", "demo_${index + 1}.jpg")
                put("mime_type", "image/jpeg")
                put("estado_sincronizacion", "sincronizada")
                putNull("url_remota")
            })
            val idOfertaDemo = db.insert("ofertas_servicio", null, ContentValues().apply {
                put("titulo", publicacion[0])
                put("descripcion", publicacion[1])
                put("detalle", publicacion[1])
                val montoDemo = when (index % 4) {
                    0 -> 18_000
                    1 -> 120_000
                    2 -> 22_000
                    else -> 0
                }
                val tipoPrecioDemo = when (index % 4) {
                    0 -> TipoPrecio.FIJO
                    1 -> TipoPrecio.DESDE
                    2 -> TipoPrecio.POR_HORA
                    else -> TipoPrecio.CONTACTAR
                }
                put("precio_texto", PrecioUtils.construirPrecioTexto(tipoPrecioDemo, montoDemo))
                put("tipo_precio", tipoPrecioDemo)
                put("monto_base", montoDemo)
                put("disponible", 1)
                put("eliminada", 0)
                putNull("fecha_eliminacion")
                put("fecha_publicacion", ahora(index.toLong()))
                put("id_categoria_servicio", (index % categoriasBase.size) + 1)
                put("id_trabajador", trabajadorDemoId)
                put("id_foto_portada", fotoDemoId)
            })
            db.insert("valoraciones", null, ContentValues().apply {
                put("voto", random.nextInt(2, 6))
                put("fecha_voto", ahora(index.toLong()))
                put("comentario", "Servicio demo valorado para pruebas UI.")
                put("id_trabajador", trabajadorDemoId)
                put("id_cliente", clienteId)
                put("id_chat_cita", -(index + 1).toLong())
                put("id_oferta_servicio", idOfertaDemo)
            })
        }

        db.insert("valoraciones", null, ContentValues().apply {
            put("voto", 4)
            put("fecha_voto", ahora())
            put("comentario", "Trabajo rapido y muy ordenado")
            put("id_trabajador", trabajadorId)
            put("id_cliente", clienteId)
            put("id_chat_cita", -1001L)
            put("id_oferta_servicio", ofertaPrincipalId)
        })

        val chatId = db.insert("chats_cita", null, ContentValues().apply {
            put("fecha_creacion", ahora())
            put("id_trabajador", trabajadorId)
            put("id_cliente", clienteId)
        })

        listOf(
            Triple(clienteId, trabajadorId, "Hola Jose, vi tu publicacion y necesito ayuda con el auto."),
            Triple(trabajadorId, clienteId, "Hola Vale, claro. Cuentame el modelo y qué sintoma presenta."),
            Triple(clienteId, trabajadorId, "Es un Suzuki Swift 2016, no parte en las mananas.")
        ).forEachIndexed { index, (idEmisor, idReceptor, contenido) ->
            db.insert("mensajes_chat", null, ContentValues().apply {
                put("fecha_envio", ahora(index.toLong()))
                put("fecha_recibido", ahora(index.toLong()))
                put("fecha_leido", ahora(index.toLong()))
                put("id_emisor", idEmisor)
                put("id_receptor", idReceptor)
                put("id_chat_cita", chatId)
                put("id_estado", EstadoCodigo.MSG_LEIDO)
                put("contenido", contenido)
            })
        }

        db.insert("configuraciones_app", null, ContentValues().apply {
            put("tema", "contrabajo")
            put("notificaciones_activas", 1)
            put("primera_ejecucion", 1)
            put("ultima_pantalla", "inicio")
            put("fecha_actualizacion", ahora())
        })
    }

    private fun aplicarDescripcionAmpliaDemo(db: SQLiteDatabase) {
        val descripcionesAmplias = mapOf(
            "Gasfiter urgente" to "Reparo fugas, llaves y WC en menos de 24 horas. Incluye revision completa de filtraciones visibles, ajuste de sellos, cambio de flexibles y pruebas de presion para dejar el sistema estable. Si detecto piezas criticas, te explico opciones y costo antes de ejecutar.",
            "Arquitecta para planos" to "Planos municipales y regularizacion de ampliaciones. Trabajo levantamiento en terreno, propuestas de distribucion y set final para ingreso en municipalidad. Tambien apoyo con observaciones y correcciones posteriores para agilizar aprobaciones.",
            "Tecnico en computadores" to "Formateo, limpieza interna y optimizacion de equipos. Realizo respaldo previo, reinstalacion segura, actualizaciones, control de temperatura y ajuste de inicio para mejorar rendimiento real en uso diario de trabajo, estudio o gaming.",
            "Electricista domiciliario" to "Cambio de enchufes, tableros y luminarias. Incluye diagnostico inicial, mediciones de seguridad, reemplazo de componentes y pruebas finales de funcionamiento para evitar sobrecargas o cortes inesperados."
        )

        descripcionesAmplias.forEach { (titulo, descripcionLarga) ->
            db.update(
                "ofertas_servicio",
                ContentValues().apply {
                    put("descripcion", descripcionLarga)
                    put("detalle", descripcionLarga)
                },
                "titulo = ? AND LENGTH(descripcion) < 150",
                arrayOf(titulo)
            )
        }
    }

    private fun Cursor.toUsuario(): Usuario = Usuario(
        idUsuario = getLong(getColumnIndexOrThrow("id_usuario")),
        run = getString(getColumnIndexOrThrow("run")),
        dv = getString(getColumnIndexOrThrow("dv")),
        username = getString(getColumnIndexOrThrow("username")),
        nombre = getString(getColumnIndexOrThrow("nombre")),
        apellidoPaterno = getString(getColumnIndexOrThrow("apellido_paterno")),
        apellidoMaterno = getString(getColumnIndexOrThrow("apellido_materno")),
        telefono = getString(getColumnIndexOrThrow("telefono")),
        correo = getString(getColumnIndexOrThrow("correo")),
        contrasenaHash = getString(getColumnIndexOrThrow("contrasena_hash")),
        fechaRegistro = getString(getColumnIndexOrThrow("fecha_registro")),
        fechaNacimiento = getString(getColumnIndexOrThrow("fecha_nacimiento")),
        verificado = getInt(getColumnIndexOrThrow("verificado")) == 1,
        tipoPerfil = getInt(getColumnIndexOrThrow("tipo_perfil")),
        numeroDocumentoIdentidad = getStringNullable("numero_documento_identidad"),
        preguntaRecuperacion = getStringNullable("pregunta_recuperacion").orEmpty(),
        respuestaRecuperacion = getStringNullable("respuesta_recuperacion").orEmpty(),
        verificacionTrabajadorPendiente = getInt(getColumnIndexOrThrow("verificacion_trabajador_pendiente")) == 1,
        fechaSolicitudVerificacionMs = getLongNullable("fecha_solicitud_verificacion_ms"),
        fotoPerfilUrl = getStringNullable("foto_perfil")
    )

    private fun Cursor.toOfertaServicio(): OfertaServicio = OfertaServicio(
        idOfertaServicio = getLong(getColumnIndexOrThrow("id_oferta_servicio")),
        titulo = getString(getColumnIndexOrThrow("titulo")),
        descripcion = getString(getColumnIndexOrThrow("descripcion")),
        precioTexto = getString(getColumnIndexOrThrow("precio_texto")),
        tipoPrecio = getIntNullable("tipo_precio") ?: TipoPrecio.FIJO,
        montoBase = getIntNullable("monto_base") ?: 0,
        disponible = getInt(getColumnIndexOrThrow("disponible")) == 1,
        fechaPublicacion = getString(getColumnIndexOrThrow("fecha_publicacion")),
        idCategoriaServicio = getLong(getColumnIndexOrThrow("id_categoria_servicio")),
        idTrabajador = getLong(getColumnIndexOrThrow("id_trabajador")),
        idCliente = getLongNullable("id_cliente"),
        idFotoPortada = getLongNullable("id_foto_portada"),
        nombreTrabajador = getStringNullable("nombre_trabajador").orEmpty(),
        usernameTrabajador = getStringNullable("username_trabajador").orEmpty(),
        nombreCategoria = getStringNullable("nombre_categoria").orEmpty(),
        puntuacionPromedio = getDoubleNullable("puntuacion_promedio") ?: 0.0,
        trabajadorVerificado = getIntNullable("trabajador_verificado") == 1,
        ubicacionReferencia = getStringNullable("ubicacion_referencia").orEmpty(),
        rangoDisponibilidadM = getIntNullable("rango_disponibilidad_m") ?: 20_000,
        latitudReferencia = getDoubleNullable("latitud_referencia"),
        longitudReferencia = getDoubleNullable("longitud_referencia"),
        fotoUrlReferencia = getStringNullable("foto_url_referencia").orEmpty(),
        fotoNombreArchivo = getStringNullable("foto_nombre_archivo").orEmpty(),
        fotoMimeType = getStringNullable("foto_mime_type").orEmpty(),
        fotoPendienteSincronizacion = getStringNullable("foto_estado_sincronizacion") == "pendiente",
        fotoPerfilTrabajador = getStringNullable("foto_perfil_trabajador").orEmpty(),
        eliminada = getIntNullable("eliminada") == 1,
        fechaEliminacion = getStringNullable("fecha_eliminacion")
    )

    private fun Cursor.toChatCita(): ChatCita = ChatCita(
        idChatCita = getLong(getColumnIndexOrThrow("id_chat_cita")),
        fechaCreacion = getString(getColumnIndexOrThrow("fecha_creacion")),
        idTrabajador = getLong(getColumnIndexOrThrow("id_trabajador")),
        idCliente = getLong(getColumnIndexOrThrow("id_cliente")),
        idOfertaServicio = getLongNullable("id_oferta_servicio"),
        idCita = getLongNullable("id_cita"),
        nombreContacto = getStringNullable("nombre_contacto").orEmpty(),
        usernameContacto = getStringNullable("username_contacto").orEmpty(),
        tituloServicio = getStringNullable("titulo_servicio").orEmpty(),
        categoriaServicio = getStringNullable("categoria_servicio").orEmpty(),
        ultimoMensaje = getStringNullable("ultimo_mensaje").orEmpty(),
        horaUltimoMensaje = getStringNullable("hora_ultimo_mensaje").orEmpty(),
        mensajesNoLeidos = getIntNullable("mensajes_no_leidos") ?: 0,
        estadoCita = getIntNullable("estado_cita"),
        chatCerrado = getIntNullable("cerrado") == 1,
        bloqueadoHastaMs = getLongNullable("bloqueado_hasta_ms")
    )

    private fun Cursor.toMensajeChat(): MensajeChat = MensajeChat(
        idMensajeChat = getLong(getColumnIndexOrThrow("id_mensaje_chat")),
        fechaEnvio = getString(getColumnIndexOrThrow("fecha_envio")),
        fechaRecibido = getStringNullable("fecha_recibido"),
        fechaLeido = getStringNullable("fecha_leido"),
        idEmisor = getLong(getColumnIndexOrThrow("id_emisor")),
        idReceptor = getLong(getColumnIndexOrThrow("id_receptor")),
        idChatCita = getLong(getColumnIndexOrThrow("id_chat_cita")),
        idEstado = getLong(getColumnIndexOrThrow("id_estado")),
        contenido = getString(getColumnIndexOrThrow("contenido"))
    )

    private fun Cursor.toNotificacionMensajePendiente(): NotificacionMensajePendiente = NotificacionMensajePendiente(
        idMensajeChat = getLong(getColumnIndexOrThrow("id_mensaje_chat")),
        idChatCita = getLong(getColumnIndexOrThrow("id_chat_cita")),
        titulo = getString(getColumnIndexOrThrow("titulo_notificacion")),
        contenido = getString(getColumnIndexOrThrow("contenido"))
    )

    private fun Cursor.toCitaServicio(): CitaServicio = CitaServicio(
        idCita = getLong(getColumnIndexOrThrow("id_cita")),
        idChatCita = getLong(getColumnIndexOrThrow("id_chat_cita")),
        fechaCreacion = getString(getColumnIndexOrThrow("fecha_creacion")),
        fechaProgramada = getString(getColumnIndexOrThrow("fecha_programada")),
        comentario = getString(getColumnIndexOrThrow("comentario")),
        precioAcordado = getInt(getColumnIndexOrThrow("precio_acordado")),
        fechaInicioTrabajo = getStringNullable("fecha_inicio_trabajo"),
        fechaFinTrabajo = getStringNullable("fecha_fin_trabajo"),
        estado = getInt(getColumnIndexOrThrow("estado_cita"))
    )

    private fun Cursor.toValoracion(): Valoracion = Valoracion(
        idValoracion = getLong(getColumnIndexOrThrow("id_valoracion")),
        voto = getInt(getColumnIndexOrThrow("voto")),
        fechaVoto = getString(getColumnIndexOrThrow("fecha_voto")),
        comentario = getString(getColumnIndexOrThrow("comentario")),
        idTrabajador = getLong(getColumnIndexOrThrow("id_trabajador")),
        idCliente = getLong(getColumnIndexOrThrow("id_cliente")),
        idChatCita = getLong(getColumnIndexOrThrow("id_chat_cita")),
        idOfertaServicio = getLong(getColumnIndexOrThrow("id_oferta_servicio")),
        usernameCliente = getStringNullable("username_cliente").orEmpty(),
        fechaFinalizacionCita = getStringNullable("fecha_finalizacion_cita")
    )

    private fun Cursor.getStringNullable(column: String): String? {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    private fun Cursor.getDoubleNullable(column: String): Double? {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) getDouble(index) else null
    }

    private fun Cursor.getLongNullable(column: String): Long? {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) getLong(index) else null
    }

    private fun Cursor.getIntNullable(column: String): Int? {
        val index = getColumnIndex(column)
        return if (index >= 0 && !isNull(index)) getInt(index) else null
    }

    companion object {
        private const val DATABASE_NAME = "contrabajo_local.db"
        private const val DATABASE_VERSION = 18
        private const val SESION_EXPIRACION_MS = 30 * 60 * 1000L
        private const val VERIFICACION_TRABAJADOR_MS = 3 * 60 * 1000L
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        private val comunasRegionMetropolitana = listOf(
            "Alhue", "Buin", "Calera de Tango", "Cerrillos", "Cerro Navia", "Colina",
            "Conchali", "Curacavi", "El Bosque", "El Monte", "Estacion Central", "Huechuraba",
            "Independencia", "Isla de Maipo", "La Cisterna", "La Florida", "La Granja", "La Pintana",
            "La Reina", "Lampa", "Las Condes", "Lo Barnechea", "Lo Espejo", "Lo Prado",
            "Macul", "Maipu", "Maria Pinto", "Melipilla", "Nunoa", "Padre Hurtado",
            "Paine", "Pedro Aguirre Cerda", "Penaflor", "Penalolen", "Pirque", "Providencia",
            "Pudahuel", "Puente Alto", "Quilicura", "Quinta Normal", "Recoleta", "Renca",
            "San Bernardo", "San Joaquin", "San Jose de Maipo", "San Miguel", "San Pedro",
            "San Ramon", "Santiago", "Talagante", "Tiltil", "Vitacura"
        )
        private val consultaOfertaSelect =
            "SELECT o.*, u.nombre || ' ' || u.apellido_paterno AS nombre_trabajador, " +
                "u.username AS username_trabajador, " +
                "cat.nombre AS nombre_categoria, " +
                "(COALESCE(uu.comuna, 'Santiago') || ', ' || COALESCE(uu.region, 'Region Metropolitana')) AS ubicacion_referencia, " +
                "u.verificado AS trabajador_verificado, " +
                "uu.latitud AS latitud_referencia, uu.longitud AS longitud_referencia, " +
                "COALESCE(uu.rango_disponibilidad_m, 20000) AS rango_disponibilidad_m, " +
                "COALESCE(f.enlace, '') AS foto_url_referencia, " +
                "COALESCE(f.nombre_archivo, '') AS foto_nombre_archivo, " +
                "COALESCE(f.mime_type, '') AS foto_mime_type, " +
                "COALESCE(f.estado_sincronizacion, '') AS foto_estado_sincronizacion, " +
                "COALESCE(u.foto_perfil, '') AS foto_perfil_trabajador, " +
                "o.eliminada AS eliminada, " +
                "o.fecha_eliminacion AS fecha_eliminacion, " +
                "COALESCE(AVG(v.voto), 0) AS puntuacion_promedio "
        private val consultaOfertaJoins =
                "FROM ofertas_servicio o " +
                "INNER JOIN usuarios u ON u.id_usuario = o.id_trabajador " +
                "INNER JOIN categorias_servicio cat ON cat.id_categoria_servicio = o.id_categoria_servicio " +
                "LEFT JOIN ubicaciones_usuario uu ON uu.id_usuario = u.id_usuario " +
                "LEFT JOIN fotos f ON f.id_foto = o.id_foto_portada " +
                "LEFT JOIN valoraciones v ON v.id_oferta_servicio = o.id_oferta_servicio "
        private val consultaOfertaGroupBy = " GROUP BY o.id_oferta_servicio"

        private fun ahora(minutosRestar: Long = 0): String =
            LocalDateTime.now().minusMinutes(minutosRestar).format(formatter)

        private fun Boolean.toInt(): Int = if (this) 1 else 0
    }
}
