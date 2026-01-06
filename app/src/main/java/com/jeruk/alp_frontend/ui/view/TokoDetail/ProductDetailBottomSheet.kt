package com.jeruk.alp_frontend.ui.view.Component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Warna Custom
val PurplePrimary = Color(0xFFBA68C8)
val PurpleText = Color(0xFF9C27B0)
val PurpleLightBg = Color(0xFFF3E5F5)
val BlueCategoryBg = Color(0xFFE3F2FD)
val BlueCategoryText = Color(0xFF1E88E5)
val GrayText = Color(0xFF757575)
val GrayBg = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailBottomSheet(
    onDismissRequest: () -> Unit,
    onAddToCart: (Int) -> Unit,
    productName: String,
    productPrice: Int,
    productDescription: String,
    productCategory: String,
    imageUrl: String // Parameter untuk URL Gambar
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var quantity by remember { mutableIntStateOf(1) }
    val totalPrice = productPrice * quantity

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // --- 1. GAMBAR & TOMBOL CLOSE ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(16.dp)
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = productName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEEEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = productName.take(1),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clickable { onDismissRequest() },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        modifier = Modifier.padding(8.dp),
                        tint = Color.Black
                    )
                }
            }

            // --- 2. INFORMASI PRODUK ---
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = productName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .background(BlueCategoryBg, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = productCategory, color = BlueCategoryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = productDescription, color = GrayText, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Rp ${formatRupiahSimple(productPrice)}", color = PurpleText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. JUMLAH ---
            Text(
                text = "Jumlah",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                QuantityIconBtn(icon = Icons.Default.Remove, onClick = { if (quantity > 1) quantity-- }, enabled = quantity > 1)
                Text(
                    text = quantity.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                QuantityIconBtn(icon = Icons.Default.Add, onClick = { quantity++ })
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. TOTAL & ADD TO CART ---
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .background(PurpleLightBg, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total Harga", fontWeight = FontWeight.Medium)
                    Text(
                        text = "Rp ${formatRupiahSimple(totalPrice)}",
                        color = PurpleText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Button(
                    onClick = { onAddToCart(quantity) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Icon(imageVector = Icons.Outlined.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Tambah ke Keranjang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Komponen Kecil Helper
@Composable
fun QuantityIconBtn(icon: ImageVector, onClick: () -> Unit, enabled: Boolean = true) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick),
        color = if (enabled) GrayBg else Color.LightGray.copy(alpha = 0.3f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = if (enabled) Color.Black else Color.Gray, modifier = Modifier.size(24.dp))
        }
    }
}

fun formatRupiahSimple(amount: Int): String {
    return String.format("%,d", amount).replace(',', '.')
}