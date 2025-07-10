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
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * TerraMonic Minecraft Launcher - Premium Version
 * Modrinth mod sistemi ve Fabric 1.21.5 desteği
 */
public class TerraMonicLauncher1 extends Application {

    // Sabitler
    private static final Color BACKGROUND_COLOR = Color.web("#000000");
    private static final Color BACKGROUND_SECONDARY = Color.web("#111111");
    private static final Color PRIMARY_COLOR = Color.web("#01a500");
    private static final Color PRIMARY_HIGHLIGHT = Color.web("#71ff61");
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color TEXT_SECONDARY = Color.web("#AAAAAA");
    private static final Color SHADOW_COLOR = Color.web("71ff61");
    
    private static final String FONT_FAMILY = "Gilroy Bold";
    private static final double BUTTON_RADIUS = 18.0;
    private static final String LAUNCHER_VERSION = "v1.0.2";
    private static final String MINECRAFT_VERSION = "1.21.5";
    private static final String FABRIC_VERSION = "0.16.14";
    private static final String TERRAMONIC_URL = "https://www.terramonic.com";
    private static final String ICON_URL = "https://www.dropbox.com/scl/fi/2yc75kqokrtivw202rt3w/icon.png?rlkey=1blmy791i17gs6t78ecjc3qxf&st=cjc590fc&dl=1";
    private static final String MODRINTH_PACK_URL = "https://cdn.modrinth.com/data/7LXGq7mG/versions/26fadAto/TerraMonic%20Enhanced%20Beta%20V1.0%201.0.0.mrpack";
    private static final String LAUNCHER_JSON_URL = "https://www.dropbox.com/scl/fi/zcty3rszkpxritcd6ip85/launcher.json?rlkey=byf0phf8xjy0j6nuuto1qohql&st=rcgtv35f&dl=1";
    
    private static final Path TERRAMONIC_PATH = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", ".terramonic");
    private static final Path LAUNCHER_VERSION_JSON = TERRAMONIC_PATH.resolve("launcher_version.json");
    private static final Path DELETED_MODS_FILE = TERRAMONIC_PATH.resolve("deleted_mods.json");
    private static final Path MODS_PROFILES_PATH = TERRAMONIC_PATH.resolve("mods_profiles");
    private static final Path ICON_PATH = TERRAMONIC_PATH.resolve("terramonic_icon");
    private static final Path ICON_FILE = ICON_PATH.resolve("terramonic_icon.png");

    // Değişkenler
    private String currentLauncherVersion = "v1.0.2";
    private String playerName = "";
    private boolean rememberUser = false;
    private double windowWidth = 1100;
    private double windowHeight = 700;
    private double xOffset, yOffset;
    
    private List<NewsItem> newsList = new ArrayList<>();
    private Button playButton;
    private ProgressBar downloadProgress;
    private final Label statusLabel = new Label("");
    private ExecutorService executorService;
    private ComboBox<String> ramCombo;
    private ComboBox<String> resCombo;
    private boolean gameIsLaunching = false;
    
    private Stage mainStage;
    private Scene currentScene;
    private Image launcherIcon;
    private JSONObject launcherConfig;
    private final javafx.beans.property.BooleanProperty modsReady = new javafx.beans.property.SimpleBooleanProperty(false);
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
        
        executorService = Executors.newFixedThreadPool(4);
        setupTerramonicFolder();
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

    private void setupTerramonicFolder() {
        try {
            Files.createDirectories(TERRAMONIC_PATH);
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
     * YENİ: MODRINTH PACK İNDİRME VE KURULUM
     */
    private void downloadAndInstallModrinthPack() {
        try {
            Platform.runLater(() -> statusLabel.setText("Modrinth pack indiriliyor..."));
            
            Path mrpackPath = Files.createTempFile("terramonic_pack", ".mrpack");
            downloadFile(MODRINTH_PACK_URL, mrpackPath);
            
            Platform.runLater(() -> statusLabel.setText("Modrinth pack çıkarılıyor..."));
            
            Path tempExtractDir = Files.createTempDirectory("terramonic_extract");
            extractZip(mrpackPath, tempExtractDir);
            
            // modrinth.index.json'u oku
            Path indexPath = tempExtractDir.resolve("modrinth.index.json");
            if (!Files.exists(indexPath)) {
                System.out.println("⚠️ modrinth.index.json bulunamadı!");
                return;
            }
            
            String indexContent = Files.readString(indexPath);
            JSONObject indexJson = new JSONObject(indexContent);
            JSONArray files = indexJson.getJSONArray("files");
            
            Path modsDir = TERRAMONIC_PATH.resolve("mods");
            Files.createDirectories(modsDir);
            
            Set<String> deletedMods = loadDeletedModsList();
            
            int totalMods = 0;
            int downloadedMods = 0;
            
            // Toplam mod sayısını hesapla
            for (int i = 0; i < files.length(); i++) {
                JSONObject fileObj = files.getJSONObject(i);
                String filePath = fileObj.getString("path");
                if (filePath.startsWith("mods/")) {
                    totalMods++;
                }
            }
            
            // Modları indir
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
                                final int currentMod = ++downloadedMods;
                                Platform.runLater(() -> statusLabel.setText("Mod indiriliyor (" + currentMod + "/" + totalMods + "): " + fileName));
                                downloadFile(downloadUrl, targetPath);
                            }
                        }
                    } else {
                        downloadedMods++;
                    }
                }
            }
            
            // Overrides klasörünü kontrol et ve kopyala
            Platform.runLater(() -> statusLabel.setText("Config dosyaları kopyalanıyor..."));
            Path overridesPath = tempExtractDir.resolve("overrides");
            if (Files.exists(overridesPath)) {
                Path sourceConfig = overridesPath.resolve("config");
                Path targetConfig = TERRAMONIC_PATH.resolve("config");
                if (Files.exists(sourceConfig)) {
                    Files.createDirectories(targetConfig);
                    copyDirectory(sourceConfig, targetConfig);
                }
                
                Path sourceMods = overridesPath.resolve("mods");
                if (Files.exists(sourceMods)) {
                    copyDirectory(sourceMods, modsDir);
                }
            }
            
            // Temizlik
            deleteDirectory(tempExtractDir);
            Files.deleteIfExists(mrpackPath);
            
            Platform.runLater(() -> {
                modsReady.set(true);
                statusLabel.setText("Modlar hazırlandı!");
                System.out.println("✅ Modrinth pack kurulumu tamamlandı!");
            });
            
        } catch (Exception e) {
            Platform.runLater(() -> showError("Modrinth pack kurulumu başarısız: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * YENİ: OYUN BAŞLATMA METODU
     */
    private void launchGame() {
        if (gameIsLaunching) {
            showError("Oyun zaten başlatılıyor!");
            return;
        }
        
        if (playerName.isEmpty()) {
            showError("Lütfen önce giriş yapın!");
            return;
        }
        
        gameIsLaunching = true;
        playButton.setDisabled(true);
        downloadProgress.setVisible(true);
        statusLabel.setVisible(true);
        
        Task<Void> launchTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Platform.runLater(() -> {
                        downloadProgress.setProgress(0.1);
                        statusLabel.setText("Minecraft başlatılıyor...");
                    });
                    
                    String ramSetting = ramCombo.getSelectionModel().getSelectedItem();
                    int ramAmount = Integer.parseInt(ramSetting.split(" ")[0]);
                    
                    String resolution = resCombo.getSelectionModel().getSelectedItem();
                    String[] resParts = resolution.split("x");
                    int width = Integer.parseInt(resParts[0]);
                    int height = Integer.parseInt(resParts[1]);
                    
                    Platform.runLater(() -> {
                        downloadProgress.setProgress(0.3);
                        statusLabel.setText("Kütüphaneler kontrol ediliyor...");
                    });
                    
                    MinecraftFabricLauncher fabricLauncher = new MinecraftFabricLauncher();
                    fabricLauncher.setProgressCallback((progress, message) -> {
                        Platform.runLater(() -> {
                            downloadProgress.setProgress(progress);
                            statusLabel.setText(message);
                        });
                    });
                    
                    boolean success = fabricLauncher.launchMinecraft(
                        playerName,
                        TERRAMONIC_PATH,
                        ramAmount,
                        width,
                        height
                    );
                    
                    if (success) {
                        Platform.runLater(() -> {
                            downloadProgress.setProgress(1.0);
                            statusLabel.setText("Minecraft başarıyla başlatıldı!");
                            
                            Timeline closeTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                                System.exit(0); // Launcher'ı tamamen kapat
                            }));
                            closeTimeline.play();
                        });
                    } else {
                        throw new Exception("Minecraft başlatılamadı!");
                    }
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        gameIsLaunching = false;
                        playButton.setDisabled(false);
                        downloadProgress.setVisible(false);
                        showError("Oyun başlatılamadı: " + e.getMessage());
                    });
                    throw e;
                }
                
                return null;
            }
        };
        
        launchTask.setOnFailed(e -> {
            gameIsLaunching = false;
            playButton.setDisabled(false);
            downloadProgress.setVisible(false);
            Throwable exception = launchTask.getException();
            showError("Oyun başlatılamadı: " + exception.getMessage());
        });
        
        executorService.submit(launchTask);
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                System.out.println("Dosya kopyalanamadı: " + sourcePath + " -> " + e.getMessage());
            }
        });
    }

    private void loadIconAndStart() {
        Task<Void> iconTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                boolean pngSuccess = downloadIconFile(ICON_URL, ICON_FILE);
                if (pngSuccess) {
                    launcherIcon = new Image(ICON_FILE.toUri().toString(), true);
                }

                try {
                    System.out.println("🔄 JSON yükleniyor: " + LAUNCHER_JSON_URL);
                    String cfg = readJsonFromUrl(LAUNCHER_JSON_URL);
                    launcherConfig = new JSONObject(cfg);
                    loadNewsFromConfig();
                } catch (Exception ex) {
                    System.out.println("❌ JSON yüklenemedi: " + ex.getMessage());
                }

                return null;
            }
        };

        iconTask.setOnSucceeded(e -> {
            if (launcherIcon != null) {
                mainStage.getIcons().clear();
                mainStage.getIcons().add(launcherIcon);
            }
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

    private void showSplashScreen() {
        Task<Void> setupTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> statusLabel.setText("Modrinth modları indiriliyor..."));
                downloadAndInstallModrinthPack();
                return null;
            }
        };
        
        setupTask.setOnSucceeded(e -> {
            showLoginScreen();
        });
        
        executorService.submit(setupTask);
    }

    private void showLoginScreen() {
        // Login screen UI burada oluşturulur
        Platform.runLater(() -> {
            transitionToMainScreen();
        });
    }

    private void transitionToMainScreen() {
        // Ana ekran UI burada oluşturulur
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        // Bottom panel - Play button
        HBox bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);

        Scene mainScene = new Scene(root, windowWidth, windowHeight);
        mainScene.setFill(BACKGROUND_COLOR);
        transitionToScene(mainScene);
    }

    private HBox createBottomPanel() {
        HBox bottomPanel = new HBox();
        bottomPanel.setPadding(new Insets(15, 25, 15, 25));
        bottomPanel.setAlignment(Pos.CENTER_LEFT);
        bottomPanel.setSpacing(20);
        bottomPanel.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");

        playButton = createStyledButton("🚀 OYUNU BAŞLAT", 220, 45);
        playButton.setOnAction(e -> launchGame());

        downloadProgress = new ProgressBar(0);
        downloadProgress.setPrefWidth(200);
        downloadProgress.setStyle("-fx-accent: " + toHexString(PRIMARY_COLOR) + ";");
        downloadProgress.setVisible(false);

        statusLabel.setFont(Font.font(FONT_FAMILY, 14));
        statusLabel.setTextFill(TEXT_SECONDARY);
        statusLabel.setVisible(false);

        // RAM ve resolution combos
        setupGameSettings();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bottomPanel.getChildren().addAll(
                playButton,
                new VBox(5, downloadProgress, statusLabel),
                spacer,
                new VBox(5, new Label("RAM:"), ramCombo),
                new VBox(5, new Label("Çözünürlük:"), resCombo)
        );

        return bottomPanel;
    }

    private void setupGameSettings() {
        // RAM settings
        long totalMem = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
        long gb = 1024L * 1024L * 1024L;
        long maxRamGb = Math.max(2, Math.min(16, totalMem / gb / 2));

        List<String> ramOptions = new ArrayList<>();
        for (int g = 2; g <= maxRamGb; g += (g >= 8 ? 4 : 2)) {
            ramOptions.add(g + " GB");
        }

        ramCombo = new ComboBox<>();
        ramCombo.getItems().addAll(ramOptions);
        ramCombo.getSelectionModel().select(Math.min(1, ramOptions.size() - 1));
        ramCombo.setPrefWidth(100);

        // Resolution settings
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
        resCombo.setPrefWidth(120);
    }

    // Utility methods
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

    private boolean downloadIconFile(String url, Path targetPath) {
        try {
            Files.createDirectories(targetPath.getParent());
            if (Files.exists(targetPath)) {
                return true;
            }
            
            URL downloadUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (InputStream in = connection.getInputStream();
                 OutputStream out = Files.newOutputStream(targetPath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return true;
        } catch (IOException e) {
            System.out.println("PNG indirme hatası: " + e.getMessage());
            return false;
        }
    }

    private String readJsonFromUrl(String url) throws IOException {
        URL jsonUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) jsonUrl.openConnection();
        connection.setRequestMethod("GET");
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
    }

    private void updateSystemIcons() {
        BufferedImage iconImage = null;
        try {
            if (Files.exists(ICON_FILE)) {
                iconImage = ImageIO.read(ICON_FILE.toFile());
            }
        } catch (IOException e) {
            System.out.println("PNG dosyası okunurken hata: " + e.getMessage());
        }

        if (iconImage == null) {
            iconImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = iconImage.createGraphics();
            g.setColor(new java.awt.Color(0, 255, 0));
            g.fillOval(0, 0, 64, 64);
            g.dispose();
        }

        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                try {
                    taskbar.setIconImage(iconImage);
                } catch (Exception e) {
                    System.out.println("Görev çubuğu ikonu güncellenemedi: " + e.getMessage());
                }
            }
        }
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
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: " + toHexString(PRIMARY_COLOR) + ";" +
                    "-fx-text-fill: black;" +
                    "-fx-background-radius: " + BUTTON_RADIUS + "px;" +
                    "-fx-cursor: hand;"
            );
        });

        return button;
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
            }
        });
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
        }
    }

    // News item classes
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

        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getDate() { return date; }
        public NewsItemType getType() { return type; }
    }

    private enum NewsItemType {
        GÜNCELLEME, GENEL, ETKİNLİK, YOUTUBE, TANITIM, YAYIN, DİSCORD
    }
}

/**
 * YENİ: MİNECRAFT FABRİC LAUNCHER SINIFI
 */
class MinecraftFabricLauncher {
    private ProgressCallback progressCallback;
    
    public interface ProgressCallback {
        void updateProgress(double progress, String message);
    }
    
    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }
    
    public boolean launchMinecraft(String playerName, Path terramonicPath, int ramGb, int width, int height) {
        try {
            updateProgress(0.4, "Minecraft JAR kontrol ediliyor...");
            Path minecraftJar = downloadMinecraftJar(terramonicPath);
            
            updateProgress(0.6, "Fabric kütüphaneleri kontrol ediliyor...");
            setupFabricLibraries(terramonicPath);
            
            updateProgress(0.8, "Oyun başlatılıyor...");
            ProcessBuilder pb = createMinecraftProcess(playerName, terramonicPath, minecraftJar, ramGb, width, height);
            Process process = pb.start();
            
            updateProgress(1.0, "Minecraft başarıyla başlatıldı!");
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void updateProgress(double progress, String message) {
        if (progressCallback != null) {
            progressCallback.updateProgress(progress, message);
        }
    }
    
    private Path downloadMinecraftJar(Path terramonicPath) throws IOException {
        Path jarPath = terramonicPath.resolve("jar").resolve("minecraft-1.21.5.jar");
        if (!Files.exists(jarPath)) {
            Files.createDirectories(jarPath.getParent());
        }
        return jarPath;
    }
    
    private void setupFabricLibraries(Path terramonicPath) throws IOException {
        Path librariesPath = terramonicPath.resolve("libraries");
        Files.createDirectories(librariesPath);
    }
    
    private ProcessBuilder createMinecraftProcess(String playerName, Path terramonicPath, Path minecraftJar, int ramGb, int width, int height) {
        List<String> command = new ArrayList<>();
        
        command.add(System.getProperty("java.home") + "/bin/java");
        command.add("-Xmx" + ramGb + "G");
        command.add("-Xms" + ramGb + "G");
        command.add("-XX:+UnlockExperimentalVMOptions");
        command.add("-XX:+UseG1GC");
        command.add("-Djava.library.path=" + terramonicPath.resolve("natives").toString());
        
        command.add("-cp");
        String classpath = buildClasspath(terramonicPath, minecraftJar);
        command.add(classpath);
        
        command.add("net.fabricmc.loader.impl.launch.knot.KnotClient");
        
        command.add("--username");
        command.add(playerName);
        command.add("--version");
        command.add("1.21.5");
        command.add("--gameDir");
        command.add(terramonicPath.toString());
        command.add("--assetsDir");
        command.add(terramonicPath.resolve("assets").toString());
        command.add("--assetIndex");
        command.add("1.21.5");
        command.add("--uuid");
        command.add(UUID.randomUUID().toString());
        command.add("--accessToken");
        command.add("0");
        command.add("--userType");
        command.add("legacy");
        command.add("--width");
        command.add(String.valueOf(width));
        command.add("--height");
        command.add(String.valueOf(height));
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(terramonicPath.toFile());
        return pb;
    }
    
    private String buildClasspath(Path terramonicPath, Path minecraftJar) {
        StringBuilder classpath = new StringBuilder();
        classpath.append(minecraftJar.toString());
        
        try {
            Files.walk(terramonicPath.resolve("libraries"))
                .filter(path -> path.toString().endsWith(".jar"))
                .forEach(path -> {
                    classpath.append(System.getProperty("path.separator"));
                    classpath.append(path.toString());
                });
                
            Files.walk(terramonicPath.resolve("mods"))
                .filter(path -> path.toString().endsWith(".jar"))
                .forEach(path -> {
                    classpath.append(System.getProperty("path.separator"));
                    classpath.append(path.toString());
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return classpath.toString();
    }
}