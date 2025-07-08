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
    private static final String FABRIC_LOADER_VERSION = "0.16.9";
    
    // Ana değişkenler
    private Stage primaryStage;
    private BorderPane mainLayout;
    private StackPane centerPane;
    private VBox sidebarPane;
    private HBox topBarPane;
    private GridPane contentGrid;
    
    // Launcher durumu
    private boolean isInstalling = false;
    private boolean isLaunching = false;
    private double downloadProgress = 0.0;
    private String currentStatus = "Hazır";
    
    // Minecraft dizinleri
    private Path minecraftDir;
    private Path launcherDir;
    private Path modsDir;
    private Path versionsDir;
    
    // UI Bileşenleri
    private Button playButton;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Label versionLabel;
    private TextField usernameField;
    
    // İş Parçacıkları
    private ExecutorService executorService;
    
    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        this.executorService = Executors.newFixedThreadPool(4);
        
        initializeDirectories();
        downloadFont();
        setupUI();
        configureStage();
        
        stage.show();
        
        // Başlangıç animasyonu
        playEntranceAnimation();
    }
    
    private void initializeDirectories() {
        try {
            // Minecraft dizinini belirle (OS'ye göre)
            String os = System.getProperty("os.name").toLowerCase();
            String userHome = System.getProperty("user.home");
            
            if (os.contains("win")) {
                minecraftDir = Paths.get(System.getenv("APPDATA"), ".minecraft");
            } else if (os.contains("mac")) {
                minecraftDir = Paths.get(userHome, "Library", "Application Support", "minecraft");
            } else {
                minecraftDir = Paths.get(userHome, ".minecraft");
            }
            
            // Launcher dizinleri
            launcherDir = minecraftDir.resolve("terramonic");
            modsDir = minecraftDir.resolve("mods");
            versionsDir = minecraftDir.resolve("versions");
            
            // Dizinleri oluştur
            Files.createDirectories(launcherDir);
            Files.createDirectories(modsDir);
            Files.createDirectories(versionsDir);
            Files.createDirectories(minecraftDir.resolve("profiles"));
            
            System.out.println("Minecraft dizini: " + minecraftDir);
            System.out.println("Launcher dizini: " + launcherDir);
            
        } catch (IOException e) {
            System.err.println("Dizinler oluşturulurken hata: " + e.getMessage());
        }
    }
    
    private void downloadFont() {
        executorService.submit(() -> {
            try {
                Path fontPath = launcherDir.resolve("Gilroy-Bold.otf");
                
                if (!Files.exists(fontPath)) {
                    System.out.println("Gilroy font indiriliyor...");
                    
                    URL fontUrl = new URL(GILROY_FONT_URL);
                    HttpURLConnection connection = (HttpURLConnection) fontUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "TerraMonic-Launcher/1.0");
                    
                    try (InputStream inputStream = connection.getInputStream();
                         FileOutputStream outputStream = new FileOutputStream(fontPath.toFile())) {
                        
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    System.out.println("Font başarıyla indirildi: " + fontPath);
                }
                
                // Font'u JavaFX'e yükle
                Platform.runLater(() -> {
                    try {
                        Font.loadFont(fontPath.toUri().toString(), 12);
                        System.out.println("Font JavaFX'e yüklendi");
                    } catch (Exception e) {
                        System.err.println("Font yüklenirken hata: " + e.getMessage());
                    }
                });
                
            } catch (Exception e) {
                System.err.println("Font indirme hatası: " + e.getMessage());
            }
        });
    }
    
    private void setupUI() {
        createMainLayout();
        createTopBar();
        createSidebar();
        createCenterContent();
        
        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.setFill(BACKGROUND_COLOR);
        primaryStage.setScene(scene);
    }
    
    private void createMainLayout() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + toHexString(BACKGROUND_COLOR) + ";");
    }
    
    private void createTopBar() {
        topBarPane = new HBox();
        topBarPane.setPadding(new Insets(15, 20, 15, 20));
        topBarPane.setAlignment(Pos.CENTER_LEFT);
        topBarPane.setSpacing(20);
        
        // Logo ve başlık
        Label titleLabel = new Label("TerraMonic Launcher");
        titleLabel.setTextFill(TEXT_COLOR);
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        
        // Sürüm etiketi
        versionLabel = new Label(LAUNCHER_VERSION);
        versionLabel.setTextFill(TEXT_SECONDARY);
        versionLabel.setFont(Font.font(FONT_FAMILY, 14));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Kullanıcı adı alanı
        usernameField = new TextField();
        usernameField.setPromptText("Minecraft Kullanıcı Adı");
        usernameField.setPrefWidth(200);
        styleTextField(usernameField);
        
        topBarPane.getChildren().addAll(titleLabel, versionLabel, spacer, usernameField);
        mainLayout.setTop(topBarPane);
    }
    
    private void createSidebar() {
        sidebarPane = new VBox();
        sidebarPane.setPrefWidth(250);
        sidebarPane.setPadding(new Insets(20));
        sidebarPane.setSpacing(15);
        sidebarPane.setStyle("-fx-background-color: " + toHexString(BACKGROUND_SECONDARY) + ";");
        
        // Oynat butonu
        playButton = createStyledButton("OYUNU BAŞLAT", 220, 50);
        playButton.setOnAction(e -> launchMinecraft());
        
        // Durum etiketi
        statusLabel = new Label(currentStatus);
        statusLabel.setTextFill(TEXT_COLOR);
        statusLabel.setFont(Font.font(FONT_FAMILY, 16));
        
        // İlerleme çubuğu
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(220);
        progressBar.setPrefHeight(8);
        styleProgressBar(progressBar);
        
        sidebarPane.getChildren().addAll(playButton, statusLabel, progressBar);
        mainLayout.setLeft(sidebarPane);
    }
    
    private void createCenterContent() {
        centerPane = new StackPane();
        centerPane.setPadding(new Insets(20));
        
        // TODO: Add mod browser, settings, news, etc.
        Label placeholderLabel = new Label("Mod Tarayıcısı ve Ayarlar Gelecek");
        placeholderLabel.setTextFill(TEXT_COLOR);
        placeholderLabel.setFont(Font.font(FONT_FAMILY, 18));
        
        centerPane.getChildren().add(placeholderLabel);
        mainLayout.setCenter(centerPane);
    }
    
    private void configureStage() {
        primaryStage.setTitle("TerraMonic Launcher " + LAUNCHER_VERSION);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.centerOnScreen();
        
        // Çıkış işlemi
        primaryStage.setOnCloseRequest(e -> {
            if (executorService != null) {
                executorService.shutdown();
            }
            Platform.exit();
        });
    }
    
    private void playEntranceAnimation() {
        // TODO: Implement entrance animation
    }
    
    private Button createStyledButton(String text, double width, double height) {
        Button button = new Button(text);
        button.setPrefSize(width, height);
        button.setTextFill(TEXT_COLOR);
        button.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        
        // Gradient arka plan
        String style = String.format(
            "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
            "-fx-background-radius: %f; " +
            "-fx-border-radius: %f; " +
            "-fx-cursor: hand;",
            toHexString(PRIMARY_COLOR), toHexString(PRIMARY_DARK), BUTTON_RADIUS, BUTTON_RADIUS
        );
        
        button.setStyle(style);
        
        // Hover efekti
        button.setOnMouseEntered(e -> {
            String hoverStyle = String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
                "-fx-background-radius: %f; " +
                "-fx-border-radius: %f; " +
                "-fx-cursor: hand;",
                toHexString(PRIMARY_HIGHLIGHT), toHexString(PRIMARY_COLOR), BUTTON_RADIUS, BUTTON_RADIUS
            );
            button.setStyle(hoverStyle);
        });
        
        button.setOnMouseExited(e -> button.setStyle(style));
        
        return button;
    }
    
    private void styleTextField(TextField textField) {
        String style = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: %s; " +
            "-fx-prompt-text-fill: %s; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-border-color: %s; " +
            "-fx-border-width: 1; " +
            "-fx-padding: 8;",
            toHexString(BACKGROUND_SECONDARY), toHexString(TEXT_COLOR), 
            toHexString(TEXT_SECONDARY), toHexString(PRIMARY_COLOR)
        );
        textField.setStyle(style);
    }
    
    private void styleProgressBar(ProgressBar progressBar) {
        String style = String.format(
            "-fx-accent: %s; " +
            "-fx-background-color: %s; " +
            "-fx-background-radius: 4; " +
            "-fx-background-insets: 0;",
            toHexString(PRIMARY_COLOR), toHexString(BACKGROUND_SECONDARY)
        );
        progressBar.setStyle(style);
    }
    
    private void launchMinecraft() {
        if (isLaunching || isInstalling) {
            return;
        }
        
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError("Lütfen kullanıcı adınızı girin!");
            return;
        }
        
        isLaunching = true;
        playButton.setDisable(true);
        updateStatus("Minecraft hazırlanıyor...");
        
        executorService.submit(() -> {
            try {
                // Minecraft ve Fabric kurulumunu kontrol et
                if (!checkMinecraftInstallation()) {
                    Platform.runLater(() -> updateStatus("Minecraft indiriliyor..."));
                    downloadMinecraft();
                }
                
                if (!checkFabricInstallation()) {
                    Platform.runLater(() -> updateStatus("Fabric Loader kuruluyor..."));
                    installFabric();
                }
                
                // Profil oluştur/güncelle
                Platform.runLater(() -> updateStatus("Profil hazırlanıyor..."));
                createLauncherProfile();
                
                // Java argumentları hazırla
                List<String> jvmArgs = prepareJvmArguments();
                List<String> gameArgs = prepareGameArguments(username);
                
                // Minecraft'ı başlat
                Platform.runLater(() -> updateStatus("Minecraft başlatılıyor..."));
                launchMinecraftProcess(jvmArgs, gameArgs);
                
                Platform.runLater(() -> {
                    isLaunching = false;
                    playButton.setDisable(false);
                    updateStatus("Minecraft başlatıldı!");
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    isLaunching = false;
                    playButton.setDisable(false);
                    updateStatus("Hata: " + e.getMessage());
                    showError("Minecraft başlatılırken hata oluştu: " + e.getMessage());
                });
            }
        });
    }
    
    private void updateStatus(String status) {
        currentStatus = status;
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String toHexString(Color color) {
        return String.format("#%02x%02x%02x", 
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
    
    private boolean checkMinecraftInstallation() {
        Path versionDir = versionsDir.resolve(MINECRAFT_VERSION);
        Path jarFile = versionDir.resolve(MINECRAFT_VERSION + ".jar");
        Path jsonFile = versionDir.resolve(MINECRAFT_VERSION + ".json");
        
        return Files.exists(jarFile) && Files.exists(jsonFile);
    }
    
    private boolean checkFabricInstallation() {
        String fabricVersion = "fabric-loader-" + FABRIC_LOADER_VERSION + "-" + MINECRAFT_VERSION;
        Path fabricDir = versionsDir.resolve(fabricVersion);
        Path fabricJar = fabricDir.resolve(fabricVersion + ".jar");
        Path fabricJson = fabricDir.resolve(fabricVersion + ".json");
        
        return Files.exists(fabricJar) && Files.exists(fabricJson);
    }
    
    private void downloadMinecraft() throws Exception {
        // Minecraft manifest'ini indir
        String manifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
        JSONObject manifest = downloadJson(manifestUrl);
        
        // İstenen sürümü bul
        JSONArray versions = manifest.getJSONArray("versions");
        String versionUrl = null;
        
        for (int i = 0; i < versions.length(); i++) {
            JSONObject version = versions.getJSONObject(i);
            if (version.getString("id").equals(MINECRAFT_VERSION)) {
                versionUrl = version.getString("url");
                break;
            }
        }
        
        if (versionUrl == null) {
            throw new RuntimeException("Minecraft sürümü bulunamadı: " + MINECRAFT_VERSION);
        }
        
        // Sürüm bilgilerini indir
        JSONObject versionData = downloadJson(versionUrl);
        
        // Sürüm dizinini oluştur
        Path versionDir = versionsDir.resolve(MINECRAFT_VERSION);
        Files.createDirectories(versionDir);
        
        // JAR dosyasını indir
        JSONObject downloads = versionData.getJSONObject("downloads");
        JSONObject client = downloads.getJSONObject("client");
        String jarUrl = client.getString("url");
        
        Path jarFile = versionDir.resolve(MINECRAFT_VERSION + ".jar");
        downloadFile(jarUrl, jarFile);
        
        // JSON dosyasını kaydet
        Path jsonFile = versionDir.resolve(MINECRAFT_VERSION + ".json");
        Files.write(jsonFile, versionData.toString(2).getBytes());
        
        System.out.println("Minecraft " + MINECRAFT_VERSION + " başarıyla indirildi");
    }
    
    private void installFabric() throws Exception {
        String fabricVersion = "fabric-loader-" + FABRIC_LOADER_VERSION + "-" + MINECRAFT_VERSION;
        
        // Fabric meta API'sinden bilgileri al
        String fabricMetaUrl = "https://meta.fabricmc.net/v2/versions/loader/" + MINECRAFT_VERSION + "/" + FABRIC_LOADER_VERSION;
        JSONArray fabricData = new JSONArray(downloadString(fabricMetaUrl));
        
        if (fabricData.length() == 0) {
            throw new RuntimeException("Fabric Loader bilgileri alınamadı");
        }
        
        JSONObject fabricInfo = fabricData.getJSONObject(0);
        
        // Fabric dizinini oluştur
        Path fabricDir = versionsDir.resolve(fabricVersion);
        Files.createDirectories(fabricDir);
        
        // Fabric profil JSON'unu oluştur
        JSONObject fabricProfile = createFabricProfile(fabricInfo, fabricVersion);
        
        Path fabricJson = fabricDir.resolve(fabricVersion + ".json");
        Files.write(fabricJson, fabricProfile.toString(2).getBytes());
        
        System.out.println("Fabric Loader " + FABRIC_LOADER_VERSION + " başarıyla kuruldu");
    }
    
    private JSONObject createFabricProfile(JSONObject fabricInfo, String fabricVersion) {
        JSONObject profile = new JSONObject();
        profile.put("id", fabricVersion);
        profile.put("inheritsFrom", MINECRAFT_VERSION);
        profile.put("type", "release");
        profile.put("time", "2024-01-01T00:00:00+00:00");
        profile.put("releaseTime", "2024-01-01T00:00:00+00:00");
        
        // Libraries ekle
        JSONArray libraries = new JSONArray();
        
        // Fabric loader library
        JSONObject loaderLib = new JSONObject();
        JSONObject loaderMaven = fabricInfo.getJSONObject("loader");
        loaderLib.put("name", loaderMaven.getString("maven"));
        loaderLib.put("url", "https://maven.fabricmc.net/");
        libraries.put(loaderLib);
        
        // Intermediary library
        JSONObject intermediaryLib = new JSONObject();
        JSONObject intermediaryMaven = fabricInfo.getJSONObject("intermediary");
        intermediaryLib.put("name", intermediaryMaven.getString("maven"));
        intermediaryLib.put("url", "https://maven.fabricmc.net/");
        libraries.put(intermediaryLib);
        
        profile.put("libraries", libraries);
        
        // Main class
        profile.put("mainClass", "net.fabricmc.loader.impl.launch.knot.KnotClient");
        
        return profile;
    }
    
    private void createLauncherProfile() throws Exception {
        Path profilesPath = minecraftDir.resolve("launcher_profiles.json");
        JSONObject profiles;
        
        if (Files.exists(profilesPath)) {
            String content = Files.readString(profilesPath);
            profiles = new JSONObject(content);
        } else {
            profiles = new JSONObject();
            profiles.put("profiles", new JSONObject());
            profiles.put("version", 3);
        }
        
        // TerraMonic profilini ekle
        JSONObject terramonicProfile = new JSONObject();
        terramonicProfile.put("name", "TerraMonic");
        terramonicProfile.put("type", "custom");
        terramonicProfile.put("created", "2024-01-01T00:00:00.000Z");
        terramonicProfile.put("lastUsed", "2024-01-01T00:00:00.000Z");
        terramonicProfile.put("lastVersionId", "fabric-loader-" + FABRIC_LOADER_VERSION + "-" + MINECRAFT_VERSION);
        terramonicProfile.put("gameDir", minecraftDir.toString());
        terramonicProfile.put("javaArgs", "-Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC");
        
        profiles.getJSONObject("profiles").put("terramonic", terramonicProfile);
        
        Files.write(profilesPath, profiles.toString(2).getBytes());
    }
    
    private List<String> prepareJvmArguments() {
        List<String> args = new ArrayList<>();
        
        // Java executable
        String javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            javaPath += ".exe";
        }
        args.add(javaPath);
        
        // JVM arguments
        args.add("-Xmx4G");
        args.add("-Xms1G");
        args.add("-XX:+UnlockExperimentalVMOptions");
        args.add("-XX:+UseG1GC");
        args.add("-XX:G1NewSizePercent=20");
        args.add("-XX:G1ReservePercent=20");
        args.add("-XX:MaxGCPauseMillis=50");
        args.add("-XX:G1HeapRegionSize=32M");
        
        // Classpath
        args.add("-cp");
        args.add(buildClasspath());
        
        return args;
    }
    
    private String buildClasspath() {
        StringBuilder classpath = new StringBuilder();
        String fabricVersion = "fabric-loader-" + FABRIC_LOADER_VERSION + "-" + MINECRAFT_VERSION;
        
        // Ana JAR dosyası
        classpath.append(versionsDir.resolve(MINECRAFT_VERSION).resolve(MINECRAFT_VERSION + ".jar"));
        
        // TODO: Library'leri ekle (Fabric ve bağımlılıkları)
        
        return classpath.toString();
    }
    
    private List<String> prepareGameArguments(String username) {
        List<String> args = new ArrayList<>();
        
        // Main class
        args.add("net.fabricmc.loader.impl.launch.knot.KnotClient");
        
        // Game arguments
        args.add("--username");
        args.add(username);
        args.add("--version");
        args.add("fabric-loader-" + FABRIC_LOADER_VERSION + "-" + MINECRAFT_VERSION);
        args.add("--gameDir");
        args.add(minecraftDir.toString());
        args.add("--assetsDir");
        args.add(minecraftDir.resolve("assets").toString());
        args.add("--assetIndex");
        args.add(MINECRAFT_VERSION);
        args.add("--uuid");
        args.add(generateOfflineUUID(username));
        args.add("--accessToken");
        args.add("0");
        args.add("--userType");
        args.add("legacy");
        args.add("--versionType");
        args.add("TerraMonic");
        
        return args;
    }
    
    private void launchMinecraftProcess(List<String> jvmArgs, List<String> gameArgs) throws Exception {
        List<String> command = new ArrayList<>();
        command.addAll(jvmArgs);
        command.addAll(gameArgs);
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(minecraftDir.toFile());
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        // Process output'unu logla
        executorService.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Minecraft] " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    private String generateOfflineUUID(String username) {
        return java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes()).toString();
    }
    
    private JSONObject downloadJson(String url) throws Exception {
        String content = downloadString(url);
        return new JSONObject(content);
    }
    
    private String downloadString(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "TerraMonic-Launcher/1.0");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }
    
    private void downloadFile(String urlString, Path targetPath) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "TerraMonic-Launcher/1.0");
        
        long totalSize = connection.getContentLengthLong();
        long downloadedSize = 0;
        
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(targetPath.toFile())) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                downloadedSize += bytesRead;
                
                // Progress güncelle
                if (totalSize > 0) {
                    double progress = (double) downloadedSize / totalSize;
                    Platform.runLater(() -> {
                        progressBar.setProgress(progress);
                        downloadProgress = progress;
                    });
                }
            }
        }
        
        Platform.runLater(() -> progressBar.setProgress(0));
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}