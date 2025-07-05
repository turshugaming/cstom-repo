# 📋 UltimateItemsAdder Config Dosyaları Rehberi

Bu klasörde **UltimateItemsAdder** plugin'i için hazırlanmış **1.21.5 Minecraft uyumlu** tüm konfigürasyon dosyaları bulunur.

## 📁 Config Dosyaları

### 🔧 **config.yml**
Ana konfigürasyon dosyası. Plugin'in genel ayarları:
- Genel ayarlar (debug, dil, prefix)
- Resource pack ayarları (format 15 - 1.21.5 uyumlu)
- HTTP sunucu ayarları
- Veritabanı ayarları
- Performans ayarları
- Güvenlik ve izinler

### ⚔️ **items.yml**
Custom itemların tanımlandığı dosya. İçerir:
- **Silahlar**: Ateş Kılıcı, Buz Yayı, Sihir Asası
- **Zırhlar**: Ejder Miğferi (set bonus'lu)
- **Araçlar**: Madenci Kazması (vein miner, auto smelt)
- **Yiyecekler**: Sihirli Elma (buff'lar ile)
- **İksirler**: İyileştirme İksiri
- **Mücevherler**: Ateş Mücevheri (socket sistemi)

### 🔨 **recipes.yml**
Custom tarifler ve crafting sistemleri:
- **Shaped recipes**: Klasik 3x3 tarifler
- **Brewing recipes**: İksir tarifleri
- **Smelting recipes**: Özel fırın tarifleri
- **Smithing recipes**: Demirci tezgahı tarifleri
- **Combination recipes**: Gelişmiş kombinasyon tarifleri

### ⚡ **abilities.yml**
Custom yetenekler ve özel güçler:
- **Ateş yetenekleri**: Fire Strike, Flame Aura
- **Buz yetenekleri**: Ice Shot, Freeze Target
- **Sihir yetenekleri**: Lightning Strike, Teleport, Heal
- **Özel yetenekler**: Auto Smelt, Vein Miner
- **Kombinasyon yetenekleri**: Fire-Ice Explosion
- **Set yetenekleri**: Dragon Strength

### 🌟 **effects.yml**
Custom efektler ve status effectler:
- **Damage Over Time**: Burning, Poison, Wither
- **Buff'lar**: Strength Boost, Defense Boost
- **Debuff'lar**: Slowness, Weakness, Stun
- **Şartlı efektler**: Berserker Rage
- **Alan efektleri**: Poison Cloud, Fire Aura
- **Set efektleri**: Dragon Power

### 🧱 **blocks.yml**
Custom bloklar ve yapılar:
- **Madenler**: Mythril Ore, Adamantium Ore
- **Yapı blokları**: Mythril Block, Enchanted Obsidian
- **Fonksiyonel bloklar**: Magic Workbench, Alchemist Station
- **Teknolojik bloklar**: Energy Generator, Teleporter Pad
- **Çiftçilik blokları**: Fertile Soil, Magic Crop

### ✨ **enchants.yml**
Custom büyüler (enchantments):
- **Silah büyüleri**: Flame Burst, Frost Aspect, Lightning Strike
- **Araç büyüleri**: Auto Smelt, Vein Miner, Replanting
- **Zırh büyüleri**: Elemental Protection, Auto Repair
- **Yay büyüleri**: Explosive Shot, Piercing Shot
- **Özel büyüler**: Soul Bound, Experience Boost
- **Lanetli büyüler**: Cursed Hunger, Cursed Fragility

### 🌍 **language.yml**
Çoklu dil desteği:
- **Türkçe (tr_tr)**: Tam Türkçe çeviri
- **İngilizce (en_us)**: Tam İngilizce çeviri
- Tüm komutlar, mesajlar ve GUI başlıkları
- Test sistemi mesajları

### 🔐 **plugin.yml**
Plugin tanımlama dosyası:
- Komut tanımları (`/ui`, `/ultimateitems`)
- Detaylı izin sistemi
- Plugin bilgileri ve dependencies

## 🚀 Kurulum

1. **Config dosyalarını yerleştir:**
```
plugins/UltimateItemsAdder/
├── config.yml
├── items.yml
├── recipes.yml
├── abilities.yml
├── effects.yml
├── blocks.yml
├── enchants.yml
├── language.yml
└── plugin.yml (plugin ana klasörüne)
```

2. **Texture dosyalarını yerleştir:**
```
plugins/UltimateItemsAdder/
└── textures/
    └── item/  # 🆕 1.21.5'te item klasörü zorunlu
        ├── flame_sword.png
        ├── ice_bow.png
        ├── magic_staff.png
        └── ... (diğer texture'lar)
```

3. **Plugin'i başlat:**
```bash
/ui reload
/ui pack regenerate
```

## 🎮 Kullanım

### Temel Komutlar:
```bash
# Test komutu (1.21.5 texture testi)
/ui test flame_sword

# Item verme
/ui give <oyuncu> flame_sword

# Item listesi
/ui list weapons

# Item bilgisi
/ui info flame_sword

# Resource pack yenileme
/ui pack regenerate
```

### Örnek Kullanımlar:

#### ⚔️ Ateş Kılıcı Oluşturma:
```bash
/ui give YourName flame_sword
```
- **Özellikler**: 15 hasar, %25 kritik şansı, ateş hasarı
- **Yetenekler**: Fire Strike, Flame Aura
- **Efektler**: Burning Target

#### 🏹 Buz Yayı Oluşturma:
```bash
/ui give YourName ice_bow
```
- **Özellikler**: 12 hasar, 32 blok menzil, dondurucu etki
- **Yetenekler**: Ice Shot, Freeze Target

#### 🧙‍♂️ Sihir Asası Oluşturma:
```bash
/ui give YourName magic_staff
```
- **Özellikler**: 100 mana, 5 saniye cooldown
- **Yetenekler**: Lightning Strike, Teleport, Heal Self

## 🔧 Özelleştirme

### Custom Item Ekleme:
1. `items.yml` dosyasına yeni item ekleyin
2. Texture dosyasını `textures/item/` klasörüne koyun
3. `/ui reload` ile yeniden yükleyin
4. `/ui pack regenerate` ile resource pack güncelleyin

### Custom Recipe Ekleme:
1. `recipes.yml` dosyasına yeni tarif ekleyin
2. Tarif tipini belirleyin (SHAPED, BREWING, vb.)
3. `/ui reload` ile aktifleştirin

### Custom Ability Ekleme:
1. `abilities.yml` dosyasına yeni yetenek ekleyin
2. Trigger tipini ayarlayın (RIGHT_CLICK, PASSIVE, vb.)
3. Efektleri, parçacıkları ve sesleri tanımlayın

## 📊 ID Aralıkları

| Kategori | ID Aralığı | Örnek |
|----------|------------|-------|
| Silahlar | 1001-1999 | flame_sword: 1001 |
| Zırhlar | 2001-2999 | dragon_helmet: 2001 |
| Araçlar | 3001-3999 | miners_pickaxe: 3001 |
| Yiyecekler | 4001-4999 | magic_apple: 4001 |
| İksirler | 5001-5999 | healing_potion: 5001 |
| Mücevherler | 6001-6999 | fire_gem: 6001 |
| Bloklar | 7001-7999 | mythril_ore: 7001 |

## 🎯 1.21.5 Uyumluluk

Bu config dosyaları **1.21.5 Minecraft** sürümü için özel olarak hazırlanmıştır:

✅ **Pack Format 15** kullanır  
✅ **Custom Model Data** otomatik atanır  
✅ **Registry-based Enchantments** destekler  
✅ **Enhanced Resource Pack** format  
✅ **Test komutu** ile doğrulama  

## 🆘 Sorun Giderme

### Texture Görünmüyor:
```bash
/ui test <item_id>
/ui pack regenerate
```

### Comut Çalışmıyor:
```bash
/ui reload
```

### Resource Pack Yüklenmiyor:
- Pack format 15 kullandığınızdan emin olun
- Texture dosyalarının `textures/item/` klasöründe olduğunu kontrol edin
- `/ui pack regenerate` komutunu kullanın

## 📞 Destek

Bu config dosyaları hakkında sorularınız için:
1. **Test komutunu** kullanın: `/ui test <item_id>`
2. **Debug modu** açın: `/ui debug on`
3. **Log dosyalarını** kontrol edin

**Başarılar!** 🚀