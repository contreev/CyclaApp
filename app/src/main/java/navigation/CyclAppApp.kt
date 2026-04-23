package com.example.cyclapp.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.cyclapp.screens.AuthScreen
import com.example.cyclapp.screens.ForgotPasswordScreen
import com.example.cyclapp.screens.MenuScreen
import com.example.cyclapp.screens.MissionsScreen
import com.example.cyclapp.screens.NewsScreen
import com.example.cyclapp.screens.ProfileScreen
import com.example.cyclapp.screens.WelcomeScreen
import com.example.cyclapp.screens.WasteScreen
import com.google.firebase.auth.FirebaseAuth

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
            onForgotPasswordClick = { screen = "forgot_password" },
            startTab = startTab
        )

        "forgot_password" -> ForgotPasswordScreen(
            auth = auth,
            onBackClick = { screen = "auth" }
        )

        "menu" -> MenuScreen(
            onLogoutClick = {
                auth.signOut()
                screen = "welcome"
            },
            onProfileClick = { screen = "profile" },
            onMissionsClick = { screen = "missions" },
            onNewsClick = { screen = "news" },
            onWasteClick = { screen = "waste" }
        )

        "news" -> NewsScreen(
            onBackClick = { screen = "menu" }
        )

        "waste" -> WasteScreen(
            onBackClick = { screen = "menu" }
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