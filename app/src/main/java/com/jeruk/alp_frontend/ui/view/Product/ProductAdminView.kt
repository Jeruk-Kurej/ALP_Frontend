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
import com.jeruk.alp_frontend.ui.model.Category
import com.jeruk.alp_frontend.ui.model.Product
import com.jeruk.alp_frontend.ui.route.AppView
import com.jeruk.alp_frontend.ui.viewmodel.CategoryViewModel
import com.jeruk.alp_frontend.ui.viewmodel.ProductViewModel

// --- PALETTE WARNA PROFESSIONAL ---
val BrandPrimary = Color(0xFF4F46E5)    // Indigo 600
val BrandSoft = Color(0xFFEEF2FF)       // Indigo 50
val PriceColor = Color(0xFF059669)      // Emerald 600
val TextDark = Color(0xFF111827)        // Gray 900
val TextGray = Color(0xFF6B7280)        // Gray 500
val CategoryText = Color(0xFF4338CA)    // Indigo 700
val CategoryBg = Color(0xFFE0E7FF)      // Indigo 100
val RedDanger = Color(0xFFEF4444)       // Merah
val RedSoft = Color(0xFFFEF2F2)         // Merah Muda

// 🔥 WARNA GRADIENT BARU (Dari TokoAdminView)
val GradientStart = Color(0xFF6B9FFF)
val GradientEnd = Color(0xFFBA68C8)

@Composable
fun ProductAdminView(
    navController: NavController,
    token: String = "",
    productViewModel: ProductViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) } // Menggunakan mutableIntStateOf agar lebih optimal
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
            .background(Color(0xFFF3F4F6))
    ) {
        // --- TAB SECTION ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
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
                    // Update Tab Aktif juga mengikuti nuansa Gradient (opsional, tapi lebih serasi)
                    if (isSelected) Brush.horizontalGradient(listOf(GradientStart, GradientEnd))
                    else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
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

// --- KOMPONEN TOMBOL TAMBAH GRADIENT (Reusable) ---
@Composable
fun GradientAddButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Add,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
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

            // 🔥 MENGGUNAKAN TOMBOL GRADIENT BARU
            GradientAddButton(
                text = "Tambah",
                onClick = { navController.navigate(AppView.AddProduct.name) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Cari nama atau deskripsi...", color = TextGray, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = GradientStart, // Update border focus ke warna tema
                unfocusedBorderColor = Color(0xFFE5E7EB),
                cursorColor = GradientStart
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Content
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GradientEnd)
            }
        } else if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada produk ditemukan",
                    fontSize = 16.sp,
                    color = TextGray
                )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Thumbnail
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(90.dp),
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

            // Info Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = CategoryBg,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = product.categoryName.uppercase(),
                            color = CategoryText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

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

                Text(
                    text = "Rp ${product.price}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PriceColor
                )
            }

            // Actions
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                ActionIconButton(
                    icon = Icons.Default.Edit,
                    color = GradientStart, // Ubah ke warna tema
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

            // 🔥 MENGGUNAKAN TOMBOL GRADIENT BARU (Konsisten)
            GradientAddButton(
                text = "Tambah",
                onClick = { navController.navigate(AppView.AddCategory.name) }
            )
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
                focusedBorderColor = GradientStart,
                unfocusedBorderColor = Color(0xFFE5E7EB)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GradientEnd)
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
            // Icon Placeholder
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
                    color = GradientStart, // Ubah ke warna tema
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
                    color = GradientStart,
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
            .clip(RoundedCornerShape(8.dp))
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