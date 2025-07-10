@echo off
echo.
echo ========================================
echo  🎮 TerraMonic Launcher Starting...
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven bulunamadi! Lutfen Maven yukleyin.
    pause
    exit /b 1
)

REM Check if Java 17+ is available
java -version 2>&1 | find "17" >nul
if %ERRORLEVEL% NEQ 0 (
    java -version 2>&1 | find "18" >nul
    if %ERRORLEVEL% NEQ 0 (
        java -version 2>&1 | find "19" >nul
        if %ERRORLEVEL% NEQ 0 (
            java -version 2>&1 | find "20" >nul
            if %ERRORLEVEL% NEQ 0 (
                java -version 2>&1 | find "21" >nul
                if %ERRORLEVEL% NEQ 0 (
                    echo ❌ Java 17+ bulunamadi! Lutfen Java 17 veya uzeri yukleyin.
                    pause
                    exit /b 1
                )
            )
        )
    )
)

echo ✅ Java version kontrolu tamam
echo ✅ Maven yuklu

echo.
echo 📦 Maven ile calistirilyor...
echo.

REM Run with Maven
mvn clean javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Launcher baslatilirken hata olustu!
    echo.
    echo 🔄 JAR dosyasi ile deneniyor...
    echo.
    
    REM Try to build and run JAR
    mvn clean package -q
    
    if exist "target\terramonic-launcher-1.0.0.jar" (
        echo ✅ JAR dosyasi olusturuldu
        echo 🚀 Launcher baslatiiliyor...
        java -jar target\terramonic-launcher-1.0.0.jar
    ) else (
        echo ❌ JAR dosyasi olusturulamadi!
        echo.
        echo 💡 Cozum onerileri:
        echo    1. Internet baglantinizi kontrol edin
        echo    2. Antivirusu gecici olarak kapatin
        echo    3. Maven cache temizleyin: mvn clean
        echo.
    )
)

echo.
echo ========================================
echo  TerraMonic Launcher kapatildi
echo ========================================
pause