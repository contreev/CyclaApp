package com.example.cyclapp.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.text.font.FontWeight

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

    val bg = Color(0xFFE9E9E9)
    val card = Color(0xFFF5F5F5)

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
            subirFotoPerfil(
                uid = uid,
                imageUri = uri,
                onSuccess = {
                    subiendoFoto = false
                    Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    subiendoFoto = false
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
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

            val perfilListener = db.collection("usuarios")
                .document(uid)
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

            val logrosListener = db.collection("usuarios")
                .document(uid)
                .collection("logros")
                .addSnapshotListener { snapshot, _ ->
                    badges = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject<BadgeItem>()?.copy(id = doc.id)
                    } ?: emptyList()
                }

            val registrosListener = db.collection("usuarios")
                .document(uid)
                .collection("registros")
                .addSnapshotListener { snapshot, _ ->
                    registros = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject<RegistroItem>()?.copy(id = doc.id)
                    } ?: emptyList()
                }

            listeners.add(perfilListener)
            listeners.add(logrosListener)
            listeners.add(registrosListener)

            onDispose {
                listeners.forEach { it.remove() }
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
        if (loading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            AppBottomBar(
                selected = "profile",
                onHomeClick = onHomeClick,
                onMissionsClick = onMissionsClick,
                onProfileClick = onProfileClick
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "←",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onBack() }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "CyclApp",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    text = "☰",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(card, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profile.fotoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = profile.fotoUrl,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = "Perfil",
                                    modifier = Modifier.size(120.dp),
                                    tint = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            ProfileEditableRow(
                                label = "Nombre",
                                value = nombreEdit,
                                onValueChange = { nombreEdit = it },
                                onSave = {
                                    val uid = user?.uid ?: return@ProfileEditableRow
                                    db.collection("usuarios")
                                        .document(uid)
                                        .set(
                                            mapOf("nombre" to nombreEdit.trim()),
                                            SetOptions.merge()
                                        )
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            ProfileEditableRow(
                                label = "Apellido",
                                value = apellidoEdit,
                                onValueChange = { apellidoEdit = it },
                                onSave = {
                                    val uid = user?.uid ?: return@ProfileEditableRow
                                    db.collection("usuarios")
                                        .document(uid)
                                        .set(
                                            mapOf("apellido" to apellidoEdit.trim()),
                                            SetOptions.merge()
                                        )
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            ProfileEditableRow(
                                label = "Fecha de nacimiento",
                                value = fechaEdit,
                                onValueChange = { fechaEdit = it },
                                onSave = {
                                    val uid = user?.uid ?: return@ProfileEditableRow
                                    db.collection("usuarios")
                                        .document(uid)
                                        .set(
                                            mapOf("fechaNacimiento" to fechaEdit.trim()),
                                            SetOptions.merge()
                                        )
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFB8CB6A)
                                    ),
                                    enabled = !subiendoFoto
                                ) {
                                    Text(
                                        text = if (profile.fotoUrl.isBlank()) "Poner foto" else "Cambiar foto",
                                        color = Color.White
                                    )
                                }

                                Button(
                                    onClick = {
                                        val uid = user?.uid ?: return@Button
                                        subiendoFoto = true
                                        eliminarFotoPerfil(
                                            uid = uid,
                                            onSuccess = {
                                                subiendoFoto = false
                                                Toast.makeText(
                                                    context,
                                                    "Foto eliminada",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onError = { error ->
                                                subiendoFoto = false
                                                Toast.makeText(
                                                    context,
                                                    error,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    },
                                    enabled = profile.fotoUrl.isNotBlank() && !subiendoFoto,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Gray
                                    )
                                ) {
                                    Text(
                                        text = "Eliminar",
                                        color = Color.White
                                    )
                                }
                            }

                            if (subiendoFoto) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Subiendo foto...",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Correo: ${profile.correo}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Puntos: ${profile.puntos}  |  Nivel: ${profile.nivel}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(thickness = 2.dp, color = Color.Black)
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8E8E8), RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "LOGROS",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF7A5A46)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (badges.isEmpty()) {
                                Text("Aún no hay logros.")
                            } else {
                                badges.forEach { badge ->
                                    Text(
                                        text = if (badge.desbloqueado) {
                                            "🏅 ${badge.titulo}"
                                        } else {
                                            "🔒 ${badge.titulo}"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8E8E8), RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "TODOS LOS REGISTROS",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF7A5A46)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (registros.isEmpty()) {
                                Text("Aún no hay registros.")
                            } else {
                                registros.forEach { reg ->
                                    Text(
                                        text = "${reg.tipoResiduo} ${reg.hora} ${reg.fecha}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        AppBottomBar(
            selected = "profile",
            onHomeClick = onHomeClick,
            onMissionsClick = onMissionsClick,
            onProfileClick = onProfileClick
        )
    }
}