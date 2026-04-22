package com.example.cyclapp.data

import android.net.Uri
import com.example.cyclapp.model.BadgeItem
import com.example.cyclapp.model.MissionItem
import com.example.cyclapp.model.UserProfile
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

fun crearDatosInicialesUsuario(uid: String) {
    val db = Firebase.firestore

    val misiones = listOf(
        MissionItem(
            id = "m1",
            titulo = "Recicla 30 botellas",
            descripcion = "Deposita 30 botellas plásticas correctamente.",
            progreso = 0,
            meta = 30,
            recompensa = 100,
            completada = false
        ),
        MissionItem(
            id = "m2",
            titulo = "Recicla 30 orgánicos",
            descripcion = "Registra 30 residuos orgánicos correctamente.",
            progreso = 0,
            meta = 30,
            recompensa = 100,
            completada = false
        ),
        MissionItem(
            id = "m3",
            titulo = "Recicla por 30 días",
            descripcion = "Mantente activo reciclando durante 30 días.",
            progreso = 0,
            meta = 30,
            recompensa = 150,
            completada = false
        )
    )

    val logros = listOf(
        BadgeItem(
            id = "l1",
            titulo = "Primer paso",
            descripcion = "Crea tu cuenta en CyclApp.",
            desbloqueado = true
        ),
        BadgeItem(
            id = "l2",
            titulo = "Maestro de botellas",
            descripcion = "Completaste la misión de reciclar 30 botellas.",
            desbloqueado = false
        ),
        BadgeItem(
            id = "l3",
            titulo = "Guardián orgánico",
            descripcion = "Completaste la misión de reciclar 30 orgánicos.",
            desbloqueado = false
        ),
        BadgeItem(
            id = "l4",
            titulo = "Constancia verde",
            descripcion = "Completaste la misión de reciclar durante 30 días.",
            desbloqueado = false
        )
    )

    misiones.forEach { mission ->
        db.collection("usuarios")
            .document(uid)
            .collection("misiones")
            .document(mission.id)
            .set(mission)
    }

    logros.forEach { logro ->
        db.collection("usuarios")
            .document(uid)
            .collection("logros")
            .document(logro.id)
            .set(logro)
    }
}

fun calcularNivel(puntos: Int): String {
    return when {
        puntos >= 300 -> "Experto"
        puntos >= 150 -> "Avanzado"
        puntos >= 50 -> "Intermedio"
        else -> "Inicial"
    }
}

fun desbloquearLogroSegunMision(uid: String, missionId: String) {
    val db = Firebase.firestore

    val logroId = when (missionId) {
        "m1" -> "l2"
        "m2" -> "l3"
        "m3" -> "l4"
        else -> null
    } ?: return

    db.collection("usuarios")
        .document(uid)
        .collection("logros")
        .document(logroId)
        .update("desbloqueado", true)
}

fun completarPasoMision(uid: String, mission: MissionItem) {
    val db = Firebase.firestore
    val missionRef = db.collection("usuarios")
        .document(uid)
        .collection("misiones")
        .document(mission.id)

    val userRef = db.collection("usuarios").document(uid)

    val nuevoProgreso = (mission.progreso + 1).coerceAtMost(mission.meta)
    val estabaCompleta = mission.completada
    val ahoraCompleta = nuevoProgreso >= mission.meta

    missionRef.update(
        mapOf(
            "progreso" to nuevoProgreso,
            "completada" to ahoraCompleta
        )
    ).addOnSuccessListener {
        if (!estabaCompleta && ahoraCompleta) {
            userRef.get().addOnSuccessListener { userDoc ->
                val userProfile = userDoc.toObject<UserProfile>() ?: return@addOnSuccessListener
                val nuevosPuntos = userProfile.puntos + mission.recompensa
                val nuevoNivel = calcularNivel(nuevosPuntos)

                userRef.update(
                    mapOf(
                        "puntos" to nuevosPuntos,
                        "nivel" to nuevoNivel
                    )
                )
            }

            desbloquearLogroSegunMision(uid, mission.id)
        }
    }
}

fun subirFotoPerfil(
    uid: String,
    imageUri: Uri,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val storageRef = Firebase.storage.reference
        .child("profile_photos/$uid.jpg")

    val userRef = Firebase.firestore.collection("usuarios").document(uid)

    storageRef.putFile(imageUri)
        .continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: Exception("Error subiendo foto")
            }
            storageRef.downloadUrl
        }
        .addOnSuccessListener { downloadUri ->
            userRef.set(
                mapOf("fotoUrl" to downloadUri.toString()),
                SetOptions.merge()
            )
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e ->
                    onError(e.localizedMessage ?: "No se pudo guardar la URL de la foto.")
                }
        }
        .addOnFailureListener { e ->
            onError(e.localizedMessage ?: "No se pudo subir la foto.")
        }
}

fun eliminarFotoPerfil(
    uid: String,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val storageRef = Firebase.storage.reference
        .child("profile_photos/$uid.jpg")

    val userRef = Firebase.firestore.collection("usuarios").document(uid)

    storageRef.delete()
        .addOnSuccessListener {
            userRef.set(
                mapOf("fotoUrl" to ""),
                SetOptions.merge()
            )
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e ->
                    onError(e.localizedMessage ?: "No se pudo limpiar la foto del perfil.")
                }
        }
        .addOnFailureListener {
            userRef.set(
                mapOf("fotoUrl" to ""),
                SetOptions.merge()
            )
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e ->
                    onError(e.localizedMessage ?: "No se pudo limpiar la foto del perfil.")
                }
        }
}