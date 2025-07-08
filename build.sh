#!/bin/bash

# TerraMonic Launcher Build Script
echo "🎮 TerraMonic Launcher Build Script"
echo "=================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Java is installed
echo -e "${YELLOW}Checking Java installation...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed or not in PATH${NC}"
    echo "Please install Java 11 or higher"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo -e "${RED}❌ Java 11 or higher is required. Found Java $JAVA_VERSION${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Java $JAVA_VERSION found${NC}"

# Create lib directory if it doesn't exist
mkdir -p lib

# Download dependencies if they don't exist
echo -e "${YELLOW}Checking dependencies...${NC}"

# JSON library
if [ ! -f "lib/json-20231013.jar" ]; then
    echo "📦 Downloading JSON library..."
    wget -O lib/json-20231013.jar "https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar"
fi

# JavaFX (if not bundled with JDK)
if ! java --list-modules | grep -q javafx; then
    echo "📦 JavaFX not found in JDK, downloading..."
    
    # Detect OS
    OS="unknown"
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="linux"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="mac"
    elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
        OS="win"
    fi
    
    if [ "$OS" != "unknown" ]; then
        JAVAFX_VERSION="17.0.2"
        JAVAFX_URL="https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_${OS}-x64_bin-sdk.zip"
        
        if [ ! -d "lib/javafx-sdk-17.0.2" ]; then
            echo "Downloading JavaFX for $OS..."
            wget -O lib/javafx.zip "$JAVAFX_URL"
            unzip -q lib/javafx.zip -d lib/
            rm lib/javafx.zip
        fi
        
        JAVAFX_PATH="lib/javafx-sdk-17.0.2/lib"
    else
        echo -e "${YELLOW}⚠️ Could not detect OS for JavaFX download${NC}"
        echo "Please download JavaFX manually and place it in lib/ directory"
        JAVAFX_PATH=""
    fi
else
    echo -e "${GREEN}✅ JavaFX found in JDK${NC}"
    JAVAFX_PATH=""
fi

echo -e "${GREEN}✅ Dependencies ready${NC}"

# Compile the launcher
echo -e "${YELLOW}Compiling TerraMonic Launcher...${NC}"

if [ -n "$JAVAFX_PATH" ]; then
    # Use downloaded JavaFX
    javac --module-path "$JAVAFX_PATH" --add-modules javafx.controls,javafx.fxml -cp "lib/*" TerraMonicLauncher1.java
    COMPILE_STATUS=$?
else
    # Use system JavaFX
    javac --add-modules javafx.controls,javafx.fxml -cp "lib/*" TerraMonicLauncher1.java
    COMPILE_STATUS=$?
fi

if [ $COMPILE_STATUS -eq 0 ]; then
    echo -e "${GREEN}✅ Compilation successful!${NC}"
else
    echo -e "${RED}❌ Compilation failed${NC}"
    exit 1
fi

# Create run script
echo -e "${YELLOW}Creating run script...${NC}"

cat > run_launcher.sh << EOF
#!/bin/bash

# TerraMonic Launcher Run Script
echo "🚀 Starting TerraMonic Launcher..."

# Build classpath
CLASSPATH="lib/*:."

# JavaFX module path
if [ -d "lib/javafx-sdk-17.0.2/lib" ]; then
    MODULE_PATH="lib/javafx-sdk-17.0.2/lib"
    java --module-path "\$MODULE_PATH" --add-modules javafx.controls,javafx.fxml -cp "\$CLASSPATH" com.terramonic.TerraMonicLauncher1
else
    java --add-modules javafx.controls,javafx.fxml -cp "\$CLASSPATH" com.terramonic.TerraMonicLauncher1
fi
EOF

chmod +x run_launcher.sh

echo -e "${GREEN}✅ Build complete!${NC}"
echo ""
echo "🚀 To run the launcher:"
echo "   ./run_launcher.sh"
echo ""
echo "📁 Project structure:"
echo "   TerraMonicLauncher1.java  - Main launcher source"
echo "   TerraMonicLauncher1.class - Compiled launcher"
echo "   lib/                      - Dependencies"
echo "   run_launcher.sh           - Quick start script"
echo ""
echo -e "${GREEN}Happy gaming! 🎮${NC}"