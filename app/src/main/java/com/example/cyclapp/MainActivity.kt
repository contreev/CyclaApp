package com.example.cyclapp

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyclapp.ui.theme.CyclAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CyclAppTheme {
                CyclAppApp()
            }
        }
    }
}

@Composable
fun CyclAppApp() {
    val auth = FirebaseAuth.getInstance()

    var screen by remember {
        mutableStateOf(if (auth.currentUser != null) "menu" else "welcome")
    }
    var startTab by remember { mutableStateOf("login") }

    when (screen) {
        "welcome" -> WelcomeScreen(
            onLoginClick = {
                startTab = "login"
                screen = "auth"
            },
            onRegisterClick = {
                startTab = "register"
                screen = "auth"
            }
        )

        "auth" -> AuthScreen(
            auth = auth,
            onBackClick = { screen = "welcome" },
            onLoginSuccess = { screen = "menu" },
            startTab = startTab
        )

        "menu" -> MenuScreen(
            onLogoutClick = {
                auth.signOut()
                screen = "welcome"
            }
        )
    }
}

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val buttonGreen = Color(0xFFB8CB6A)

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
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x10000000),
                            Color(0x22000000),
                            Color(0x44000000),
                            Color(0xBB000000)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
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

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonGreen)
            ) {
                Text(
                    text = "Iniciar Sesión",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = buildAnnotatedString {
                    append("¿No tienes cuenta? ")
                    withStyle(
                        style = SpanStyle(
                            color = buttonGreen,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Regístrate")
                    }
                },
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onRegisterClick() }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun AuthScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    startTab: String = "login"
) {
    val context = LocalContext.current

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
                        fontSize = 12.sp
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
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Cuenta creada correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onLoginSuccess()
                                        } else {
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

@Composable
fun MenuScreen(
    onLogoutClick: () -> Unit
) {
    val bgColor = Color(0xFFE9E9E9)
    val cardColor = Color(0xFFF8F8F8)
    val accentBrown = Color(0xFFB78B66)
    val navGreen = Color(0xFFB8CB6A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.reciclaje_fondo),
                    contentDescription = "Encabezado",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                        .background(Color(0x33FFFFFF))
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(60.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "CyclApp",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categorias",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF9D9D9D))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        title = "Residuos",
                        imageRes = R.drawable.logo,
                        arrowColor = accentBrown
                    )

                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        title = "Puntos de reciclaje",
                        imageRes = R.drawable.logo,
                        arrowColor = accentBrown
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        title = "Noticias",
                        imageRes = R.drawable.logo,
                        arrowColor = accentBrown
                    )

                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        title = "Misiones y Logros",
                        imageRes = R.drawable.logo,
                        arrowColor = accentBrown
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardColor, RoundedCornerShape(30.dp))
                        .padding(horizontal = 18.dp, vertical = 20.dp)
                ) {
                    Column {
                        Text(
                            text = "Ultimos Registros",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = accentBrown,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "♻️  13:40 6/03/2026",
                                color = accentBrown,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )

                            Text(
                                text = "🧴  13:40 6/03/2026",
                                color = accentBrown,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "🥛  13:40 6/03/2026",
                            color = accentBrown,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text(
                        text = "Cerrar sesión",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = navGreen,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(selected = true) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Inicio",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            BottomNavItem(selected = false) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Ubicación",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            BottomNavItem(selected = false) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Cámara",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            BottomNavItem(selected = false) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = "Logros",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            BottomNavItem(selected = false) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Perfil",
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    modifier: Modifier = Modifier,
    title: String,
    imageRes: Int,
    arrowColor: Color
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(30.dp))
            .padding(vertical = 14.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(arrowColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "➜",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    selected: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (selected) Color.White else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ThinField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    placeholderColor: Color,
    textColor: Color,
    lineColor: Color,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = placeholderColor,
                    fontSize = 13.sp
                )
            },
            singleLine = true,
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(
                color = textColor,
                fontSize = 14.sp
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(
            thickness = 1.dp,
            color = lineColor
        )
    }
}