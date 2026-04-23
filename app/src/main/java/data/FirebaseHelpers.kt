package com.example.cyclapp.data

import android.net.Uri
import android.util.Log
import com.example.cyclapp.model.BadgeItem
import com.example.cyclapp.model.MissionItem
import com.example.cyclapp.model.RecyclingPoint
import com.example.cyclapp.model.Review
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun crearDatosInicialesUsuario(uid: String) {
    val db = Firebase.firestore
    val batch = db.batch()
    
    val misiones = listOf(
        MissionItem(id = "m1", titulo = "Primer Reciclaje", descripcion = "Lleva tu primer residuo a un punto de acopio.", meta = 1, recompensa = 50),
        MissionItem(id = "m2", titulo = "Experto en Plástico", descripcion = "Recicla 5 botellas de plástico.", meta = 5, recompensa = 100),
        MissionItem(id = "m3", titulo = "Comunidad Activa", descripcion = "Deja 3 reseñas en puntos de reciclaje.", meta = 3, recompensa = 150)
    )

    misiones.forEach { mission ->
        val ref = db.collection("usuarios").document(uid).collection("misiones").document(mission.id)
        batch.set(ref, mission)
    }

    val logros = listOf(
        BadgeItem(id = "b1", titulo = "Reciclador Novato", descripcion = "Has empezado tu camino ecológico."),
        BadgeItem(id = "b2", titulo = "Héroe del Vidrio", descripcion = "Reciclaste 10 botellas de vidrio.")
    )

    logros.forEach { badge ->
        val ref = db.collection("usuarios").document(uid).collection("logros").document(badge.id)
        batch.set(ref, badge)
    }

    batch.commit().addOnFailureListener { e ->
        Log.e("FirebaseHelpers", "Error al crear datos iniciales: ${e.message}")
    }
}

fun completarPasoMision(uid: String, mission: MissionItem) {
    val db = Firebase.firestore
    val missionRef = db.collection("usuarios").document(uid).collection("misiones").document(mission.id)
    val userRef = db.collection("usuarios").document(uid)

    if (mission.completada) return

    val nuevoProgreso = mission.progreso + 1
    val seCompleto = nuevoProgreso >= mission.meta

    db.runTransaction { transaction ->
        if (seCompleto) {
            transaction.update(missionRef, "progreso", mission.meta)
            transaction.update(missionRef, "completada", true)
            transaction.update(userRef, "puntos", FieldValue.increment(mission.recompensa.toLong()))
        } else {
            transaction.update(missionRef, "progreso", nuevoProgreso)
        }
        null
    }.addOnFailureListener { e ->
        Log.e("FirebaseHelpers", "Error al actualizar progreso: ${e.message}")
    }
}

fun getPuntosReciclaje(onPuntosChanged: (List<RecyclingPoint>) -> Unit) {
    val db = Firebase.firestore
    
    db.collection("puntos_reciclaje").addSnapshotListener { snapshot, e ->
        if (e != null) {
            Log.e("FirebaseHelpers", "Error al escuchar puntos: ${e.message}")
            return@addSnapshotListener
        }
        
        val puntos = snapshot?.mapNotNull { it.toObject<RecyclingPoint>().copy(id = it.id) } ?: emptyList()
        onPuntosChanged(puntos)
        
        // Si hay muy pocos puntos, insertamos los masivos automáticamente
        if (puntos.size <= 3) {
            insertarPuntosMasivosBogota()
        }
    }
}

fun insertarPuntosMasivosBogota() {
    val db = Firebase.firestore
    val puntos = listOf(
        RecyclingPoint(name = "Punto Verde Chapinero", address = "Calle 60 #13-40", categories = listOf("Plástico", "Vidrio"), latitude = 4.6437, longitude = -74.0628),
        RecyclingPoint(name = "EcoPunto Unicentro", address = "Av. Carrera 15 #124-30", categories = listOf("Pilas", "Electrónicos"), latitude = 4.7015, longitude = -74.0410),
        RecyclingPoint(name = "Recicladora Central", address = "Carrera 10 #15-20", categories = listOf("Papel", "Cartón"), latitude = 4.6028, longitude = -74.0750),
        RecyclingPoint(name = "Punto Eco Suba", address = "Calle 145 #92-30", categories = listOf("Plástico", "Metal"), latitude = 4.7430, longitude = -74.0850),
        RecyclingPoint(name = "Reciclaje Kennedy", address = "Calle 26 Sur #78-40", categories = listOf("Papel", "Vidrio"), latitude = 4.6310, longitude = -74.1380),
        RecyclingPoint(name = "EcoFontibón", address = "Calle 17 #99-20", categories = listOf("Electrónicos", "Baterías"), latitude = 4.6750, longitude = -74.1450),
        RecyclingPoint(name = "Punto Verde Bosa", address = "Calle 59 Sur #80-10", categories = listOf("Plástico", "Cartón"), latitude = 4.6150, longitude = -74.1850),
        RecyclingPoint(name = "Recicla Engativá", address = "Calle 80 #102-50", categories = listOf("Vidrio", "Metal"), latitude = 4.7120, longitude = -74.1120),
        RecyclingPoint(name = "EcoTeusaquillo", address = "Carrera 24 #45-10", categories = listOf("Papel", "Plástico"), latitude = 4.6380, longitude = -74.0850),
        RecyclingPoint(name = "Punto Limpio Usaquén", address = "Calle 116 #7-15", categories = listOf("Orgánicos", "Plástico"), latitude = 4.6980, longitude = -74.0320),
        RecyclingPoint(name = "Centro Reciclaje Alquería", address = "Carrera 52 #42-10 Sur", categories = listOf("Plástico", "Chatarra"), latitude = 4.5950, longitude = -74.1250),
        RecyclingPoint(name = "EcoPunto Galerías", address = "Calle 53 #21-40", categories = listOf("Papel", "Cartón"), latitude = 4.6410, longitude = -74.0720),
        RecyclingPoint(name = "Reciclaje Salitre", address = "Av. Esperanza #68-20", categories = listOf("Vidrio", "Electrónicos"), latitude = 4.6580, longitude = -74.1080),
        RecyclingPoint(name = "Punto Verde Restrepo", address = "Calle 18 Sur #16-30", categories = listOf("Plástico", "Metal"), latitude = 4.5850, longitude = -74.1020),
        RecyclingPoint(name = "EcoUsaquén Cedritos", address = "Calle 142 #12-40", categories = listOf("Papel", "Vidrio"), latitude = 4.7210, longitude = -74.0480),
        RecyclingPoint(name = "Recicla Suba Pinar", address = "Carrera 92 #152-10", categories = listOf("Plástico", "Cartón"), latitude = 4.7550, longitude = -74.0920),
        RecyclingPoint(name = "Punto Limpio Modelia", address = "Calle 24 #75-10", categories = listOf("Metal", "Plástico"), latitude = 4.6650, longitude = -74.1220),
        RecyclingPoint(name = "EcoPunto Castilla", address = "Calle 8 #79-40", categories = listOf("Papel", "Vidrio"), latitude = 4.6450, longitude = -74.1480),
        RecyclingPoint(name = "Reciclaje Quirigua", address = "Calle 82 #91-20", categories = listOf("Plástico", "Metal"), latitude = 4.7080, longitude = -74.0980),
        RecyclingPoint(name = "Punto Verde Venecia", address = "Autopista Sur #52-10", categories = listOf("Cartón", "Vidrio"), latitude = 4.5920, longitude = -74.1380),
        RecyclingPoint(name = "EcoSuba La Gaitana", address = "Calle 132 #125-20", categories = listOf("Plástico", "Vidrio"), latitude = 4.7480, longitude = -74.1180),
        RecyclingPoint(name = "Recicla Kennedy Central", address = "Carrera 78K #35-10", categories = listOf("Papel", "Plástico"), latitude = 4.6220, longitude = -74.1520),
        RecyclingPoint(name = "Punto Verde San Cristóbal", address = "Calle 11 Sur #12-30", categories = listOf("Vidrio", "Metal"), latitude = 4.5780, longitude = -74.0880),
        RecyclingPoint(name = "EcoRafael Uribe", address = "Calle 32 Sur #24-10", categories = listOf("Cartón", "Plástico"), latitude = 4.5820, longitude = -74.1120),
        RecyclingPoint(name = "Punto Limpio Tunjuelito", address = "Calle 52 Sur #13-40", categories = listOf("Metal", "Plástico"), latitude = 4.5680, longitude = -74.1320),
        RecyclingPoint(name = "Recicla Puente Aranda", address = "Calle 13 #62-30", categories = listOf("Vidrio", "Electrónicos"), latitude = 4.6320, longitude = -74.1180),
        RecyclingPoint(name = "EcoBarrios Unidos", address = "Calle 68 #24-10", categories = listOf("Papel", "Metal"), latitude = 4.6680, longitude = -74.0780),
        RecyclingPoint(name = "Punto Verde Santa Fe", address = "Carrera 7 #19-20", categories = listOf("Plástico", "Vidrio"), latitude = 4.6050, longitude = -74.0680),
        RecyclingPoint(name = "Recicla Candelaria", address = "Calle 11 #3-40", categories = listOf("Papel", "Cartón"), latitude = 4.5980, longitude = -74.0720),
        RecyclingPoint(name = "EcoAntonio Nariño", address = "Calle 1 Sur #18-20", categories = listOf("Plástico", "Metal"), latitude = 4.5880, longitude = -74.0980)
    )
    
    val batch = db.batch()
    puntos.forEach { punto ->
        val ref = db.collection("puntos_reciclaje").document()
        batch.set(ref, punto)
    }
    
    batch.commit().addOnSuccessListener {
        Log.d("FirebaseHelpers", "Inserción masiva completada con éxito.")
    }
}

fun agregarOEditarResena(puntoId: String, uid: String, resena: Review, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val db = Firebase.firestore
    val puntoRef = db.collection("puntos_reciclaje").document(puntoId)
    
    db.runTransaction { transaction ->
        val snapshot = transaction.get(puntoRef)
        val punto = snapshot.toObject<RecyclingPoint>() ?: return@runTransaction
        
        val reviews = punto.reviews.toMutableList()
        val index = reviews.indexOfFirst { it.id == uid }
        
        if (index != -1) {
            reviews[index] = resena.copy(id = uid)
        } else {
            reviews.add(resena.copy(id = uid))
        }
        
        val nuevoPromedio = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
        
        transaction.update(puntoRef, "reviews", reviews)
        transaction.update(puntoRef, "averageRating", nuevoPromedio)
    }.addOnSuccessListener { onSuccess() }
    .addOnFailureListener { onError(it) }
}

fun subirFotoPerfil(uid: String, imageUri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val storageRef = Firebase.storage.reference.child("perfiles/$uid.jpg")
    val db = Firebase.firestore

    storageRef.putFile(imageUri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                db.collection("usuarios").document(uid)
                    .update("fotoUrl", uri.toString())
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.message ?: "Error al actualizar Firestore") }
            }
        }
        .addOnFailureListener { e -> onError(e.message ?: "Error al subir imagen") }
}

fun eliminarFotoPerfil(uid: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val storageRef = Firebase.storage.reference.child("perfiles/$uid.jpg")
    val db = Firebase.firestore

    storageRef.delete()
        .addOnCompleteListener {
            db.collection("usuarios").document(uid)
                .update("fotoUrl", "")
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "Error al actualizar Firestore") }
        }
}
