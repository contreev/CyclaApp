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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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

data class UserProfile(
    val uid: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val fechaNacimiento: String = "",
    val puntos: Int = 0,
    val nivel: String = "Inicial"
)

data class MissionItem(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val progreso: Int = 0,
    val meta: Int = 1,
    val recompensa: Int = 0,
    val completada: Boolean = false
)

data class BadgeItem(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val desbloqueado: Boolean = false
)

data class RegistroItem(
    val id: String = "",
    val tipoResiduo: String = "",
    val fecha: String = "",
    val hora: String = "",
    val puntosGanados: Int = 0
)

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
            },
            onProfileClick = { screen = "profile" },
            onMissionsClick = { screen = "missions" }
        )

        "profile" -> ProfileScreen(
            onBack = { screen = "menu" },
            onHomeClick = { screen = "menu" },
            onMissionsClick = { screen = "missions" },
            onProfileClick = { screen = "profile" }
        )

        "missions" -> MissionsScreen(
            onBack = { screen = "menu" },
            onHomeClick = { screen = "menu" },
            onMissionsClick = { screen = "missions" },
            onProfileClick = { screen = "profile" }
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
                                                    nivel = "Inicial"
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

fun crearDatosInicialesUsuario(uid: String) {
    val db = Firebase.firestore

    val misiones = listOf(
        MissionItem(
            id = "m1",
            titulo = "Recicla 3 botellas",
            descripcion = "Deposita 3 botellas plásticas correctamente.",
            progreso = 0,
            meta = 3,
            recompensa = 30,
            completada = false
        ),
        MissionItem(
            id = "m2",
            titulo = "Primer registro",
            descripcion = "Realiza tu primer registro de reciclaje.",
            progreso = 0,
            meta = 1,
            recompensa = 20,
            completada = false
        ),
        MissionItem(
            id = "m3",
            titulo = "Visita un punto verde",
            descripcion = "Consulta un punto de reciclaje cercano.",
            progreso = 0,
            meta = 1,
            recompensa = 15,
            completada = false
        )
    )

    val logros = listOf(
        BadgeItem(
            id = "l1",
            titulo = "Primer paso",
            descripcion = "Crea tu cuenta en CyclApp.",
            desbloqueado = true
        ),
        BadgeItem(
            id = "l2",
            titulo = "Reciclador inicial",
            descripcion = "Completa tu primer registro.",
            desbloqueado = false
        ),
        BadgeItem(
            id = "l3",
            titulo = "Explorador verde",
            descripcion = "Consulta un punto de reciclaje.",
            desbloqueado = false
        )
    )

    misiones.forEach { mission ->
        db.collection("usuarios")
            .document(uid)
            .collection("misiones")
            .document(mission.id)
            .set(mission)
    }

    logros.forEach { logro ->
        db.collection("usuarios")
            .document(uid)
            .collection("logros")
            .document(logro.id)
            .set(logro)
    }
}

@Composable
fun MenuScreen(
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMissionsClick: () -> Unit
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

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onMissionsClick() }
                    ) {
                        CategoryCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Misiones y Logros",
                            imageRes = R.drawable.logo,
                            arrowColor = accentBrown
                        )
                    }
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
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onMissionsClick() }
                )
            }

            BottomNavItem(selected = false) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Perfil",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { onProfileClick() }
                )
            }
        }
    }
}

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
    var refreshKey by remember { mutableIntStateOf(0) }

    var nombreEdit by remember { mutableStateOf("") }
    var apellidoEdit by remember { mutableStateOf("") }
    var fechaEdit by remember { mutableStateOf("") }

    LaunchedEffect(user?.uid, refreshKey) {
        val uid = user?.uid
        if (uid == null) {
            loading = false
            return@LaunchedEffect
        }

        loading = true

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                val data = document.toObject<UserProfile>()
                if (data != null) {
                    profile = data
                    nombreEdit = data.nombre
                    apellidoEdit = data.apellido
                    fechaEdit = data.fechaNacimiento
                }
                loading = false
            }
            .addOnFailureListener {
                loading = false
            }

        db.collection("usuarios").document(uid).collection("logros").get()
            .addOnSuccessListener { snapshot ->
                badges = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<BadgeItem>()?.copy(id = doc.id)
                }
            }

        db.collection("usuarios").document(uid).collection("registros").get()
            .addOnSuccessListener { snapshot ->
                registros = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<RegistroItem>()?.copy(id = doc.id)
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
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(120.dp),
                            tint = Color.Black
                        )

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
                                        .update("nombre", nombreEdit)
                                        .addOnSuccessListener { refreshKey++ }
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
                                        .update("apellido", apellidoEdit)
                                        .addOnSuccessListener { refreshKey++ }
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
                                        .update("fechaNacimiento", fechaEdit)
                                        .addOnSuccessListener { refreshKey++ }
                                }
                            )
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
                        text = "Puntos: ${profile.puntos}   |   Nivel: ${profile.nivel}",
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

@Composable
fun ProfileEditableRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    val green = Color(0xFFB8CB6A)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                text = if (editing) "Guardar" else "Editar",
                color = green,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.clickable {
                    if (editing) onSave()
                    editing = !editing
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (editing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            Text(
                text = if (value.isBlank()) "Sin agregar" else value,
                fontSize = 14.sp
            )
        }
    }
}

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

    LaunchedEffect(auth.currentUser?.uid) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            loading = false
            return@LaunchedEffect
        }

        db.collection("usuarios")
            .document(uid)
            .collection("misiones")
            .get()
            .addOnSuccessListener { snapshot ->
                missions = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<MissionItem>()?.copy(id = doc.id)
                }
                loading = false
            }
            .addOnFailureListener {
                loading = false
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

                                Text(
                                    text = "Recompensa: ${mission.recompensa} pts",
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = if (mission.completada) {
                                        "Estado: completada"
                                    } else {
                                        "Estado: pendiente"
                                    },
                                    color = if (mission.completada) {
                                        Color(0xFF2E7D32)
                                    } else {
                                        Color(0xFFD32F2F)
                                    },
                                    fontWeight = FontWeight.Bold
                                )
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

@Composable
fun AppBottomBar(
    selected: String,
    onHomeClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val navGreen = Color(0xFFB8CB6A)

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
        BottomNavItem(selected = selected == "home") {
            Icon(
                imageVector = Icons.Outlined.Home,
                contentDescription = "Inicio",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onHomeClick() }
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

        BottomNavItem(selected = selected == "missions") {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = "Misiones",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onMissionsClick() }
            )
        }

        BottomNavItem(selected = selected == "profile") {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "Perfil",
                tint = Color.Black,
                modifier = Modifier
                    .size(30.dp)
                    .clickable { onProfileClick() }
            )
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