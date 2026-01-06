package com.jeruk.alp_frontend.ui.view.Product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jeruk.alp_frontend.ui.model.Category
import com.jeruk.alp_frontend.ui.model.Product
import com.jeruk.alp_frontend.ui.viewmodel.CategoryViewModel
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale

// 🔥 PENTING: Import komponen BottomSheet yang baru dibuat di File 1
import com.jeruk.alp_frontend.ui.view.Component.ProductDetailBottomSheet

@Composable
fun ProductMenuView(
    navController: NavController,
    token: String,
    tokoId: Int,
    tokoName: String,
    productViewModel: ProductViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    // --- STATE ---
    val products by productViewModel.products.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()
    val isLoadingProducts by productViewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    // 🔥 STATE UNTUK POPUP 🔥
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(Unit) {
        categoryViewModel.getAllCategories(token)
        productViewModel.getAllProducts(token)
    }

    val filteredProducts = remember(products, searchQuery, selectedCategoryId, tokoId) {
        products.filter { product ->
            val belongToStore = product.tokoIds.contains(tokoId)
            val matchSearch = product.name.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            belongToStore && matchSearch && matchCategory
        }
    }

    // --- UI UTAMA ---
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            SearchBarComponent(query = searchQuery, onQueryChange = { searchQuery = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Kategori
            CategoryFilterSection(
                categories = categories,
                selectedId = selectedCategoryId,
                onSelect = { selectedCategoryId = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grid Produk
            if (isLoadingProducts) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFA855F7))
                }
            } else if (filteredProducts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Menu tidak ditemukan", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredProducts) { product ->
                        val catName = categories.find { it.id == product.categoryId }?.name ?: "Menu"

                        ProductGridItem(product = product, categoryName = catName) {
                            // 🔥 TRIGGER POPUP 🔥
                            selectedProduct = product
                            showBottomSheet = true
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // 🔥 PANGGIL POPUP (BOTTOM SHEET) 🔥
        if (showBottomSheet && selectedProduct != null) {
            val categoryName = categories.find { it.id == selectedProduct!!.categoryId }?.name ?: "Menu"

            ProductDetailBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    selectedProduct = null
                },
                onAddToCart = { quantity ->
                    // LOGIKA ADD TO CART DISINI
                    android.util.Log.d("Cart", "Add ${selectedProduct!!.name} qty: $quantity")
                    showBottomSheet = false
                },
                productName = selectedProduct!!.name,
                productPrice = selectedProduct!!.price,
                productDescription = selectedProduct!!.description,
                productCategory = categoryName,
                imageUrl = selectedProduct!!.imageUrl // Kirim URL Gambar
            )
        }
    }
}

// --- KOMPONEN PENDUKUNG (Search, Chip, Card) ---
@Composable
fun SearchBarComponent(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Cari menu...", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFF3E8FF),
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )
}

@Composable
fun CategoryFilterSection(categories: List<Category>, selectedId: Int?, onSelect: (Int?) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item { FilterChipItem(text = "Semua", isSelected = selectedId == null, onClick = { onSelect(null) }) }
        items(categories) { category -> FilterChipItem(text = category.name, isSelected = selectedId == category.id, onClick = { onSelect(category.id) }) }
    }
}

@Composable
fun FilterChipItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF9F7AEA), Color(0xFFC084FC))) else Brush.linearGradient(listOf(Color.White, Color.White))
    val textColor = if (isSelected) Color.White else Color.Gray
    val borderColor = if (isSelected) Color.Transparent else Color(0xFFE5E7EB)

    Box(
        modifier = Modifier.clip(RoundedCornerShape(50)).background(backgroundColor).border(1.dp, borderColor, RoundedCornerShape(50)).clickable { onClick() }.padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) }
}

@Composable
fun ProductGridItem(product: Product, categoryName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(model = product.imageUrl, contentDescription = product.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
                        Text(text = product.name.take(1), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    }
                }
                Surface(modifier = Modifier.padding(8.dp), shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.6f)) {
                    Text(text = categoryName, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = product.description, color = Color.Gray, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp, modifier = Modifier.height(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(product.price), fontWeight = FontWeight.Bold, color = Color(0xFFA855F7), fontSize = 14.sp)
            }
        }
    }
}