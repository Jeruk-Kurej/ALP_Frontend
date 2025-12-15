package com.jeruk.alp_frontend.ui.view.Analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AnalysisDetailView(navController: NavController) {
    // Tampilan detail analisis sederhana
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Halaman Detail Analisis",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Data statistik mendalam akan tampil di sini.",
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA))
        ) {
            Text("Kembali ke Dashboard")
        }
    }
}