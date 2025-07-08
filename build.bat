@echo off
REM TerraMonic Launcher Build Script for Windows
echo 🎮 TerraMonic Launcher Build Script (Windows)
echo ============================================

REM Check if Java is installed
echo Checking Java installation...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java is not installed or not in PATH
    echo Please install Java 11 or higher
    pause
    exit /b 1
)

echo ✅ Java found

REM Create lib directory if it doesn't exist
if not exist "lib" mkdir lib

REM Download dependencies if they don't exist
echo Checking dependencies...

REM JSON library
if not exist "lib\json-20231013.jar" (
    echo 📦 Downloading JSON library...
    powershell -Command "(New-Object System.Net.WebClient).DownloadFile('https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar', 'lib\json-20231013.jar')"
)

REM Check if JavaFX is available
java --list-modules | findstr javafx >nul 2>&1
if %errorlevel% neq 0 (
    echo 📦 JavaFX not found in JDK, downloading...
    
    if not exist "lib\javafx-sdk-17.0.2" (
        echo Downloading JavaFX for Windows...
        powershell -Command "(New-Object System.Net.WebClient).DownloadFile('https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_windows-x64_bin-sdk.zip', 'lib\javafx.zip')"
        powershell -Command "Expand-Archive -Path 'lib\javafx.zip' -DestinationPath 'lib\' -Force"
        del "lib\javafx.zip"
    )
    
    set JAVAFX_PATH=lib\javafx-sdk-17.0.2\lib
) else (
    echo ✅ JavaFX found in JDK
    set JAVAFX_PATH=
)

echo ✅ Dependencies ready

REM Compile the launcher
echo Compiling TerraMonic Launcher...

if defined JAVAFX_PATH (
    javac --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp "lib/*" TerraMonicLauncher1.java
) else (
    javac --add-modules javafx.controls,javafx.fxml -cp "lib/*" TerraMonicLauncher1.java
)

if %errorlevel% neq 0 (
    echo ❌ Compilation failed
    pause
    exit /b 1
)

echo ✅ Compilation successful!

REM Create run script
echo Creating run script...

(
echo @echo off
echo REM TerraMonic Launcher Run Script
echo echo 🚀 Starting TerraMonic Launcher...
echo.
echo REM Build classpath
echo set CLASSPATH=lib/*;.
echo.
echo REM JavaFX module path
echo if exist "lib\javafx-sdk-17.0.2\lib" ^(
echo     set MODULE_PATH=lib\javafx-sdk-17.0.2\lib
echo     java --module-path "%%MODULE_PATH%%" --add-modules javafx.controls,javafx.fxml -cp "%%CLASSPATH%%" com.terramonic.TerraMonicLauncher1
echo ^) else ^(
echo     java --add-modules javafx.controls,javafx.fxml -cp "%%CLASSPATH%%" com.terramonic.TerraMonicLauncher1
echo ^)
echo.
echo pause
) > run_launcher.bat

echo ✅ Build complete!
echo.
echo 🚀 To run the launcher:
echo    run_launcher.bat
echo.
echo 📁 Project structure:
echo    TerraMonicLauncher1.java  - Main launcher source
echo    TerraMonicLauncher1.class - Compiled launcher
echo    lib\                      - Dependencies
echo    run_launcher.bat          - Quick start script
echo.
echo Happy gaming! 🎮
pause