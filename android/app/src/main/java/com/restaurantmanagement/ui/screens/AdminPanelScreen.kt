package com.restaurantmanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.restaurantmanagement.data.model.Table
import com.restaurantmanagement.data.model.TableStatus
import com.restaurantmanagement.data.model.User
import com.restaurantmanagement.viewmodel.MenuViewModel
import com.restaurantmanagement.viewmodel.TableViewModel

import androidx.compose.material.icons.filled.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    navController: NavController,
    menuViewModel: MenuViewModel,
    tableViewModel: TableViewModel,
    adminViewModel: com.restaurantmanagement.viewmodel.AdminViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Menu Items", "Tables", "Staff")


    var showMenuDialog by remember { mutableStateOf(false) }
    var editingMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var showTableDialog by remember { mutableStateOf(false) }
    var editingTable by remember { mutableStateOf<Table?>(null) }


    var showDeleteMenuDialog by remember { mutableStateOf(false) }
    var deletingMenuItem by remember { mutableStateOf<MenuItem?>(null) }
    var showDeleteTableDialog by remember { mutableStateOf(false) }
    var deletingTable by remember { mutableStateOf<Table?>(null) }

    var showUserDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    } else if(selectedTab == 1) {
                        editingTable = null
                        showTableDialog = true
                    }else{
                        showUserDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
                    menuItemsList = menuViewModel.menuItems,
                    onEdit = { editingMenuItem = it; showMenuDialog = true },
                    onDelete = { deletingMenuItem = it; showDeleteMenuDialog = true }
                )
                1 -> TablesTab(
                    tablesList = tableViewModel.tables,
                    onEdit = { editingTable = it; showTableDialog = true },
                    onDelete = { deletingTable = it; showDeleteTableDialog = true }
                )
                2 -> {

                    StaffTab(staffList = adminViewModel.users)
                }
            }
        }
    }

    if (showMenuDialog) {
        MenuItemDialog(
            menuItem = editingMenuItem,
            onDismiss = { showMenuDialog = false },
            onConfirm = { name, price, category, description ->
                adminViewModel.saveMenuItem(editingMenuItem?.id, name, price, category.name, description) {
                    menuViewModel.fetchMenu()
                    showMenuDialog = false
                }
            }
        )
    }

    if (showTableDialog) {
        TableDialog(
            table = editingTable,
            onDismiss = { showTableDialog = false },
            onConfirm = { number, capacity ->
                adminViewModel.saveTable(editingTable, number, capacity) {
                    showTableDialog = false
                }
            }
        )
    }

    if (showUserDialog) {
        UserDialog(
            onDismiss = { showUserDialog = false },
            onConfirm = { username, email, password, role ->
                adminViewModel.saveUser(username, email, password, role) {
                    showUserDialog = false
                }
            }
        )
    }

    if (showDeleteMenuDialog && deletingMenuItem != null) {
        AlertDialog(
            onDismissRequest = { showDeleteMenuDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete \"${deletingMenuItem!!.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    adminViewModel.deleteMenuItem(deletingMenuItem!!.id) {
                        menuViewModel.fetchMenu()
                        showDeleteMenuDialog = false
                    }
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteMenuDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteTableDialog && deletingTable != null) {
        AlertDialog(
            onDismissRequest = { showDeleteTableDialog = false },
            title = { Text("Delete Table") },
            text = { Text("Delete Table ${deletingTable!!.number}?") },
            confirmButton = {
                TextButton(onClick = {
                    adminViewModel.deleteTable(deletingTable!!.id) {
                        tableViewModel.fetchTables()
                        showDeleteTableDialog = false
                    }
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteTableDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun MenuItemsTab(menuItemsList: List<MenuItem>, onEdit: (MenuItem) -> Unit, onDelete: (MenuItem) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(menuItemsList) { item ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.category.emoji(), fontSize = 24.sp, modifier = Modifier.width(40.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text("$${String.format("%.2f", item.price)}", color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onEdit(item) }) { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Blue) }
                    IconButton(onClick = { onDelete(item) }) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                }
            }
        }
    }
}

@Composable
fun TablesTab(tablesList: List<Table>, onEdit: (Table) -> Unit, onDelete: (Table) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tablesList) { table ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Table ${table.number}", fontWeight = FontWeight.Bold)
                        Text("Capacity: ${table.capacity}", fontSize = 12.sp)
                    }
                    IconButton(onClick = { onEdit(table) }) { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Blue) }
                    IconButton(onClick = { onDelete(table) }) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
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

@Composable
fun StaffTab(staffList: List<User>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(staffList) { employee ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = employee.username,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = employee.email ?: "No email",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = employee.role.name,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var user by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("Username") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") })
                OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") })


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isAdmin, onCheckedChange = { isAdmin = it })
                    Text("Assign Admin Role")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val role = if (isAdmin) "ADMIN" else "STAFF"
                onConfirm(user, email, pass, role)
            }) { Text("Add User") }
        }
    )
}