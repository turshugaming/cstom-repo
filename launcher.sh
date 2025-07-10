#!/bin/bash

echo ""
echo "🎮 TerraMonic Launcher 🎮"
echo "=========================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven bulunamadı! Lütfen Maven yükleyin."
    exit 1
fi

# Check if Java 17+ is available
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "17" ]; then
    echo "❌ Java 17+ bulunamadı! Lütfen Java 17 veya üzeri yükleyin."
    echo "Mevcut Java version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java $JAVA_VERSION bulundu"
echo "✅ Maven yüklü"
echo ""
echo "🚀 Launcher başlatılıyor..."
echo ""

# Run with Maven
mvn clean javafx:run

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ HATA! Launcher başlatırken sorun oluştu."
    echo ""
    echo "💡 Çözümler:"
    echo "   1. Java 17+ yüklü olduğunu kontrol edin"
    echo "   2. Maven yüklü olduğunu kontrol edin"
    echo "   3. İnternet bağlantınızı kontrol edin"
    echo ""
    exit 1
fi

echo ""
echo "✅ Launcher kapatıldı."