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
    private static final String ICON_URL = "https://www.dropbox.com/scl/fi/2yc75kqokrtivw202rt3w/icon.png?rlkey=1blmy791i17gs6t78ecjc3qxf&st=cjc590fc&dl=1";
    
    // Yeni birleşik JSON URL'i - ESKİ 3 AYRI JSON YERİNE TEK JSON
    // *** TEST İÇİN LOCAL DOSYA KULLANILIYOR - KENDİ URL'İNİZİ BURAYA YAZIN ***
    private static final String LAUNCHER_JSON_URL = "file:///" + System.getProperty("user.dir").replace("\\", "/") + "/launcher.json";
    
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
    private static final Path ICON_FILE = ICON_PATH.resolve("terramonic_icon.png");
    private static final Path ICO_FILE = ICON_PATH.resolve("terramonic_icon.ico");

    // Launcher config data - YENİ BİRLEŞİK JSON SİSTEMİ
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
                // ESKİ SİSTEM KORUNDU - PNG'yi indir
                boolean pngSuccess = downloadIconFile(ICON_URL, ICON_FILE);

                // PNG yüklenemediyse uygulama ikonu olmadan devam et
                if (pngSuccess) {
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

            // YENİ BİRLEŞİK JSON SİSTEMİ - Önce config'i yükle
            loadLauncherConfig();
            
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
     * Launcher config'ini yükler (YENİ BİRLEŞİK JSON SİSTEMİ)
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
            
            System.out.println("Launcher config yüklendi! Haberler sayısı: " + newsList.size());
            
            // Eğer ana ekran açıksa haberleri refresh et
            Platform.runLater(() -> {
                try {
                    if (currentScene != null && currentScene.getRoot() instanceof BorderPane) {
                        BorderPane root = (BorderPane) currentScene.getRoot();
                        VBox vbox = (VBox) root.getCenter();
                        if (vbox != null && vbox.getChildren().size() > 1) {
                            BorderPane mainContent = (BorderPane) vbox.getChildren().get(1);
                            StackPane centerPanel = (StackPane) mainContent.getCenter();
                            if (centerPanel != null) {
                                // Ana sayfaysa haberleri refresh et
                                centerPanel.getChildren().clear();
                                centerPanel.getChildren().add(createNewsPanel());
                                System.out.println("Haberler refresh edildi!");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Haber refresh hatası: " + e.getMessage());
                }
            });
        });

        configTask.setOnFailed(event -> {
            System.out.println("Launcher config yüklenemedi: " + event.getSource().getException().getMessage());
            // Fallback haberler
            newsList.clear();
            newsList.add(new NewsItem(
                    "Hata - JSON Yüklenemedi",
                    "launcher.json dosyası okunamadı. URL'yi kontrol edin veya dosyanın mevcut olduğundan emin olun.",
                    "N/A",
                    NewsItemType.GENEL
            ));
            
            // Ana ekranda haber refresh et
            Platform.runLater(() -> {
                try {
                    if (currentScene != null && currentScene.getRoot() instanceof BorderPane) {
                        BorderPane root = (BorderPane) currentScene.getRoot();
                        VBox vbox = (VBox) root.getCenter();
                        if (vbox != null && vbox.getChildren().size() > 1) {
                            BorderPane mainContent = (BorderPane) vbox.getChildren().get(1);
                            StackPane centerPanel = (StackPane) mainContent.getCenter();
                            if (centerPanel != null) {
                                centerPanel.getChildren().clear();
                                centerPanel.getChildren().add(createNewsPanel());
                                System.out.println("Fallback haberler gösterildi!");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Fallback haber refresh hatası: " + e.getMessage());
                }
            });
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
     * ESKİ HABERLERİ YÜKLEME SİSTEMİ - FALLBACK OLARAK KORUNDU
     */
    private void initializeNews() {
        Task<List<NewsItem>> fetchNewsTask = new Task<>() {
            @Override
            protected List<NewsItem> call() throws Exception {
                List<NewsItem> items = new ArrayList<>();
                try {
                    // Yeni sistemde config'den yükle
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

                            items.add(new NewsItem(title, content, date, type));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Config'den haber yüklenemedi: " + e.getMessage());
                }
                return items;
            }
        };

        fetchNewsTask.setOnSucceeded(event -> {
            newsList = fetchNewsTask.getValue();
            if (newsList.isEmpty()) {
                newsList.add(new NewsItem(
                        "Haber Bulunamadı",
                        "Şu anda gösterilecek haber yok. Daha sonra tekrar kontrol edin.",
                        "N/A",
                        NewsItemType.GENEL
                ));
            }
        });

        fetchNewsTask.setOnFailed(event -> {
            System.out.println("Haberler yüklenemedi: " + event.getSource().getException().getMessage());
            newsList.clear();
            newsList.add(new NewsItem(
                    "Hata",
                    "Haberler yüklenirken bir hata oluştu. Lütfen daha sonra tekrar deneyin.",
                    "N/A",
                    NewsItemType.GENEL
            ));
        });

        executorService.submit(fetchNewsTask);
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
     * Haber panelini oluşturur
     */
    private ScrollPane createNewsPanel() {
        // Haberleri içeren panel
        VBox newsContainer = new VBox(20);
        newsContainer.setPadding(new Insets(20));

        // Başlık
        Label newsTitle = new Label("HABERLER & DUYURULAR");
        newsTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        newsTitle.setTextFill(PRIMARY_COLOR);

        newsContainer.getChildren().add(newsTitle);
        newsContainer.getChildren().add(new Separator());

        // Haberler için kapsayıcı
        VBox newsListContainer = new VBox(15);

        // Haberler
        for (NewsItem news : newsList) {
            VBox newsCard = createNewsCard(news);
            newsListContainer.getChildren().add(newsCard);
        }

        newsContainer.getChildren().add(newsListContainer);

        // Alt kısım - TerraMonic websitesi bağlantısı
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

        // ScrollPane oluştur
        ScrollPane scrollPane = new ScrollPane(newsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("news-scroll");
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );

        return scrollPane;
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
                if (launcherConfig != null && launcherConfig.has("dosyalar")) {
                    String zipUrl = launcherConfig.getString("dosyalar");
                    
                    // ZIP dosyasını indir ve .terramonic'e çıkar
                    Platform.runLater(() -> loadingLabel.setText("Dosyalar indiriliyor ve çıkarılıyor..."));
                    downloadAndExtractZip(zipUrl, TERRAMONIC_PATH);
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
     * ESKİ SİSTEM KORUNDU - Downloads ekdosyalar.zip, nests it in a complex-named ZIP, and extracts it to targetDir
     */
    private void downloadAndExtractZip(String zipUrl, Path targetDir) throws IOException {
        Path ekdosyalarZip = TERRAMONIC_PATH.resolve("ekdosyalar.zip");
        Path nestedZip = TERRAMONIC_PATH.resolve(generateComplexZipName());

        // Check if the nested ZIP exists and is valid
        boolean isNestedZipValid = Files.exists(nestedZip) && isValidZip(nestedZip);

        // Download ekdosyalar.zip if it doesn't exist or is invalid
        if (!Files.exists(ekdosyalarZip) || !isValidZip(ekdosyalarZip)) {
            Platform.runLater(() -> statusLabel.setText("ekdosyalar.zip indiriliyor..."));
            try (InputStream in = new URL(zipUrl).openStream();
                 OutputStream out = Files.newOutputStream(ekdosyalarZip)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            Platform.runLater(() -> statusLabel.setText("ekdosyalar.zip indirildi!"));
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
            Platform.runLater(() -> statusLabel.setText("Nested ZIP oluşturuldu: " + nestedZip.getFileName()));
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
        Platform.runLater(() -> statusLabel.setText("Dosya kontrolü ve çıkarma tamamlandı!"));
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
        StackPane centerPanel = new StackPane();
        centerPanel.setBackground(new Background(new BackgroundFill(
                BACKGROUND_SECONDARY, CornerRadii.EMPTY, Insets.EMPTY)));
        centerPanel.setPadding(new Insets(20));

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
                updateMenuItemStyles.accept(-1);

                // İçeriği değiştir
                StackPane centerPanel = (StackPane) ((BorderPane) ((VBox) root.getCenter()).getChildren().get(1)).getCenter();
                centerPanel.getChildren().clear();
                switch (navItems[index]) {
                    case "Ana Sayfa":
                        centerPanel.getChildren().add(createNewsPanel());
                        break;
                    case "Mod Paketleri":
                        centerPanel.getChildren().add(createModManagementPanel());
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
     * Mod yönetim panelini oluşturur - ESKİ SİSTEM KORUNDU
     */
    private VBox createModManagementPanel() {
        VBox modPanel = new VBox(20);
        modPanel.setPadding(new Insets(20));
        modPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_SECONDARY) + ";");

        // Başlık
        Label title = new Label("MOD YÖNETİMİ");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(PRIMARY_COLOR);

        // Mod listesi
        ListView<String> modListView = new ListView<>();
        modListView.setPrefHeight(300);
        modListView.setStyle(
                "-fx-background-color: #1A1A1A;" +
                        "-fx-text-fill: white;" +
                        "-fx-control-inner-background: #1A1A1A;"
        );

        // Mevcut modları yükle
        loadMods(modListView);

        // Modları sıfırla butonu
        Button resetModButton = createStyledButton("MODLARI SIFIRLA", 150, 40);
        resetModButton.setOnAction(e -> {
            try {
                deleteDirectory(TERRAMONIC_PATH.resolve("mods"));
                Files.createDirectories(TERRAMONIC_PATH.resolve("mods"));
                loadMods(modListView);
                showInfo("Modlar sıfırlandı!");
            } catch (IOException ex) {
                showError("Modlar sıfırlanamadı: " + ex.getMessage());
            }
        });

        // Mod kaldır butonu
        Button removeModButton = createStyledButton("MOD KALDIR", 150, 40);
        removeModButton.setOnAction(e -> {
            String selectedMod = modListView.getSelectionModel().getSelectedItem();
            if (selectedMod != null) {
                try {
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

        // Profil kaydet
        TextField profileNameField = createStyledTextField("Profil adı girin");
        Button saveProfileButton = createStyledButton("PROFİLİ KAYDET", 150, 40);
        saveProfileButton.setOnAction(e -> {
            String profileName = profileNameField.getText().trim();
            if (profileName.isEmpty()) {
                shakeNode(profileNameField);
                profileNameField.setStyle(profileNameField.getStyle() + "-fx-border-color: #FF3A3A;");
                return;
            }
            saveModProfile(profileName);
            profileNameField.clear();
        });

        // Profil yükle
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
            String selectedProfile = profileComboBox.getSelectionModel().getSelectedItem();
            if (selectedProfile != null) {
                loadModProfile(selectedProfile);
                loadMods(modListView);
            } else {
                showError("Lütfen yüklenecek bir profil seçin.");
            }
        });

        // Butonları düzenle
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

    /**
     * Hesap panelini oluşturur - ESKİ SİSTEM KORUNDU
     */
    private VBox createAccountPanel() {
        VBox accountPanel = new VBox(20);
        accountPanel.setPadding(new Insets(20));
        accountPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_SECONDARY) + ";");

        // Başlık
        Label title = new Label("HESAP");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(PRIMARY_COLOR);

        // Çıkış yap butonu
        Button logoutButton = createStyledButton("ÇIKIŞ YAP", 150, 40);
        logoutButton.setOnAction(e -> {
            playerName = "";
            showLoginScreen();
        });

        accountPanel.getChildren().addAll(title, new Separator(), logoutButton);
        return accountPanel;
    }

    /**
     * Ayarlar panelini oluşturur - ESKİ SİSTEM KORUNDU
     */
    private VBox createSettingsPanel() {
        VBox settingsPanel = new VBox(20);
        settingsPanel.setPadding(new Insets(20));
        settingsPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_SECONDARY) + ";");

        // Başlık
        Label title = new Label("AYARLAR");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(PRIMARY_COLOR);

        // Placeholder içerik
        Label content = new Label("Ayarlar bölümü geliştirme aşamasında.");
        content.setFont(Font.font(FONT_FAMILY, 16));
        content.setTextFill(TEXT_COLOR);

        settingsPanel.getChildren().addAll(title, new Separator(), content);
        return settingsPanel;
    }

    /**
     * Sağ profil panelini oluşturur - ESKİ SİSTEM KORUNDU
     */
    private VBox createProfilePanel() {
        VBox profilePanel = new VBox(15);
        profilePanel.setPadding(new Insets(20, 15, 20, 15));
        profilePanel.setPrefWidth(250);
        profilePanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        // Profil başlığı
        Label profileTitle = new Label("OYUNCU PROFİLİ");
        profileTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        profileTitle.setTextFill(TEXT_SECONDARY);

        // Kullanıcı avatarı
        Circle avatarCircle = new Circle(40);
        StackPane avatarStack = new StackPane();
        if (!playerName.isEmpty()) {
            // Avatar URL
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
                    // Varsayılan avatar
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
            // Varsayılan avatar
            avatarCircle.setFill(PRIMARY_COLOR);
            Text initials = new Text("U");
            initials.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 30));
            initials.setFill(Color.BLACK);
            avatarStack.getChildren().add(initials);
        }

        // Avatar kenar çizgisi
        Circle avatarBorder = new Circle(43);
        avatarBorder.setFill(Color.TRANSPARENT);
        avatarBorder.setStroke(PRIMARY_COLOR);
        avatarBorder.setStrokeWidth(2);

        avatarStack.getChildren().addAll(avatarBorder, avatarCircle);

        // Kullanıcı adı
        Label usernameLabel = new Label(playerName.isEmpty() ? "Kullanıcı" : playerName);
        usernameLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        usernameLabel.setTextFill(TEXT_COLOR);

        // Hesap tipi
        Label accountTypeLabel = new Label("Oyuncu");
        accountTypeLabel.setFont(Font.font(FONT_FAMILY, 14));
        accountTypeLabel.setTextFill(TEXT_SECONDARY);

        // Kullanıcı bilgileri
        VBox userInfoBox = new VBox(5);
        userInfoBox.setAlignment(Pos.CENTER);
        userInfoBox.getChildren().addAll(avatarStack, usernameLabel, accountTypeLabel);

        // Ayırıcı
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Oyun istatistikleri
        Label statsTitle = new Label("İSTATİSTİKLER");
        statsTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        statsTitle.setTextFill(TEXT_SECONDARY);

        // Örnek istatistikler
        VBox statsBox = new VBox(15);

        HBox playTimeBox = createStatInfoItem("Oynama Süresi", "32 saat");
        HBox gamesBox = createStatInfoItem("Coin", "126");
        HBox sayginlikBox = createStatInfoItem("Saygınlık Seviyesi", "10");

        statsBox.getChildren().addAll(playTimeBox, gamesBox, sayginlikBox);

        // Ayırıcı
        Separator separator2 = new Separator();
        separator2.setPadding(new Insets(10, 0, 10, 0));

        // Arkadaşlar (Basit)
        Label friendsTitle = new Label("ARKADAŞLAR (3/5 ÇEVRİMİÇİ)");
        friendsTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        friendsTitle.setTextFill(TEXT_SECONDARY);

        // Örnek arkadaşlar
        VBox friendsBox = new VBox(10);

        HBox friend1 = createFriendItem("G3YIK", true);
        HBox friend2 = createFriendItem("AethriusMC", true);
        HBox friend3 = createFriendItem("YusufKGD", false);
        HBox friend4 = createFriendItem("CraftMaster", true);
        HBox friend5 = createFriendItem("YunusKGD", false);

        friendsBox.getChildren().addAll(friend1, friend2, friend3, friend4, friend5);

        // Paneli düzenle
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

    /**
     * Alt paneli (oyun başlatma kontrolleri) oluşturur - ESKİ SİSTEM KORUNDU
     */
    private HBox createBottomPanel() {
        HBox bottomPanel = new HBox();
        bottomPanel.setPadding(new Insets(15, 25, 15, 25));
        bottomPanel.setAlignment(Pos.CENTER_LEFT);
        bottomPanel.setSpacing(20);
        bottomPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        // Sistem tepsisine ikon ekle
        addSystemTrayIcon();

        playButton = createStyledButton("OYUNU BAŞLAT", 200, 35);
        playButton.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        playButton.setAlignment(Pos.CENTER);
        playButton.setTranslateX(-15);
        playButton.setTranslateY(-5);

        Label ramLabel = new Label("RAM:");
        ramLabel.setFont(Font.font(FONT_FAMILY, 14));
        ramLabel.setTextFill(TEXT_COLOR);

        ComboBox<String> ramComboBox = new ComboBox<>();
        List<String> ramOptions = Arrays.asList("2 GB", "4 GB", "6 GB", "8 GB", "12 GB", "16 GB");
        ramComboBox.getItems().addAll(ramOptions);
        ramComboBox.getSelectionModel().select(1); // 4GB default

        Label resolutionLabel = new Label("Çözünürlük:");
        resolutionLabel.setFont(Font.font(FONT_FAMILY, 14));
        resolutionLabel.setTextFill(TEXT_COLOR);

        ComboBox<String> resolutionComboBox = new ComboBox<>();
        List<String> resolutionOptions = Arrays.asList("1280x720", "1920x1080", "2560x1440", "3840x2160");
        resolutionComboBox.getItems().addAll(resolutionOptions);
        resolutionComboBox.getSelectionModel().select(1); // 1920x1080 default

        downloadProgress = new ProgressBar(0);
        downloadProgress.setPrefWidth(150);
        downloadProgress.setStyle("-fx-accent: " + toHexString(PRIMARY_COLOR) + ";");
        downloadProgress.setVisible(false);

        statusLabel = new Label("");
        statusLabel.setFont(Font.font(FONT_FAMILY, 14));
        statusLabel.setTextFill(TEXT_SECONDARY);
        statusLabel.setVisible(false);

        playButton.setOnAction(e -> launchGame());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox ramBox = new HBox(5);
        ramBox.setAlignment(Pos.CENTER_LEFT);
        ramBox.getChildren().addAll(ramLabel, ramComboBox);

        HBox resolutionBox = new HBox(5);
        resolutionBox.setAlignment(Pos.CENTER_LEFT);
        resolutionBox.getChildren().addAll(resolutionLabel, resolutionComboBox);

        bottomPanel.getChildren().addAll(
                playButton,
                new VBox(5, downloadProgress, statusLabel),
                spacer,
                ramBox,
                resolutionBox
        );

        return bottomPanel;
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

                // PNG dosyasını yükle
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

                // Fallback: Basit bir AWT görseli oluştur
                if (trayIconImage == null) {
                    System.out.println("Fallback görsel oluşturuluyor...");
                    trayIconImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = trayIconImage.createGraphics();
                    g.setColor(new java.awt.Color(0, 255, 0)); // Yeşil daire
                    g.fillOval(0, 0, 64, 64);
                    g.dispose();
                }

                TrayIcon trayIcon = new TrayIcon(trayIconImage, "TerraMonic Launcher");
                trayIcon.setImageAutoSize(true);

                // Tıklama için popup menü
                PopupMenu popup = new PopupMenu();
                MenuItem exitItem = new MenuItem("Çıkış");
                exitItem.addActionListener(e -> Platform.exit());
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
     * ESKİ SİSTEMDEN KORUNAN HELPER METODLARI
     */
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
                // Mods klasörü
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

                // Config klasörü
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
    }

    private void addToZip(Path filePath, String entryName, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        Files.copy(filePath, zos);
        zos.closeEntry();
    }

    private void loadModProfile(String profileName) {
        try {
            // Mevcut modları ve config'i temizle
            Path modsPath = TERRAMONIC_PATH.resolve("mods");
            Path configPath = TERRAMONIC_PATH.resolve("config");
            deleteDirectory(modsPath);
            deleteDirectory(configPath);
            Files.createDirectories(modsPath);
            Files.createDirectories(configPath);

            // Zip dosyasını çıkar
            Path zipPath = MODS_PROFILES_PATH.resolve(profileName);
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    Path destPath;

                    // Dosya yoluna göre hedef belirle
                    if (entryName.startsWith("mods/")) {
                        destPath = modsPath.resolve(entryName.substring("mods/".length()));
                    } else if (entryName.startsWith("config/")) {
                        destPath = configPath.resolve(entryName.substring("config/".length()));
                    } else {
                        continue; // Diğer dosyaları atla
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

    /**
     * OYUNU BAŞLATMA METODUPELLEGİ
     */
    private void launchGame() {
        // Basit bir placeholder - tam oyun başlatma sistemi burada olacak
        Platform.runLater(() -> {
            downloadProgress.setVisible(true);
            statusLabel.setVisible(true);
            statusLabel.setText("Oyun başlatılıyor...");
            playButton.setDisable(true);
            
            // 3 saniye sonra gizle
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                downloadProgress.setVisible(false);
                statusLabel.setVisible(false);
                playButton.setDisable(false);
                showInfo("Oyun başlatıldı! (Demo Mode)");
            }));
            timeline.play();
        });
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
            // Local file için
            Path filePath = Paths.get(url.substring(7));
            return Files.readString(filePath);
        } else {
            // HTTP/HTTPS URL için
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

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Bilgi");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void updateSystemIcons() {
        // System icon update implementation
        BufferedImage iconImage = null;

        try {
            if (Files.exists(ICON_FILE)) {
                iconImage = ImageIO.read(ICON_FILE.toFile());
            }
        } catch (IOException e) {
            System.out.println("PNG dosyası okunurken hata: " + ICON_FILE + ", Hata=" + e.getMessage());
        }

        // Fallback icon if PNG can't be loaded
        if (iconImage == null) {
            iconImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = iconImage.createGraphics();
            g.setColor(new java.awt.Color(0, 255, 0)); // Green circle
            g.fillOval(0, 0, 64, 64);
            g.setColor(java.awt.Color.BLACK);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
            g.drawString("T", 20, 40); // 'T' for TerraMonic
            g.dispose();
        }

        // Windows taskbar icon
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

        // System tray icon
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                TrayIcon trayIcon = new TrayIcon(iconImage, "TerraMonic Launcher");
                trayIcon.setImageAutoSize(true);

                // Tray menu
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

    // UI Helper methods
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

        // Focus effect
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

        // Shadow effect
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

        // Hover effects
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

        // Sol kısım - Launcher adı
        Label titleLabel = new Label("TerraMonic Launcher " + currentLauncherVersion);
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

        // Sağ kısım - Pencere kontrolleri
        HBox windowControls = new HBox(10);
        windowControls.setAlignment(Pos.CENTER_RIGHT);

        Button minimizeBtn = createWindowControlButton("—", "#555555");
        minimizeBtn.setOnAction(e -> mainStage.setIconified(true));

        Button closeBtn = createWindowControlButton("✕", "#FF3A3A");
        closeBtn.setOnAction(e -> {
            if (executorService != null) {
                executorService.shutdownNow();
            }
            Platform.exit();
        });

        windowControls.getChildren().addAll(minimizeBtn, closeBtn);
        titleBar.setRight(windowControls);

        // Sürükleme için mouse event'leri
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

        // Başlık
        Label title = new Label(news.getTitle());
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        title.setTextFill(TEXT_COLOR);

        // Tarih
        Label date = new Label(news.getDate());
        date.setFont(Font.font(FONT_FAMILY, 12));
        date.setTextFill(TEXT_SECONDARY);

        // İçerik
        Label content = new Label(news.getContent());
        content.setFont(Font.font(FONT_FAMILY, 14));
        content.setTextFill(TEXT_COLOR);
        content.setWrapText(true);

        // Tür etiketi
        Label typeLabel = new Label(news.getType().toString());
        typeLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        typeLabel.setTextFill(PRIMARY_COLOR);
        typeLabel.setStyle("-fx-background-color: #222222; -fx-padding: 5px 10px; -fx-background-radius: 5px;");

        card.getChildren().addAll(title, date, content, typeLabel);

        // Hover effect
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