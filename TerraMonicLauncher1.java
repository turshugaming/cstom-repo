package com.terramonic;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.MenuItem;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static javafx.scene.layout.GridPane.setVgrow;

/**
 * TerraMonic Minecraft Launcher - Premium Version
 *
 * Modern, akıcı ve özellikli tek sınıf launcher tasarımı
 * Modrinth mod sistemi ve Fabric 1.21.5 desteği
 *
 * @version 2.0
 */
public class TerraMonicLauncher1 extends Application {

    // Ana renkler ve tema
    private static final Color BACKGROUND_COLOR = Color.web("#000000");
    private static final Color BACKGROUND_SECONDARY = Color.web("#111111");
    private static final Color PRIMARY_COLOR = Color.web("#01a500");
    private static final Color PRIMARY_DARK = Color.web("#01a500");
    private static final Color PRIMARY_HIGHLIGHT = Color.web("#71ff61");
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color TEXT_SECONDARY = Color.web("#AAAAAA");
    private static final Color SHADOW_COLOR = Color.web("71ff61");

    // Genel font ayarları
    private static final String GILROY_FONT_URL = "https://www.dropbox.com/scl/fi/to4nfmn8047ec1f4vqujv/Gilroy-Bold.otf?rlkey=egesrxgfpx0ppj8cruis7mksp&st=crk2grid&dl=1";
    private static final String FONT_FAMILY = "Gilroy Bold";
    private static final double BUTTON_RADIUS = 18.0;

    // Launcher sabitleri
    private static final String LAUNCHER_VERSION = "v1.0.2";
    private static final String MINECRAFT_VERSION = "1.21.5";
    private static final String FABRIC_VERSION = "0.16.14";
    private static final String TERRAMONIC_URL = "https://www.terramonic.com";
    private static final String ICON_URL = "https://www.dropbox.com/scl/fi/2yc75kqokrtivw202rt3w/icon.png?rlkey=1blmy791i17gs6t78ecjc3qxf&st=cjc590fc&dl=1";

    // JSON URL'i
    private static final String LAUNCHER_JSON_URL = "https://www.dropbox.com/scl/fi/zcty3rszkpxritcd6ip85/launcher.json?rlkey=byf0phf8xjy0j6nuuto1qohql&st=rcgtv35f&dl=1";

    // Fabric installer
    private static final String FABRIC_INSTALLER_URL = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.0.3/fabric-installer-1.0.3.jar";

    // YENİ: USER-SPECİFİC PATH SİSTEMİ
    private static final Path TERRAMONIC_PATH = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", ".terramonic");
    private static final Path LAUNCHER_VERSION_JSON = TERRAMONIC_PATH.resolve("launcher_version.json");
    private String currentLauncherVersion = "v1.0.2";

    // Kullanıcı verileri
    private String playerName = "";
    private boolean rememberUser = false;

    // Ekran boyut ve kontrol değişkenleri
    private double xOffset;
    private double yOffset;
    private double windowWidth = 1100;
    private double windowHeight = 700;

    // Haber ve duyurular
    private List<NewsItem> newsList = new ArrayList<>();

    // UI elemanları
    private Button playButton;
    private ProgressBar downloadProgress;
    private final Label statusLabel = new Label("");
    private ImageView userAvatar;
    private ExecutorService executorService;

    // YENİ: OYUN BAŞLATMA İÇİN GEREKLI UI ELEMANLARI - AYARLAR SEKMESİNDE OLACAK
    private ComboBox<String> ramCombo;
    private ComboBox<String> resCombo;
    
    // YENİ: MOD KONTROL SİSTEMİ
    private static final Path DELETED_MODS_FILE = TERRAMONIC_PATH.resolve("deleted_mods.json");
    private static final Path MOD_INTEGRITY_FILE = TERRAMONIC_PATH.resolve("mod_integrity.json");
    
    // YENİ: UI DISABLE SISTEMI
    private boolean gameIsLaunching = false;

    // Stage ve scene referansları
    private Stage mainStage;
    private Scene currentScene;

    // Ana launcher iconu
    private Image launcherIcon;

    // .terramonic klasör yolu
    private static final Path MODS_PROFILES_PATH = TERRAMONIC_PATH.resolve("mods_profiles");
    private static final Path ICON_PATH = TERRAMONIC_PATH.resolve("terramonic_icon");
    private static final Path ICON_FILE = ICON_PATH.resolve("terramonic_icon.png");
    private static final Path ICO_FILE = ICON_PATH.resolve("terramonic_icon.ico");

    // Launcher config data
    private JSONObject launcherConfig;

    // Mod indirimi durumu
    private final javafx.beans.property.BooleanProperty modsReady = new javafx.beans.property.SimpleBooleanProperty(false);
    private final javafx.beans.property.BooleanProperty librariesReady = new javafx.beans.property.SimpleBooleanProperty(false);

    // Ana merkez panel referansı
    private StackPane centerPanel;

    // Navigasyon seçili indeks
    private int currentNavIndex = 0;

    /**
     * Ana metod
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        mainStage.initStyle(StageStyle.UNDECORATED);
        mainStage.setTitle("TerraMonic Launcher " + LAUNCHER_VERSION);

        // Thread pool oluştur
        executorService = Executors.newFixedThreadPool(4);

        // .terramonic klasörünü kur
        setupTerramonicFolder();

        // Iconu yükle ve ardından başlat
        loadIconAndStart();

        mainStage.setWidth(windowWidth);
        mainStage.setHeight(windowHeight);
        mainStage.centerOnScreen();
    }

    @Override
    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    /**
     * .terramonic klasörünü ve icon klasörünü kurar
     */
    private void setupTerramonicFolder() {
        try {
            // .terramonic klasörünü oluştur
            Files.createDirectories(TERRAMONIC_PATH);
            // Gerekli alt klasörler - FABRIC VE MODRİNTH İÇİN GÜNCELLENDİ
            Files.createDirectories(TERRAMONIC_PATH.resolve("mods"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("config"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("versions"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("libraries"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("natives"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("assets"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("assets/objects"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("assets/indexes"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("jar"));
            Files.createDirectories(MODS_PROFILES_PATH);
            Files.createDirectories(ICON_PATH);
        } catch (IOException e) {
            System.out.println("Klasör oluşturma hatası: " + e.getMessage());
        }
    }

    /**
     * YENİ: MİNECRAFT LİBRARİES İNDİRME METODU
     */
    private void downloadMinecraftLibraries() {
        Task<Void> libraryTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> statusLabel.setText("Minecraft kütüphaneleri indiriliyor..."));
                
                // Version manifest URL
                String versionManifestUrl = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
                String manifestContent = readJsonFromUrl(versionManifestUrl);
                JSONObject manifest = new JSONObject(manifestContent);
                
                // 1.21.5 versiyonunu bul
                JSONArray versions = manifest.getJSONArray("versions");
                String versionUrl = null;
                for (int i = 0; i < versions.length(); i++) {
                    JSONObject version = versions.getJSONObject(i);
                    if (MINECRAFT_VERSION.equals(version.getString("id"))) {
                        versionUrl = version.getString("url");
                        break;
                    }
                }
                
                if (versionUrl == null) {
                    throw new IOException("Minecraft " + MINECRAFT_VERSION + " versiyonu bulunamadı!");
                }
                
                // Versiyon bilgilerini indir
                String versionContent = readJsonFromUrl(versionUrl);
                JSONObject versionJson = new JSONObject(versionContent);
                
                // Libraries dizini
                Path librariesDir = TERRAMONIC_PATH.resolve("libraries");
                Files.createDirectories(librariesDir);
                
                // YENİ: SLF4J kütüphanelerini ekle
                downloadSLF4JLibraries(librariesDir);
                
                // YENİ: LWJGL native DLL'lerini çıkar
                extractLWJGLNatives(librariesDir);
                
                // Library'leri indir
                JSONArray libraries = versionJson.getJSONArray("libraries");
                int totalLibs = libraries.length();
                for (int i = 0; i < totalLibs; i++) {
                    JSONObject library = libraries.getJSONObject(i);
                    
                    // Platform kontrolü (Windows için)
                    if (library.has("rules")) {
                        JSONArray rules = library.getJSONArray("rules");
                        boolean allowed = false;
                        for (int j = 0; j < rules.length(); j++) {
                            JSONObject rule = rules.getJSONObject(j);
                            String action = rule.getString("action");
                            if ("allow".equals(action)) {
                                if (!rule.has("os")) {
                                    allowed = true;
                                } else {
                                    JSONObject os = rule.getJSONObject("os");
                                    if ("windows".equals(os.optString("name"))) {
                                        allowed = true;
                                    }
                                }
                            }
                        }
                        if (!allowed) continue;
                    }
                    
                    // Download bilgilerini al
                    if (library.has("downloads")) {
                        JSONObject downloads = library.getJSONObject("downloads");
                        if (downloads.has("artifact")) {
                            JSONObject artifact = downloads.getJSONObject("artifact");
                            String downloadUrl = artifact.getString("url");
                            String path = artifact.getString("path");
                            
                            Path targetPath = librariesDir.resolve(path);
                            Files.createDirectories(targetPath.getParent());
                            
                            if (!Files.exists(targetPath)) {
                                Platform.runLater(() -> statusLabel.setText("Library indiriliyor: " + path));
                                downloadFile(downloadUrl, targetPath);
                                System.out.println("Library indirildi: " + path);
                            }
                        }
                        
                        // Natives (Windows için)
                        if (downloads.has("classifiers")) {
                            JSONObject classifiers = downloads.getJSONObject("classifiers");
                            String nativeKey = "natives-windows";
                            if (classifiers.has(nativeKey)) {
                                JSONObject nativeArtifact = classifiers.getJSONObject(nativeKey);
                                String downloadUrl = nativeArtifact.getString("url");
                                String path = nativeArtifact.getString("path");
                                
                                Path targetPath = librariesDir.resolve(path);
                                Files.createDirectories(targetPath.getParent());
                                
                                if (!Files.exists(targetPath)) {
                                    Platform.runLater(() -> statusLabel.setText("Native indiriliyor: " + path));
                                    downloadFile(downloadUrl, targetPath);
                                    System.out.println("Native indirildi: " + path);
                                }
                            }
                        }
                    }
                }
                
                // Client JAR'ı indir
                JSONObject downloads = versionJson.getJSONObject("downloads");
                JSONObject client = downloads.getJSONObject("client");
                String clientUrl = client.getString("url");
                
                Path clientJarPath = TERRAMONIC_PATH.resolve("versions").resolve(MINECRAFT_VERSION).resolve(MINECRAFT_VERSION + ".jar");
                Files.createDirectories(clientJarPath.getParent());
                
                if (!Files.exists(clientJarPath)) {
                    Platform.runLater(() -> statusLabel.setText("Client JAR indiriliyor..."));
                    downloadFile(clientUrl, clientJarPath);
                    System.out.println("Client JAR indirildi: " + clientJarPath);
                }
                
                // Assets indir
                JSONObject assetIndex = versionJson.getJSONObject("assetIndex");
                String assetsUrl = assetIndex.getString("url");
                String assetsId = assetIndex.getString("id");
                
                Path assetsIndexPath = TERRAMONIC_PATH.resolve("assets/indexes").resolve(assetsId + ".json");
                Files.createDirectories(assetsIndexPath.getParent());
                
                if (!Files.exists(assetsIndexPath)) {
                    Platform.runLater(() -> statusLabel.setText("Asset index indiriliyor..."));
                    downloadFile(assetsUrl, assetsIndexPath);
                }
                
                librariesReady.set(true);
                Platform.runLater(() -> statusLabel.setText("Minecraft kütüphaneleri hazır!"));
                System.out.println("🎉 Minecraft libraries başarıyla indirildi!");
                
                return null;
            }
        };
        
        libraryTask.setOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            System.out.println("❌ Minecraft libraries indirme hatası: " + exception.getMessage());
            exception.printStackTrace();
            Platform.runLater(() -> {
                statusLabel.setText("Library indirme hatası!");
                showError("Minecraft kütüphaneleri indirilemedi:\n\n" + exception.getMessage());
            });
        });
        
        executorService.submit(libraryTask);
    }

    /**
     * YENİ: SLF4J KÜTÜPHANELERİNİ İNDİRİR (c2me, badoptimizations vb modlar için)
     */
    private void downloadSLF4JLibraries(Path librariesDir) throws IOException {
        // SLF4J, LWJGL ve kritik kütüphaneler (modlar için gerekli)
                            String[][] slf4jLibraries = {
              {"org/slf4j", "slf4j-api", "2.0.13"},
              // JOptSimple - Minecraft için gerekli
              {"net/sf/jopt-simple", "jopt-simple", "5.0.4"},
              // LWJGL 3.3.3 kütüphaneleri
              {"org/lwjgl", "lwjgl", "3.3.3"},
            {"org/lwjgl", "lwjgl-opengl", "3.3.3"},
            {"org/lwjgl", "lwjgl-glfw", "3.3.3"},
            {"org/lwjgl", "lwjgl-stb", "3.3.3"},
            {"org/lwjgl", "lwjgl-tinyfd", "3.3.3"},
                          // Native DLL'ler için - platform specific
              {"org/lwjgl", "lwjgl", "3.3.3", "natives-windows"},
              {"org/lwjgl", "lwjgl-opengl", "3.3.3", "natives-windows"},
              {"org/lwjgl", "lwjgl-glfw", "3.3.3", "natives-windows"},
              {"org/lwjgl", "lwjgl-stb", "3.3.3", "natives-windows"},
              {"org/lwjgl", "lwjgl-tinyfd", "3.3.3", "natives-windows"},
              // Linux desteği
              {"org/lwjgl", "lwjgl", "3.3.3", "natives-linux"},
              {"org/lwjgl", "lwjgl-opengl", "3.3.3", "natives-linux"},
              {"org/lwjgl", "lwjgl-glfw", "3.3.3", "natives-linux"},
              {"org/lwjgl", "lwjgl-stb", "3.3.3", "natives-linux"},
              {"org/lwjgl", "lwjgl-tinyfd", "3.3.3", "natives-linux"},
                            // Mac desteği
              {"org/lwjgl", "lwjgl", "3.3.3", "natives-macos"},
              {"org/lwjgl", "lwjgl-opengl", "3.3.3", "natives-macos"},
              {"org/lwjgl", "lwjgl-glfw", "3.3.3", "natives-macos"},
              {"org/lwjgl", "lwjgl-stb", "3.3.3", "natives-macos"},
              {"org/lwjgl", "lwjgl-tinyfd", "3.3.3", "natives-macos"},
              // Mojang kütüphaneleri - Minecraft için kritik
              {"com/mojang", "logging", "1.1.1"},
              {"com/mojang", "brigadier", "1.0.18"},
              {"com/mojang", "datafixerupper", "8.0.16"},
              {"com/mojang", "authlib", "6.0.54"},
              // JOML - Minecraft matematik kütüphanesi (kritik!)
              {"org/joml", "joml", "1.10.8"},
              // Fastutil - modlar için kritik
            {"it/unimi/dsi", "fastutil", "8.5.13"},
            // Apache Commons
            {"org/apache/commons", "commons-lang3", "3.14.0"},
            {"org/apache/commons", "commons-collections4", "4.4"},
            // Guava
            {"com/google/guava", "guava", "32.1.3-jre"},
            // Netty - C2ME için kritik (tüm modüller)
            {"io/netty", "netty-all", "4.1.109.Final"},
            {"io/netty", "netty-common", "4.1.109.Final"},
            {"io/netty", "netty-buffer", "4.1.109.Final"},
            {"io/netty", "netty-transport", "4.1.109.Final"},
            {"io/netty", "netty-codec", "4.1.109.Final"},
            {"io/netty", "netty-handler", "4.1.109.Final"},
            // Minecraft için gerekli ek kütüphaneler
            {"com/google/code/gson", "gson", "2.10.1"},
            {"commons-io", "commons-io", "2.15.1"},
            // Log4j
            {"org/apache/logging/log4j", "log4j-api", "2.22.1"},
            {"org/apache/logging/log4j", "log4j-core", "2.22.1"},
            {"org/apache/logging/log4j", "log4j-slf4j2-impl", "2.22.1"},
            // OSHI - Hardware info for Minecraft
            {"com/github/oshi", "oshi-core", "6.8.0"},
            // JNA - Java Native Access for Windows/Linux/Mac native operations
            {"net/java/dev/jna", "jna", "5.16.0"},
            {"net/java/dev/jna", "jna-platform", "5.16.0"},
              {"com/mojang", "jtracy", "1.0.29"},
              {"com/ibm/icu", "icu4j", "75.1"},
              // ... within slf4jLibraries initialization, LWJGL listinin hemen altına ekle
              {"org/lwjgl", "lwjgl-openal", "3.3.6"},
              {"org/lwjgl", "lwjgl-openal", "3.3.6", "natives-windows"},
              {"org/lwjgl", "lwjgl-openal", "3.3.6", "natives-linux"},
              {"org/lwjgl", "lwjgl-openal", "3.3.6", "natives-macos"},
              // Apache Commons Codec – Sodium parmak izi için
              {"commons-codec", "commons-codec", "1.18.0"},
              {"com/mojang", "text2speech", "1.17.9"},
        };
        
        for (String[] lib : slf4jLibraries) {
            String groupPath = lib[0];
            String artifactId = lib[1];
            String version = lib[2];
            String classifier = lib.length > 3 ? lib[3] : null;
            
            // Maven path oluştur
            String fileName = classifier != null ? 
                artifactId + "-" + version + "-" + classifier + ".jar" :
                artifactId + "-" + version + ".jar";
                
            Path libPath = librariesDir.resolve(groupPath)
                    .resolve(artifactId)
                    .resolve(version)
                    .resolve(fileName);
            
            if (!Files.exists(libPath)) {
                Files.createDirectories(libPath.getParent());
                
                // Maven Central URL
                String downloadUrl = "https://repo1.maven.org/maven2/" + 
                    groupPath + "/" + artifactId + "/" + version + "/" + fileName;
                
                try {
                    Platform.runLater(() -> statusLabel.setText("🔧 Gerekli kütüphane indiriliyor: " + artifactId + 
                        (classifier != null ? " (natives)" : "")));
                    downloadFile(downloadUrl, libPath);
                    System.out.println("✅ Gerekli kütüphane indirildi: " + fileName);
                } catch (IOException e) {
                    System.out.println("⚠️ Kütüphane indirilemedi: " + fileName + " - " + e.getMessage());
                    
                    // Alternatif URL dene (Mojang için özel)
                    try {
                        String alternativeUrl;
                        if (groupPath.startsWith("com/mojang")) {
                            // Mojang libraries için özel URL
                            alternativeUrl = "https://libraries.minecraft.net/" + 
                                groupPath + "/" + artifactId + "/" + version + "/" + fileName;
                        } else {
                            // Diğer kütüphaneler için JCenter
                            alternativeUrl = "https://jcenter.bintray.com/" + 
                                groupPath + "/" + artifactId + "/" + version + "/" + fileName;
                        }
                        downloadFile(alternativeUrl, libPath);
                        System.out.println("✅ Alternative URL'den indirildi: " + fileName);
                    } catch (IOException e2) {
                        System.out.println("❌ Alternative URL de başarısız: " + fileName + " - " + e2.getMessage());
                    }
                }
            } else {
                System.out.println("✅ Kütüphane zaten mevcut: " + fileName);
            }
        }
    }

    /**
     * YENİ: LWJGL NATIVE DLL'LERİNİ ÇIKARIR
     */
    private void extractLWJGLNatives(Path librariesDir) throws IOException {
        System.out.println("🔧 LWJGL native'ları extract ediliyor...");
        Path nativesDir = TERRAMONIC_PATH.resolve("natives");
        
        // Önce eski native'ları temizle
        if (Files.exists(nativesDir)) {
            System.out.println("🧹 Eski native dosyalar temizleniyor...");
            try {
                Files.list(nativesDir)
                    .filter(path -> {
                        String fileName = path.toString().toLowerCase();
                        return fileName.endsWith(".dll") || fileName.endsWith(".so") || fileName.endsWith(".dylib");
                    })
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                            System.out.println("🗑️ Eski native silindi: " + path.getFileName());
                        } catch (IOException e) {
                            System.out.println("⚠️ Native silinemedi: " + path.getFileName());
                        }
                    });
            } catch (IOException e) {
                System.out.println("⚠️ Native klasörü temizlenemedi: " + e.getMessage());
            }
        }
        
        Files.createDirectories(nativesDir);
        
        // Platform detection
        String osName = System.getProperty("os.name").toLowerCase();
        String platform;
        if (osName.contains("win")) {
            platform = "natives-windows";
        } else if (osName.contains("linux")) {
            platform = "natives-linux";
        } else if (osName.contains("mac")) {
            platform = "natives-macos";
        } else {
            platform = "natives-windows"; // fallback
        }

        System.out.println("🖥️ Platform tespit edildi: " + platform);
        
        String[] lwjglModules = {"lwjgl", "lwjgl-opengl", "lwjgl-glfw", "lwjgl-stb", "lwjgl-tinyfd", "lwjgl-openal"};
        
        for (String module : lwjglModules) {
            // 3.3.3 (diğer modüller) ve 3.3.6 (openal) sürümlerini sırayla dene
            Path nativeJarPath = null;
            for (String verToTry : new String[]{"3.3.3", "3.3.6"}) {
                Path p = librariesDir.resolve("org/lwjgl")
                        .resolve(module)
                        .resolve(verToTry)
                        .resolve(module + "-" + verToTry + "-" + platform + ".jar");
                if (Files.exists(p)) {
                    nativeJarPath = p;
                    break;
                }
            }
            if (nativeJarPath == null) {
                System.out.println("⚠️ Native JAR bulunamadı (" + module + "): hiçbiri");
                continue;
            }
            
            if (Files.exists(nativeJarPath)) {
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(nativeJarPath))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        String entryName = entry.getName();
                        // Platform specific native dosyaları
                        boolean isNative = (entryName.endsWith(".dll") || 
                                           entryName.endsWith(".so") || 
                                           entryName.endsWith(".dylib")) && !entry.isDirectory();
                        
                        if (isNative) {
                            // Sadece dosya adını al, path bilgisini kaldır
                            String fileName = Paths.get(entry.getName()).getFileName().toString();
                            Path dllPath = nativesDir.resolve(fileName);
                            
                            if (!Files.exists(dllPath)) {
                                // Parent klasörleri oluşturmaya gerek yok, sadece dosya adını kullan
                                try {
                                    Files.copy(zis, dllPath, StandardCopyOption.REPLACE_EXISTING);
                                    System.out.println("✅ Native DLL çıkarıldı: " + fileName);
                                } catch (IOException e) {
                                    System.out.println("⚠️ DLL çıkarılamadı: " + fileName + " - " + e.getMessage());
                                }
                            } else {
                                System.out.println("✅ DLL zaten mevcut: " + fileName);
                            }
                        }
                        zis.closeEntry();
                    }
                }
            } else {
                System.out.println("⚠️ Native JAR bulunamadı: " + nativeJarPath);
            }
        }
        
        // Natives klasöründeki native dosyaları listele
        try {
            long nativeCount = Files.list(nativesDir)
                    .filter(path -> {
                        String fileName = path.toString().toLowerCase();
                        return fileName.endsWith(".dll") || fileName.endsWith(".so") || fileName.endsWith(".dylib");
                    })
                    .count();
            System.out.println("📂 Toplam " + nativeCount + " adet native dosya natives klasöründe");
        } catch (IOException e) {
            System.out.println("⚠️ Natives klasörü listelenemedi: " + e.getMessage());
        }
    }

    /**
     * YENİ: GEREKLİ KÜTÜPHANELERİ CLASSPATH'E EKLER
     */
    private void addEssentialLibrariesToClasspath(StringBuilder classpath, Path librariesDir) {
        // SLF4J, LWJGL ve kritik kütüphaneler - modlar için
        String[][] essentialLibraries = {
            {"org/slf4j", "slf4j-api", "2.0.13"},
                          // JOptSimple - Minecraft için kritik
              {"net/sf/jopt-simple", "jopt-simple", "5.0.4"},
              // LWJGL 3.3.3 kütüphaneleri
              {"org/lwjgl", "lwjgl", "3.3.3"},
              {"org/lwjgl", "lwjgl-opengl", "3.3.3"},
              {"org/lwjgl", "lwjgl-glfw", "3.3.3"},
              {"org/lwjgl", "lwjgl-stb", "3.3.3"},
              {"org/lwjgl", "lwjgl-tinyfd", "3.3.3"},
              // Mojang kütüphaneleri - Minecraft için kritik
              {"com/mojang", "logging", "1.1.1"},
              {"com/mojang", "brigadier", "1.0.18"},
              {"com/mojang", "datafixerupper", "8.0.16"},
              {"com/mojang", "authlib", "6.0.54"},
              // JOML - Minecraft matematik kütüphanesi (kritik!)
              {"org/joml", "joml", "1.10.8"},
              // Fastutil - modlar için kritik
            {"it/unimi/dsi", "fastutil", "8.5.13"},
            // Apache Commons
            {"org/apache/commons", "commons-lang3", "3.14.0"},
            {"org/apache/commons", "commons-collections4", "4.4"},
            // Guava
            {"com/google/guava", "guava", "32.1.3-jre"},
            // Netty - C2ME için kritik (tüm modüller)
            {"io/netty", "netty-all", "4.1.109.Final"},
            {"io/netty", "netty-common", "4.1.109.Final"},
            {"io/netty", "netty-buffer", "4.1.109.Final"},
            {"io/netty", "netty-transport", "4.1.109.Final"},
            {"io/netty", "netty-codec", "4.1.109.Final"},
            {"io/netty", "netty-handler", "4.1.109.Final"},
            // Minecraft için gerekli ek kütüphaneler
            {"com/google/code/gson", "gson", "2.10.1"},
            {"commons-io", "commons-io", "2.15.1"},
            // Log4j
            {"org/apache/logging/log4j", "log4j-api", "2.22.1"},
            {"org/apache/logging/log4j", "log4j-core", "2.22.1"},
            {"org/apache/logging/log4j", "log4j-slf4j2-impl", "2.22.1"},
            // OSHI - Hardware info for Minecraft
            {"com/github/oshi", "oshi-core", "6.8.0"},
            // JNA - Java Native Access for Windows/Linux/Mac native operations
            {"net/java/dev/jna", "jna", "5.16.0"},
            {"net/java/dev/jna", "jna-platform", "5.16.0"},
              {"com/mojang", "jtracy", "1.0.29"},
              {"com/ibm/icu", "icu4j", "75.1"},
              // ... essentialLibraries dizisine ekle
              {"org/lwjgl", "lwjgl-openal", "3.3.6"},
              {"commons-codec", "commons-codec", "1.18.0"},
              {"com/mojang", "text2speech", "1.17.9"},
        };
        
        for (String[] lib : essentialLibraries) {
            String groupPath = lib[0];
            String artifactId = lib[1];
            String version = lib[2];
            
            Path libPath = librariesDir.resolve(groupPath)
                    .resolve(artifactId)
                    .resolve(version)
                    .resolve(artifactId + "-" + version + ".jar");
            
            if (Files.exists(libPath)) {
                if (classpath.length() > 0) {
                    classpath.append(";");
                }
                classpath.append(libPath.toString());
                System.out.println("✅ Essential library eklendi: " + artifactId);
            } else {
                System.out.println("⚠️ Essential library eksik: " + libPath);
            }
        }
    }

    /**
     * YENİ: GERÇEK OYUN BAŞLATMA METODU - KnotClient ile
     */
    private void launchGame() {
        Platform.runLater(() -> {
            if (!modsReady.get()) {
                showError("Modlar henüz hazır değil. Lütfen modların indirilmesini bekleyin.");
                return;
            }
            
            if (!librariesReady.get()) {
                showError("Minecraft kütüphaneleri henüz hazır değil. Lütfen bekleyin.");
                return;
            }
            
            // YENİ: UI'yi disable et
            gameIsLaunching = true;
            
            downloadProgress.setVisible(true);
            statusLabel.setVisible(true);
            statusLabel.setText("🚀 Oyun başlatılıyor...");
            statusLabel.setTextFill(PRIMARY_COLOR);
            playButton.setDisable(true);
            
            Task<Void> launchTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    // Seçilen RAM ve çözünürlük değerlerini al (ayarlar sekmesinden)
                    String selectedRam = ramCombo != null ? ramCombo.getSelectionModel().getSelectedItem() : "4 GB";
                    String selectedRes = resCombo != null ? resCombo.getSelectionModel().getSelectedItem() : "1280x720";
                    
                    if (selectedRam == null) selectedRam = "4 GB";
                    if (selectedRes == null) selectedRes = "1280x720";
                    
                    // RAM değerini parse et (örn: "8 GB" -> "8G")
                    String ramAmount = selectedRam.replace(" GB", "G");
                    
                    // Çözünürlük değerlerini parse et
                    String[] resParts = selectedRes.split("x");
                    String width = resParts[0];
                    String height = resParts[1];
                    
                    // Fabric version path
                    String fabricVersionName = "fabric-loader-" + FABRIC_VERSION + "-" + MINECRAFT_VERSION;
                    Path fabricVersionDir = TERRAMONIC_PATH.resolve("versions").resolve(fabricVersionName);
                    Path fabricJarPath = fabricVersionDir.resolve(fabricVersionName + ".jar");
                    
                    // Libraries classpath oluştur
                    StringBuilder classpath = new StringBuilder();
                    Path librariesDir = TERRAMONIC_PATH.resolve("libraries");
                    
                    // YENİ: İLK ÖNCE SLF4J VE LWJGL KÜTÜPHANELERİNİ EKLE
                    addEssentialLibrariesToClasspath(classpath, librariesDir);

                    // JTrace stub JAR'ını üret
                    Path jtracyStubJar = ensureJTraceStub(librariesDir);
                    if (jtracyStubJar != null && java.nio.file.Files.exists(jtracyStubJar)) {
                        if (classpath.length() > 0) {
                            classpath.append(";");
                        }
                        classpath.append(jtracyStubJar.toString());
                        System.out.println("✅ JTrace stub JAR classpath'e eklendi: " + jtracyStubJar);
                    }

                    // Gerekli temel kütüphaneleri ekle (tekrar çağrılmasına gerek yoktu)
                    // addEssentialLibrariesToClasspath(classpath, librariesDir); // ← kaldırıldı

                    // Fabric JSON'dan library'leri oku
                    Path fabricJsonPath = fabricVersionDir.resolve(fabricVersionName + ".json");
                    if (Files.exists(fabricJsonPath)) {
                        String fabricJsonContent = Files.readString(fabricJsonPath);
                        JSONObject fabricJson = new JSONObject(fabricJsonContent);
                        JSONArray libraries = fabricJson.getJSONArray("libraries");
                        
                        for (int i = 0; i < libraries.length(); i++) {
                            JSONObject library = libraries.getJSONObject(i);
                            String name = library.getString("name");
                            
                            // Maven path'e çevir
                            String[] nameParts = name.split(":");
                            String groupId = nameParts[0].replace(".", "/");
                            String artifactId = nameParts[1];
                            String version = nameParts[2];
                            
                            Path libraryPath = librariesDir.resolve(groupId)
                                    .resolve(artifactId)
                                    .resolve(version)
                                    .resolve(artifactId + "-" + version + ".jar");
                            
                            if (Files.exists(libraryPath)) {
                                if (classpath.length() > 0) {
                                    classpath.append(";");
                                }
                                classpath.append(libraryPath.toString());
                            }
                        }
                    }
                    
                    // Minecraft client JAR'ını da ekle
                    Path minecraftClientPath = TERRAMONIC_PATH.resolve("versions")
                        .resolve(MINECRAFT_VERSION)
                        .resolve(MINECRAFT_VERSION + ".jar");
                    
                    if (Files.exists(minecraftClientPath)) {
                        if (classpath.length() > 0) {
                            classpath.append(";");
                        }
                        classpath.append(minecraftClientPath.toString());
                        System.out.println("✅ Minecraft client JAR eklendi: " + MINECRAFT_VERSION + ".jar");
                    } else {
                        System.out.println("⚠️ Minecraft client JAR bulunamadı: " + minecraftClientPath);
                    }
                    
                    // Client JAR'ı classpath'e ekle
                    if (classpath.length() > 0) {
                        classpath.append(";");
                    }
                    classpath.append(fabricJarPath.toString());
                    
                    // YENİ: KnotClient ile Java command oluştur
                    List<String> command = new ArrayList<>();
                    command.add("java");
                    // RAM ve performans ayarları
                    command.add("-Xmx" + ramAmount);
                    command.add("-Xms1G");
                    // Modern JVM uyumluluk ayarları
                    command.add("--enable-native-access=ALL-UNNAMED");
                    command.add("--add-opens");
                    command.add("java.base/java.lang=ALL-UNNAMED");
                    command.add("--add-opens");
                    command.add("java.base/java.util=ALL-UNNAMED");
                    command.add("--add-opens");
                    command.add("java.base/java.io=ALL-UNNAMED");
                    // SLF4J çakışmasını önle
                    command.add("-Dorg.slf4j.simpleLogger.defaultLogLevel=WARN");
                    command.add("-Dlog4j2.formatMsgNoLookups=true");
                    // JTrace profiling'i devre dışı bırak (eksik kütüphane sorunu için)
                    command.add("-Dcom.mojang.jtracy.disable=true");
                    command.add("-Dmojang.tracy.enabled=false");
                    // LWJGL ayarı - modern LWJGL 3.3.3 için optimize
                    command.add("-Dorg.lwjgl.librarypath=" + TERRAMONIC_PATH.resolve("natives").toString());
                    // Minecraft launcher ayarları
                    command.add("-Dminecraft.launcher.brand=TerraMonic");
                    command.add("-Dminecraft.launcher.version=1.0.2");
                    // Classpath
                    command.add("-cp");
                    command.add(classpath.toString());
                    command.add("net.fabricmc.loader.impl.launch.knot.KnotClient"); // YENİ: KnotClient
                    command.add("--username");
                    command.add(playerName);
                    command.add("--version");
                    command.add(fabricVersionName);
                    command.add("--gameDir");
                    command.add(TERRAMONIC_PATH.toString());
                    command.add("--assetsDir");
                    command.add(TERRAMONIC_PATH.resolve("assets").toString());
                    command.add("--assetIndex");
                    command.add(MINECRAFT_VERSION);
                    command.add("--width");
                    command.add(width);
                    command.add("--height");
                    command.add(height);
                    
                    Platform.runLater(() -> statusLabel.setText("🎮 Minecraft başlatılıyor..."));
                    
                    // Process'i başlat
                    ProcessBuilder processBuilder = new ProcessBuilder(command);
                    processBuilder.directory(TERRAMONIC_PATH.toFile());
                    processBuilder.redirectErrorStream(true);
                    
                    Process process = processBuilder.start();
                    
                    System.out.println("🚀 Minecraft başlatıldı!");
                    System.out.println("📋 Komut: " + String.join(" ", command));
                    
                    // Process output'unu oku (opsiyonel - arkaplanda)
                    executorService.submit(() -> {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println("Minecraft: " + line);
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
                    gameIsLaunching = false; // YENİ: UI'yi tekrar enable et
                    downloadProgress.setVisible(false);
                    statusLabel.setText("✅ Minecraft başarıyla başlatıldı!");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                    playButton.setDisable(false);
                    
                    // 3 saniye sonra status'u gizle
                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), ev -> {
                        statusLabel.setVisible(false);
                    }));
                    timeline.play();
                });
            });
            
            launchTask.setOnFailed(e -> {
                Throwable exception = e.getSource().getException();
                System.out.println("❌ Oyun başlatma hatası: " + exception.getMessage());
                exception.printStackTrace();
                Platform.runLater(() -> {
                    gameIsLaunching = false; // YENİ: UI'yi tekrar enable et
                    downloadProgress.setVisible(false);
                    playButton.setDisable(false);
                    showError("Oyun başlatılamadı: " + exception.getMessage());
                });
            });
            
            executorService.submit(launchTask);
        });
    }

    /**
     * Iconu yükler ve launcher'ı başlatır
     */
    private void loadIconAndStart() {
        Task<Void> iconTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // ESKİ SİSTEM KORUNDU - PNG'yi indir
                boolean pngSuccess = downloadIconFile(ICON_URL, ICON_FILE);

                // PNG yüklenemediyse uygulama ikonu olmadan devam et
                if (pngSuccess) {
                    launcherIcon = new Image(ICON_FILE.toUri().toString(), true);
                }

                // CONFIG'İ SENKRON YÜKLE
                try {
                    System.out.println("🔄 (SYNC) JSON yükleniyor: " + LAUNCHER_JSON_URL);
                    String cfg = readJsonFromUrl(LAUNCHER_JSON_URL);
                    launcherConfig = new JSONObject(cfg);
                    System.out.println("✅ (SYNC) JSON yüklendi! Haber sayısı: " + (launcherConfig.has("haberler") ? launcherConfig.getJSONArray("haberler").length() : 0));
                    // Haberleri hazırla
                    loadNewsFromConfig();
                } catch (Exception ex) {
                    System.out.println("❌ (SYNC) JSON yüklenemedi: " + ex.getMessage());
                }

                // Gilroy fontu yükle
                try {
                    Font.loadFont(new URL(GILROY_FONT_URL).openStream(), 12);
                    System.out.println("✅ Gilroy Bold font yüklendi");
                } catch (Exception fe) {
                    System.out.println("⚠️ Font yüklenemedi: " + fe.getMessage());
                }

                return null;
            }
        };

        iconTask.setOnSucceeded(e -> {
            // Pencere ikonunu ayarla
            if (launcherIcon != null) {
                mainStage.getIcons().clear();
                mainStage.getIcons().add(launcherIcon);
                System.out.println("JavaFX Stage ikonu ayarlandı: " + ICON_FILE);
            } else {
                System.out.println("Launcher ikonu yüklenemedi, varsayılan ikon kullanılacak.");
            }

            // Görev çubuğu ve sistem tepsi ikonlarını güncelle
            updateSystemIcons();
            showSplashScreen();
            mainStage.show();

            // Show'dan sonra icon'u tekrar güncelle (bazı sistemlerde gerekli)
            Platform.runLater(() -> {
                updateSystemIcons();
            });
        });

        iconTask.setOnFailed(e -> {
            System.out.println("İkon yükleme hatası: " + e.getSource().getException());
            showSplashScreen();
            mainStage.show();
        });

        executorService.submit(iconTask);
    }

    /**
     * İkon dosyasını indirir ve kaydeder - ESKİ SİSTEM KORUNDU
     */
    private boolean downloadIconFile(String url, Path targetPath) {
        try {
            // Hedef klasörü oluştur
            Files.createDirectories(targetPath.getParent());

            // Dosya zaten varsa ve geçerliyse, tekrar indirme
            if (Files.exists(targetPath)) {
                try {
                    BufferedImage testImage = ImageIO.read(targetPath.toFile());
                    if (testImage != null) {
                        long fileSize = Files.size(targetPath);
                        if (fileSize > 1024) {
                            System.out.println("Mevcut PNG dosyası geçerli: " + targetPath);
                            return true;
                        }
                    }
                    System.out.println("Mevcut PNG dosyası geçersiz, siliniyor: " + targetPath);
                    Files.delete(targetPath);
                } catch (IOException e) {
                    System.out.println("Mevcut PNG dosyası kontrol edilirken hata: " + e.getMessage());
                    Files.deleteIfExists(targetPath);
                }
            }

            // URL bağlantısını aç
            URL downloadUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);

            // Yanıt kodunu kontrol et
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("İndirme başarısız, HTTP kodu: " + responseCode);
                return false;
            }

            // Content-Type kontrolü (PNG veya binary kabul et)
            String contentType = connection.getContentType();
            if (contentType == null ||
                    (!contentType.toLowerCase().contains("image/png") &&
                            !contentType.toLowerCase().contains("application/binary"))) {
                System.out.println("İndirilen dosya PNG formatında değil, Content-Type: " + contentType);
                return false;
            }

            // Dosyayı indir
            try (InputStream in = connection.getInputStream();
                 OutputStream out = Files.newOutputStream(targetPath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // İndirilen dosyanın geçerliliğini kontrol et
            try {
                BufferedImage testImage = ImageIO.read(targetPath.toFile());
                if (testImage == null) {
                    System.out.println("İndirilen PNG dosyası geçersiz: " + targetPath);
                    Files.deleteIfExists(targetPath);
                    return false;
                }
                long fileSize = Files.size(targetPath);
                System.out.println("PNG dosyası başarıyla indirildi: " + targetPath + ", Boyut: " + fileSize + " bayt");
                return true;
            } catch (IOException e) {
                System.out.println("İndirilen PNG dosyası okunamadı: " + e.getMessage());
                Files.deleteIfExists(targetPath);
                return false;
            }
        } catch (IOException e) {
            System.out.println("PNG indirme hatası: URL=" + url + ", Hata=" + e.getMessage());
            return false;
        }
    }

    /**
     * Config'den haberleri yükler
     */
    private void loadNewsFromConfig() {
        newsList.clear();

        if (launcherConfig != null && launcherConfig.has("haberler")) {
            JSONArray newsArray = launcherConfig.getJSONArray("haberler");
            for (int i = 0; i < newsArray.length(); i++) {
                JSONObject newsObj = newsArray.getJSONObject(i);
                String title = newsObj.getString("title");
                String content = newsObj.getString("content");
                String date = newsObj.getString("date");
                String typeStr = newsObj.getString("type");

                NewsItemType type;
                try {
                    type = NewsItemType.valueOf(typeStr);
                } catch (IllegalArgumentException e) {
                    type = NewsItemType.GENEL;
                }

                newsList.add(new NewsItem(title, content, date, type));
            }
        }

        if (newsList.isEmpty()) {
            newsList.add(new NewsItem(
                    "Haber Bulunamadı",
                    "Şu anda gösterilecek haber yok. Daha sonra tekrar kontrol edin.",
                    "N/A",
                    NewsItemType.GENEL
            ));
        }
    }

    /**
     * Config ve mods klasörünü temizler (launcher güncellendiğinde)
     */
    private void clearConfigAndMods() {
        try {
            Path configPath = TERRAMONIC_PATH.resolve("config");
            Path modsPath = TERRAMONIC_PATH.resolve("mods");

            if (Files.exists(configPath)) {
                deleteDirectory(configPath);
            }
            if (Files.exists(modsPath)) {
                deleteDirectory(modsPath);
            }

            // Klasörleri yeniden oluştur
            Files.createDirectories(configPath);
            Files.createDirectories(modsPath);

            System.out.println("Config ve mods klasörleri temizlendi - launcher güncellemesi nedeniyle");
        } catch (IOException e) {
            System.out.println("Klasör temizleme hatası: " + e.getMessage());
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
                    // Silinmiş modları kontrol et
                    Set<String> deletedMods = loadDeletedModsList();
                    
                    // Modrinth pack'i tekrar kontrol et
                    String modrinthUrl = launcherConfig.getString("modrinth_pack");
                    Path mrpackPath = Files.createTempFile("terramonic_pack_check", ".mrpack");
                    downloadFile(modrinthUrl, mrpackPath);
                    
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
                            
                            // Eğer mod launcher'dan silinmediyse ve eksikse, tekrar indir
                            if (!deletedMods.contains(fileName)) {
                                Path targetPath = modsDir.resolve(fileName);
                                if (!Files.exists(targetPath)) {
                                    JSONArray downloads = fileObj.getJSONArray("downloads");
                                    if (downloads.length() > 0) {
                                        String downloadUrl = downloads.getString(0);
                                        Platform.runLater(() -> statusLabel.setText("Eksik mod indiriliyor: " + fileName));
                                        downloadFile(downloadUrl, targetPath);
                                        repairedCount++;
                                    }
                                }
                            }
                        }
                    }
                    
                    // Temizlik
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
                
                // Geçici dosyaları temizle
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
     * Modrinth mod paketini indirir ve yükler - YENİ: AKILLI KONTROL SİSTEMİ
     */
    private void downloadAndInstallModrinthPack() {
        System.out.println("🔄 Modrinth pack kontrolü başlıyor...");
        System.out.println("   - launcherConfig null mu? " + (launcherConfig == null ? "❌ EVET" : "✅ HAYIR"));

        if (launcherConfig == null) {
            System.out.println("❌ LauncherConfig null - Modrinth pack yüklenemez");
            return;
        }

        System.out.println("   - modrinth_pack field var mı? " + (launcherConfig.has("modrinth_pack") ? "✅ VAR" : "❌ YOK"));

        if (!launcherConfig.has("modrinth_pack")) {
            System.out.println("❌ JSON'da modrinth_pack field'ı bulunamadı!");
            System.out.println("📋 Mevcut JSON field'ları:");
            for (String key : launcherConfig.keySet()) {
                System.out.println("   - " + key);
            }
            return;
        }

        String modrinthUrl = launcherConfig.getString("modrinth_pack");
        System.out.println("✅ Modrinth pack URL bulundu: " + modrinthUrl);

        // YENİ: MOD KONTROLÜ - MEVCUT MODLARI KONTROL ET
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
                System.out.println("🚀 Modrinth task başlıyor...");
                System.out.println("📥 İndirilecek URL: " + modrinthUrl);
                Platform.runLater(() -> {
                    statusLabel.setText("📦 Modlar indiriliyor...");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                });

                // .mrpack dosyasını indir (temp dizin)
                Path mrpackPath = Files.createTempFile("terramonic_pack", ".mrpack");
                System.out.println("📁 Mrpack dosyası indiriliyor: " + mrpackPath);
                downloadFile(modrinthUrl, mrpackPath);
                System.out.println("✅ Mrpack dosyası indirildi!");

                Platform.runLater(() -> {
                    statusLabel.setText("📂 Mod paketi çıkarılıyor...");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                });

                // .mrpack dosyasını çıkar (ZIP formatı)
                Path tempExtractDir = Files.createTempDirectory("terramonic_extract");
                if (Files.exists(tempExtractDir)) {
                    deleteDirectory(tempExtractDir);
                }
                Files.createDirectories(tempExtractDir);

                // ZIP olarak çıkar
                extractZip(mrpackPath, tempExtractDir);

                // modrinth.index.json'u oku
                Path indexPath = tempExtractDir.resolve("modrinth.index.json");
                System.out.println("📋 Index dosyası aranıyor: " + indexPath);
                if (!Files.exists(indexPath)) {
                    System.out.println("❌ modrinth.index.json bulunamadı!");
                    System.out.println("📁 Temp klasör içeriği:");
                    try {
                        Files.list(tempExtractDir).forEach(p -> System.out.println("   - " + p.getFileName()));
                    } catch (IOException e) {
                        System.out.println("   - Klasör listelenemedi: " + e.getMessage());
                    }
                    throw new IOException("modrinth.index.json bulunamadı!");
                }
                System.out.println("✅ Index dosyası bulundu!");

                String indexContent = Files.readString(indexPath);
                System.out.println("📄 Index dosyası okundu, uzunluk: " + indexContent.length());
                JSONObject indexJson = new JSONObject(indexContent);
                System.out.println("✅ Index JSON parse edildi!");

                Platform.runLater(() -> {
                    statusLabel.setText("⬬ Mod dosyaları indiriliyor...");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                });

                // Mods klasörünü temizle
                Path modsDir = TERRAMONIC_PATH.resolve("mods");
                System.out.println("🗂️ Mods klasörü temizleniyor: " + modsDir);
                if (Files.exists(modsDir)) {
                    deleteDirectory(modsDir);
                }
                Files.createDirectories(modsDir);
                System.out.println("✅ Mods klasörü hazırlandı!");

                // Her mod dosyasını indir
                JSONArray files = indexJson.getJSONArray("files");
                System.out.println("📦 Toplam dosya sayısı: " + files.length());
                int modCount = 0;
                for (int i = 0; i < files.length(); i++) {
                    JSONObject fileObj = files.getJSONObject(i);
                    String filePath = fileObj.getString("path");

                    // Sadece mods klasöründeki dosyaları işle
                    if (filePath.startsWith("mods/")) {
                        modCount++;
                        JSONArray downloads = fileObj.getJSONArray("downloads");
                        if (downloads.length() > 0) {
                            String downloadUrl = downloads.getString(0);
                            String fileName = Paths.get(filePath).getFileName().toString();
                            Path targetPath = modsDir.resolve(fileName);

                            Platform.runLater(() -> {
                                statusLabel.setText("📥 İndiriliyor: " + fileName);
                                statusLabel.setTextFill(PRIMARY_COLOR);
                            });
                            System.out.println("📥 [" + modCount + "] İndiriliyor: " + fileName);
                            downloadFile(downloadUrl, targetPath);
                            System.out.println("✅ [" + modCount + "] İndirildi: " + fileName);
                        } else {
                            System.out.println("⚠️ [" + modCount + "] Download URL bulunamadı: " + filePath);
                        }
                    } else {
                        System.out.println("⏭️ Mod olmayan dosya atlanıyor: " + filePath);
                    }
                }
                System.out.println("🎉 Toplam " + modCount + " mod indirildi!");

                // Temizlik
                System.out.println("🧹 Geçici dosyalar temizleniyor...");
                deleteDirectory(tempExtractDir);
                Files.deleteIfExists(mrpackPath);
                System.out.println("✅ Temizlik tamamlandı!");
                modsReady.set(true);
                refreshModPanelUI();

                Platform.runLater(() -> {
                    statusLabel.setText("✅ Modlar başarıyla yüklendi!");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                });
                System.out.println("🎊 Modrinth pack kurulumu başarıyla tamamlandı!");

                return null;
            }
        };

        modrinthTask.setOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            System.out.println("❌ Modrinth pack kurulumu başarısız!");
            System.out.println("💥 Hata detayı: " + exception.getClass().getSimpleName() + " - " + exception.getMessage());
            if (exception.getCause() != null) {
                System.out.println("🔗 Sebep: " + exception.getCause().getMessage());
            }
            exception.printStackTrace();

            Platform.runLater(() -> {
                statusLabel.setText("Modrinth pack kurulumu başarısız!");
                showError("Modrinth pack kurulumu başarısız:\n\n" + exception.getClass().getSimpleName() + ": " + exception.getMessage());
            });
        });

        executorService.submit(modrinthTask);
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
     * Fabric kurulumunu yapar - YENİ: LIBRARIES İNDİRME EKLENDİ
     */
    private void setupFabric() throws IOException, InterruptedException {
        String fabricVersionName = "fabric-loader-" + FABRIC_VERSION + "-" + MINECRAFT_VERSION;
        Path versionsDir = TERRAMONIC_PATH.resolve("versions");
        Path fabricVersionDir = versionsDir.resolve(fabricVersionName);
        Path fabricJarPath = fabricVersionDir.resolve(fabricVersionName + ".jar");
        Path fabricJsonPath = fabricVersionDir.resolve(fabricVersionName + ".json");

        // Fabric klasörünü oluştur
        Files.createDirectories(fabricVersionDir);

        // Client jar'ı indir
        if (!Files.exists(fabricJarPath) && launcherConfig != null && launcherConfig.has("jar")) {
            System.out.println("Client jar indiriliyor...");
            String clientJarUrl = launcherConfig.getString("jar");
            downloadFile(clientJarUrl, fabricJarPath);
            System.out.println("Client jar indirildi: " + fabricJarPath);
        }

        // Fabric installer'ı indir ve çalıştır
        if (!Files.exists(fabricJsonPath)) {
            System.out.println("Fabric installer indiriliyor...");

            Path fabricInstallerPath = TERRAMONIC_PATH.resolve("fabric-installer.jar");
            downloadFile(FABRIC_INSTALLER_URL, fabricInstallerPath);

            System.out.println("Fabric kuruluyor...");

            // Fabric installer'ı çalıştır
            ProcessBuilder fabricInstaller = new ProcessBuilder(
                    "java", "-jar", fabricInstallerPath.toString(),
                    "client",
                    "-mcversion", MINECRAFT_VERSION,
                    "-loader", FABRIC_VERSION,
                    "-dir", TERRAMONIC_PATH.toString(),
                    "-noprofile"
            );

            fabricInstaller.redirectErrorStream(true);
            Process fabricProcess = fabricInstaller.start();

            // Process output'unu oku
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(fabricProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Fabric Installer: " + line);
                }
            }

            int exitCode = fabricProcess.waitFor();

            if (exitCode != 0) {
                throw new IOException("Fabric kurulumu başarısız oldu, çıkış kodu: " + exitCode);
            }

            // Installer'ı sil
            Files.deleteIfExists(fabricInstallerPath);

            System.out.println("Fabric başarıyla kuruldu: " + fabricVersionName);
        }
        
        // YENİ: FABRIC KURULDUKTAN SONRA MİNECRAFT LİBRARİES'İ İNDİR
        Platform.runLater(() -> {
            downloadMinecraftLibraries();
        });
    }

    /**
     * Splash ekranını gösterir
     */
    private void showSplashScreen() {
        // İcon yükleme kontrolü (reused for both screens)
        final ImageView logoView = new ImageView();
        if (launcherIcon != null && !launcherIcon.isError()) {
            logoView.setImage(launcherIcon);
        } else {
            // İcon yerine text logo
            Text logoText = new Text("TerraMonic");
            logoText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 72));
            logoText.setFill(PRIMARY_COLOR);

            // Glow effect
            Glow glow = new Glow();
            glow.setLevel(0.6);
            logoText.setEffect(glow);
        }

        // Logo boyutu
        logoView.setFitWidth(240);
        logoView.setFitHeight(240);
        logoView.setPreserveRatio(true);

        // Glow efekti
        Glow glow = new Glow();
        glow.setLevel(0.3);
        logoView.setEffect(glow);

        // YENİ SİSTEM: Bakım modu kontrolü birleşik JSON'dan
        try {
            if (launcherConfig != null && launcherConfig.optBoolean("bakimmmodu", false)) {
                // Maintenance mode screen
                final Label maintenanceMessage = new Label("Şuan Bakım Modundayız.");
                maintenanceMessage.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
                maintenanceMessage.setTextFill(PRIMARY_COLOR);

                String maintenanceReason = launcherConfig.optString("bakimmmodusebebi", "Sebep belirtilmemiş.");
                final Label reasonLabel = new Label(maintenanceReason);
                reasonLabel.setFont(Font.font(FONT_FAMILY, FontWeight.LIGHT, 18));
                reasonLabel.setTextFill(TEXT_SECONDARY);

                // Düzenleme
                final VBox centerContent = new VBox(20);
                centerContent.setAlignment(Pos.CENTER);
                centerContent.getChildren().addAll(logoView, maintenanceMessage, reasonLabel);

                // Arka plan dekoratif çizgiler
                final AnchorPane decorPane = createDecorativeBackground();

                // Ana panel
                final StackPane root = new StackPane();
                root.setBackground(new Background(new BackgroundFill(
                        BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
                root.getChildren().addAll(decorPane, centerContent);

                // Sahne oluştur
                final Scene maintenanceScene = new Scene(root, windowWidth, windowHeight);
                maintenanceScene.setFill(BACKGROUND_COLOR);
                mainStage.setScene(maintenanceScene);
                currentScene = maintenanceScene;

                return; // Stop further processing
            }
        } catch (Exception e) {
            // JSON okuma hatası durumunda normal splash screen'e devam et
            System.out.println("Bakım modu kontrolü hatası: " + e.getMessage());
        }

        // Normal splash screen logic (if not in maintenance mode)
        // Alt başlık
        final Label subtitle = new Label("TerraMonic Launcher " + currentLauncherVersion);
        subtitle.setFont(Font.font(FONT_FAMILY, FontWeight.LIGHT, 24));
        subtitle.setTextFill(PRIMARY_COLOR);

        // Yükleniyor göstergesi
        final ProgressBar loadingBar = new ProgressBar(0);
        loadingBar.setPrefWidth(300);
        loadingBar.setStyle("-fx-accent: " + toHexString(PRIMARY_COLOR) + ";");

        // Durum etiketi
        final Label loadingLabel = new Label("Başlatılıyor...");
        loadingLabel.setTextFill(TEXT_SECONDARY);
        loadingLabel.setFont(Font.font(FONT_FAMILY, 14));

        // Düzenleme
        final VBox centerContent = new VBox(30);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(logoView, subtitle, new VBox(20), loadingBar, loadingLabel);

        // Arka plan dekoratif çizgiler
        final AnchorPane decorPane = createDecorativeBackground();

        // Ana panel
        final StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(
                BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        root.getChildren().addAll(decorPane, centerContent);

        // Sahne oluştur
        final Scene splashScene = new Scene(root, windowWidth, windowHeight);
        splashScene.setFill(BACKGROUND_COLOR);
        mainStage.setScene(splashScene);
        currentScene = splashScene;

        // Versiyon kontrol ve Fabric kurulum görevi
        Task<Void> setupTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // launcher_version.json oluştur veya kontrol et
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

                // YENİ BİRLEŞİK JSON SİSTEMİ + ESKİ SİSTEM KORUNDU
                if (launcherConfig != null) {
                    final String finalRemoteVersion = launcherConfig.getString("version");

                    if (!localVersion.equals(finalRemoteVersion)) {
                        Platform.runLater(() -> loadingLabel.setText("Güncelleme tespit edildi: v" + finalRemoteVersion));

                        // launcher_version.json'u güncelle
                        JSONObject updatedVersionJson = new JSONObject();
                        updatedVersionJson.put("current_version", finalRemoteVersion);
                        Files.writeString(LAUNCHER_VERSION_JSON, updatedVersionJson.toString());

                        // Update currentLauncherVersion and subtitle
                        currentLauncherVersion = "v" + finalRemoteVersion;
                        Platform.runLater(() -> subtitle.setText("TerraMonic Launcher v" + finalRemoteVersion));

                        // Config ve mods klasörünü temizle
                        clearConfigAndMods();
                    }
                }

                Platform.runLater(() -> loadingLabel.setText("Dosyalar kontrol ediliyor..."));

                // ESKİ SİSTEM KORUNDU - dosyalar.json kullanımı
                System.out.println("🔄 Dosyalar kontrolü:");
                System.out.println("   - launcherConfig null mu? " + (launcherConfig == null ? "❌ EVET" : "✅ HAYIR"));
                if (launcherConfig != null) {
                    System.out.println("   - dosyalar field var mı? " + (launcherConfig.has("dosyalar") ? "✅ VAR" : "❌ YOK"));

                    if (launcherConfig.has("dosyalar")) {
                        String zipUrl = launcherConfig.getString("dosyalar");
                        System.out.println("✅ Dosyalar URL bulundu: " + zipUrl);

                        // ZIP dosyasını indir ve .terramonic'e çıkar
                        Platform.runLater(() -> loadingLabel.setText("Dosyalar indiriliyor ve çıkarılıyor..."));

                        // Sadece geçerli URL'lerde download yap
                        if (!zipUrl.contains("placeholder")) {
                            downloadAndExtractZip(zipUrl, TERRAMONIC_PATH);
                        } else {
                            System.out.println("⏭️ Legacy ekdosyalar.zip sistemi devre dışı - atlanıyor");
                        }
                    } else {
                        System.out.println("⚠️ dosyalar field JSON'da bulunamadı - download atlanıyor");
                    }
                } else {
                    System.out.println("❌ launcherConfig null - dosyalar download edilemez");
                }

                Platform.runLater(() -> loadingLabel.setText("Fabric kurulumu kontrol ediliyor..."));

                // Fabric kurulumunu kontrol et ve kur
                try {
                    setupFabric();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Fabric kurulumu kesildi", ie);
                }

                Platform.runLater(() -> loadingLabel.setText("Modrinth modları kontrol ediliyor..."));

                // Modrinth modlarını indir
                System.out.println("🔄 Modrinth pack download başlatılıyor...");
                downloadAndInstallModrinthPack();

                return null;
            }
        };

        setupTask.setOnSucceeded(e -> {
            // Yükleme animasyonu
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

            // Logo animasyonu
            final ScaleTransition scaleAnimation = new ScaleTransition(Duration.seconds(2), logoView);
            scaleAnimation.setFromX(0.9);
            scaleAnimation.setFromY(0.9);
            scaleAnimation.setToX(1.0);
            scaleAnimation.setToY(1.0);
            scaleAnimation.setCycleCount(Animation.INDEFINITE);
            scaleAnimation.setAutoReverse(true);
            scaleAnimation.play();

            // Tüm içerik fade-in
            final FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), centerContent);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setCycleCount(1);
            fadeIn.play();

            // Yükleme bittiğinde login ekranına geç
            loadingAnimation.setOnFinished(event -> {
                // Logo animasyonunu durdur
                scaleAnimation.stop();

                // Fade-out animasyonu
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
            System.out.println("💥 Hata detayı: " + exception.getClass().getSimpleName() + " - " + exception.getMessage());
            if (exception.getCause() != null) {
                System.out.println("🔗 Sebep: " + exception.getCause().getMessage());
            }
            exception.printStackTrace();

            Platform.runLater(() -> {
                showError("Kurulum başarısız:\n\n" + exception.getClass().getSimpleName() + ": " + exception.getMessage());
                loadingLabel.setText("Hata oluştu!");
            });
        });

        executorService.submit(setupTask);
    }

    /**
     * ESKİ SİSTEM KORUNDU - Downloads ekdosyalar.zip, nests it in a complex-named ZIP, and extracts it to targetDir
     */
    private void downloadAndExtractZip(String zipUrl, Path targetDir) throws IOException {
        Path ekdosyalarZip = TERRAMONIC_PATH.resolve("ekdosyalar.zip");
        Path nestedZip = TERRAMONIC_PATH.resolve(generateComplexZipName());

        // Check if the nested ZIP exists and is valid
        boolean isNestedZipValid = Files.exists(nestedZip) && isValidZip(nestedZip);

        // Download ekdosyalar.zip if it doesn't exist or is invalid
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
            Platform.runLater(() -> { if(statusLabel!=null) statusLabel.setText("ekdosyalar.zip indirildi!"); });
        }

        // Create nested ZIP if it doesn't exist or is invalid
        if (!isNestedZipValid) {
            Files.createDirectories(nestedZip.getParent());
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(nestedZip))) {
                ZipEntry entry = new ZipEntry("ekdosyalar.zip");
                zos.putNextEntry(entry);
                Files.copy(ekdosyalarZip, zos);
                zos.closeEntry();
            }
            Platform.runLater(() -> { if(statusLabel!=null) statusLabel.setText("Nested ZIP oluşturuldu: " + nestedZip.getFileName()); });
        }

        // Check and extract missing files from nested ZIP
        checkAndExtractMissingFiles(nestedZip, targetDir);
    }

    /**
     * ESKİ SİSTEM - Validates if a file is a valid ZIP
     */
    private boolean isValidZip(Path zipPath) {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            return zis.getNextEntry() != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * ESKİ SİSTEM - Checks for missing files and extracts them from the nested ZIP
     */
    private void checkAndExtractMissingFiles(Path nestedZip, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(nestedZip))) {
            ZipEntry nestedEntry;
            while ((nestedEntry = zis.getNextEntry()) != null) {
                if (nestedEntry.getName().equals("ekdosyalar.zip")) {
                    // Create a temporary file to extract ekdosyalar.zip
                    Path tempEkdosyalar = Files.createTempFile("temp_ekdosyalar", ".zip");
                    try (OutputStream out = Files.newOutputStream(tempEkdosyalar)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }

                    // Now extract files from ekdosyalar.zip if they are missing
                    try (ZipInputStream innerZis = new ZipInputStream(Files.newInputStream(tempEkdosyalar))) {
                        ZipEntry innerEntry;
                        while ((innerEntry = innerZis.getNextEntry()) != null) {
                            Path destPath = targetDir.resolve(innerEntry.getName());
                            if (!Files.exists(destPath)) {
                                if (innerEntry.isDirectory()) {
                                    Files.createDirectories(destPath);
                                } else {
                                    Files.createDirectories(destPath.getParent());
                                    // Capture the file name in a local variable
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
        Platform.runLater(() -> { if(statusLabel!=null) statusLabel.setText("Dosya kontrolü ve çıkarma tamamlandı!"); });
    }

    /**
     * ESKİ SİSTEM - Generates a complex ZIP file name with random characters and numbers
     */
    private String generateComplexZipName() {
        StringBuilder complexName = new StringBuilder("TERRAMONIC_");
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();

        // Helper to add random string segment
        Consumer<Integer> addRandomSegment = length -> {
            for (int i = 0; i < length; i++) {
                complexName.append(characters.charAt(random.nextInt(characters.length())));
            }
        };

        // Build the complex name with segments
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

        // Arka plan grid çizgileri
        int lineCount = 20;
        for (int i = 0; i < lineCount; i++) {
            // Yatay çizgiler
            Line hLine = new Line(0, (windowHeight / lineCount) * i, windowWidth, (windowHeight / lineCount) * i);
            hLine.setStroke(Color.web("#222222", 0.3));
            hLine.setStrokeWidth(0.5);

            // Dikey çizgiler
            Line vLine = new Line((windowWidth / lineCount) * i, 0, (windowWidth / lineCount) * i, windowHeight);
            vLine.setStroke(Color.web("#222222", 0.3));
            vLine.setStrokeWidth(0.5);

            decorPane.getChildren().addAll(hLine, vLine);
        }

        // Vurgu çizgileri
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
        // Ana panel
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
                BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        // Arka plan dekoratif öğeler
        AnchorPane decorPane = createDecorativeBackground();

        // Sol panel - Logo
        VBox leftPanel = new VBox();
        leftPanel.setPadding(new Insets(50, 30, 50, 50));
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setMinWidth(windowWidth * 0.5);

        // Logo
        ImageView logoView = new ImageView();
        if (launcherIcon != null && !launcherIcon.isError()) {
            logoView.setImage(launcherIcon);
            logoView.setFitWidth(180);
            logoView.setFitHeight(180);
            logoView.setPreserveRatio(true);
        } else {
            // Alternatif text logo
            Text logoText = new Text("TerraMonic");
            logoText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 60));
            logoText.setFill(PRIMARY_COLOR);

            // Glow effect
            Glow glow = new Glow();
            glow.setLevel(0.6);
            logoText.setEffect(glow);
            leftPanel.getChildren().add(logoText);
        }

        // Slogan
        Label sloganLabel = new Label("Minecraft Deneyimini\nYeniden Keşfet");
        sloganLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 32));
        sloganLabel.setTextFill(TEXT_COLOR);
        sloganLabel.setWrapText(true);

        // Alt slogan
        Label subSloganLabel = new Label("TerraMonic sunucularına özel, optimize edilmiş launcher ile\noyun deneyimini maksimuma çıkar.");
        subSloganLabel.setFont(Font.font(FONT_FAMILY, 16));
        subSloganLabel.setTextFill(TEXT_SECONDARY);
        subSloganLabel.setWrapText(true);

        // Sol panel sunucu istatistikleri
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        // Çevrimiçi oyuncu
        VBox onlineStats = createStatBox("127", "Çevrimiçi Oyuncu");

        // Sunucu durumu
        VBox serverStats = createStatBox("AÇIK", "Sunucu Durumu");
        serverStats.getChildren().get(0).setStyle("-fx-text-fill: " + toHexString(PRIMARY_COLOR) + ";");

        statsBox.getChildren().addAll(onlineStats, serverStats);

        // Sol paneli düzenle
        leftPanel.getChildren().addAll(logoView, new VBox(20), sloganLabel, new VBox(10),
                subSloganLabel, new VBox(40), statsBox);

        // Sağ panel - Giriş formu
        VBox rightPanel = new VBox(20);
        rightPanel.setPadding(new Insets(50));
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setMinWidth(windowWidth * 0.5);
        rightPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_SECONDARY) + ";");

        // Giriş başlığı
        Label loginTitle = new Label("TerraMonic'e Hoşgeldiniz");
        loginTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 28));
        loginTitle.setTextFill(PRIMARY_COLOR);

        // Kullanıcı adı giriş alanı
        TextField usernameField = createStyledTextField("Minecraft kullanıcı adınız");

        // Checkbox
        CheckBox rememberBox = new CheckBox("Kullanıcı adımı hatırla");
        rememberBox.setFont(Font.font(FONT_FAMILY, 14));
        rememberBox.setTextFill(TEXT_COLOR);
        rememberBox.setSelected(rememberUser);

        // Giriş butonu
        Button loginButton = createStyledButton("GİRİŞ YAP", 200, 50);

        // Giriş butonuna aksiyon ekle
        loginButton.setOnAction(e -> {
            playerName = usernameField.getText().trim();
            rememberUser = rememberBox.isSelected();

            if (playerName.isEmpty()) {
                // Hata animasyonu
                shakeNode(usernameField);
                usernameField.setStyle(usernameField.getStyle() + "-fx-border-color: #FF3A3A;");
                return;
            }

            // Giriş başarılı ise ana ekrana geç
            playButtonClickAnimation(loginButton);
            transitionToMainScreen();
        });

        // Launcher versiyonu
        Label versionLabel = new Label("TerraMonic Launcher " + currentLauncherVersion);
        versionLabel.setFont(Font.font(FONT_FAMILY, 12));
        versionLabel.setTextFill(TEXT_SECONDARY);

        // Social media links
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

            // Hover efekti
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

        // Sağ paneli düzenle
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

        // Ana düzeni ayarla
        StackPane mainContent = new StackPane();
        mainContent.getChildren().addAll(decorPane);

        HBox contentBox = new HBox();
        contentBox.getChildren().addAll(leftPanel, rightPanel);
        mainContent.getChildren().add(contentBox);

        // Sürükle bırak için pencere kontrolü
        setupWindowControls(mainContent);

        // Üst çubuk ekle
        VBox rootWithTitleBar = new VBox();
        rootWithTitleBar.getChildren().addAll(createTitleBar(), mainContent);
        root.setCenter(rootWithTitleBar);

        // Yeni sahne oluştur ve animasyonla göster
        Scene loginScene = new Scene(root, windowWidth, windowHeight);
        loginScene.setFill(BACKGROUND_COLOR);

        // Sahne geçişi yap
        transitionToScene(loginScene);
    }

    /**
     * Ana ekrana geçiş yapar - ESKİ SİSTEM TAM OLARAK KORUNDU
     */
    private void transitionToMainScreen() {
        // Ana panel
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
                BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        // Üst çubuk ekle
        VBox rootWithTitleBar = new VBox();
        rootWithTitleBar.getChildren().add(createTitleBar());

        // Ana içerik bölümü
        BorderPane mainContent = new BorderPane();

        // Sol panel - Navigasyon
        VBox leftNav = createNavigationPanel(root);

        // Orta panel - Ana içerik
        centerPanel = new StackPane();
        centerPanel.setBackground(new Background(new BackgroundFill(
                BACKGROUND_SECONDARY, CornerRadii.EMPTY, Insets.EMPTY)));
        centerPanel.setPadding(new Insets(20));

        centerPanel.setPrefHeight(windowHeight - 100); // Sabit yükseklik
        centerPanel.setMinHeight(windowHeight - 100);  // Min yükseklik
        centerPanel.setMaxHeight(windowHeight - 100);  // Max yükseklik

        // Haberler panelini oluştur ve göster
        ScrollPane newsPanel = createNewsPanel();
        centerPanel.getChildren().add(newsPanel);

        // Sağ panel - Profil ve ayarlar
        VBox rightPanel = createProfilePanel();

        // Ana içeriği düzenle
        mainContent.setLeft(leftNav);
        mainContent.setCenter(centerPanel);
        mainContent.setRight(rightPanel);

        // Alt panel - Oyun başlatma kontrolleri
        HBox bottomPanel = createBottomPanel();
        mainContent.setBottom(bottomPanel);

        // Ana düzeni ayarla
        rootWithTitleBar.getChildren().add(mainContent);
        root.setCenter(rootWithTitleBar);

        // Yeni sahne oluştur ve göster
        Scene mainScene = new Scene(root, windowWidth, windowHeight);
        mainScene.setFill(BACKGROUND_COLOR);

        // Sahne geçişi yap
        transitionToScene(mainScene);
    }

    /**
     * Sol navigasyon panelini oluşturur - ESKİ SİSTEM TAM KORUNDU
     */
    private VBox createNavigationPanel(BorderPane root) {
        VBox navPanel = new VBox(10);
        navPanel.setPadding(new Insets(20, 15, 20, 15));
        navPanel.setPrefWidth(200);
        navPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        // Navigasyon öğeleri
        String[] navItems = {"Ana Sayfa", "Mod Paketleri", "Hesap", "Ayarlar"};
        String[] navIcons = {"🏠", "📦", "👤", "⚙"};

        // Seçili öğeyi takip etmek için değişken
        final int[] selectedIndex = {0}; // Ana Sayfa varsayılan olarak seçili
        currentNavIndex = 0;

        // LogoBox'u method kapsamında tanımla
        HBox logoBox = null;

        // Logo
        if (launcherIcon != null && !launcherIcon.isError()) {
            ImageView logoView = new ImageView(launcherIcon);
            logoView.setFitWidth(60);
            logoView.setFitHeight(60);
            logoView.setPreserveRatio(true);

            // Logo başlık
            Label logoTitle = new Label("TerraMonic");
            logoTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
            logoTitle.setTextFill(PRIMARY_COLOR);

            logoBox = new HBox(10);
            logoBox.setAlignment(Pos.CENTER_LEFT);
            logoBox.getChildren().addAll(logoView, logoTitle);

            navPanel.getChildren().add(logoBox);
            navPanel.getChildren().add(new Separator());
        }

        // Tüm menü öğelerini saklayacak liste
        List<HBox> menuItems = new ArrayList<>();

        // Her öğe için buton oluştur
        final HBox finalLogoBox = logoBox; // Lambda için final referans
        for (int i = 0; i < navItems.length; i++) {
            final int index = i;

            HBox navItem = new HBox(10);
            navItem.setPadding(new Insets(10, 15, 10, 15));
            navItem.setAlignment(Pos.CENTER_LEFT);
            navItem.setCursor(Cursor.HAND);

            // İkon ve metin
            Label iconLabel = new Label(navIcons[i]);
            iconLabel.setFont(Font.font(FONT_FAMILY, 16));

            Label textLabel = new Label(navItems[i]);
            textLabel.setFont(Font.font(FONT_FAMILY, 14));

            // Görsel öğeleri ekle
            navItem.getChildren().addAll(iconLabel, textLabel);

            // Menü öğesini listeye ekle
            menuItems.add(navItem);

            navPanel.getChildren().add(navItem);
        }

        // Menü öğelerinin görünümünü güncelleyen yardımcı metod
        Consumer<Integer> updateMenuItemStyles = (hoveredIndex) -> {
            for (int i = 0; i < menuItems.size(); i++) {
                HBox item = menuItems.get(i);
                Label itemIcon = (Label) item.getChildren().get(0);
                Label itemText = (Label) item.getChildren().get(1);

                if (i == selectedIndex[0]) {
                    // Seçili öğe
                    itemIcon.setTextFill(PRIMARY_COLOR);
                    itemText.setTextFill(PRIMARY_COLOR);
                    item.setStyle("-fx-background-color: #1A1A1A; -fx-background-radius: 5px;");
                } else if (i == hoveredIndex) {
                    // Üzerinde fare olan öğe
                    itemIcon.setTextFill(PRIMARY_COLOR);
                    itemText.setTextFill(PRIMARY_COLOR);
                    item.setStyle("-fx-background-color: #1A1A1A; -fx-background-radius: 5px;");
                } else {
                    // Diğer öğeler
                    itemIcon.setTextFill(TEXT_SECONDARY);
                    itemText.setTextFill(TEXT_SECONDARY);
                    item.setStyle("-fx-background-color: transparent;");
                }
            }
        };

        // İlk render için menü öğelerini güncelle
        updateMenuItemStyles.accept(-1);

        // Her öğe için olay işleyicilerini ekle
        for (int i = 0; i < menuItems.size(); i++) {
            final int index = i;
            HBox navItem = menuItems.get(i);

            // Hover efekti
            navItem.setOnMouseEntered(e -> {
                updateMenuItemStyles.accept(index);
            });

            navItem.setOnMouseExited(e -> {
                updateMenuItemStyles.accept(-1);
            });

            // Tıklama efekti
            navItem.setOnMousePressed(e -> {
                navItem.setStyle("-fx-background-color: #151515; -fx-background-radius: 5px;");
            });

            navItem.setOnMouseReleased(e -> {
                // Seçili öğeyi güncelle
                selectedIndex[0] = index;
                currentNavIndex = index;
                updateMenuItemStyles.accept(-1);

                // İçeriği değiştir
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

                            // Dinle
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

        // Boşluk
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        navPanel.getChildren().add(spacer);

        // Alt kısım - Versiyon - YENİ VERSİYONLAR
        Label versionLabel = new Label("TerraMonic " + currentLauncherVersion);
        versionLabel.setFont(Font.font(FONT_FAMILY, 12));
        versionLabel.setTextFill(TEXT_SECONDARY);

        // Minecraft versiyon + Fabric - YENİ SİSTEM
        Label mcVersionLabel = new Label("Minecraft " + MINECRAFT_VERSION + " + Fabric " + FABRIC_VERSION);
        mcVersionLabel.setFont(Font.font(FONT_FAMILY, 12));
        mcVersionLabel.setTextFill(TEXT_SECONDARY);

        VBox versionBox = new VBox(5);
        versionBox.getChildren().addAll(versionLabel, mcVersionLabel);
        navPanel.getChildren().add(versionBox);

        return navPanel;
    }

    /**
     * Alt paneli (oyun başlatma kontrolleri) oluşturur - YENİ: SADECE OYUN BAŞLAT BUTONU
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
        // Sabit genişlik: Pencere genişliğine eşitle
        bottomPanel.setPrefWidth(windowWidth);
        bottomPanel.setMinWidth(windowWidth);
        bottomPanel.setMaxWidth(windowWidth);

        addSystemTrayIcon();

        playButton = createStyledButton("🚀 OYUNU BAŞLAT", 220, 45);
        playButton.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        playButton.setAlignment(Pos.CENTER);
        playButton.setTranslateX(-20); // Sola kaydır

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

    // Geriye kalan tüm UI ve helper metodları
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
                
                // YENİ: SİLİNMİŞ MODLAR LİSTESİNİ TEMİZLE
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
                    // YENİ: MODİ SİLİNMİŞ OLARAK İŞARETLE
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

        // YENİ: OYUN AYARLARI BÖLÜMÜ
        Label gameSettingsTitle = new Label("Oyun Ayarları");
        gameSettingsTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        gameSettingsTitle.setTextFill(TEXT_COLOR);

        // RAM AYARI
        Label ramLabel = new Label("RAM Miktarı:");
        ramLabel.setFont(Font.font(FONT_FAMILY, 14));
        ramLabel.setTextFill(TEXT_COLOR);

        // Sistem RAM'ini kontrol et
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

        // ÇÖZÜNÜRLüK AYARI
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

        // LAUNCHER AYARLARI
        Label launcherSettingsTitle = new Label("Launcher Ayarları");
        launcherSettingsTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        launcherSettingsTitle.setTextFill(TEXT_COLOR);

        // MOD KONTROLü
        Button checkModsButton = createStyledButton("MODLARI KONTROL ET", 200, 40);
        checkModsButton.setOnAction(e -> {
            if (!gameIsLaunching) {
                checkAndRepairMods();
            }
        });

        // CACHE TEMİZLE
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

    // YENİ: HELPER METODLARI
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
     * Sistem tepsisine ikon ekler - ESKİ SİSTEM KORUNDU
     */
    private void addSystemTrayIcon() {
        if (SystemTray.isSupported()) {
            System.out.println("Sistem tepsisi destekleniyor, ikon yükleniyor...");
            try {
                SystemTray tray = SystemTray.getSystemTray();
                BufferedImage trayIconImage = null;

                if (Files.exists(ICON_FILE)) {
                    System.out.println("PNG dosyası bulundu: " + ICON_FILE.toString());
                    try {
                        trayIconImage = ImageIO.read(ICON_FILE.toFile());
                        if (trayIconImage != null) {
                            System.out.println("PNG başarıyla yüklendi, boyut: " + trayIconImage.getWidth() + "x" + trayIconImage.getHeight());
                        } else {
                            System.out.println("PNG yüklenemedi, null döndü.");
                        }
                    } catch (IOException e) {
                        System.out.println("PNG dosyası okunamadı: " + e.getMessage());
                    }
                } else {
                    System.out.println("PNG dosyası bulunamadı: " + ICON_FILE.toString());
                }

                if (trayIconImage == null) {
                    System.out.println("Fallback görsel oluşturuluyor...");
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
        } else {
            System.out.println("Sistem tepsisi desteklenmiyor.");
        }
    }

    /**
     * Utility metodları
     */
    private void downloadFile(String url, Path target) throws IOException {
        try (InputStream in = new URL(url).openStream();
             OutputStream out = Files.newOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private String readJsonFromUrl(String url) throws IOException {
        if (url.startsWith("file://")) {
            Path filePath = Paths.get(url.substring(7));
            return Files.readString(filePath);
        } else {
            URL jsonUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) jsonUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
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

    // YENİ: POPUP YERİNE STATUSLABEL KULLAN
    private void showError(String message) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText("❌ " + message);
                statusLabel.setTextFill(Color.web("#FF3A3A"));
                statusLabel.setVisible(true);
                
                // 5 saniye sonra gizle
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
                
                // 3 saniye sonra gizle
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
                    System.out.println("Görev çubuğu ikonu ayarlandı: " + ICON_FILE);
                } catch (UnsupportedOperationException | SecurityException e) {
                    System.out.println("Görev çubuğu ikonu güncellenemedi: " + e.getMessage());
                }
            } else {
                System.out.println("Taskbar ICON_IMAGE özelliği desteklenmiyor.");
            }
        } else {
            System.out.println("Taskbar desteklenmiyor.");
        }

        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                TrayIcon trayIcon = new TrayIcon(iconImage, "TerraMonic Launcher");
                trayIcon.setImageAutoSize(true);

                PopupMenu popup = new PopupMenu();
                MenuItem exitItem = new MenuItem("Çıkış");
                exitItem.addActionListener(event -> Platform.exit());
                popup.add(exitItem);
                trayIcon.setPopupMenu(popup);

                tray.add(trayIcon);
                System.out.println("Sistem tepsisi ikonu ayarlandı: " + ICON_FILE);
            } catch (AWTException e) {
                System.out.println("Sistem tepsisi ikonu ayarlanamadı: " + e.getMessage());
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

    /**
     * JTrace stub (TracyClient/Zone) sınıflarını içeren küçük bir JAR oluşturur ve
     * libraries/com/mojang/jtracy/0.1.0/ dizinine yerleştirir.
     */
    private Path ensureJTraceStub(Path librariesDir) {
        Path stubJarPath = librariesDir.resolve("com/mojang/jtracy/0.1.0/jtracy-0.1.0.jar");
        try {
            if (java.nio.file.Files.exists(stubJarPath)) {
                try (java.util.jar.JarFile jf = new java.util.jar.JarFile(stubJarPath.toFile())) {
                    java.util.jar.JarEntry entry = jf.getJarEntry("com/mojang/jtracy/Zone.class");
                    if (entry != null) {
                        try (java.io.InputStream in = jf.getInputStream(entry)) {
                            byte[] hdr = in.readNBytes(8); // magic + minor + major
                            if (hdr.length == 8) {
                                int major = ((hdr[6] & 0xFF) << 8) | (hdr[7] & 0xFF);
                                if (major <= 65) { // Java 21 or lower
                                    return stubJarPath; // mevcut jar uygun
                                }
                            }
                        }
                    }
                } catch (Exception ignore) {}
                // Uygun değilse sil ve yeniden oluştur
                try { java.nio.file.Files.delete(stubJarPath); } catch (Exception ignore) {}
            }
            java.nio.file.Files.createDirectories(stubJarPath.getParent());

            // Kaynak .class dosyalarını bul
            String base = "/com/mojang/jtracy/";
            java.net.URL zoneRes = TerraMonicLauncher1.class.getResource(base + "Zone.class");
            java.net.URL tracyRes = TerraMonicLauncher1.class.getResource(base + "TracyClient.class");
            if (zoneRes == null || tracyRes == null) {
                // Derlenmiş sınıflar bulunamadıysa kaynaktan derle
                try {
                    javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
                    if (compiler == null) {
                        System.out.println("⚠️ JDK compiler not available, cannot compile JTrace stub");
                        return null;
                    }
                    java.nio.file.Path tempSrcDir = java.nio.file.Files.createTempDirectory("jtracy_src");

                    String zoneSrc = "package com.mojang.jtracy; public final class Zone implements AutoCloseable { public Zone(String name) {} @Override public void close() {} }";
                    String tracySrc = "package com.mojang.jtracy; public final class TracyClient { private static boolean a=false; private TracyClient(){} public static boolean isActive(){return a;} public static void beginFrame(){} public static void endFrame(){} public static Zone zone(String n){return new Zone(n);} }";

                    java.nio.file.Path zoneFile = tempSrcDir.resolve("Zone.java");
                    java.nio.file.Path tracyFile = tempSrcDir.resolve("TracyClient.java");
                    java.nio.file.Files.writeString(zoneFile, zoneSrc);
                    java.nio.file.Files.writeString(tracyFile, tracySrc);

                    int res = compiler.run(null, null, null,
                            "-g:none",
                            "--release", "21",
                            "-classpath", System.getProperty("java.class.path"),
                            zoneFile.toString(), tracyFile.toString());
                    if (res != 0) {
                        System.out.println("⚠️ JTrace stub kaynak derleme başarısız, kod=" + res);
                        return null;
                    }

                    // Derlenmiş sınıfların bulunduğu dizin tempSrcDir/com/mojang/jtracy/
                    java.nio.file.Path zoneClass = tempSrcDir.resolve("com/mojang/jtracy/Zone.class");
                    java.nio.file.Path tracyClass = tempSrcDir.resolve("com/mojang/jtracy/TracyClient.class");

                    try (java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(stubJarPath))) {
                        java.util.function.BiConsumer<java.nio.file.Path,String> addEntry = (p, entryName) -> {
                            try {
                                jos.putNextEntry(new java.util.jar.JarEntry(entryName));
                                java.nio.file.Files.copy(p, jos);
                                jos.closeEntry();
                            } catch (Exception ignore) {}
                        };
                        addEntry.accept(zoneClass, "com/mojang/jtracy/Zone.class");
                        addEntry.accept(tracyClass, "com/mojang/jtracy/TracyClient.class");
                    }
                    System.out.println("✅ JTrace stub JAR derlenerek oluşturuldu: " + stubJarPath);
                } catch (Exception ex2) {
                    System.out.println("⚠️ JTrace stub derleme hatası: " + ex2.getMessage());
                    return null;
                }
            } else {
                // mevcut akış
                try (java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(stubJarPath))) {
                    java.util.function.BiConsumer<java.net.URL,String> addEntry = (url, entryName) -> {
                        try (java.io.InputStream in = url.openStream()) {
                            jos.putNextEntry(new java.util.jar.JarEntry(entryName));
                            in.transferTo(jos);
                            jos.closeEntry();
                        } catch (Exception e) { /* ignore */ }
                    };
                    addEntry.accept(zoneRes, "com/mojang/jtracy/Zone.class");
                    addEntry.accept(tracyRes, "com/mojang/jtracy/TracyClient.class");
                }
                System.out.println("✅ JTrace stub JAR oluşturuldu: " + stubJarPath);
            }
        } catch (Exception ex) {
            System.out.println("⚠️ JTrace stub JAR oluşturulamadı: " + ex.getMessage());
        }
        return stubJarPath;
    }
}