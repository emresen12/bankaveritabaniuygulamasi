package com.example.projeveriarayuz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class MusteriUrunDAO {

    // Helper: Basit bir Hesap Numarası oluşturucu (Ürün için referans no)
    private String generateHesapNumarasi() {
        Random random = new Random();
        long uniquePart = System.currentTimeMillis() + random.nextInt(1000000);
        return "ACC" + uniquePart;
    }

    /**
     * Ürün Satın Alma Metodu:
     * 1. Parayı seçilen hesaptan çeker.
     * 2. Ürünü müşteriye tanımlar (MusteriUrun tablosuna ekler).
     * 3. İşlemi loglar (Islemler tablosuna ekler).
     */
    public boolean urunSatinAl(int musteriId, int urunId, String kaynakHesapNo, double tutar, String urunAdi) {

        String urunHesapNo = generateHesapNumarasi(); // Ürüne özel numara

        // 1. Bakiyeyi Düşme Sorgusu
        String updateBakiyeSql = "UPDATE Hesaplar SET Bakiye = Bakiye - ? WHERE HesapNo = ?";

        // 2. Ürünü Ekleme Sorgusu
        String insertUrunSql = "INSERT INTO MusteriUrun (MusteriID, UrunID, BaslangicTarihi, HesapNumarasi) VALUES (?, ?, GETDATE(), ?)";

        try (Connection conn = DbConnection.getConnection()) {

            // A. Ödemeyi Al (Bakiyeden Düş)
            try (PreparedStatement psUpdate = conn.prepareStatement(updateBakiyeSql)) {
                psUpdate.setDouble(1, tutar);
                psUpdate.setString(2, kaynakHesapNo);
                int updateCount = psUpdate.executeUpdate();

                if (updateCount == 0) {
                    System.err.println("Hata: Bakiye düşülemedi veya hesap bulunamadı.");
                    return false;
                }
            }

            // B. Ürünü Müşteriye Ekle
            try (PreparedStatement psInsert = conn.prepareStatement(insertUrunSql)) {
                psInsert.setInt(1, musteriId);
                psInsert.setInt(2, urunId);
                psInsert.setString(3, urunHesapNo);
                psInsert.executeUpdate();
            }

            // C. İşlemi Kaydet (IslemDAO Kullanarak)
            // Parametreler: KaynakHesap, HedefHesap(NULL), KartID(NULL), KrediID(NULL), Açıklama, Tutar
            IslemDAO.islemKaydet(kaynakHesapNo, null, null, null, urunAdi + " Satın Alımı", tutar);

            return true;

        } catch (SQLException e) {
            System.err.println("Ürün Satın Alma Hatası: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet musteriUrunleriniGetir(int musteriId) {
        String sql = "SELECT MU.HesapNumarasi, P.UrunTipi, P.Aciklama, MU.BaslangicTarihi " +
                "FROM MusteriUrun MU JOIN Product P ON MU.UrunID = P.UrunID " +
                "WHERE MU.MusteriID = ?";

        try {
            Connection conn = DbConnection.getConnection();
            if (conn == null) return null;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, musteriId);
            return ps.executeQuery();

        } catch (SQLException e) {
            System.err.println("Müşteri Ürünleri Getirme Hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}