-- Restaurant Order and Table Management System
-- Database Setup Script
-- Run: psql -U your_username -d postgres -f setup.sql

-- Create database
CREATE DATABASE restaurantdb;

-- Connect to database
\c restaurantdb

-- Drop tables if they exist
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS tables;
DROP TABLE IF EXISTS users;

-- Create tables
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    email VARCHAR(150)
);

CREATE TABLE tables (
    id SERIAL PRIMARY KEY,
    number INT NOT NULL UNIQUE,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'EMPTY'
);

CREATE TABLE menu_items (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(50) NOT NULL
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    table_id INT REFERENCES tables(id),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id),
    menu_item_id INT REFERENCES menu_items(id),
    quantity INT NOT NULL DEFAULT 1,
    table_id INT REFERENCES tables(id)
);

-- Insert sample users
INSERT INTO users (username, password, role, email) VALUES
('admin', '1234', 'ADMIN', 'admin@gmail.com'),
('staff1', '1234', 'STAFF', 'staff1@gmail.com'),
('staff2', '1234', 'STAFF', 'staff2@gmail.com');

-- Insert sample tables
INSERT INTO tables (number, capacity, status) VALUES
(1, 4, 'EMPTY'),
(2, 2, 'EMPTY'),
(3, 6, 'EMPTY'),
(4, 4, 'EMPTY'),
(5, 2, 'EMPTY'),
(6, 8, 'EMPTY');

-- Insert menu items
INSERT INTO menu_items (name, description, price, category) VALUES
('Water', 'Bottle Water', 1.00, 'DRINK'),
('Burger', 'Beef burger with lettuce, tomato and cheese', 6.00, 'FOOD'),
('Sandwich', 'Simple sandwich', 3.00, 'FOOD'),
('Cake', 'Chocolate cake', 4.50, 'DESSERT'),
('Coffee', 'Espresso', 2.50, 'DRINK'),
('Tea', 'Green tea', 2.00, 'DRINK'),
('Pizza', 'Pepperoni pizza', 8.00, 'FOOD'),
('Salad', 'Fresh salad', 5.00, 'FOOD'),
('Ice Cream', 'Vanilla ice cream', 3.50, 'DESSERT'),
('Orange Mango Juice', 'Fresh juice', 3.00, 'DRINK'),
('French Fries', 'Crispy fries', 2.50, 'SNACK'),
('Onion Rings', 'Crispy onion rings', 3.00, 'SNACK');

-- Done
\echo 'Database setup complete!'