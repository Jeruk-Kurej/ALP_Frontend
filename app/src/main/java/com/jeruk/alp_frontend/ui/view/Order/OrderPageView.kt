package com.jeruk.alp_frontend.ui.view.Order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jeruk.alp_frontend.ui.model.Product
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderPageView(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel()
) {
    // --- DATA ---
    val cartItems by productViewModel.cartItems.collectAsState()
    val products by productViewModel.products.collectAsState()

    val cartProductList = remember(cartItems, products) {
        products.filter { cartItems.containsKey(it.id) }
    }

    val subTotal = cartProductList.sumOf { product ->
        val qty = cartItems[product.id] ?: 0
        (product.price * qty).toDouble()
    }

    val tax = subTotal * 0.1
    val grandTotal = subTotal + tax

    // --- UI CONTENT (Tanpa Scaffold) ---
    // Gunakan Column langsung sebagai root
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // Background abu muda
    ) {

        if (cartProductList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Keranjang masih kosong", color = Color.Gray)
            }
        } else {
            // 1. LIST ITEM (Weight 1f agar mengisi ruang)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cartProductList) { product ->
                    val qty = cartItems[product.id] ?: 0
                    CartItemCard(
                        product = product,
                        quantity = qty,
                        onQuantityChange = { newQty -> productViewModel.updateCart(product, newQty) },
                        onDelete = { productViewModel.removeFromCart(product) }
                    )
                }
            }

            // 2. RINGKASAN PESANAN (Menempel di bawah)
            OrderSummarySection(
                subTotal = subTotal,
                tax = tax,
                grandTotal = grandTotal,
                onCheckout = { /* Logika Checkout */ }
            )
        }
    }
}

// --- KOMPONEN PENDUKUNG (Card & Summary) ---

@Composable
fun CartItemCard(
    product: Product,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val subTotalItem = (product.price * quantity).toDouble()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = product.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
                    Text("Subtotal", fontSize = 10.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text(text = formatRupiah(product.price.toDouble()), color = Color(0xFFA855F7), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(onClick = { onQuantityChange(quantity - 1) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Remove, null, tint = Color.Gray, modifier = Modifier.size(16.dp)) }
                            Text(text = quantity.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(onClick = { onQuantityChange(quantity + 1) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(16.dp)) }
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = formatRupiah(subTotalItem), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFEF2F2)).clickable { onDelete() },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummarySection(subTotal: Double, tax: Double, grandTotal: Double, onCheckout: () -> Unit) {
    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Ringkasan Pesanan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            SummaryRow(label = "Subtotal", amount = subTotal)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(label = "Pajak (10%)", amount = tax)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE5E7EB))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(formatRupiah(grandTotal), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFA855F7))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(colors = listOf(Color(0xFF8B5CF6), Color(0xFFC084FC))))
                    .clickable { onCheckout() },
                contentAlignment = Alignment.Center
            ) { Text("Lanjut ke Pembayaran", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }
    }
}

@Composable
fun SummaryRow(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(formatRupiah(amount), color = Color.Gray, fontSize = 14.sp)
    }
}

fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ").substringBefore(",")
}