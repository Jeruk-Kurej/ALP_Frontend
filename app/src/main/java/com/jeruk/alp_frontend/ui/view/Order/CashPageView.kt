package com.jeruk.alp_frontend.ui.view.Order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jeruk.alp_frontend.ui.route.AppView
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashPageView(
    navController: NavController,
    productViewModel: ProductViewModel
) {
    val cartItems by productViewModel.cartItems.collectAsState()
    val products by productViewModel.products.collectAsState()

    // 1. Hitung Total yang harus dibayar
    val grandTotal = remember(cartItems, products) {
        val subTotal = products.filter { cartItems.containsKey(it.id) }.sumOf {
            (it.price * (cartItems[it.id] ?: 0)).toDouble()
        }
        subTotal + (subTotal * 0.1)
    }

    // 2. State untuk Input Uang
    var cashAmountInput by remember { mutableStateOf("") }
    val cashAmount = cashAmountInput.toDoubleOrNull() ?: 0.0
    val change = cashAmount - grandTotal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(20.dp)
    ) {
        // Header info total
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF60A5FA), Color(0xFFC084FC))))
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text("Total Pembayaran", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Text(formatRupiah(grandTotal), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Kembali
        Text(
            text = "← Kembali pilih metode",
            color = Color(0xFFC084FC),
            fontSize = 14.sp,
            modifier = Modifier.clickable { navController.popBackStack() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Area Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Pembayaran Tunai", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Jumlah Uang Diterima", fontSize = 14.sp, color = Color.Gray)
                OutlinedTextField(
                    value = cashAmountInput,
                    onValueChange = { if (it.all { char -> char.isDigit() }) cashAmountInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC084FC),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Logika Tampilan Kembalian / Kurang
                if (cashAmount > 0) {
                    val statusColor = if (change >= 0) Color(0xFF22C55E) else Color(0xFFEF4444)
                    val statusLabel = if (change >= 0) "Kembalian" else "Kurang"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.1f))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(statusLabel, color = statusColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(formatRupiah(kotlin.math.abs(change)), color = statusColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Tombol Selesaikan
        Button(
            onClick = {
                navController.navigate(AppView.SuccessPage.name)
            },
            enabled = cashAmount >= grandTotal,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC084FC),
                disabledContainerColor = Color(0xFFE5E7EB)
            )
        ) {
            Text("Selesaikan Pembayaran", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}