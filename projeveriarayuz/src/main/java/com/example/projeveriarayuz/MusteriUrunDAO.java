package com.example.projeveriarayuz;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class MusteriUrunDAO {

    private String generateHesapNumarasi() {
        Random random = new Random();
        long uniquePart = System.currentTimeMillis() + random.nextInt(1000000);
        return "ACC" + uniquePart;
    }

    public boolean urunSatinAl(int musteriId, int urunId, String kaynakHesapNo, double tutar, String urunAdi) {

        String urunHesapNo = generateHesapNumarasi(); // Ürüne özel numara
        String updateBakiyeSql = "UPDATE Hesaplar SET Bakiye = Bakiye - ? WHERE HesapNo = ?";
        String insertUrunSql = "INSERT INTO MusteriUrun (MusteriID, UrunID, BaslangicTarihi, HesapNumarasi) VALUES (?, ?, GETDATE(), ?)";

        try (Connection conn = DbConnection.getConnection()) {

            try (PreparedStatement psUpdate = conn.prepareStatement(updateBakiyeSql)) {
                psUpdate.setDouble(1, tutar);
                psUpdate.setString(2, kaynakHesapNo);
                int updateCount = psUpdate.executeUpdate();

                if (updateCount == 0) {
                    System.err.println("Hata: Bakiye düşülemedi veya hesap bulunamadı.");
                    return false;
                }
            }

            try (PreparedStatement psInsert = conn.prepareStatement(insertUrunSql)) {
                psInsert.setInt(1, musteriId);
                psInsert.setInt(2, urunId);
                psInsert.setString(3, urunHesapNo);
                psInsert.executeUpdate();
            }
            IslemDAO.islemKaydet(kaynakHesapNo, null, null, null, urunAdi + " Satın Alımı", tutar);

            return true;

        } catch (SQLException e) {
            System.err.println("Ürün Satın Alma Hatası: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public ResultSet musteriUrunleriniGetir(int musteriId) throws SQLException {

        Connection conn = DbConnection.getConnection();
        if (conn == null) {
            System.err.println("HATA: Veritabanı bağlantısı kurulamadı...");
            throw new SQLException("Veritabanı bağlantısı kurulamadı.");
        }

        // Sütunları AS kullanarak zorla Controller'ın beklediği isme eşleştirme
        String sql = "SELECT " +
                "    MU.HesapNumarasi AS HesapNumarasi, " +
                "    P.UrunTipi AS UrunTipi, " +
                "    P.Aciklama AS Aciklama, " +
                "    MU.BaslangicTarihi AS BaslangicTarihi " +
                "FROM MusteriUrun MU " +
                "JOIN Product P ON MU.UrunID = P.UrunID " +
                "WHERE MU.MusteriID = ?";

        try {
            System.out.println("DEBUG: SQL Sorgusu Çalıştırılıyor: " + sql);
            System.out.println("DEBUG: MusteriID Parametresi: " + musteriId);

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, musteriId);

            ResultSet rs = ps.executeQuery();

            return rs;

        } catch (SQLException e) {
            // ... (Hata yakalama kısmı aynı)
            System.err.println("KRİTİK HATA: SQL Sorgusu Çalıştırılamadı...");


            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) {  }
            }
            throw e;
        }
    }}