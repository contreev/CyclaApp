package com.example.cyclapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class WasteType(
    val title: String,
    val shortDesc: String,
    val detailedDesc: String,
    val color: Color,
    val icon: String,
    val binColorName: String,
    val examples: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteScreen(onBackClick: () -> Unit) {
    val wasteTypes = listOf(
        WasteType(
            "Plásticos",
            "Envases de comida, botellas, bolsas.",
            "Los plásticos deben estar limpios y secos. Incluye botellas de bebidas, envases de detergentes y bolsas plásticas que no tengan restos de comida.",
            Color(0xFFFFFFFF),
            "🧴",
            "Blanco",
            "Botellas de PET, envases de yogurt, bolsas de supermercado."
        ),
        WasteType(
            "Papel y Cartón",
            "Cajas, periódicos, revistas.",
            "Todo el papel y cartón debe estar seco y libre de grasa o restos de alimentos para poder ser reciclado correctamente.",
            Color(0xFFFFFFFF), // En muchos países el papel limpio va en blanco
            "📦",
            "Blanco",
            "Cajas de cereal, cuadernos viejos, folletos, periódicos."
        ),
        WasteType(
            "Vidrio",
            "Botellas y frascos de vidrio.",
            "El vidrio es 100% reciclable. Asegúrate de retirar tapas metálicas o plásticas antes de depositarlo.",
            Color(0xFFFFFFFF), // También suele ir en blanco si es aprovechable
            "🍾",
            "Blanco",
            "Botellas de gaseosa, frascos de mermelada, envases de perfume."
        ),
        WasteType(
            "Residuos Orgánicos",
            "Restos de comida y desechos agrícolas.",
            "Estos residuos pueden convertirse en abono (compost). No incluyas servilletas ni papeles sucios aquí.",
            Color(0xFF1B5E20),
            "🍎",
            "Verde",
            "Cáscaras de fruta, restos de verdura, restos de café, pasto cortado."
        ),
        WasteType(
            "Metales",
            "Latas de refresco y conservas.",
            "Las latas de aluminio y acero son altamente valoradas en el reciclaje. Enjuágalas un poco antes de botarlas.",
            Color(0xFFFFFFFF),
            "🥫",
            "Blanco",
            "Latas de atún, latas de refresco, tapas de frascos."
        ),
        WasteType(
            "No Aprovechables",
            "Residuos que no se pueden reciclar.",
            "Son elementos que deben ir a disposición final porque están contaminados o no tienen mercado de reciclaje.",
            Color(0xFF000000),
            "🗑️",
            "Negro",
            "Papel higiénico, servilletas usadas, papeles metalizados, colillas de cigarrillo."
        )
    )

    var selectedWaste by remember { mutableStateOf<WasteType?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guía de Residuos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFB8CB6A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE9E9E9)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(wasteTypes) { waste ->
                    WasteItemCard(waste) {
                        selectedWaste = waste
                        showSheet = true
                    }
                }
            }

            if (showSheet && selectedWaste != null) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White
                ) {
                    WasteDetailContent(selectedWaste!!)
                }
            }
        }
    }
}

@Composable
fun WasteItemCard(waste: WasteType, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        if (waste.color == Color.White) Color(0xFFF0F0F0) else waste.color,
                        RoundedCornerShape(12.dp)
                    )
                    .then(if (waste.color == Color.White) Modifier.background(Color.White, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(Color.White) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (waste.color == Color.White) {
                    // Borde sutil para el color blanco
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = waste.icon, fontSize = 30.sp)
                        }
                    }
                } else {
                    Text(text = waste.icon, fontSize = 30.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = waste.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = waste.shortDesc,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Ver más",
                tint = Color(0xFFB8CB6A)
            )
        }
    }
}

@Composable
fun WasteDetailContent(waste: WasteType) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = waste.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Representación visual de la caneca/bolsa
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp, 120.dp)
                    .background(waste.color, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                    .then(if(waste.color == Color.White) Modifier.background(Color.White, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 20.dp, bottomEnd = 20.dp)).clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 20.dp, bottomEnd = 20.dp)).background(Color.White) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (waste.color == Color.White) {
                     Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Gray)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = waste.icon, fontSize = 40.sp)
                        }
                    }
                } else {
                    Text(text = waste.icon, fontSize = 40.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Usar bolsa/caneca COLOR ${waste.binColorName.uppercase()}",
                fontWeight = FontWeight.ExtraBold,
                color = if (waste.color == Color.White) Color.DarkGray else waste.color,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "¿Qué es?",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Text(
            text = waste.detailedDesc,
            fontSize = 15.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Ejemplos:",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Text(
            text = waste.examples,
            fontSize = 15.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}