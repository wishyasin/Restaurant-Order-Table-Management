package com.restaurantmanagement.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.restaurantmanagement.data.model.MenuCategory
import com.restaurantmanagement.data.model.MenuItem
import com.restaurantmanagement.data.model.MockData
import com.restaurantmanagement.data.model.Table
import com.restaurantmanagement.data.model.TableStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(navController: NavController) {

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Menu Items", "Tables")

    // Menu item dialog state
    var showMenuDialog by remember { mutableStateOf(false) }
    var editingMenuItem by remember { mutableStateOf<MenuItem?>(null) }

    // Table dialog state
    var showTableDialog by remember { mutableStateOf(false) }
    var editingTable by remember { mutableStateOf<Table?>(null) }

    // Delete confirmation state
    var showDeleteMenuDialog by remember { mutableStateOf(false) }
    var deletingMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var showDeleteTableDialog by remember { mutableStateOf(false) }
    var deletingTable by remember { mutableStateOf<Table?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Admin Panel",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
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
                    if (selectedTab == 0) {
                        editingMenuItem = null
                        showMenuDialog = true
                    } else {
                        editingTable = null
                        showTableDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> MenuItemsTab(
                    onEdit = {
                        editingMenuItem = it
                        showMenuDialog = true
                    },
                    onDelete = {
                        deletingMenuItem = it
                        showDeleteMenuDialog = true
                    }
                )
                1 -> TablesTab(
                    onEdit = {
                        editingTable = it
                        showTableDialog = true
                    },
                    onDelete = {
                        deletingTable = it
                        showDeleteTableDialog = true
                    }
                )
            }
        }
    }

    // Menu Item Add/Edit Dialog
    if (showMenuDialog) {
        MenuItemDialog(
            menuItem = editingMenuItem,
            onDismiss = { showMenuDialog = false },
            onConfirm = { name, price, category, description ->
                if (editingMenuItem != null) {
                    val index = MockData.menuItems.indexOfFirst { it.id == editingMenuItem!!.id }
                    if (index >= 0) {
                        MockData.menuItems[index] = editingMenuItem!!.copy(
                            name = name,
                            price = price,
                            category = category,
                            description = description
                        )
                    }
                } else {
                    MockData.menuItems.add(
                        MenuItem(
                            id = MockData.menuItems.size + 1,
                            name = name,
                            price = price,
                            category = category,
                            description = description
                        )
                    )
                }
                showMenuDialog = false
            }
        )
    }

    // Table Add/Edit Dialog
    if (showTableDialog) {
        TableDialog(
            table = editingTable,
            onDismiss = { showTableDialog = false },
            onConfirm = { number, capacity ->
                if (editingTable != null) {
                    val index = MockData.tables.indexOfFirst { it.id == editingTable!!.id }
                    if (index >= 0) {
                        MockData.tables[index] = editingTable!!.copy(
                            number = number,
                            capacity = capacity
                        )
                    }
                } else {
                    MockData.tables.add(
                        Table(
                            id = MockData.tables.size + 1,
                            number = number,
                            capacity = capacity,
                            status = TableStatus.EMPTY
                        )
                    )
                }
                showTableDialog = false
            }
        )
    }

    // Delete Menu Item Confirmation
    if (showDeleteMenuDialog && deletingMenuItem != null) {
        AlertDialog(
            onDismissRequest = { showDeleteMenuDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete \"${deletingMenuItem!!.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        MockData.menuItems.removeIf { it.id == deletingMenuItem!!.id }
                        showDeleteMenuDialog = false
                    }
                ) {
                    Text("Delete", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteMenuDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Table Confirmation
    if (showDeleteTableDialog && deletingTable != null) {
        AlertDialog(
            onDismissRequest = { showDeleteTableDialog = false },
            title = { Text("Delete Table") },
            text = { Text("Are you sure you want to delete Table ${deletingTable!!.number}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        MockData.tables.removeIf { it.id == deletingTable!!.id }
                        showDeleteTableDialog = false
                    }
                ) {
                    Text("Delete", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTableDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MenuItemsTab(
    onEdit: (MenuItem) -> Unit,
    onDelete: (MenuItem) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(MockData.menuItems) { menuItem ->
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
                    Text(
                        text = menuItem.category.emoji(),
                        fontSize = 24.sp,
                        modifier = Modifier.width(40.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = menuItem.name,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = menuItem.category.displayName(),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.2f", menuItem.price)}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = { onEdit(menuItem) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onDelete(menuItem) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TablesTab(
    onEdit: (Table) -> Unit,
    onDelete: (Table) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(MockData.tables) { table ->
            val statusColor = when (table.status) {
                TableStatus.EMPTY -> Color(0xFF4CAF50)
                TableStatus.OCCUPIED -> Color(0xFFF44336)
                TableStatus.WAITING_BILL -> Color(0xFFFF9800)
            }
            val statusText = when (table.status) {
                TableStatus.EMPTY -> "Empty"
                TableStatus.OCCUPIED -> "Occupied"
                TableStatus.WAITING_BILL -> "Waiting Bill"
            }
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Table ${table.number}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${table.capacity} Seats",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = { onEdit(table) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onDelete(table) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDialog(
    menuItem: MenuItem?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, MenuCategory, String) -> Unit
) {
    var name by remember { mutableStateOf(menuItem?.name ?: "") }
    var price by remember { mutableStateOf(menuItem?.price?.toString() ?: "") }
    var description by remember { mutableStateOf(menuItem?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(menuItem?.category ?: MenuCategory.FOOD) }
    var expandedCategory by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (menuItem != null) "Edit Item" else "Add New Item",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Item Name") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Name cannot be empty") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        priceError = false
                    },
                    label = { Text("Price ($)") },
                    isError = priceError,
                    supportingText = if (priceError) {
                        { Text("Enter a valid price") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        MenuCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.emoji()} ${category.displayName()}") },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nameError = name.isBlank()
                    priceError = price.toDoubleOrNull() == null
                    if (!nameError && !priceError) {
                        onConfirm(name.trim(), price.toDouble(), selectedCategory, description.trim())
                    }
                }
            ) {
                Text(if (menuItem != null) "Save" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TableDialog(
    table: Table?,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var number by remember { mutableStateOf(table?.number?.toString() ?: "") }
    var capacity by remember { mutableStateOf(table?.capacity?.toString() ?: "") }
    var numberError by remember { mutableStateOf(false) }
    var capacityError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (table != null) "Edit Table" else "Add New Table",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = {
                        number = it
                        numberError = false
                    },
                    label = { Text("Table Number") },
                    isError = numberError,
                    supportingText = if (numberError) {
                        { Text("Enter a valid table number") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = {
                        capacity = it
                        capacityError = false
                    },
                    label = { Text("Capacity") },
                    isError = capacityError,
                    supportingText = if (capacityError) {
                        { Text("Enter a valid capacity") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    numberError = number.toIntOrNull() == null
                    capacityError = capacity.toIntOrNull() == null
                    if (!numberError && !capacityError) {
                        onConfirm(number.toInt(), capacity.toInt())
                    }
                }
            ) {
                Text(if (table != null) "Save" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}