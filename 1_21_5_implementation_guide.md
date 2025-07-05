# 🔧 1.21.5 Minecraft Item Texture Fix Kılavuzu

## 🚨 Problem
1.21.5 Minecraft sürümünde texture oluşuyor ama item'lara assign edilmiyor.

## ✅ Çözüm Adımları

### 1. 📂 Dosya Hazırlığı

#### A) Yedek Al
```bash
# Mevcut plugin klasörünü yedekle
cp -r plugins/UltimateItemsAdder plugins/UltimateItemsAdder_backup
```

#### B) Dosyaları Yerleştir
```
plugins/UltimateItemsAdder/
├── configs/
│   └── items.yml (example_items_1_21_5.yml içeriğini kopyala)
├── textures/
│   └── item/ (texture dosyalarını buraya koy)
└── UltimateItemsAdder.jar
```

### 2. 🔧 Kod Güncellemeleri

#### A) `createItemStack()` Methodunu Güncelle
UltimateItemsAdder.java'da CustomItem class'ının `createItemStack()` methodunu **minecraft_1_21_5_item_fix.java** dosyasındaki version ile replace et.

**Önemli Değişiklikler:**
- ✅ Custom Model Data otomatik assignment
- ✅ Registry.ENCHANTMENT kullanımı
- ✅ PersistentDataContainer'a CMD reference ekleme

#### B) `updateBaseItemModel()` Methodunu Güncelle
1.21.5 yeni format kullanılıyor:
```json
{
  "model": {
    "type": "minecraft:select",
    "property": "minecraft:custom_model_data",
    "cases": [
      {"when": 1001, "model": "ultimateitems:item/flame_sword"}
    ],
    "fallback": {"type": "minecraft:model", "model": "minecraft:item/iron_sword"}
  }
}
```

#### C) `createPackMeta()` Methodunu Güncelle
**pack_format**: 15 (1.21.5 için)

#### D) Test Komutunu Ekle
**minecraft_1_21_5_item_fix.java** ve **test_command_handler.java** dosyalarındaki kod parçalarını ekle.

### 3. 🎨 Texture Hazırlığı

#### A) Texture Klasörü Oluştur
```bash
mkdir -p plugins/UltimateItemsAdder/textures/item/
```

#### B) Texture Dosyalarını Koy
```
textures/item/
├── flame_sword.png
├── ice_staff.png
├── dragon_helmet.png
├── ruby_pickaxe.png
├── magic_apple.png
├── magic_arrow.png
├── mithril_ingot.png
├── teleport_crystal.png
├── wolf_whistle.png
└── gold_coin.png
```

**Texture Formatı:**
- ✅ 16x16 PNG
- ✅ Alpha channel destekli
- ✅ Minecraft pixel art style

### 4. ⚙️ Konfigürasyon

#### A) items.yml Güncelle
**example_items_1_21_5.yml** içeriğini `plugins/UltimateItemsAdder/configs/items.yml`'ye kopyala.

#### B) config.yml'de Port Ayarla
```yaml
# config.yml
resource-pack:
  enabled: true
  auto-send: true
  required: false
  port: 8080
  host: "localhost"
```

### 5. 🚀 Test Etme

#### A) Plugin'i Yeniden Başlat
```bash
/reload
# veya sunucuyu restart et
```

#### B) Resource Pack'i Oluştur
```bash
/ui pack reload
```

#### C) Item'i Test Et
```bash
/ui test flame_sword
```

**Beklenen Çıktı:**
```
[UltimateItems] Test ediliyor: flame_sword
Custom Model Data: 1001
Texture: flame_sword
Model: flame_sword
[UltimateItems] Test item verildi!
Resource pack'i reload ettikten sonra texture görünecek.
[UltimateItems] Resource pack yeniden gönderildi!
```

### 6. 🔍 Debug & Troubleshooting

#### A) Debug Mode Aktif Et
```bash
/ui debug
```

#### B) Logları Kontrol Et
```bash
tail -f logs/latest.log | grep UltimateItems
```

#### C) Resource Pack Kontrol Et
```
plugins/UltimateItemsAdder/generated_pack.zip
└── assets/
    ├── minecraft/models/item/
    │   └── iron_sword.json (1.21.5 format)
    └── ultimateitems/
        ├── models/item/
        │   └── flame_sword.json
        └── textures/item/
            └── flame_sword.png
```

### 7. 🎯 Yaygın Sorunlar & Çözümler

#### Problem: "Texture gösterilmiyor"
```bash
# 1. Resource pack hash'ini kontrol et
/ui debug
# 2. Item'in custom model data'sını kontrol et
/ui info flame_sword
# 3. Texture dosyasının varlığını kontrol et
ls plugins/UltimateItemsAdder/textures/item/
```

#### Problem: "Resource pack indirilmiyor"
```bash
# Port açık mı kontrol et
netstat -an | grep 8080
# HTTP server çalışıyor mu kontrol et
curl http://localhost:8080/resource_pack.zip
```

#### Problem: "Custom Model Data assignment edilmiyor"
```java
// createItemStack() methodunda debug ekle
getLogger().info("Item: " + id + ", CMD: " + customModelData);
```

### 8. 📊 Performance Optimization

#### A) Memory Usage Kontrol Et
```bash
/ui stats
```

#### B) Resource Pack Boyutunu Küçült
- ✅ Gereksiz texture'ları sil
- ✅ PNG compression kullan
- ✅ Animated texture'ları sınırla

#### C) HTTP Server Cache
```java
// HTTP response'lara cache header ekle
exchange.getResponseHeaders().set("Cache-Control", "max-age=3600");
```

### 9. 🎮 Sonuç

Tüm adımları tamamladıktan sonra:

1. ✅ Item'lar 1.21.5'te doğru texture'la görünecek
2. ✅ Resource pack otomatik generate edilecek
3. ✅ Custom Model Data düzgün assign edilecek
4. ✅ Player'lara otomatik resource pack gönderilecek

### 10. 📝 Notlar

#### A) Version Compatibility
- 🔴 1.21.4 ve altı: **Çalışmaz**
- 🟢 1.21.5: **Tam uyumlu**
- 🟡 1.22+: **Test gerekli**

#### B) Client Requirements
- ✅ Resource pack enabled
- ✅ Minimum Java 17
- ✅ 512MB+ allocated memory

#### C) Server Requirements
- ✅ Spigot/Paper 1.21.5+
- ✅ Minimum 2GB RAM
- ✅ Port 8080 açık

---

## 🆘 Yardım Gerekirse

Sorun yaşarsan:

1. 📋 **Debug log'ları gönder:**
   ```bash
   /ui debug
   /ui test <item_id>
   ```

2. 📁 **Generated pack'i kontrol et:**
   ```bash
   unzip -l plugins/UltimateItemsAdder/generated_pack.zip
   ```

3. 🔧 **Test komutuyla item'i kontrol et:**
   ```bash
   /ui info <item_id>
   /ui test <item_id>
   ```

Bu guide'ı takip edersen 1.21.5'te texture'ların düzgün çalışacağından emin olabilirsin! 🚀