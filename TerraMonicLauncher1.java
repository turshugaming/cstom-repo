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
     * Iconu yükler ve launcher'ı başlatır
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
                            System.out.println("Dosya silinemedi: " + path + " - " + e.getMessage());
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
     * Splash ekranını gösterir
     */
    private void showSplashScreen() {
        // İcon yükleme kontrolü
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

        // Bakım modu kontrolü
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
        root.setBackground(new Background(new BackgroundFill(
                BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        root.getChildren().addAll(decorPane, centerContent);

        final Scene splashScene = new Scene(root, windowWidth, windowHeight);
        splashScene.setFill(BACKGROUND_COLOR);
        mainStage.setScene(splashScene);
        currentScene = splashScene;

        // Setup task
        setupTask(loadingBar, loadingLabel, subtitle);
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
     * Kurulum görevini başlatır
     */
    private void setupTask(ProgressBar loadingBar, Label loadingLabel, Label subtitle) {
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

                // Versiyon kontrolü
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

                // Dosyalar kontrolü
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

            loadingAnimation.setOnFinished(event -> {
                final FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.8), currentScene.getRoot());
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
            System.out.println("❌ Setup task başarısız: " + exception.getMessage());
            exception.printStackTrace();

            Platform.runLater(() -> {
                showError("Kurulum başarısız: " + exception.getMessage());
                loadingLabel.setText("Hata oluştu!");
            });
        });

        executorService.submit(setupTask);
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
            Text logoText = new Text("TerraMonic");
            logoText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 60));
            logoText.setFill(PRIMARY_COLOR);

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

        // İstatistikler
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        VBox onlineStats = createStatBox("127", "Çevrimiçi Oyuncu");
        VBox serverStats = createStatBox("AÇIK", "Sunucu Durumu");
        serverStats.getChildren().get(0).setStyle("-fx-text-fill: " + toHexString(PRIMARY_COLOR) + ";");

        statsBox.getChildren().addAll(onlineStats, serverStats);

        leftPanel.getChildren().addAll(logoView, new VBox(20), sloganLabel, new VBox(10),
                subSloganLabel, new VBox(40), statsBox);

        // Sağ panel - Giriş formu
        VBox rightPanel = createLoginPanel();

        // Ana düzeni ayarla
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
     * Login paneli oluşturur
     */
    private VBox createLoginPanel() {
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

        return rightPanel;
    }

    /**
     * İstatistik kutusu oluşturur
     */
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

    /**
     * Config ve mods klasörünü temizler
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

            Files.createDirectories(configPath);
            Files.createDirectories(modsPath);

            System.out.println("Config ve mods klasörleri temizlendi - launcher güncellemesi nedeniyle");
        } catch (IOException e) {
            System.out.println("Klasör temizleme hatası: " + e.getMessage());
        }
    }

    /**
     * ZIP dosyasını indirir ve çıkarır
     */
    private void downloadAndExtractZip(String zipUrl, Path targetDir) throws IOException {
        Path ekdosyalarZip = TERRAMONIC_PATH.resolve("ekdosyalar.zip");
        Path nestedZip = TERRAMONIC_PATH.resolve(generateComplexZipName());

        boolean isNestedZipValid = Files.exists(nestedZip) && isValidZip(nestedZip);

        if (!Files.exists(ekdosyalarZip) || !isValidZip(ekdosyalarZip)) {
            Platform.runLater(() -> {
                if (statusLabel != null) statusLabel.setText("ekdosyalar.zip indiriliyor...");
            });
            try (InputStream in = new URL(zipUrl).openStream();
                 OutputStream out = Files.newOutputStream(ekdosyalarZip)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            Platform.runLater(() -> {
                if (statusLabel != null) statusLabel.setText("ekdosyalar.zip indirildi!");
            });
        }

        if (!isNestedZipValid) {
            Files.createDirectories(nestedZip.getParent());
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(nestedZip))) {
                ZipEntry entry = new ZipEntry("ekdosyalar.zip");
                zos.putNextEntry(entry);
                Files.copy(ekdosyalarZip, zos);
                zos.closeEntry();
            }
            Platform.runLater(() -> {
                if (statusLabel != null) statusLabel.setText("Nested ZIP oluşturuldu: " + nestedZip.getFileName());
            });
        }

        checkAndExtractMissingFiles(nestedZip, targetDir);
    }

    /**
     * ZIP dosyasının geçerli olup olmadığını kontrol eder
     */
    private boolean isValidZip(Path zipPath) {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            return zis.getNextEntry() != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Karmaşık ZIP dosya adı oluşturur
     */
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
     * Eksik dosyaları kontrol eder ve çıkarır
     */
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
        Platform.runLater(() -> {
            if (statusLabel != null) statusLabel.setText("Dosya kontrolü ve çıkarma tamamlandı!");
        });
    }

    /**
     * Fabric kurulumunu yapar
     */
    private void setupFabric() throws IOException, InterruptedException {
        String fabricVersionName = "fabric-loader-" + FABRIC_VERSION + "-" + MINECRAFT_VERSION;
        Path versionsDir = TERRAMONIC_PATH.resolve("versions");
        Path fabricVersionDir = versionsDir.resolve(fabricVersionName);
        Path fabricJarPath = fabricVersionDir.resolve(fabricVersionName + ".jar");
        Path fabricJsonPath = fabricVersionDir.resolve(fabricVersionName + ".json");

        Files.createDirectories(fabricVersionDir);

        if (!Files.exists(fabricJarPath) && launcherConfig != null && launcherConfig.has("jar")) {
            System.out.println("Client jar indiriliyor...");
            String clientJarUrl = launcherConfig.getString("jar");
            downloadFile(clientJarUrl, fabricJarPath);
            System.out.println("Client jar indirildi: " + fabricJarPath);
        }

        if (!Files.exists(fabricJsonPath)) {
            System.out.println("Fabric installer indiriliyor...");

            Path fabricInstallerPath = TERRAMONIC_PATH.resolve("fabric-installer.jar");
            downloadFile(FABRIC_INSTALLER_URL, fabricInstallerPath);

            System.out.println("Fabric kuruluyor...");

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

            Files.deleteIfExists(fabricInstallerPath);

            System.out.println("Fabric başarıyla kuruldu: " + fabricVersionName);
        }

        Platform.runLater(() -> {
            downloadMinecraftLibraries();
        });
    }

    /**
     * Minecraft kütüphanelerini indirir
     */
    private void downloadMinecraftLibraries() {
        Task<Void> libraryTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> statusLabel.setText("Minecraft kütüphaneleri indiriliyor..."));

                String versionManifestUrl = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
                String manifestContent = readJsonFromUrl(versionManifestUrl);
                JSONObject manifest = new JSONObject(manifestContent);

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

                String versionContent = readJsonFromUrl(versionUrl);
                JSONObject versionJson = new JSONObject(versionContent);

                Path librariesDir = TERRAMONIC_PATH.resolve("libraries");
                Files.createDirectories(librariesDir);

                downloadSLF4JLibraries(librariesDir);
                extractLWJGLNatives(librariesDir);

                JSONArray libraries = versionJson.getJSONArray("libraries");
                int totalLibs = libraries.length();
                for (int i = 0; i < totalLibs; i++) {
                    JSONObject library = libraries.getJSONObject(i);

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

                // Client JAR
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

                // Assets
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
                showError("Minecraft kütüphaneleri indirilemedi: " + exception.getMessage());
            });
        });

        executorService.submit(libraryTask);
    }

    /**
     * SLF4J ve gerekli kütüphaneleri indirir
     */
    private void downloadSLF4JLibraries(Path librariesDir) throws IOException {
        String[][] slf4jLibraries = {
                {"org/slf4j", "slf4j-api", "2.0.13"},
                {"net/sf/jopt-simple", "jopt-simple", "5.0.4"},
                {"org/lwjgl", "lwjgl", "3.3.3"},
                {"org/lwjgl", "lwjgl-opengl", "3.3.3"},
                {"org/lwjgl", "lwjgl-glfw", "3.3.3"},
                {"org/lwjgl", "lwjgl-stb", "3.3.3"},
                {"org/lwjgl", "lwjgl-tinyfd", "3.3.3"},
                {"org/lwjgl", "lwjgl-openal", "3.3.6"},
                {"com/mojang", "logging", "1.1.1"},
                {"com/mojang", "brigadier", "1.0.18"},
                {"com/mojang", "datafixerupper", "8.0.16"},
                {"com/mojang", "authlib", "6.0.54"},
                {"org/joml", "joml", "1.10.8"},
                {"it/unimi/dsi", "fastutil", "8.5.13"},
                {"org/apache/commons", "commons-lang3", "3.14.0"},
                {"com/google/guava", "guava", "32.1.3-jre"},
                {"io/netty", "netty-all", "4.1.109.Final"},
                {"com/google/code/gson", "gson", "2.10.1"},
                {"commons-io", "commons-io", "2.15.1"},
                {"org/apache/logging/log4j", "log4j-api", "2.22.1"},
                {"org/apache/logging/log4j", "log4j-core", "2.22.1"},
                {"commons-codec", "commons-codec", "1.18.0"}
        };

        for (String[] lib : slf4jLibraries) {
            String groupPath = lib[0];
            String artifactId = lib[1];
            String version = lib[2];

            String fileName = artifactId + "-" + version + ".jar";

            Path libPath = librariesDir.resolve(groupPath)
                    .resolve(artifactId)
                    .resolve(version)
                    .resolve(fileName);

            if (!Files.exists(libPath)) {
                Files.createDirectories(libPath.getParent());

                String downloadUrl = "https://repo1.maven.org/maven2/" +
                        groupPath + "/" + artifactId + "/" + version + "/" + fileName;

                try {
                    Platform.runLater(() -> statusLabel.setText("🔧 Gerekli kütüphane indiriliyor: " + artifactId));
                    downloadFile(downloadUrl, libPath);
                    System.out.println("✅ Gerekli kütüphane indirildi: " + fileName);
                } catch (IOException e) {
                    System.out.println("⚠️ Kütüphane indirilemedi: " + fileName + " - " + e.getMessage());

                    try {
                        String alternativeUrl;
                        if (groupPath.startsWith("com/mojang")) {
                            alternativeUrl = "https://libraries.minecraft.net/" +
                                    groupPath + "/" + artifactId + "/" + version + "/" + fileName;
                        } else {
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
     * LWJGL native DLL'lerini çıkarır
     */
    private void extractLWJGLNatives(Path librariesDir) throws IOException {
        System.out.println("🔧 LWJGL native'ları extract ediliyor...");
        Path nativesDir = TERRAMONIC_PATH.resolve("natives");

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

        String osName = System.getProperty("os.name").toLowerCase();
        String platform;
        if (osName.contains("win")) {
            platform = "natives-windows";
        } else if (osName.contains("linux")) {
            platform = "natives-linux";
        } else if (osName.contains("mac")) {
            platform = "natives-macos";
        } else {
            platform = "natives-windows";
        }

        System.out.println("🖥️ Platform tespit edildi: " + platform);

        String[] lwjglModules = {"lwjgl", "lwjgl-opengl", "lwjgl-glfw", "lwjgl-stb", "lwjgl-tinyfd", "lwjgl-openal"};

        for (String module : lwjglModules) {
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
                System.out.println("⚠️ Native JAR bulunamadı (" + module + ")");
                continue;
            }

            if (Files.exists(nativeJarPath)) {
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(nativeJarPath))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        String entryName = entry.getName();
                        boolean isNative = (entryName.endsWith(".dll") ||
                                entryName.endsWith(".so") ||
                                entryName.endsWith(".dylib")) && !entry.isDirectory();

                        if (isNative) {
                            String fileName = Paths.get(entry.getName()).getFileName().toString();
                            Path dllPath = nativesDir.resolve(fileName);

                            if (!Files.exists(dllPath)) {
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
     * Modrinth mod paketini indirir ve yükler
     */
    private void downloadAndInstallModrinthPack() {
        System.out.println("🔄 Modrinth pack kontrolü başlıyor...");

        if (launcherConfig == null) {
            System.out.println("❌ LauncherConfig null - Modrinth pack yüklenemez");
            return;
        }

        if (!launcherConfig.has("modrinth_pack")) {
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
                System.out.println("🚀 Modrinth task başlıyor...");
                Platform.runLater(() -> {
                    statusLabel.setText("📦 Modlar indiriliyor...");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                });

                // .mrpack dosyasını indir
                Path mrpackPath = Files.createTempFile("terramonic_pack", ".mrpack");
                System.out.println("📁 Mrpack dosyası indiriliyor: " + mrpackPath);
                downloadFile(modrinthUrl, mrpackPath);
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
                    statusLabel.setText("⬬ Mod dosyaları indiriliyor...");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                });

                // Mods klasörünü temizle
                Path modsDir = TERRAMONIC_PATH.resolve("mods");
                if (Files.exists(modsDir)) {
                    deleteDirectory(modsDir);
                }
                Files.createDirectories(modsDir);

                // Her mod dosyasını indir
                JSONArray files = indexJson.getJSONArray("files");
                int modCount = 0;
                for (int i = 0; i < files.length(); i++) {
                    JSONObject fileObj = files.getJSONObject(i);
                    String filePath = fileObj.getString("path");

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
                            downloadFile(downloadUrl, targetPath);
                        }
                    }
                }

                // Temizlik
                deleteDirectory(tempExtractDir);
                Files.deleteIfExists(mrpackPath);
                modsReady.set(true);
                refreshModPanelUI();

                Platform.runLater(() -> {
                    statusLabel.setText("✅ Modlar başarıyla yüklendi!");
                    statusLabel.setTextFill(PRIMARY_COLOR);
                });

                return null;
            }
        };

        modrinthTask.setOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            System.out.println("❌ Modrinth pack kurulumu başarısız: " + exception.getMessage());
            exception.printStackTrace();

            Platform.runLater(() -> {
                statusLabel.setText("Modrinth pack kurulumu başarısız!");
                showError("Modrinth pack kurulumu başarısız: " + exception.getMessage());
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
     * Oyunu başlatır
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

            gameIsLaunching = true;

            downloadProgress.setVisible(true);
            statusLabel.setVisible(true);
            statusLabel.setText("🚀 Oyun başlatılıyor...");
            statusLabel.setTextFill(PRIMARY_COLOR);
            playButton.setDisable(true);

            Task<Void> launchTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    // RAM ve çözünürlük ayarları
                    String selectedRam = ramCombo != null ? ramCombo.getSelectionModel().getSelectedItem() : "4 GB";
                    String selectedRes = resCombo != null ? resCombo.getSelectionModel().getSelectedItem() : "1280x720";

                    if (selectedRam == null) selectedRam = "4 GB";
                    if (selectedRes == null) selectedRes = "1280x720";

                    String ramAmount = selectedRam.replace(" GB", "G");

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

                    addEssentialLibrariesToClasspath(classpath, librariesDir);

                    // JTrace stub JAR
                    Path jtracyStubJar = ensureJTraceStub(librariesDir);
                    if (jtracyStubJar != null && Files.exists(jtracyStubJar)) {
                        if (classpath.length() > 0) {
                            classpath.append(";");
                        }
                        classpath.append(jtracyStubJar.toString());
                    }

                    // Fabric JSON'dan library'leri oku
                    Path fabricJsonPath = fabricVersionDir.resolve(fabricVersionName + ".json");
                    if (Files.exists(fabricJsonPath)) {
                        String fabricJsonContent = Files.readString(fabricJsonPath);
                        JSONObject fabricJson = new JSONObject(fabricJsonContent);
                        JSONArray libraries = fabricJson.getJSONArray("libraries");

                        for (int i = 0; i < libraries.length(); i++) {
                            JSONObject library = libraries.getJSONObject(i);
                            String name = library.getString("name");

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

                    // Minecraft client JAR
                    Path minecraftClientPath = TERRAMONIC_PATH.resolve("versions")
                            .resolve(MINECRAFT_VERSION)
                            .resolve(MINECRAFT_VERSION + ".jar");

                    if (Files.exists(minecraftClientPath)) {
                        if (classpath.length() > 0) {
                            classpath.append(";");
                        }
                        classpath.append(minecraftClientPath.toString());
                    }

                    // Fabric JAR
                    if (classpath.length() > 0) {
                        classpath.append(";");
                    }
                    classpath.append(fabricJarPath.toString());

                    // Java command oluştur
                    List<String> command = new ArrayList<>();
                    command.add("java");
                    command.add("-Xmx" + ramAmount);
                    command.add("-Xms1G");
                    command.add("--enable-native-access=ALL-UNNAMED");
                    command.add("--add-opens");
                    command.add("java.base/java.lang=ALL-UNNAMED");
                    command.add("--add-opens");
                    command.add("java.base/java.util=ALL-UNNAMED");
                    command.add("--add-opens");
                    command.add("java.base/java.io=ALL-UNNAMED");
                    command.add("-Dorg.slf4j.simpleLogger.defaultLogLevel=WARN");
                    command.add("-Dlog4j2.formatMsgNoLookups=true");
                    command.add("-Dcom.mojang.jtracy.disable=true");
                    command.add("-Dmojang.tracy.enabled=false");
                    command.add("-Dorg.lwjgl.librarypath=" + TERRAMONIC_PATH.resolve("natives").toString());
                    command.add("-Dminecraft.launcher.brand=TerraMonic");
                    command.add("-Dminecraft.launcher.version=1.0.2");
                    command.add("-cp");
                    command.add(classpath.toString());
                    command.add("net.fabricmc.loader.impl.launch.knot.KnotClient");
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

                    // Process output'unu oku
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
                    gameIsLaunching = false;
                    downloadProgress.setVisible(false);
                    statusLabel.setText("✅ Minecraft başarıyla başlatıldı!");
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
                System.out.println("❌ Oyun başlatma hatası: " + exception.getMessage());
                exception.printStackTrace();
                Platform.runLater(() -> {
                    gameIsLaunching = false;
                    downloadProgress.setVisible(false);
                    playButton.setDisable(false);
                    showError("Oyun başlatılamadı: " + exception.getMessage());
                });
            });

            executorService.submit(launchTask);
        });
    }

    /**
     * Classpath'e gerekli kütüphaneleri ekler
     */
    private void addEssentialLibrariesToClasspath(StringBuilder classpath, Path librariesDir) {
        String[][] essentialLibraries = {
                {"org/slf4j", "slf4j-api", "2.0.13"},
                {"net/sf/jopt-simple", "jopt-simple", "5.0.4"},
                {"org/lwjgl", "lwjgl", "3.3.3"},
                {"org/lwjgl", "lwjgl-opengl", "3.3.3"},
                {"org/lwjgl", "lwjgl-glfw", "3.3.3"},
                {"org/lwjgl", "lwjgl-stb", "3.3.3"},
                {"org/lwjgl", "lwjgl-tinyfd", "3.3.3"},
                {"org/lwjgl", "lwjgl-openal", "3.3.6"},
                {"com/mojang", "logging", "1.1.1"},
                {"com/mojang", "brigadier", "1.0.18"},
                {"com/mojang", "datafixerupper", "8.0.16"},
                {"com/mojang", "authlib", "6.0.54"},
                {"org/joml", "joml", "1.10.8"},
                {"it/unimi/dsi", "fastutil", "8.5.13"},
                {"org/apache/commons", "commons-lang3", "3.14.0"},
                {"com/google/guava", "guava", "32.1.3-jre"},
                {"io/netty", "netty-all", "4.1.109.Final"},
                {"com/google/code/gson", "gson", "2.10.1"},
                {"commons-io", "commons-io", "2.15.1"},
                {"org/apache/logging/log4j", "log4j-api", "2.22.1"},
                {"org/apache/logging/log4j", "log4j-core", "2.22.1"},
                {"commons-codec", "commons-codec", "1.18.0"}
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
     * JTrace stub JAR oluşturur
     */
    private Path ensureJTraceStub(Path librariesDir) {
        Path stubJarPath = librariesDir.resolve("com/mojang/jtracy/0.1.0/jtracy-0.1.0.jar");
        try {
            if (Files.exists(stubJarPath)) {
                return stubJarPath;
            }
            Files.createDirectories(stubJarPath.getParent());

            try (java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(Files.newOutputStream(stubJarPath))) {
                // Zone class
                String zoneClass = "package com.mojang.jtracy; public final class Zone implements AutoCloseable { public Zone(String name) {} @Override public void close() {} }";
                jos.putNextEntry(new java.util.jar.JarEntry("com/mojang/jtracy/Zone.class"));
                jos.write(createClassBytes(zoneClass));
                jos.closeEntry();

                // TracyClient class  
                String tracyClass = "package com.mojang.jtracy; public final class TracyClient { private static boolean a=false; private TracyClient(){} public static boolean isActive(){return a;} public static void beginFrame(){} public static void endFrame(){} public static Zone zone(String n){return new Zone(n);} }";
                jos.putNextEntry(new java.util.jar.JarEntry("com/mojang/jtracy/TracyClient.class"));
                jos.write(createClassBytes(tracyClass));
                jos.closeEntry();
            }
            System.out.println("✅ JTrace stub JAR oluşturuldu: " + stubJarPath);
        } catch (Exception ex) {
            System.out.println("⚠️ JTrace stub JAR oluşturulamadı: " + ex.getMessage());
        }
        return stubJarPath;
    }

    private byte[] createClassBytes(String javaSource) {
        // Basit stub implementation
        return new byte[]{
                (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, // magic
                0x00, 0x00, 0x00, 0x41, // version (Java 21)
                0x00, 0x0A, // constant pool count
                0x01, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, // dummy constant pool entries
                0x00, 0x21, // access flags (public)
                0x00, 0x02, // this class
                0x00, 0x04, // super class
                0x00, 0x00, // interfaces count
                0x00, 0x00, // fields count
                0x00, 0x01, // methods count
                0x00, 0x01, // access flags
                0x00, 0x05, // name index
                0x00, 0x06, // descriptor index
                0x00, 0x01, // attributes count
                0x00, 0x07, // attribute name index
                0x00, 0x00, 0x00, 0x00 // attribute length
        };
    }

    /**
     * Hata mesajı gösterir
     */
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

    /**
     * Bilgi mesajı gösterir
     */
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

    /**
     * Ana ekrana geçiş yapar
     */
    private void transitionToMainScreen() {
        // Sadece basit geçiş - detaylı UI'yi sonra ekleyeceğiz
        Label label = new Label("TerraMonic Ana Ekran - Oyuncu: " + playerName);
        label.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        label.setTextFill(PRIMARY_COLOR);
        
        playButton = createStyledButton("🚀 OYUNU BAŞLAT", 220, 45);
        playButton.setOnAction(e -> launchGame());
        
        downloadProgress = new ProgressBar(0);
        downloadProgress.setPrefWidth(200);
        downloadProgress.setStyle("-fx-accent: " + toHexString(PRIMARY_COLOR) + ";");
        downloadProgress.setVisible(false);

        statusLabel.setFont(Font.font(FONT_FAMILY, 14));
        statusLabel.setTextFill(TEXT_SECONDARY);
        statusLabel.setVisible(false);
        
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(label, playButton, downloadProgress, statusLabel);
        
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        root.setCenter(content);
        
        Scene mainScene = new Scene(root, windowWidth, windowHeight);
        transitionToScene(mainScene);
    }

    /**
     * Sahne geçişi animasyonu
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

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newScene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
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

    /**
     * Pencere kontrol butonu oluşturur
     */
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

    /**
     * Stil uygulanmış text field oluşturur
     */
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

    /**
     * Stil uygulanmış buton oluşturur
     */
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

    /**
     * Buton tıklama animasyonu
     */
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

    /**
     * Node titretme animasyonu
     */
    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    /**
     * Sistem ikonlarını günceller
     */
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
            }
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

    /**
     * Mod panel UI'sini yeniler
     */
    private void refreshModPanelUI() {
        // Basit implementation - mod paneli henüz eklenmedi
        System.out.println("Mod panel UI yenilendi");
    }
}