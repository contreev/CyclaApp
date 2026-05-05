package com.example.cyclapp.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import android.graphics.BitmapFactory
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File
import androidx.compose.ui.graphics.asImageBitmap

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
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var capturedImagePath by remember { mutableStateOf<String?>(null) }

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
                        onRetake = {
                            capturedImagePath = null
                        },
                        onAnalyze = {
                            Log.d("CameraScreen", "Imagen lista para analizar: $capturedImagePath")
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
                        text = "Detectando objeto...",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }

                // Botones de control inferiores (sobre el BottomBar)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Galería
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

                    // Botón de Captura (Círculo Blanco)
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

                    // Flash
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
                // Mensaje si no hay permiso
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
        }
    }
}
    @Composable
    fun CapturedImagePreview(
        imagePath: String,
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
                text = "Foto capturada",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto del residuo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(430.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("No se pudo cargar la imagen")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAnalyze,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB8CB6A),
                    contentColor = Color.Black
                )
            ) {
                Text("Analizar residuo")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Repetir foto")
            }
        }
    }
