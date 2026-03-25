package com.restaurantmanagement.data.model

import androidx.compose.runtime.mutableStateListOf

object MockData {

    // masalar (şimdilik random durumlar verdim)
    val tables = mutableStateListOf(
        Table(1, 1, 4, TableStatus.EMPTY),
        Table(2, 2, 2, TableStatus.OCCUPIED),
        Table(3, 3, 6, TableStatus.WAITING_BILL),
        Table(4, 4, 4, TableStatus.EMPTY),
        Table(5, 5, 2, TableStatus.OCCUPIED),
        Table(6, 6, 8, TableStatus.EMPTY),
    )

    // menu
    val menuItems = mutableStateListOf(
        MenuItem(1, "Water", 1.0, MenuCategory.DRINK, "Bottle Water"),
        MenuItem(2, "Burger", 6.0, MenuCategory.FOOD, "Beef burger with lettuce, tomato and cheese"),
        MenuItem(3, "Sandwich", 3.0, MenuCategory.FOOD, "Simple sandwich"),
        MenuItem(4, "Cake", 4.5, MenuCategory.DESSERT, "Chocolate cake"),
        MenuItem(5, "Coffee", 2.5, MenuCategory.DRINK, "Espresso"),
        MenuItem(6, "Tea", 2.0, MenuCategory.DRINK, "Green tea"),
        MenuItem(7, "Pizza", 8.0, MenuCategory.FOOD, "Pepperoni pizza"),
        MenuItem(8, "Salad", 5.0, MenuCategory.FOOD, "Mixed salad"),
        MenuItem(9, "Ice Cream", 3.5, MenuCategory.DESSERT, "Vanilla ice cream"),
        MenuItem(10,"Orange Mango Juice",3.5,MenuCategory.DRINK,"Refreshing Juice"),
        MenuItem(11,"French Fries",2.5,MenuCategory.SNACK,"Crispy Potato"),
        MenuItem(12,"Onion Rings",3.0,MenuCategory.SNACK,"Crispy Onion"),
        MenuItem(13,"Hot Dog",4.0,MenuCategory.SNACK,"Hot Dog")

    )

    // örnek siparişler
    val mockOrders = mutableStateListOf(
        Order(
            id = 1,
            tableId = 2,
            items = listOf(
                OrderItem(1, menuItems[1], 2, 2), // burger x2
                OrderItem(2, menuItems[4], 1, 2)  // coffee
            ),
            status = OrderStatus.OPEN
        ),

        Order(
            id = 2,
            tableId = 5,
            items = listOf(
                OrderItem(3, menuItems[0], 1, 5), // water
                OrderItem(4, menuItems[3], 1, 5), // cake
                OrderItem(5, menuItems[6], 1, 5)  // pizza
            ),
            status = OrderStatus.OPEN
        )
    )

    // kullanıcılar
    val users = listOf(
        User(1, "admin", "1234", UserRole.ADMIN),
        User(2, "staff", "1234", UserRole.STAFF),
        User(3,"barking","1702",UserRole.ADMIN)

    )
}