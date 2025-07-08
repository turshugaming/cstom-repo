# 🎮 TerraMonic Minecraft Launcher

Modern, feature-rich Minecraft launcher built with JavaFX, supporting Minecraft 1.21.5 and Fabric Loader 0.16.9.

![Version](https://img.shields.io/badge/version-1.0.2-green)
![Java](https://img.shields.io/badge/java-11+-blue)
![Minecraft](https://img.shields.io/badge/minecraft-1.21.5-brightgreen)
![Fabric](https://img.shields.io/badge/fabric-0.16.9-orange)

## ✨ Features

### 🎨 **Modern UI Design**
- Dark theme with green accent colors
- Custom Gilroy Bold font (automatically downloaded)
- Smooth animations and hover effects
- Responsive layout with sidebar and main content area

### ⚡ **Minecraft Integration**
- **Automatic Minecraft 1.21.5 installation** from official Mojang servers
- **Fabric Loader 0.16.9** integration with automatic setup
- **Launcher profile management** - Creates and manages TerraMonic profile
- **Cross-platform support** - Windows, macOS, and Linux

### 🔧 **Advanced Features**
- **Real-time progress tracking** for downloads and installations
- **Optimized JVM arguments** with G1GC for better performance
- **Offline mode support** with username-based UUID generation
- **Multi-threaded operations** for smooth UI experience
- **Error handling** with user-friendly error messages

### 🎯 **Planned Features**
- Modrinth mod browser and installer
- Mod pack management
- Settings panel with customizable options
- News feed integration
- Resource pack management

## 🚀 Quick Start

### Prerequisites
- **Java 11 or higher** with JavaFX support
- **Internet connection** (for initial setup)
- **At least 4GB RAM** recommended

### Building the Project

1. **Clone the repository:**
```bash
git clone <repository-url>
cd terramonic-launcher
```

2. **Compile the Java file:**
```bash
# Make sure you have JavaFX in your classpath
javac --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp "lib/*" TerraMonicLauncher1.java
```

3. **Run the launcher:**
```bash
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp "lib/*:." com.terramonic.TerraMonicLauncher1
```

### Required Dependencies

Add these JAR files to your `lib/` directory:

- **JavaFX** (if not bundled with your JDK)
  - `javafx-controls-*.jar`
  - `javafx-base-*.jar`
  - `javafx-graphics-*.jar`

- **JSON Processing**
  - `json-*.jar` (org.json library)

### Maven Setup (Recommended)

Create a `pom.xml` file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.terramonic</groupId>
    <artifactId>terramonic-launcher</artifactId>
    <version>1.0.2</version>
    
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <javafx.version>17.0.2</javafx.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.json</groupId>
            <artifactId>json</artifactId>
            <version>20231013</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>com.terramonic.TerraMonicLauncher1</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

Then run:
```bash
mvn clean compile
mvn javafx:run
```

## 📖 Usage Guide

### First Launch
1. **Enter your Minecraft username** in the top-right text field
2. **Click "OYUNU BAŞLAT"** (Start Game) button
3. The launcher will automatically:
   - Download Minecraft 1.21.5 if not present
   - Install Fabric Loader 0.16.9
   - Create the TerraMonic launcher profile
   - Launch Minecraft with optimized settings

### Directory Structure
The launcher creates these directories in your Minecraft folder:
```
~/.minecraft/                 # Main Minecraft directory
├── terramonic/              # TerraMonic launcher files
│   └── Gilroy-Bold.otf     # Downloaded font
├── versions/               # Minecraft versions
│   ├── 1.21.5/            # Vanilla Minecraft
│   └── fabric-loader-*/   # Fabric installation
├── mods/                  # Fabric mods (future feature)
└── launcher_profiles.json # Launcher profiles
```

### System Requirements
- **Minimum RAM:** 4GB (launcher allocates 4GB to Minecraft)
- **Java:** Version 11 or higher
- **Disk Space:** ~500MB for Minecraft + Fabric
- **OS:** Windows 7+, macOS 10.12+, or Linux

## 🔧 Configuration

### JVM Arguments
The launcher uses optimized JVM arguments:
```
-Xmx4G                              # Maximum 4GB RAM
-Xms1G                              # Initial 1GB RAM
-XX:+UnlockExperimentalVMOptions    # Enable experimental features
-XX:+UseG1GC                        # Use G1 Garbage Collector
-XX:G1NewSizePercent=20             # G1 young generation size
-XX:G1ReservePercent=20             # G1 reserve memory
-XX:MaxGCPauseMillis=50             # Maximum GC pause time
-XX:G1HeapRegionSize=32M            # G1 heap region size
```

### Customization
You can modify these constants in the source code:
```java
private static final String MINECRAFT_VERSION = "1.21.5";
private static final String FABRIC_LOADER_VERSION = "0.16.9";
private static final Color PRIMARY_COLOR = Color.web("#01a500");
```

## 🐛 Troubleshooting

### Common Issues

**1. "Font download failed"**
- Check internet connection
- Firewall might be blocking the download
- Font will fall back to system default

**2. "Minecraft version not found"**
- Ensure you have internet connection
- The version might not be available yet
- Check Mojang's version manifest

**3. "Java not found"**
- Make sure Java 11+ is installed
- Verify JAVA_HOME environment variable
- JavaFX might need to be installed separately

**4. "Fabric installation failed"**
- Check Fabric Meta API availability
- Ensure write permissions to .minecraft folder
- Try running as administrator (Windows)

### Debug Mode
Enable console output to see detailed logs:
```bash
java -Dprism.verbose=true --module-path ... TerraMonicLauncher1
```

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

### Code Style
- Use 4 spaces for indentation
- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Keep methods under 50 lines when possible

### Future Development
Priority features to implement:
1. **Mod Browser** - Modrinth API integration
2. **Settings Panel** - Java args, RAM allocation, etc.
3. **Mod Pack Support** - Import/export mod packs
4. **News Feed** - Display Minecraft/mod news
5. **Multiple Profiles** - Support for different mod configurations

## 📄 License

This project is open source. Please respect Mojang's terms of service when using this launcher.

## 🙏 Acknowledgments

- **Mojang Studios** - For Minecraft
- **Fabric** - For the modding framework
- **OpenJFX** - For the JavaFX UI framework
- **Gilroy Font** - For the beautiful typography

---

**Built with ❤️ for the Minecraft community**