package com.example.projeveriarayuz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Random;

public class MusteriUrunDAO {

    // Helper: Basit bir Hesap Numarası oluşturucu
    private String generateHesapNumarasi() {
        Random random = new Random();
        long uniquePart = System.currentTimeMillis() + random.nextInt(1000000);
        return "ACC" + uniquePart;
    }

    // ❗ Ürün Satın Alma Metodu: MusteriUrun tablosuna kayıt ekler
    public boolean urunSatinAl(int musteriId, int urunId) {

        String hesapNo = generateHesapNumarasi();
        String sql = "INSERT INTO MusteriUrun (MusteriID, UrunID, BaslangicTarihi, HesapNumarasi) " +
                "VALUES (?, ?, GETDATE(), ?)"; // GETDATE() SQL'in o anki tarihini kullanır

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, musteriId);
            ps.setInt(2, urunId);
            ps.setString(3, hesapNo);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println(" Ürün Satın Alma Hatası: " + e.getMessage());
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
            System.err.println(" Müşteri Ürünleri Getirme Hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
