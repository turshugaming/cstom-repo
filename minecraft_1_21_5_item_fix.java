// 🔧 1.21.5 Minecraft Item Texture Fix 
// Bu kod parçalarını UltimateItemsAdder.java'ya replace et

// ============================
// 1. createItemStack methodunu güncelle
// ============================
public ItemStack createItemStack() {
    ItemStack item = new ItemStack(baseMaterial);
    ItemMeta meta = item.getItemMeta();

    if (meta != null) {
        // İsim
        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        }

        // Lore
        if (lore != null && !lore.isEmpty()) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);
        }

        // 🆕 1.21.5 Custom Model Data - Doğru assignment
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        } else {
            // Otomatik ID assignment (hash-based)
            int autoId = Math.abs(id.hashCode()) % 10000 + 1000;
            meta.setCustomModelData(autoId);
            customModelData = autoId; // Update the field
        }

        // Enchantments
        if (enchantments != null) {
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                try {
                    Enchantment enchant = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(entry.getKey().toLowerCase()));
                    if (enchant != null) {
                        meta.addEnchant(enchant, entry.getValue(), true);
                    }
                } catch (Exception e) {
                    // Fallback to old method
                    Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(entry.getKey().toLowerCase()));
                    if (enchant != null) {
                        meta.addEnchant(enchant, entry.getValue(), true);
                    }
                }
            }
        }

        // ItemFlags
        if (flags != null) {
            for (String flag : flags) {
                try {
                    meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        // Unbreakable
        if (unbreakable) {
            meta.setUnbreakable(true);
        }

        // 🆕 1.21.5 Attributes - Updated API
        if (attributes != null) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                try {
                    Attribute attribute = Attribute.valueOf(entry.getKey().toUpperCase());
                    double value = Double.parseDouble(entry.getValue().toString());

                    AttributeModifier modifier = new AttributeModifier(
                            new NamespacedKey(UltimateItemsAdder.instance, "ui_" + id + "_" + attribute.name().toLowerCase()),
                            value,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlotGroup.HAND
                    );
                    meta.addAttributeModifier(attribute, modifier);
                } catch (Exception ignored) {
                }
            }
        }

        // Persistent Data Container
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // Custom item ID
        container.set(
                new NamespacedKey(UltimateItemsAdder.instance, "custom_item_id"),
                PersistentDataType.STRING,
                id
        );

        // Custom model data reference
        container.set(
                new NamespacedKey(UltimateItemsAdder.instance, "cmd"),
                PersistentDataType.INTEGER,
                customModelData
        );

        // NBT Data
        if (nbt != null) {
            for (Map.Entry<String, Object> entry : nbt.entrySet()) {
                container.set(
                        new NamespacedKey(UltimateItemsAdder.instance, entry.getKey()),
                        PersistentDataType.STRING,
                        entry.getValue().toString()
                );
            }
        }

        item.setItemMeta(meta);
    }

    return item;
}

// ============================
// 2. 1.21.5 updateBaseItemModel - YENİ FORMAT
// ============================
private void updateBaseItemModel(CustomItem item) throws IOException {
    Path minecraftModelsPath = resourcePackPath.resolve("assets/minecraft/models/item");
    minecraftModelsPath.toFile().mkdirs();

    String materialName = item.baseMaterial.name().toLowerCase();
    File baseModelFile = new File(minecraftModelsPath.toFile(), materialName + ".json");

    JsonObject baseModel;
    if (baseModelFile.exists()) {
        try (FileReader reader = new FileReader(baseModelFile)) {
            baseModel = gson.fromJson(reader, JsonObject.class);
        }
    } else {
        // 🆕 1.21.5 Yeni base model format
        baseModel = new JsonObject();
        
        JsonObject modelWrapper = new JsonObject();
        modelWrapper.addProperty("type", "minecraft:select");
        modelWrapper.addProperty("property", "minecraft:custom_model_data");

        // Fallback model
        JsonObject fallback = new JsonObject();
        fallback.addProperty("type", "minecraft:model");
        fallback.addProperty("model", "minecraft:item/" + materialName);
        modelWrapper.add("fallback", fallback);

        // Cases array
        JsonArray cases = new JsonArray();
        modelWrapper.add("cases", cases);
        
        baseModel.add("model", modelWrapper);
    }

    // 🆕 Custom model case ekle
    JsonObject modelWrapper = baseModel.getAsJsonObject("model");
    JsonArray cases = modelWrapper.has("cases") ? 
            modelWrapper.getAsJsonArray("cases") : new JsonArray();

    // Mevcut case'i kontrol et
    boolean caseExists = false;
    for (JsonElement element : cases) {
        JsonObject caseObj = element.getAsJsonObject();
        if (caseObj.has("when") && 
            caseObj.get("when").getAsInt() == item.customModelData) {
            caseExists = true;
            break;
        }
    }

    if (!caseExists) {
        JsonObject newCase = new JsonObject();
        newCase.addProperty("when", item.customModelData); // Integer olarak

        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", namespace + ":item/" + item.model);
        newCase.add("model", model);

        cases.add(newCase);
    }

    modelWrapper.add("cases", cases);

    // Dosyayı yaz
    try (FileWriter writer = new FileWriter(baseModelFile)) {
        gson.toJson(baseModel, writer);
    }
}

// ============================
// 3. 1.21.5 createItemModel - Güncellenmiş
// ============================
private void createItemModel(CustomItem item, Path modelsPath) throws IOException {
    JsonObject model = new JsonObject();

    // Parent model
    String parent = "minecraft:item/generated";
    if (item.weaponType != null) {
        switch (item.weaponType.toUpperCase()) {
            case "SWORD":
            case "AXE":
            case "PICKAXE":
            case "SHOVEL":
            case "HOE":
                parent = "minecraft:item/handheld";
                break;
            case "BOW":
                parent = "minecraft:item/bow";
                break;
            case "CROSSBOW":
                parent = "minecraft:item/crossbow";
                break;
        }
    }
    model.addProperty("parent", parent);

    // Textures - 1.21.5 format
    JsonObject textures = new JsonObject();
    
    // Ana texture
    if (item.texture != null && !item.texture.isEmpty()) {
        textures.addProperty("layer0", namespace + ":item/" + item.texture);
    } else {
        // Fallback texture
        textures.addProperty("layer0", namespace + ":item/" + item.id);
    }

    // Ek texture katmanları
    for (int i = 1; i <= 4; i++) {
        String layerTexture = item.texture + "_layer" + i;
        File layerFile = new File(texturesPath.toFile(), "item/" + layerTexture + ".png");
        if (layerFile.exists()) {
            textures.addProperty("layer" + i, namespace + ":item/" + layerTexture);
        }
    }

    model.add("textures", textures);

    // Display settings - 1.21.5 uyumlu
    if (item.weaponType != null || item.toolType != null) {
        JsonObject display = createDisplaySettings(item);
        model.add("display", display);
    }

    // Overrides - durability, pulling vb.
    JsonArray overrides = createOverrides(item);
    if (overrides.size() > 0) {
        model.add("overrides", overrides);
    }

    // 🆕 1.21.5 Gui light option
    model.addProperty("gui_light", "front");

    // Model dosyasını yaz
    File modelFile = new File(modelsPath.toFile(), item.model + ".json");
    modelFile.getParentFile().mkdirs();
    
    try (FileWriter writer = new FileWriter(modelFile)) {
        gson.toJson(model, writer);
    }
}

// ============================
// 4. Texture Creation - 1.21.5 uyumlu
// ============================
private void createDefaultTextures(Path texturesPath) throws IOException {
    Path itemTexturesPath = texturesPath.resolve("item");
    itemTexturesPath.toFile().mkdirs();

    for (CustomItem item : customItems.values()) {
        if (!item.enabled) continue;

        String textureName = item.texture != null ? item.texture : item.id;
        Path texturePath = itemTexturesPath.resolve(textureName + ".png");
        
        if (!Files.exists(texturePath)) {
            // 🆕 Daha kaliteli default texture oluştur
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            // Anti-aliasing aktif
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Gradient arka plan
            java.awt.Color color1, color2;
            switch (item.rarity != null ? item.rarity.toUpperCase() : "COMMON") {
                case "LEGENDARY":
                    color1 = new java.awt.Color(255, 215, 0); // Gold
                    color2 = new java.awt.Color(255, 140, 0); // Dark orange
                    break;
                case "EPIC":
                    color1 = new java.awt.Color(138, 43, 226); // Blue violet
                    color2 = new java.awt.Color(75, 0, 130);   // Indigo
                    break;
                case "RARE":
                    color1 = new java.awt.Color(0, 191, 255);  // Deep sky blue
                    color2 = new java.awt.Color(0, 100, 200);  // Darker blue
                    break;
                case "UNCOMMON":
                    color1 = new java.awt.Color(50, 205, 50);  // Lime green
                    color2 = new java.awt.Color(34, 139, 34);  // Forest green
                    break;
                default: // COMMON
                    color1 = new java.awt.Color(169, 169, 169); // Dark gray
                    color2 = new java.awt.Color(105, 105, 105); // Dim gray
                    break;
            }

            GradientPaint gradient = new GradientPaint(0, 0, color1, 16, 16, color2);
            g2d.setPaint(gradient);
            g2d.fillRect(1, 1, 14, 14);

            // Border
            g2d.setColor(new java.awt.Color(64, 64, 64));
            g2d.drawRect(0, 0, 15, 15);

            // Item type indicator
            g2d.setColor(java.awt.Color.WHITE);
            if (item.weaponType != null) {
                // Kılıç simgesi
                g2d.fillRect(7, 3, 2, 10);
                g2d.fillRect(5, 5, 6, 2);
                g2d.fillRect(6, 12, 4, 2);
            } else if (item.toolType != null) {
                // Alet simgesi
                g2d.fillOval(5, 5, 6, 6);
            } else {
                // Genel item simgesi
                g2d.fillRect(5, 5, 6, 6);
            }

            g2d.dispose();

            ImageIO.write(image, "PNG", texturePath.toFile());
        }
    }
}

// ============================
// 5. Resource Pack Meta - 1.21.5 format
// ============================
private void createPackMeta() throws IOException {
    JsonObject packMeta = new JsonObject();
    
    JsonObject pack = new JsonObject();
    pack.addProperty("pack_format", 15); // 🆕 1.21.5 format number
    pack.addProperty("description", ChatColor.translateAlternateColorCodes('&', 
            "&dUltimateItems Resource Pack\n&7Generated automatically"));
    
    // 🆕 1.21.5 supported formats
    JsonObject supportedFormats = new JsonObject();
    supportedFormats.addProperty("min_inclusive", 13);
    supportedFormats.addProperty("max_inclusive", 15);
    pack.add("supported_formats", supportedFormats);
    
    packMeta.add("pack", pack);

    // 🆕 Filter for better performance
    JsonObject filter = new JsonObject();
    JsonArray blockList = new JsonArray();
    // Block unnecessary files
    filter.add("block", blockList);
    packMeta.add("filter", filter);

    File metaFile = new File(resourcePackPath.toFile(), "pack.mcmeta");
    try (FileWriter writer = new FileWriter(metaFile)) {
        gson.toJson(packMeta, writer);
    }
}

// ============================
// 6. Debug & Test Method
// ============================
public void testItemGeneration(Player player, String itemId) {
    CustomItem item = customItems.get(itemId);
    if (item == null) {
        player.sendMessage(PREFIX + ChatColor.RED + "Item bulunamadı!");
        return;
    }

    player.sendMessage(PREFIX + ChatColor.YELLOW + "Test ediliyor: " + itemId);
    player.sendMessage(ChatColor.GRAY + "Custom Model Data: " + item.customModelData);
    player.sendMessage(ChatColor.GRAY + "Texture: " + item.texture);
    player.sendMessage(ChatColor.GRAY + "Model: " + item.model);
    
    ItemStack testItem = item.createItemStack();
    player.getInventory().addItem(testItem);
    
    player.sendMessage(PREFIX + ChatColor.GREEN + "Test item verildi!");
    player.sendMessage(ChatColor.YELLOW + "Resource pack'i reload ettikten sonra texture görünecek.");
    
    // Force resource pack reload
    scheduler.schedule(() -> {
        sendResourcePack(player);
        player.sendMessage(PREFIX + ChatColor.CYAN + "Resource pack yeniden gönderildi!");
    }, 2, TimeUnit.SECONDS);
}

// ============================
// 7. Enhanced Resource Pack Generation
// ============================
private void generateResourcePack() {
    try {
        getLogger().info("🔧 1.21.5 Resource Pack oluşturuluyor...");
        
        // Clean old pack
        if (Files.exists(resourcePackPath)) {
            FileUtils.deleteDirectory(resourcePackPath.toFile());
        }
        
        // Create directories
        createDirectories();
        
        // Create pack meta (1.21.5 format)
        createPackMeta();
        
        // Create pack icon
        createDefaultPackIcon();
        
        // Generate item models (1.21.5 compatible)
        createItemModels();
        
        // Copy/create textures
        copyTextures();
        
        // Create language files
        createLanguageFiles();
        
        // Create sounds (if any)
        copySounds();
        
        // Create final zip
        createZipFile();
        
        // Generate hash
        generateHash();
        
        getLogger().info("✅ 1.21.5 Resource Pack başarıyla oluşturuldu!");
        
    } catch (Exception e) {
        getLogger().severe("❌ Resource Pack oluşturulurken hata: " + e.getMessage());
        e.printStackTrace();
    }
}

// Bu methodları /ui test <itemid> komutu için ekle:
// handleTest methodunu onCommand'a ekle