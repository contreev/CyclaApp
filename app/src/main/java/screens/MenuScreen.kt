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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyclapp.R
import com.example.cyclapp.components.AppBottomBar
import com.example.cyclapp.components.CategoryCard

@Composable
fun MenuScreen(
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMissionsClick: () -> Unit,
    onNewsClick: () -> Unit,
    onWasteClick: () -> Unit,
    onMapClick: () -> Unit
) {
    val bgColor = Color(0xFFE9E9E9)
    val cardColor = Color(0xFFF8F8F8)
    val accentBrown = Color(0xFFB78B66)

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
            // Header con imagen curva y logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.reciclaje_fondo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(bottomStart = 150.dp, bottomEnd = 150.dp))
                )

                // Icono de Menú Hamburguesa
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint = Color.Black,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 20.dp, end = 20.dp)
                        .size(32.dp)
                )

                // Logo y Nombre
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CyclApp",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Categorias",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Grid de Categorías
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onWasteClick() }
                    ) {
                        CategoryCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Residuos",
                            imageRes = R.drawable.residuos,
                            arrowColor = accentBrown
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onMapClick() }
                    ) {
                        CategoryCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Puntos de reciclaje",
                            imageRes = R.drawable.puntos,
                            arrowColor = accentBrown
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNewsClick() }
                    ) {
                        CategoryCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Noticias",
                            imageRes = R.drawable.noticias,
                            arrowColor = accentBrown
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onMissionsClick() }
                    ) {
                        CategoryCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Misiones y Logros",
                            imageRes = R.drawable.misiones,
                            arrowColor = accentBrown
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sección Últimos Registros
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(35.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Ultimos Registros",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = accentBrown,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📄", fontSize = 24.sp) // Reemplazar con icono real
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "13:40 6/03/2026",
                                    color = accentBrown,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🧴", fontSize = 24.sp) // Reemplazar con icono real
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "13:40 6/03/2026",
                                    color = accentBrown,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🥛", fontSize = 24.sp) // Reemplazar con icono real
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "13:40 6/03/2026",
                                color = accentBrown,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cerrar sesión", color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        AppBottomBar(
            selected = "home",
            onHomeClick = { },
            onMissionsClick = onMissionsClick,
            onProfileClick = onProfileClick
        )
    }
}
