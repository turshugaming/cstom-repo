# TerraMonic Launcher v2.0

Modern, özellik zengin Minecraft launcher'ı Modrinth mod sistemi ve Fabric 1.21.5 desteği ile.

## ✨ Özellikler

- **Modrinth Mod Sistemi**: Modrinth mod paketlerini otomatik indirme ve yükleme
- **Fabric 1.21.5 Desteği**: Otomatik Fabric kurulumu ve yapılandırması  
- **Birleşik JSON Konfigürasyonu**: Tek dosyada tüm ayarlar
- **Otomatik Güncelleme**: Launcher ve modlar için otomatik güncelleme sistemi
- **Modern UI**: Lunar Client benzeri şık arayüz
- **Güvenli**: Şifreleme sorunları düzeltildi, boş dosya bırakılmıyor

## 🚀 Kurulum

### 1. Gereksinimler
- Java 11 veya üzeri
- JavaFX kütüphaneleri
- JSON library (org.json)

### 2. Konfigürasyon

#### A. JSON URL'ini Ayarlayın
`TerraMonicLauncher1.java` dosyasında bulunan JSON URL'ini kendi sunucunuza göre değiştirin:

```java
private static final String LAUNCHER_JSON_URL = "YOUR_JSON_URL_HERE";
```

#### B. Icon Dosyasını Ekleyin
`src/main/resources/com/terramonic/icon.png` konumuna launcher ikonunuzu yerleştirin.

### 3. launcher.json Dosyası
JSON dosyanızı aşağıdaki formatta hazırlayın:

```json
{
  "version": "1.0.2",
  "bakimmmodu": false,
  "bakimmmodusebebi": "DENEME",
  "minecraftversion": "1.21.5",
  "jar": "https://piston-data.mojang.com/v1/objects/..../client.jar",
  "modrinth_pack": "https://cdn.modrinth.com/data/.../modpack.mrpack",
  "haberler": [
    {
      "title": "Haber Başlığı",
      "content": "Haber içeriği...",
      "date": "2024-03-21",
      "type": "GÜNCELLEME"
    }
  ]
}
```

## 📋 JSON Ayarları

### Ana Ayarlar
- `version`: Launcher versiyonu
- `bakimmmodu`: Bakım modu açık/kapalı (true/false)
- `bakimmmodusebebi`: Bakım modu nedeni
- `minecraftversion`: Minecraft versiyonu (örn: "1.21.5")
- `jar`: Minecraft client.jar indirme URL'i
- `modrinth_pack`: Modrinth mod paketi (.mrpack) URL'i

### Haber Türleri
- `GÜNCELLEME`: Launcher/oyun güncellemeleri
- `DİSCORD`: Discord etkinlikleri
- `KAYNAK`: Yeni kaynaklar/haritalar
- `İNDİRİM`: İndirim duyuruları
- `YOUTUBE`: Video duyuruları
- `YAYIN`: Canlı yayın duyuruları
- `GENEL`: Genel haberler

## 🔧 Çalışma Prensibi

### 1. Başlangıç
- Icon yüklenir (`com/terramonic/icon.png`)
- Launcher config (`launcher.json`) indirilir
- Versiyon kontrolü yapılır
- Gerekirse config/mods temizlenir

### 2. Fabric Kurulumu
- `versions/fabric-loader-0.16.14-1.21.5/` klasörü oluşturulur
- Client.jar `fabric-loader-0.16.14-1.21.5.jar` olarak indirilir
- Fabric installer indirilip çalıştırılır
- Kurulum tamamlandıktan sonra installer silinir

### 3. Modrinth Sistemi
- `.mrpack` dosyası indirilir
- ZIP olarak çıkarılır
- `modrinth.index.json` okunur
- Her mod dosyası `downloads` URL'lerinden indirilir
- `.terramonic/mods/` klasörüne yerleştirilir
- Geçici dosyalar temizlenir

### 4. Güncelleme Sistemi
- Her açılışta versiyon kontrol edilir
- Launcher güncellenirse:
  - Config klasörü silinir
  - Mods klasörü silinir
  - Yeniden kurulum yapılır

## 📁 Klasör Yapısı

```
%APPDATA%/.terramonic/
├── mods/                    # Modrinth modları
├── config/                  # Oyun ayarları
├── versions/                # Minecraft versiyonları
│   └── fabric-loader-0.16.14-1.21.5/
│       ├── fabric-loader-0.16.14-1.21.5.jar
│       └── fabric-loader-0.16.14-1.21.5.json
├── libraries/               # Kütüphaneler
├── natives/                 # Native dosyalar
├── assets/                  # Oyun varlıkları
├── mods_profiles/           # Mod profilleri
└── terramonic_icon/         # Launcher ikonu
    └── icon.png
```

## 🎮 Oyun Başlatma

Launcher şu özellikleri destekler:
- **RAM Ayarı**: 1GB - 64GB arası (sistem sınırları dahilinde)
- **Çözünürlük**: 720p'den 12K'ya kadar desteklenen çözünürlükler
- **Fabric Mods**: Modrinth paketi otomatik yüklenir
- **Otomatik Java**: Sistem Java'sı kullanılır

## 🔒 Güvenlik İyileştirmeleri

- ✅ ZIP şifreleme sorunları düzeltildi
- ✅ Boş dosya bırakma sorunu çözüldü
- ✅ Güvenli dosya indirme
- ✅ Geçici dosyaların otomatik temizlenmesi

## 🐛 Hata Ayıklama

### Yaygın Sorunlar

1. **"Launcher config yüklenemedi"**
   - JSON URL'inin doğru olduğunu kontrol edin
   - Internet bağlantınızı kontrol edin

2. **"Fabric kurulumu başarısız"**
   - Java 11+ kurulu olduğunu kontrol edin
   - Antivirus yazılımının engellediğini kontrol edin

3. **"Modrinth pack kurulumu başarısız"**
   - Modrinth pack URL'inin geçerli olduğunu kontrol edin
   - Yeterli disk alanınız olduğunu kontrol edin

### Log Dosyaları
Tüm işlemler console'a yazdırılır. Hata ayıklama için console çıktısını kontrol edin.

## 🔄 Güncelleme

Launcher otomatik güncelleme yapar:
1. Her açılışta JSON'dan versiyon kontrol edilir
2. Yeni versiyon varsa otomatik güncelleme başlar
3. Config ve mods temizlenir, yeniden indirilir

## 📞 Destek

TerraMonic sunucusu için:
- Website: https://www.terramonic.com
- Discord: [TerraMonic Discord]

## 📄 Lisans

Bu proje TerraMonic sunucusu için özel olarak geliştirilmiştir.

---

**Not**: Bu launcher Minecraft 1.21.5 ve Fabric 0.16.14 için optimize edilmiştir. Diğer versiyonlar için kod değişiklikleri gerekebilir.
