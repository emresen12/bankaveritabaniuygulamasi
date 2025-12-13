
package com.example.projeveriarayuz;

public class AppSession {
    private static int activeMusteriId = 0;
    private static String kullaniciTC = "";
    private static String kullaniciIsim = "";

    /**
     * Müşteri oturum bilgilerini ayarlar.
     */
    public static void setSession(int musteriId, String tc, String isim) {
        AppSession.activeMusteriId = musteriId;
        AppSession.kullaniciTC = tc;
        AppSession.kullaniciIsim = isim;
    }

    /**
     * Oturumu temizler (Çıkış/Logout için).
     */
    public static void clearSession() {
        AppSession.activeMusteriId = 0;
        AppSession.kullaniciTC = "";
        AppSession.kullaniciIsim = "";
    }

    // Getter'lar
    public static int getActiveMusteriId() {
        return activeMusteriId;
    }

    public static String getKullaniciTC() {
        return kullaniciTC;
    }

    public static String getKullaniciIsim() {
        return kullaniciIsim;
    }

    public static boolean isUserLoggedIn() {
        return activeMusteriId > 0;
    }
}