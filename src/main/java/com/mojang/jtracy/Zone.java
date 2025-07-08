package com.mojang.jtracy;

/**
 * Minecraft 1.21 sürümündeki JTrace profil aracı için gereken {@code com.mojang.jtracy.Zone}
 * sınıfının işlevsiz (stub) implementasyonu.
 * <p>
 * Oyunun yalnızca sınıfın *var* olmasına ihtiyaç duyduğu için metodlar gövdesiz bırakıldı.
 */
public final class Zone implements AutoCloseable {

    /**
     * Yeni bir profil bölgesi başlatır. Asıl JTrace sürümünde bu isim
     * Tracy penceresinde görünüyor; bizim sahte sürümde ise yalnızca
     * varlığını korumak için tutuluyor.
     */
    public Zone(String name) {
        // no-op
    }

    /**
     * try-with-resources bloğundan çıkıldığında çağrılır.
     * Gerçek Tracy kapatma işlemine ihtiyaç duymaz.
     */
    @Override
    public void close() {
        // no-op
    }
}