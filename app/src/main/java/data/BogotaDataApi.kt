package com.example.cyclapp.data

import android.util.Log
import com.example.cyclapp.model.RecyclingPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Estructura de respuesta de Datos Abiertos Bogotá (CKAN API)
data class BogotaResponse(
    val result: BogotaResult
)

data class BogotaResult(
    val records: List<BogotaPoint>
)

data class BogotaPoint(
    val NOMBRE_ESTABLECIMIENTO: String?,
    val DIRECCION: String?,
    val LATITUD: String?,
    val LONGITUD: String?,
    val MATERIALES_RECOLECTADOS: String?
)

interface BogotaDataApi {
    @GET("api/3/action/datastore_search")
    suspend fun getPuntosReciclaje(
        @Query("resource_id") resourceId: String = "d0a3d664-9f7a-4f51-9e5c-067753381e9d", // ID oficial de puntos de reciclaje
        @Query("limit") limit: Int = 500 // Traeremos 500 puntos para empezar
    ): BogotaResponse
}

/**
 * Sincroniza los puntos de reciclaje desde la API de Datos Abiertos de Bogotá
 * hacia la base de datos de Firebase Firestore.
 */
fun sincronizarTodosLosPuntosDeBogota() {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://datosabiertos.bogota.gov.co/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(BogotaDataApi::class.java)
    val db = Firebase.firestore

    CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d("BogotaDataApi", "Iniciando sincronización de puntos...")
            val response = api.getPuntosReciclaje()
            val points = response.result.records.map { point ->
                RecyclingPoint(
                    name = point.NOMBRE_ESTABLECIMIENTO ?: "Punto sin nombre",
                    address = point.DIRECCION ?: "Sin dirección",
                    latitude = point.LATITUD?.toDoubleOrNull() ?: 0.0,
                    longitude = point.LONGITUD?.toDoubleOrNull() ?: 0.0,
                    categories = point.MATERIALES_RECOLECTADOS?.split(",")?.map { it.trim() } ?: emptyList()
                )
            }.filter { it.latitude != 0.0 && it.longitude != 0.0 } // Filtrar puntos sin coordenadas válidas

            if (points.isNotEmpty()) {
                val batch = db.batch()
                points.forEach { punto ->
                    val ref = db.collection("puntos_reciclaje").document()
                    batch.set(ref, punto)
                }
                batch.commit().await()
                Log.d("BogotaDataApi", "Sincronización completada: ${points.size} puntos insertados.")
            } else {
                Log.w("BogotaDataApi", "No se encontraron puntos para sincronizar.")
            }
        } catch (e: Exception) {
            Log.e("BogotaDataApi", "Error sincronizando puntos: ${e.message}", e)
        }
    }
}
