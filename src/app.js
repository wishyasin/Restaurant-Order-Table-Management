const express = require('express');
const dotenv  = require('dotenv');
const bcrypt  = require('bcrypt');   // Şifre doğrulama için
const jwt     = require('jsonwebtoken'); // Giriş sonrası token üretmek için
const pool    = require('./db');     // Az önce yazdığımız db.js'i bağla

// .env dosyasını oku (DB_HOST, JWT_SECRET gibi değişkenler buradan gelir)
dotenv.config();

const app = express(); 

// Gelen isteklerin gövdesini (body) JSON olarak oku
// Bu olmadan req.body boş gelir
app.use(express.json());

// ─── TEST ENDPOINT ──────────────────────────────────────
// Sunucu çalışıyor mu diye kontrol etmek için
app.get('/', (req, res) => {
  res.json({ message: 'Adisyon sistemi çalışıyor! 🍽️' });
});

// ─── LOGIN ENDPOINT ─────────────────────────────────────
// Android'den POST isteği gelir: { email: "...", password: "..." }
app.post('/api/login', async (req, res) => {

  // İstek gövdesinden email ve şifreyi al
  const { email, password } = req.body;

  // Adım 1: Email veya şifre boş geldiyse hemen reddet
  if (!email || !password) {
    return res.status(400).json({ error: 'Email ve şifre zorunlu' });
  }

  try {
    // Adım 2: Bu email'e sahip kullanıcı veritabanında var mı?
    // $1 → SQL injection'a karşı güvenli parametre kullanımı
    // "Users" → tablo adı büyük harfle başlıyorsa tırnak gerekir
    const result = await pool.query(
      'SELECT * FROM "Users" WHERE email = $1',
      [email]
    );

    // Sorgu sonuç döndürmediyse → böyle bir kullanıcı yok
    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'Kullanıcı bulunamadı' });
    }

    // Kullanıcı bulunduysa ilk satırı al
    const user = result.rows[0];

    // Adım 3: Şifre doğru mu?
    // bcrypt.compare → kullanıcının girdiği düz şifreyi
    // veritabanındaki hash'li şifreyle karşılaştırır
    const isValid = await bcrypt.compare(password, user.password);

    if (!isValid) {
      return res.status(401).json({ error: 'Şifre hatalı' });
    }

    // Adım 4: Her şey tamam → JWT token üret
    // Bu token Android'e gönderilir, sonraki isteklerde kimlik kartı gibi kullanılır
    // İçine kullanıcının id ve rolünü göm (admin mi, garson mu?)
    const token = jwt.sign(
      { id: user.id, role: user.role },  // Token'ın içindeki bilgiler
      process.env.JWT_SECRET,            // İmzalama anahtarı (.env'den)
      { expiresIn: '8h' }                // 8 saat sonra geçersiz olur
    );

    // Başarılı yanıt: token + kullanıcı bilgisi
    // Şifreyi asla geri gönderme!
    res.json({
      token,
      user: { id: user.id, name: user.name, role: user.role }
    });

  } catch (err) {
    // Veritabanı bağlantı hatası gibi beklenmedik durumlar
    console.error(err);
    res.status(500).json({ error: 'Sunucu hatası' });
  }
});

// ─── SUNUCUYU BAŞLAT ────────────────────────────────────
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Sunucu ${PORT} portunda çalışıyor`));