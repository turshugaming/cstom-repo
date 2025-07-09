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
import java.security.MessageDigest;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * TerraMonic Minecraft Launcher - Python kodundan tam çeviri
 * Fabric 0.16.14 + Minecraft 1.21.5 desteği
 */
public class TerraMonicLauncher extends Application {

    // Ana renkler ve tema
    private static final Color BACKGROUND_COLOR = Color.web("#000000");
    private static final Color BACKGROUND_SECONDARY = Color.web("#111111");
    private static final Color PRIMARY_COLOR = Color.web("#01a500");
    private static final Color PRIMARY_HIGHLIGHT = Color.web("#71ff61");
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color TEXT_SECONDARY = Color.web("#AAAAAA");
    private static final Color SHADOW_COLOR = Color.web("71ff61");

    // Font ayarları
    private static final String GILROY_FONT_URL = "https://www.dropbox.com/scl/fi/to4nfmn8047ec1f4vqujv/Gilroy-Bold.otf?rlkey=egesrxgfpx0ppj8cruis7mksp&st=crk2grid&dl=1";
    private static final String FONT_FAMILY = "Gilroy Bold";
    private static final double BUTTON_RADIUS = 18.0;

    // Launcher sabitleri - Python kodundan
    private static final String LAUNCHER_VERSION = "v2.1.0";
    private static final String MINECRAFT_VERSION = "1.21.5";
    private static final String FABRIC_VERSION = "0.16.14";
    private static final String TERRAMONIC_URL = "https://www.terramonic.com";
    private static final String ICON_URL = "https://www.dropbox.com/scl/fi/2yc75kqokrtivw202rt3w/icon.png?rlkey=1blmy791i17gs6t78ecjc3qxf&st=cjc590fc&dl=1";
    private static final String LAUNCHER_JSON_URL = "https://www.dropbox.com/scl/fi/zcty3rszkpxritcd6ip85/launcher.json?rlkey=byf0phf8xjy0j6nuuto1qohql&st=rcgtv35f&dl=1";

    // Python kodundan - API URLs
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String FABRIC_PROFILE_URL = "https://meta.fabricmc.net/v2/versions/loader/" + MINECRAFT_VERSION + "/" + FABRIC_VERSION + "/profile/json";

    // Path sistem - Python kodundan çeviri
    private static final Path TERRAMONIC_PATH = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", ".terramonic");
    private static final Path VERSIONS_DIR = TERRAMONIC_PATH.resolve("versions");
    private static final Path LIBRARIES_DIR = TERRAMONIC_PATH.resolve("libraries");
    private static final Path ASSETS_DIR = TERRAMONIC_PATH.resolve("assets");
    private static final Path NATIVES_DIR = TERRAMONIC_PATH.resolve("natives");
    private static final Path MODS_PROFILES_PATH = TERRAMONIC_PATH.resolve("mods_profiles");
    private static final Path ICON_PATH = TERRAMONIC_PATH.resolve("terramonic_icon");
    private static final Path ICON_FILE = ICON_PATH.resolve("terramonic_icon.png");
    private static final Path LAUNCHER_VERSION_JSON = TERRAMONIC_PATH.resolve("launcher_version.json");
    private static final Path DELETED_MODS_FILE = TERRAMONIC_PATH.resolve("deleted_mods.json");

    private String currentLauncherVersion = "v2.1.0";
    private String fabricVersionId = "fabric-loader-" + FABRIC_VERSION + "-" + MINECRAFT_VERSION;

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
    private ExecutorService executorService;
    private ComboBox<String> ramCombo;
    private ComboBox<String> resCombo;

    // Durum kontrolleri
    private final javafx.beans.property.BooleanProperty modsReady = new javafx.beans.property.SimpleBooleanProperty(false);
    private final javafx.beans.property.BooleanProperty librariesReady = new javafx.beans.property.SimpleBooleanProperty(false);
    private final javafx.beans.property.BooleanProperty assetsReady = new javafx.beans.property.SimpleBooleanProperty(false);
    private boolean gameIsLaunching = false;

    // Stage ve scene referansları
    private Stage mainStage;
    private Scene currentScene;
    private Image launcherIcon;
    private JSONObject launcherConfig;
    private StackPane centerPanel;
    private int currentNavIndex = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        mainStage.initStyle(StageStyle.UNDECORATED);
        mainStage.setTitle("TerraMonic Launcher " + LAUNCHER_VERSION);

        executorService = Executors.newFixedThreadPool(8);

        // Python kodundan - klasörleri oluştur
        createDirectories();
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
     * Python create_directories metodunun çevirisi
     */
    private void createDirectories() {
        try {
            Path[] dirs = {
                TERRAMONIC_PATH, VERSIONS_DIR, LIBRARIES_DIR, ASSETS_DIR, NATIVES_DIR,
                TERRAMONIC_PATH.resolve("mods"), TERRAMONIC_PATH.resolve("config"),
                ASSETS_DIR.resolve("objects"), ASSETS_DIR.resolve("indexes"),
                VERSIONS_DIR.resolve(MINECRAFT_VERSION), VERSIONS_DIR.resolve(fabricVersionId),
                MODS_PROFILES_PATH, ICON_PATH
            };

            for (Path dir : dirs) {
                Files.createDirectories(dir);
            }
            System.out.println("✓ Klasörler oluşturuldu: " + TERRAMONIC_PATH);
        } catch (IOException e) {
            System.out.println("❌ Klasör oluşturma hatası: " + e.getMessage());
        }
    }

    /**
     * Python kodundan çeviri - Minecraft versiyonunu indir
     */
    private boolean downloadMinecraftVersion() {
        try {
            System.out.println("🎮 Minecraft " + MINECRAFT_VERSION + " indiriliyor...");

            // Version manifest al
            JSONObject manifest = new JSONObject(readJsonFromUrl(VERSION_MANIFEST_URL));
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
                System.out.println("❌ Version " + MINECRAFT_VERSION + " bulunamadı!");
                return false;
            }

            // Version bilgilerini al
            JSONObject versionJson = new JSONObject(readJsonFromUrl(versionUrl));

            // Version JSON'unu kaydet
            Path versionJsonPath = VERSIONS_DIR.resolve(MINECRAFT_VERSION).resolve(MINECRAFT_VERSION + ".json");
            Files.writeString(versionJsonPath, versionJson.toString(2));

            // Client JAR'ı indir
            JSONObject clientInfo = versionJson.getJSONObject("downloads").getJSONObject("client");
            Path clientJarPath = VERSIONS_DIR.resolve(MINECRAFT_VERSION).resolve(MINECRAFT_VERSION + ".jar");
            
            if (!Files.exists(clientJarPath) || !verifyFileHash(clientJarPath, clientInfo.getString("sha1"), "sha1")) {
                downloadFileWithHash(clientInfo.getString("url"), clientJarPath, clientInfo.getString("sha1"), "sha1");
                System.out.println("✓ Client JAR indirildi");
            }

            // Libraries'i indir
            downloadLibrariesParallel(versionJson.getJSONArray("libraries"));

            // Assets'leri indir
            downloadAssets(versionJson.getJSONObject("assetIndex"));

            return true;
        } catch (Exception e) {
            System.out.println("❌ Minecraft indirme hatası: " + e.getMessage());
            return false;
        }
    }
}