package com.example.cyclapp.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.cyclapp.model.BadgeItem
import com.example.cyclapp.model.MissionItem
import com.example.cyclapp.model.RecyclingPoint
import com.example.cyclapp.model.Review
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Sistema de 8 Logros Innovadores con limpieza profunda.
 */
fun crearDatosInicialesUsuario(uid: String) {
    val db = Firebase.firestore
    val userRef = db.collection("usuarios").document(uid)
    val misionesRef = userRef.collection("misiones")
    val logrosRef = userRef.collection("logros")
    
    val misionesOficiales = listOf(
        MissionItem("m1", "Recicla 3 botellas", "Deposita 3 botellas plásticas.", 3, 30),
        MissionItem("m2", "Primer registro", "Realiza tu primer registro de reciclaje.", 1, 20),
        MissionItem("m3", "Visita un punto verde", "Consulta un punto cercano.", 1, 15),
        MissionItem("m4", "Eco-Explorador", "Visita 3 puntos diferentes.", 3, 100),
        MissionItem("m5", "Crítico Verde", "Deja 5 reseñas.", 5, 150),
        MissionItem("m6", "Rey del Plástico", "Registra 10 botellas.", 10, 200),
        MissionItem("m7", "Cero Papel", "Recicla 5 kg de papel.", 5, 120),
        MissionItem("m8", "Vidrio Brillante", "Lleva 8 botellas de vidrio.", 8, 180),
        MissionItem("m9", "Pilas Fuera", "Recicla 2 pilas.", 2, 250),
        MissionItem("m10", "Reciclador Constante", "Registro por 3 días.", 3, 400)
    )

    val logrosOficiales = listOf(
        BadgeItem("e1", "🌱 Semilla", "1 misión completada.", false),
        BadgeItem("e2", "🌿 Brote", "4 misiones completadas.", false),
        BadgeItem("e3", "🌳 Árbol", "7 misiones completadas.", false),
        BadgeItem("e4", "✨ Espíritu de la Naturaleza", "10 misiones completadas.", false),
        BadgeItem("s1", "🧪 Alquimista del Plástico", "Completaste m1 y m6.", false),
        BadgeItem("s2", "🎒 Eco-Viajero", "Completaste m4.", false),
        BadgeItem("s3", "📢 Voz Ecológica", "Completaste m5.", false),
        BadgeItem("s4", "💎 Maestro del Vidrio", "Completaste m8.", false)
    )

    logrosRef.get().addOnSuccessListener { snapshot ->
        val batch = db.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        logrosOficiales.forEach { b -> batch.set(logrosRef.document(b.id), b) }
        
        batch.commit().addOnSuccessListener {
            misionesRef.get().addOnSuccessListener { mSnap ->
                val mBatch = db.batch()
                misionesOficiales.forEach { m ->
                    if (mSnap.documents.none { it.id == m.id }) mBatch.set(misionesRef.document(m.id), m)
                }
                mBatch.commit().addOnSuccessListener {
                    sincronizarPuntosYLogros(uid)
                }
            }
        }
    }
}

fun sincronizarPuntosYLogros(uid: String) {
    val db = Firebase.firestore
    val userRef = db.collection("usuarios").document(uid)
    val misionesRef = userRef.collection("misiones")
    val logrosRef = userRef.collection("logros")

    misionesRef.get().addOnSuccessListener { mSnap ->
        val misiones = mSnap.documents.mapNotNull { it.toObject<MissionItem>()?.copy(id = it.id) }
        val completadas = misiones.filter { it.completada }
        val count = completadas.size
        val totalPuntos = completadas.sumOf { it.recompensa }

        userRef.update(mapOf("puntos" to totalPuntos, "nivel" to when {
            totalPuntos > 1000 -> "Maestro"
            totalPuntos > 500 -> "Avanzado"
            else -> "Inicial"
        }))

        val batch = db.batch()
        batch.update(logrosRef.document("e1"), "desbloqueado", count >= 1)
        batch.update(logrosRef.document("e2"), "desbloqueado", count >= 4)
        batch.update(logrosRef.document("e3"), "desbloqueado", count >= 7)
        batch.update(logrosRef.document("e4"), "desbloqueado", count >= 10)
        
        val m1yM6 = completadas.any { it.id == "m1" } && completadas.any { it.id == "m6" }
        batch.update(logrosRef.document("s1"), "desbloqueado", m1yM6)
        batch.update(logrosRef.document("s2"), "desbloqueado", completadas.any { it.id == "m4" })
        batch.update(logrosRef.document("s3"), "desbloqueado", completadas.any { it.id == "m5" })
        batch.update(logrosRef.document("s4"), "desbloqueado", completadas.any { it.id == "m8" })
        
        batch.commit()
    }
}

fun completarPasoMision(uid: String, mission: MissionItem) {
    val db = Firebase.firestore
    val missionRef = db.collection("usuarios").document(uid).collection("misiones").document(mission.id)
    if (mission.completada) return

    val nuevoProgreso = mission.progreso + 1
    val seCompleto = nuevoProgreso >= mission.meta

    db.runTransaction { transaction ->
        if (seCompleto) {
            transaction.update(missionRef, "progreso", mission.meta, "completada", true)
        } else {
            transaction.update(missionRef, "progreso", nuevoProgreso)
        }
        null
    }.addOnSuccessListener { if (seCompleto) sincronizarPuntosYLogros(uid) }
}

fun getPuntosReciclaje(onUpdate: (List<RecyclingPoint>) -> Unit) {
    val db = Firebase.firestore
    db.collection("puntos_reciclaje").addSnapshotListener { snapshot, _ ->
        if (snapshot != null) {
            val puntos = snapshot.documents.mapNotNull { it.toObject<RecyclingPoint>()?.copy(id = it.id) }
            onUpdate(puntos)
        }
    }
}

fun agregarOEditarResena(puntoId: String, uid: String, resena: Review, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val db = Firebase.firestore
    val puntoRef = db.collection("puntos_reciclaje").document(puntoId)
    db.runTransaction { transaction ->
        val snapshot = transaction.get(puntoRef)
        val punto = snapshot.toObject<RecyclingPoint>() ?: return@runTransaction
        val nuevasResenas = punto.reviews.toMutableList()
        val index = nuevasResenas.indexOfFirst { it.id == uid }
        val resenaConId = resena.copy(id = uid)
        if (index != -1) nuevasResenas[index] = resenaConId else nuevasResenas.add(resenaConId)
        val nuevoPromedio = if (nuevasResenas.isNotEmpty()) nuevasResenas.map { it.rating }.average() else 0.0
        transaction.update(puntoRef, "reviews", nuevasResenas, "averageRating", nuevoPromedio)
        null
    }.addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it.message ?: "Error") }
}

fun subirFotoPerfil(context: Context, uid: String, imageUri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val cloudName = "dhcsw9o8y"
    val uploadPreset = "cyclapp_prese"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes() ?: throw Exception("No se pudo leer la imagen")
            
            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "profile.jpg", bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                .addFormDataPart("upload_preset", uploadPreset)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val json = JsonParser.parseString(bodyString).asJsonObject
                    val url = json.get("secure_url").asString
                    
                    Firebase.firestore.collection("usuarios").document(uid)
                        .update("fotoUrl", url)
                        .await()
                    
                    // También guardamos en la colección que pediste
                    Firebase.firestore.collection("fotos_perfil").document(uid).set(mapOf(
                        "uid" to uid,
                        "url" to url,
                        "fecha" to FieldValue.serverTimestamp()
                    )).await()
                    
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    withContext(Dispatchers.Main) { onError("Error Cloudinary: ${response.code} ${response.message}") }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onError(e.message ?: "Error desconocido") }
        }
    }
}

fun eliminarFotoPerfil(uid: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    Firebase.firestore.collection("usuarios").document(uid).set(mapOf("fotoUrl" to ""), SetOptions.merge()).addOnSuccessListener { onSuccess() }
}
