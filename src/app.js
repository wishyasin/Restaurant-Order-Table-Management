const express = require('express');
const dotenv  = require('dotenv');
const bcrypt  = require('bcrypt');
const jwt     = require('jsonwebtoken');
const pool    = require('./db');

dotenv.config();

const app = express();
app.use(express.json());

// TEST
app.get('/', (req, res) => {
  res.json({ message: 'Restaurant management system is running!' });
});

// LOGIN
app.post('/api/login', async (req, res) => {
  const { username, password } = req.body;

  if (!username || !password) {
    return res.status(400).json({ error: 'Username and password are required' });
  }

  try {
    const result = await pool.query(
      'SELECT * FROM "Users" WHERE username = $1',
      [username]
    );

    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'User not found' });
    }

    const user = result.rows[0];

    if (password !== user.password) {
      return res.status(401).json({ error: 'Invalid password' });
    }

    const token = jwt.sign(
      { id: user.id, role: user.role },
      process.env.JWT_SECRET,
      { expiresIn: '8h' }
    );

    res.json({
      token,
      user: { id: user.id, username: user.username, role: user.role }
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// GET ALL TABLES
app.get('/api/tables', async (req, res) => {
  try {
    const result = await pool.query(
      'SELECT * FROM "Tables" ORDER BY table_number'
    );
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// GET MENU
app.get('/api/menu', async (req, res) => {
  try {
    const result = await pool.query(
      'SELECT * FROM "MenuItems" ORDER BY category, name'
    );
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// CREATE ORDER
app.post('/api/orders', async (req, res) => {
  const { table_id, items } = req.body;

  if (!table_id || !items || items.length === 0) {
    return res.status(400).json({ error: 'Table and items are required' });
  }

  try {
    const orderResult = await pool.query(
      'INSERT INTO "Orders" (table_id, status) VALUES ($1, $2) RETURNING *',
      [table_id, 'open']
    );
    const order = orderResult.rows[0];

    for (const item of items) {
      await pool.query(
        'INSERT INTO "OrderItems" (order_id, menu_item_id, quantity) VALUES ($1, $2, $3)',
        [order.id, item.menu_item_id, item.quantity]
      );
    }

    await pool.query(
      'UPDATE "Tables" SET status = $1 WHERE id = $2',
      ['occupied', table_id]
    );

    res.status(201).json({ message: 'Order created', order_id: order.id });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// GET ORDERS BY TABLE
app.get('/api/orders/:table_id', async (req, res) => {
  const { table_id } = req.params;

  try {
    const result = await pool.query(
      `SELECT o.id as order_id, o.status, o.created_at,
              oi.quantity, m.name, m.price, m.category
       FROM "Orders" o
       JOIN "OrderItems" oi ON oi.order_id = o.id
       JOIN "MenuItems" m ON m.id = oi.menu_item_id
       WHERE o.table_id = $1 AND o.status = 'open'
       ORDER BY o.created_at DESC`,
      [table_id]
    );
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// UPDATE TABLE STATUS
app.patch('/api/tables/:id', async (req, res) => {
  const { id } = req.params;
  const { status } = req.body;

  const validStatuses = ['empty', 'occupied', 'waiting_bill'];
  if (!validStatuses.includes(status)) {
    return res.status(400).json({ error: 'Invalid status' });
  }

  try {
    const result = await pool.query(
      'UPDATE "Tables" SET status = $1 WHERE id = $2 RETURNING *',
      [status, id]
    );
    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// PAY ORDER
app.post('/api/orders/:id/pay', async (req, res) => {
  const { id } = req.params;

  try {
    const orderResult = await pool.query(
      'UPDATE "Orders" SET status = $1 WHERE id = $2 RETURNING *',
      ['paid', id]
    );

    if (orderResult.rows.length === 0) {
      return res.status(404).json({ error: 'Order not found' });
    }

    const order = orderResult.rows[0];

    await pool.query(
      'UPDATE "Tables" SET status = $1 WHERE id = $2',
      ['empty', order.table_id]
    );

    res.json({ message: 'Payment received, table is now empty' });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// DAILY REPORT
app.get('/api/reports/daily', async (req, res) => {
  try {
    const ordersResult = await pool.query(
      `SELECT COUNT(*) as total_orders FROM "Orders"
       WHERE created_at::date = CURRENT_DATE`
    );

    const revenueResult = await pool.query(
      `SELECT COALESCE(SUM(m.price * oi.quantity), 0) as total_revenue
       FROM "Orders" o
       JOIN "OrderItems" oi ON oi.order_id = o.id
       JOIN "MenuItems" m ON m.id = oi.menu_item_id
       WHERE o.created_at::date = CURRENT_DATE`
    );

    const tablesResult = await pool.query(
      `SELECT status, COUNT(*) as count FROM "Tables" GROUP BY status`
    );

    res.json({
      total_orders: ordersResult.rows[0].total_orders,
      total_revenue: revenueResult.rows[0].total_revenue,
      tables: tablesResult.rows
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// START SERVER
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));