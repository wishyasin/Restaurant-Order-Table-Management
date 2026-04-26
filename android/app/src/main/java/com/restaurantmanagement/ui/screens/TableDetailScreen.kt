package com.restaurantmanagement.ui.screens

import androidx.compose.animation.*
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
import com.restaurantmanagement.navigation.Screen
import com.restaurantmanagement.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDetailScreen(
    navController: NavController,
    tableId: Int,
    viewModel: OrderViewModel = viewModel()
) {
    LaunchedEffect(tableId) { viewModel.fetchActiveOrder(tableId) }

    val orderItems   = viewModel.orderItems
    val currentOrder by viewModel.currentOrder
    val isLoading    by viewModel.isLoading

    val totalAmount  = orderItems.sumOf { it.menuItem.price * it.quantity }
    var showPaymentDialog  by remember { mutableStateOf(false) }
    var showOrderSummary   by remember { mutableStateOf(false) }
    var orderNote          by remember { mutableStateOf("") }
    var showNoteField      by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Table $tableId", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            if (orderItems.isEmpty()) "Empty Table" else "${orderItems.sumOf { it.quantity }} items",
                            fontSize = 12.sp,
                            color    = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Note toggle button
                    IconButton(onClick = { showNoteField = !showNoteField }) {
                        Icon(
                            imageVector = if (orderNote.isNotBlank()) Icons.Default.Note else Icons.Default.NoteAdd,
                            contentDescription = "Note",
                            tint = if (orderNote.isNotBlank())
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    // Refresh
                    IconButton(onClick = { viewModel.fetchActiveOrder(tableId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    titleContentColor      = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick          = { navController.navigate(Screen.Menu.createRoute(tableId)) },
                containerColor   = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // ── Note field ────────────────────────────
            AnimatedVisibility(visible = showNoteField) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "📝 Order Note",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 13.sp,
                            color      = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value       = orderNote,
                            onValueChange = { orderNote = it },
                            placeholder = { Text("Special requests, allergies, etc...") },
                            modifier    = Modifier.fillMaxWidth(),
                            maxLines    = 3,
                            shape       = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // ── Content ───────────────────────────────
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (orderItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🪑", fontSize = 52.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No active orders", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text("Tap the + button to add items from the menu",
                            fontSize = 13.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign= TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(orderItems) { orderItem ->
                        OrderItemCard(
                            orderItem = orderItem,
                            onIncrease = {
                                val idx = orderItems.indexOf(orderItem)
                                orderItems[idx] = orderItem.copy(quantity = orderItem.quantity + 1)
                            },
                            onDecrease = {
                                val idx = orderItems.indexOf(orderItem)
                                if (orderItem.quantity > 1)
                                    orderItems[idx] = orderItem.copy(quantity = orderItem.quantity - 1)
                                else
                                    orderItems.removeAt(idx)
                            },
                            onDelete = { orderItems.remove(orderItem) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }

            // ── Bottom summary bar ────────────────────
            if (orderItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    elevation= CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp)
                    ) {
                        // Summary row
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Items", fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${orderItems.sumOf { it.quantity }} items",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Amount", fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "$${String.format("%.2f", totalAmount)}",
                                    fontSize   = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick  = { showOrderSummary = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null,
                                    modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Summary")
                            }
                            Button(
                                onClick = { showPaymentDialog = true },
                                modifier= Modifier.weight(2f),
                                colors  = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF388E3C)
                                )
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null,
                                    modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Checkout", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Order Summary Dialog ──────────────────────────
    if (showOrderSummary) {
        AlertDialog(
            onDismissRequest = { showOrderSummary = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Order Summary — Table $tableId", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    orderItems.forEach { item ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${item.quantity}x  ${item.menuItem.name}",
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "$${String.format("%.2f", item.menuItem.price * item.quantity)}",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    if (orderNote.isNotBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Note, contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(4.dp))
                            Text(orderNote, fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("TOTAL", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "$${String.format("%.2f", totalAmount)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 18.sp,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showOrderSummary = false }) { Text("Close") }
            }
        )
    }

    // ── Payment Dialog ────────────────────────────────
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payment, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Process Payment", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape  = RoundedCornerShape(12.dp),
                        color  = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            Arrangement.SpaceBetween,
                            Alignment.CenterVertically
                        ) {
                            Text("Total Amount", fontWeight = FontWeight.Medium)
                            Text(
                                "$${String.format("%.2f", totalAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize   = 20.sp,
                                color      = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text("Select payment method:", fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val orderId = viewModel.currentOrder.value?.id
                                if (orderId != null && orderId != 0) {
                                    viewModel.processPayment(orderId, tableId, "PAID_CASH") {
                                        showPaymentDialog = false
                                        navController.popBackStack()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💵", fontSize = 20.sp)
                                Text("Cash", fontSize = 12.sp)
                            }
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
                            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💳", fontSize = 20.sp)
                                Text("Card", fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ── Order Item Card ────────────────────────────────────

@Composable
fun OrderItemCard(
    orderItem: OrderItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete  : () -> Unit
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
            // Category emoji
            Text(
                text     = orderItem.menuItem.category.emoji(),
                fontSize = 28.sp,
                modifier = Modifier.width(40.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(orderItem.menuItem.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    "$${String.format("%.2f", orderItem.menuItem.price)} / unit",
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Subtotal: $${String.format("%.2f", orderItem.menuItem.price * orderItem.quantity)}",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color      = MaterialTheme.colorScheme.primary
                )
            }
            // Quantity controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease",
                        tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    "${orderItem.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.width(28.dp)
                )
                IconButton(onClick = onIncrease, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase",
                        tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}