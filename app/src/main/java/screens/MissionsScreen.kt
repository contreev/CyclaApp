package com.example.cyclapp.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyclapp.components.AppBottomBar
import com.example.cyclapp.data.completarPasoMision
import com.example.cyclapp.data.crearDatosInicialesUsuario
import com.example.cyclapp.model.MissionItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMapClick: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore
    val bg = Color(0xFFF5F5F5)

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

            misionesRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) crearDatosInicialesUsuario(uid)
            }

            val listener = misionesRef.addSnapshotListener { snapshot, _ ->
                missions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<MissionItem>()?.copy(id = doc.id)
                } ?: emptyList()
                loading = false
            }
            onDispose { listener.remove() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Misiones y Logros", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            crearDatosInicialesUsuario(uid)
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reiniciar Datos", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFB8CB6A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            AppBottomBar(
                selected = "missions",
                onHomeClick = onHomeClick,
                onMissionsClick = onMissionsClick,
                onProfileClick = onProfileClick,
                onMapClick = onMapClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(bg)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFB8CB6A))
            } else if (missions.isEmpty()) {
                Text("No hay misiones disponibles", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(missions) { mission ->
                        MissionCard(mission) {
                            val uid = auth.currentUser?.uid
                            if (uid != null) completarPasoMision(uid, mission)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MissionCard(mission: MissionItem, onCompleteClick: () -> Unit) {
    val progress = mission.progreso.toFloat() / mission.meta.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Sticker de completado (Copa)
            if (mission.completada) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(45.dp)
                        .background(Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Completado",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFB8CB6A),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mission.titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF333333)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = mission.descripcion,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Barra de progreso mejorada
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Progreso del reto",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "${mission.progreso}/${mission.meta}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB8CB6A)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(Color(0xFFEEEEEE), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFB8CB6A), Color(0xFF8BC34A))
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Recompensa", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "+${mission.recompensa} PUNTOS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB78B66)
                        )
                    }

                    Button(
                        onClick = onCompleteClick,
                        enabled = !mission.completada,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB8CB6A),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (mission.completada) "¡LOGRADO!" else "AVANZAR",
                            fontWeight = FontWeight.Bold,
                            color = if (mission.completada) Color.Gray else Color.White
                        )
                    }
                }
            }
        }
    }
}
