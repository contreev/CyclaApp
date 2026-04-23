package com.example.cyclapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cyclapp.R
import com.example.cyclapp.components.AppBottomBar
import com.example.cyclapp.model.RecyclingPoint
import com.example.cyclapp.model.Review
import com.example.cyclapp.model.UserProfile
import com.example.cyclapp.viewmodel.MapViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MapScreen(
    onBackClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onProfileClick: () -> Unit,
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val puntos by mapViewModel.puntos.collectAsState()
    val searchQuery by mapViewModel.searchQuery.collectAsState()
    var selectedPoint by remember { mutableStateOf<RecyclingPoint?>(null) }
    val accentGreen = Color(0xFFA8B978)

    // Configuración indispensable para osmdroid
    SideEffect {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Configuración de OpenStreetMap
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(4.6097, -74.0817)) // Bogotá
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa Interactivo
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.overlays.clear()
                puntos.forEach { punto ->
                    val marker = Marker(view)
                    marker.position = GeoPoint(punto.latitude, punto.longitude)
                    marker.title = punto.name
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.setOnMarkerClickListener { m, _ ->
                        selectedPoint = punto
                        view.controller.animateTo(GeoPoint(punto.latitude, punto.longitude))
                        true
                    }
                    view.overlays.add(marker)
                }
                
                // Si hay una búsqueda activa y hay resultados, mover el mapa al primero
                if (searchQuery.isNotEmpty() && puntos.isNotEmpty()) {
                    val firstPoint = puntos.first()
                    view.controller.animateTo(GeoPoint(firstPoint.latitude, firstPoint.longitude))
                }
                
                view.invalidate()
            }
        )

        // Overlay Superior
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CyclApp",
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = accentGreen,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(end = 100.dp)
            ) {
                Text(
                    text = "Mapa de reciclaje",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        // Barra de búsqueda funcional
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { mapViewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxSize(),
                placeholder = { Text("Buscar Punto de reciclaje", fontWeight = FontWeight.Bold) },
                leadingIcon = { 
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(28.dp)) 
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )
        }

        // Bottom Bar
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            AppBottomBar(
                selected = "map",
                onHomeClick = onBackClick,
                onMissionsClick = onMissionsClick,
                onProfileClick = onProfileClick
            )
        }

        if (selectedPoint != null) {
            PointDetailOverlay(
                point = selectedPoint!!,
                onDismiss = { selectedPoint = null },
                onSendReview = { resena, uid ->
                    mapViewModel.enviarResena(selectedPoint!!.id, uid, resena)
                }
            )
        }
    }
}

@Composable
fun PointDetailOverlay(point: RecyclingPoint, onDismiss: () -> Unit, onSendReview: (Review, String) -> Unit) {
    val accentGreen = Color(0xFFA8B978)
    val lightGray = Color(0xFFF0F0F0)
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uid = currentUser?.uid ?: ""
    val db = Firebase.firestore

    var currentUserName by remember { mutableStateOf("Usuario") }
    
    // Cargar nombre del usuario actual
    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            db.collection("usuarios").document(uid).get().addOnSuccessListener { doc ->
                val userProfile = doc.toObject<UserProfile>()
                if (userProfile != null) {
                    currentUserName = "${userProfile.nombre} ${userProfile.apellido}".trim()
                    if (currentUserName.isEmpty()) currentUserName = "Usuario"
                }
            }
        }
    }

    // Buscar si el usuario ya tiene una reseña para este punto usando su UID
    val existingReview = point.reviews.find { it.id == uid }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .clickable(enabled = false) {}
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color.LightGray, CircleShape))
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(text = point.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "📍 ${point.address}", fontSize = 16.sp, color = Color.DarkGray)
            Text(text = "♻️ ${point.categories.joinToString(" • ")}", fontSize = 14.sp, color = accentGreen, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            StarRating(rating = point.averageRating.toInt(), iconSize = 24)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección de Comentarios de la gente
            if (point.reviews.isNotEmpty()) {
                Text(
                    text = "RESEÑAS DE LA COMUNIDAD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                point.reviews.forEach { review ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(lightGray, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (review.id == uid) "Tú ($currentUserName)" else review.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            StarRating(rating = review.rating, iconSize = 14)
                        }
                        if (review.comment.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = review.comment, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (existingReview != null) "EDITA TU RESEÑA" else "¿QUÉ TE PARECIÓ ESTE PUNTO?",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
            
            var userRating by remember { mutableStateOf(existingReview?.rating ?: 0) }
            LaunchedEffect(existingReview) {
                userRating = existingReview?.rating ?: 0
            }

            StarRating(rating = userRating, onRatingSelected = { userRating = it }, iconSize = 36)

            var comment by remember { mutableStateOf(existingReview?.comment ?: "") }
            LaunchedEffect(existingReview) {
                comment = existingReview?.comment ?: ""
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("Comparte tu experiencia...") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (userRating > 0 && uid.isNotBlank()) {
                        onSendReview(Review(userName = currentUserName, rating = userRating, comment = comment, date = "Hoy"), uid)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(text = if (existingReview != null) "Actualizar Reseña" else "Enviar Reseña", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun StarRating(rating: Int, onRatingSelected: ((Int) -> Unit)? = null, iconSize: Int = 30) {
    Row(horizontalArrangement = Arrangement.Center) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (i <= rating) Color(0xFFFFD700) else Color.LightGray,
                modifier = Modifier
                    .size(iconSize.dp)
                    .clickable(enabled = onRatingSelected != null) { onRatingSelected?.invoke(i) }
            )
        }
    }
}
