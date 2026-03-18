package com.restaurantmanagement.data.model

object MockData {

    val tables = listOf(
        Table(1, 1, 4, TableStatus.EMPTY),
        Table(2, 2, 2, TableStatus.OCCUPIED),
        Table(3, 3, 6, TableStatus.WAITING_BILL),
        Table(4, 4, 4, TableStatus.EMPTY),
        Table(5, 5, 2, TableStatus.OCCUPIED),
        Table(6, 6, 8, TableStatus.EMPTY),
        Table(7, 7, 4, TableStatus.EMPTY),
        Table(8, 8, 2, TableStatus.WAITING_BILL),
    )

    val menuItems = listOf(
        MenuItem(1, "Türk Kahvesi", 45.0, "Kahve"),
        MenuItem(2, "Latte", 65.0, "Kahve"),
        MenuItem(3, "Cappuccino", 60.0, "Kahve"),
        MenuItem(4, "Americano", 55.0, "Kahve"),
        MenuItem(5, "Çay", 25.0, "Çay"),
        MenuItem(6, "Bitki Çayı", 35.0, "Çay"),
        MenuItem(7, "Fanta", 40.0, "Soğuk İçecek"),
        MenuItem(8, "Cola", 40.0, "Soğuk İçecek"),
        MenuItem(9, "Su", 15.0, "Soğuk İçecek"),
        MenuItem(10, "Cheesecake", 85.0, "Tatlı"),
        MenuItem(11, "Brownie", 75.0, "Tatlı"),
        MenuItem(12, "Tost", 70.0, "Atıştırmalık"),
        MenuItem(13, "Sandviç", 80.0, "Atıştırmalık"),
    )

    val mockOrders = listOf(
        Order(
            id = 1,
            tableId = 2,
            items = listOf(
                OrderItem(1, menuItems[1], 2, 2),
                OrderItem(2, menuItems[11], 1, 2),
            ),
            status = OrderStatus.OPEN
        ),
        Order(
            id = 2,
            tableId = 5,
            items = listOf(
                OrderItem(3, menuItems[0], 1, 5),
                OrderItem(4, menuItems[9], 2, 5),
            ),
            status = OrderStatus.OPEN
        )
    )

    val users = listOf(
        User(1, "admin", "1234", UserRole.ADMIN),
        User(2, "staff", "1234", UserRole.STAFF)
    )
}