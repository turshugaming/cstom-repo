package com.mojang.jtracy;

/**
 * Mojang'ın dahili JTrace profil aracına ait "{@code com.mojang.jtracy.TracyClient}"
 * sınıfının işlevsiz (stub) sürümü.
 * Oyunun yalnızca bu sınıfı ve metod imzalarını yükleyebilmesi yeterlidir.
 */
public final class TracyClient {

    private static boolean active = false;

    private TracyClient() {
        // Singleton benzeri kullanım, örnek oluşturulmaz
    }

    /**
     * Profil kaydı aktif mi? Gerçek kütüphanede Tracy durumu kontrol edilir.
     * Stub sürümünde her zaman {@code false} döner.
     */
    public static boolean isActive() {
        return active;
    }

    /**
     * Yeni kare başlangıcında çağrılır. İşlevsiz.
     */
    public static void beginFrame() {
        // no-op
    }

    /**
     * Kare bitiminde çağrılır. İşlevsiz.
     */
    public static void endFrame() {
        // no-op
    }

    /**
     * Profil kayıt bölgesi başlatır. Gerçek sürümde Tracy arayüzünde görünür.
     * Burada yalnızca yeni bir {@link Zone} döndürülür.
     */
    public static Zone zone(String name) {
        return new Zone(name);
    }
}