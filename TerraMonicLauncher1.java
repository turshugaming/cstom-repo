    /**
     * YENİ: Gelişmiş Fabric kurulumu - Python kodundan alınan
     */
    private void setupFabricAdvanced() throws IOException, InterruptedException {
        String fabricVersionName = "fabric-loader-" + FABRIC_VERSION + "-" + MINECRAFT_VERSION;
        Path versionsDir = TERRAMONIC_PATH.resolve("versions");
        Path fabricVersionDir = versionsDir.resolve(fabricVersionName);
        Path fabricJarPath = fabricVersionDir.resolve(fabricVersionName + ".jar");
        Path fabricJsonPath = fabricVersionDir.resolve(fabricVersionName + ".json");

        Files.createDirectories(fabricVersionDir);
        System.out.println("🧵 Gelişmiş Fabric kurulumu yapılıyor...");

        // Fabric profil bilgisini al - Python kodundan
        if (!Files.exists(fabricJsonPath)) {
            Platform.runLater(() -> statusLabel.setText("🧵 Fabric profil bilgisi alınıyor..."));
            
            try {
                String fabricProfileContent = readJsonFromUrl(FABRIC_PROFILE_URL);
                JSONObject fabricProfile = new JSONObject(fabricProfileContent);
                
                Files.writeString(fabricJsonPath, fabricProfile.toString(2));
                System.out.println("✅ Fabric profil JSON kaydedildi");
                
                if (fabricProfile.has("libraries")) {
                    JSONArray fabricLibraries = fabricProfile.getJSONArray("libraries");
                    downloadFabricLibrariesAdvanced(fabricLibraries);
                }
            } catch (Exception e) {
                System.out.println("❌ Fabric profil indirme hatası: " + e.getMessage());
                throw new IOException("Fabric profil indirilemedi", e);
            }
        }

        // Client jar'ı kopyala - Python kodundan
        Path minecraftJarPath = versionsDir.resolve(MINECRAFT_VERSION).resolve(MINECRAFT_VERSION + ".jar");
        if (!Files.exists(fabricJarPath) && Files.exists(minecraftJarPath)) {
            try {
                if ("windows".equals(platformName)) {
                    Files.copy(minecraftJarPath, fabricJarPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("✅ Fabric JAR kopyalandı (Windows)");
                } else {
                    Files.createSymbolicLink(fabricJarPath, minecraftJarPath);
                    System.out.println("✅ Fabric JAR symlink oluşturuldu");
                }
            } catch (IOException e) {
                Files.copy(minecraftJarPath, fabricJarPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        System.out.println("🎉 Gelişmiş Fabric kurulumu tamamlandı!");
    }

    /**
     * YENİ: Fabric libraries'i paralel indir - Python kodundan
     */
    private void downloadFabricLibrariesAdvanced(JSONArray fabricLibraries) {
        System.out.println("📚 Fabric kütüphaneleri paralel indiriliyor... (" + fabricLibraries.length() + " adet)");
        
        Path librariesDir = TERRAMONIC_PATH.resolve("libraries");
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < fabricLibraries.length(); i++) {
            JSONObject library = fabricLibraries.getJSONObject(i);
            
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    downloadFabricSingleLibrary(library, librariesDir);
                } catch (Exception e) {
                    System.out.println("❌ Fabric library indirme hatası: " + e.getMessage());
                }
            }, executorService);
            
            futures.add(future);
        }
        
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        
        try {
            allFutures.get(3, TimeUnit.MINUTES);
            System.out.println("✅ Tüm Fabric kütüphaneleri paralel olarak indirildi!");
        } catch (Exception e) {
            System.out.println("⚠️ Bazı Fabric kütüphaneleri indirilemedi: " + e.getMessage());
        }
    }

    /**
     * YENİ: Tek Fabric library indirme - Python kodundan
     */
    private void downloadFabricSingleLibrary(JSONObject library, Path librariesDir) {
        try {
            String name = library.getString("name");
            String[] parts = name.split(":");
            if (parts.length >= 3) {
                String groupId = parts[0].replace(".", "/");
                String artifactId = parts[1];
                String version = parts[2];
                String classifier = parts.length > 3 ? "-" + parts[3] : "";
                
                String filename = artifactId + "-" + version + classifier + ".jar";
                String path = groupId + "/" + artifactId + "/" + version + "/" + filename;
                String baseUrl = library.optString("url", "https://maven.fabricmc.net/");
                if (!baseUrl.endsWith("/")) {
                    baseUrl += "/";
                }
                String url = baseUrl + path;
                
                Path targetPath = librariesDir.resolve(path);
                
                String expectedHash = library.optString("sha1", null);
                String hashType = "sha1";
                if (expectedHash == null) {
                    expectedHash = library.optString("md5", null);
                    hashType = "md5";
                }
                
                Platform.runLater(() -> statusLabel.setText("🧵 Fabric Lib: " + filename));
                downloadFileWithHash(url, targetPath, expectedHash, hashType);
            }
        } catch (Exception e) {
            System.out.println("❌ Fabric library indirme hatası: " + e.getMessage());
        }
    }

    /**
     * YENİ: Gelişmiş oyun başlatma - Python kodundan alınan command oluşturma
     */
    private void launchGameAdvanced() {
        Platform.runLater(() -> {
            if (!modsReady.get() || !librariesReady.get()) {
                showError("Dosyalar henüz hazır değil. Lütfen bekleyin.");
                return;
            }

            gameIsLaunching = true;
            downloadProgress.setVisible(true);
            statusLabel.setVisible(true);
            statusLabel.setText("🚀 Oyun başlatılıyor (Gelişmiş)...");
            statusLabel.setTextFill(PRIMARY_COLOR);
            playButton.setDisable(true);

            Task<Void> launchTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    String selectedRam = ramCombo != null ? ramCombo.getSelectionModel().getSelectedItem() : "4 GB";
                    String selectedRes = resCombo != null ? resCombo.getSelectionModel().getSelectedItem() : "1280x720";

                    if (selectedRam == null) selectedRam = "4 GB";
                    if (selectedRes == null) selectedRes = "1280x720";

                    String ramAmount = selectedRam.replace(" GB", "G");
                    String[] resParts = selectedRes.split("x");
                    String width = resParts[0];
                    String height = resParts[1];

                    String fabricVersionName = "fabric-loader-" + FABRIC_VERSION + "-" + MINECRAFT_VERSION;

                    // Gelişmiş classpath oluştur - Python kodundan
                    StringBuilder classpath = new StringBuilder();
                    Path librariesDir = TERRAMONIC_PATH.resolve("libraries");

                    // Fabric JAR
                    Path fabricJarPath = TERRAMONIC_PATH.resolve("versions").resolve(fabricVersionName).resolve(fabricVersionName + ".jar");
                    if (Files.exists(fabricJarPath)) {
                        classpath.append(fabricJarPath.toString());
                    }

                    // Essential libraries - Python kodundan alınan kritik liste
                    String[] essentialLibs = {
                        "net/fabricmc/fabric-loader/" + FABRIC_VERSION + "/fabric-loader-" + FABRIC_VERSION + ".jar",
                        "net/fabricmc/sponge-mixin/0.15.5+mixin.0.8.7/sponge-mixin-0.15.5+mixin.0.8.7.jar",
                        "net/fabricmc/intermediary/" + MINECRAFT_VERSION + "/intermediary-" + MINECRAFT_VERSION + ".jar",
                        "org/ow2/asm/asm/9.8/asm-9.8.jar",
                        "org/ow2/asm/asm-tree/9.8/asm-tree-9.8.jar",
                        "org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3.jar",
                        "org/lwjgl/lwjgl-opengl/3.3.3/lwjgl-opengl-3.3.3.jar",
                        "org/lwjgl/lwjgl-glfw/3.3.3/lwjgl-glfw-3.3.3.jar",
                        "org/lwjgl/lwjgl-stb/3.3.3/lwjgl-stb-3.3.3.jar",
                        "org/joml/joml/1.10.8/joml-1.10.8.jar",
                        "com/mojang/authlib/6.0.58/authlib-6.0.58.jar",
                        "com/mojang/brigadier/1.3.10/brigadier-1.3.10.jar",
                        "com/mojang/datafixerupper/8.0.16/datafixerupper-8.0.16.jar",
                        "com/google/guava/guava/33.3.1-jre/guava-33.3.1-jre.jar",
                        "com/google/code/gson/gson/2.11.0/gson-2.11.0.jar",
                        "org/apache/logging/log4j/log4j-core/2.24.1/log4j-core-2.24.1.jar",
                        "org/apache/logging/log4j/log4j-api/2.24.1/log4j-api-2.24.1.jar",
                        "org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar",
                        "it/unimi/dsi/fastutil/8.5.15/fastutil-8.5.15.jar",
                        "io/netty/netty-all/4.1.118.Final/netty-all-4.1.118.Final.jar",
                        "net/sf/jopt-simple/jopt-simple/5.0.4/jopt-simple-5.0.4.jar",
                        "commons-io/commons-io/2.17.0/commons-io-2.17.0.jar"
                    };

                    for (String lib : essentialLibs) {
                        Path libPath = librariesDir.resolve(lib);
                        if (Files.exists(libPath)) {
                            if (classpath.length() > 0) {
                                classpath.append("windows".equals(platformName) ? ";" : ":");
                            }
                            classpath.append(libPath.toString());
                        }
                    }

                    Platform.runLater(() -> statusLabel.setText("🔧 Launch command hazırlanıyor..."));

                    // Python kodundaki command yapısı
                    List<String> command = new ArrayList<>();
                    command.add("java");
                    
                    // Memory settings
                    command.add("-Xmx" + ramAmount);
                    command.add("-Xms1G");
                    
                    // JVM settings - Python kodundan geliştirilmiş
                    command.add("--enable-native-access=ALL-UNNAMED");
                    command.add("--add-opens");
                    command.add("java.base/java.lang=ALL-UNNAMED");
                    command.add("--add-opens");
                    command.add("java.base/java.util=ALL-UNNAMED");
                    command.add("--add-opens");
                    command.add("java.base/java.io=ALL-UNNAMED");
                    
                    // Library path - Python kodundan
                    command.add("-Djava.library.path=" + TERRAMONIC_PATH.resolve("natives").toString());
                    
                    // Fabric settings - Python kodundan
                    command.add("-DFabricMcEmu=net.minecraft.client.main.Main");
                    
                    // Locale settings - Python kodundan
                    command.add("-Duser.language=tr");
                    command.add("-Duser.country=TR");
                    
                    // Debug ve güvenlik ayarları
                    command.add("-Dfabric.development=false");
                    command.add("-Dlog4j2.formatMsgNoLookups=true");
                    command.add("-Dorg.slf4j.simpleLogger.defaultLogLevel=WARN");
                    
                    // Launcher branding
                    command.add("-Dminecraft.launcher.brand=TerraMonic");
                    command.add("-Dminecraft.launcher.version=" + LAUNCHER_VERSION);
                    
                    // Classpath
                    command.add("-cp");
                    command.add(classpath.toString());
                    
                    // Main class - Python kodundan KnotClient
                    command.add("net.fabricmc.loader.impl.launch.knot.KnotClient");
                    
                    // Game arguments - Python kodundan
                    command.add("--username");
                    command.add(playerName);
                    command.add("--version");
                    command.add(MINECRAFT_VERSION);
                    command.add("--gameDir");
                    command.add(TERRAMONIC_PATH.toString());
                    command.add("--assetsDir");
                    command.add(TERRAMONIC_PATH.resolve("assets").toString());
                    command.add("--assetIndex");
                    command.add(MINECRAFT_VERSION);
                    command.add("--uuid");
                    command.add("00000000-0000-0000-0000-000000000000");
                    command.add("--accessToken");
                    command.add("0");
                    command.add("--userType");
                    command.add("legacy");
                    command.add("--width");
                    command.add(width);
                    command.add("--height");
                    command.add(height);

                    Platform.runLater(() -> statusLabel.setText("🎮 Minecraft başlatılıyor..."));

                    ProcessBuilder processBuilder = new ProcessBuilder(command);
                    processBuilder.directory(TERRAMONIC_PATH.toFile());
                    processBuilder.redirectErrorStream(true);

                    Process process = processBuilder.start();
                    System.out.println("🚀 Minecraft başlatıldı (Gelişmiş - Python entegre)!");
                    System.out.println("📋 Platform: " + platformName);
                    System.out.println("📋 RAM: " + ramAmount);
                    System.out.println("📋 Çözünürlük: " + width + "x" + height);

                    executorService.submit(() -> {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println("[MC] " + line);
                                
                                // Önemli log mesajlarını UI'da göster
                                if (line.contains("Setting user:") || line.contains("Backend library:") || 
                                    line.contains("Loaded") || line.contains("Starting integrated minecraft server")) {
                                    final String logLine = line;
                                    Platform.runLater(() -> {
                                        statusLabel.setText("🎮 " + logLine.substring(Math.max(0, logLine.length() - 50)));
                                    });
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Process output okuma hatası: " + e.getMessage());
                        }
                    });

                    return null;
                }
            };

            launchTask.setOnSucceeded(e -> {
                Platform.runLater(() -> {
                    gameIsLaunching = false;
                    downloadProgress.setVisible(false);
                    statusLabel.setText("✅ Minecraft başarıyla başlatıldı (Gelişmiş)!");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                    playButton.setDisable(false);

                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), ev -> {
                        statusLabel.setVisible(false);
                    }));
                    timeline.play();
                });
            });

            launchTask.setOnFailed(e -> {
                Throwable exception = e.getSource().getException();
                System.out.println("❌ Gelişmiş oyun başlatma hatası: " + exception.getMessage());
                exception.printStackTrace();
                Platform.runLater(() -> {
                    gameIsLaunching = false;
                    downloadProgress.setVisible(false);
                    playButton.setDisable(false);
                    showError("Oyun başlatılamadı (Gelişmiş): " + exception.getMessage());
                });
            });

            executorService.submit(launchTask);
        });
    }

    /**
     * ESKİ METODu YENİ ile DEĞİŞTİR
     */
    private void launchGame() {
        launchGameAdvanced(); // Gelişmiş versiyonu kullan
    }

    /**
     * Fabric kurulumunu yapar - Gelişmiş versiyon kullan
     */
    private void setupFabric() throws IOException, InterruptedException {
        setupFabricAdvanced(); // Gelişmiş versiyonu kullan
    }

    /**
     * Modrinth mod paketini indirir ve yükler
     */
    private void downloadAndInstallModrinthPack() {
        System.out.println("🔄 Modrinth pack kontrolü başlıyor (Gelişmiş)...");

        if (launcherConfig == null || !launcherConfig.has("modrinth_pack")) {
            System.out.println("❌ JSON'da modrinth_pack field'ı bulunamadı!");
            return;
        }

        String modrinthUrl = launcherConfig.getString("modrinth_pack");
        System.out.println("✅ Modrinth pack URL bulundu: " + modrinthUrl);

        // Mevcut modları kontrol et
        Path modsDir = TERRAMONIC_PATH.resolve("mods");
        boolean hasExistingMods = false;
        try {
            if (Files.exists(modsDir)) {
                long modCount = Files.list(modsDir)
                        .filter(path -> path.toString().endsWith(".jar"))
                        .count();
                hasExistingMods = modCount > 0;
                System.out.println("📦 Mevcut mod sayısı: " + modCount);
            }
        } catch (IOException e) {
            System.out.println("⚠️ Mod klasörü kontrol edilemedi: " + e.getMessage());
        }

        if (hasExistingMods) {
            System.out.println("✅ Modlar zaten mevcut, indirme atlanıyor.");
            modsReady.set(true);
            refreshModPanelUI();
            return;
        }

        modsReady.set(false);

        Task<Void> modrinthTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String modrinthUrl = launcherConfig.getString("modrinth_pack");
                System.out.println("🚀 Modrinth task başlıyor (Gelişmiş)...");
                Platform.runLater(() -> {
                    statusLabel.setText("📦 Modlar indiriliyor (Gelişmiş)...");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                });

                // .mrpack dosyasını indir
                Path mrpackPath = Files.createTempFile("terramonic_pack", ".mrpack");
                System.out.println("📁 Mrpack dosyası indiriliyor: " + mrpackPath);
                
                if (!downloadFileWithHash(modrinthUrl, mrpackPath, null, "sha1")) {
                    throw new IOException("Mrpack dosyası indirilemedi!");
                }
                
                System.out.println("✅ Mrpack dosyası indirildi!");

                Platform.runLater(() -> {
                    statusLabel.setText("📂 Mod paketi çıkarılıyor...");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                });

                // .mrpack dosyasını çıkar
                Path tempExtractDir = Files.createTempDirectory("terramonic_extract");
                if (Files.exists(tempExtractDir)) {
                    deleteDirectory(tempExtractDir);
                }
                Files.createDirectories(tempExtractDir);

                extractZip(mrpackPath, tempExtractDir);

                // modrinth.index.json'u oku
                Path indexPath = tempExtractDir.resolve("modrinth.index.json");
                if (!Files.exists(indexPath)) {
                    throw new IOException("modrinth.index.json bulunamadı!");
                }

                String indexContent = Files.readString(indexPath);
                JSONObject indexJson = new JSONObject(indexContent);

                Platform.runLater(() -> {
                    statusLabel.setText("⬬ Mod dosyaları indiriliyor (Paralel)...");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                });

                // Mods klasörünü temizle
                Path modsDir = TERRAMONIC_PATH.resolve("mods");
                if (Files.exists(modsDir)) {
                    deleteDirectory(modsDir);
                }
                Files.createDirectories(modsDir);

                // Modları paralel indir - Python kodundan
                JSONArray files = indexJson.getJSONArray("files");
                downloadModsParallel(files, modsDir);

                // Temizlik
                deleteDirectory(tempExtractDir);
                Files.deleteIfExists(mrpackPath);
                
                modsReady.set(true);
                refreshModPanelUI();

                Platform.runLater(() -> {
                    statusLabel.setText("✅ Modlar başarıyla yüklendi (Gelişmiş)!");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                });
                System.out.println("🎊 Modrinth pack kurulumu başarıyla tamamlandı (Gelişmiş)!");

                return null;
            }
        };

        modrinthTask.setOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            System.out.println("❌ Gelişmiş Modrinth pack kurulumu başarısız!");
            exception.printStackTrace();

            Platform.runLater(() -> {
                statusLabel.setText("❌ Modrinth pack kurulumu başarısız!");
                showError("Modrinth pack kurulumu başarısız:\n\n" + exception.getMessage());
            });
        });

        executorService.submit(modrinthTask);
    }

    /**
     * YENİ: Modları paralel indir - Python kodundan
     */
    private void downloadModsParallel(JSONArray files, Path modsDir) {
        System.out.println("📦 Modlar paralel indiriliyor...");
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        int modCount = 0;
        
        for (int i = 0; i < files.length(); i++) {
            JSONObject fileObj = files.getJSONObject(i);
            String filePath = fileObj.getString("path");

            if (filePath.startsWith("mods/")) {
                modCount++;
                final int currentModIndex = modCount;
                
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        JSONArray downloads = fileObj.getJSONArray("downloads");
                        if (downloads.length() > 0) {
                            String downloadUrl = downloads.getString(0);
                            String fileName = Paths.get(filePath).getFileName().toString();
                            Path targetPath = modsDir.resolve(fileName);

                            String expectedHash = fileObj.optString("hashes", null);
                            if (expectedHash != null && expectedHash.contains("sha1")) {
                                JSONObject hashes = new JSONObject(expectedHash);
                                expectedHash = hashes.optString("sha1", null);
                            }

                            Platform.runLater(() -> {
                                statusLabel.setText("📥 Mod [" + currentModIndex + "]: " + fileName);
                                statusLabel.setTextFill(PRIMARY_COLOR);
                            });
                            
                            System.out.println("📥 [" + currentModIndex + "] İndiriliyor: " + fileName);
                            
                            if (downloadFileWithHash(downloadUrl, targetPath, expectedHash, "sha1")) {
                                System.out.println("✅ [" + currentModIndex + "] İndirildi: " + fileName);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Mod indirme hatası: " + e.getMessage());
                    }
                }, executorService);
                
                futures.add(future);
            }
        }
        
        // Batch olarak bekle
        int batchSize = 5; // Daha küçük batch - modlar büyük olabilir
        for (int i = 0; i < futures.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, futures.size());
            List<CompletableFuture<Void>> batch = futures.subList(i, endIndex);
            
            CompletableFuture<Void> batchFuture = CompletableFuture.allOf(
                batch.toArray(new CompletableFuture[0])
            );
            
            try {
                batchFuture.get(5, TimeUnit.MINUTES);
                System.out.println("✅ Mod batch " + (i/batchSize + 1) + " tamamlandı");
            } catch (Exception e) {
                System.out.println("⚠️ Mod batch " + (i/batchSize + 1) + " kısmen başarısız: " + e.getMessage());
            }
        }
        
        System.out.println("🎉 Toplam " + modCount + " mod paralel olarak indirildi!");
    }

    /**
     * ZIP dosyasını çıkarır
     */
    private void extractZip(Path zipPath, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path destPath = targetDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(destPath);
                } else {
                    Files.createDirectories(destPath.getParent());
                    Files.copy(zis, destPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * YENİ: MOD KONTROL SİSTEMİ
     */
    private void checkAndRepairMods() {
        Task<Void> repairTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> statusLabel.setText("Modlar kontrol ediliyor..."));

                if (launcherConfig != null && launcherConfig.has("modrinth_pack")) {
                    Set<String> deletedMods = loadDeletedModsList();
                    String modrinthUrl = launcherConfig.getString("modrinth_pack");
                    Path mrpackPath = Files.createTempFile("terramonic_pack_check", ".mrpack");
                    
                    if (downloadFileWithHash(modrinthUrl, mrpackPath, null, "sha1")) {
                        Path tempExtractDir = Files.createTempDirectory("terramonic_extract_check");
                        extractZip(mrpackPath, tempExtractDir);

                        Path indexPath = tempExtractDir.resolve("modrinth.index.json");
                        String indexContent = Files.readString(indexPath);
                        JSONObject indexJson = new JSONObject(indexContent);

                        JSONArray files = indexJson.getJSONArray("files");
                        Path modsDir = TERRAMONIC_PATH.resolve("mods");

                        int repairedCount = 0;
                        for (int i = 0; i < files.length(); i++) {
                            JSONObject fileObj = files.getJSONObject(i);
                            String filePath = fileObj.getString("path");

                            if (filePath.startsWith("mods/")) {
                                String fileName = Paths.get(filePath).getFileName().toString();

                                if (!deletedMods.contains(fileName)) {
                                    Path targetPath = modsDir.resolve(fileName);
                                    if (!Files.exists(targetPath)) {
                                        JSONArray downloads = fileObj.getJSONArray("downloads");
                                        if (downloads.length() > 0) {
                                            String downloadUrl = downloads.getString(0);
                                            Platform.runLater(() -> statusLabel.setText("Eksik mod indiriliyor: " + fileName));
                                            
                                            if (downloadFileWithHash(downloadUrl, targetPath, null, "sha1")) {
                                                repairedCount++;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        deleteDirectory(tempExtractDir);
                        Files.deleteIfExists(mrpackPath);

                        final int finalCount = repairedCount;
                        Platform.runLater(() -> {
                            if (finalCount > 0) {
                                showInfo("Mod kontrolü tamamlandı! " + finalCount + " mod onarıldı.");
                            } else {
                                showInfo("Mod kontrolü tamamlandı! Tüm modlar mevcut.");
                            }
                        });
                    }
                }
                return null;
            }
        };

        repairTask.setOnFailed(event -> {
            Platform.runLater(() -> showError("Mod kontrolü başarısız: " + event.getSource().getException().getMessage()));
        });

        executorService.submit(repairTask);
    }

    private void clearLauncherCache() {
        Task<Void> clearTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> statusLabel.setText("Cache temizleniyor..."));

                try {
                    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
                    Files.list(tempDir)
                            .filter(path -> path.getFileName().toString().startsWith("terramonic"))
                            .forEach(path -> {
                                try {
                                    if (Files.isDirectory(path)) {
                                        deleteDirectory(path);
                                    } else {
                                        Files.deleteIfExists(path);
                                    }
                                } catch (IOException e) {
                                    System.out.println("Geçici dosya silinemedi: " + path);
                                }
                            });
                } catch (IOException e) {
                    System.out.println("Temp klasörü temizlenemedi: " + e.getMessage());
                }

                Platform.runLater(() -> showInfo("Cache başarıyla temizlendi!"));
                return null;
            }
        };

        executorService.submit(clearTask);
    }

    private Set<String> loadDeletedModsList() {
        Set<String> deletedMods = new HashSet<>();
        try {
            if (Files.exists(DELETED_MODS_FILE)) {
                String content = Files.readString(DELETED_MODS_FILE);
                JSONArray deletedArray = new JSONArray(content);
                for (int i = 0; i < deletedArray.length(); i++) {
                    deletedMods.add(deletedArray.getString(i));
                }
            }
        } catch (Exception e) {
            System.out.println("Silinmiş modlar listesi yüklenemedi: " + e.getMessage());
        }
        return deletedMods;
    }

    private void saveDeletedModsList(Set<String> deletedMods) {
        try {
            JSONArray deletedArray = new JSONArray();
            for (String mod : deletedMods) {
                deletedArray.put(mod);
            }
            Files.writeString(DELETED_MODS_FILE, deletedArray.toString());
        } catch (Exception e) {
            System.out.println("Silinmiş modlar listesi kaydedilemedi: " + e.getMessage());
        }
    }

    /**
     * Splash ekranını gösterir
     */
    private void showSplashScreen() {
        final ImageView logoView = new ImageView();
        if (launcherIcon != null && !launcherIcon.isError()) {
            logoView.setImage(launcherIcon);
        }

        logoView.setFitWidth(240);
        logoView.setFitHeight(240);
        logoView.setPreserveRatio(true);

        Glow glow = new Glow();
        glow.setLevel(0.3);
        logoView.setEffect(glow);

        // Bakım modu kontrolü
        try {
            if (launcherConfig != null && launcherConfig.optBoolean("bakimmmodu", false)) {
                final Label maintenanceMessage = new Label("Şuan Bakım Modundayız.");
                maintenanceMessage.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
                maintenanceMessage.setTextFill(PRIMARY_COLOR);

                String maintenanceReason = launcherConfig.optString("bakimmmodusebebi", "Sebep belirtilmemiş.");
                final Label reasonLabel = new Label(maintenanceReason);
                reasonLabel.setFont(Font.font(FONT_FAMILY, FontWeight.LIGHT, 18));
                reasonLabel.setTextFill(TEXT_SECONDARY);

                final VBox centerContent = new VBox(20);
                centerContent.setAlignment(Pos.CENTER);
                centerContent.getChildren().addAll(logoView, maintenanceMessage, reasonLabel);

                final AnchorPane decorPane = createDecorativeBackground();

                final StackPane root = new StackPane();
                root.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
                root.getChildren().addAll(decorPane, centerContent);

                final Scene maintenanceScene = new Scene(root, windowWidth, windowHeight);
                maintenanceScene.setFill(BACKGROUND_COLOR);
                mainStage.setScene(maintenanceScene);
                currentScene = maintenanceScene;
                return;
            }
        } catch (Exception e) {
            System.out.println("Bakım modu kontrolü hatası: " + e.getMessage());
        }

        // Normal splash screen
        final Label subtitle = new Label("TerraMonic Launcher " + currentLauncherVersion);
        subtitle.setFont(Font.font(FONT_FAMILY, FontWeight.LIGHT, 24));
        subtitle.setTextFill(PRIMARY_COLOR);

        final ProgressBar loadingBar = new ProgressBar(0);
        loadingBar.setPrefWidth(300);
        loadingBar.setStyle("-fx-accent: " + toHexString(PRIMARY_COLOR) + ";");

        final Label loadingLabel = new Label("Başlatılıyor...");
        loadingLabel.setTextFill(TEXT_SECONDARY);
        loadingLabel.setFont(Font.font(FONT_FAMILY, 14));

        final VBox centerContent = new VBox(30);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(logoView, subtitle, new VBox(20), loadingBar, loadingLabel);

        final AnchorPane decorPane = createDecorativeBackground();

        final StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        root.getChildren().addAll(decorPane, centerContent);

        final Scene splashScene = new Scene(root, windowWidth, windowHeight);
        splashScene.setFill(BACKGROUND_COLOR);
        mainStage.setScene(splashScene);
        currentScene = splashScene;

        // Setup task - Python entegrasyonu ile geliştirilmiş
        Task<Void> setupTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Version kontrolü
                String localVersion = currentLauncherVersion.replace("v", "");
                if (!Files.exists(LAUNCHER_VERSION_JSON)) {
                    JSONObject versionJson = new JSONObject();
                    versionJson.put("current_version", localVersion);
                    Files.writeString(LAUNCHER_VERSION_JSON, versionJson.toString());
                } else {
                    String content = Files.readString(LAUNCHER_VERSION_JSON);
                    JSONObject localJson = new JSONObject(content);
                    localVersion = localJson.getString("current_version");
                }

                Platform.runLater(() -> loadingLabel.setText("Versiyon kontrol ediliyor..."));

                if (launcherConfig != null) {
                    final String finalRemoteVersion = launcherConfig.getString("version");

                    if (!localVersion.equals(finalRemoteVersion)) {
                        Platform.runLater(() -> loadingLabel.setText("Güncelleme tespit edildi: v" + finalRemoteVersion));

                        JSONObject updatedVersionJson = new JSONObject();
                        updatedVersionJson.put("current_version", finalRemoteVersion);
                        Files.writeString(LAUNCHER_VERSION_JSON, updatedVersionJson.toString());

                        currentLauncherVersion = "v" + finalRemoteVersion;
                        Platform.runLater(() -> subtitle.setText("TerraMonic Launcher v" + finalRemoteVersion));

                        clearConfigAndMods();
                    }
                }

                Platform.runLater(() -> loadingLabel.setText("Dosyalar kontrol ediliyor..."));

                // ESKİ SİSTEM KORUNDU
                if (launcherConfig != null && launcherConfig.has("dosyalar")) {
                    String zipUrl = launcherConfig.getString("dosyalar");
                    if (!zipUrl.contains("placeholder")) {
                        downloadAndExtractZip(zipUrl, TERRAMONIC_PATH);
                    }
                }

                Platform.runLater(() -> loadingLabel.setText("Fabric kurulumu kontrol ediliyor..."));

                try {
                    setupFabric();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Fabric kurulumu kesildi", ie);
                }

                Platform.runLater(() -> loadingLabel.setText("Modrinth modları kontrol ediliyor..."));
                downloadAndInstallModrinthPack();

                Platform.runLater(() -> loadingLabel.setText("Minecraft dosyaları indiriliyor..."));
                downloadMinecraftLibrariesAdvanced();

                return null;
            }
        };

        setupTask.setOnSucceeded(e -> {
            final Timeline loadingAnimation = new Timeline();
            KeyFrame[] keyFrames = new KeyFrame[11];
            for (int i = 0; i <= 10; i++) {
                final double progress = i / 10.0;
                keyFrames[i] = new KeyFrame(Duration.seconds(i * 0.2), event -> {
                    loadingBar.setProgress(progress);

                    if (progress < 0.3) {
                        loadingLabel.setText("Bileşenler kontrol ediliyor...");
                    } else if (progress < 0.6) {
                        loadingLabel.setText("Güncellemeler kontrol ediliyor...");
                    } else if (progress < 0.9) {
                        loadingLabel.setText("Launcher hazırlanıyor...");
                    } else {
                        loadingLabel.setText("Tamamlandı!");
                    }
                });
            }
            loadingAnimation.getKeyFrames().addAll(keyFrames);

            final ScaleTransition scaleAnimation = new ScaleTransition(Duration.seconds(2), logoView);
            scaleAnimation.setFromX(0.9);
            scaleAnimation.setFromY(0.9);
            scaleAnimation.setToX(1.0);
            scaleAnimation.setToY(1.0);
            scaleAnimation.setCycleCount(Animation.INDEFINITE);
            scaleAnimation.setAutoReverse(true);
            scaleAnimation.play();

            final FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), centerContent);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setCycleCount(1);
            fadeIn.play();

            loadingAnimation.setOnFinished(event -> {
                scaleAnimation.stop();
                final FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.8), centerContent);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setCycleCount(1);
                fadeOut.setOnFinished(evt -> showLoginScreen());
                fadeOut.play();
            });

            loadingAnimation.play();
        });

        setupTask.setOnFailed(e -> {
            Throwable exception = e.getSource().getException();
            System.out.println("❌ Setup task başarısız!");
            exception.printStackTrace();

            Platform.runLater(() -> {
                showError("Kurulum başarısız:\n\n" + exception.getClass().getSimpleName() + ": " + exception.getMessage());
                loadingLabel.setText("Hata oluştu!");
            });
        });

        executorService.submit(setupTask);
    }

    /**
     * ESKİ SİSTEM KORUNDU
     */
    private void downloadAndExtractZip(String zipUrl, Path targetDir) throws IOException {
        Path ekdosyalarZip = TERRAMONIC_PATH.resolve("ekdosyalar.zip");
        Path nestedZip = TERRAMONIC_PATH.resolve(generateComplexZipName());

        boolean isNestedZipValid = Files.exists(nestedZip) && isValidZip(nestedZip);

        if (!Files.exists(ekdosyalarZip) || !isValidZip(ekdosyalarZip)) {
            Platform.runLater(() -> { if(statusLabel!=null) statusLabel.setText("ekdosyalar.zip indiriliyor..."); });
            try (InputStream in = new URL(zipUrl).openStream();
                 OutputStream out = Files.newOutputStream(ekdosyalarZip)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        }

        if (!isNestedZipValid) {
            Files.createDirectories(nestedZip.getParent());
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(nestedZip))) {
                ZipEntry entry = new ZipEntry("ekdosyalar.zip");
                zos.putNextEntry(entry);
                Files.copy(ekdosyalarZip, zos);
                zos.closeEntry();
            }
        }

        checkAndExtractMissingFiles(nestedZip, targetDir);
    }

    private boolean isValidZip(Path zipPath) {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            return zis.getNextEntry() != null;
        } catch (IOException e) {
            return false;
        }
    }

    private void checkAndExtractMissingFiles(Path nestedZip, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(nestedZip))) {
            ZipEntry nestedEntry;
            while ((nestedEntry = zis.getNextEntry()) != null) {
                if (nestedEntry.getName().equals("ekdosyalar.zip")) {
                    Path tempEkdosyalar = Files.createTempFile("temp_ekdosyalar", ".zip");
                    try (OutputStream out = Files.newOutputStream(tempEkdosyalar)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }

                    try (ZipInputStream innerZis = new ZipInputStream(Files.newInputStream(tempEkdosyalar))) {
                        ZipEntry innerEntry;
                        while ((innerEntry = innerZis.getNextEntry()) != null) {
                            Path destPath = targetDir.resolve(innerEntry.getName());
                            if (!Files.exists(destPath)) {
                                if (innerEntry.isDirectory()) {
                                    Files.createDirectories(destPath);
                                } else {
                                    Files.createDirectories(destPath.getParent());
                                    String fileName = innerEntry.getName();
                                    Platform.runLater(() -> statusLabel.setText("Dosya çıkarılıyor: " + fileName));
                                    Files.copy(innerZis, destPath, StandardCopyOption.REPLACE_EXISTING);
                                }
                            }
                            innerZis.closeEntry();
                        }
                    }
                    Files.delete(tempEkdosyalar);
                }
                zis.closeEntry();
            }
        }
    }

    private String generateComplexZipName() {
        StringBuilder complexName = new StringBuilder("TERRAMONIC_");
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();

        Consumer<Integer> addRandomSegment = length -> {
            for (int i = 0; i < length; i++) {
                complexName.append(characters.charAt(random.nextInt(characters.length())));
            }
        };

        addRandomSegment.accept(10);
        complexName.append("_LAUNCHER_");
        addRandomSegment.accept(8);
        complexName.append("_B0K_");
        addRandomSegment.accept(12);
        complexName.append("_ACARSIN_");
        addRandomSegment.accept(10);
        complexName.append("_SEN_");
        addRandomSegment.accept(8);
        complexName.append("_BU_JARI_");
        addRandomSegment.accept(10);

        return complexName.toString() + ".zip";
    }

    /**
     * Dekoratif arka plan oluşturur
     */
    private AnchorPane createDecorativeBackground() {
        AnchorPane decorPane = new AnchorPane();

        int lineCount = 20;
        for (int i = 0; i < lineCount; i++) {
            Line hLine = new Line(0, (windowHeight / lineCount) * i, windowWidth, (windowHeight / lineCount) * i);
            hLine.setStroke(Color.web("#222222", 0.3));
            hLine.setStrokeWidth(0.5);

            Line vLine = new Line((windowWidth / lineCount) * i, 0, (windowWidth / lineCount) * i, windowHeight);
            vLine.setStroke(Color.web("#222222", 0.3));
            vLine.setStrokeWidth(0.5);

            decorPane.getChildren().addAll(hLine, vLine);
        }

        Line accentLine1 = new Line(0, windowHeight * 0.2, windowWidth, windowHeight * 0.8);
        accentLine1.setStroke(PRIMARY_COLOR.deriveColor(1, 1, 1, 0.05));
        accentLine1.setStrokeWidth(2);

        Line accentLine2 = new Line(0, windowHeight * 0.8, windowWidth, windowHeight * 0.2);
        accentLine2.setStroke(PRIMARY_COLOR.deriveColor(1, 1, 1, 0.03));
        accentLine2.setStrokeWidth(2);

        decorPane.getChildren().addAll(accentLine1, accentLine2);

        return decorPane;
    }

    /**
     * Login ekranını gösterir
     */
    private void showLoginScreen() {
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        AnchorPane decorPane = createDecorativeBackground();

        VBox leftPanel = new VBox();
        leftPanel.setPadding(new Insets(50, 30, 50, 50));
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setMinWidth(windowWidth * 0.5);

        ImageView logoView = new ImageView();
        if (launcherIcon != null && !launcherIcon.isError()) {
            logoView.setImage(launcherIcon);
            logoView.setFitWidth(180);
            logoView.setFitHeight(180);
            logoView.setPreserveRatio(true);
        } else {
            Text logoText = new Text("TerraMonic");
            logoText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 60));
            logoText.setFill(PRIMARY_COLOR);
            Glow glow = new Glow();
            glow.setLevel(0.6);
            logoText.setEffect(glow);
            leftPanel.getChildren().add(logoText);
        }

        Label sloganLabel = new Label("Minecraft Deneyimini\nYeniden Keşfet");
        sloganLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 32));
        sloganLabel.setTextFill(TEXT_COLOR);
        sloganLabel.setWrapText(true);

        Label subSloganLabel = new Label("TerraMonic sunucularına özel, optimize edilmiş launcher ile\noyun deneyimini maksimuma çıkar.");
        subSloganLabel.setFont(Font.font(FONT_FAMILY, 16));
        subSloganLabel.setTextFill(TEXT_SECONDARY);
        subSloganLabel.setWrapText(true);

        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        VBox onlineStats = createStatBox("127", "Çevrimiçi Oyuncu");
        VBox serverStats = createStatBox("AÇIK", "Sunucu Durumu");
        serverStats.getChildren().get(0).setStyle("-fx-text-fill: " + toHexString(PRIMARY_COLOR) + ";");

        statsBox.getChildren().addAll(onlineStats, serverStats);

        leftPanel.getChildren().addAll(logoView, new VBox(20), sloganLabel, new VBox(10),
                subSloganLabel, new VBox(40), statsBox);

        VBox rightPanel = new VBox(20);
        rightPanel.setPadding(new Insets(50));
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setMinWidth(windowWidth * 0.5);
        rightPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_SECONDARY) + ";");

        Label loginTitle = new Label("TerraMonic'e Hoşgeldiniz");
        loginTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 28));
        loginTitle.setTextFill(PRIMARY_COLOR);

        TextField usernameField = createStyledTextField("Minecraft kullanıcı adınız");

        CheckBox rememberBox = new CheckBox("Kullanıcı adımı hatırla");
        rememberBox.setFont(Font.font(FONT_FAMILY, 14));
        rememberBox.setTextFill(TEXT_COLOR);
        rememberBox.setSelected(rememberUser);

        Button loginButton = createStyledButton("GİRİŞ YAP", 200, 50);

        loginButton.setOnAction(e -> {
            playerName = usernameField.getText().trim();
            rememberUser = rememberBox.isSelected();

            if (playerName.isEmpty()) {
                shakeNode(usernameField);
                usernameField.setStyle(usernameField.getStyle() + "-fx-border-color: #FF3A3A;");
                return;
            }

            playButtonClickAnimation(loginButton);
            transitionToMainScreen();
        });

        Label versionLabel = new Label("TerraMonic Launcher " + currentLauncherVersion);
        versionLabel.setFont(Font.font(FONT_FAMILY, 12));
        versionLabel.setTextFill(TEXT_SECONDARY);

        HBox socialLinks = new HBox(15);
        socialLinks.setAlignment(Pos.CENTER);

        String[] socialIcons = {"discord", "youtube", "instagram"};
        for (String social : socialIcons) {
            Circle socialCircle = new Circle(20);
            socialCircle.setFill(Color.web("#222222"));
            socialCircle.setStroke(PRIMARY_COLOR);
            socialCircle.setStrokeWidth(1.5);

            Label socialLabel = new Label(social.substring(0, 1).toUpperCase());
            socialLabel.setTextFill(PRIMARY_COLOR);
            socialLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));

            StackPane socialStack = new StackPane(socialCircle, socialLabel);
            socialStack.setCursor(Cursor.HAND);

            socialStack.setOnMouseEntered(event -> {
                socialCircle.setFill(PRIMARY_COLOR);
                socialLabel.setTextFill(Color.BLACK);
            });

            socialStack.setOnMouseExited(event -> {
                socialCircle.setFill(Color.web("#222222"));
                socialLabel.setTextFill(PRIMARY_COLOR);
            });

            socialLinks.getChildren().add(socialStack);
        }

        rightPanel.getChildren().addAll(
                loginTitle,
                new VBox(30),
                usernameField,
                rememberBox,
                new VBox(20),
                loginButton,
                new Region() {{
                    setVgrow(this, Priority.ALWAYS);
                }},
                socialLinks,
                versionLabel
        );

        StackPane mainContent = new StackPane();
        mainContent.getChildren().addAll(decorPane);

        HBox contentBox = new HBox();
        contentBox.getChildren().addAll(leftPanel, rightPanel);
        mainContent.getChildren().add(contentBox);

        setupWindowControls(mainContent);

        VBox rootWithTitleBar = new VBox();
        rootWithTitleBar.getChildren().addAll(createTitleBar(), mainContent);
        root.setCenter(rootWithTitleBar);

        Scene loginScene = new Scene(root, windowWidth, windowHeight);
        loginScene.setFill(BACKGROUND_COLOR);

        transitionToScene(loginScene);
    }

    /**
     * Ana ekrana geçiş yapar
     */
    private void transitionToMainScreen() {
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        VBox rootWithTitleBar = new VBox();
        rootWithTitleBar.getChildren().add(createTitleBar());

        BorderPane mainContent = new BorderPane();

        VBox leftNav = createNavigationPanel(root);

        centerPanel = new StackPane();
        centerPanel.setBackground(new Background(new BackgroundFill(BACKGROUND_SECONDARY, CornerRadii.EMPTY, Insets.EMPTY)));
        centerPanel.setPadding(new Insets(20));

        centerPanel.setPrefHeight(windowHeight - 100);
        centerPanel.setMinHeight(windowHeight - 100);
        centerPanel.setMaxHeight(windowHeight - 100);

        ScrollPane newsPanel = createNewsPanel();
        centerPanel.getChildren().add(newsPanel);

        VBox rightPanel = createProfilePanel();

        mainContent.setLeft(leftNav);
        mainContent.setCenter(centerPanel);
        mainContent.setRight(rightPanel);

        HBox bottomPanel = createBottomPanel();
        mainContent.setBottom(bottomPanel);

        rootWithTitleBar.getChildren().add(mainContent);
        root.setCenter(rootWithTitleBar);

        Scene mainScene = new Scene(root, windowWidth, windowHeight);
        mainScene.setFill(BACKGROUND_COLOR);

        transitionToScene(mainScene);
    }

    /**
     * Sol navigasyon panelini oluşturur
     */
    private VBox createNavigationPanel(BorderPane root) {
        VBox navPanel = new VBox(10);
        navPanel.setPadding(new Insets(20, 15, 20, 15));
        navPanel.setPrefWidth(200);
        navPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        String[] navItems = {"Ana Sayfa", "Mod Paketleri", "Hesap", "Ayarlar"};
        String[] navIcons = {"🏠", "📦", "👤", "⚙"};

        final int[] selectedIndex = {0};
        currentNavIndex = 0;

        HBox logoBox = null;

        if (launcherIcon != null && !launcherIcon.isError()) {
            ImageView logoView = new ImageView(launcherIcon);
            logoView.setFitWidth(60);
            logoView.setFitHeight(60);
            logoView.setPreserveRatio(true);

            Label logoTitle = new Label("TerraMonic");
            logoTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
            logoTitle.setTextFill(PRIMARY_COLOR);

            logoBox = new HBox(10);
            logoBox.setAlignment(Pos.CENTER_LEFT);
            logoBox.getChildren().addAll(logoView, logoTitle);

            navPanel.getChildren().add(logoBox);
            navPanel.getChildren().add(new Separator());
        }

        List<HBox> menuItems = new ArrayList<>();

        final HBox finalLogoBox = logoBox;
        for (int i = 0; i < navItems.length; i++) {
            final int index = i;

            HBox navItem = new HBox(10);
            navItem.setPadding(new Insets(10, 15, 10, 15));
            navItem.setAlignment(Pos.CENTER_LEFT);
            navItem.setCursor(Cursor.HAND);

            Label iconLabel = new Label(navIcons[i]);
            iconLabel.setFont(Font.font(FONT_FAMILY, 16));

            Label textLabel = new Label(navItems[i]);
            textLabel.setFont(Font.font(FONT_FAMILY, 14));

            navItem.getChildren().addAll(iconLabel, textLabel);

            menuItems.add(navItem);

            navPanel.getChildren().add(navItem);
        }

        Consumer<Integer> updateMenuItemStyles = (hoveredIndex) -> {
            for (int i = 0; i < menuItems.size(); i++) {
                HBox item = menuItems.get(i);
                Label itemIcon = (Label) item.getChildren().get(0);
                Label itemText = (Label) item.getChildren().get(1);

                if (i == selectedIndex[0]) {
                    itemIcon.setTextFill(PRIMARY_COLOR);
                    itemText.setTextFill(PRIMARY_COLOR);
                    item.setStyle("-fx-background-color: #1A1A1A; -fx-background-radius: 5px;");
                } else if (i == hoveredIndex) {
                    itemIcon.setTextFill(PRIMARY_COLOR);
                    itemText.setTextFill(PRIMARY_COLOR);
                    item.setStyle("-fx-background-color: #1A1A1A; -fx-background-radius: 5px;");
                } else {
                    itemIcon.setTextFill(TEXT_SECONDARY);
                    itemText.setTextFill(TEXT_SECONDARY);
                    item.setStyle("-fx-background-color: transparent;");
                }
            }
        };

        updateMenuItemStyles.accept(-1);

        for (int i = 0; i < menuItems.size(); i++) {
            final int index = i;
            HBox navItem = menuItems.get(i);

            navItem.setOnMouseEntered(e -> {
                updateMenuItemStyles.accept(index);
            });

            navItem.setOnMouseExited(e -> {
                updateMenuItemStyles.accept(-1);
            });

            navItem.setOnMousePressed(e -> {
                navItem.setStyle("-fx-background-color: #151515; -fx-background-radius: 5px;");
            });

            navItem.setOnMouseReleased(e -> {
                selectedIndex[0] = index;
                currentNavIndex = index;
                updateMenuItemStyles.accept(-1);

                StackPane centerPanel = (StackPane) ((BorderPane) ((VBox) root.getCenter()).getChildren().get(1)).getCenter();
                centerPanel.getChildren().clear();
                switch (navItems[index]) {
                    case "Ana Sayfa":
                        centerPanel.getChildren().add(createNewsPanel());
                        break;
                    case "Mod Paketleri":
                        if (modsReady.get()) {
                            centerPanel.getChildren().add(createModManagementPanel());
                        } else {
                            Label waitLbl = new Label("Modlar hazırlanıyor, biraz bekleyin...");
                            waitLbl.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
                            waitLbl.setTextFill(PRIMARY_COLOR);
                            waitLbl.setId("waitMods");
                            centerPanel.getChildren().add(waitLbl);

                            modsReady.addListener((obs, oldV, newV) -> {
                                if (newV) {
                                    refreshModPanelUI();
                                }
                            });
                        }
                        break;
                    case "Hesap":
                        centerPanel.getChildren().add(createAccountPanel());
                        break;
                    case "Ayarlar":
                        centerPanel.getChildren().add(createSettingsPanel());
                        break;
                }
            });
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        navPanel.getChildren().add(spacer);

        Label versionLabel = new Label("TerraMonic " + currentLauncherVersion);
        versionLabel.setFont(Font.font(FONT_FAMILY, 12));
        versionLabel.setTextFill(TEXT_SECONDARY);

        Label mcVersionLabel = new Label("Minecraft " + MINECRAFT_VERSION + " + Fabric " + FABRIC_VERSION);
        mcVersionLabel.setFont(Font.font(FONT_FAMILY, 12));
        mcVersionLabel.setTextFill(TEXT_SECONDARY);

        VBox versionBox = new VBox(5);
        versionBox.getChildren().addAll(versionLabel, mcVersionLabel);
        navPanel.getChildren().add(versionBox);

        return navPanel;
    }

    /**
     * Alt paneli oluşturur
     */
    private HBox createBottomPanel() {
        HBox bottomPanel = new HBox();
        bottomPanel.setPadding(new Insets(15, 25, 15, 25));
        bottomPanel.setAlignment(Pos.CENTER_LEFT);
        bottomPanel.setSpacing(20);
        bottomPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");
        bottomPanel.setPrefHeight(60);
        bottomPanel.setMinHeight(60);
        bottomPanel.setMaxHeight(60);
        bottomPanel.setPrefWidth(windowWidth);
        bottomPanel.setMinWidth(windowWidth);
        bottomPanel.setMaxWidth(windowWidth);

        addSystemTrayIcon();

        playButton = createStyledButton("🚀 OYUNU BAŞLAT", 220, 45);
        playButton.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        playButton.setAlignment(Pos.CENTER);
        playButton.setTranslateX(-20);

        downloadProgress = new ProgressBar(0);
        downloadProgress.setPrefWidth(200);
        downloadProgress.setStyle("-fx-accent: " + toHexString(PRIMARY_COLOR) + ";");
        downloadProgress.setVisible(false);

        statusLabel.setFont(Font.font(FONT_FAMILY, 14));
        statusLabel.setTextFill(TEXT_SECONDARY);
        statusLabel.setVisible(false);

        playButton.setOnAction(e -> launchGame());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bottomPanel.getChildren().addAll(
                playButton,
                new VBox(5, downloadProgress, statusLabel),
                spacer
        );

        return bottomPanel;
    }

    // Remaining UI components and utility methods
    private ScrollPane createNewsPanel() {
        VBox newsContainer = new VBox(20);
        newsContainer.setPadding(new Insets(20));

        Label newsTitle = new Label("📰 HABERLER & DUYURULAR");
        newsTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        newsTitle.setTextFill(PRIMARY_COLOR);

        newsContainer.getChildren().add(newsTitle);
        newsContainer.getChildren().add(new Separator());

        VBox newsListContainer = new VBox(15);

        for (NewsItem news : newsList) {
            VBox newsCard = createNewsCard(news);
            newsListContainer.getChildren().add(newsCard);
        }

        newsContainer.getChildren().add(newsListContainer);

        HBox bottomLink = new HBox();
        bottomLink.setAlignment(Pos.CENTER);
        bottomLink.setPadding(new Insets(30, 0, 20, 0));

        Hyperlink websiteLink = new Hyperlink("TerraMonic Web Sitesini Ziyaret Et");
        websiteLink.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        websiteLink.setTextFill(PRIMARY_COLOR);
        websiteLink.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI(TERRAMONIC_URL));
            } catch (Exception ex) {
                System.out.println("Web sitesi açılamadı: " + ex.getMessage());
            }
        });

        bottomLink.getChildren().add(websiteLink);
        newsContainer.getChildren().add(bottomLink);

        ScrollPane scrollPane = new ScrollPane(newsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent;-fx-background-color: transparent;-fx-padding: 0;-fx-vbar-policy: never; -fx-vbar-visible: false;");
        scrollPane.getStyleClass().add("news-scroll");

        return scrollPane;
    }

    private VBox createModManagementPanel() {
        VBox modPanel = new VBox(20);
        modPanel.setPadding(new Insets(20));
        modPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_SECONDARY) + ";");

        Label title = new Label("MOD YÖNETİMİ");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(PRIMARY_COLOR);

        ListView<String> modListView = new ListView<>();
        modListView.setPrefHeight(300);
        VBox.setVgrow(modListView, Priority.ALWAYS);
        modListView.setStyle(
                "-fx-background-color: #1A1A1A;" +
                        "-fx-text-fill: white;" +
                        "-fx-control-inner-background: #1A1A1A;"
        );

        loadMods(modListView);

        Button resetModButton = createStyledButton("MODLARI SIFIRLA", 150, 40);
        resetModButton.setOnAction(e -> {
            if (gameIsLaunching) {
                showError("Oyun başlatılırken modlar sıfırlanamaz!");
                return;
            }

            try {
                deleteDirectory(TERRAMONIC_PATH.resolve("mods"));
                Files.createDirectories(TERRAMONIC_PATH.resolve("mods"));
                Files.deleteIfExists(DELETED_MODS_FILE);
                loadMods(modListView);
                showInfo("Modlar sıfırlandı!");
            } catch (IOException ex) {
                showError("Modlar sıfırlanamadı: " + ex.getMessage());
            }
        });

        Button removeModButton = createStyledButton("MOD KALDIR", 150, 40);
        removeModButton.setOnAction(e -> {
            if (gameIsLaunching) {
                showError("Oyun başlatılırken mod kaldırılamaz!");
                return;
            }

            String selectedMod = modListView.getSelectionModel().getSelectedItem();
            if (selectedMod != null) {
                try {
                    Set<String> deletedMods = loadDeletedModsList();
                    deletedMods.add(selectedMod);
                    saveDeletedModsList(deletedMods);

                    Files.deleteIfExists(TERRAMONIC_PATH.resolve("mods").resolve(selectedMod));
                    modListView.getItems().remove(selectedMod);
                    showInfo("Mod kaldırıldı: " + selectedMod);
                } catch (IOException ex) {
                    showError("Mod kaldırılamadı: " + ex.getMessage());
                }
            } else {
                showError("Lütfen kaldırılacak bir mod seçin.");
            }
        });

        TextField profileNameField = createStyledTextField("Profil adı girin");
        Button saveProfileButton = createStyledButton("PROFİLİ KAYDET", 150, 40);
        saveProfileButton.setOnAction(e -> {
            if (gameIsLaunching) {
                showError("Oyun başlatılırken profil kaydedilemez!");
                return;
            }

            String profileName = profileNameField.getText().trim();
            if (profileName.isEmpty()) {
                shakeNode(profileNameField);
                profileNameField.setStyle(profileNameField.getStyle() + "-fx-border-color: #FF3A3A;");
                return;
            }
            saveModProfile(profileName);
            profileNameField.clear();
        });

        ComboBox<String> profileComboBox = new ComboBox<>();
        loadProfileList(profileComboBox);
        profileComboBox.setStyle(
                "-fx-background-color: #222222;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: white;" +
                        "-fx-border-color: #333333;"
        );

        Button loadProfileButton = createStyledButton("PROFİLİ YÜKLE", 150, 40);
        loadProfileButton.setOnAction(e -> {
            if (gameIsLaunching) {
                showError("Oyun başlatılırken profil yüklenemez!");
                return;
            }

            String selectedProfile = profileComboBox.getSelectionModel().getSelectedItem();
            if (selectedProfile != null) {
                loadModProfile(selectedProfile);
                loadMods(modListView);
            } else {
                showError("Lütfen yüklenecek bir profil seçin.");
            }
        });

        HBox modButtons = new HBox(20, resetModButton, removeModButton);
        modButtons.setAlignment(Pos.CENTER_LEFT);

        HBox profileButtons = new HBox(20, profileNameField, saveProfileButton);
        profileButtons.setAlignment(Pos.CENTER_LEFT);

        HBox loadProfileBox = new HBox(20, profileComboBox, loadProfileButton);
        loadProfileBox.setAlignment(Pos.CENTER_LEFT);

        modPanel.getChildren().addAll(
                title,
                new Separator(),
                modListView,
                modButtons,
                profileButtons,
                loadProfileBox
        );

        return modPanel;
    }

    private VBox createAccountPanel() {
        VBox accountPanel = new VBox(20);
        accountPanel.setPadding(new Insets(20));
        accountPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_SECONDARY) + ";");

        Label title = new Label("HESAP");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(PRIMARY_COLOR);

        Button logoutButton = createStyledButton("ÇIKIŞ YAP", 150, 40);
        logoutButton.setOnAction(e -> {
            if (gameIsLaunching) {
                showError("Oyun başlatılırken çıkış yapılamaz!");
                return;
            }

            playerName = "";
            showLoginScreen();
        });

        accountPanel.getChildren().addAll(title, new Separator(), logoutButton);
        return accountPanel;
    }

    private VBox createSettingsPanel() {
        VBox settingsPanel = new VBox(20);
        settingsPanel.setPadding(new Insets(20));
        settingsPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_SECONDARY) + ";");

        Label title = new Label("AYARLAR");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(PRIMARY_COLOR);

        Label gameSettingsTitle = new Label("Oyun Ayarları");
        gameSettingsTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        gameSettingsTitle.setTextFill(TEXT_COLOR);

        Label ramLabel = new Label("RAM Miktarı:");
        ramLabel.setFont(Font.font(FONT_FAMILY, 14));
        ramLabel.setTextFill(TEXT_COLOR);

        long totalMem = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
        long gb = 1024L * 1024L * 1024L;
        long maxRamGb = Math.max(2, Math.min(16, totalMem / gb / 2));

        List<String> ramOptionsDyn = new ArrayList<>();
        for (int g = 2; g <= maxRamGb; g += (g >= 8 ? 4 : 2)) {
            ramOptionsDyn.add(g + " GB");
        }

        ramCombo = new ComboBox<>();
        ramCombo.getItems().addAll(ramOptionsDyn);
        ramCombo.getSelectionModel().select(Math.min(1, ramOptionsDyn.size() - 1));
        ramCombo.setPrefWidth(150);
        ramCombo.setStyle(
                "-fx-background-color: #222222;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: white;" +
                        "-fx-border-color: #333333;"
        );

        HBox ramBox = new HBox(10);
        ramBox.setAlignment(Pos.CENTER_LEFT);
        ramBox.getChildren().addAll(ramLabel, ramCombo);

        Label resLabel = new Label("Çözünürlük:");
        resLabel.setFont(Font.font(FONT_FAMILY, 14));
        resLabel.setTextFill(TEXT_COLOR);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        int scrW = (int) screenBounds.getWidth();
        int scrH = (int) screenBounds.getHeight();

        List<String> resList = Arrays.asList("1280x720", "1600x900", "1920x1080", "2560x1440", "3840x2160");
        List<String> availableRes = new ArrayList<>();
        for (String r : resList) {
            String[] sp = r.split("x");
            int w = Integer.parseInt(sp[0]);
            int h = Integer.parseInt(sp[1]);
            if (w <= scrW && h <= scrH) availableRes.add(r);
        }

        resCombo = new ComboBox<>();
        resCombo.getItems().addAll(availableRes);
        resCombo.getSelectionModel().select(Math.max(0, availableRes.size() - 1));
        resCombo.setPrefWidth(150);
        resCombo.setStyle(
                "-fx-background-color: #222222;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: white;" +
                        "-fx-border-color: #333333;"
        );

        HBox resolutionBox = new HBox(10);
        resolutionBox.setAlignment(Pos.CENTER_LEFT);
        resolutionBox.getChildren().addAll(resLabel, resCombo);

        Label launcherSettingsTitle = new Label("Launcher Ayarları");
        launcherSettingsTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        launcherSettingsTitle.setTextFill(TEXT_COLOR);

        Button checkModsButton = createStyledButton("MODLARI KONTROL ET", 200, 40);
        checkModsButton.setOnAction(e -> {
            if (!gameIsLaunching) {
                checkAndRepairMods();
            }
        });

        Button clearCacheButton = createStyledButton("CACHE TEMİZLE", 200, 40);
        clearCacheButton.setOnAction(e -> {
            if (!gameIsLaunching) {
                clearLauncherCache();
            }
        });

        HBox buttonBox = new HBox(15);
        buttonBox.getChildren().addAll(checkModsButton, clearCacheButton);

        settingsPanel.getChildren().addAll(
                title,
                new Separator(),
                gameSettingsTitle,
                ramBox,
                resolutionBox,
                new Separator(),
                launcherSettingsTitle,
                buttonBox
        );

        return settingsPanel;
    }

    private VBox createProfilePanel() {
        VBox profilePanel = new VBox(15);
        profilePanel.setPadding(new Insets(20, 15, 20, 15));
        profilePanel.setPrefWidth(250);
        profilePanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        Label profileTitle = new Label("OYUNCU PROFİLİ");
        profileTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        profileTitle.setTextFill(TEXT_SECONDARY);

        Circle avatarCircle = new Circle(40);
        StackPane avatarStack = new StackPane();
        if (!playerName.isEmpty()) {
            String avatarUrl = "https://minotar.net/helm/" + playerName + "/100.png";

            Task<Image> loadAvatarTask = new Task<>() {
                @Override
                protected Image call() throws Exception {
                    return new Image(avatarUrl, 100, 100, true, true);
                }
            };

            loadAvatarTask.setOnSucceeded(event -> {
                Image avatar = loadAvatarTask.getValue();
                if (avatar != null && !avatar.isError()) {
                    avatarCircle.setFill(new javafx.scene.paint.ImagePattern(avatar));
                } else {
                    avatarCircle.setFill(PRIMARY_COLOR);
                    Text initials = new Text(playerName.substring(0, 1).toUpperCase());
                    initials.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 30));
                    initials.setFill(Color.BLACK);
                    avatarStack.getChildren().add(initials);
                }
            });

            loadAvatarTask.setOnFailed(event -> {
                avatarCircle.setFill(PRIMARY_COLOR);
                Text initials = new Text(playerName.substring(0, 1).toUpperCase());
                initials.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 30));
                initials.setFill(Color.BLACK);
                avatarStack.getChildren().add(initials);
            });

            executorService.submit(loadAvatarTask);
        } else {
            avatarCircle.setFill(PRIMARY_COLOR);
            Text initials = new Text("U");
            initials.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 30));
            initials.setFill(Color.BLACK);
            avatarStack.getChildren().add(initials);
        }

        Circle avatarBorder = new Circle(43);
        avatarBorder.setFill(Color.TRANSPARENT);
        avatarBorder.setStroke(PRIMARY_COLOR);
        avatarBorder.setStrokeWidth(2);

        avatarStack.getChildren().addAll(avatarBorder, avatarCircle);

        Label usernameLabel = new Label(playerName.isEmpty() ? "Kullanıcı" : playerName);
        usernameLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        usernameLabel.setTextFill(TEXT_COLOR);

        Label accountTypeLabel = new Label("Oyuncu");
        accountTypeLabel.setFont(Font.font(FONT_FAMILY, 14));
        accountTypeLabel.setTextFill(TEXT_SECONDARY);

        VBox userInfoBox = new VBox(5);
        userInfoBox.setAlignment(Pos.CENTER);
        userInfoBox.getChildren().addAll(avatarStack, usernameLabel, accountTypeLabel);

        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        Label statsTitle = new Label("İSTATİSTİKLER");
        statsTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        statsTitle.setTextFill(TEXT_SECONDARY);

        VBox statsBox = new VBox(15);

        HBox playTimeBox = createStatInfoItem("Oynama Süresi", "32 saat");
        HBox gamesBox = createStatInfoItem("Coin", "126");
        HBox sayginlikBox = createStatInfoItem("Saygınlık Seviyesi", "10");

        statsBox.getChildren().addAll(playTimeBox, gamesBox, sayginlikBox);

        Separator separator2 = new Separator();
        separator2.setPadding(new Insets(10, 0, 10, 0));

        Label friendsTitle = new Label("ARKADAŞLAR (3/5 ÇEVRİMİÇİ)");
        friendsTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        friendsTitle.setTextFill(TEXT_SECONDARY);

        VBox friendsBox = new VBox(10);

        HBox friend1 = createFriendItem("G3YIK", true);
        HBox friend2 = createFriendItem("AethriusMC", true);
        HBox friend3 = createFriendItem("YusufKGD", false);
        HBox friend4 = createFriendItem("CraftMaster", true);
        HBox friend5 = createFriendItem("YunusKGD", false);

        friendsBox.getChildren().addAll(friend1, friend2, friend3, friend4, friend5);

        profilePanel.getChildren().addAll(
                profileTitle,
                userInfoBox,
                separator,
                statsTitle,
                statsBox,
                separator2,
                friendsTitle,
                friendsBox
        );

        return profilePanel;
    }

    // Mod management helper methods
    private void loadMods(ListView<String> modListView) {
        modListView.getItems().clear();
        try {
            Files.list(TERRAMONIC_PATH.resolve("mods"))
                    .filter(path -> path.toString().endsWith(".jar"))
                    .forEach(path -> modListView.getItems().add(path.getFileName().toString()));
        } catch (IOException e) {
            Platform.runLater(() -> showError("Modlar yüklenemedi: " + e.getMessage()));
        }
    }

    private void loadProfileList(ComboBox<String> profileComboBox) {
        profileComboBox.getItems().clear();
        try {
            Files.list(MODS_PROFILES_PATH)
                    .filter(path -> path.toString().endsWith(".zip"))
                    .forEach(path -> profileComboBox.getItems().add(path.getFileName().toString()));
        } catch (IOException e) {
            Platform.runLater(() -> showError("Profiller yüklenemedi: " + e.getMessage()));
        }
    }

    private void saveModProfile(String profileName) {
        try {
            Path zipPath = MODS_PROFILES_PATH.resolve(profileName + ".zip");
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
                Path modsPath = TERRAMONIC_PATH.resolve("mods");
                if (Files.exists(modsPath)) {
                    Files.walk(modsPath)
                            .filter(path -> !Files.isDirectory(path))
                            .forEach(path -> {
                                try {
                                    addToZip(path, "mods/" + modsPath.relativize(path).toString(), zos);
                                } catch (IOException e) {
                                    Platform.runLater(() -> showError("Mod dosyası zip'e eklenemedi: " + e.getMessage()));
                                }
                            });
                }

                Path configPath = TERRAMONIC_PATH.resolve("config");
                if (Files.exists(configPath)) {
                    Files.walk(configPath)
                            .filter(path -> !Files.isDirectory(path))
                            .forEach(path -> {
                                try {
                                    addToZip(path, "config/" + configPath.relativize(path).toString(), zos);
                                } catch (IOException e) {
                                    Platform.runLater(() -> showError("Config dosyası zip'e eklenemedi: " + e.getMessage()));
                                }
                            });
                }
            }
            Platform.runLater(() -> showInfo("Profil başarıyla kaydedildi: " + profileName));
        } catch (IOException e) {
            Platform.runLater(() -> showError("Profil kaydedilemedi: " + e.getMessage()));
        }

        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(3), ev -> refreshModPanelUI()));
        tl.play();
    }

    private void addToZip(Path filePath, String entryName, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        Files.copy(filePath, zos);
        zos.closeEntry();
    }

    private void loadModProfile(String profileName) {
        try {
            Path modsPath = TERRAMONIC_PATH.resolve("mods");
            Path configPath = TERRAMONIC_PATH.resolve("config");
            deleteDirectory(modsPath);
            deleteDirectory(configPath);
            Files.createDirectories(modsPath);
            Files.createDirectories(configPath);

            Path zipPath = MODS_PROFILES_PATH.resolve(profileName);
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    Path destPath;

                    if (entryName.startsWith("mods/")) {
                        destPath = modsPath.resolve(entryName.substring("mods/".length()));
                    } else if (entryName.startsWith("config/")) {
                        destPath = configPath.resolve(entryName.substring("config/".length()));
                    } else {
                        continue;
                    }

                    if (entry.isDirectory()) {
                        Files.createDirectories(destPath);
                    } else {
                        Files.createDirectories(destPath.getParent());
                        Files.copy(zis, destPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zis.closeEntry();
                }
            }
            Platform.runLater(() -> showInfo("Profil başarıyla yüklendi: " + profileName));
        } catch (IOException e) {
            Platform.runLater(() -> showError("Profil yüklenemedi: " + e.getMessage()));
        }
    }

    private HBox createStatInfoItem(String label, String value) {
        Label labelText = new Label(label + ":");
        labelText.setFont(Font.font(FONT_FAMILY, 14));
        labelText.setTextFill(TEXT_SECONDARY);

        Label valueText = new Label(value);
        valueText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        valueText.setTextFill(TEXT_COLOR);

        HBox statBox = new HBox(10);
        statBox.setAlignment(Pos.CENTER_LEFT);
        statBox.getChildren().addAll(labelText, valueText);

        return statBox;
    }

    private HBox createFriendItem(String name, boolean isOnline) {
        Circle statusCircle = new Circle(5);
        statusCircle.setFill(isOnline ? PRIMARY_COLOR : Color.web("#666666"));

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font(FONT_FAMILY, 14));
        nameLabel.setTextFill(isOnline ? TEXT_COLOR : TEXT_SECONDARY);

        HBox friendBox = new HBox(10);
        friendBox.setAlignment(Pos.CENTER_LEFT);
        friendBox.getChildren().addAll(statusCircle, nameLabel);

        return friendBox;
    }

    private VBox createNewsCard(NewsItem news) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: #1A1A1A;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-border-color: #333333;" +
                        "-fx-border-width: 1px;"
        );

        Label title = new Label(news.getTitle());
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        title.setTextFill(TEXT_COLOR);

        Label date = new Label(news.getDate());
        date.setFont(Font.font(FONT_FAMILY, 12));
        date.setTextFill(TEXT_SECONDARY);

        Label content = new Label(news.getContent());
        content.setFont(Font.font(FONT_FAMILY, 14));
        content.setTextFill(TEXT_COLOR);
        content.setWrapText(true);

        Label typeLabel = new Label(news.getType().toString());
        typeLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        typeLabel.setTextFill(PRIMARY_COLOR);
        typeLabel.setStyle("-fx-background-color: #222222; -fx-padding: 5px 10px; -fx-background-radius: 5px;");

        card.getChildren().addAll(title, date, content, typeLabel);

        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #222222;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-border-radius: 10px;" +
                            "-fx-border-color: " + toHexString(PRIMARY_COLOR) + ";" +
                            "-fx-border-width: 1px;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: #1A1A1A;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-border-radius: 10px;" +
                            "-fx-border-color: #333333;" +
                            "-fx-border-width: 1px;"
            );
        });

        return card;
    }

    /**
     * Sistem tepsisine ikon ekler
     */
    private void addSystemTrayIcon() {
        if (SystemTray.isSupported()) {
            System.out.println("Sistem tepsisi destekleniyor, ikon yükleniyor...");
            try {
                SystemTray tray = SystemTray.getSystemTray();
                BufferedImage trayIconImage = null;

                if (Files.exists(ICON_FILE)) {
                    try {
                        trayIconImage = ImageIO.read(ICON_FILE.toFile());
                        if (trayIconImage != null) {
                            System.out.println("PNG başarıyla yüklendi");
                        }
                    } catch (IOException e) {
                        System.out.println("PNG dosyası okunamadı: " + e.getMessage());
                    }
                }

                if (trayIconImage == null) {
                    trayIconImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = trayIconImage.createGraphics();
                    g.setColor(new java.awt.Color(0, 255, 0));
                    g.fillOval(0, 0, 64, 64);
                    g.dispose();
                }

                TrayIcon trayIcon = new TrayIcon(trayIconImage, "TerraMonic Launcher");
                trayIcon.setImageAutoSize(true);

                PopupMenu popup = new PopupMenu();
                MenuItem exitItem = new MenuItem("Çıkış");
                exitItem.addActionListener(e -> System.exit(0));
                popup.add(exitItem);
                trayIcon.setPopupMenu(popup);

                tray.add(trayIcon);
                System.out.println("Sistem tepsisi ikonu başarıyla eklendi.");
            } catch (AWTException e) {
                System.out.println("Sistem tepsisi ikonu ayarlanamadı: " + e.getMessage());
            }
        }
    }

    /**
     * Utility metodları
     */
    private String readJsonFromUrl(String url) throws IOException {
        if (url.startsWith("file://")) {
            Path filePath = Paths.get(url.substring(7));
            return Files.readString(filePath);
        } else {
            URL jsonUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) jsonUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "TerraMonic-Launcher/2.1.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                return content.toString();
            }
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.out.println("Dosya silinemedi: " + path + " - " + e.getMessage());
                        }
                    });
        }
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText("❌ " + message);
                statusLabel.setTextFill(Color.web("#FF3A3A"));
                statusLabel.setVisible(true);

                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                    statusLabel.setVisible(false);
                }));
                timeline.play();
            }
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText("✅ " + message);
                statusLabel.setTextFill(PRIMARY_COLOR);
                statusLabel.setVisible(true);

                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                    statusLabel.setVisible(false);
                }));
                timeline.play();
            }
        });
    }

    private void updateSystemIcons() {
        BufferedImage iconImage = null;

        try {
            if (Files.exists(ICON_FILE)) {
                iconImage = ImageIO.read(ICON_FILE.toFile());
            }
        } catch (IOException e) {
            System.out.println("PNG dosyası okunurken hata: " + ICON_FILE + ", Hata=" + e.getMessage());
        }

        if (iconImage == null) {
            iconImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = iconImage.createGraphics();
            g.setColor(new java.awt.Color(0, 255, 0));
            g.fillOval(0, 0, 64, 64);
            g.setColor(java.awt.Color.BLACK);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
            g.drawString("T", 20, 40);
            g.dispose();
        }

        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                try {
                    taskbar.setIconImage(iconImage);
                    System.out.println("Görev çubuğu ikonu ayarlandı");
                } catch (UnsupportedOperationException | SecurityException e) {
                    System.out.println("Görev çubuğu ikonu güncellenemedi: " + e.getMessage());
                }
            }
        }
    }

    private VBox createStatBox(String value, String label) {
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 28));
        valueLabel.setTextFill(TEXT_COLOR);

        Label descLabel = new Label(label);
        descLabel.setFont(Font.font(FONT_FAMILY, 14));
        descLabel.setTextFill(TEXT_SECONDARY);

        VBox statBox = new VBox(5);
        statBox.setAlignment(Pos.CENTER_LEFT);
        statBox.getChildren().addAll(valueLabel, descLabel);

        return statBox;
    }

    private TextField createStyledTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setPrefHeight(45);
        textField.setFont(Font.font(FONT_FAMILY, 14));
        textField.setStyle(
                "-fx-background-color: #1A1A1A;" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #555555;" +
                        "-fx-background-radius: " + BUTTON_RADIUS + "px;" +
                        "-fx-border-radius: " + BUTTON_RADIUS + "px;" +
                        "-fx-border-color: #333333;" +
                        "-fx-border-width: 1px;" +
                        "-fx-padding: 10px;"
        );

        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                textField.setStyle(
                        "-fx-background-color: #1A1A1A;" +
                                "-fx-text-fill: white;" +
                                "-fx-prompt-text-fill: #555555;" +
                                "-fx-background-radius: " + BUTTON_RADIUS + "px;" +
                                "-fx-border-radius: " + BUTTON_RADIUS + "px;" +
                                "-fx-border-color: " + toHexString(PRIMARY_COLOR) + ";" +
                                "-fx-border-width: 1.5px;" +
                                "-fx-padding: 10px;"
                );
            } else {
                textField.setStyle(
                        "-fx-background-color: #1A1A1A;" +
                                "-fx-text-fill: white;" +
                                "-fx-prompt-text-fill: #555555;" +
                                "-fx-background-radius: " + BUTTON_RADIUS + "px;" +
                                "-fx-border-radius: " + BUTTON_RADIUS + "px;" +
                                "-fx-border-color: #333333;" +
                                "-fx-border-width: 1px;" +
                                "-fx-padding: 10px;"
                );
            }
        });

        return textField;
    }

    private Button createStyledButton(String text, double width, double height) {
        Button button = new Button(text);
        button.setPrefSize(width, height);
        button.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));

        DropShadow shadow = new DropShadow();
        shadow.setColor(SHADOW_COLOR);
        shadow.setRadius(15);
        shadow.setSpread(0.15);
        button.setEffect(shadow);

        button.setStyle(
                "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";" +
                        "-fx-text-fill: black;" +
                        "-fx-background-radius: " + BUTTON_RADIUS + "px;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-background-color: " + toHexString(PRIMARY_HIGHLIGHT) + ";" +
                            "-fx-text-fill: black;" +
                            "-fx-background-radius: " + BUTTON_RADIUS + "px;" +
                            "-fx-cursor: hand;"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";" +
                            "-fx-text-fill: black;" +
                            "-fx-background-radius: " + BUTTON_RADIUS + "px;" +
                            "-fx-cursor: hand;"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return button;
    }

    private void playButtonClickAnimation(Button button) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
        scaleDown.setToX(0.9);
        scaleDown.setToY(0.9);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        scaleDown.setOnFinished(e -> scaleUp.play());
        scaleDown.play();
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void transitionToScene(Scene newScene) {
        if (currentScene != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentScene.getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                mainStage.setScene(newScene);
                currentScene = newScene;

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        } else {
            mainStage.setScene(newScene);
            currentScene = newScene;

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newScene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    private void setupWindowControls(javafx.scene.Node content) {
        content.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        content.setOnMouseDragged(event -> {
            mainStage.setX(event.getScreenX() - xOffset);
            mainStage.setY(event.getScreenY() - yOffset);
        });
    }

    private BorderPane createTitleBar() {
        BorderPane titleBar = new BorderPane();
        titleBar.setPadding(new Insets(10, 15, 10, 15));
        titleBar.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        Label titleLabel = new Label("TerraMonic");
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        titleLabel.setTextFill(PRIMARY_COLOR);

        HBox leftBox = new HBox(10);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        if (launcherIcon != null && !launcherIcon.isError()) {
            ImageView iconView = new ImageView(launcherIcon);
            iconView.setFitHeight(20);
            iconView.setFitWidth(20);
            iconView.setPreserveRatio(true);
            leftBox.getChildren().addAll(iconView, titleLabel);
        } else {
            leftBox.getChildren().add(titleLabel);
        }

        titleBar.setLeft(leftBox);

        HBox windowControls = new HBox(10);
        windowControls.setAlignment(Pos.CENTER_RIGHT);

        Button minimizeBtn = createWindowControlButton("—", "#555555");
        minimizeBtn.setOnAction(e -> mainStage.setIconified(true));

        Button closeBtn = createWindowControlButton("✕", "#FF3A3A");
        closeBtn.setOnAction(e -> {
            if (executorService != null) {
                executorService.shutdownNow();
            }
            System.exit(0);
        });

        windowControls.getChildren().addAll(minimizeBtn, closeBtn);
        titleBar.setRight(windowControls);

        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            mainStage.setX(event.getScreenX() - xOffset);
            mainStage.setY(event.getScreenY() - yOffset);
        });

        return titleBar;
    }

    private Button createWindowControlButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 10));
        button.setPrefSize(20, 20);
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + color + ";" +
                        "-fx-border-color: transparent;" +
                        "-fx-padding: 0;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: " + color + ";" +
                                "-fx-text-fill: black;" +
                                "-fx-background-radius: 5px;" +
                                "-fx-border-color: transparent;" +
                                "-fx-padding: 0;" +
                                "-fx-cursor: hand;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: " + color + ";" +
                                "-fx-border-color: transparent;" +
                                "-fx-padding: 0;" +
                                "-fx-cursor: hand;"
                )
        );

        return button;
    }

    private void refreshModPanelUI() {
        if (centerPanel != null) {
            Platform.runLater(() -> {
                if (currentNavIndex==1 && centerPanel.lookup("#waitMods") != null) {
                    centerPanel.getChildren().clear();
                    centerPanel.getChildren().add(createModManagementPanel());
                }
            });
        }
    }

    /**
     * NewsItem sınıfı
     */
    private static class NewsItem {
        private final String title;
        private final String content;
        private final String date;
        private final NewsItemType type;

        public NewsItem(String title, String content, String date, NewsItemType type) {
            this.title = title;
            this.content = content;
            this.date = date;
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getDate() {
            return date;
        }

        public NewsItemType getType() {
            return type;
        }
    }

    /**
     * NewsItemType enum
     */
    private enum NewsItemType {
        GÜNCELLEME("#01a500", "📦"),
        KAYNAK("#00b4d8", "🗺️"),
        ETKİNLİK("#f72585", "🎉"),
        İNDİRİM("#ff5400", "💰"),
        YOUTUBE("#ff0000", "▶️"),
        TANITIM("#8a2be2", "🌟"),
        GENEL("#ffffff", "📢"),
        YAYIN("#9146ff", "🔴"),
        DİSCORD("#5865f2", "💬");

        private final String color;
        private final String icon;

        NewsItemType(String color, String icon) {
            this.color = color;
            this.icon = icon;
        }

        public String getColor() {
            return color;
        }

        public String getIcon() {
            return icon;
        }
    }
}