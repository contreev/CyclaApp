package com.example.cyclapp.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.cyclapp.R
import com.example.cyclapp.components.AppBottomBar
import com.example.cyclapp.data.WasteClassifier
import com.example.cyclapp.data.registrarResiduoDetectado
import com.example.cyclapp.model.MissionItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun CameraScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMapClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val auth = FirebaseAuth.getInstance()
    
    val classifier = remember { WasteClassifier(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    
    var capturedImagePath by remember { mutableStateOf<String?>(null) }
    var classificationResult by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Estado para la notificación de misión
    var missionNotification by remember { mutableStateOf<Pair<MissionItem, Boolean>?>(null) }

    // Ocultar notificación después de 3 segundos
    LaunchedEffect(missionNotification) {
        if (missionNotification != null) {
            delay(3500)
            missionNotification = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            classifier.close()
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selected = "camera",
                onHomeClick = onHomeClick,
                onMissionsClick = onMissionsClick,
                onProfileClick = onProfileClick,
                onMapClick = onMapClick,
                onCameraClick = { /* Ya estamos aquí */ }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            if (hasCameraPermission) {
                if (capturedImagePath != null) {
                    CapturedImagePreview(
                        imagePath = capturedImagePath!!,
                        result = classificationResult,
                        isAnalyzing = isAnalyzing,
                        onRetake = {
                            capturedImagePath = null
                            classificationResult = null
                        },
                        onAnalyze = {
                            val bitmap = BitmapFactory.decodeFile(capturedImagePath)
                            if (bitmap != null) {
                                isAnalyzing = true
                                val results = classifier.classify(bitmap)
                                if (results.isNotEmpty()) {
                                    val topResult = results[0]
                                    classificationResult = "${topResult.label} (${(topResult.score * 100).toInt()}%)"
                                    
                                    // Actualizar misiones si la confianza es alta
                                    if (topResult.score > 0.7f) {
                                        val uid = auth.currentUser?.uid
                                        if (uid != null) {
                                            registrarResiduoDetectado(uid, topResult.label) { mission, completed ->
                                                missionNotification = mission to completed
                                            }
                                        }
                                    }
                                } else {
                                    classificationResult = "No se pudo identificar"
                                }
                                isAnalyzing = false
                            }
                        }
                    )
                } else {
                    // Vista de Cámara
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val executor = ContextCompat.getMainExecutor(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture
                                    )
                                } catch (e: Exception) {
                                    Log.e("CameraScreen", "Use case binding failed", e)
                                }
                            }, executor)
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay de Escaneo (Marco blanco)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        )
                    }

                    // UI Superior
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.Black)
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
                                    color = Color(0xFFB8CB6A),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(onClick = { /* Menú */ }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.Black)
                            }
                        }
                    }

                    // Textos de estado
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 320.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Escaneando...",
                            color = Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Apunta a un residuo",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }

                    // Botones de control inferiores
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { /* Galería */ },
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Image, 
                                contentDescription = "Galería", 
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Botón de Captura
                        Surface(
                            modifier = Modifier
                                .size(70.dp)
                                .clickable {
                                    val photoFile = File(
                                        context.cacheDir,
                                        "residuo_${System.currentTimeMillis()}.jpg"
                                    )

                                    val outputOptions = ImageCapture.OutputFileOptions
                                        .Builder(photoFile)
                                        .build()

                                    imageCapture.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(
                                                outputFileResults: ImageCapture.OutputFileResults
                                            ) {
                                                capturedImagePath = photoFile.absolutePath
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                Log.e("CameraScreen", "Error capturando imagen", exception)
                                            }
                                        }
                                    )
                                },
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(4.dp, Color.LightGray)
                        ) { }

                        IconButton(
                            onClick = { /* Flash */ },
                            modifier = Modifier.size(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FlashOn,
                                contentDescription = "Flash",
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Se requiere permiso de cámara para esta función")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { launcher.launch(Manifest.permission.CAMERA) }
                        ) {
                            Text("Habilitar Cámara")
                        }
                    }
                }
            }

            // Notificación Flotante de Misión
            AnimatedVisibility(
                visible = missionNotification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
            ) {
                missionNotification?.let { (mission, completed) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (completed) Color(0xFFE8F5E9) else Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (completed) Color(0xFF4CAF50) else Color(0xFFB8CB6A), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (completed) Icons.Default.Check else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (completed) "¡MISIÓN COMPLETADA!" else "¡PROGRESO DE MISIÓN!",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = if (completed) Color(0xFF2E7D32) else Color(0xFFB8CB6A)
                                )
                                Text(
                                    text = mission.titulo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                LinearProgressIndicator(
                                    progress = { mission.progreso.toFloat() / mission.meta.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFFB8CB6A),
                                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                                )
                                Text(
                                    text = "${mission.progreso}/${mission.meta}",
                                    fontSize = 11.sp,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CapturedImagePreview(
    imagePath: String,
    result: String?,
    isAnalyzing: Boolean,
    onRetake: () -> Unit,
    onAnalyze: () -> Unit
) {
    val bitmap = remember(imagePath) {
        BitmapFactory.decodeFile(imagePath)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Análisis de Residuo",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (bitmap != null) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto del residuo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                
                if (result != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp),
                        color = Color(0xFFB8CB6A),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = result,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (result == null) {
            Button(
                onClick = onAnalyze,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                enabled = !isAnalyzing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB8CB6A),
                    contentColor = Color.White
                )
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("IDENTIFICAR RESIDUO", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¡Residuo registrado en tus misiones!", fontSize = 14.sp, color = Color(0xFF2E7D32))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRetake,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Text("REPETIR FOTO", color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}
