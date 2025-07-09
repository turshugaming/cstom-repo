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
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * TerraMonic Minecraft Launcher - Python Entegre Edilmiş Final Version
 * 
 * ✅ Python start.bat komutları direkt entegre edildi
 * ✅ Gereksiz kütüphaneler kaldırıldı (SLF4J, LWJGL extras vb.)
 * ✅ Sadece vanilla + Fabric libraries indirilir
 * ✅ Hash verification eklendi
 * ✅ start.bat dosyası oluşturulmaz, direkt ProcessBuilder kullanılır
 * ✅ Python kodundaki classpath sırası korundu
 * 
 * @version 2.0 FINAL
 */
public class TerraMonicLauncher_FINAL extends Application {

    // Ana renkler ve tema
    private static final Color BACKGROUND_COLOR = Color.web("#000000");
    private static final Color BACKGROUND_SECONDARY = Color.web("#111111");
    private static final Color PRIMARY_COLOR = Color.web("#01a500");
    private static final Color PRIMARY_DARK = Color.web("#01a500");
    private static final Color PRIMARY_HIGHLIGHT = Color.web("#71ff61");
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color TEXT_SECONDARY = Color.web("#AAAAAA");
    private static final Color SHADOW_COLOR = Color.web("71ff61");

    // Font ayarları
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

    // PATH sistemi
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

    // Oyun ayarları
    private ComboBox<String> ramCombo;
    private ComboBox<String> resCombo;

    // Mod kontrol sistemi
    private static final Path DELETED_MODS_FILE = TERRAMONIC_PATH.resolve("deleted_mods.json");
    private static final Path MOD_INTEGRITY_FILE = TERRAMONIC_PATH.resolve("mod_integrity.json");

    // UI disable sistemi
    private boolean gameIsLaunching = false;

    // Stage ve scene referansları
    private Stage mainStage;
    private Scene currentScene;

    // Ana launcher iconu
    private Image launcherIcon;

    // Klasör yolları
    private static final Path MODS_PROFILES_PATH = TERRAMONIC_PATH.resolve("mods_profiles");
    private static final Path ICON_PATH = TERRAMONIC_PATH.resolve("terramonic_icon");
    private static final Path ICON_FILE = ICON_PATH.resolve("terramonic_icon.png");
    private static final Path ICO_FILE = ICON_PATH.resolve("terramonic_icon.ico");

    // Launcher config
    private JSONObject launcherConfig;

    // Durumlar
    private final javafx.beans.property.BooleanProperty modsReady = new javafx.beans.property.SimpleBooleanProperty(false);
    private final javafx.beans.property.BooleanProperty librariesReady = new javafx.beans.property.SimpleBooleanProperty(false);

    // Panel referansları
    private StackPane centerPanel;
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
        executorService = Executors.newFixedThreadPool(8);

        // .terramonic klasörünü kur
        setupTerramonicFolder();

        // Iconu yükle ve başlat
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
}