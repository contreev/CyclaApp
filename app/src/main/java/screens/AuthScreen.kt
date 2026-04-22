package com.example.cyclapp.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyclapp.R
import com.example.cyclapp.components.ThinField
import com.example.cyclapp.data.crearDatosInicialesUsuario
import com.example.cyclapp.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun AuthScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    startTab: String = "login"
) {
    val context = LocalContext.current
    val db = Firebase.firestore

    val mainGreen = Color(0xFFB8CB6A)
    val cardColor = Color(0xFFF3F3F3)
    val lineColor = Color(0xFFD7D7D7)
    val softText = Color(0xFFB5B5B5)
    val darkText = Color(0xFF1F1F1F)
    val inactiveTab = Color(0xFF8E8E8E)
    val errorColor = Color(0xFFD32F2F)
    val successColor = Color(0xFF2E7D32)

    var selectedTab by remember { mutableStateOf(startTab) }

    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    var registerName by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerConfirmPassword by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }
    var isErrorMessage by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.reciclaje_fondo),
            contentDescription = "Fondo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x33000000))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(70.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "CyclApp",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = cardColor,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFE6E6E6),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedTab == "login") mainGreen else Color.Transparent,
                                RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                if (!isLoading) {
                                    selectedTab = "login"
                                    message = ""
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            color = if (selectedTab == "login") Color.White else inactiveTab,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedTab == "register") mainGreen else Color.Transparent,
                                RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                if (!isLoading) {
                                    selectedTab = "register"
                                    message = ""
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Registrarse",
                            color = if (selectedTab == "register") Color.White else inactiveTab,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (selectedTab == "login") "Bienvenido a CyclApp" else "Crea tu cuenta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkText
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedTab == "login") {
                    ThinField(
                        value = loginEmail,
                        onValueChange = {
                            loginEmail = it
                            message = ""
                        },
                        placeholder = "E-mail",
                        placeholderColor = softText,
                        textColor = darkText,
                        lineColor = lineColor,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ThinField(
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
                            message = ""
                        },
                        placeholder = "Contraseña",
                        placeholderColor = softText,
                        textColor = darkText,
                        lineColor = lineColor,
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            color = if (isErrorMessage) errorColor else successColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Button(
                        onClick = {
                            when {
                                loginEmail.isBlank() || loginPassword.isBlank() -> {
                                    isErrorMessage = true
                                    message = "Por favor completa todos los campos."
                                }

                                !isValidEmail(loginEmail) -> {
                                    isErrorMessage = true
                                    message = "Ingresa un correo válido."
                                }

                                else -> {
                                    isLoading = true
                                    auth.signInWithEmailAndPassword(
                                        loginEmail.trim(),
                                        loginPassword.trim()
                                    ).addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Inicio de sesión exitoso",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onLoginSuccess()
                                        } else {
                                            isErrorMessage = true
                                            message = task.exception?.localizedMessage
                                                ?: "No se pudo iniciar sesión."
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = mainGreen)
                    ) {
                        Text(
                            text = if (isLoading) "Cargando..." else "Iniciar Sesión",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = softText,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            if (!isLoading) onForgotPasswordClick()
                        }
                    )
                } else {
                    ThinField(
                        value = registerName,
                        onValueChange = {
                            registerName = it
                            message = ""
                        },
                        placeholder = "Nombre",
                        placeholderColor = softText,
                        textColor = darkText,
                        lineColor = lineColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ThinField(
                        value = registerEmail,
                        onValueChange = {
                            registerEmail = it
                            message = ""
                        },
                        placeholder = "E-mail",
                        placeholderColor = softText,
                        textColor = darkText,
                        lineColor = lineColor,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ThinField(
                        value = registerPassword,
                        onValueChange = {
                            registerPassword = it
                            message = ""
                        },
                        placeholder = "Contraseña",
                        placeholderColor = softText,
                        textColor = darkText,
                        lineColor = lineColor,
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ThinField(
                        value = registerConfirmPassword,
                        onValueChange = {
                            registerConfirmPassword = it
                            message = ""
                        },
                        placeholder = "Confirmar Contraseña",
                        placeholderColor = softText,
                        textColor = darkText,
                        lineColor = lineColor,
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            color = if (isErrorMessage) errorColor else successColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Button(
                        onClick = {
                            when {
                                registerName.isBlank() ||
                                        registerEmail.isBlank() ||
                                        registerPassword.isBlank() ||
                                        registerConfirmPassword.isBlank() -> {
                                    isErrorMessage = true
                                    message = "Completa todos los campos."
                                }

                                !isValidEmail(registerEmail) -> {
                                    isErrorMessage = true
                                    message = "Ingresa un correo válido."
                                }

                                registerPassword.length < 6 -> {
                                    isErrorMessage = true
                                    message = "La contraseña debe tener mínimo 6 caracteres."
                                }

                                registerPassword != registerConfirmPassword -> {
                                    isErrorMessage = true
                                    message = "Las contraseñas no coinciden."
                                }

                                else -> {
                                    isLoading = true
                                    auth.createUserWithEmailAndPassword(
                                        registerEmail.trim(),
                                        registerPassword.trim()
                                    ).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val firebaseUser = auth.currentUser

                                            if (firebaseUser != null) {
                                                val userProfile = UserProfile(
                                                    uid = firebaseUser.uid,
                                                    nombre = registerName.trim(),
                                                    apellido = "",
                                                    correo = registerEmail.trim(),
                                                    fechaNacimiento = "",
                                                    puntos = 0,
                                                    nivel = "Inicial",
                                                    fotoUrl = ""
                                                )

                                                db.collection("usuarios")
                                                    .document(firebaseUser.uid)
                                                    .set(userProfile)
                                                    .addOnSuccessListener {
                                                        crearDatosInicialesUsuario(firebaseUser.uid)

                                                        isLoading = false
                                                        Toast.makeText(
                                                            context,
                                                            "Cuenta creada correctamente",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        onLoginSuccess()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        isLoading = false
                                                        isErrorMessage = true
                                                        message = e.localizedMessage
                                                            ?: "No se pudo guardar el perfil."
                                                    }
                                            } else {
                                                isLoading = false
                                                isErrorMessage = true
                                                message = "No se pudo obtener el usuario creado."
                                            }
                                        } else {
                                            isLoading = false
                                            isErrorMessage = true
                                            message = task.exception?.localizedMessage
                                                ?: "No se pudo crear la cuenta."
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = mainGreen)
                    ) {
                        Text(
                            text = if (isLoading) "Cargando..." else "Registrarse",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Volver",
                    color = mainGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            if (!isLoading) onBackClick()
                        }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}