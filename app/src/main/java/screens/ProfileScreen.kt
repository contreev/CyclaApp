package com.example.cyclapp.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cyclapp.R
import com.example.cyclapp.components.AppBottomBar
import com.example.cyclapp.components.ProfileEditableRow
import com.example.cyclapp.data.eliminarFotoPerfil
import com.example.cyclapp.data.subirFotoPerfil
import com.example.cyclapp.model.BadgeItem
import com.example.cyclapp.model.RegistroItem
import com.example.cyclapp.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore
    val user = auth.currentUser

    val mainGreen = Color(0xFFB8CB6A)
    val bgLight = Color(0xFFF8F9FA)
    val cardBg = Color.White
    val textPrimary = Color(0xFF2D3436)
    val textSecondary = Color(0xFF636E72)
    val dangerRed = Color(0xFFEF5350)

    var profile by remember { mutableStateOf(UserProfile()) }
    var badges by remember { mutableStateOf(listOf<BadgeItem>()) }
    var registros by remember { mutableStateOf(listOf<RegistroItem>()) }
    var loading by remember { mutableStateOf(true) }

    var nombreEdit by remember { mutableStateOf("") }
    var apellidoEdit by remember { mutableStateOf("") }
    var fechaEdit by remember { mutableStateOf("") }

    val context = LocalContext.current
    var subiendoFoto by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val uid = user?.uid
        if (uri != null && uid != null) {
            subiendoFoto = true
            subirFotoPerfil(context, uid, uri, {
                subiendoFoto = false
                Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
            }, { subiendoFoto = false })
        }
    }

    DisposableEffect(user?.uid) {
        val uid = user?.uid
        if (uid == null) {
            loading = false
            onDispose { }
        } else {
            loading = true
            val listeners = mutableListOf<ListenerRegistration>()

            val perfilListener = db.collection("usuarios").document(uid)
                .addSnapshotListener { document, _ ->
                    if (document != null && document.exists()) {
                        val data = document.toObject<UserProfile>()
                        if (data != null) {
                            profile = data
                            nombreEdit = data.nombre
                            apellidoEdit = data.apellido
                            fechaEdit = data.fechaNacimiento
                        }
                    }
                    loading = false
                }

            val logrosListener = db.collection("usuarios").document(uid).collection("logros")
                .addSnapshotListener { snapshot, _ ->
                    badges = snapshot?.documents?.mapNotNull { it.toObject<BadgeItem>() } ?: emptyList()
                }

            val registrosListener = db.collection("usuarios").document(uid).collection("registros")
                .addSnapshotListener { snapshot, _ ->
                    registros = snapshot?.documents?.mapNotNull { it.toObject<RegistroItem>() } ?: emptyList()
                }

            listeners.addAll(listOf(perfilListener, logrosListener, registrosListener))
            onDispose { listeners.forEach { it.remove() } }
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selected = "profile",
                onHomeClick = onHomeClick,
                onMissionsClick = onMissionsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().background(bgLight).padding(innerPadding)
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = mainGreen)
                }
                return@Scaffold
            }

            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Text("←", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Text("Mi Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                Spacer(modifier = Modifier.size(48.dp))
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
            ) {
                // Avatar Section
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (profile.fotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = profile.fotoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(130.dp).clip(CircleShape).border(3.dp, mainGreen, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Outlined.AccountCircle, null, modifier = Modifier.size(130.dp), tint = textSecondary)
                        }
                        if (subiendoFoto) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = mainGreen)
                        IconButton(
                            onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.background(mainGreen, CircleShape).size(36.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Stats Card
                Row(
                    modifier = Modifier.fillMaxWidth().background(cardBg, RoundedCornerShape(16.dp)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PUNTOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                        Text("${profile.puntos}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = mainGreen)
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp, color = bgLight)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NIVEL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                        Text(profile.nivel, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logros Section
                val unlockedCount = badges.count { it.desbloqueado }
                SectionHeader(title = "MIS LOGROS ($unlockedCount / ${badges.size})")
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (badges.isEmpty()) {
                        Text("No hay logros disponibles", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        badges.take(4).forEach { badge ->
                            LogroIcon(badge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Info Personal
                SectionHeader(title = "INFORMACIÓN PERSONAL")
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileEditableRow("Nombre", nombreEdit, { nombreEdit = it }, {
                            db.collection("usuarios").document(user?.uid ?: "").update("nombre", nombreEdit)
                        })
                        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = bgLight)
                        ProfileEditableRow("Apellido", apellidoEdit, { apellidoEdit = it }, {
                            db.collection("usuarios").document(user?.uid ?: "").update("apellido", apellidoEdit)
                        })
                        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = bgLight)
                        ProfileEditableRow("Fecha de Nacimiento", fechaEdit, { fechaEdit = it }, {
                            db.collection("usuarios").document(user?.uid ?: "").update("fechaNacimiento", fechaEdit)
                        })
                        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = bgLight)
                        Text("Correo Electrónico", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textSecondary)
                        Text(profile.correo, fontSize = 14.sp, color = textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF636E72),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun LogroIcon(badge: BadgeItem) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(if (badge.desbloqueado) Color(0xFFFFF9C4) else Color(0xFFF1F2F6), CircleShape)
                .border(2.dp, if (badge.desbloqueado) Color(0xFFFFD600) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(if (badge.desbloqueado) "🏅" else "🔒", fontSize = 24.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(badge.titulo, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
