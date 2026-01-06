package com.jeruk.alp_frontend.ui.view.Order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jeruk.alp_frontend.data.service.OrderItemRequest
import com.jeruk.alp_frontend.ui.route.AppView
import com.jeruk.alp_frontend.ui.viewmodel.OrderViewModel
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel

@Composable
fun QRISPageView(
    navController: NavController,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    token: String,
    tokoId: Int
) {
    val cartItems by productViewModel.cartItems.collectAsState()
    val products by productViewModel.products.collectAsState()

    val grandTotal = remember(cartItems, products) {
        val subTotal = products.filter { cartItems.containsKey(it.id) }.sumOf {
            (it.price * (cartItems[it.id] ?: 0)).toDouble()
        }
        subTotal + (subTotal * 0.1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(20.dp)
    ) {
        // 1. CARD TOTAL (Gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF60A5FA), Color(0xFFC084FC))
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text("Total Pembayaran", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatRupiah(grandTotal),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. QR SECTION CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Scan QR Code", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(24.dp))

                // QR Placeholder (Kotak abu-abu)
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Placeholder Icon QR (Bisa diganti image QR asli nanti)
                    Text("QR CODE HERE", color = Color.LightGray, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Scan QR code dengan aplikasi pembayaran Anda",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(32.dp))

                // LaunchedEffect untuk observe isSuccess
                val isSuccess by orderViewModel.isSuccess.collectAsState()
                val isLoading by orderViewModel.isLoading.collectAsState()
                val errorMessage by orderViewModel.errorMessage.collectAsState()

                LaunchedEffect(isSuccess) {
                    if (isSuccess) {
                        productViewModel.clearCart()
                        orderViewModel.resetSuccess()
                        navController.navigate(AppView.SuccessPage.name) {
                            popUpTo(AppView.ProductMenu.name) { inclusive = false }
                        }
                    }
                }

                // Show error if any
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Tombol Pembayaran Selesai
                Button(
                    onClick = {
                        val orderItems =
                            products.filter { cartItems.containsKey(it.id) }.map { product ->
                                OrderItemRequest(
                                    productId = product.id,
                                    amount = (cartItems[product.id] ?: 0)
                                )
                            }
                        orderViewModel.createOrder(
                            token = token,
                            customerName = "Walk-in Customer",
                            paymentId = 1, tokoId = tokoId,
                            orderItems = orderItems
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF60A5FA),
                                        Color(0xFFC084FC)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                "Pembayaran Selesai",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}