package com.example.cyclapp.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyclapp.R
import com.example.cyclapp.components.ThinField
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ForgotPasswordScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val mainGreen = Color(0xFFB8CB6A)
    val cardColor = Color(0xFFF3F3F3)
    val lineColor = Color(0xFFD7D7D7)
    val softText = Color(0xFFB5B5B5)
    val darkText = Color(0xFF1F1F1F)
    val errorColor = Color(0xFFD32F2F)
    val successColor = Color(0xFF2E7D32)

    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isErrorMessage by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    fun isValidEmail(value: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(value).matches()
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
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Recuperar contraseña",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkText
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Escribe tu correo y te enviaremos un enlace para restablecer tu contraseña.",
                    fontSize = 13.sp,
                    color = softText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                ThinField(
                    value = email,
                    onValueChange = {
                        email = it
                        message = ""
                    },
                    placeholder = "Correo electrónico",
                    placeholderColor = softText,
                    textColor = darkText,
                    lineColor = lineColor,
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        color = if (isErrorMessage) errorColor else successColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Button(
                    onClick = {
                        when {
                            email.isBlank() -> {
                                isErrorMessage = true
                                message = "Ingresa tu correo."
                            }

                            !isValidEmail(email) -> {
                                isErrorMessage = true
                                message = "Ingresa un correo válido."
                            }

                            else -> {
                                isLoading = true
                                auth.sendPasswordResetEmail(email.trim())
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            isErrorMessage = false
                                            message = "Te enviamos un correo para restablecer tu contraseña."
                                            Toast.makeText(
                                                context,
                                                "Correo enviado",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            isErrorMessage = true
                                            message = task.exception?.localizedMessage
                                                ?: "No se pudo enviar el correo."
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
                        text = if (isLoading) "Enviando..." else "Confirmar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Volver",
                    color = mainGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        if (!isLoading) onBackClick()
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
