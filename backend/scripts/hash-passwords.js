// One-off migration: hashes any plain-text passwords left in the users table.
// Run once on databases created before bcrypt was introduced:
//   node scripts/hash-passwords.js
const bcrypt = require("bcryptjs");
const pool = require("../db");

async function main() {
	const { rows } = await pool.query("SELECT id, password FROM users");
	let updated = 0;
	for (const user of rows) {
		// bcrypt hashes start with $2a$, $2b$ or $2y$
		if (/^\$2[aby]\$/.test(user.password)) continue;
		const hash = await bcrypt.hash(user.password, 10);
		await pool.query("UPDATE users SET password = $1 WHERE id = $2", [hash, user.id]);
		updated++;
	}
	console.log(`Hashed ${updated} of ${rows.length} passwords.`);
	await pool.end();
}

main().catch((err) => {
	console.error(err);
	process.exit(1);
});
