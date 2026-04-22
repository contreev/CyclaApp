package com.example.cyclapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyclapp.R
import com.example.cyclapp.components.BottomNavItem
import com.example.cyclapp.components.CategoryCard

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
                    imageVector = Icons.Outlined.PhotoCamera,
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