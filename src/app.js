const express = require('express');
const dotenv = require('dotenv');

// .env dosyasını yükle
dotenv.config();

const app = express();

// Gelen isteklerin JSON olduğunu söyle
app.use(express.json());

// İlk endpoint — test amaçlı
app.get('/', (req, res) => {
  res.json({ message: 'Adisyon sistemi çalışıyor! 🍽️' });
});

// Sunucuyu başlat
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Sunucu ${PORT} portunda çalışıyor`);
});