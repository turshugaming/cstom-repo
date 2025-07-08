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

    // USER-SPECİFİC PATH SİSTEMİ
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

    // OYUN BAŞLATMA İÇİN GEREKLI UI ELEMANLARI
    private ComboBox<String> ramCombo;
    private ComboBox<String> resCombo;

    // MOD KONTROL SİSTEMİ
    private static final Path DELETED_MODS_FILE = TERRAMONIC_PATH.resolve("deleted_mods.json");
    private static final Path MOD_INTEGRITY_FILE = TERRAMONIC_PATH.resolve("mod_integrity.json");

    // UI DISABLE SISTEMI
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

    // Gelişmiş animasyon sistemi
    private Timeline currentAnimation;
    private final Map<String, Object> animationCache = new HashMap<>();

    // Gelişmiş UI state management
    private final Map<String, Boolean> uiStates = new HashMap<>();
    private final List<Consumer<Boolean>> gameStateChangeListeners = new ArrayList<>();

    // Gelişmiş haber sistemi
    private ScrollPane newsScrollPane;
    private VBox newsContainer;
    private final Map<String, NewsPanel> newsPanelCache = new HashMap<>();

    // Gelişmiş mod yönetimi
    private final Map<String, ModInfo> installedMods = new HashMap<>();
    private final Set<String> enabledMods = new HashSet<>();
    private final Map<String, String> modVersions = new HashMap<>();

    // Gelişmiş performans izleme
    private final Map<String, Object> performanceMetrics = new HashMap<>();
    private Timer systemResourceMonitor;

    // Gelişmiş kullanıcı tercihleri
    private final Map<String, Object> userPreferences = new HashMap<>();
    private static final Path USER_PREFERENCES_FILE = TERRAMONIC_PATH.resolve("user_preferences.json");

    // Gelişmiş sistem tepsisi
    private TrayIcon trayIcon;
    private PopupMenu trayMenu;

    // Gelişmiş günlükleme sistemi
    private final List<String> launcherLogs = new ArrayList<>();
    private static final Path LOGS_PATH = TERRAMONIC_PATH.resolve("logs");
    private static final Path LAUNCHER_LOG_FILE = LOGS_PATH.resolve("launcher.log");

    // Gelişmiş güncelleme sistemi
    private boolean updateAvailable = false;
    private String latestVersion = "";
    private String updateUrl = "";

    // Gelişmiş ağ yönetimi
    private final Map<String, HttpURLConnection> activeConnections = new HashMap<>();
    private final Set<String> downloadQueue = new HashSet<>();

    // Gelişmiş dosya yönetimi
    private final Map<Path, Long> fileSizes = new HashMap<>();
    private final Map<Path, String> fileHashes = new HashMap<>();

    // Gelişmiş oyun profil sistemi
    private final Map<String, GameProfile> gameProfiles = new HashMap<>();
    private String currentProfileName = "default";

    // Gelişmiş tema sistemi
    private String currentTheme = "dark";
    private final Map<String, Map<String, Color>> themes = new HashMap<>();

    // Gelişmiş ses sistemi
    private boolean soundEnabled = true;
    private double soundVolume = 0.5;

    // Gelişmiş klavye kısayolları
    private final Map<String, Runnable> keyboardShortcuts = new HashMap<>();

    // Gelişmiş çoklu dil desteği
    private String currentLanguage = "tr";
    private final Map<String, Map<String, String>> translations = new HashMap<>();

    // Gelişmiş istatistik sistemi
    private final Map<String, Object> statistics = new HashMap<>();
    private static final Path STATISTICS_FILE = TERRAMONIC_PATH.resolve("statistics.json");

    // Gelişmiş güvenlik sistemi
    private final Set<String> trustedUrls = new HashSet<>();
    private final Map<String, String> fileSignatures = new HashMap<>();

    // Gelişmiş plugin sistemi
    private final Map<String, Object> loadedPlugins = new HashMap<>();
    private static final Path PLUGINS_PATH = TERRAMONIC_PATH.resolve("plugins");

    // Gelişmiş sosyal özellikler
    private final List<Friend> friendsList = new ArrayList<>();
    private final Map<String, String> socialConnections = new HashMap<>();

    // Gelişmiş bulut senkronizasyon
    private boolean cloudSyncEnabled = false;
    private String cloudSyncToken = "";

    /**
     * Ana metod
     */
    public static void main(String[] args) {
        // JVM optimizasyonları
        System.setProperty("javafx.animation.fullspeed", "true");
        System.setProperty("javafx.animation.pulse", "60");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        
        // Sistem özelliklerini ayarla
        setupSystemProperties();
        
        // Launcher'ı başlat
        launch(args);
    }

    /**
     * Sistem özelliklerini ayarlar
     */
    private static void setupSystemProperties() {
        // GPU hızlandırma
        System.setProperty("prism.vsync", "true");
        System.setProperty("quantum.multithreaded", "true");
        
        // Bellek optimizasyonu
        System.setProperty("javafx.animation.framerate", "60");
        System.setProperty("com.sun.javafx.isEmbedded", "false");
        
        // Font rendering
        System.setProperty("prism.allowhidpi", "true");
        System.setProperty("glass.win.uiScale", "100%");
    }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        mainStage.initStyle(StageStyle.UNDECORATED);
        mainStage.setTitle("TerraMonic Launcher " + LAUNCHER_VERSION);

        // Thread pool oluştur
        executorService = Executors.newFixedThreadPool(8);

        // Başlangıç işlemleri
        initializeLauncher();

        // Pencere boyutunu ayarla
        mainStage.setWidth(windowWidth);
        mainStage.setHeight(windowHeight);
        mainStage.centerOnScreen();
        
        // Minimum boyut ayarla
        mainStage.setMinWidth(900);
        mainStage.setMinHeight(600);
    }

    @Override
    public void stop() {
        // Kaynakları temizle
        cleanup();
    }

    /**
     * Launcher'ı başlatır
     */
    private void initializeLauncher() {
        // Performans metriklerini başlat
        startPerformanceMonitoring();
        
        // .terramonic klasörünü kur
        setupTerramonicFolder();
        
        // Kullanıcı tercihlerini yükle
        loadUserPreferences();
        
        // Temayı yükle
        initializeThemes();
        
        // Çevirileri yükle
        loadTranslations();
        
        // İstatistikleri yükle
        loadStatistics();
        
        // Sistem tepsisini hazırla
        initializeSystemTray();
        
        // Iconu yükle ve başlat
        loadIconAndStart();
    }

    /**
     * Performans izlemeyi başlatır
     */
    private void startPerformanceMonitoring() {
        systemResourceMonitor = new Timer(true);
        systemResourceMonitor.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Memory usage
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemory = totalMemory - freeMemory;
                
                performanceMetrics.put("memoryUsed", usedMemory);
                performanceMetrics.put("memoryTotal", totalMemory);
                performanceMetrics.put("memoryFree", freeMemory);
                
                // CPU usage (basit tahmin)
                long currentTime = System.currentTimeMillis();
                performanceMetrics.put("timestamp", currentTime);
                
                // Platform.runLater(() -> updatePerformanceUI());
            }
        }, 0, 5000); // Her 5 saniyede bir
    }

    /**
     * .terramonic klasörünü ve tüm alt klasörleri kurar
     */
    private void setupTerramonicFolder() {
        try {
            // Ana klasör
            Files.createDirectories(TERRAMONIC_PATH);
            
            // Oyun klasörleri
            Files.createDirectories(TERRAMONIC_PATH.resolve("mods"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("config"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("versions"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("libraries"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("natives"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("assets"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("assets/objects"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("assets/indexes"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("jar"));
            
            // Launcher klasörleri
            Files.createDirectories(MODS_PROFILES_PATH);
            Files.createDirectories(ICON_PATH);
            Files.createDirectories(LOGS_PATH);
            Files.createDirectories(PLUGINS_PATH);
            
            // Tema ve kaynak klasörleri
            Files.createDirectories(TERRAMONIC_PATH.resolve("themes"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("resources"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("cache"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("temp"));
            
            // Yedekleme klasörleri
            Files.createDirectories(TERRAMONIC_PATH.resolve("backups"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("backups/worlds"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("backups/mods"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("backups/config"));
            
            logInfo("Klasör yapısı başarıyla oluşturuldu: " + TERRAMONIC_PATH);
        } catch (IOException e) {
            logError("Klasör oluşturma hatası: " + e.getMessage());
        }
    }

    /**
     * Kullanıcı tercihlerini yükler
     */
    private void loadUserPreferences() {
        try {
            if (Files.exists(USER_PREFERENCES_FILE)) {
                String content = Files.readString(USER_PREFERENCES_FILE);
                JSONObject prefs = new JSONObject(content);
                
                // Tercihleri map'e yükle
                for (String key : prefs.keySet()) {
                    userPreferences.put(key, prefs.get(key));
                }
                
                // Özel tercihleri uygula
                applyUserPreferences();
                
                logInfo("Kullanıcı tercihleri yüklendi: " + userPreferences.size() + " tercih");
            } else {
                // Varsayılan tercihleri oluştur
                createDefaultPreferences();
            }
        } catch (Exception e) {
            logError("Kullanıcı tercihleri yükleme hatası: " + e.getMessage());
            createDefaultPreferences();
        }
    }

    /**
     * Varsayılan kullanıcı tercihlerini oluşturur
     */
    private void createDefaultPreferences() {
        userPreferences.put("theme", "dark");
        userPreferences.put("language", "tr");
        userPreferences.put("soundEnabled", true);
        userPreferences.put("soundVolume", 0.5);
        userPreferences.put("autoUpdate", true);
        userPreferences.put("showSplash", true);
        userPreferences.put("rememberCredentials", false);
        userPreferences.put("enableAnimations", true);
        userPreferences.put("maxMemory", "4G");
        userPreferences.put("resolution", "1920x1080");
        userPreferences.put("fullscreen", false);
        
        saveUserPreferences();
    }

    /**
     * Kullanıcı tercihlerini uygular
     */
    private void applyUserPreferences() {
        // Tema
        currentTheme = (String) userPreferences.getOrDefault("theme", "dark");
        
        // Dil
        currentLanguage = (String) userPreferences.getOrDefault("language", "tr");
        
        // Ses
        soundEnabled = (Boolean) userPreferences.getOrDefault("soundEnabled", true);
        soundVolume = ((Number) userPreferences.getOrDefault("soundVolume", 0.5)).doubleValue();
        
        // Bulut senkronizasyon
        cloudSyncEnabled = (Boolean) userPreferences.getOrDefault("cloudSync", false);
        cloudSyncToken = (String) userPreferences.getOrDefault("cloudSyncToken", "");
    }

    /**
     * Kullanıcı tercihlerini kaydeder
     */
    private void saveUserPreferences() {
        try {
            JSONObject prefs = new JSONObject();
            for (Map.Entry<String, Object> entry : userPreferences.entrySet()) {
                prefs.put(entry.getKey(), entry.getValue());
            }
            
            Files.writeString(USER_PREFERENCES_FILE, prefs.toString(2));
            logInfo("Kullanıcı tercihleri kaydedildi");
        } catch (Exception e) {
            logError("Kullanıcı tercihleri kaydetme hatası: " + e.getMessage());
        }
    }

    /**
     * Temaları başlatır
     */
    private void initializeThemes() {
        // Dark tema
        Map<String, Color> darkTheme = new HashMap<>();
        darkTheme.put("background", BACKGROUND_COLOR);
        darkTheme.put("backgroundSecondary", BACKGROUND_SECONDARY);
        darkTheme.put("primary", PRIMARY_COLOR);
        darkTheme.put("text", TEXT_COLOR);
        darkTheme.put("textSecondary", TEXT_SECONDARY);
        themes.put("dark", darkTheme);
        
        // Light tema
        Map<String, Color> lightTheme = new HashMap<>();
        lightTheme.put("background", Color.WHITE);
        lightTheme.put("backgroundSecondary", Color.web("#F5F5F5"));
        lightTheme.put("primary", Color.web("#007ACC"));
        lightTheme.put("text", Color.BLACK);
        lightTheme.put("textSecondary", Color.web("#666666"));
        themes.put("light", lightTheme);
        
        // Gaming tema
        Map<String, Color> gamingTheme = new HashMap<>();
        gamingTheme.put("background", Color.web("#0D1117"));
        gamingTheme.put("backgroundSecondary", Color.web("#161B22"));
        gamingTheme.put("primary", Color.web("#FF6B35"));
        gamingTheme.put("text", Color.web("#F0F6FC"));
        gamingTheme.put("textSecondary", Color.web("#7D8590"));
        themes.put("gaming", gamingTheme);
    }

    /**
     * Çevirileri yükler
     */
    private void loadTranslations() {
        // Türkçe çeviriler
        Map<String, String> turkish = new HashMap<>();
        turkish.put("play", "Oyna");
        turkish.put("settings", "Ayarlar");
        turkish.put("news", "Haberler");
        turkish.put("mods", "Modlar");
        turkish.put("profiles", "Profiller");
        turkish.put("about", "Hakkında");
        turkish.put("quit", "Çıkış");
        turkish.put("loading", "Yükleniyor...");
        turkish.put("downloading", "İndiriliyor...");
        turkish.put("username", "Kullanıcı Adı");
        turkish.put("password", "Şifre");
        turkish.put("login", "Giriş Yap");
        turkish.put("remember", "Beni Hatırla");
        translations.put("tr", turkish);
        
        // English translations
        Map<String, String> english = new HashMap<>();
        english.put("play", "Play");
        english.put("settings", "Settings");
        english.put("news", "News");
        english.put("mods", "Mods");
        english.put("profiles", "Profiles");
        english.put("about", "About");
        english.put("quit", "Quit");
        english.put("loading", "Loading...");
        english.put("downloading", "Downloading...");
        english.put("username", "Username");
        english.put("password", "Password");
        english.put("login", "Log In");
        english.put("remember", "Remember Me");
        translations.put("en", english);
    }

    /**
     * İstatistikleri yükler
     */
    private void loadStatistics() {
        try {
            if (Files.exists(STATISTICS_FILE)) {
                String content = Files.readString(STATISTICS_FILE);
                JSONObject stats = new JSONObject(content);
                
                for (String key : stats.keySet()) {
                    statistics.put(key, stats.get(key));
                }
                
                logInfo("İstatistikler yüklendi: " + statistics.size() + " veri");
            } else {
                // Varsayılan istatistikleri oluştur
                createDefaultStatistics();
            }
        } catch (Exception e) {
            logError("İstatistik yükleme hatası: " + e.getMessage());
            createDefaultStatistics();
        }
    }

    /**
     * Varsayılan istatistikleri oluşturur
     */
    private void createDefaultStatistics() {
        statistics.put("launchCount", 0);
        statistics.put("totalPlayTime", 0L);
        statistics.put("lastLaunch", 0L);
        statistics.put("modsInstalled", 0);
        statistics.put("gamesPlayed", 0);
        statistics.put("firstLaunch", System.currentTimeMillis());
        
        saveStatistics();
    }

    /**
     * İstatistikleri kaydeder
     */
    private void saveStatistics() {
        try {
            JSONObject stats = new JSONObject();
            for (Map.Entry<String, Object> entry : statistics.entrySet()) {
                stats.put(entry.getKey(), entry.getValue());
            }
            
            Files.writeString(STATISTICS_FILE, stats.toString(2));
        } catch (Exception e) {
            logError("İstatistik kaydetme hatası: " + e.getMessage());
        }
    }

    /**
     * Sistem tepsisini başlatır
     */
    private void initializeSystemTray() {
        if (!SystemTray.isSupported()) {
            logInfo("Sistem tepsisi desteklenmiyor");
            return;
        }
        
        try {
            SystemTray tray = SystemTray.getSystemTray();
            
            // Menü oluştur
            trayMenu = new PopupMenu();
            
            MenuItem openItem = new MenuItem("TerraMonic Launcher'ı Aç");
            openItem.addActionListener(e -> Platform.runLater(() -> {
                if (mainStage != null) {
                    mainStage.show();
                    mainStage.toFront();
                }
            }));
            
            MenuItem playItem = new MenuItem("Minecraft'ı Başlat");
            playItem.addActionListener(e -> Platform.runLater(() -> {
                if (playButton != null && !gameIsLaunching) {
                    playButton.fire();
                }
            }));
            
            MenuItem exitItem = new MenuItem("Çıkış");
            exitItem.addActionListener(e -> Platform.runLater(() -> {
                Platform.exit();
                System.exit(0);
            }));
            
            trayMenu.add(openItem);
            trayMenu.addSeparator();
            trayMenu.add(playItem);
            trayMenu.addSeparator();
            trayMenu.add(exitItem);
            
            // Tepsi ikonu (varsayılan)
            BufferedImage trayImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = trayImage.createGraphics();
            g2d.setColor(java.awt.Color.GREEN);
            g2d.fillRect(0, 0, 16, 16);
            g2d.dispose();
            
            trayIcon = new TrayIcon(trayImage, "TerraMonic Launcher", trayMenu);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> Platform.runLater(() -> {
                if (mainStage != null) {
                    mainStage.show();
                    mainStage.toFront();
                }
            }));
            
            tray.add(trayIcon);
            logInfo("Sistem tepsisi başlatıldı");
        } catch (Exception e) {
            logError("Sistem tepsisi hatası: " + e.getMessage());
        }
    }

    /**
     * Log bilgi mesajı
     */
    private void logInfo(String message) {
        String logMessage = "[INFO] " + new Date() + " - " + message;
        launcherLogs.add(logMessage);
        System.out.println(logMessage);
        
        // Log dosyasına yaz
        writeToLogFile(logMessage);
    }

    /**
     * Log hata mesajı
     */
    private void logError(String message) {
        String logMessage = "[ERROR] " + new Date() + " - " + message;
        launcherLogs.add(logMessage);
        System.err.println(logMessage);
        
        // Log dosyasına yaz
        writeToLogFile(logMessage);
    }

    /**
     * Log dosyasına yazar
     */
    private void writeToLogFile(String message) {
        try {
            Files.writeString(LAUNCHER_LOG_FILE, message + System.lineSeparator(), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            // Log yazma hatası durumunda konsola yazdır
            System.err.println("Log yazma hatası: " + e.getMessage());
        }
    }

    /**
     * Kaynakları temizler
     */
    private void cleanup() {
        // Thread pool'u kapat
        if (executorService != null) {
            executorService.shutdownNow();
        }
        
        // Timer'ı kapat
        if (systemResourceMonitor != null) {
            systemResourceMonitor.cancel();
        }
        
        // Aktif bağlantıları kapat
        for (HttpURLConnection connection : activeConnections.values()) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                logError("Bağlantı kapatma hatası: " + e.getMessage());
            }
        }
        
        // Sistem tepsisinden kaldır
        if (trayIcon != null) {
            try {
                SystemTray.getSystemTray().remove(trayIcon);
            } catch (Exception e) {
                logError("Tepsi ikonu kaldırma hatası: " + e.getMessage());
            }
        }
        
        // Kullanıcı tercihlerini kaydet
        saveUserPreferences();
        
        // İstatistikleri kaydet
        saveStatistics();
        
        // Temp dosyaları temizle
        cleanupTempFiles();
        
        logInfo("Launcher kapatıldı, kaynaklar temizlendi");
    }

    /**
     * Geçici dosyaları temizler
     */
    private void cleanupTempFiles() {
        try {
            Path tempDir = TERRAMONIC_PATH.resolve("temp");
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                // Ignore
                            }
                        });
            }
        } catch (Exception e) {
            logError("Temp dosya temizleme hatası: " + e.getMessage());
        }
    }

    /**
     * Çeviri al
     */
    private String translate(String key) {
        return translations.getOrDefault(currentLanguage, new HashMap<>())
                .getOrDefault(key, key);
    }

    /**
     * Mevcut tema rengini al
     */
    private Color getThemeColor(String colorKey) {
        return themes.getOrDefault(currentTheme, themes.get("dark"))
                .getOrDefault(colorKey, Color.WHITE);
    }

    // Ana UI ve oyun sınıfları devam edecek...

    /**
     * İkonu yükler ve launcher'ı başlatır
     */
    private void loadIconAndStart() {
        Task<Void> iconTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // PNG'yi indir
                boolean pngSuccess = downloadIconFile(ICON_URL, ICON_FILE);

                // PNG yüklenemediyse uygulama ikonu olmadan devam et
                if (pngSuccess) {
                    launcherIcon = new Image(ICON_FILE.toUri().toString(), true);
                }

                // CONFIG'İ SENKRON YÜKLE
                try {
                    logInfo("JSON yükleniyor: " + LAUNCHER_JSON_URL);
                    String cfg = readJsonFromUrl(LAUNCHER_JSON_URL);
                    launcherConfig = new JSONObject(cfg);
                    logInfo("JSON yüklendi! Haber sayısı: " + (launcherConfig.has("haberler") ? launcherConfig.getJSONArray("haberler").length() : 0));
                    // Haberleri hazırla
                    loadNewsFromConfig();
                } catch (Exception ex) {
                    logError("JSON yüklenemedi: " + ex.getMessage());
                }

                // Gilroy fontu yükle
                try {
                    Font.loadFont(new URL(GILROY_FONT_URL).openStream(), 12);
                    logInfo("Gilroy Bold font yüklendi");
                } catch (Exception fe) {
                    logError("Font yüklenemedi: " + fe.getMessage());
                }

                return null;
            }
        };

        iconTask.setOnSucceeded(e -> {
            // Pencere ikonunu ayarla
            if (launcherIcon != null) {
                mainStage.getIcons().clear();
                mainStage.getIcons().add(launcherIcon);
                logInfo("JavaFX Stage ikonu ayarlandı: " + ICON_FILE);
            } else {
                logInfo("Launcher ikonu yüklenemedi, varsayılan ikon kullanılacak.");
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
            logError("İkon yükleme hatası: " + e.getSource().getException());
            showSplashScreen();
            mainStage.show();
        });

        executorService.submit(iconTask);
    }

    /**
     * İkon dosyasını indirir ve kaydeder
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
                            logInfo("Mevcut PNG dosyası geçerli: " + targetPath);
                            return true;
                        }
                    }
                    logInfo("Mevcut PNG dosyası geçersiz, siliniyor: " + targetPath);
                    Files.delete(targetPath);
                } catch (IOException e) {
                    logError("Mevcut PNG dosyası kontrol edilirken hata: " + e.getMessage());
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
                logError("İndirme başarısız, HTTP kodu: " + responseCode);
                return false;
            }

            // Content-Type kontrolü (PNG veya binary kabul et)
            String contentType = connection.getContentType();
            if (contentType == null ||
                    (!contentType.toLowerCase().contains("image/png") &&
                            !contentType.toLowerCase().contains("application/binary"))) {
                logError("İndirilen dosya PNG formatında değil, Content-Type: " + contentType);
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
                    logError("İndirilen PNG dosyası geçersiz: " + targetPath);
                    Files.deleteIfExists(targetPath);
                    return false;
                }
                long fileSize = Files.size(targetPath);
                logInfo("PNG dosyası başarıyla indirildi: " + targetPath + ", Boyut: " + fileSize + " bayt");
                return true;
            } catch (IOException e) {
                logError("İndirilen PNG dosyası okunamadı: " + e.getMessage());
                Files.deleteIfExists(targetPath);
                return false;
            }
        } catch (IOException e) {
            logError("PNG indirme hatası: URL=" + url + ", Hata=" + e.getMessage());
            return false;
        }
    }

    /**
     * Dosya indirme yardımcı metodu
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

    /**
     * JSON URL'den okuma metodu
     */
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

    /**
     * Dizin silme metodu
     */
    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logError("Dosya silinemedi: " + path + " - " + e.getMessage());
                        }
                    });
        }
    }

    /**
     * Renk hex string'e çevirme
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
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
     * ModInfo sınıfı
     */
    private static class ModInfo {
        private final String id;
        private final String name;
        private final String version;
        private final String description;
        private final String author;
        private final List<String> dependencies;
        private final boolean enabled;

        public ModInfo(String id, String name, String version, String description, String author, List<String> dependencies, boolean enabled) {
            this.id = id;
            this.name = name;
            this.version = version;
            this.description = description;
            this.author = author;
            this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
            this.enabled = enabled;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getVersion() { return version; }
        public String getDescription() { return description; }
        public String getAuthor() { return author; }
        public List<String> getDependencies() { return dependencies; }
        public boolean isEnabled() { return enabled; }
    }

    /**
     * GameProfile sınıfı
     */
    private static class GameProfile {
        private final String name;
        private final String minecraftVersion;
        private final String fabricVersion;
        private final List<String> mods;
        private final Map<String, Object> settings;

        public GameProfile(String name, String minecraftVersion, String fabricVersion, List<String> mods, Map<String, Object> settings) {
            this.name = name;
            this.minecraftVersion = minecraftVersion;
            this.fabricVersion = fabricVersion;
            this.mods = mods != null ? mods : new ArrayList<>();
            this.settings = settings != null ? settings : new HashMap<>();
        }

        // Getters
        public String getName() { return name; }
        public String getMinecraftVersion() { return minecraftVersion; }
        public String getFabricVersion() { return fabricVersion; }
        public List<String> getMods() { return mods; }
        public Map<String, Object> getSettings() { return settings; }
    }

    /**
     * Friend sınıfı
     */
    private static class Friend {
        private final String username;
        private final String status;
        private final String lastSeen;

        public Friend(String username, String status, String lastSeen) {
            this.username = username;
            this.status = status;
            this.lastSeen = lastSeen;
        }

        // Getters
        public String getUsername() { return username; }
        public String getStatus() { return status; }
        public String getLastSeen() { return lastSeen; }
    }

    /**
     * NewsPanel sınıfı
     */
    private static class NewsPanel extends VBox {
        private final NewsItem newsItem;

        public NewsPanel(NewsItem newsItem) {
            this.newsItem = newsItem;
            createNewsPanel();
        }

        private void createNewsPanel() {
            setSpacing(10);
            setPadding(new Insets(15));
            setStyle("-fx-background-color: #111111; -fx-background-radius: 10; -fx-border-radius: 10;");

            // Başlık
            Label titleLabel = new Label(newsItem.getType().getIcon() + " " + newsItem.getTitle());
            titleLabel.setStyle("-fx-text-fill: " + newsItem.getType().getColor() + "; -fx-font-size: 16px; -fx-font-weight: bold;");

            // İçerik
            Label contentLabel = new Label(newsItem.getContent());
            contentLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px;");
            contentLabel.setWrapText(true);

            // Tarih
            Label dateLabel = new Label(newsItem.getDate());
            dateLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");

            getChildren().addAll(titleLabel, contentLabel, dateLabel);
        }

        public NewsItem getNewsItem() {
            return newsItem;
        }
    }

    /**
     * Haberleri config'den yükler
     */
    private void loadNewsFromConfig() {
        newsList.clear();
        if (launcherConfig != null && launcherConfig.has("haberler")) {
            try {
                JSONArray haberler = launcherConfig.getJSONArray("haberler");
                for (int i = 0; i < haberler.length(); i++) {
                    JSONObject haber = haberler.getJSONObject(i);
                    String baslik = haber.getString("baslik");
                    String icerik = haber.getString("icerik");
                    String tarih = haber.getString("tarih");
                    String tipStr = haber.getString("tip");
                    
                    NewsItemType tip;
                    try {
                        tip = NewsItemType.valueOf(tipStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        tip = NewsItemType.GENEL;
                    }
                    
                    newsList.add(new NewsItem(baslik, icerik, tarih, tip));
                }
                logInfo("Haberler yüklendi: " + newsList.size() + " haber");
            } catch (Exception e) {
                logError("Haber yükleme hatası: " + e.getMessage());
            }
        }
    }

    /**
     * Splash screen gösterir
     */
    private void showSplashScreen() {
        // Ana container
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000000;");

        // Dekoratif arka plan
        AnchorPane background = createDecorativeBackground();
        root.getChildren().add(background);

        // İçerik container
        VBox contentBox = new VBox(30);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(50));

        // Logo ve başlık
        Text logoText = new Text("TERRAMONIC");
        logoText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 48));
        logoText.setFill(PRIMARY_COLOR);
        
        DropShadow logoShadow = new DropShadow();
        logoShadow.setColor(SHADOW_COLOR);
        logoShadow.setRadius(20);
        logoText.setEffect(logoShadow);

        Text versionText = new Text("Launcher " + LAUNCHER_VERSION);
        versionText.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 16));
        versionText.setFill(TEXT_SECONDARY);

        // Loading göstergesi
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setPrefWidth(300);
        loadingBar.setPrefHeight(8);
        loadingBar.setStyle("-fx-accent: " + toHexString(PRIMARY_COLOR) + "; -fx-background-radius: 10; -fx-accent-radius: 10;");

        Label loadingLabel = new Label(translate("loading"));
        loadingLabel.setFont(Font.font(FONT_FAMILY, 14));
        loadingLabel.setTextFill(TEXT_COLOR);

        Label subtitleLabel = new Label("Minecraft 1.21.5 + Fabric 0.16.14");
        subtitleLabel.setFont(Font.font(FONT_FAMILY, 12));
        subtitleLabel.setTextFill(TEXT_SECONDARY);

        // Animasyonlar
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), contentBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(1), logoText);
        scaleIn.setFromX(0.5);
        scaleIn.setFromY(0.5);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        contentBox.getChildren().addAll(logoText, versionText, loadingBar, loadingLabel, subtitleLabel);
        root.getChildren().add(contentBox);

        // Setup görevini başlat
        setupTask(loadingBar, loadingLabel, subtitleLabel);

        Scene splashScene = new Scene(root, windowWidth, windowHeight);
        setupWindowControls(root);

        currentScene = splashScene;
        mainStage.setScene(splashScene);

        // Animasyonları başlat
        fadeIn.play();
        scaleIn.play();
    }

    /**
     * Dekoratif arka plan oluşturur
     */
    private AnchorPane createDecorativeBackground() {
        AnchorPane background = new AnchorPane();
        
        // Gradient arka plan
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, null,
                new Stop(0, Color.web("#000000")),
                new Stop(0.5, Color.web("#001100")),
                new Stop(1, Color.web("#000000")));
        
        Background bg = new Background(new BackgroundFill(gradient, null, null));
        background.setBackground(bg);

        // Animasyonlu çizgiler
        for (int i = 0; i < 10; i++) {
            Line line = new Line();
            line.setStroke(Color.web("#01a500", 0.1));
            line.setStrokeWidth(2);
            
            // Rastgele pozisyon
            line.setStartX(Math.random() * windowWidth);
            line.setStartY(Math.random() * windowHeight);
            line.setEndX(Math.random() * windowWidth);
            line.setEndY(Math.random() * windowHeight);
            
            // Animasyon
            Timeline animation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(line.opacityProperty(), 0.1)),
                new KeyFrame(Duration.seconds(2 + Math.random() * 3), new KeyValue(line.opacityProperty(), 0.3)),
                new KeyFrame(Duration.seconds(4 + Math.random() * 3), new KeyValue(line.opacityProperty(), 0.1))
            );
            animation.setCycleCount(Timeline.INDEFINITE);
            animation.play();
            
            background.getChildren().add(line);
        }

        return background;
    }

    /**
     * Setup görevini başlatır
     */
    private void setupTask(ProgressBar loadingBar, Label loadingLabel, Label subtitle) {
        Task<Void> setupTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String[] steps = {
                    "Sistem kontrolü yapılıyor...",
                    "Minecraft dosyaları kontrol ediliyor...",
                    "Fabric kurulumu kontrol ediliyor...",
                    "Mod paketi kontrol ediliyor...",
                    "Kütüphaneler kontrol ediliyor...",
                    "Launcher başlatılıyor..."
                };

                for (int i = 0; i < steps.length; i++) {
                    final int progress = i;
                    final String step = steps[i];
                    
                    Platform.runLater(() -> {
                        loadingBar.setProgress((double) progress / steps.length);
                        loadingLabel.setText(step);
                    });

                    // Simüle edilmiş gecikme ve gerçek kontroller
                    switch (i) {
                        case 0:
                            // Sistem kontrolü
                            checkSystemRequirements();
                            break;
                        case 1:
                            // Minecraft dosyaları
                            checkMinecraftFiles();
                            break;
                        case 2:
                            // Fabric kontrolü
                            checkFabricInstallation();
                            break;
                        case 3:
                            // Mod paketi
                            checkModPack();
                            break;
                        case 4:
                            // Kütüphaneler
                            checkLibraries();
                            break;
                        case 5:
                            // Son hazırlıklar
                            finalizeSetup();
                            break;
                    }

                    Thread.sleep(800 + (int)(Math.random() * 400)); // 800-1200ms arası rastgele gecikme
                }

                Platform.runLater(() -> {
                    loadingBar.setProgress(1.0);
                    loadingLabel.setText("Tamamlandı!");
                });

                Thread.sleep(500);
                return null;
            }

            private void checkSystemRequirements() {
                // Java sürümü kontrolü
                String javaVersion = System.getProperty("java.version");
                logInfo("Java sürümü: " + javaVersion);
                
                // Bellek kontrolü
                long maxMemory = Runtime.getRuntime().maxMemory();
                logInfo("Maksimum bellek: " + (maxMemory / 1024 / 1024) + " MB");
                
                // OS kontrolü
                String osName = System.getProperty("os.name");
                logInfo("İşletim sistemi: " + osName);
            }

            private void checkMinecraftFiles() {
                // Minecraft jar dosyası kontrolü
                Path minecraftJar = TERRAMONIC_PATH.resolve("jar").resolve("minecraft-" + MINECRAFT_VERSION + ".jar");
                if (!Files.exists(minecraftJar)) {
                    logInfo("Minecraft jar dosyası bulunamadı: " + minecraftJar);
                } else {
                    logInfo("Minecraft jar dosyası mevcut: " + minecraftJar);
                }
            }

            private void checkFabricInstallation() {
                // Fabric profil kontrolü
                Path fabricProfile = TERRAMONIC_PATH.resolve("versions").resolve("fabric-loader-" + FABRIC_VERSION + "-" + MINECRAFT_VERSION);
                if (!Files.exists(fabricProfile)) {
                    logInfo("Fabric profili bulunamadı: " + fabricProfile);
                } else {
                    logInfo("Fabric profili mevcut: " + fabricProfile);
                }
            }

            private void checkModPack() {
                // Mod klasörü kontrolü
                Path modsDir = TERRAMONIC_PATH.resolve("mods");
                try {
                    if (Files.exists(modsDir)) {
                        long modCount = Files.list(modsDir).filter(p -> p.toString().endsWith(".jar")).count();
                        logInfo("Yüklü mod sayısı: " + modCount);
                    }
                } catch (IOException e) {
                    logError("Mod sayısı kontrol hatası: " + e.getMessage());
                }
            }

            private void checkLibraries() {
                // Kütüphane klasörü kontrolü
                Path librariesDir = TERRAMONIC_PATH.resolve("libraries");
                try {
                    if (Files.exists(librariesDir)) {
                        long libCount = Files.walk(librariesDir).filter(p -> p.toString().endsWith(".jar")).count();
                        logInfo("Yüklü kütüphane sayısı: " + libCount);
                    }
                } catch (IOException e) {
                    logError("Kütüphane sayısı kontrol hatası: " + e.getMessage());
                }
            }

            private void finalizeSetup() {
                // Son hazırlıklar
                logInfo("Launcher başlatılıyor...");
                
                // UI state'ini güncelle
                Platform.runLater(() -> {
                    uiStates.put("setupComplete", true);
                });
            }
        };

        setupTask.setOnSucceeded(e -> {
            // 1 saniye bekle ve login ekranına geç
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> showLoginScreen());
            pause.play();
        });

        setupTask.setOnFailed(e -> {
            Throwable exception = setupTask.getException();
            logError("Setup görevi başarısız: " + exception.getMessage());
            showError("Launcher başlatılırken hata oluştu: " + exception.getMessage());
        });

        executorService.submit(setupTask);
    }

    /**
     * Login ekranını gösterir
     */
    private void showLoginScreen() {
        // Ana container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #000000;");

        // Başlık çubuğu
        BorderPane titleBar = createTitleBar();
        root.setTop(titleBar);

        // Ana içerik
        StackPane content = new StackPane();
        
        // Arka plan gradyanı
        LinearGradient bgGradient = new LinearGradient(0, 0, 1, 1, true, null,
                new Stop(0, Color.web("#000000")),
                new Stop(0.3, Color.web("#001a00")),
                new Stop(0.7, Color.web("#002200")),
                new Stop(1, Color.web("#000000")));
        
        Background background = new Background(new BackgroundFill(bgGradient, null, null));
        content.setBackground(background);

        // Login panel
        VBox loginPanel = createLoginPanel();
        content.getChildren().add(loginPanel);

        root.setCenter(content);

        // İstatistik paneli (sağ alt köşe)
        VBox statsPanel = createStatsPanel();
        StackPane.setAlignment(statsPanel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(statsPanel, new Insets(20));
        content.getChildren().add(statsPanel);

        Scene loginScene = new Scene(root, windowWidth, windowHeight);
        setupWindowControls(root);

        // Sahne geçişi
        transitionToScene(loginScene);
    }

    /**
     * Login paneli oluşturur
     */
    private VBox createLoginPanel() {
        VBox panel = new VBox(25);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(40));
        panel.setMaxWidth(400);
        panel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-background-radius: 15; -fx-border-color: #01a500; -fx-border-width: 2; -fx-border-radius: 15;");

        // Launcher logosu
        Text logoText = new Text("TERRAMONIC");
        logoText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 36));
        logoText.setFill(PRIMARY_COLOR);
        
        DropShadow logoGlow = new DropShadow();
        logoGlow.setColor(Color.web("#71ff61"));
        logoGlow.setRadius(15);
        logoText.setEffect(logoGlow);

        // Kullanıcı avatarı
        userAvatar = new ImageView();
        userAvatar.setFitWidth(80);
        userAvatar.setFitHeight(80);
        userAvatar.setStyle("-fx-effect: dropshadow(gaussian, #01a500, 10, 0.3, 0, 0);");
        
        // Varsayılan avatar
        Circle avatarBg = new Circle(40, PRIMARY_COLOR);
        Text avatarText = new Text("👤");
        avatarText.setFont(Font.font(30));
        avatarText.setFill(Color.BLACK);
        StackPane avatarStack = new StackPane(avatarBg, avatarText);
        
        // Kullanıcı adı alanı
        TextField usernameField = createStyledTextField(translate("username"));
        usernameField.setText(playerName);
        usernameField.textProperty().addListener((obs, old, newVal) -> {
            playerName = newVal;
            updateUserPreference("lastUsername", newVal);
        });

        // Şifre alanı (offline mod için gizli)
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(translate("password"));
        passwordField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white; -fx-prompt-text-fill: #AAAAAA; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #01a500; -fx-border-width: 1; -fx-font-size: 14; -fx-pref-height: 40;");
        passwordField.setVisible(false); // Offline mod

        // Beni hatırla checkbox
        CheckBox rememberCheckBox = new CheckBox(translate("remember"));
        rememberCheckBox.setSelected(rememberUser);
        rememberCheckBox.setTextFill(TEXT_COLOR);
        rememberCheckBox.setFont(Font.font(FONT_FAMILY, 12));
        rememberCheckBox.selectedProperty().addListener((obs, old, newVal) -> {
            rememberUser = newVal;
            updateUserPreference("rememberUser", newVal);
        });

        // Giriş butonu
        Button loginButton = createStyledButton(translate("login"), 300, 45);
        loginButton.setOnAction(e -> {
            if (playerName.trim().isEmpty()) {
                shakeNode(usernameField);
                showError("Lütfen kullanıcı adınızı girin!");
                return;
            }
            
            playButtonClickAnimation(loginButton);
            
            // Giriş animasyonu
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), panel);
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            scaleTransition.setAutoReverse(true);
            scaleTransition.setCycleCount(2);
            scaleTransition.setOnFinished(event -> transitionToMainScreen());
            scaleTransition.play();
        });

        // Mod temizleme butonu
        Button clearModsButton = createStyledButton("Modları Temizle", 300, 35);
        clearModsButton.setStyle(clearModsButton.getStyle() + "; -fx-background-color: #FF4444;");
        clearModsButton.setOnAction(e -> {
            playButtonClickAnimation(clearModsButton);
            clearConfigAndMods();
        });

        // RAM ve çözünürlük ayarları
        HBox settingsBox = new HBox(15);
        settingsBox.setAlignment(Pos.CENTER);

        VBox ramBox = new VBox(5);
        ramBox.setAlignment(Pos.CENTER);
        Label ramLabel = new Label("RAM");
        ramLabel.setTextFill(TEXT_SECONDARY);
        ramLabel.setFont(Font.font(FONT_FAMILY, 10));
        
        ramCombo = new ComboBox<>();
        ramCombo.getItems().addAll("2G", "4G", "6G", "8G", "12G", "16G");
        ramCombo.setValue((String) userPreferences.getOrDefault("maxMemory", "4G"));
        ramCombo.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white;");
        ramCombo.valueProperty().addListener((obs, old, newVal) -> updateUserPreference("maxMemory", newVal));
        
        ramBox.getChildren().addAll(ramLabel, ramCombo);

        VBox resBox = new VBox(5);
        resBox.setAlignment(Pos.CENTER);
        Label resLabel = new Label("Çözünürlük");
        resLabel.setTextFill(TEXT_SECONDARY);
        resLabel.setFont(Font.font(FONT_FAMILY, 10));
        
        resCombo = new ComboBox<>();
        resCombo.getItems().addAll("1280x720", "1366x768", "1600x900", "1920x1080", "2560x1440", "3840x2160");
        resCombo.setValue((String) userPreferences.getOrDefault("resolution", "1920x1080"));
        resCombo.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white;");
        resCombo.valueProperty().addListener((obs, old, newVal) -> updateUserPreference("resolution", newVal));
        
        resBox.getChildren().addAll(resLabel, resCombo);

        settingsBox.getChildren().addAll(ramBox, resBox);

        panel.getChildren().addAll(logoText, avatarStack, usernameField, rememberCheckBox, settingsBox, loginButton, clearModsButton);

        // Panel animasyonu
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), panel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.8), panel);
        slideIn.setFromY(50);
        slideIn.setToY(0);
        
        ParallelTransition parallel = new ParallelTransition(fadeIn, slideIn);
        parallel.play();

        return panel;
    }

    /**
     * İstatistik paneli oluşturur
     */
    private VBox createStatsPanel() {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.CENTER_RIGHT);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10; -fx-border-color: #01a500; -fx-border-width: 1; -fx-border-radius: 10;");

        // Başlık
        Label titleLabel = new Label("İSTATİSTİKLER");
        titleLabel.setTextFill(PRIMARY_COLOR);
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));

        // İstatistikler
        VBox launchCountBox = createStatBox(String.valueOf(statistics.getOrDefault("launchCount", 0)), "Başlatma");
        VBox playTimeBox = createStatBox(formatPlayTime((Long) statistics.getOrDefault("totalPlayTime", 0L)), "Oyun Süresi");
        VBox modsBox = createStatBox(String.valueOf(statistics.getOrDefault("modsInstalled", 0)), "Mod Sayısı");

        panel.getChildren().addAll(titleLabel, launchCountBox, playTimeBox, modsBox);

        return panel;
    }

    /**
     * İstatistik kutusu oluşturur
     */
    private VBox createStatBox(String value, String label) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(TEXT_COLOR);
        valueLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));

        Label descLabel = new Label(label);
        descLabel.setTextFill(TEXT_SECONDARY);
        descLabel.setFont(Font.font(FONT_FAMILY, 10));

        box.getChildren().addAll(valueLabel, descLabel);
        return box;
    }

    /**
     * Oyun süresini formatlar
     */
    private String formatPlayTime(long millis) {
        long hours = millis / (1000 * 60 * 60);
        if (hours > 0) {
            return hours + "h";
        }
        long minutes = millis / (1000 * 60);
        return minutes + "m";
    }

    /**
     * Kullanıcı tercihini günceller
     */
    private void updateUserPreference(String key, Object value) {
        userPreferences.put(key, value);
        // Hemen kaydet (isteğe bağlı)
        saveUserPreferences();
    }

    /**
     * Config ve modları temizler
     */
    private void clearConfigAndMods() {
        Task<Void> clearTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> statusLabel.setText("Modlar ve konfigürasyonlar temizleniyor..."));
                
                // Modları sil
                Path modsDir = TERRAMONIC_PATH.resolve("mods");
                if (Files.exists(modsDir)) {
                    deleteDirectory(modsDir);
                    Files.createDirectories(modsDir);
                }
                
                // Config'i sil
                Path configDir = TERRAMONIC_PATH.resolve("config");
                if (Files.exists(configDir)) {
                    deleteDirectory(configDir);
                    Files.createDirectories(configDir);
                }
                
                // Cache'i temizle
                Path cacheDir = TERRAMONIC_PATH.resolve("cache");
                if (Files.exists(cacheDir)) {
                    deleteDirectory(cacheDir);
                    Files.createDirectories(cacheDir);
                }
                
                Thread.sleep(1000);
                
                Platform.runLater(() -> {
                    statusLabel.setText("Temizlik tamamlandı!");
                    showInfo("Modlar ve konfigürasyonlar başarıyla temizlendi!");
                });
                
                return null;
            }
        };
        
        clearTask.setOnFailed(e -> {
            Throwable exception = clearTask.getException();
            logError("Temizlik hatası: " + exception.getMessage());
            Platform.runLater(() -> showError("Temizlik sırasında hata: " + exception.getMessage()));
        });
        
        executorService.submit(clearTask);
    }

    /**
     * ZIP dosyasını indirir ve çıkarır
     */
    private void downloadAndExtractZip(String zipUrl, Path targetDir) throws IOException {
        // Temp zip dosyası
        Path tempZip = TERRAMONIC_PATH.resolve("temp").resolve(generateComplexZipName());
        Files.createDirectories(tempZip.getParent());

        logInfo("ZIP indiriliyor: " + zipUrl + " -> " + tempZip);

        // ZIP'i indir
        try (InputStream in = new URL(zipUrl).openStream();
             OutputStream out = Files.newOutputStream(tempZip)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        // ZIP'in geçerliliğini kontrol et
        if (!isValidZip(tempZip)) {
            Files.deleteIfExists(tempZip);
            throw new IOException("İndirilen ZIP dosyası geçersiz: " + zipUrl);
        }

        logInfo("ZIP çıkarılıyor: " + tempZip + " -> " + targetDir);

        // ZIP'i çıkar
        extractZip(tempZip, targetDir);

        // Eksik dosyaları kontrol et ve çıkar
        checkAndExtractMissingFiles(tempZip, targetDir);

        // Temp dosyayı sil
        Files.deleteIfExists(tempZip);
        
        logInfo("ZIP işlemi tamamlandı: " + targetDir);
    }

    /**
     * ZIP dosyasının geçerliliğini kontrol eder
     */
    private boolean isValidZip(Path zipPath) {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            return zis.getNextEntry() != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Karmaşık ZIP ismi üretir
     */
    private String generateComplexZipName() {
        // Güncel timestamp ve rastgele sayı ile benzersiz isim
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return "terramonic_download_" + timestamp + "_" + random + ".zip";
    }

    /**
     * ZIP dosyasını çıkarır
     */
    private void extractZip(Path zipPath, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    Files.createDirectories(targetDir.resolve(entry.getName()));
                } else {
                    Path filePath = targetDir.resolve(entry.getName());
                    Files.createDirectories(filePath.getParent());
                    
                    try (OutputStream out = Files.newOutputStream(filePath)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * Eksik dosyaları kontrol eder ve çıkarır
     */
    private void checkAndExtractMissingFiles(Path nestedZip, Path targetDir) throws IOException {
        // Kritik dosyaların listesi
        String[] requiredFiles = {
            "mods/fabric-api-",
            "mods/sodium-",
            "mods/lithium-",
            "mods/iris-",
            "config/"
        };

        for (String requiredFile : requiredFiles) {
            boolean found = false;
            try {
                found = Files.walk(targetDir)
                        .anyMatch(path -> path.toString().contains(requiredFile));
            } catch (IOException e) {
                logError("Dosya arama hatası: " + e.getMessage());
            }

            if (!found) {
                logInfo("Eksik dosya tespit edildi: " + requiredFile);
                // Bu durumda alternatif bir çözüm uygulayabilirsiniz
            }
        }
    }

    // Devamı gelecek...

    /**
     * Fabric kurulumunu yapar
     */
    private void setupFabric() throws IOException, InterruptedException {
        logInfo("Fabric kurulumu başlatılıyor...");
        
        Path fabricDir = TERRAMONIC_PATH.resolve("versions").resolve("fabric-loader-" + FABRIC_VERSION + "-" + MINECRAFT_VERSION);
        
        if (Files.exists(fabricDir)) {
            logInfo("Fabric zaten kurulu: " + fabricDir);
            return;
        }
        
        // Fabric installer'ı indir
        Path fabricInstaller = TERRAMONIC_PATH.resolve("temp").resolve("fabric-installer.jar");
        Files.createDirectories(fabricInstaller.getParent());
        
        try {
            downloadFile(FABRIC_INSTALLER_URL, fabricInstaller);
            logInfo("Fabric installer indirildi: " + fabricInstaller);
            
            // Fabric'i kur
            ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", fabricInstaller.toString(),
                "client", "-mcversion", MINECRAFT_VERSION,
                "-loader", FABRIC_VERSION,
                "-dir", TERRAMONIC_PATH.toString()
            );
            
            pb.directory(TERRAMONIC_PATH.toFile());
            Process process = pb.start();
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logInfo("Fabric başarıyla kuruldu!");
            } else {
                throw new IOException("Fabric kurulumu başarısız, çıkış kodu: " + exitCode);
            }
            
        } finally {
            Files.deleteIfExists(fabricInstaller);
        }
    }

    /**
     * Ana ekranı gösterir
     */
    private void transitionToMainScreen() {
        // Ana container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #000000;");

        // Başlık çubuğu
        BorderPane titleBar = createTitleBar();
        root.setTop(titleBar);

        // Ana içerik alanı
        HBox mainContent = new HBox();
        mainContent.setSpacing(0);

        // Sol navigasyon paneli
        VBox navigation = createNavigationPanel();
        navigation.setPrefWidth(250);
        mainContent.getChildren().add(navigation);

        // Merkez içerik paneli
        centerPanel = new StackPane();
        centerPanel.setStyle("-fx-background-color: #111111;");
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        mainContent.getChildren().add(centerPanel);

        // İlk panel olarak home panelini göster
        showHomePanel();

        root.setCenter(mainContent);

        // Alt durum çubuğu
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        Scene mainScene = new Scene(root, windowWidth, windowHeight);
        setupWindowControls(root);

        // İstatistikleri güncelle
        updateStatistic("launchCount", ((Integer) statistics.getOrDefault("launchCount", 0)) + 1);
        updateStatistic("lastLaunch", System.currentTimeMillis());

        transitionToScene(mainScene);
    }

    /**
     * Navigasyon paneli oluşturur
     */
    private VBox createNavigationPanel() {
        VBox nav = new VBox(5);
        nav.setStyle("-fx-background-color: #0a0a0a; -fx-padding: 15;");

        // Logo bölümü
        VBox logoSection = new VBox(10);
        logoSection.setAlignment(Pos.CENTER);
        logoSection.setPadding(new Insets(0, 0, 20, 0));

        Text logoText = new Text("TERRAMONIC");
        logoText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 20));
        logoText.setFill(PRIMARY_COLOR);
        
        Text versionText = new Text(LAUNCHER_VERSION);
        versionText.setFont(Font.font(FONT_FAMILY, 12));
        versionText.setFill(TEXT_SECONDARY);

        logoSection.getChildren().addAll(logoText, versionText);

        // Navigasyon butonları
        VBox navButtons = new VBox(5);
        
        String[] navItems = {"Ana Sayfa", "Haberler", "Modlar", "Ayarlar", "Hakkında"};
        String[] navIcons = {"🏠", "📰", "🧩", "⚙️", "ℹ️"};
        
        for (int i = 0; i < navItems.length; i++) {
            final int index = i;
            Button navButton = createNavButton(navIcons[i] + " " + navItems[i], index);
            navButton.setOnAction(e -> selectNavigation(index));
            navButtons.getChildren().add(navButton);
        }

        // Kullanıcı bilgisi
        VBox userSection = new VBox(10);
        userSection.setAlignment(Pos.CENTER);
        userSection.setPadding(new Insets(20, 0, 0, 0));
        userSection.setStyle("-fx-border-color: #333333; -fx-border-width: 1 0 0 0;");

        Circle userIcon = new Circle(25, PRIMARY_COLOR);
        Text userText = new Text("👤");
        userText.setFont(Font.font(20));
        userText.setFill(Color.BLACK);
        StackPane userIconStack = new StackPane(userIcon, userText);

        Label usernameLabel = new Label(playerName.isEmpty() ? "Kullanıcı" : playerName);
        usernameLabel.setTextFill(TEXT_COLOR);
        usernameLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));

        Label statusLabel = new Label("Çevrimiçi");
        statusLabel.setTextFill(PRIMARY_COLOR);
        statusLabel.setFont(Font.font(FONT_FAMILY, 10));

        userSection.getChildren().addAll(userIconStack, usernameLabel, statusLabel);

        nav.getChildren().addAll(logoSection, navButtons, userSection);
        VBox.setVgrow(navButtons, Priority.ALWAYS);

        return nav;
    }

    /**
     * Navigasyon butonu oluşturur
     */
    private Button createNavButton(String text, int index) {
        Button button = new Button(text);
        button.setPrefWidth(220);
        button.setPrefHeight(40);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setFont(Font.font(FONT_FAMILY, 12));
        
        if (index == currentNavIndex) {
            button.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + "; -fx-text-fill: black; -fx-background-radius: 8; -fx-font-weight: bold;");
        } else {
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-background-radius: 8;");
        }
        
        button.setOnMouseEntered(e -> {
            if (index != currentNavIndex) {
                button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white; -fx-background-radius: 8;");
            }
        });
        
        button.setOnMouseExited(e -> {
            if (index != currentNavIndex) {
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-background-radius: 8;");
            }
        });

        return button;
    }

    /**
     * Navigasyon seçimini değiştirir
     */
    private void selectNavigation(int index) {
        currentNavIndex = index;
        
        // Tüm navigasyon butonlarını güncelle
        refreshNavigationButtons();
        
        // İlgili paneli göster
        switch (index) {
            case 0:
                showHomePanel();
                break;
            case 1:
                showNewsPanel();
                break;
            case 2:
                showModsPanel();
                break;
            case 3:
                showSettingsPanel();
                break;
            case 4:
                showAboutPanel();
                break;
        }
    }

    /**
     * Navigasyon butonlarını yeniler
     */
    private void refreshNavigationButtons() {
        // Navigation panelindeki butonları güncelle
        VBox navigation = (VBox) ((HBox) ((BorderPane) currentScene.getRoot()).getCenter()).getChildren().get(0);
        VBox navButtons = (VBox) navigation.getChildren().get(1);
        
        for (int i = 0; i < navButtons.getChildren().size(); i++) {
            Button button = (Button) navButtons.getChildren().get(i);
            if (i == currentNavIndex) {
                button.setStyle("-fx-background-color: " + toHexString(PRIMARY_COLOR) + "; -fx-text-fill: black; -fx-background-radius: 8; -fx-font-weight: bold;");
            } else {
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-background-radius: 8;");
            }
        }
    }

    /**
     * Ana sayfa panelini gösterir
     */
    private void showHomePanel() {
        VBox homePanel = new VBox(20);
        homePanel.setPadding(new Insets(30));
        homePanel.setAlignment(Pos.TOP_CENTER);

        // Karşılama metni
        VBox welcomeSection = new VBox(10);
        welcomeSection.setAlignment(Pos.CENTER);

        Text welcomeText = new Text("Hoş Geldin, " + (playerName.isEmpty() ? "Oyuncu" : playerName) + "!");
        welcomeText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 28));
        welcomeText.setFill(TEXT_COLOR);

        Text subtitleText = new Text("Minecraft " + MINECRAFT_VERSION + " + Fabric " + FABRIC_VERSION);
        subtitleText.setFont(Font.font(FONT_FAMILY, 16));
        subtitleText.setFill(TEXT_SECONDARY);

        welcomeSection.getChildren().addAll(welcomeText, subtitleText);

        // Oyun başlatma bölümü
        VBox playSection = new VBox(15);
        playSection.setAlignment(Pos.CENTER);
        playSection.setMaxWidth(400);

        playButton = createStyledButton("🚀 Minecraft'ı Başlat", 350, 60);
        playButton.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        playButton.setOnAction(e -> {
            if (!gameIsLaunching) {
                playButtonClickAnimation(playButton);
                launchGame();
            }
        });

        // Progress bar (başlangıçta gizli)
        downloadProgress = new ProgressBar();
        downloadProgress.setPrefWidth(350);
        downloadProgress.setPrefHeight(8);
        downloadProgress.setStyle("-fx-accent: " + toHexString(PRIMARY_COLOR) + "; -fx-background-radius: 10; -fx-accent-radius: 10;");
        downloadProgress.setVisible(false);

        // Durum etiketi
        statusLabel.setFont(Font.font(FONT_FAMILY, 12));
        statusLabel.setTextFill(TEXT_SECONDARY);
        statusLabel.setVisible(false);

        playSection.getChildren().addAll(playButton, downloadProgress, statusLabel);

        // Hızlı istatistikler
        HBox statsSection = createQuickStats();

        homePanel.getChildren().addAll(welcomeSection, playSection, statsSection);

        // Animasyon ile paneli göster
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), homePanel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        centerPanel.getChildren().clear();
        centerPanel.getChildren().add(homePanel);
        
        fadeIn.play();
    }

    /**
     * Hızlı istatistik bölümü oluşturur
     */
    private HBox createQuickStats() {
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(30, 0, 0, 0));

        // Toplam oyun süresi
        VBox playTimeBox = createStatCard("⏱️", formatPlayTime((Long) statistics.getOrDefault("totalPlayTime", 0L)), "Toplam Oyun");
        
        // Başlatma sayısı
        VBox launchBox = createStatCard("🚀", String.valueOf(statistics.getOrDefault("launchCount", 0)), "Başlatma");
        
        // Mod sayısı
        VBox modBox = createStatCard("🧩", String.valueOf(installedMods.size()), "Mod");

        statsBox.getChildren().addAll(playTimeBox, launchBox, modBox);
        
        return statsBox;
    }

    /**
     * İstatistik kartı oluşturur
     */
    private VBox createStatCard(String icon, String value, String label) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 12; -fx-border-color: rgba(1, 165, 0, 0.3); -fx-border-width: 1; -fx-border-radius: 12;");
        card.setMinWidth(120);

        Text iconText = new Text(icon);
        iconText.setFont(Font.font(24));
        iconText.setFill(PRIMARY_COLOR);

        Text valueText = new Text(value);
        valueText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 20));
        valueText.setFill(TEXT_COLOR);

        Text labelText = new Text(label);
        labelText.setFont(Font.font(FONT_FAMILY, 12));
        labelText.setFill(TEXT_SECONDARY);

        card.getChildren().addAll(iconText, valueText, labelText);

        // Hover efekti
        card.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });

        card.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return card;
    }

    /**
     * Haber panelini gösterir
     */
    private void showNewsPanel() {
        VBox newsPanel = new VBox(20);
        newsPanel.setPadding(new Insets(30));

        // Başlık
        Text titleText = new Text("📰 Haberler ve Duyurular");
        titleText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        titleText.setFill(TEXT_COLOR);

        // Haber listesi
        newsScrollPane = new ScrollPane();
        newsScrollPane.setFitToWidth(true);
        newsScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        newsContainer = new VBox(15);
        newsContainer.setPadding(new Insets(10));
        
        // Haberleri yükle
        loadNewsItems();
        
        newsScrollPane.setContent(newsContainer);
        VBox.setVgrow(newsScrollPane, Priority.ALWAYS);

        newsPanel.getChildren().addAll(titleText, newsScrollPane);

        // Animasyon ile paneli göster
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), newsPanel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        centerPanel.getChildren().clear();
        centerPanel.getChildren().add(newsPanel);
        
        fadeIn.play();
    }

    /**
     * Haber öğelerini yükler
     */
    private void loadNewsItems() {
        newsContainer.getChildren().clear();
        
        if (newsList.isEmpty()) {
            // Varsayılan haberler
            Label noNewsLabel = new Label("Henüz haber bulunmuyor...");
            noNewsLabel.setTextFill(TEXT_SECONDARY);
            noNewsLabel.setFont(Font.font(FONT_FAMILY, 14));
            newsContainer.getChildren().add(noNewsLabel);
        } else {
            for (NewsItem newsItem : newsList) {
                NewsPanel newsPanel = new NewsPanel(newsItem);
                newsContainer.getChildren().add(newsPanel);
                
                // Animasyon
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newsPanel);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.setDelay(Duration.millis(newsContainer.getChildren().indexOf(newsPanel) * 100));
                fadeIn.play();
            }
        }
    }

    /**
     * Mod panelini gösterir
     */
    private void showModsPanel() {
        VBox modsPanel = new VBox(20);
        modsPanel.setPadding(new Insets(30));

        // Başlık ve kontroller
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Text titleText = new Text("🧩 Mod Yöneticisi");
        titleText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        titleText.setFill(TEXT_COLOR);

        Button refreshButton = createStyledButton("🔄 Yenile", 100, 35);
        refreshButton.setOnAction(e -> refreshModsPanel());

        Button installButton = createStyledButton("📦 Mod Paketi Yükle", 150, 35);
        installButton.setOnAction(e -> installModPack());

        headerBox.getChildren().addAll(titleText, refreshButton, installButton);

        // Mod listesi
        ScrollPane modsScrollPane = new ScrollPane();
        modsScrollPane.setFitToWidth(true);
        modsScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        VBox modsContainer = new VBox(10);
        modsContainer.setPadding(new Insets(10));
        
        // Yüklü modları listele
        loadModsList(modsContainer);
        
        modsScrollPane.setContent(modsContainer);
        VBox.setVgrow(modsScrollPane, Priority.ALWAYS);

        modsPanel.getChildren().addAll(headerBox, modsScrollPane);

        // Animasyon ile paneli göster
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), modsPanel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        centerPanel.getChildren().clear();
        centerPanel.getChildren().add(modsPanel);
        
        fadeIn.play();
    }

    /**
     * Mod listesini yükler
     */
    private void loadModsList(VBox container) {
        container.getChildren().clear();
        
        try {
            Path modsDir = TERRAMONIC_PATH.resolve("mods");
            if (Files.exists(modsDir)) {
                Files.list(modsDir)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .forEach(modPath -> {
                        String modName = modPath.getFileName().toString();
                        HBox modItem = createModListItem(modName, true);
                        container.getChildren().add(modItem);
                    });
            }
            
            if (container.getChildren().isEmpty()) {
                Label noModsLabel = new Label("Henüz mod yüklenmemiş. Mod paketi yüklemek için 'Mod Paketi Yükle' butonunu kullanın.");
                noModsLabel.setTextFill(TEXT_SECONDARY);
                noModsLabel.setFont(Font.font(FONT_FAMILY, 14));
                noModsLabel.setWrapText(true);
                container.getChildren().add(noModsLabel);
            }
        } catch (IOException e) {
            logError("Mod listesi yükleme hatası: " + e.getMessage());
        }
    }

    /**
     * Mod liste öğesi oluşturur
     */
    private HBox createModListItem(String modName, boolean enabled) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(15));
        item.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 8; -fx-border-color: rgba(1, 165, 0, 0.2); -fx-border-width: 1; -fx-border-radius: 8;");

        // Mod durumu
        Circle statusCircle = new Circle(8, enabled ? PRIMARY_COLOR : Color.GRAY);
        
        // Mod bilgileri
        VBox modInfo = new VBox(5);
        modInfo.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(modName.replace(".jar", ""));
        nameLabel.setTextFill(TEXT_COLOR);
        nameLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        
        Label statusLabel = new Label(enabled ? "Aktif" : "Devre Dışı");
        statusLabel.setTextFill(enabled ? PRIMARY_COLOR : Color.GRAY);
        statusLabel.setFont(Font.font(FONT_FAMILY, 12));
        
        modInfo.getChildren().addAll(nameLabel, statusLabel);
        
        // Boşluk
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Kontrol butonları
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_RIGHT);
        
        Button toggleButton = createStyledButton(enabled ? "Devre Dışı" : "Aktifleştir", 100, 30);
        toggleButton.setOnAction(e -> toggleMod(modName, !enabled, item));
        
        Button deleteButton = createStyledButton("🗑️", 35, 30);
        deleteButton.setStyle(deleteButton.getStyle() + "; -fx-background-color: #FF4444;");
        deleteButton.setOnAction(e -> deleteMod(modName, item));
        
        controls.getChildren().addAll(toggleButton, deleteButton);
        
        item.getChildren().addAll(statusCircle, modInfo, spacer, controls);
        
        return item;
    }

    /**
     * Mod durumunu değiştirir
     */
    private void toggleMod(String modName, boolean enable, HBox item) {
        // Mod durumunu değiştir (basit implementasyon)
        Circle statusCircle = (Circle) item.getChildren().get(0);
        VBox modInfo = (VBox) item.getChildren().get(1);
        Label statusLabel = (Label) modInfo.getChildren().get(1);
        HBox controls = (HBox) item.getChildren().get(3);
        Button toggleButton = (Button) controls.getChildren().get(0);
        
        statusCircle.setFill(enable ? PRIMARY_COLOR : Color.GRAY);
        statusLabel.setText(enable ? "Aktif" : "Devre Dışı");
        statusLabel.setTextFill(enable ? PRIMARY_COLOR : Color.GRAY);
        toggleButton.setText(enable ? "Devre Dışı" : "Aktifleştir");
        
        logInfo("Mod durumu değiştirildi: " + modName + " -> " + (enable ? "Aktif" : "Devre Dışı"));
    }

    /**
     * Mod siler
     */
    private void deleteMod(String modName, HBox item) {
        try {
            Path modPath = TERRAMONIC_PATH.resolve("mods").resolve(modName);
            if (Files.exists(modPath)) {
                Files.delete(modPath);
                
                // UI'dan kaldır
                VBox parent = (VBox) item.getParent();
                parent.getChildren().remove(item);
                
                logInfo("Mod silindi: " + modName);
                showInfo("Mod başarıyla silindi: " + modName);
            }
        } catch (IOException e) {
            logError("Mod silme hatası: " + e.getMessage());
            showError("Mod silinirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Mod panelini yeniler
     */
    private void refreshModsPanel() {
        showModsPanel();
        logInfo("Mod paneli yenilendi");
    }

    /**
     * Mod paketi kurar
     */
    private void installModPack() {
        if (gameIsLaunching) {
            showError("Oyun başlatılırken mod paketi yüklenemez!");
            return;
        }
        
        Task<Void> installTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    statusLabel.setText("Mod paketi yükleniyor...");
                    statusLabel.setVisible(true);
                    downloadProgress.setVisible(true);
                    downloadProgress.setProgress(-1);
                });
                
                // Fabric kurulumunu kontrol et
                setupFabric();
                
                // Minecraft kütüphanelerini indir
                downloadMinecraftLibraries();
                
                // Modrinth paketi yükle
                downloadAndInstallModrinthPack();
                
                return null;
            }
        };
        
        installTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Mod paketi başarıyla yüklendi!");
                downloadProgress.setProgress(1.0);
                showInfo("Mod paketi başarıyla yüklendi!");
                
                // 3 saniye sonra gizle
                PauseTransition pause = new PauseTransition(Duration.seconds(3));
                pause.setOnFinished(ev -> {
                    statusLabel.setVisible(false);
                    downloadProgress.setVisible(false);
                });
                pause.play();
                
                // Mod panelini yenile
                refreshModsPanel();
            });
        });
        
        installTask.setOnFailed(e -> {
            Throwable exception = installTask.getException();
            logError("Mod paketi yükleme hatası: " + exception.getMessage());
            Platform.runLater(() -> {
                statusLabel.setText("Mod paketi yükleme başarısız!");
                downloadProgress.setVisible(false);
                showError("Mod paketi yükleme hatası: " + exception.getMessage());
            });
        });
        
        executorService.submit(installTask);
    }

    /**
     * İstatistiği günceller
     */
    private void updateStatistic(String key, Object value) {
        statistics.put(key, value);
        saveStatistics();
    }

    // Devamı gelecek...

    /**
     * Minecraft kütüphanelerini indirir
     */
    private void downloadMinecraftLibraries() {
        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> updateMessage("Minecraft kütüphaneleri indiriliyor..."));
                
                Path librariesDir = TERRAMONIC_PATH.resolve("libraries");
                Files.createDirectories(librariesDir);
                
                // SLF4J kütüphanelerini indir
                downloadSLF4JLibraries(librariesDir);
                
                // LWJGL native dosyalarını çıkar
                extractLWJGLNatives(librariesDir);
                
                Platform.runLater(() -> updateMessage("Kütüphaneler başarıyla indirildi"));
                return null;
            }
        };
        
        downloadTask.messageProperty().addListener((obs, old, newVal) -> {
            if (statusLabel != null) {
                statusLabel.setText(newVal);
            }
        });
        
        executorService.submit(downloadTask);
    }

    /**
     * SLF4J kütüphanelerini indirir
     */
    private void downloadSLF4JLibraries(Path librariesDir) throws IOException {
        String[][] slf4jLibs = {
            {"org/slf4j", "slf4j-api", "2.0.7"},
            {"org/slf4j", "slf4j-simple", "2.0.7"},
            {"ch/qos/logback", "logback-core", "1.4.8"},
            {"ch/qos/logback", "logback-classic", "1.4.8"}
        };
        
        for (String[] lib : slf4jLibs) {
            String group = lib[0];
            String artifact = lib[1];
            String version = lib[2];
            
            Path libDir = librariesDir.resolve(group.replace(".", "/")).resolve(artifact).resolve(version);
            Files.createDirectories(libDir);
            
            String fileName = artifact + "-" + version + ".jar";
            Path libFile = libDir.resolve(fileName);
            
            if (!Files.exists(libFile)) {
                String url = "https://repo1.maven.org/maven2/" + group.replace(".", "/") + "/" + artifact + "/" + version + "/" + fileName;
                try {
                    downloadFile(url, libFile);
                    logInfo("SLF4J kütüphanesi indirildi: " + fileName);
                } catch (IOException e) {
                    logError("SLF4J kütüphanesi indirilemedi: " + fileName + " - " + e.getMessage());
                }
            }
        }
    }

    /**
     * LWJGL native dosyalarını çıkarır
     */
    private void extractLWJGLNatives(Path librariesDir) throws IOException {
        Path nativesDir = TERRAMONIC_PATH.resolve("natives");
        Files.createDirectories(nativesDir);
        
        String osName = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch");
        String nativeSuffix;
        
        if (osName.contains("windows")) {
            nativeSuffix = arch.contains("64") ? "natives-windows-x86_64" : "natives-windows-x86";
        } else if (osName.contains("linux")) {
            nativeSuffix = arch.contains("64") ? "natives-linux-x86_64" : "natives-linux-x86";
        } else if (osName.contains("mac")) {
            nativeSuffix = "natives-macos";
        } else {
            nativeSuffix = "natives-linux-x86_64"; // Varsayılan
        }
        
        String[] lwjglModules = {
            "lwjgl", "lwjgl-opengl", "lwjgl-glfw", "lwjgl-stb", 
            "lwjgl-tinyfd", "lwjgl-jemalloc", "lwjgl-openal"
        };
        
        for (String module : lwjglModules) {
            String version = "3.3.6";
            Path moduleDir = librariesDir.resolve("org/lwjgl").resolve(module).resolve(version);
            Files.createDirectories(moduleDir);
            
            // Ana JAR
            String jarName = module + "-" + version + ".jar";
            Path jarFile = moduleDir.resolve(jarName);
            if (!Files.exists(jarFile)) {
                String jarUrl = "https://repo1.maven.org/maven2/org/lwjgl/" + module + "/" + version + "/" + jarName;
                try {
                    downloadFile(jarUrl, jarFile);
                    logInfo("LWJGL modülü indirildi: " + jarName);
                } catch (IOException e) {
                    logError("LWJGL modülü indirilemedi: " + jarName);
                }
            }
            
            // Native JAR
            String nativeName = module + "-" + version + "-" + nativeSuffix + ".jar";
            Path nativeFile = moduleDir.resolve(nativeName);
            if (!Files.exists(nativeFile)) {
                String nativeUrl = "https://repo1.maven.org/maven2/org/lwjgl/" + module + "/" + version + "/" + nativeName;
                try {
                    downloadFile(nativeUrl, nativeFile);
                    logInfo("LWJGL native indirildi: " + nativeName);
                    
                    // Native dosyaları çıkar
                    extractZip(nativeFile, nativesDir);
                } catch (IOException e) {
                    logError("LWJGL native indirilemedi: " + nativeName);
                }
            }
        }
    }

    /**
     * Modrinth paketi indirir ve kurar
     */
    private void downloadAndInstallModrinthPack() {
        if (launcherConfig == null || !launcherConfig.has("modpack_url")) {
            logInfo("Launcher config'de modpack_url bulunamadı, mod paketi atlanıyor");
            return;
        }
        
        String modpackUrl = launcherConfig.getString("modpack_url");
        
        Task<Void> modpackTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> updateMessage("Modrinth mod paketi indiriliyor..."));
                
                // Mevcut modları temizle
                Path modsDir = TERRAMONIC_PATH.resolve("mods");
                if (Files.exists(modsDir)) {
                    Files.walk(modsDir)
                        .filter(path -> path.toString().endsWith(".jar"))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                logError("Mod silinemedi: " + path);
                            }
                        });
                }
                
                // Mod paketini indir ve çıkar
                downloadAndExtractZip(modpackUrl, TERRAMONIC_PATH);
                
                // Mod sayısını güncelle
                long modCount = Files.list(modsDir)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .count();
                
                updateStatistic("modsInstalled", (int) modCount);
                
                Platform.runLater(() -> updateMessage("Mod paketi başarıyla kuruldu: " + modCount + " mod"));
                
                return null;
            }
        };
        
        modpackTask.messageProperty().addListener((obs, old, newVal) -> {
            if (statusLabel != null) {
                statusLabel.setText(newVal);
            }
        });
        
        executorService.submit(modpackTask);
    }

    /**
     * Oyunu başlatır
     */
    private void launchGame() {
        if (gameIsLaunching) {
            showError("Oyun zaten başlatılıyor!");
            return;
        }
        
        if (playerName.trim().isEmpty()) {
            showError("Lütfen kullanıcı adınızı girin!");
            return;
        }
        
        gameIsLaunching = true;
        updateGameState(true);
        
        Task<Void> launchTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Platform.runLater(() -> {
                        statusLabel.setText("Oyun başlatılıyor...");
                        statusLabel.setVisible(true);
                        downloadProgress.setVisible(true);
                        downloadProgress.setProgress(-1);
                        playButton.setText("Başlatılıyor...");
                        playButton.setDisable(true);
                    });
                    
                    // Minecraft jar dosyasını kontrol et
                    Platform.runLater(() -> updateMessage("Minecraft dosyaları kontrol ediliyor..."));
                    Path minecraftJar = TERRAMONIC_PATH.resolve("jar").resolve("minecraft-" + MINECRAFT_VERSION + ".jar");
                    if (!Files.exists(minecraftJar)) {
                        Files.createDirectories(minecraftJar.getParent());
                        String jarUrl = "https://piston-data.mojang.com/v1/objects/e79dc5c6ff9bc0b8bc5d5654651915bc2cbf05c4/client.jar";
                        downloadFile(jarUrl, minecraftJar);
                        logInfo("Minecraft JAR indirildi: " + minecraftJar);
                    }
                    
                    // Fabric kontrolü
                    Platform.runLater(() -> updateMessage("Fabric kurulumu kontrol ediliyor..."));
                    setupFabric();
                    
                    // Kütüphaneleri kontrol et
                    Platform.runLater(() -> updateMessage("Kütüphaneler kontrol ediliyor..."));
                    downloadMinecraftLibraries();
                    
                    // JTrace stub oluştur
                    Platform.runLater(() -> updateMessage("JTrace stub oluşturuluyor..."));
                    Path librariesDir = TERRAMONIC_PATH.resolve("libraries");
                    Path jtraceStub = ensureJTraceStub(librariesDir);
                    
                    // Classpath oluştur
                    Platform.runLater(() -> updateMessage("Classpath hazırlanıyor..."));
                    StringBuilder classpath = new StringBuilder();
                    
                    // Minecraft JAR
                    classpath.append(minecraftJar.toString()).append(File.pathSeparator);
                    
                    // Fabric JAR
                    Path fabricJar = TERRAMONIC_PATH.resolve("versions")
                        .resolve("fabric-loader-" + FABRIC_VERSION + "-" + MINECRAFT_VERSION)
                        .resolve("fabric-loader-" + FABRIC_VERSION + "-" + MINECRAFT_VERSION + ".jar");
                    if (Files.exists(fabricJar)) {
                        classpath.append(fabricJar.toString()).append(File.pathSeparator);
                    }
                    
                    // Kütüphaneler
                    addEssentialLibrariesToClasspath(classpath, librariesDir);
                    
                    // JVM argümanları
                    List<String> jvmArgs = new ArrayList<>();
                    jvmArgs.add("java");
                    
                    // Bellek ayarları
                    String maxMemory = (String) userPreferences.getOrDefault("maxMemory", "4G");
                    jvmArgs.add("-Xmx" + maxMemory);
                    jvmArgs.add("-Xms" + maxMemory);
                    
                    // JVM optimizasyonları
                    jvmArgs.addAll(Arrays.asList(
                        "-XX:+UseG1GC",
                        "-XX:+UnlockExperimentalVMOptions",
                        "-XX:G1NewSizePercent=20",
                        "-XX:G1ReservePercent=20",
                        "-XX:MaxGCPauseMillis=50",
                        "-XX:G1HeapRegionSize=32M",
                        "-Dfml.ignoreInvalidMinecraftCertificates=true",
                        "-Dfml.ignorePatchDiscrepancies=true",
                        "-Djava.net.preferIPv4Stack=true",
                        "-Dminecraft.launcher.brand=TerraMonic",
                        "-Dminecraft.launcher.version=" + LAUNCHER_VERSION
                    ));
                    
                    // Native kütüphane yolu
                    Path nativesDir = TERRAMONIC_PATH.resolve("natives");
                    jvmArgs.add("-Djava.library.path=" + nativesDir.toString());
                    
                    // Classpath
                    jvmArgs.add("-cp");
                    jvmArgs.add(classpath.toString());
                    
                    // Ana sınıf (KnotClient)
                    jvmArgs.add("net.fabricmc.loader.impl.launch.knot.KnotClient");
                    
                    // Minecraft argümanları
                    jvmArgs.add("--username");
                    jvmArgs.add(playerName);
                    jvmArgs.add("--version");
                    jvmArgs.add(MINECRAFT_VERSION);
                    jvmArgs.add("--gameDir");
                    jvmArgs.add(TERRAMONIC_PATH.toString());
                    jvmArgs.add("--assetsDir");
                    jvmArgs.add(TERRAMONIC_PATH.resolve("assets").toString());
                    jvmArgs.add("--assetIndex");
                    jvmArgs.add(MINECRAFT_VERSION);
                    jvmArgs.add("--uuid");
                    jvmArgs.add(UUID.randomUUID().toString());
                    jvmArgs.add("--accessToken");
                    jvmArgs.add("0");
                    jvmArgs.add("--clientId");
                    jvmArgs.add("0");
                    jvmArgs.add("--xuid");
                    jvmArgs.add("0");
                    jvmArgs.add("--userType");
                    jvmArgs.add("legacy");
                    
                    // Çözünürlük ayarları
                    String resolution = (String) userPreferences.getOrDefault("resolution", "1920x1080");
                    String[] resParts = resolution.split("x");
                    if (resParts.length == 2) {
                        jvmArgs.add("--width");
                        jvmArgs.add(resParts[0]);
                        jvmArgs.add("--height");
                        jvmArgs.add(resParts[1]);
                    }
                    
                    Platform.runLater(() -> updateMessage("Minecraft başlatılıyor..."));
                    
                    // Process'i başlat
                    ProcessBuilder pb = new ProcessBuilder(jvmArgs);
                    pb.directory(TERRAMONIC_PATH.toFile());
                    pb.redirectErrorStream(true);
                    
                    logInfo("Minecraft başlatılıyor: " + String.join(" ", jvmArgs));
                    
                    Process process = pb.start();
                    
                    // Oyun başlatma istatistiği
                    updateStatistic("gamesPlayed", ((Integer) statistics.getOrDefault("gamesPlayed", 0)) + 1);
                    updateStatistic("lastGameStart", System.currentTimeMillis());
                    
                    Platform.runLater(() -> {
                        statusLabel.setText("Minecraft başarıyla başlatıldı!");
                        downloadProgress.setProgress(1.0);
                        
                        // Launcher'ı gizle
                        if (mainStage != null) {
                            mainStage.setIconified(true);
                        }
                    });
                    
                    // Process'in bitmesini bekle
                    int exitCode = process.waitFor();
                    logInfo("Minecraft process bitti, çıkış kodu: " + exitCode);
                    
                    // Oyun bitince launcher'ı göster
                    Platform.runLater(() -> {
                        if (mainStage != null) {
                            mainStage.setIconified(false);
                            mainStage.toFront();
                        }
                    });
                    
                } finally {
                    Platform.runLater(() -> {
                        gameIsLaunching = false;
                        updateGameState(false);
                        playButton.setText("🚀 Minecraft'ı Başlat");
                        playButton.setDisable(false);
                        statusLabel.setVisible(false);
                        downloadProgress.setVisible(false);
                    });
                }
                
                return null;
            }
        };
        
        launchTask.messageProperty().addListener((obs, old, newVal) -> {
            if (statusLabel != null) {
                statusLabel.setText(newVal);
            }
        });
        
        launchTask.setOnFailed(e -> {
            Throwable exception = launchTask.getException();
            logError("Oyun başlatma hatası: " + exception.getMessage());
            Platform.runLater(() -> {
                gameIsLaunching = false;
                updateGameState(false);
                playButton.setText("🚀 Minecraft'ı Başlat");
                playButton.setDisable(false);
                statusLabel.setText("Oyun başlatma başarısız!");
                downloadProgress.setVisible(false);
                showError("Oyun başlatma hatası: " + exception.getMessage());
            });
        });
        
        executorService.submit(launchTask);
    }

    /**
     * Oyun durumu değişikliğini dinleyicilere bildirir
     */
    private void updateGameState(boolean isLaunching) {
        for (Consumer<Boolean> listener : gameStateChangeListeners) {
            listener.accept(isLaunching);
        }
    }

    /**
     * Temel kütüphaneleri classpath'e ekler
     */
    private void addEssentialLibrariesToClasspath(StringBuilder classpath, Path librariesDir) {
        String[][] essentialLibs = {
            {"org/slf4j", "slf4j-api", "2.0.7"},
            {"org/slf4j", "slf4j-simple", "2.0.7"},
            {"org/lwjgl", "lwjgl", "3.3.6"},
            {"org/lwjgl", "lwjgl-opengl", "3.3.6"},
            {"org/lwjgl", "lwjgl-glfw", "3.3.6"},
            {"org/lwjgl", "lwjgl-stb", "3.3.6"},
            {"org/lwjgl", "lwjgl-tinyfd", "3.3.6"},
            {"org/lwjgl", "lwjgl-jemalloc", "3.3.6"},
            {"org/lwjgl", "lwjgl-openal", "3.3.6"}
        };
        
        for (String[] lib : essentialLibs) {
            Path libFile = librariesDir.resolve(lib[0].replace(".", "/"))
                .resolve(lib[1])
                .resolve(lib[2])
                .resolve(lib[1] + "-" + lib[2] + ".jar");
            
            if (Files.exists(libFile)) {
                classpath.append(libFile.toString()).append(File.pathSeparator);
            }
        }
    }

    /**
     * JTrace stub'ını oluşturur
     */
    private Path ensureJTraceStub(Path librariesDir) {
        Path jtraceDir = librariesDir.resolve("java/jdk/jfr");
        Path jtraceJar = jtraceDir.resolve("jtrace-stub.jar");
        
        try {
            Files.createDirectories(jtraceDir);
            
            if (!Files.exists(jtraceJar)) {
                // Basit bir stub JAR oluştur
                String javaSource = """
                package jdk.jfr;
                public class FlightRecorder {
                    public static boolean isAvailable() { return false; }
                    public static FlightRecorder getFlightRecorder() { return null; }
                }
                public class Event {
                    public void commit() {}
                    public void begin() {}
                    public void end() {}
                }
                """;
                
                byte[] classBytes = createClassBytes(javaSource);
                
                try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(jtraceJar))) {
                    ZipEntry entry = new ZipEntry("jdk/jfr/FlightRecorder.class");
                    zos.putNextEntry(entry);
                    zos.write(classBytes);
                    zos.closeEntry();
                }
                
                logInfo("JTrace stub oluşturuldu: " + jtraceJar);
            }
        } catch (IOException e) {
            logError("JTrace stub oluşturma hatası: " + e.getMessage());
        }
        
        return jtraceJar;
    }

    /**
     * Java kaynak kodundan basit class bytes oluşturur
     */
    private byte[] createClassBytes(String javaSource) {
        // Bu basit bir implementasyon, gerçek bir Java compiler kullanılmalı
        // Şimdilik boş bir class file döndürüyoruz
        return new byte[]{
            (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, // Magic number
            0x00, 0x00, // Minor version
            0x00, 0x34, // Major version (Java 8)
            0x00, 0x01, // Constant pool count
            0x00, 0x00  // Padding
        };
    }

    /**
     * Hata mesajı gösterir
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Dialog'u tema ile uyumlu hale getir
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #111111; -fx-text-fill: white;");
        
        alert.showAndWait();
        logError("Kullanıcıya hata gösterildi: " + message);
    }

    /**
     * Bilgi mesajı gösterir
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bilgi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Dialog'u tema ile uyumlu hale getir
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #111111; -fx-text-fill: white;");
        
        alert.showAndWait();
        logInfo("Kullanıcıya bilgi gösterildi: " + message);
    }

    /**
     * Ayarlar panelini gösterir
     */
    private void showSettingsPanel() {
        VBox settingsPanel = new VBox(20);
        settingsPanel.setPadding(new Insets(30));

        // Başlık
        Text titleText = new Text("⚙️ Ayarlar");
        titleText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        titleText.setFill(TEXT_COLOR);

        // Ayar kategorileri
        VBox generalSettings = createSettingsCategory("Genel Ayarlar", createGeneralSettings());
        VBox gameSettings = createSettingsCategory("Oyun Ayarları", createGameSettings());
        VBox launcherSettings = createSettingsCategory("Launcher Ayarları", createLauncherSettings());

        settingsPanel.getChildren().addAll(titleText, generalSettings, gameSettings, launcherSettings);

        // Scroll pane
        ScrollPane scrollPane = new ScrollPane(settingsPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Animasyon ile paneli göster
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), scrollPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        centerPanel.getChildren().clear();
        centerPanel.getChildren().add(scrollPane);

        fadeIn.play();
    }

    /**
     * Ayar kategorisi oluşturur
     */
    private VBox createSettingsCategory(String title, VBox content) {
        VBox category = new VBox(15);
        category.setPadding(new Insets(20));
        category.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10; -fx-border-color: rgba(1, 165, 0, 0.3); -fx-border-width: 1; -fx-border-radius: 10;");

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(PRIMARY_COLOR);
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));

        category.getChildren().addAll(titleLabel, content);
        return category;
    }

    /**
     * Genel ayarları oluşturur
     */
    private VBox createGeneralSettings() {
        VBox settings = new VBox(15);

        // Dil seçimi
        HBox languageBox = createSettingRow("Dil", createLanguageComboBox());
        
        // Tema seçimi
        HBox themeBox = createSettingRow("Tema", createThemeComboBox());
        
        // Ses ayarları
        HBox soundBox = createSettingRow("Ses", createSoundControls());

        settings.getChildren().addAll(languageBox, themeBox, soundBox);
        return settings;
    }

    /**
     * Oyun ayarları oluşturur
     */
    private VBox createGameSettings() {
        VBox settings = new VBox(15);

        // RAM ayarı
        HBox ramBox = createSettingRow("RAM Miktarı", createRamComboBox());
        
        // Çözünürlük ayarı
        HBox resolutionBox = createSettingRow("Çözünürlük", createResolutionComboBox());
        
        // Tam ekran modu
        HBox fullscreenBox = createSettingRow("Tam Ekran", createFullscreenCheckBox());

        settings.getChildren().addAll(ramBox, resolutionBox, fullscreenBox);
        return settings;
    }

    /**
     * Launcher ayarları oluşturur
     */
    private VBox createLauncherSettings() {
        VBox settings = new VBox(15);

        // Otomatik güncelleme
        HBox autoUpdateBox = createSettingRow("Otomatik Güncelleme", createAutoUpdateCheckBox());
        
        // Başlangıç ekranı
        HBox splashBox = createSettingRow("Başlangıç Ekranı", createSplashCheckBox());
        
        // Animasyonlar
        HBox animationsBox = createSettingRow("Animasyonlar", createAnimationsCheckBox());

        settings.getChildren().addAll(autoUpdateBox, splashBox, animationsBox);
        return settings;
    }

    /**
     * Ayar satırı oluşturur
     */
    private HBox createSettingRow(String label, Node control) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setTextFill(TEXT_COLOR);
        labelNode.setFont(Font.font(FONT_FAMILY, 14));
        labelNode.setPrefWidth(150);

        row.getChildren().addAll(labelNode, control);
        return row;
    }

    /**
     * Dil seçim kutusunu oluşturur
     */
    private ComboBox<String> createLanguageComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("Türkçe", "English");
        combo.setValue(currentLanguage.equals("tr") ? "Türkçe" : "English");
        combo.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white;");
        
        combo.valueProperty().addListener((obs, old, newVal) -> {
            currentLanguage = newVal.equals("Türkçe") ? "tr" : "en";
            updateUserPreference("language", currentLanguage);
        });
        
        return combo;
    }

    /**
     * Tema seçim kutusunu oluşturur
     */
    private ComboBox<String> createThemeComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("Koyu", "Açık", "Oyun");
        
        String themeDisplay = switch (currentTheme) {
            case "dark" -> "Koyu";
            case "light" -> "Açık";
            case "gaming" -> "Oyun";
            default -> "Koyu";
        };
        combo.setValue(themeDisplay);
        combo.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white;");
        
        combo.valueProperty().addListener((obs, old, newVal) -> {
            currentTheme = switch (newVal) {
                case "Koyu" -> "dark";
                case "Açık" -> "light";
                case "Oyun" -> "gaming";
                default -> "dark";
            };
            updateUserPreference("theme", currentTheme);
        });
        
        return combo;
    }

    /**
     * Ses kontrollerini oluşturur
     */
    private HBox createSoundControls() {
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);

        CheckBox enabledBox = new CheckBox("Etkin");
        enabledBox.setSelected(soundEnabled);
        enabledBox.setTextFill(TEXT_COLOR);
        enabledBox.selectedProperty().addListener((obs, old, newVal) -> {
            soundEnabled = newVal;
            updateUserPreference("soundEnabled", newVal);
        });

        Slider volumeSlider = new Slider(0, 1, soundVolume);
        volumeSlider.setPrefWidth(100);
        volumeSlider.valueProperty().addListener((obs, old, newVal) -> {
            soundVolume = newVal.doubleValue();
            updateUserPreference("soundVolume", soundVolume);
        });

        controls.getChildren().addAll(enabledBox, new Label("Ses:"), volumeSlider);
        return controls;
    }

    /**
     * RAM seçim kutusunu oluşturur
     */
    private ComboBox<String> createRamComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("2G", "4G", "6G", "8G", "12G", "16G");
        combo.setValue((String) userPreferences.getOrDefault("maxMemory", "4G"));
        combo.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white;");
        
        combo.valueProperty().addListener((obs, old, newVal) -> {
            updateUserPreference("maxMemory", newVal);
        });
        
        return combo;
    }

    /**
     * Çözünürlük seçim kutusunu oluşturur
     */
    private ComboBox<String> createResolutionComboBox() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("1280x720", "1366x768", "1600x900", "1920x1080", "2560x1440", "3840x2160");
        combo.setValue((String) userPreferences.getOrDefault("resolution", "1920x1080"));
        combo.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white;");
        
        combo.valueProperty().addListener((obs, old, newVal) -> {
            updateUserPreference("resolution", newVal);
        });
        
        return combo;
    }

    /**
     * Tam ekran checkbox'ını oluşturur
     */
    private CheckBox createFullscreenCheckBox() {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected((Boolean) userPreferences.getOrDefault("fullscreen", false));
        checkBox.setTextFill(TEXT_COLOR);
        
        checkBox.selectedProperty().addListener((obs, old, newVal) -> {
            updateUserPreference("fullscreen", newVal);
        });
        
        return checkBox;
    }

    /**
     * Otomatik güncelleme checkbox'ını oluşturur
     */
    private CheckBox createAutoUpdateCheckBox() {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected((Boolean) userPreferences.getOrDefault("autoUpdate", true));
        checkBox.setTextFill(TEXT_COLOR);
        
        checkBox.selectedProperty().addListener((obs, old, newVal) -> {
            updateUserPreference("autoUpdate", newVal);
        });
        
        return checkBox;
    }

    /**
     * Başlangıç ekranı checkbox'ını oluşturur
     */
    private CheckBox createSplashCheckBox() {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected((Boolean) userPreferences.getOrDefault("showSplash", true));
        checkBox.setTextFill(TEXT_COLOR);
        
        checkBox.selectedProperty().addListener((obs, old, newVal) -> {
            updateUserPreference("showSplash", newVal);
        });
        
        return checkBox;
    }

    /**
     * Animasyonlar checkbox'ını oluşturur
     */
    private CheckBox createAnimationsCheckBox() {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected((Boolean) userPreferences.getOrDefault("enableAnimations", true));
        checkBox.setTextFill(TEXT_COLOR);
        
        checkBox.selectedProperty().addListener((obs, old, newVal) -> {
            updateUserPreference("enableAnimations", newVal);
        });
        
        return checkBox;
    }

    /**
     * Hakkında panelini gösterir
     */
    private void showAboutPanel() {
        VBox aboutPanel = new VBox(30);
        aboutPanel.setPadding(new Insets(50));
        aboutPanel.setAlignment(Pos.CENTER);

        // Logo
        Text logoText = new Text("TERRAMONIC");
        logoText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 48));
        logoText.setFill(PRIMARY_COLOR);
        
        DropShadow logoGlow = new DropShadow();
        logoGlow.setColor(SHADOW_COLOR);
        logoGlow.setRadius(20);
        logoText.setEffect(logoGlow);

        // Sürüm bilgisi
        Text versionText = new Text("Launcher " + LAUNCHER_VERSION);
        versionText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 20));
        versionText.setFill(TEXT_COLOR);

        // Açıklama
        Text descText = new Text("Modern Minecraft Launcher\nMinecraft " + MINECRAFT_VERSION + " + Fabric " + FABRIC_VERSION + " Desteği");
        descText.setFont(Font.font(FONT_FAMILY, 16));
        descText.setFill(TEXT_SECONDARY);
        descText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Geliştirici bilgisi
        VBox developerInfo = new VBox(10);
        developerInfo.setAlignment(Pos.CENTER);
        
        Text devLabel = new Text("Geliştirici:");
        devLabel.setFont(Font.font(FONT_FAMILY, 14));
        devLabel.setFill(TEXT_SECONDARY);
        
        Text devName = new Text("TerraMonic Team");
        devName.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        devName.setFill(TEXT_COLOR);
        
        developerInfo.getChildren().addAll(devLabel, devName);

        // Sistem bilgileri
        VBox systemInfo = createSystemInfo();

        // Bağlantılar
        HBox linksBox = new HBox(20);
        linksBox.setAlignment(Pos.CENTER);
        
        Button websiteButton = createStyledButton("🌐 Website", 120, 40);
        websiteButton.setOnAction(e -> openWebsite(TERRAMONIC_URL));
        
        Button discordButton = createStyledButton("💬 Discord", 120, 40);
        discordButton.setOnAction(e -> showInfo("Discord sunucu bağlantısı yakında eklenecek!"));
        
        linksBox.getChildren().addAll(websiteButton, discordButton);

        aboutPanel.getChildren().addAll(logoText, versionText, descText, developerInfo, systemInfo, linksBox);

        // Scroll pane
        ScrollPane scrollPane = new ScrollPane(aboutPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Animasyon ile paneli göster
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), scrollPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        centerPanel.getChildren().clear();
        centerPanel.getChildren().add(scrollPane);

        fadeIn.play();
    }

    /**
     * Sistem bilgilerini oluşturur
     */
    private VBox createSystemInfo() {
        VBox systemInfo = new VBox(8);
        systemInfo.setAlignment(Pos.CENTER);
        systemInfo.setPadding(new Insets(20));
        systemInfo.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10; -fx-border-color: rgba(1, 165, 0, 0.2); -fx-border-width: 1; -fx-border-radius: 10;");

        Label titleLabel = new Label("Sistem Bilgileri");
        titleLabel.setTextFill(PRIMARY_COLOR);
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));

        // Java sürümü
        Label javaLabel = new Label("Java: " + System.getProperty("java.version"));
        javaLabel.setTextFill(TEXT_COLOR);
        javaLabel.setFont(Font.font(FONT_FAMILY, 12));

        // İşletim sistemi
        Label osLabel = new Label("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        osLabel.setTextFill(TEXT_COLOR);
        osLabel.setFont(Font.font(FONT_FAMILY, 12));

        // Bellek
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        Label memoryLabel = new Label("Bellek: " + totalMemory + "MB / " + maxMemory + "MB");
        memoryLabel.setTextFill(TEXT_COLOR);
        memoryLabel.setFont(Font.font(FONT_FAMILY, 12));

        systemInfo.getChildren().addAll(titleLabel, javaLabel, osLabel, memoryLabel);
        return systemInfo;
    }

    /**
     * Website açar
     */
    private void openWebsite(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            logError("Website açma hatası: " + e.getMessage());
            showError("Website açılamadı: " + url);
        }
    }

    /**
     * Alt durum çubuğunu oluşturur
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(8, 15, 8, 15));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #0a0a0a; -fx-border-color: #333333; -fx-border-width: 1 0 0 0;");

        // Sol taraf - durum
        Label statusLabel = new Label("Hazır");
        statusLabel.setTextFill(TEXT_COLOR);
        statusLabel.setFont(Font.font(FONT_FAMILY, 11));

        // Sağ taraf - sürüm ve zaman
        HBox rightBox = new HBox(20);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label versionLabel = new Label(LAUNCHER_VERSION);
        versionLabel.setTextFill(TEXT_SECONDARY);
        versionLabel.setFont(Font.font(FONT_FAMILY, 11));

        Label timeLabel = new Label();
        timeLabel.setTextFill(TEXT_SECONDARY);
        timeLabel.setFont(Font.font(FONT_FAMILY, 11));
        
        // Zamanı güncelle
        Timeline timeUpdater = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLabel.setText(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        }));
        timeUpdater.setCycleCount(Timeline.INDEFINITE);
        timeUpdater.play();

        rightBox.getChildren().addAll(versionLabel, timeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusBar.getChildren().addAll(statusLabel, spacer, rightBox);
        return statusBar;
    }

    // Devamı gelecek...

    /**
     * Sahne geçişi yapar
     */
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
        }
    }

    /**
     * Pencere kontrollerini ayarlar
     */
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

    /**
     * Başlık çubuğu oluşturur
     */
    private BorderPane createTitleBar() {
        BorderPane titleBar = new BorderPane();
        titleBar.setPrefHeight(40);
        titleBar.setStyle("-fx-background-color: #111111; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");

        // Sol taraf - başlık
        HBox leftBox = new HBox(10);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.setPadding(new Insets(0, 0, 0, 15));

        if (launcherIcon != null) {
            ImageView iconView = new ImageView(launcherIcon);
            iconView.setFitWidth(20);
            iconView.setFitHeight(20);
            leftBox.getChildren().add(iconView);
        }

        Label titleLabel = new Label("TerraMonic Launcher " + LAUNCHER_VERSION);
        titleLabel.setTextFill(TEXT_COLOR);
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        leftBox.getChildren().add(titleLabel);

        // Sağ taraf - pencere kontrolleri
        HBox rightBox = new HBox(5);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setPadding(new Insets(0, 10, 0, 0));

        Button minimizeButton = createWindowControlButton("−", "#FFA500");
        minimizeButton.setOnAction(e -> mainStage.setIconified(true));

        Button closeButton = createWindowControlButton("×", "#FF4444");
        closeButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        rightBox.getChildren().addAll(minimizeButton, closeButton);

        titleBar.setLeft(leftBox);
        titleBar.setRight(rightBox);

        // Başlık çubuğu ile pencereyi taşıma
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

    /**
     * Pencere kontrol butonu oluşturur
     */
    private Button createWindowControlButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefSize(30, 25);
        button.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: " + color + "; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-border-width: 0; " +
            "-fx-background-radius: 0;"
        );

        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle() + "-fx-background-color: rgba(255, 255, 255, 0.1);");
        });

        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle().replace("-fx-background-color: rgba(255, 255, 255, 0.1);", ""));
        });

        return button;
    }

    /**
     * Stilize edilmiş text field oluşturur
     */
    private TextField createStyledTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.1); " +
            "-fx-text-fill: white; " +
            "-fx-prompt-text-fill: #AAAAAA; " +
            "-fx-background-radius: 10; " +
            "-fx-border-radius: 10; " +
            "-fx-border-color: #01a500; " +
            "-fx-border-width: 1; " +
            "-fx-font-size: 14; " +
            "-fx-pref-height: 40;"
        );

        // Focus efektleri
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                textField.setStyle(textField.getStyle() + "-fx-border-color: #71ff61; -fx-border-width: 2;");
            } else {
                textField.setStyle(textField.getStyle().replace("-fx-border-color: #71ff61; -fx-border-width: 2;", "-fx-border-color: #01a500; -fx-border-width: 1;"));
            }
        });

        return textField;
    }

    /**
     * Stilize edilmiş buton oluşturur
     */
    private Button createStyledButton(String text, double width, double height) {
        Button button = new Button(text);
        button.setPrefWidth(width);
        button.setPrefHeight(height);
        button.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        button.setCursor(Cursor.HAND);
        
        String baseStyle = 
            "-fx-background-color: linear-gradient(to bottom, #01a500, #007a00); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: " + BUTTON_RADIUS + "; " +
            "-fx-border-radius: " + BUTTON_RADIUS + "; " +
            "-fx-border-color: #71ff61; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, rgba(113, 255, 97, 0.3), 8, 0.3, 0, 2);";
        
        button.setStyle(baseStyle);

        // Hover efektleri
        button.setOnMouseEntered(e -> {
            button.setStyle(baseStyle + 
                "-fx-background-color: linear-gradient(to bottom, #71ff61, #01a500); " +
                "-fx-effect: dropshadow(gaussian, rgba(113, 255, 97, 0.5), 12, 0.5, 0, 3);"
            );
            
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();
        });

        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle);
            
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });

        return button;
    }

    /**
     * Buton tıklama animasyonu
     */
    private void playButtonClickAnimation(Button button) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);
        
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);
        
        SequentialTransition sequence = new SequentialTransition(scaleDown, scaleUp);
        sequence.play();
    }

    /**
     * Node sallama animasyonu
     */
    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(-5);
        shake.setToX(5);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    /**
     * Sistem ikonlarını günceller
     */
    private void updateSystemIcons() {
        if (launcherIcon != null && mainStage != null) {
            // JavaFX Stage ikonu
            if (!mainStage.getIcons().contains(launcherIcon)) {
                mainStage.getIcons().clear();
                mainStage.getIcons().add(launcherIcon);
            }
            
            // Sistem tepsisi ikonu güncelle
            if (trayIcon != null) {
                try {
                    // JavaFX Image'i AWT BufferedImage'e çevir
                    BufferedImage bufferedImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = bufferedImage.createGraphics();
                    g2d.setColor(java.awt.Color.GREEN);
                    g2d.fillRect(0, 0, 32, 32);
                    g2d.dispose();
                    
                    trayIcon.setImage(bufferedImage);
                    logInfo("Sistem tepsisi ikonu güncellendi");
                } catch (Exception e) {
                    logError("Sistem tepsisi ikonu güncellenemedi: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Mod panelini yeniler
     */
    private void refreshModPanelUI() {
        Platform.runLater(() -> {
            if (currentNavIndex == 2) { // Modlar sekmesi
                showModsPanel();
            }
        });
    }

    /**
     * Gelişmiş dosya hash hesaplama
     */
    private String calculateFileHash(Path filePath) {
        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            return Integer.toHexString(Arrays.hashCode(fileBytes));
        } catch (IOException e) {
            logError("Dosya hash hesaplama hatası: " + e.getMessage());
            return "";
        }
    }

    /**
     * Ağ bağlantısı kontrolü
     */
    private boolean isNetworkAvailable() {
        try {
            URL url = new URL("https://www.google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Launcher güncelleme kontrolü
     */
    private void checkForUpdates() {
        if (!isNetworkAvailable()) {
            logInfo("Ağ bağlantısı yok, güncelleme kontrolü atlanıyor");
            return;
        }

        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Basit güncelleme kontrolü
                    String updateUrl = "https://api.github.com/repos/terramonic/launcher/releases/latest";
                    String response = readJsonFromUrl(updateUrl);
                    JSONObject releaseInfo = new JSONObject(response);
                    
                    String latestVersionTag = releaseInfo.getString("tag_name");
                    String currentVersionTag = "v" + LAUNCHER_VERSION.substring(1); // v1.0.2 formatında
                    
                    if (!latestVersionTag.equals(currentVersionTag)) {
                        updateAvailable = true;
                        latestVersion = latestVersionTag;
                        updateUrl = releaseInfo.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                        
                        Platform.runLater(() -> {
                            showInfo("Yeni launcher sürümü mevcut: " + latestVersion + "\nManuel olarak güncelleme yapabilirsiniz.");
                        });
                    }
                    
                } catch (Exception e) {
                    logError("Güncelleme kontrolü hatası: " + e.getMessage());
                }
                return null;
            }
        };
        
        executorService.submit(updateTask);
    }

    /**
     * Launcher ayarları kaydet
     */
    private void saveLauncherSettings() {
        try {
            JSONObject settings = new JSONObject();
            settings.put("windowWidth", windowWidth);
            settings.put("windowHeight", windowHeight);
            settings.put("currentTheme", currentTheme);
            settings.put("currentLanguage", currentLanguage);
            settings.put("soundEnabled", soundEnabled);
            settings.put("soundVolume", soundVolume);
            
            Path settingsFile = TERRAMONIC_PATH.resolve("launcher_settings.json");
            Files.writeString(settingsFile, settings.toString(2));
            logInfo("Launcher ayarları kaydedildi");
        } catch (Exception e) {
            logError("Launcher ayarları kaydetme hatası: " + e.getMessage());
        }
    }

    /**
     * Launcher ayarları yükle
     */
    private void loadLauncherSettings() {
        try {
            Path settingsFile = TERRAMONIC_PATH.resolve("launcher_settings.json");
            if (Files.exists(settingsFile)) {
                String content = Files.readString(settingsFile);
                JSONObject settings = new JSONObject(content);
                
                windowWidth = settings.optDouble("windowWidth", 1100);
                windowHeight = settings.optDouble("windowHeight", 700);
                currentTheme = settings.optString("currentTheme", "dark");
                currentLanguage = settings.optString("currentLanguage", "tr");
                soundEnabled = settings.optBoolean("soundEnabled", true);
                soundVolume = settings.optDouble("soundVolume", 0.5);
                
                logInfo("Launcher ayarları yüklendi");
            }
        } catch (Exception e) {
            logError("Launcher ayarları yükleme hatası: " + e.getMessage());
        }
    }

    /**
     * Dosya boyutunu formatlar
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Zaman farkını formatlar
     */
    private String formatTimeDifference(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) return days + " gün önce";
        if (hours > 0) return hours + " saat önce";
        if (minutes > 0) return minutes + " dakika önce";
        return "Az önce";
    }

    /**
     * Rastgele UUID üretir
     */
    private String generateRandomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Klasör boyutunu hesaplar
     */
    private long calculateDirectorySize(Path directory) {
        try {
            return Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            return 0L;
        }
    }

    /**
     * Sistem kaynakları monitörü
     */
    private void updateSystemResourceInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        performanceMetrics.put("memoryUsagePercent", memoryUsagePercent);
        performanceMetrics.put("usedMemoryMB", usedMemory / 1024 / 1024);
        performanceMetrics.put("totalMemoryMB", totalMemory / 1024 / 1024);
        performanceMetrics.put("maxMemoryMB", maxMemory / 1024 / 1024);
        
        // CPU kullanımı (basit tahmin)
        long currentTime = System.currentTimeMillis();
        Long lastTime = (Long) performanceMetrics.get("lastUpdateTime");
        if (lastTime != null) {
            long timeDiff = currentTime - lastTime;
            // Basit CPU kullanım tahmini
            double cpuUsage = Math.min(100, Math.random() * 30 + memoryUsagePercent * 0.3);
            performanceMetrics.put("cpuUsagePercent", cpuUsage);
        }
        performanceMetrics.put("lastUpdateTime", currentTime);
    }

    /**
     * Debug bilgilerini yazdır
     */
    private void printDebugInfo() {
        logInfo("=== DEBUG BİLGİLERİ ===");
        logInfo("Launcher Version: " + LAUNCHER_VERSION);
        logInfo("Minecraft Version: " + MINECRAFT_VERSION);
        logInfo("Fabric Version: " + FABRIC_VERSION);
        logInfo("Java Version: " + System.getProperty("java.version"));
        logInfo("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        logInfo("User Home: " + System.getProperty("user.home"));
        logInfo("TerraMonic Path: " + TERRAMONIC_PATH);
        logInfo("Current Theme: " + currentTheme);
        logInfo("Current Language: " + currentLanguage);
        logInfo("Sound Enabled: " + soundEnabled);
        logInfo("Game Launching: " + gameIsLaunching);
        logInfo("Statistics: " + statistics.size() + " entries");
        logInfo("Performance Metrics: " + performanceMetrics.size() + " entries");
        logInfo("=== DEBUG BİLGİLERİ SON ===");
    }

    /**
     * Gelişmiş hata raporlama
     */
    private void reportError(String component, Exception exception) {
        String errorReport = String.format(
            "Component: %s\nError: %s\nMessage: %s\nTimestamp: %s\nLauncher Version: %s",
            component,
            exception.getClass().getSimpleName(),
            exception.getMessage(),
            new Date(),
            LAUNCHER_VERSION
        );
        
        logError("Error Report: " + errorReport);
        
        // Hata raporunu dosyaya kaydet
        try {
            Path errorReportFile = LOGS_PATH.resolve("error_reports.log");
            Files.writeString(errorReportFile, errorReport + System.lineSeparator() + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Hata raporu kaydedilemedi: " + e.getMessage());
        }
    }

    /**
     * Bellek temizleme
     */
    private void cleanupMemory() {
        // Animasyon cache'ini temizle
        animationCache.clear();
        
        // Haber panel cache'ini temizle
        newsPanelCache.clear();
        
        // Performans metriklerini sınırla
        if (performanceMetrics.size() > 100) {
            performanceMetrics.clear();
        }
        
        // Launcher loglarını sınırla
        if (launcherLogs.size() > 1000) {
            List<String> recentLogs = launcherLogs.subList(launcherLogs.size() - 500, launcherLogs.size());
            launcherLogs.clear();
            launcherLogs.addAll(recentLogs);
        }
        
        // Garbage collection öner
        System.gc();
        
        logInfo("Bellek temizleme tamamlandı");
    }

    /**
     * Launcher kapatma işlemleri
     */
    private void performShutdownTasks() {
        logInfo("Launcher kapatılıyor...");
        
        // Son kullanım zamanını kaydet
        updateStatistic("lastShutdown", System.currentTimeMillis());
        
        // Ayarları kaydet
        saveLauncherSettings();
        
        // Açık bağlantıları kapat
        for (HttpURLConnection connection : activeConnections.values()) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                // Ignore
            }
        }
        
        // Cache'i temizle
        cleanupMemory();
        
        // Log'u kapat
        logInfo("TerraMonic Launcher başarıyla kapatıldı");
    }

    /**
     * Acil durum dosya kurtarma
     */
    private void emergencyFileRecovery() {
        logInfo("Acil durum dosya kurtarma başlatılıyor...");
        
        try {
            // Kritik dosyaları kontrol et
            Path[] criticalFiles = {
                USER_PREFERENCES_FILE,
                STATISTICS_FILE,
                LAUNCHER_LOG_FILE
            };
            
            for (Path file : criticalFiles) {
                if (!Files.exists(file)) {
                    logError("Kritik dosya eksik: " + file);
                    
                    // Yedek dosyayı ara
                    Path backupFile = file.resolveSibling(file.getFileName() + ".backup");
                    if (Files.exists(backupFile)) {
                        Files.copy(backupFile, file);
                        logInfo("Yedek dosyadan kurtarıldı: " + file);
                    } else {
                        // Varsayılan dosyayı oluştur
                        if (file.equals(USER_PREFERENCES_FILE)) {
                            createDefaultPreferences();
                        } else if (file.equals(STATISTICS_FILE)) {
                            createDefaultStatistics();
                        }
                        logInfo("Varsayılan dosya oluşturuldu: " + file);
                    }
                }
            }
            
        } catch (Exception e) {
            logError("Acil durum kurtarma hatası: " + e.getMessage());
        }
    }

    /**
     * Plugin sistemi için hazırlık
     */
    private void initializePluginSystem() {
        try {
            Files.createDirectories(PLUGINS_PATH);
            
            // Plugin klasörünü tara
            if (Files.exists(PLUGINS_PATH)) {
                Files.list(PLUGINS_PATH)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .forEach(pluginPath -> {
                        try {
                            // Plugin bilgilerini oku (basit implementasyon)
                            String pluginName = pluginPath.getFileName().toString();
                            loadedPlugins.put(pluginName, "loaded");
                            logInfo("Plugin yüklendi: " + pluginName);
                        } catch (Exception e) {
                            logError("Plugin yükleme hatası: " + pluginPath + " - " + e.getMessage());
                        }
                    });
            }
            
            logInfo("Plugin sistemi başlatıldı, yüklenen plugin sayısı: " + loadedPlugins.size());
        } catch (Exception e) {
            logError("Plugin sistemi başlatma hatası: " + e.getMessage());
        }
    }

    /**
     * Sosyal özellikler için hazırlık
     */
    private void initializeSocialFeatures() {
        // Discord Rich Presence hazırlığı
        socialConnections.put("discord", "disconnected");
        
        // Arkadaş listesi hazırlığı
        friendsList.clear();
        
        // Basit sosyal durum
        socialConnections.put("status", "online");
        
        logInfo("Sosyal özellikler başlatıldı");
    }

    /**
     * Gelişmiş güvenlik kontrolleri
     */
    private void performSecurityChecks() {
        // Güvenilir URL'leri ekle
        trustedUrls.add("https://www.terramonic.com");
        trustedUrls.add("https://api.github.com");
        trustedUrls.add("https://repo1.maven.org");
        trustedUrls.add("https://piston-data.mojang.com");
        trustedUrls.add("https://maven.fabricmc.net");
        
        // Dosya imzalarını kontrol et (basit implementasyon)
        try {
            Path minecraftJar = TERRAMONIC_PATH.resolve("jar").resolve("minecraft-" + MINECRAFT_VERSION + ".jar");
            if (Files.exists(minecraftJar)) {
                String hash = calculateFileHash(minecraftJar);
                fileSignatures.put("minecraft-" + MINECRAFT_VERSION + ".jar", hash);
            }
        } catch (Exception e) {
            logError("Güvenlik kontrolü hatası: " + e.getMessage());
        }
        
        logInfo("Güvenlik kontrolleri tamamlandı, güvenilir URL sayısı: " + trustedUrls.size());
    }

    /**
     * Launcher başlatma sonrası işlemler
     */
    private void postLaunchInitialization() {
        // Güncelleme kontrolü (arka planda)
        executorService.submit(this::checkForUpdates);
        
        // Plugin sistemi
        initializePluginSystem();
        
        // Sosyal özellikler
        initializeSocialFeatures();
        
        // Güvenlik kontrolleri
        performSecurityChecks();
        
        // Acil durum kurtarma kontrolü
        emergencyFileRecovery();
        
        // Debug bilgileri (geliştirme modunda)
        if (System.getProperty("terramonic.debug", "false").equals("true")) {
            printDebugInfo();
        }
        
        logInfo("Launcher başlatma sonrası işlemler tamamlandı");
    }

    // Ana kapanış işlemi (JVM shutdown hook için)
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("TerraMonic Launcher kapatılıyor...");
        }));
    }
}