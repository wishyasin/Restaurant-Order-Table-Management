// PostgreSQL ile Node.js'i konuşturan kütüphane olarak geçiyor. 
const { Pool } = require('pg');

// Bağlantı havuzu oluştur
// Pool: her istek için yeni bağlantı açmak yerine
// hazır bağlantıları tekrar kullanır → daha hızlı 
// örneğin 10 istek gelirse, 10 bağlantı açmak yerine
//  5 bağlantı açıp sırayla kullanırız
const pool = new Pool({
  host:     process.env.DB_HOST,      // Örn: localhost
  port:     process.env.DB_PORT || 5432, // PostgreSQL'in varsayılan portu 5432
  database: process.env.DB_NAME,      // Hangi veritabanı
  user:     process.env.DB_USER,      // PostgreSQL kullanıcı adı
  password: process.env.DB_PASSWORD,  // Şifre — .env'den okunur, koda yazılmaz!
});

// Bu pool'u dışarıya aç — app.js import edecek
module.exports = pool;