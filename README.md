# 🎮 TerraMonic Launcher - Modern Minecraft Fabric Launcher

> En havalı, en güzel, en modern Minecraft Fabric launcher'ı! Glassmorphism UI, animasyonlar, otomatik mod kurulumu ve daha fazlası!

## ✨ Özellikler

### 🎨 Modern UI & Efektler
- **Glassmorphism** efektli arayüz
- **Blur** efektli arkaplan
- **Glow** ve **DropShadow** efektleri
- **Animasyonlar** (fade, scale, rotate)
- **800x600** pencere boyutu, köşeleri yuvarlak
- **Şeffaf** pencere, özel window controls

### 🚀 Launcher Özellikleri
- **Splashscreen** ile % ilerleme göstergesi
- **Otomatik modpack indirme** (.mrpack desteği)
- **Minecraft 1.21.5** + **Fabric Loader 0.16.14**
- **Mod yönetimi** (ekleme, silme, profil yönetimi)
- **RAM ayarı** (2-16GB slider)
- **Arkaplan değiştirme** özelliği
- **Bakım modu** desteği
- **start.bat yerine direkt Java** başlatma

### 📦 Otomatik İndirmeler
- TerraMonic Enhanced Beta modpack
- Minecraft client + kütüphaneler
- Assets dosyaları
- Native dosyalar
- Fabric kütüphaneleri

## 🛠️ Kurulum

### Gereksinimler
- **Java 17** veya üzeri
- **Maven 3.6+**
- **Windows** (APPDATA klasörü kullanılıyor)

### 1. Projeyi İndir
```bash
git clone https://github.com/yourusername/terramonic-launcher.git
cd terramonic-launcher
```

### 2. Kaynakları Ekle
Aşağıdaki dosyaları `src/main/resources/com/terramonic/` klasörüne ekle:
- `icon.png` - Launcher ikonu (80x80 önerilen)
- `arkaplan.png` - Varsayılan arkaplan resmi 
- `Gilroy-Bold.otf` - Font dosyası (opsiyonel)

### 3. Derleme ve Çalıştırma

#### Windows
```bash
# 🚀 TEK TIKLA ÇALIŞTIR
launcher.bat
```

#### Linux/Mac
```bash
# 🚀 TEK KOMUTLA ÇALIŞTIR
./launcher.sh
```

#### Manuel Çalıştırma
```bash
# Derleme ve çalıştırma
mvn clean javafx:run

# JAR oluşturmak için
mvn clean package
```

## � Kullanım

### İlk Çalıştırma
1. Uygulama açılır açılmaz **splashscreen** gelir
2. **Modpack indirilir** ve kurulur (%0-100 ilerleme)
3. **Minecraft dosyaları** indirilir
4. **Ana ekran** açılır

### Ana Ekran
- **Kullanıcı adı** gir (zorunlu)
- **Şifre** gir (opsiyonel)
- **RAM** miktarını ayarla
- **Mod yönetimi** ile modları ekle/sil
- **Arkaplan** değiştir (🖼 butonu)
- **OYNA** butonuna tıkla!

### Pencere Kontrolleri
- **─** Simge durumuna küçült
- **✕** Uygulamayı kapat
- **🖼** Arkaplan değiştir

## � Dosya Yapısı

```
%APPDATA%/.terramonic/
├── mods/                 # Modlar
├── config/              # Config dosyaları
├── versions/            # Minecraft sürümleri
├── libraries/           # Kütüphaneler
├── assets/              # Oyun varlıkları
├── natives/             # Native dosyalar
├── icon.png             # Launcher ikonu
├── arkaplan.png         # Arkaplan resmi
├── maintenance.json     # Bakım modu ayarları
└── launcher_profiles.json
```

## ⚙️ Bakım Modu

`%APPDATA%/.terramonic/maintenance.json` dosyasını düzenleyerek:

```json
{
  "bakimmmodu": true,
  "bakimmmodusebebi": "Sunucu güncellemesi yapılıyor..."
}
```

## 🎨 Özelleştirme

### Arkaplan Değiştirme
- 🖼 butonuna tıkla
- PNG/JPG dosyası seç
- Otomatik blur efekti uygulanır

### RAM Ayarı
- Alt kısımdaki slider ile 2-16GB arası
- Otomatik olarak oyun başlatırken uygulanır

### Mod Ekleme
- "Mod Ekle" butonuna tıkla
- .jar dosyası seç
- `mods/` klasörüne kopyalanır

## � Geliştirme

### Proje Yapısı
```
src/
├── main/
│   ├── java/com/terramonic/
│   │   ├── TerraMonicLauncher.java     # Ana launcher
│   │   └── MinecraftFabricLauncher.java # Minecraft indirici
│   └── resources/com/terramonic/
│       ├── icon.png
│       ├── arkaplan.png
│       └── Gilroy-Bold.otf
├── pom.xml                             # Maven config
└── README.md
```

### Teknolojiler
- **JavaFX 21** - Modern UI
- **Gson** - JSON işleme
- **HttpClient** - Dosya indirme
- **Maven** - Bağımlılık yönetimi

## � Sorun Giderme

### JavaFX Bulunamıyor
```bash
# JavaFX modüllerini manuel ekle
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar launcher.jar
```

### Modpack İndirilemiyor
- İnternet bağlantınızı kontrol edin
- Firewall ayarlarını kontrol edin
- Antivirüs yazılımını geçici olarak kapatın

### Oyun Başlatılamıyor
- Java 17+ kurulu olduğundan emin olun
- %APPDATA%/.terramonic klasörünü silin ve tekrar deneyin
- "Onar" butonuna tıklayın

## 📝 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Branch'i push yapın (`git push origin feature/amazing-feature`)
5. Pull Request açın

## 📞 İletişim

- **Discord**: TerraMonic Community
- **GitHub**: [Issues](https://github.com/yourusername/terramonic-launcher/issues)

---

🎮 **TerraMonic - En havalı Minecraft deneyimi için!** ✨
