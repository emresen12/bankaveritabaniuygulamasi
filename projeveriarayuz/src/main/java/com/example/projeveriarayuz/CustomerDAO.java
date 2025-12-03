package com.example.projeveriarayuz;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CustomerDAO {

    // Müşteri ekleme (Kayıt ekranı için)
    public boolean musteriEkle(String tc, String ad, String soyad, String dogumTarihi, String telefon) {

        if (!isValidTC(tc)) {
            System.out.println("✗ Geçersiz TC: 11 haneli olmalı!");
            return false;
        }

        if (dogumTarihi != null && !dogumTarihi.isEmpty() && !isValidDate(dogumTarihi)) {
            System.out.println("✗ Geçersiz Doğum Tarihi formatı! YYYY-MM-DD olmalı.");
            return false;
        }

        if (musteriVarMi(tc)) {
            System.out.println("✗ Bu TC Kimlik No zaten kayıtlı!");
            return false;
        }

        // KrediPuani=0, MusteriOlmaTarihi=GETDATE() (SQL tarafından atanır)
        String sql = "INSERT INTO Musteri (TC_KimlikNo, Ad, Soyad, DogumTarihi, MusteriOlmaTarihi, Telefon, KrediPuani) " +
                "VALUES (?, ?, ?, ?, GETDATE(), ?, 0)";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("✗ Veritabanı bağlantısı kurulamadı!");
                return false;
            }

            ps.setString(1, tc);
            ps.setString(2, ad);
            ps.setString(3, soyad);

            if (dogumTarihi != null && !dogumTarihi.isEmpty()) {
                ps.setDate(4, Date.valueOf(dogumTarihi));
            } else {
                ps.setNull(4, Types.DATE);
            }

            if (telefon != null && !telefon.isEmpty()) {
                ps.setString(5, telefon);
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("✗ SQL Hatası: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // TC Kimlik No ile müşteri var mı kontrol et
    public boolean musteriVarMi(String tc) {
        String sql = "SELECT COUNT(*) FROM Musteri WHERE TC_KimlikNo = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { // Query'yi execute ederken try-with-resources kullanamayız, iç içe olması gerekir.

            ps.setString(1, tc);

            // ResultSet'i de try-with-resources ile yönetelim.
            try (ResultSet innerRs = ps.executeQuery()) {
                if (innerRs.next()) {
                    return innerRs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ musteriVarMi Kontrol hatası: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // TC ile MusteriID ve Ad/Soyad bilgisini getir (Giriş kontrolü için kritik)
    public ResultSet musteriGetir(String tc) throws SQLException {
        // MusteriID, Ad ve Soyad'ı çekiyoruz (Giriş ve ID taşıma için gerekli)
        String sql = "SELECT MusteriID, Ad, Soyad, TC_KimlikNo FROM Musteri WHERE TC_KimlikNo = ?";

        Connection conn = DbConnection.getConnection();
        if (conn == null) throw new SQLException("Veritabanı bağlantısı kurulamadı.");

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, tc);

        // ÖNEMLİ: ResultSet'i kullanan Controller'ın bağlantıyı yönetmesi gerekir!
        return ps.executeQuery();
    }

    // --- Diğer Helper Metotları ---

    private boolean isValidTC(String tc) {
        return tc != null && tc.matches("\\d{11}");
    }

    private boolean isValidDate(String date) {
        if (date == null || date.isEmpty()) return false;
        try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}