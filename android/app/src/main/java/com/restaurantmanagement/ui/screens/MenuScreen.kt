package com.restaurantmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.restaurantmanagement.data.model.MenuCategory
import com.restaurantmanagement.data.model.MenuItem
import com.restaurantmanagement.data.model.MockData
import com.restaurantmanagement.data.model.OrderItem
import com.restaurantmanagement.viewmodel.MenuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavController, tableId: Int, menuViewModel: MenuViewModel) {


    val menuItems = menuViewModel.menuItems
    var selectedCategory by remember { mutableStateOf<MenuCategory?>(null) }
    val selectedItems = remember { mutableStateListOf<OrderItem>() }

    val filteredItems = if (selectedCategory == null) {
        menuItems
    } else {
        menuItems.filter { it.category == selectedCategory }
    }

    val totalSelected = selectedItems.sumOf { it.quantity }
    val totalAmount = selectedItems.sumOf { it.menuItem.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Menu", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Table $tableId", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                }
                items(MenuCategory.entries) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.displayName()) }
                    )
                }
            }


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
                        onAdd = {
                            val index = selectedItems.indexOfFirst { it.menuItem.id == menuItem.id }
                            if (index >= 0) {
                                selectedItems[index] = selectedItems[index].copy(quantity = selectedItems[index].quantity + 1)
                            } else {
                                selectedItems.add(OrderItem(id = 0, menuItem = menuItem, quantity = 1, tableId = tableId))
                            }
                        },
                        onRemove = {
                            val index = selectedItems.indexOfFirst { it.menuItem.id == menuItem.id }
                            if (index >= 0) {
                                if (selectedItems[index].quantity > 1) {
                                    selectedItems[index] = selectedItems[index].copy(quantity = selectedItems[index].quantity - 1)
                                } else {
                                    selectedItems.removeAt(index)
                                }
                            }
                        }
                    )
                }
            }


            if (totalSelected > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "$totalSelected items selected", fontSize = 13.sp)
                            Text(text = "$${String.format("%.2f", totalAmount)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = {

                                menuViewModel.placeOrder(tableId, selectedItems) {
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
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
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = menuItem.category.emoji(),
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))


            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menuItem.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (menuItem.description.isNotEmpty()) {
                    Text(
                        text = menuItem.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$${String.format("%.2f", menuItem.price)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }


            Row(verticalAlignment = Alignment.CenterVertically) {
                if (quantity > 0) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.width(32.dp).height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "$quantity",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier.width(32.dp).height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

fun MenuCategory.displayName(): String = when (this) {
    MenuCategory.FOOD -> "Food"
    MenuCategory.DRINK -> "Drink"
    MenuCategory.DESSERT -> "Dessert"
    MenuCategory.SNACK -> "Snack"
}

fun MenuCategory.emoji(): String = when (this) {
    MenuCategory.FOOD -> "🍽️"
    MenuCategory.DRINK -> "☕"
    MenuCategory.DESSERT -> "🍰"
    MenuCategory.SNACK -> "🍿"
}