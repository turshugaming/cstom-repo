package com.mojang.jtracy;

/**
 * JTrace kütüphanesinin gerektirdiği {@code com.mojang.jtracy.TracyClient}
 * sınıfının minimal/sahte (stub) sürümü.
 * <p>
 * Gerçek kütüphane oyun içi performans profilini Tracy aracıyla paylaşır.
 * Bu stub, yalnızca Minecraft başlatılırken sınıfların bulunmasını sağlar
 * ve hiçbir işlem yapmaz.
 */
public final class TracyClient {

    private static boolean active = false;

    private TracyClient() {
        // kurucuya ihtiyaç yok (statik sınıf)
    }

    /**
     * Gerçek uygulamada Tracy profilinin aktif olup olmadığını döner.
     * Burada her zaman false.
     */
    public static boolean isActive() {
        return active;
    }

    /**
     * Çerçeve (frame) başlatıldığında çağrılır. İşlevsiz.
     */
    public static void beginFrame() {
        // no-op
    }

    /**
     * Çerçeve (frame) bittiğinde çağrılır. İşlevsiz.
     */
    public static void endFrame() {
        // no-op
    }

    /**
     * Profillenecek yeni bir bölge oluşturur.
     * Gerçekte Tracy arayüzünde bir "zone" başlatır; burada yalnızca
     * {@link Zone} nesnesi döndürülür.
     */
    public static Zone zone(String name) {
        return new Zone(name);
    }
}