package com.restaurantmanagement.data.model

data class Table(
    val id: Int,
    val number: Int,
    val capacity: Int,
    val status: TableStatus = TableStatus.EMPTY
)

enum class TableStatus {
    EMPTY,
    OCCUPIED,
    WAITING_BILL
}