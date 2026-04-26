package com.restaurantmanagement.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.restaurantmanagement.data.model.MenuCategory
import com.restaurantmanagement.data.model.MenuItem
import com.restaurantmanagement.data.model.OrderItem
import com.restaurantmanagement.viewmodel.MenuViewModel
import com.restaurantmanagement.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    navController : NavController,
    tableId       : Int,
    menuViewModel : MenuViewModel,
    orderViewModel: OrderViewModel = viewModel()
) {
    LaunchedEffect(tableId) { orderViewModel.fetchActiveOrder(tableId) }

    val menuItems        = menuViewModel.menuItems
    var selectedCategory by remember { mutableStateOf<MenuCategory?>(null) }
    val selectedItems    = remember { mutableStateListOf<OrderItem>() }
    var searchQuery      by remember { mutableStateOf("") }
    var showSearch       by remember { mutableStateOf(false) }

    val filteredItems = remember(menuItems, selectedCategory, searchQuery) {
        menuItems
            .filter { selectedCategory == null || it.category == selectedCategory }
            .filter { searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) }
    }

    val totalSelected = selectedItems.sumOf { it.quantity }
    val totalAmount   = selectedItems.sumOf { it.menuItem.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        OutlinedTextField(
                            value       = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search items...") },
                            singleLine  = true,
                            modifier    = Modifier.fillMaxWidth().padding(end = 8.dp),
                            colors      = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = MaterialTheme.colorScheme.onPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                focusedTextColor     = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor   = MaterialTheme.colorScheme.onPrimary,
                                cursorColor          = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    } else {
                        Column {
                            Text("Menu", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Table $tableId", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) searchQuery = ""
                    }) {
                        Icon(
                            if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    titleContentColor      = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Category filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick  = { selectedCategory = null },
                        label    = { Text("All") }
                    )
                }
                items(MenuCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick  = { selectedCategory = cat },
                        label    = { Text("${cat.emoji()} ${cat.displayName()}") }
                    )
                }
            }

            // Results count
            if (searchQuery.isNotBlank()) {
                Text(
                    "${filteredItems.size} results found",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (filteredItems.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 36.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No items found", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems) { menuItem ->
                        val existingItem = selectedItems.find { it.menuItem.id == menuItem.id }
                        MenuItemCard(
                            menuItem = menuItem,
                            quantity = existingItem?.quantity ?: 0,
                            onAdd    = {
                                val idx = selectedItems.indexOfFirst { it.menuItem.id == menuItem.id }
                                if (idx >= 0) selectedItems[idx] = selectedItems[idx].copy(quantity = selectedItems[idx].quantity + 1)
                                else selectedItems.add(OrderItem(0, menuItem, 1, tableId))
                            },
                            onRemove = {
                                val idx = selectedItems.indexOfFirst { it.menuItem.id == menuItem.id }
                                if (idx >= 0) {
                                    if (selectedItems[idx].quantity > 1)
                                        selectedItems[idx] = selectedItems[idx].copy(quantity = selectedItems[idx].quantity - 1)
                                    else
                                        selectedItems.removeAt(idx)
                                }
                            }
                        )
                    }
                }
            }

            // Bottom confirm bar
            AnimatedVisibility(visible = totalSelected > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    elevation= CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$totalSelected items selected", fontSize = 13.sp)
                            Text("$${String.format("%.2f", totalAmount)}",
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = {
                                menuViewModel.placeOrder(tableId, selectedItems, orderViewModel.currentOrder.value?.id) {
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Confirm Order")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    quantity: Int,
    onAdd   : () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Text(menuItem.category.emoji(), fontSize = 28.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(menuItem.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (menuItem.description.isNotEmpty()) {
                    Text(menuItem.description, fontSize = 12.sp,
                        color   = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines= 1, overflow = TextOverflow.Ellipsis)
                }
                Text("$${String.format("%.2f", menuItem.price)}",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (quantity > 0) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("$quantity", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center)
                }
                IconButton(onClick = onAdd, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

fun MenuCategory.displayName(): String = when (this) {
    MenuCategory.FOOD    -> "Main Course"
    MenuCategory.DRINK   -> "Beverage"
    MenuCategory.DESSERT -> "Dessert"
    MenuCategory.SNACK   -> "Snack"
}

fun MenuCategory.emoji(): String = when (this) {
    MenuCategory.FOOD    -> "🍽️"
    MenuCategory.DRINK   -> "☕"
    MenuCategory.DESSERT -> "🍰"
    MenuCategory.SNACK   -> "🍿"
}