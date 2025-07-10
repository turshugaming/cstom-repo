package com.terramonic;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TerraMonicLauncher extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final String APPDATA = System.getenv("APPDATA") + "/.terramonic";
    private static final String MODPACK_URL = "https://cdn.modrinth.com/data/7LXGq7mG/versions/26fadAto/TerraMonic%20Enhanced%20Beta%20V1.0%201.0.0.mrpack";
    
    private Stage primaryStage;
    private StackPane root;
    private ImageView backgroundImageView;
    private TextField nameField;
    private PasswordField passwordField;
    private Slider ramSlider;
    private Label statusLabel;
    private VBox modManagerBox;
    private Font customFont;
    private Gson gson = new Gson();
    private HttpClient httpClient = HttpClient.newHttpClient();
    private MinecraftFabricLauncher fabricLauncher;
    
    // Maintenance mode
    private boolean maintenanceMode = false;
    private String maintenanceReason = "";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        loadResources();
        showSplashScreen();
    }

    private void loadResources() {
        try {
            // Create directories
            Files.createDirectories(Paths.get(APPDATA));
            Files.createDirectories(Paths.get(APPDATA + "/mods"));
            Files.createDirectories(Paths.get(APPDATA + "/config"));
            
            // Load custom font from JAR
            try (InputStream fontStream = getClass().getResourceAsStream("/com/terramonic/Gilroy-Bold.otf")) {
                if (fontStream != null) {
                    customFont = Font.loadFont(fontStream, 14);
                } else {
                    customFont = Font.font("Segoe UI", FontWeight.BOLD, 14);
                }
            }
            
            // Load icon and background from JAR
            loadDefaultResources();
            
            // Initialize Fabric Launcher
            fabricLauncher = new MinecraftFabricLauncher();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDefaultResources() {
        try {
            // Load icon from JAR
            try (InputStream iconStream = getClass().getResourceAsStream("/com/terramonic/icon.png")) {
                if (iconStream != null) {
                    Files.copy(iconStream, Paths.get(APPDATA + "/icon.png"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            
            // Load background from JAR
            try (InputStream bgStream = getClass().getResourceAsStream("/com/terramonic/arkaplan.png")) {
                if (bgStream != null) {
                    Files.copy(bgStream, Paths.get(APPDATA + "/arkaplan.png"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSplashScreen() {
        root = new StackPane();
        root.setPrefSize(WIDTH, HEIGHT);
        
        // Glassmorphism background
        Rectangle bg = new Rectangle(WIDTH, HEIGHT);
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, null,
            new Stop(0, Color.rgb(30, 30, 60, 0.9)),
            new Stop(1, Color.rgb(60, 30, 90, 0.9))
        );
        bg.setFill(gradient);
        bg.setEffect(new GaussianBlur(50));
        
        VBox splashBox = new VBox(20);
        splashBox.setAlignment(Pos.CENTER);
        splashBox.setPadding(new Insets(50));
        
        // Logo with glow effect
        ImageView logo = createGlowingLogo();
        
        Label titleLabel = new Label("TerraMonic Launcher");
        titleLabel.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(new DropShadow(20, Color.CYAN));
        
        Label loadingLabel = new Label("Gerekli dosyalar indiriliyor...");
        loadingLabel.setFont(Font.font(customFont.getFamily(), 16));
        loadingLabel.setTextFill(Color.LIGHTGRAY);
        
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setStyle("-fx-accent: linear-gradient(to right, #667eea, #764ba2);");
        
        Label percentLabel = new Label("0%");
        percentLabel.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 14));
        percentLabel.setTextFill(Color.WHITE);
        
        splashBox.getChildren().addAll(logo, titleLabel, loadingLabel, progressBar, percentLabel);
        
        root.getChildren().addAll(bg, splashBox);
        
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.show();
        
        // Start loading process
        startLoadingProcess(progressBar, percentLabel, loadingLabel);
    }

    private ImageView createGlowingLogo() {
        try {
            Image logoImage = new Image("file:" + APPDATA + "/icon.png");
            ImageView logo = new ImageView(logoImage);
            logo.setFitWidth(80);
            logo.setFitHeight(80);
            
            // Glow effect
            Glow glow = new Glow(0.8);
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.CYAN);
            dropShadow.setRadius(30);
            dropShadow.setSpread(0.5);
            glow.setInput(dropShadow);
            logo.setEffect(glow);
            
            // Rotation animation
            RotateTransition rotate = new RotateTransition(Duration.seconds(4), logo);
            rotate.setByAngle(360);
            rotate.setCycleCount(Animation.INDEFINITE);
            rotate.play();
            
            return logo;
        } catch (Exception e) {
            // Fallback rectangle
            Rectangle rect = new Rectangle(80, 80, Color.CYAN);
            rect.setEffect(new Glow(0.8));
            return new ImageView();
        }
    }

    private void startLoadingProcess(ProgressBar progressBar, Label percentLabel, Label loadingLabel) {
        Task<Void> loadingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Step 1: Check maintenance mode
                updateMessage("Bakım durumu kontrol ediliyor...");
                updateProgress(10, 100);
                checkMaintenanceMode();
                Thread.sleep(500);
                
                // Step 2: Download modpack
                updateMessage("Modpack indiriliyor...");
                updateProgress(20, 100);
                downloadModpack();
                Thread.sleep(1000);
                
                // Step 3: Extract modpack
                updateMessage("Modpack çıkarılıyor...");
                updateProgress(40, 100);
                extractModpack();
                Thread.sleep(1000);
                
                // Step 4: Download Minecraft
                updateMessage("Minecraft indiriliyor...");
                updateProgress(60, 100);
                fabricLauncher.install();
                Thread.sleep(1000);
                
                // Step 5: Setup mods
                updateMessage("Modlar kuruluyor...");
                updateProgress(80, 100);
                setupMods();
                Thread.sleep(1000);
                
                // Step 6: Finalize
                updateMessage("Kurulum tamamlanıyor...");
                updateProgress(100, 100);
                Thread.sleep(500);
                
                return null;
            }
        };
        
        loadingTask.progressProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                progressBar.setProgress(newVal.doubleValue() / 100);
                percentLabel.setText((int)(newVal.doubleValue()) + "%");
            });
        });
        
        loadingTask.messageProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> loadingLabel.setText(newVal));
        });
        
        loadingTask.setOnSucceeded(e -> Platform.runLater(this::showMainScreen));
        loadingTask.setOnFailed(e -> Platform.runLater(() -> {
            loadingLabel.setText("Hata oluştu: " + loadingTask.getException().getMessage());
            loadingLabel.setTextFill(Color.RED);
        }));
        
        new Thread(loadingTask).start();
    }

    private void downloadModpack() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(MODPACK_URL))
            .build();
        
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        Files.write(Paths.get(APPDATA + "/modpack.mrpack"), response.body());
    }

    private void extractModpack() throws Exception {
        Path modpackPath = Paths.get(APPDATA + "/modpack.mrpack");
        Path extractPath = Paths.get(APPDATA + "/modpack_temp");
        
        // Extract .mrpack (which is a zip file)
        try (ZipFile zipFile = new ZipFile(modpackPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path entryPath = extractPath.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        Files.copy(inputStream, entryPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
        
        // Copy overrides
        Path overridesPath = extractPath.resolve("overrides");
        if (Files.exists(overridesPath)) {
            copyDirectory(overridesPath, Paths.get(APPDATA));
        }
    }

    private void setupMods() throws Exception {
        // Read modrinth.index.json and download mods
        Path indexPath = Paths.get(APPDATA + "/modpack_temp/modrinth.index.json");
        if (Files.exists(indexPath)) {
            String indexContent = Files.readString(indexPath);
            JsonObject indexData = gson.fromJson(indexContent, JsonObject.class);
            JsonArray files = indexData.getAsJsonArray("files");
            
            for (JsonElement fileElement : files) {
                JsonObject fileObj = fileElement.getAsJsonObject();
                String path = fileObj.get("path").getAsString();
                JsonArray downloads = fileObj.getAsJsonArray("downloads");
                
                if (downloads.size() > 0) {
                    String downloadUrl = downloads.get(0).getAsString();
                    downloadFile(downloadUrl, Paths.get(APPDATA + "/" + path));
                }
            }
        }
    }

    private void downloadFile(String url, Path filePath) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build();
        
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, response.body());
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
                e.printStackTrace();
            }
        });
    }

    private void checkMaintenanceMode() {
        try {
            JsonObject maintenance = new JsonObject();
            maintenance.addProperty("bakimmmodu", false);
            maintenance.addProperty("bakimmmodusebebi", "DENEME");
            
            Path maintenancePath = Paths.get(APPDATA + "/maintenance.json");
            Files.write(maintenancePath, gson.toJson(maintenance).getBytes());
            
            // Read maintenance mode
            if (Files.exists(maintenancePath)) {
                String content = Files.readString(maintenancePath);
                JsonObject maintenanceData = gson.fromJson(content, JsonObject.class);
                maintenanceMode = maintenanceData.get("bakimmmodu").getAsBoolean();
                maintenanceReason = maintenanceData.get("bakimmmodusebebi").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMainScreen() {
        root.getChildren().clear();
        
        // Background with blur effect
        try {
            Image bgImage = new Image("file:" + APPDATA + "/arkaplan.png");
            backgroundImageView = new ImageView(bgImage);
            backgroundImageView.setFitWidth(WIDTH);
            backgroundImageView.setFitHeight(HEIGHT);
            backgroundImageView.setEffect(new GaussianBlur(15));
        } catch (Exception e) {
            // Fallback gradient background
            Rectangle bgRect = new Rectangle(WIDTH, HEIGHT);
            LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, null,
                new Stop(0, Color.rgb(20, 30, 70)),
                new Stop(0.5, Color.rgb(40, 20, 80)),
                new Stop(1, Color.rgb(70, 30, 50))
            );
            bgRect.setFill(gradient);
            backgroundImageView = new ImageView();
            root.getChildren().add(bgRect);
        }
        
        if (backgroundImageView.getImage() != null) {
            root.getChildren().add(backgroundImageView);
        }
        
        // Main container with glassmorphism effect
        VBox mainContainer = new VBox();
        mainContainer.setPrefSize(WIDTH, HEIGHT);
        mainContainer.setPadding(new Insets(20));
        
        // Top bar with window controls
        HBox topBar = createTopBar();
        
        // Center content
        VBox centerContent = createCenterContent();
        
        // Bottom bar with tools
        HBox bottomBar = createBottomBar();
        
        mainContainer.getChildren().addAll(topBar, centerContent, bottomBar);
        VBox.setVgrow(centerContent, Priority.ALWAYS);
        
        root.getChildren().add(mainContainer);
        
        // Add maintenance overlay if needed
        if (maintenanceMode) {
            showMaintenanceOverlay();
        }
        
        // Apply corner radius to stage
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        
        // Add fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(10);
        
        // Background change button
        Button bgButton = createGlassButton("🖼");
        bgButton.setTooltip(new Tooltip("Arkaplan Değiştir"));
        bgButton.setOnAction(e -> changeBackground());
        
        // Minimize button
        Button minimizeBtn = createGlassButton("─");
        minimizeBtn.setOnAction(e -> primaryStage.setIconified(true));
        
        // Close button
        Button closeBtn = createGlassButton("✕");
        closeBtn.setOnAction(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> Platform.exit());
            fadeOut.play();
        });
        
        topBar.getChildren().addAll(bgButton, minimizeBtn, closeBtn);
        return topBar;
    }

    private VBox createCenterContent() {
        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(20));
        
        // Logo with shadow
        ImageView logo = new ImageView();
        try {
            Image logoImage = new Image("file:" + APPDATA + "/icon.png");
            logo.setImage(logoImage);
            logo.setFitWidth(100);
            logo.setFitHeight(100);
            
            DropShadow logoShadow = new DropShadow();
            logoShadow.setColor(Color.BLACK);
            logoShadow.setRadius(25);
            logoShadow.setSpread(0.3);
            logo.setEffect(logoShadow);
        } catch (Exception e) {
            // Fallback
        }
        
        // Title
        Label titleLabel = new Label("TerraMonic Enhanced");
        titleLabel.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(new DropShadow(10, Color.CYAN));
        
        // Login form
        VBox loginBox = createLoginForm();
        
        // Mod manager
        VBox modManager = createModManager();
        
        // Play button
        Button playButton = createPlayButton();
        
        centerContent.getChildren().addAll(logo, titleLabel, loginBox, modManager, playButton);
        return centerContent;
    }

    private VBox createLoginForm() {
        VBox loginBox = new VBox(15);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setMaxWidth(350);
        loginBox.setPadding(new Insets(20));
        loginBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 15; -fx-border-width: 1;");
        loginBox.setEffect(new GaussianBlur(0.5));
        
        nameField = new TextField();
        nameField.setPromptText("Kullanıcı Adı");
        nameField.setFont(Font.font(customFont.getFamily(), 14));
        nameField.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.7); -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 10;");
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Şifre (Opsiyonel)");
        passwordField.setFont(Font.font(customFont.getFamily(), 14));
        passwordField.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10; -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.7); -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 10;");
        
        loginBox.getChildren().addAll(nameField, passwordField);
        return loginBox;
    }

    private VBox createModManager() {
        VBox modManager = new VBox(10);
        modManager.setAlignment(Pos.CENTER);
        modManager.setMaxWidth(400);
        modManager.setPadding(new Insets(15));
        modManager.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 15; -fx-border-width: 1;");
        
        Label modLabel = new Label("Mod Yönetimi");
        modLabel.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 16));
        modLabel.setTextFill(Color.WHITE);
        
        HBox modButtons = new HBox(10);
        modButtons.setAlignment(Pos.CENTER);
        
        Button addModBtn = createGlassButton("Mod Ekle");
        Button removeModBtn = createGlassButton("Mod Sil");
        Button createProfileBtn = createGlassButton("Profil Oluştur");
        Button loadProfileBtn = createGlassButton("Profil Yükle");
        
        addModBtn.setOnAction(e -> addMod());
        removeModBtn.setOnAction(e -> removeMod());
        createProfileBtn.setOnAction(e -> createProfile());
        loadProfileBtn.setOnAction(e -> loadProfile());
        
        modButtons.getChildren().addAll(addModBtn, removeModBtn, createProfileBtn, loadProfileBtn);
        modManager.getChildren().addAll(modLabel, modButtons);
        
        return modManager;
    }

    private Button createPlayButton() {
        Button playButton = new Button("OYNA");
        playButton.setPrefSize(200, 50);
        playButton.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 18));
        playButton.setTextFill(Color.WHITE);
        playButton.setStyle("-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-background-radius: 25; -fx-border-radius: 25; -fx-effect: dropshadow(gaussian, rgba(102,126,234,0.8), 15, 0.5, 0, 0);");
        
        // Hover animation
        playButton.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), playButton);
            scale.setToX(1.1);
            scale.setToY(1.1);
            scale.play();
        });
        
        playButton.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), playButton);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });
        
        playButton.setOnAction(e -> startGame());
        
        return playButton;
    }

    private HBox createBottomBar() {
        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(10));
        bottomBar.setSpacing(20);
        
        // Repair button
        Button repairBtn = createGlassButton("Onar");
        repairBtn.setOnAction(e -> repairGame());
        
        // RAM slider
        VBox ramBox = new VBox(5);
        ramBox.setAlignment(Pos.CENTER);
        
        Label ramLabel = new Label("RAM: 4GB");
        ramLabel.setFont(Font.font(customFont.getFamily(), 12));
        ramLabel.setTextFill(Color.WHITE);
        
        ramSlider = new Slider(2, 16, 4);
        ramSlider.setShowTickMarks(true);
        ramSlider.setMajorTickUnit(2);
        ramSlider.setPrefWidth(150);
        ramSlider.setStyle("-fx-control-inner-background: rgba(255,255,255,0.2);");
        
        ramSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ramLabel.setText("RAM: " + (int)newVal.doubleValue() + "GB");
        });
        
        ramBox.getChildren().addAll(ramLabel, ramSlider);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Status label
        statusLabel = new Label("Hazır");
        statusLabel.setFont(Font.font(customFont.getFamily(), 12));
        statusLabel.setTextFill(Color.LIGHTGREEN);
        
        bottomBar.getChildren().addAll(repairBtn, ramBox, spacer, statusLabel);
        return bottomBar;
    }

    private Button createGlassButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font(customFont.getFamily(), 12));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 10; -fx-border-width: 1;");
        
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.5); -fx-border-radius: 10; -fx-border-width: 1;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 10; -fx-border-width: 1;");
        });
        
        return button;
    }

    private void showMaintenanceOverlay() {
        VBox maintenanceOverlay = new VBox(20);
        maintenanceOverlay.setAlignment(Pos.CENTER);
        maintenanceOverlay.setPrefSize(WIDTH, HEIGHT);
        maintenanceOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.8);");
        
        Label maintenanceTitle = new Label("🔧 BAKIM MODU");
        maintenanceTitle.setFont(Font.font(customFont.getFamily(), FontWeight.BOLD, 32));
        maintenanceTitle.setTextFill(Color.ORANGE);
        
        Label maintenanceMsg = new Label(maintenanceReason);
        maintenanceMsg.setFont(Font.font(customFont.getFamily(), 18));
        maintenanceMsg.setTextFill(Color.WHITE);
        
        Button okButton = createGlassButton("Tamam");
        okButton.setOnAction(e -> root.getChildren().remove(maintenanceOverlay));
        
        maintenanceOverlay.getChildren().addAll(maintenanceTitle, maintenanceMsg, okButton);
        root.getChildren().add(maintenanceOverlay);
    }

    private void changeBackground() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Arkaplan Seç");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg"));
        
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                Files.copy(selectedFile.toPath(), Paths.get(APPDATA + "/arkaplan.png"), StandardCopyOption.REPLACE_EXISTING);
                Image newBg = new Image("file:" + APPDATA + "/arkaplan.png");
                backgroundImageView.setImage(newBg);
                statusLabel.setText("Arkaplan değiştirildi");
            } catch (Exception e) {
                statusLabel.setText("Arkaplan değiştirilemedi");
                statusLabel.setTextFill(Color.RED);
            }
        }
    }

    private void addMod() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Mod Seç");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Mod Dosyaları", "*.jar"));
        
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                Files.copy(selectedFile.toPath(), Paths.get(APPDATA + "/mods/" + selectedFile.getName()), StandardCopyOption.REPLACE_EXISTING);
                statusLabel.setText("Mod eklendi: " + selectedFile.getName());
                statusLabel.setTextFill(Color.LIGHTGREEN);
            } catch (Exception e) {
                statusLabel.setText("Mod eklenemedi");
                statusLabel.setTextFill(Color.RED);
            }
        }
    }

    private void removeMod() {
        // Implementation for mod removal
        statusLabel.setText("Mod silme özelliği yakında...");
    }

    private void createProfile() {
        // Implementation for profile creation
        statusLabel.setText("Profil oluşturma özelliği yakında...");
    }

    private void loadProfile() {
        // Implementation for profile loading
        statusLabel.setText("Profil yükleme özelliği yakında...");
    }

    private void repairGame() {
        statusLabel.setText("Oyun onarılıyor...");
        statusLabel.setTextFill(Color.YELLOW);
        
        Task<Void> repairTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                fabricLauncher.install();
                return null;
            }
        };
        
        repairTask.setOnSucceeded(e -> Platform.runLater(() -> {
            statusLabel.setText("Oyun onarıldı");
            statusLabel.setTextFill(Color.LIGHTGREEN);
        }));
        
        new Thread(repairTask).start();
    }

    private void startGame() {
        if (nameField.getText().trim().isEmpty()) {
            statusLabel.setText("Kullanıcı adı gerekli!");
            statusLabel.setTextFill(Color.RED);
            return;
        }
        
        statusLabel.setText("Oyun başlatılıyor...");
        statusLabel.setTextFill(Color.YELLOW);
        
        Task<Void> gameStartTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String username = nameField.getText().trim();
                int ramMB = (int) (ramSlider.getValue() * 1024);
                
                // Build complete classpath
                String classpath = getClasspath();
                
                // Build launch command
                List<String> command = new ArrayList<>();
                command.add("java");
                command.add("-Xmx" + ramMB + "M");
                command.add("-Xms1G");
                command.add("-Djava.library.path=" + APPDATA + "/natives");
                command.add("-Dminecraft.launcher.brand=TerraMonic");
                command.add("-Dminecraft.launcher.version=1.0.0");
                command.add("-DFabricMcEmu=net.minecraft.client.main.Main");
                command.add("-cp");
                command.add(classpath);
                command.add("net.fabricmc.loader.impl.launch.knot.KnotClient");
                command.add("--username");
                command.add(username);
                command.add("--version");
                command.add("1.21.5");
                command.add("--gameDir");
                command.add(APPDATA);
                command.add("--assetsDir");
                command.add(APPDATA + "/assets");
                command.add("--assetIndex");
                command.add(getAssetIndexId());
                command.add("--uuid");
                command.add("00000000-0000-0000-0000-000000000000");
                command.add("--accessToken");
                command.add("0");
                command.add("--userType");
                command.add("legacy");
                
                // Create process
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(new File(APPDATA));
                pb.redirectErrorStream(true);
                
                // Set environment variables
                Map<String, String> env = pb.environment();
                env.put("JAVA_HOME", System.getProperty("java.home"));
                
                // Start the process
                Process process = pb.start();
                
                // Wait a bit to see if it starts successfully
                Thread.sleep(3000);
                
                if (process.isAlive()) {
                    updateMessage("Oyun başlatıldı!");
                } else {
                    // Process died, check exit code
                    int exitCode = process.exitValue();
                    if (exitCode != 0) {
                        // Read error output
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            StringBuilder error = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                error.append(line).append("\n");
                            }
                            throw new RuntimeException("Oyun başlatılamadı (Exit Code: " + exitCode + ")\n" + error.toString());
                        }
                    }
                }
                
                return null;
            }
        };
        
        gameStartTask.setOnSucceeded(e -> Platform.runLater(() -> {
            statusLabel.setText("Oyun başlatıldı!");
            statusLabel.setTextFill(Color.LIGHTGREEN);
            
            // Show success animation
            ScaleTransition successScale = new ScaleTransition(Duration.millis(300), statusLabel);
            successScale.setFromX(1.0);
            successScale.setFromY(1.0);
            successScale.setToX(1.2);
            successScale.setToY(1.2);
            successScale.setAutoReverse(true);
            successScale.setCycleCount(2);
            successScale.play();
        }));
        
        gameStartTask.setOnFailed(e -> Platform.runLater(() -> {
            Throwable exception = gameStartTask.getException();
            statusLabel.setText("Hata: " + exception.getMessage());
            statusLabel.setTextFill(Color.RED);
            
            // Show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Oyun Başlatma Hatası");
            alert.setHeaderText("Minecraft başlatılamadı!");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }));
        
        new Thread(gameStartTask).start();
    }

    private String getClasspath() {
        List<String> classpathParts = new ArrayList<>();
        
        // Add main jar
        classpathParts.add(APPDATA + "/versions/1.21.5/1.21.5.jar");
        
        // Add all libraries from libraries directory
        try {
            Path libDir = Paths.get(APPDATA + "/libraries");
            if (Files.exists(libDir)) {
                Files.walk(libDir)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .forEach(path -> classpathParts.add(path.toString()));
            }
        } catch (IOException e) {
            System.err.println("Error scanning libraries: " + e.getMessage());
        }
        
        // Add essential libraries manually if not found
        String[] essentialLibs = {
            "net/fabricmc/fabric-loader/0.16.14/fabric-loader-0.16.14.jar",
            "net/fabricmc/sponge-mixin/0.15.5+mixin.0.8.7/sponge-mixin-0.15.5+mixin.0.8.7.jar",
            "net/fabricmc/intermediary/1.21.5/intermediary-1.21.5.jar",
            "org/ow2/asm/asm/9.8/asm-9.8.jar",
            "org/ow2/asm/asm-tree/9.8/asm-tree-9.8.jar",
            "org/ow2/asm/asm-commons/9.8/asm-commons-9.8.jar",
            "org/ow2/asm/asm-analysis/9.8/asm-analysis-9.8.jar",
            "org/ow2/asm/asm-util/9.8/asm-util-9.8.jar"
        };
        
        for (String libPath : essentialLibs) {
            Path fullPath = Paths.get(APPDATA + "/libraries/" + libPath);
            if (Files.exists(fullPath) && !classpathParts.contains(fullPath.toString())) {
                classpathParts.add(fullPath.toString());
            }
        }
        
        return String.join(System.getProperty("path.separator"), classpathParts);
    }

    private String getAssetIndexId() {
        try {
            Path versionJsonPath = Paths.get(APPDATA + "/versions/1.21.5/1.21.5.json");
            if (Files.exists(versionJsonPath)) {
                String content = Files.readString(versionJsonPath);
                JsonObject versionData = gson.fromJson(content, JsonObject.class);
                if (versionData.has("assetIndex")) {
                    return versionData.getAsJsonObject("assetIndex").get("id").getAsString();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting asset index ID: " + e.getMessage());
        }
        return "24"; // Default fallback for 1.21.5
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        launch(args);
    }
}