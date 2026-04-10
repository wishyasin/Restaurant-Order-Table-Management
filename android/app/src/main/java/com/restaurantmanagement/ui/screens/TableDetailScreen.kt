package com.restaurantmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.restaurantmanagement.data.model.OrderItem
import com.restaurantmanagement.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDetailScreen(
    navController: NavController,
    tableId: Int,
    viewModel: OrderViewModel = viewModel()
) {

    LaunchedEffect(tableId) {
        viewModel.fetchActiveOrder(tableId)
    }

    val orderItems = viewModel.orderItems
    val currentOrder by viewModel.currentOrder
    val isLoading by viewModel.isLoading


    val totalAmount = orderItems.sumOf { it.menuItem.price * it.quantity }
    var showPaymentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {

                        Text(
                            text = "Table $tableId",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (orderItems.isEmpty()) "Empty Table" else "Active Order",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(com.restaurantmanagement.navigation.Screen.Menu.createRoute(tableId))
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item", tint = Color.White)
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (orderItems.isEmpty()) {

                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🪑", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "No orders yet", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text(text = "Tap + to add items from menu", fontSize = 14.sp)
                    }
                }
            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(orderItems) { orderItem ->
                        OrderItemCard(
                            orderItem = orderItem,
                            onIncrease = {
                                val index = orderItems.indexOf(orderItem)
                                orderItems[index] = orderItem.copy(quantity = orderItem.quantity + 1)
                            },
                            onDecrease = {
                                val index = orderItems.indexOf(orderItem)
                                if (orderItem.quantity > 1) {
                                    orderItems[index] = orderItem.copy(quantity = orderItem.quantity - 1)
                                } else {
                                    orderItems.removeAt(index)
                                }
                            },
                            onDelete = { orderItems.remove(orderItem) }
                        )
                    }
                }
            }


            if (orderItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 32.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Items: ${orderItems.sumOf { it.quantity }}", fontSize = 14.sp)
                            Text(text = "Total: $${String.format("%.2f", totalAmount)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { orderItems.clear() }, modifier = Modifier.weight(1f)) {
                                Text("Clear All")
                            }
                            Button(
                                onClick = { showPaymentDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("Payment", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }


    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Payment", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Total: $${String.format("%.2f", totalAmount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        Button(
                            onClick = {

                                val orderId = viewModel.currentOrder.value?.id

                                if (orderId != null && orderId != 0) {
                                    viewModel.processPayment(
                                        orderId = orderId,
                                        tableId = tableId,
                                        status = "PAID_CASH"
                                    ) {
                                        navController.popBackStack()
                                    }
                                } else {

                                    println("ERROR!")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("💵 Cash")
                        }

                        Button(
                            onClick = {
                                currentOrder?.let { order ->
                                    viewModel.processPayment(order.id, tableId, "PAID_CARD") {
                                        showPaymentDialog = false
                                        navController.popBackStack()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Text("💳 Card")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showPaymentDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun OrderItemCard(
    orderItem: OrderItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = orderItem.menuItem.name, fontWeight = FontWeight.Medium)
                Text(text = "$${String.format("%.2f", orderItem.menuItem.price)}", fontSize = 13.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease) { Icon(Icons.Default.Remove, contentDescription = null) }
                Text(text = "${orderItem.quantity}", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                IconButton(onClick = onIncrease) { Icon(Icons.Default.Add, contentDescription = null) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
            }
        }
    }
}