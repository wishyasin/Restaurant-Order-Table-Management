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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.restaurantmanagement.data.model.MenuCategory
import com.restaurantmanagement.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportScreen(
    navController: NavController,
    viewModel: ReportViewModel = viewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.fetchReport()
    }

    val reportData by viewModel.reportData
    val isLoading by viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Report", fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (reportData == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No report data available.")
            }
        } else {
            val data = reportData!!
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Text("Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AttachMoney,
                            iconColor = Color(0xFF4CAF50),
                            label = "Total Revenue",
                            value = "$${String.format("%.2f", data.summary.totalRevenue)}"
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Receipt,
                            iconColor = Color(0xFF2196F3),
                            label = "Total Orders",
                            value = "${data.summary.totalOrders}"
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.TableRestaurant,
                            iconColor = Color(0xFFFF9800),
                            label = "Open Orders",
                            value = "${data.summary.openOrders}"
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AttachMoney,
                            iconColor = Color(0xFFFF9800),
                            label = "Pending Amount",
                            value = "$${String.format("%.2f", data.summary.pendingAmount)}"
                        )
                    }
                }

                item {
                    Text("Sales by Category", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            data.categorySales.forEachIndexed { index, sale ->

                                val categoryEnum = try { MenuCategory.valueOf(sale.category) } catch (e: Exception) { MenuCategory.FOOD }

                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(categoryEnum.emoji(), fontSize = 20.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(categoryEnum.displayName(), fontWeight = FontWeight.Medium)
                                            Text("${sale.qty} items sold", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                    Text("$${String.format("%.2f", sale.total)}", fontWeight = FontWeight.Bold)
                                }
                                if (index < data.categorySales.size - 1) Divider(Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }


                item {
                    Text("Top Selling Items", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            data.topItems.forEachIndexed { index, item ->
                                val categoryEnum = try { MenuCategory.valueOf(item.category) } catch (e: Exception) { MenuCategory.FOOD }

                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${index + 1}.", modifier = Modifier.width(24.dp), color = MaterialTheme.colorScheme.primary)
                                        Text(categoryEnum.emoji(), fontSize = 18.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.Medium)
                                            Text("${item.qty} sold", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                    Text("$${String.format("%.2f", item.total)}", fontWeight = FontWeight.Bold)
                                }
                                if (index < data.topItems.size - 1) Divider(Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}