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
    private static final String FONT_FAMILY = "Segoe UI";
    private static final double BUTTON_RADIUS = 18.0;

    // Launcher sabitleri
    private static final String LAUNCHER_VERSION = "v1.0.2";
    private static final String MINECRAFT_VERSION = "1.21.5";
    private static final String FABRIC_VERSION = "0.16.14";
    private static final String TERRAMONIC_URL = "https://www.terramonic.com";
    
    // Yeni birleşik JSON URL'i (Bu URL'i kendi sunucunuza/dropbox'ınıza göre değiştirin)
    private static final String LAUNCHER_JSON_URL = "https://raw.githubusercontent.com/example/terramonic-config/main/launcher.json";
    
    // Fabric installer
    private static final String FABRIC_INSTALLER_URL = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.0.3/fabric-installer-1.0.3.jar";
    
    private static final Path TERRAMONIC_PATH = Paths.get(System.getenv("APPDATA"), ".terramonic");
    private static final Path LAUNCHER_VERSION_JSON = TERRAMONIC_PATH.resolve("launcher_version.json");
    private String currentLauncherVersion = "v1.0.2"; // Dynamic launcher version

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
    private Label statusLabel;
    private ImageView userAvatar;
    private ExecutorService executorService;

    // Stage ve scene referansları
    private Stage mainStage;
    private Scene currentScene;

    // Ana launcher iconu
    private Image launcherIcon;

    // .terramonic klasör yolu
    private static final Path MODS_PROFILES_PATH = TERRAMONIC_PATH.resolve("mods_profiles");
    private static final Path ICON_PATH = TERRAMONIC_PATH.resolve("terramonic_icon");
    private static final Path ICON_FILE = ICON_PATH.resolve("icon.png");
    private static final Path ICO_FILE = ICON_PATH.resolve("icon.ico");

    // Launcher config data
    private JSONObject launcherConfig;

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
            // Gerekli alt klasörler
            Files.createDirectories(TERRAMONIC_PATH.resolve("mods"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("config"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("versions"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("libraries"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("natives"));
            Files.createDirectories(TERRAMONIC_PATH.resolve("assets"));
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
                // Com.terramonic klasöründeki icon.png'i kullan
                Path iconSource = Paths.get("src/main/resources/com/terramonic/icon.png");
                boolean iconSuccess = false;
                
                if (Files.exists(iconSource)) {
                    Files.copy(iconSource, ICON_FILE, StandardCopyOption.REPLACE_EXISTING);
                    iconSuccess = true;
                } else {
                    // Alternatif olarak resources'dan yükle
                    try (InputStream iconStream = getClass().getResourceAsStream("/com/terramonic/icon.png")) {
                        if (iconStream != null) {
                            Files.copy(iconStream, ICON_FILE, StandardCopyOption.REPLACE_EXISTING);
                            iconSuccess = true;
                        }
                    }
                }

                if (iconSuccess) {
                    launcherIcon = new Image(ICON_FILE.toUri().toString(), true);
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
        });

        iconTask.setOnFailed(e -> {
            System.out.println("İkon yükleme hatası: " + e.getSource().getException());
            showSplashScreen();
            mainStage.show();
        });

        executorService.submit(iconTask);
    }

    /**
     * Launcher config'ini yükler (birleşik JSON)
     */
    private void loadLauncherConfig() {
        Task<JSONObject> configTask = new Task<>() {
            @Override
            protected JSONObject call() throws Exception {
                String configContent = readJsonFromUrl(LAUNCHER_JSON_URL);
                return new JSONObject(configContent);
            }
        };

        configTask.setOnSucceeded(event -> {
            launcherConfig = configTask.getValue();
            
            // Haberleri yükle
            loadNewsFromConfig();
            
            // Versiyonu kontrol et
            String remoteVersion = launcherConfig.getString("version");
            if (!currentLauncherVersion.replace("v", "").equals(remoteVersion)) {
                currentLauncherVersion = "v" + remoteVersion;
                // Config ve mods klasörünü temizle
                clearConfigAndMods();
            }
        });

        configTask.setOnFailed(event -> {
            System.out.println("Launcher config yüklenemedi: " + event.getSource().getException().getMessage());
            // Fallback haberler
            newsList.clear();
            newsList.add(new NewsItem(
                    "Hata",
                    "Launcher ayarları yüklenirken bir hata oluştu. Lütfen daha sonra tekrar deneyin.",
                    "N/A",
                    NewsItemType.GENEL
            ));
        });

        executorService.submit(configTask);
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
     * Modrinth mod paketini indirir ve yükler
     */
    private void downloadAndInstallModrinthPack() {
        if (launcherConfig == null || !launcherConfig.has("modrinth_pack")) {
            System.out.println("Modrinth pack URL bulunamadı");
            return;
        }
        
        Task<Void> modrinthTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String modrinthUrl = launcherConfig.getString("modrinth_pack");
                Platform.runLater(() -> statusLabel.setText("Modrinth pack indiriliyor..."));
                
                // .mrpack dosyasını indir
                Path mrpackPath = TERRAMONIC_PATH.resolve("terramonic_modpack.mrpack");
                downloadFile(modrinthUrl, mrpackPath);
                
                Platform.runLater(() -> statusLabel.setText("Modrinth pack çıkarılıyor..."));
                
                // .mrpack dosyasını çıkar (ZIP formatı)
                Path tempExtractDir = TERRAMONIC_PATH.resolve("temp_extract");
                if (Files.exists(tempExtractDir)) {
                    deleteDirectory(tempExtractDir);
                }
                Files.createDirectories(tempExtractDir);
                
                // ZIP olarak çıkar
                extractZip(mrpackPath, tempExtractDir);
                
                // modrinth.index.json'u oku
                Path indexPath = tempExtractDir.resolve("modrinth.index.json");
                if (!Files.exists(indexPath)) {
                    throw new IOException("modrinth.index.json bulunamadı!");
                }
                
                String indexContent = Files.readString(indexPath);
                JSONObject indexJson = new JSONObject(indexContent);
                
                Platform.runLater(() -> statusLabel.setText("Modlar indiriliyor..."));
                
                // Mods klasörünü temizle
                Path modsDir = TERRAMONIC_PATH.resolve("mods");
                if (Files.exists(modsDir)) {
                    deleteDirectory(modsDir);
                }
                Files.createDirectories(modsDir);
                
                // Her mod dosyasını indir
                JSONArray files = indexJson.getJSONArray("files");
                for (int i = 0; i < files.length(); i++) {
                    JSONObject fileObj = files.getJSONObject(i);
                    String filePath = fileObj.getString("path");
                    
                    // Sadece mods klasöründeki dosyaları işle
                    if (filePath.startsWith("mods/")) {
                        JSONArray downloads = fileObj.getJSONArray("downloads");
                        if (downloads.length() > 0) {
                            String downloadUrl = downloads.getString(0);
                            String fileName = Paths.get(filePath).getFileName().toString();
                            Path targetPath = modsDir.resolve(fileName);
                            
                            Platform.runLater(() -> statusLabel.setText("İndiriliyor: " + fileName));
                            downloadFile(downloadUrl, targetPath);
                            
                            System.out.println("İndirildi: " + fileName);
                        }
                    }
                }
                
                // Temizlik
                deleteDirectory(tempExtractDir);
                Files.deleteIfExists(mrpackPath);
                
                Platform.runLater(() -> statusLabel.setText("Modrinth pack kurulumu tamamlandı!"));
                
                return null;
            }
        };

        modrinthTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                statusLabel.setText("Modrinth pack kurulumu başarısız!");
                showError("Modrinth pack kurulumu başarısız: " + event.getSource().getException().getMessage());
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

        // Launcher config'i yükle
        loadLauncherConfig();

        // Bakım modu kontrolü
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

                // Config'den versiyon al
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
            Platform.runLater(() -> {
                showError("Kurulum başarısız: " + e.getSource().getException().getMessage());
                loadingLabel.setText("Hata oluştu!");
            });
        });

        executorService.submit(setupTask);
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
     * Dosyayı indirir
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
     * JSON dosyasını okur
     */
    private String readJsonFromUrl(String url) throws IOException {
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

    /**
     * Klasörü ve içeriğini siler
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
     * Color nesnesini hex string'e dönüştürür
     */
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    /**
     * Hata mesajı gösterir
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Bilgi mesajı gösterir
     */
    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Bilgi");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Placeholder metodlar - UI kısmını buraya ekleyelim
    
    private void showLoginScreen() {
        // Login screen implementation placeholder
        System.out.println("Login screen would be shown here");
        // For now, skip to main screen
        transitionToMainScreen();
    }
    
    private void transitionToMainScreen() {
        // Main screen implementation placeholder  
        System.out.println("Main screen would be shown here");
    }
    
    private void updateSystemIcons() {
        // System icons update placeholder
        System.out.println("System icons would be updated here");
    }

    /**
     * Haber öğesi sınıfı
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
     * Haber türü enum
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