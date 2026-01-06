package com.jeruk.alp_frontend.ui.view.Product

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.jeruk.alp_frontend.ui.model.Category
import com.jeruk.alp_frontend.ui.model.Product
import com.jeruk.alp_frontend.ui.route.AppView
import com.jeruk.alp_frontend.ui.viewmodel.CategoryViewModel
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel

// --- PALETTE WARNA PROFESSIONAL (Clean & Elegant) ---
// Indigo: Warna utama yang melambangkan kepercayaan & teknologi
val BrandPrimary = Color(0xFF4F46E5)    // Indigo 600
val BrandSoft = Color(0xFFEEF2FF)       // Indigo 50 (Background)

// Emerald: Warna khusus Harga (Money/Success)
val PriceColor = Color(0xFF059669)      // Emerald 600 (Jelas & Elegan)

// Slate: Warna Netral untuk Teks
val TextDark = Color(0xFF111827)        // Gray 900 (Judul)
val TextGray = Color(0xFF6B7280)        // Gray 500 (Subtext)

// Kategori Chip
val CategoryText = Color(0xFF4338CA)    // Indigo 700 (Teks Kategori)
val CategoryBg = Color(0xFFE0E7FF)      // Indigo 100 (Background Kategori)

val RedDanger = Color(0xFFEF4444)       // Merah (Delete)
val RedSoft = Color(0xFFFEF2F2)         // Merah Muda (Background Delete)

@Composable
fun ProductAdminView(
    navController: NavController,
    token: String = "",
    productViewModel: ProductViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val products by productViewModel.products.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, selectedTab) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (selectedTab == 0) {
                    productViewModel.getAllProducts(token)
                } else {
                    categoryViewModel.getAllCategories(token)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)) // Background abu-abu sangat muda (Clean)
    ) {
        // --- TAB SECTION ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp) // Shadow tipis
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    text = "Produk",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Kategori",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Content based on selected tab
        when (selectedTab) {
            0 -> ProductListContent(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                navController = navController,
                token = token,
                products = products,
                isLoading = isLoading,
                productViewModel = productViewModel
            )

            1 -> CategoryListContent(
                navController = navController,
                token = token,
                categoryViewModel = categoryViewModel
            )
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (isSelected) Color.White else TextGray
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected) BrandPrimary else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ProductListContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    navController: NavController,
    token: String,
    products: List<Product>,
    isLoading: Boolean,
    productViewModel: ProductViewModel
) {
    val context = LocalContext.current

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daftar Produk",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "${products.size} item tersedia",
                    fontSize = 14.sp,
                    color = TextGray
                )
            }

            // Tambah Button (Modern Gradient Blue-Teal)
            Button(
                onClick = { navController.navigate(AppView.AddProduct.name) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(42.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandPrimary)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Tambah",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar (Clean Style)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            placeholder = {
                Text("Cari nama atau deskripsi...", color = TextGray, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = BrandPrimary,
                unfocusedBorderColor = Color(0xFFE5E7EB), // Abu-abu sangat muda
                cursorColor = BrandPrimary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Content
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        } else if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tidak ada produk ditemukan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextGray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onEdit = {
                            navController.navigate("${AppView.UpdateProduct.name}/${product.id}")
                        },
                        onDelete = {
                            productViewModel.deleteProduct(token, product.id)
                            Toast.makeText(context, "Dihapus", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

// 🔥 REDESIGN: Professional Card (Elegant & Clean)
@Composable
fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat style
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6)) // Border tipis halus
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Image Thumbnail
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(90.dp), // Sedikit lebih compact
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF9FAFB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.name.firstOrNull()?.toString() ?: "?",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD1D5DB)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Info Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Kategori Chip (Clean & Professional)
                    Surface(
                        color = CategoryBg, // Soft Indigo Background
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = product.categoryName.uppercase(),
                            color = CategoryText, // Deep Indigo Text
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Nama Produk
                    Text(
                        text = product.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }

                // Harga & Toko
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Harga (Emerald Green - Money Color)
                    Text(
                        text = "Rp ${product.price}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PriceColor
                    )
                }
            }

            // 3. Actions (Vertical, Minimalist)
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                ActionIconButton(
                    icon = Icons.Default.Edit,
                    color = BrandPrimary,
                    backgroundColor = BrandSoft,
                    onClick = onEdit
                )

                ActionIconButton(
                    icon = Icons.Default.Delete,
                    color = RedDanger,
                    backgroundColor = RedSoft,
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
fun CategoryListContent(
    navController: NavController,
    token: String,
    categoryViewModel: CategoryViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val categories by categoryViewModel.categories.collectAsState()
    val isLoading by categoryViewModel.isLoading.collectAsState()
    val errorMessage by categoryViewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daftar Kategori",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "${categories.size} kategori",
                    fontSize = 14.sp,
                    color = TextGray
                )
            }
            Button(
                onClick = { navController.navigate(AppView.AddCategory.name) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Tambah", color = Color.White, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari kategori...", color = TextGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = BrandPrimary,
                unfocusedBorderColor = Color(0xFFE5E7EB)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        } else if (filteredCategories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Kategori tidak ditemukan", color = TextGray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredCategories, key = { it.id }) { category ->
                    CategoryCard(
                        category = category,
                        onEdit = {
                            navController.navigate("${AppView.UpdateCategory.name}/${category.id}")
                        },
                        onDelete = {
                            categoryViewModel.deleteCategory(token, category.id)
                        }
                    )
                }
            }
        }
    }
}

// 🔥 REDESIGN: Category Card - Minimalist
@Composable
fun CategoryCard(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder (Blue Theme)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BrandSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name.firstOrNull()?.toString()?.uppercase() ?: "C",
                    fontWeight = FontWeight.Bold,
                    color = BrandPrimary,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = category.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = TextDark,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionIconButton(
                    icon = Icons.Default.Edit,
                    color = BrandPrimary,
                    backgroundColor = BrandSoft,
                    onClick = onEdit,
                    size = 32.dp,
                    iconSize = 16.dp
                )
                ActionIconButton(
                    icon = Icons.Default.Delete,
                    color = RedDanger,
                    backgroundColor = RedSoft,
                    onClick = onDelete,
                    size = 32.dp,
                    iconSize = 16.dp
                )
            }
        }
    }
}

@Composable
fun ActionIconButton(
    icon: ImageVector,
    color: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp)) // Sedikit lebih kotak (Squircle) biar lebih modern
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
    }
}