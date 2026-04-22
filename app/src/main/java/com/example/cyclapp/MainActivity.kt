package com.example.cyclapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cyclapp.navigation.CyclAppApp
import com.example.cyclapp.ui.theme.CyclAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("UID", FirebaseAuth.getInstance().currentUser?.uid ?: "null")

        setContent {
            CyclAppTheme {
                CyclAppApp()
            }
        }
    }
}