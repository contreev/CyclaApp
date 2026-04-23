package com.example.cyclapp.data

import android.net.Uri
import com.example.cyclapp.model.BadgeItem
import com.example.cyclapp.model.MissionItem
import com.example.cyclapp.model.UserProfile
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage

fun crearDatosInicialesUsuario(uid: String) {
    val db = Firebase.firestore

    val misiones = listOf(
        MissionItem(
            id = "m1",
            titulo = "Recicla 3 botellas",
            descripcion = "Deposita 3 botellas plásticas correctamente.",
            progreso = 3,
            meta = 3,
            recompensa = 30,
            completada = true
        ),
        MissionItem(
            id = "m2",
            titulo = "Primer registro",
            descripcion = "Realiza tu primer registro de reciclaje.",
            progreso = 1,
            meta = 1,
            recompensa = 20,
            completada = true
        ),
        MissionItem(
            id = "m3",
            titulo = "Visita un punto verde",
            descripcion = "Consulta un punto de reciclaje cercano.",
            progreso = 1,
            meta = 1,
            recompensa = 15,
            completada = true
        ),
        MissionItem(
            id = "m4",
            titulo = "Eco-Héroe: 50 Botellas",
            descripcion = "Un gran reto para un gran reciclador.",
            progreso = 12,
            meta = 50,
            recompensa = 200,
            completada = false
        ),
        MissionItem(
            id = "m5",
            titulo = "Maestro Orgánico",
            descripcion = "Separa tus residuos de comida por una semana.",
            progreso = 2,
            meta = 7,
            recompensa = 100,
            completada = false
        ),
        MissionItem(
            id = "m6",
            titulo = "Limpia tu barrio",
            descripcion = "Participa en una jornada de limpieza.",
            progreso = 0,
            meta = 1,
            recompensa = 50,
            completada = false
        ),
        MissionItem(
            id = "m7",
            titulo = "Reciclador de Papel",
            descripcion = "Recicla 10kg de papel o cartón.",
            progreso = 0,
            meta = 10,
            recompensa = 80,
            completada = false
        ),
        MissionItem(
            id = "m8",
            titulo = "Sin Plástico",
            descripcion = "No uses plásticos de un solo uso por un día.",
            progreso = 0,
            meta = 1,
            recompensa = 40,
            completada = false
        ),
        MissionItem(
            id = "m9",
            titulo = "Compostaje Master",
            descripcion = "Inicia tu propio sistema de compostaje.",
            progreso = 0,
            meta = 1,
            recompensa = 150,
            completada = false
        ),
        MissionItem(
            id = "m10",
            titulo = "Embajador Eco",
            descripcion = "Invita a 3 amigos a unirse a la app.",
            progreso = 1,
            meta = 3,
            recompensa = 120,
            completada = false
        )
    )

    val logros = listOf(
        BadgeItem("l1", "Primer paso", "Crea tu cuenta en CyclApp.", true),
        BadgeItem("l2", "Explorador", "Visitaste tu primer punto verde.", true),
        BadgeItem("l3", "Reciclador Novato", "Reciclaste tus primeras botellas.", true)
    )

    val userRef = db.collection("usuarios").document(uid)

    userRef.get().addOnSuccessListener { document ->
        if (!document.exists()) {
            // Si el usuario no existe, creamos el perfil completo
            val perfilInicial = UserProfile(
                nombre = "Usuario CyclApp",
                correo = "",
                puntos = 65,
                nivel = "Intermedio",
                fotoUrl = ""
            )
            userRef.set(perfilInicial)
        }
        
        // Siempre añadimos/actualizamos las misiones y logros 
        // sin tocar los campos principales (nombre, puntos, etc.) si ya existen
        misiones.forEach { mission ->
            userRef.collection("misiones").document(mission.id).set(mission, SetOptions.merge())
        }

        logros.forEach { logro ->
            userRef.collection("logros").document(logro.id).set(logro, SetOptions.merge())
        }
    }
}

fun calcularNivel(puntos: Int): String {
    return when {
        puntos >= 500 -> "Eco-Leyenda"
        puntos >= 300 -> "Experto"
        puntos >= 150 -> "Avanzado"
        puntos >= 50 -> "Intermedio"
        else -> "Inicial"
    }
}

fun completarPasoMision(uid: String, mission: MissionItem) {
    val db = Firebase.firestore
    val missionRef = db.collection("usuarios").document(uid).collection("misiones").document(mission.id)
    val userRef = db.collection("usuarios").document(uid)

    val nuevoProgreso = (mission.progreso + 1).coerceAtMost(mission.meta)
    val estabaCompleta = mission.completada
    val ahoraCompleta = nuevoProgreso >= mission.meta

    missionRef.update(mapOf("progreso" to nuevoProgreso, "completada" to ahoraCompleta))
        .addOnSuccessListener {
            if (!estabaCompleta && ahoraCompleta) {
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val puntosActuales = snapshot.getLong("puntos") ?: 0
                    val nuevosPuntos = puntosActuales + mission.recompensa
                    val nuevoNivel = calcularNivel(nuevosPuntos.toInt())
                    
                    transaction.update(userRef, "puntos", nuevosPuntos)
                    transaction.update(userRef, "nivel", nuevoNivel)
                }.addOnSuccessListener {
                    // Aquí podrías desbloquear un logro si quisieras
                }
            }
        }
}

fun subirFotoPerfil(uid: String, imageUri: Uri, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
    val timestamp = System.currentTimeMillis()
    val storageRef = Firebase.storage.reference.child("profile_photos/${uid}_$timestamp.jpg")
    val userRef = Firebase.firestore.collection("usuarios").document(uid)
    storageRef.putFile(imageUri).continueWithTask { task ->
        if (!task.isSuccessful) task.exception?.let { throw it }
        storageRef.downloadUrl
    }.addOnSuccessListener { downloadUri ->
        val url = downloadUri.toString()
        userRef.set(mapOf("fotoUrl" to url), SetOptions.merge()).addOnSuccessListener { onSuccess() }
    }
}

fun eliminarFotoPerfil(uid: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
    val storageRef = Firebase.storage.reference.child("profile_photos/$uid.jpg")
    val userRef = Firebase.firestore.collection("usuarios").document(uid)
    storageRef.delete().addOnCompleteListener { task ->
        if (task.isSuccessful) userRef.set(mapOf("fotoUrl" to ""), SetOptions.merge()).addOnSuccessListener { onSuccess() }
    }
}
