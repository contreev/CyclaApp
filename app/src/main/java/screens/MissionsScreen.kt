package com.example.cyclapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.cyclapp.components.AppBottomBar
import com.example.cyclapp.data.completarPasoMision
import com.example.cyclapp.data.crearDatosInicialesUsuario
import com.example.cyclapp.model.MissionItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

@Composable
fun MissionsScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore
    val bg = Color(0xFFE9E9E9)

    var missions by remember { mutableStateOf(listOf<MissionItem>()) }
    var loading by remember { mutableStateOf(true) }

    DisposableEffect(auth.currentUser?.uid) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            loading = false
            onDispose { }
        } else {
            val misionesRef = db.collection("usuarios")
                .document(uid)
                .collection("misiones")

            misionesRef.get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        crearDatosInicialesUsuario(uid)
                    }
                }

            val listener = misionesRef.addSnapshotListener { snapshot, _ ->
                missions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<MissionItem>()?.copy(id = doc.id)
                } ?: emptyList()
                loading = false
            }

            onDispose {
                listener.remove()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Text(
                text = "← Volver",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBack() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Misiones y Logros",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (loading) {
                CircularProgressIndicator()
            } else if (missions.isEmpty()) {
                Text("No hay misiones registradas.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(missions) { mission ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(22.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = mission.titulo,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = mission.descripcion,
                                    fontSize = 14.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Progreso: ${mission.progreso}/${mission.meta}",
                                    fontWeight = FontWeight.Bold
                                )

                                LinearProgressIndicator(
                                    progress = { mission.progreso.toFloat() / mission.meta.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = Color(0xFFB8CB6A)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Recompensa: ${mission.recompensa} pts",
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = if (mission.completada) "Completada ✅" else "En progreso ⏳",
                                    color = if (mission.completada) Color(0xFF2E7D32) else Color(0xFFB78B66),
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        val uid = auth.currentUser?.uid ?: return@Button
                                        completarPasoMision(uid, mission)
                                    },
                                    enabled = !mission.completada,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFB8CB6A)
                                    )
                                ) {
                                    Text(
                                        text = if (mission.completada) "Completada" else "Completar",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        AppBottomBar(
            selected = "missions",
            onHomeClick = onHomeClick,
            onMissionsClick = onMissionsClick,
            onProfileClick = onProfileClick
        )
    }
}