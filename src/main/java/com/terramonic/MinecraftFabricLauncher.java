package com.terramonic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.time.Duration;

public class MinecraftFabricLauncher {
    private final Path appdataPath;
    private final Path minecraftDir;
    private final Path versionsDir;
    private final Path librariesDir;
    private final Path assetsDir;
    private final Path nativesDir;

    private final String minecraftVersion = "1.21.5";
    private final String fabricLoaderVersion = "0.16.14";
    private final String fabricVersionId;

    private final String versionManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";
    private final String fabricProfileUrl;

    private final HttpClient httpClient;
    private final Gson gson;
    private final ExecutorService executor;

    public MinecraftFabricLauncher() {
        this.appdataPath = Paths.get(System.getenv("APPDATA"));
        this.minecraftDir = appdataPath.resolve(".terramonic");
        this.versionsDir = minecraftDir.resolve("versions");
        this.librariesDir = minecraftDir.resolve("libraries");
        this.assetsDir = minecraftDir.resolve("assets");
        this.nativesDir = minecraftDir.resolve("natives");

        this.fabricVersionId = "fabric-loader-" + fabricLoaderVersion + "-" + minecraftVersion;
        this.fabricProfileUrl = "https://meta.fabricmc.net/v2/versions/loader/" +
                minecraftVersion + "/" + fabricLoaderVersion + "/profile/json";

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.executor = Executors.newFixedThreadPool(10);

        createDirectories();
    }

    private void createDirectories() {
        List<Path> dirs = Arrays.asList(
                minecraftDir,
                versionsDir,
                librariesDir,
                assetsDir,
                nativesDir,
                versionsDir.resolve(minecraftVersion),
                versionsDir.resolve(fabricVersionId)
        );

        for (Path dir : dirs) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                System.err.println("Klasör oluşturulamadı: " + dir);
            }
        }

        System.out.println("✓ Klasörler oluşturuldu: " + minecraftDir);
    }

    private boolean downloadFile(String url, Path filePath, String expectedHash, String hashType) {
        try {
            if (Files.exists(filePath) && expectedHash != null) {
                if (verifyHash(filePath, expectedHash, hashType)) {
                    System.out.println("✓ Zaten mevcut: " + filePath.getFileName());
                    return true;
                }
            }

            System.out.println("📥 İndiriliyor: " + filePath.getFileName());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(5))
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                System.err.println("❌ İndirme hatası: " + response.statusCode());
                return false;
            }

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, response.body());

            if (expectedHash != null && !verifyHash(filePath, expectedHash, hashType)) {
                System.err.println("❌ Hash doğrulama başarısız: " + filePath.getFileName());
                return false;
            }

            System.out.println("✓ İndirildi: " + filePath.getFileName());
            return true;

        } catch (Exception e) {
            System.err.println("❌ İndirme hatası " + filePath.getFileName() + ": " + e.getMessage());
            return false;
        }
    }

    private boolean downloadFile(String url, Path filePath) {
        return downloadFile(url, filePath, null, null);
    }

    private boolean verifyHash(Path filePath, String expectedHash, String hashType) {
        try {
            MessageDigest digest = MessageDigest.getInstance(hashType.toUpperCase());
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = digest.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString().equals(expectedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private JsonObject getVersionManifest() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(versionManifestUrl))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), JsonObject.class);
        } catch (Exception e) {
            System.err.println("❌ Version manifest alınamadı: " + e.getMessage());
            return null;
        }
    }

    private JsonObject getVersionInfo(String versionUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(versionUrl))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), JsonObject.class);
        } catch (Exception e) {
            System.err.println("❌ Version bilgisi alınamadı: " + e.getMessage());
            return null;
        }
    }

    private boolean downloadMinecraftVersion() {
        System.out.println("🎮 Minecraft " + minecraftVersion + " indiriliyor...");

        JsonObject manifest = getVersionManifest();
        if (manifest == null) return false;

        JsonObject versionInfo = null;
        JsonArray versions = manifest.getAsJsonArray("versions");

        for (JsonElement version : versions) {
            JsonObject versionObj = version.getAsJsonObject();
            if (minecraftVersion.equals(versionObj.get("id").getAsString())) {
                versionInfo = versionObj;
                break;
            }
        }

        if (versionInfo == null) {
            System.err.println("❌ Version " + minecraftVersion + " bulunamadı!");
            return false;
        }

        JsonObject versionJson = getVersionInfo(versionInfo.get("url").getAsString());
        if (versionJson == null) return false;

        // Version JSON'ı kaydet
        Path versionJsonPath = versionsDir.resolve(minecraftVersion).resolve(minecraftVersion + ".json");
        try {
            Files.write(versionJsonPath, gson.toJson(versionJson).getBytes());
        } catch (IOException e) {
            System.err.println("❌ Version JSON kaydedilemedi: " + e.getMessage());
            return false;
        }

        // Client JAR'ı indir
        JsonObject clientInfo = versionJson.getAsJsonObject("downloads").getAsJsonObject("client");
        Path clientJarPath = versionsDir.resolve(minecraftVersion).resolve(minecraftVersion + ".jar");

        if (!downloadFile(clientInfo.get("url").getAsString(), clientJarPath,
                clientInfo.get("sha1").getAsString(), "sha1")) {
            return false;
        }

        downloadLibraries(versionJson.getAsJsonArray("libraries"));
        downloadAssets(versionJson.getAsJsonObject("assetIndex"));

        return true;
    }

    private void downloadLibraries(JsonArray libraries) {
        System.out.println("📚 Kütüphaneler indiriliyor...");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (JsonElement library : libraries) {
            JsonObject libraryObj = library.getAsJsonObject();

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    if (libraryObj.has("rules")) {
                        if (!checkRules(libraryObj.getAsJsonArray("rules"))) {
                            return;
                        }
                    }

                    if (libraryObj.has("downloads")) {
                        JsonObject downloads = libraryObj.getAsJsonObject("downloads");

                        if (downloads.has("artifact")) {
                            JsonObject artifact = downloads.getAsJsonObject("artifact");
                            Path libPath = librariesDir.resolve(artifact.get("path").getAsString());
                            downloadFile(artifact.get("url").getAsString(), libPath,
                                    artifact.get("sha1").getAsString(), "sha1");
                        }

                        if (downloads.has("classifiers")) {
                            JsonObject classifiers = downloads.getAsJsonObject("classifiers");
                            String nativesKey = "natives-" + getOsName();

                            if (classifiers.has(nativesKey)) {
                                JsonObject nativeInfo = classifiers.getAsJsonObject(nativesKey);
                                Path nativePath = librariesDir.resolve(nativeInfo.get("path").getAsString());
                                if (downloadFile(nativeInfo.get("url").getAsString(), nativePath,
                                        nativeInfo.get("sha1").getAsString(), "sha1")) {
                                    extractNative(nativePath);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Kütüphane indirme hatası: " + e.getMessage());
                }
            }, executor);

            futures.add(future);
        }

        // Tüm indirmelerin tamamlanmasını bekle
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void downloadAssets(JsonObject assetIndex) {
        System.out.println("🎨 Assets indiriliyor...");

        Path indexPath = assetsDir.resolve("indexes").resolve(assetIndex.get("id").getAsString() + ".json");
        if (!downloadFile(assetIndex.get("url").getAsString(), indexPath,
                assetIndex.get("sha1").getAsString(), "sha1")) {
            return;
        }

        try {
            String indexContent = Files.readString(indexPath);
            JsonObject indexData = gson.fromJson(indexContent, JsonObject.class);
            JsonObject objects = indexData.getAsJsonObject("objects");

            Path objectsDir = assetsDir.resolve("objects");
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Map.Entry<String, JsonElement> entry : objects.entrySet()) {
                JsonObject obj = entry.getValue().getAsJsonObject();
                String hash = obj.get("hash").getAsString();

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        String hashPrefix = hash.substring(0, 2);
                        String assetUrl = "https://resources.download.minecraft.net/" + hashPrefix + "/" + hash;
                        Path assetPath = objectsDir.resolve(hashPrefix).resolve(hash);
                        downloadFile(assetUrl, assetPath, hash, "sha1");
                    } catch (Exception e) {
                        System.err.println("❌ Asset indirme hatası: " + e.getMessage());
                    }
                }, executor);

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } catch (IOException e) {
            System.err.println("❌ Assets index okunamadı: " + e.getMessage());
        }
    }

    private JsonObject downloadFabricProfile() {
        System.out.println("🧵 Fabric profil bilgisi alınıyor...");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fabricProfileUrl))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), JsonObject.class);
        } catch (Exception e) {
            System.err.println("❌ Fabric profil bilgisi alınamadı: " + e.getMessage());
            return null;
        }
    }

    private boolean setupFabric() {
        System.out.println("🧵 Fabric kurulumu yapılıyor...");

        JsonObject fabricProfile = downloadFabricProfile();
        if (fabricProfile == null) return false;

        Path fabricJsonPath = versionsDir.resolve(fabricVersionId).resolve(fabricVersionId + ".json");
        try {
            Files.write(fabricJsonPath, gson.toJson(fabricProfile).getBytes());
        } catch (IOException e) {
            System.err.println("❌ Fabric JSON kaydedilemedi: " + e.getMessage());
            return false;
        }

        downloadFabricLibraries(fabricProfile.getAsJsonArray("libraries"));

        Path fabricJarPath = versionsDir.resolve(fabricVersionId).resolve(fabricVersionId + ".jar");
        Path minecraftJarPath = versionsDir.resolve(minecraftVersion).resolve(minecraftVersion + ".jar");

        if (!Files.exists(fabricJarPath) && Files.exists(minecraftJarPath)) {
            try {
                Files.copy(minecraftJarPath, fabricJarPath);
                System.out.println("✓ Fabric JAR oluşturuldu: " + fabricJarPath);
            } catch (IOException e) {
                System.err.println("⚠️ Fabric JAR oluşturulamadı: " + e.getMessage());
            }
        }

        return true;
    }

    private void downloadFabricLibraries(JsonArray libraries) {
        System.out.println("📚 Fabric kütüphaneleri indiriliyor...");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (JsonElement library : libraries) {
            JsonObject libraryObj = library.getAsJsonObject();

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String name = libraryObj.get("name").getAsString();
                    String[] parts = name.split(":");

                    if (parts.length >= 3) {
                        String group = parts[0];
                        String artifact = parts[1];
                        String version = parts[2];
                        String classifier = parts.length > 3 ? "-" + parts[3] : "";
                        String filename = artifact + "-" + version + classifier + ".jar";
                        String path = group.replace(".", "/") + "/" + artifact + "/" + version + "/" + filename;

                        String baseUrl = libraryObj.has("url") ? libraryObj.get("url").getAsString() : "https://maven.fabricmc.net/";
                        if (!baseUrl.endsWith("/")) baseUrl += "/";
                        String url = baseUrl + path;

                        Path libPath = librariesDir.resolve(path);

                        if (libraryObj.has("sha1")) {
                            downloadFile(url, libPath, libraryObj.get("sha1").getAsString(), "sha1");
                        } else if (libraryObj.has("md5")) {
                            downloadFile(url, libPath, libraryObj.get("md5").getAsString(), "md5");
                        } else {
                            downloadFile(url, libPath);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Fabric kütüphane indirme hatası: " + e.getMessage());
                }
            }, executor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void extractNative(Path nativePath) {
        try (ZipFile zipFile = new ZipFile(nativePath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (!entry.getName().startsWith("META-INF/")) {
                    Path extractPath = nativesDir.resolve(entry.getName());
                    Files.createDirectories(extractPath.getParent());

                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        Files.copy(inputStream, extractPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Native çıkarma hatası: " + e.getMessage());
        }
    }

    private boolean checkRules(JsonArray rules) {
        for (JsonElement rule : rules) {
            JsonObject ruleObj = rule.getAsJsonObject();
            String action = ruleObj.has("action") ? ruleObj.get("action").getAsString() : "allow";

            if (ruleObj.has("os")) {
                JsonObject os = ruleObj.getAsJsonObject("os");
                if (os.has("name")) {
                    String osName = os.get("name").getAsString();
                    if (!osName.equals(getOsName())) {
                        if ("allow".equals(action)) {
                            return false;
                        } else if ("disallow".equals(action)) {
                            return true;
                        }
                    }
                }
            }
        }
        return true;
    }

    private String getOsName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        } else if (os.contains("mac")) {
            return "osx";
        } else {
            return "linux";
        }
    }

    private void createLauncherProfile() {
        System.out.println("⚙️ Launcher profili oluşturuluyor...");

        JsonObject profiles = new JsonObject();
        JsonObject fabricProfile = new JsonObject();
        fabricProfile.addProperty("name", "TerraMonic");
        fabricProfile.addProperty("type", "custom");
        fabricProfile.addProperty("created", "2025-07-09T11:20:01.000Z");
        fabricProfile.addProperty("lastUsed", "2025-07-09T11:20:01.000Z");
        fabricProfile.addProperty("icon", "Crafting_Table");
        fabricProfile.addProperty("lastVersionId", fabricVersionId);
        fabricProfile.addProperty("gameDir", minecraftDir.toString());

        profiles.add("TerraMonic", fabricProfile);

        JsonObject settings = new JsonObject();
        settings.addProperty("enableSnapshots", false);
        settings.addProperty("enableAdvanced", false);
        settings.addProperty("keepLauncherOpen", false);
        settings.addProperty("showGameLog", false);
        settings.addProperty("locale", "tr_TR");

        JsonObject launcherProfiles = new JsonObject();
        launcherProfiles.add("profiles", profiles);
        launcherProfiles.add("settings", settings);
        launcherProfiles.addProperty("version", 3);

        Path profilesPath = minecraftDir.resolve("launcher_profiles.json");
        try {
            Files.write(profilesPath, gson.toJson(launcherProfiles).getBytes());
            System.out.println("✓ Launcher profili oluşturuldu");
        } catch (IOException e) {
            System.err.println("❌ Launcher profili oluşturulamadı: " + e.getMessage());
        }
    }

    public boolean install() {
        System.out.println("🎮 TerraMonic Minecraft Fabric Launcher Kurulumu Başlıyor...");
        System.out.println("=".repeat(50));

        try {
            if (!downloadMinecraftVersion()) {
                System.err.println("❌ Minecraft indirme başarısız!");
                return false;
            }

            if (!setupFabric()) {
                System.err.println("❌ Fabric kurulumu başarısız!");
                return false;
            }

            createLauncherProfile();

            System.out.println("=".repeat(50));
            System.out.println("✅ TerraMonic kurulumu tamamlandı!");
            System.out.println("📁 Kurulum dizini: " + minecraftDir);
            System.out.println("=".repeat(50));

            return true;

        } catch (Exception e) {
            System.err.println("❌ Kurulum hatası: " + e.getMessage());
            return false;
        } finally {
            executor.shutdown();
        }
    }
}