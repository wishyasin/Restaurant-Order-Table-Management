const express = require("express");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const pool = require("./db");

const BCRYPT_ROUNDS = 10;
const JWT_SECRET = process.env.JWT_SECRET;
const JWT_EXPIRES_IN = "8h";

if (!JWT_SECRET) {
	console.error("JWT_SECRET is not set. Add it to backend/.env");
	process.exit(1);
}

const app = express();
app.use(cors());
app.use(express.json());

// ─────────────────────────────────────────
//  AUTH
// ─────────────────────────────────────────

app.post("/api/login", async (req, res) => {
	const { username, password } = req.body;
	try {
		const result = await pool.query(
			"SELECT id, username, role, password FROM users WHERE username = $1",
			[username],
		);
		const user = result.rows[0];
		// Same message whether the user is missing or the password is wrong
		if (!user || !(await bcrypt.compare(password ?? "", user.password))) {
			return res.status(401).json({ message: "Invalid username or password" });
		}
		const token = jwt.sign({ sub: user.id, role: user.role }, JWT_SECRET, {
			algorithm: "HS256",
			expiresIn: JWT_EXPIRES_IN,
		});
		res.json({ id: user.id, username: user.username, role: user.role, token });
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

// Verifies the "Authorization: Bearer <token>" header and exposes the
// token payload as req.user. Registered after /api/login, so every route
// below this line requires a valid token.
function authenticate(req, res, next) {
	const [scheme, token] = (req.headers.authorization || "").split(" ");
	if (scheme !== "Bearer" || !token) {
		return res.status(401).json({ message: "Missing token" });
	}
	try {
		req.user = jwt.verify(token, JWT_SECRET, { algorithms: ["HS256"] });
		next();
	} catch (err) {
		res.status(401).json({ message: "Invalid or expired token" });
	}
}

function requireAdmin(req, res, next) {
	if (req.user.role !== "ADMIN") {
		return res.status(403).json({ message: "Admin only" });
	}
	next();
}

app.use("/api", authenticate);

// ─────────────────────────────────────────
//  USERS
// ─────────────────────────────────────────

app.get("/api/users", requireAdmin, async (req, res) => {
	try {
		const result = await pool.query("SELECT id, username, role, email FROM users ORDER BY id ASC");
		res.json(result.rows);
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

app.post("/api/users", requireAdmin, async (req, res) => {
	const { username, password, role, email } = req.body;
	if (!password) return res.status(400).json({ message: "Password is required" });
	try {
		const hash = await bcrypt.hash(password, BCRYPT_ROUNDS);
		await pool.query(
			"INSERT INTO users (username, password, role, email) VALUES ($1, $2, $3, $4)",
			[username, hash, role, email],
		);
		res.sendStatus(201);
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

// ─────────────────────────────────────────
//  MENU
// ─────────────────────────────────────────

app.get("/api/menu", async (req, res) => {
	try {
		const result = await pool.query("SELECT * FROM menu_items ORDER BY category, name");
		res.json(result.rows);
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

app.post("/api/menu", requireAdmin, async (req, res) => {
	const { name, price, category, description } = req.body;
	try {
		const result = await pool.query(
			"INSERT INTO menu_items (name, price, category, description) VALUES ($1, $2, $3, $4) RETURNING *",
			[name, price, category, description],
		);
		res.status(201).json(result.rows[0]);
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

app.put("/api/menu/:id", requireAdmin, async (req, res) => {
	const { name, price, category, description } = req.body;
	try {
		const result = await pool.query(
			"UPDATE menu_items SET name = $1, price = $2, category = $3, description = $4 WHERE id = $5 RETURNING *",
			[name, price, category, description, req.params.id],
		);
		if (result.rowCount === 0) return res.status(404).json({ message: "Menu item not found" });
		res.json(result.rows[0]);
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

app.delete("/api/menu/:id", requireAdmin, async (req, res) => {
	try {
		await pool.query("DELETE FROM menu_items WHERE id = $1", [req.params.id]);
		res.sendStatus(200);
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

// ─────────────────────────────────────────
//  TABLES
// ─────────────────────────────────────────

app.get("/api/tables", async (req, res) => {
	try {
		const result = await pool.query("SELECT * FROM tables ORDER BY number ASC");
		res.json(result.rows);
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

app.post("/api/tables", requireAdmin, async (req, res) => {
	const { number, capacity } = req.body;
	try {
		const result = await pool.query(
			"INSERT INTO tables (number, capacity, status) VALUES ($1, $2, $3) RETURNING *",
			[number, capacity, "EMPTY"],
		);
		res.status(201).json(result.rows[0]);
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

app.put("/api/tables/:id", requireAdmin, async (req, res) => {
	const { number, capacity } = req.body;
	try {
		const result = await pool.query(
			"UPDATE tables SET number = $1, capacity = $2 WHERE id = $3 RETURNING *",
			[number, capacity, req.params.id],
		);
		if (result.rowCount === 0) return res.status(404).json({ message: "Table not found" });
		res.json(result.rows[0]);
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

app.delete("/api/tables/:id", requireAdmin, async (req, res) => {
	const client = await pool.connect();
	try {
		await client.query("BEGIN");
		// Önce order_items.table_id referanslarını temizle
		await client.query("UPDATE order_items SET table_id = NULL WHERE table_id = $1", [req.params.id]);
		// Masaya ait order_items'ı ve orders'ı sil
		const orderIds = await client.query("SELECT id FROM orders WHERE table_id = $1", [req.params.id]);
		for (const row of orderIds.rows) {
			await client.query("DELETE FROM order_items WHERE order_id = $1", [row.id]);
		}
		await client.query("DELETE FROM orders WHERE table_id = $1", [req.params.id]);
		await client.query("DELETE FROM tables WHERE id = $1", [req.params.id]);
		await client.query("COMMIT");
		res.sendStatus(200);
	} catch (err) {
		await client.query("ROLLBACK");
		res.status(500).json({ error: err.message });
	} finally {
		client.release();
	}
});

// ─────────────────────────────────────────
//  ORDERS
// ─────────────────────────────────────────

app.post("/api/orders", async (req, res) => {
	const { table_id, items } = req.body;
	const client = await pool.connect();
	try {
		await client.query("BEGIN");

		const orderRes = await client.query(
			"INSERT INTO orders (table_id, status) VALUES ($1, $2) RETURNING id",
			[table_id, "OPEN"],
		);
		const orderId = orderRes.rows[0].id;

		for (const item of items) {
			await client.query(
				"INSERT INTO order_items (order_id, menu_item_id, quantity, table_id) VALUES ($1, $2, $3, $4)",
				[orderId, item.menu_item_id, item.quantity, table_id],
			);
		}

		await client.query("UPDATE tables SET status = $1 WHERE id = $2", ["OCCUPIED", table_id]);
		await client.query("COMMIT");
		res.status(201).json({ message: "Sipariş alındı ve masa doldu.", orderId });
	} catch (e) {
		await client.query("ROLLBACK");
		res.status(500).json({ error: e.message });
	} finally {
		client.release();
	}
});

// Mevcut siparişe ürün ekle
app.post("/api/orders/:orderId/items", async (req, res) => {
	const { orderId } = req.params;
	const { items, table_id } = req.body;
	const client = await pool.connect();
	try {
		await client.query("BEGIN");

		// Siparişin table_id'sini al (body'den gelmeyebilir)
		let resolvedTableId = table_id;
		if (!resolvedTableId) {
			const orderRow = await client.query("SELECT table_id FROM orders WHERE id = $1", [orderId]);
			if (orderRow.rows.length > 0) resolvedTableId = orderRow.rows[0].table_id;
		}

		for (const item of items) {
			const existing = await client.query(
				"SELECT * FROM order_items WHERE order_id = $1 AND menu_item_id = $2",
				[orderId, item.menu_item_id],
			);
			if (existing.rows.length > 0) {
				await client.query(
					"UPDATE order_items SET quantity = quantity + $1 WHERE order_id = $2 AND menu_item_id = $3",
					[item.quantity, orderId, item.menu_item_id],
				);
			} else {
				await client.query(
					"INSERT INTO order_items (order_id, menu_item_id, quantity, table_id) VALUES ($1, $2, $3, $4)",
					[orderId, item.menu_item_id, item.quantity, resolvedTableId],
				);
			}
		}

		await client.query("COMMIT");
		res.status(200).json({ message: "Items added to order" });
	} catch (e) {
		await client.query("ROLLBACK");
		res.status(500).json({ error: e.message });
	} finally {
		client.release();
	}
});

// Masanın aktif siparişini getir
app.get("/api/orders/table/:tableId", async (req, res) => {
	const { tableId } = req.params;
	try {
		const orderResult = await pool.query(
			"SELECT * FROM orders WHERE table_id = $1 AND status = $2 ORDER BY created_at DESC LIMIT 1",
			[tableId, "OPEN"],
		);
		if (orderResult.rows.length === 0) return res.json(null);

		const order = orderResult.rows[0];
		const itemsResult = await pool.query(
			`SELECT 
				oi.id as order_item_id, 
				oi.quantity, 
				oi.table_id,
				mi.id as menu_item_id, 
				mi.name, 
				mi.price, 
				mi.category, 
				mi.description
			FROM order_items oi 
			JOIN menu_items mi ON oi.menu_item_id = mi.id 
			WHERE oi.order_id = $1`,
			[order.id],
		);

		const formattedItems = itemsResult.rows.map(row => ({
			id: row.order_item_id,
			quantity: row.quantity,
			tableId: row.table_id,
			menuItem: {
				id: row.menu_item_id,
				name: row.name,
				price: parseFloat(row.price),
				category: row.category,
				description: row.description,
			},
		}));

		res.json({
			id: order.id,
			tableId: order.table_id,
			status: order.status,
			items: formattedItems,
		});
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

// Ödeme al
app.post("/api/orders/payment", async (req, res) => {
	const orderId = req.body.orderId || req.body.order_id;
	const tableId = req.body.tableId || req.body.table_id;
	const status = req.body.status || "PAID";
	try {
		await pool.query(
			"UPDATE orders SET status = $1 WHERE table_id = $2 AND status = 'OPEN'",
			[status, tableId],
		);
		await pool.query("UPDATE tables SET status = $1 WHERE id = $2", ["EMPTY", tableId]);
		res.status(200).json({ message: "Payment successful, table is now empty" });
	} catch (err) {
		res.status(500).json({ error: err.message });
	}
});

// ─────────────────────────────────────────
//  REPORTS
// ─────────────────────────────────────────

app.get("/api/reports/daily", requireAdmin, async (req, res) => {
	try {
		const summary = await pool.query(`
			SELECT 
				COALESCE(SUM(CASE WHEN o.status != 'OPEN' THEN (mi.price * oi.quantity) ELSE 0 END), 0)::FLOAT as total_revenue,
				COUNT(DISTINCT o.id)::INT as total_orders,
				COUNT(DISTINCT CASE WHEN o.status = 'OPEN' THEN o.id END)::INT as open_orders,
				COALESCE(SUM(CASE WHEN o.status = 'OPEN' THEN (mi.price * oi.quantity) ELSE 0 END), 0)::FLOAT as pending_amount
			FROM orders o
			LEFT JOIN order_items oi ON o.id = oi.order_id
			LEFT JOIN menu_items mi ON oi.menu_item_id = mi.id
		`);

		const categorySales = await pool.query(`
			SELECT mi.category, 
				   SUM(oi.quantity)::INT as qty, 
				   SUM(mi.price * oi.quantity)::FLOAT as total
			FROM order_items oi
			JOIN menu_items mi ON oi.menu_item_id = mi.id
			GROUP BY mi.category
			ORDER BY total DESC
		`);

		const topItems = await pool.query(`
			SELECT mi.name, mi.category, 
				   SUM(oi.quantity)::INT as qty, 
				   SUM(mi.price * oi.quantity)::FLOAT as total
			FROM order_items oi
			JOIN menu_items mi ON oi.menu_item_id = mi.id
			GROUP BY mi.id, mi.name, mi.category
			ORDER BY qty DESC LIMIT 5
		`);

		res.json({
			summary: summary.rows[0],
			categorySales: categorySales.rows,
			topItems: topItems.rows,
		});
	} catch (err) {
		console.error(err);
		res.status(500).json({ error: err.message });
	}
});

// ─────────────────────────────────────────
//  SERVER
// ─────────────────────────────────────────

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
	console.log(`Server ${PORT} portunda çalışıyor...`);
});