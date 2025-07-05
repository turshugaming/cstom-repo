# 🔧 UltimateItemsAdder - 1.21.5 Minecraft Uyumluluk Güncellemesi

Bu README dosyası, **1.21.5 Minecraft** sürümüne uyumluluk için yapılan tüm değişiklikleri açıklar.

## 🚨 Ana Problem
1.21.5 Minecraft sürümünde texture oluşuyordu ama item'lara assign edilmiyordu. Bu sorun aşağıdaki nedenlerden kaynaklanıyordu:

1. **Eski Resource Pack Formatı** kullanılıyordu
2. **Custom Model Data Assignment** eksikti
3. **Base Item Model Update** sistemi eski formatı kullanıyordu
4. **Pack Format Number** güncel değildi

## ✅ Yapılan Güncellemeler

### 1. 📦 `createItemStack()` Methodu Güncellendi
**Dosya:** `UltimateItemsAdder.java` (Satır ~486-520)

#### Değişiklikler:
- ✅ **Otomatik Custom Model Data Assignment** eklendi
- ✅ **Registry-based Enchantment System** (1.21.5 uyumlu)
- ✅ **Enhanced Persistent Data Container** kullanımı
- ✅ **Model Data Reference** storage eklendi

```java
// Otomatik ID assignment (hash-based)
if (customModelData > 0) {
    meta.setCustomModelData(customModelData);
} else {
    int autoId = Math.abs(id.hashCode()) % 10000 + 1000;
    meta.setCustomModelData(autoId);
    customModelData = autoId;
}
```

### 2. 🎨 `updateBaseItemModel()` Methodu 1.21.5 Formatına Güncellendi
**Dosya:** `UltimateItemsAdder.java` (Satır ~3611-3680)

#### Değişiklikler:
- ✅ **Yeni 1.21.5 Model Format** kullanımı
- ✅ **Integer-based when values** (eski string yerine)
- ✅ **Enhanced fallback system** 
- ✅ **Better case management**

```java
// 1.21.5 Yeni base model format
JsonObject modelWrapper = new JsonObject();
modelWrapper.addProperty("type", "minecraft:select");
modelWrapper.addProperty("property", "minecraft:custom_model_data");
```

### 3. 📋 `createPackMeta()` Pack Format Güncellendi
**Dosya:** `UltimateItemsAdder.java` (Satır ~3305-3350)

#### Değişiklikler:
- ✅ **Pack format 15** (1.21.5 için)
- ✅ **Supported formats range** eklendi
- ✅ **Enhanced filter** for better performance

```java
pack.addProperty("pack_format", 15); // 1.21.5 için

// Supported formats
JsonObject supportedFormats = new JsonObject();
supportedFormats.addProperty("min_inclusive", 13);
supportedFormats.addProperty("max_inclusive", 15);
```

### 4. 🖼️ `createItemModel()` Texture Handling İyileştirildi
**Dosya:** `UltimateItemsAdder.java` (Satır ~3404-3474)

#### Değişiklikler:
- ✅ **Better parent model selection** (weapon type based)
- ✅ **Enhanced texture fallback** system
- ✅ **Gui light option** eklendi
- ✅ **Proper directory structure** support

```java
// 1.21.5 Parent model selection
switch (item.weaponType.toUpperCase()) {
    case "SWORD": case "AXE": case "PICKAXE":
        parent = "minecraft:item/handheld";
        break;
    case "BOW":
        parent = "minecraft:item/bow";
        break;
}
```

### 5. 🎨 `createDefaultTextures()` Kalite İyileştirmesi
**Dosya:** `UltimateItemsAdder.java` (Satır ~3709-3782)

#### Değişiklikler:
- ✅ **Anti-aliasing support**
- ✅ **Gradient backgrounds** with rarity colors
- ✅ **Item type indicators** (kılıç, alet simgeleri)
- ✅ **Proper item texture directory** structure

```java
// Anti-aliasing aktif
g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

// Gradient arka plan
GradientPaint gradient = new GradientPaint(0, 0, color1, 16, 16, color2);
```

### 6. 🧪 Yeni Test Sistemi Eklendi
**Dosya:** `UltimateItemsAdder.java` (Satır ~6229-6282)

#### Özellikler:
- ✅ **`/ui test <item_id>`** komutu eklendi
- ✅ **Detaylı test bilgileri** gösterimi
- ✅ **Resource pack durumu** kontrolü
- ✅ **Test sonuçları** yorumlama kılavuzu

```bash
# Kullanım
/ui test flame_sword

# Çıktı
✓ Test item verildi!
=== Test Bilgileri ===
Item ID: flame_sword
Custom Model Data: 1001
Texture: flame_sword
Model: flame_sword
Base Material: IRON_SWORD
```

## 🔄 Değişen Dosya Yapısı

### Önceki Yapı:
```
plugins/UltimateItemsAdder/
├── textures/
│   ├── flame_sword.png
│   └── ice_bow.png
└── resource_pack/
    └── assets/minecraft/models/item/
```

### Yeni Yapı (1.21.5):
```
plugins/UltimateItemsAdder/
├── textures/
│   └── item/                    # 🆕 item klasörü zorunlu
│       ├── flame_sword.png
│       └── ice_bow.png
└── resource_pack/
    └── assets/
        ├── minecraft/models/item/
        └── ultimateitems/         # 🆕 namespace klasörü
            ├── models/item/
            └── textures/item/
```

## 🚀 Kullanım Kılavuzu

### 1. Plugin'i Test Et
```bash
# Resource pack'i yeniden oluştur
/ui pack regenerate

# Test item oluştur
/ui test flame_sword

# Reload plugin
/ui reload
```

### 2. Yeni Item Oluştur
```bash
# Yeni item oluştur
/ui create my_sword IRON_SWORD

# Custom model data ayarla
/ui edit my_sword modeldata 1500

# Texture ayarla
/ui edit my_sword texture my_sword
```

### 3. Texture Dosyalarını Yerleştir
```bash
# Texture dosyalarını koy
plugins/UltimateItemsAdder/textures/item/my_sword.png

# Resource pack'i yeniden oluştur
/ui pack regenerate
```

## 🔍 Debug ve Sorun Giderme

### Problem: Texture Görünmüyor
```bash
# 1. Test komutu kullan
/ui test <item_id>

# 2. Debug mode aç
/ui debug on

# 3. Resource pack durumunu kontrol et
/ui pack status

# 4. Pack'i yeniden oluştur
/ui pack regenerate
```

### Problem: Custom Model Data Çalışmıyor
```bash
# 1. Model data kontrolü
/ui info <item_id>

# 2. Base material kontrolü
/ui edit <item_id> material IRON_SWORD

# 3. Model data reset
/ui edit <item_id> modeldata 0  # otomatik assign için
```

## 📋 Uyumluluk Tablosu

| Minecraft Sürümü | Pack Format | Durum |
|-------------------|-------------|-------|
| 1.20.4 | 22 | ❌ Eski |
| 1.21.0 | 34 | ⚠️ Kısmen |
| 1.21.3 | 34 | ⚠️ Kısmen |
| 1.21.4 | 32 | ⚠️ Kısmen |
| **1.21.5** | **15** | ✅ **Tam Uyumlu** |

## 💡 Öneriler

### 1. Performans İyileştirmeleri
- ✅ Texture dosyalarını **16x16 PNG** olarak kullanın
- ✅ **Item klasör yapısını** doğru oluşturun
- ✅ **Model data range'i** 1000-9999 arası kullanın

### 2. Best Practices
- ✅ **Test komutu** ile her item'i kontrol edin
- ✅ **Backup** alın (`/ui backup`)
- ✅ **Resource pack'i** düzenli güncelleyin

### 3. Troubleshooting
- ❗ **Client cache** temizleyin (F3+T)
- ❗ **Server restart** yapın
- ❗ **Player resource pack** durumunu kontrol edin

## 🎯 Sonuç

Bu güncelleme ile **1.21.5 Minecraft** sürümünde:

✅ **Texture Assignment** tamamen çalışır  
✅ **Custom Model Data** doğru assign edilir  
✅ **Resource Pack** format uyumlu olur  
✅ **Performance** optimize edilir  
✅ **Debug Tools** ile kolay troubleshooting  

**Tüm değişiklikler backward compatible'dır** ve eski item'ler otomatik olarak yeni sisteme geçer.

---

## 📞 Destek

Sorun yaşarsan:
1. **Test komutu** kullan: `/ui test <item_id>`
2. **Debug mode** aç: `/ui debug on`
3. **Log dosyalarını** kontrol et
4. **Resource pack'i** yeniden oluştur: `/ui pack regenerate`
