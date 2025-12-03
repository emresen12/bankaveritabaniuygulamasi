package com.example.projeveriarayuz;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CustomerDAO {


    public boolean musteriEkle(String tc, String ad, String soyad, String dogumTarihi,
                               String telefon, int krediPuani) {

        if (!isValidTC(tc)) {
            System.out.println("✗ Geçersiz TC: 11 haneli olmalı!");
            return false;
        }

        if (dogumTarihi != null && !dogumTarihi.isEmpty() && !isValidDate(dogumTarihi)) {
            System.out.println("✗ Geçersiz Doğum Tarihi formatı! YYYY-MM-DD olmalı (örn: 1990-05-15)");
            return false;
        }


        if (musteriVarMi(tc)) {
            System.out.println("✗ Bu TC Kimlik No zaten kayıtlı!");
            return false;
        }

        String sql = "INSERT INTO Musteri (TC_KimlikNo, Ad, Soyad, DogumTarihi, Telefon, KrediPuani) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("✗ Veritabanı bağlantısı kurulamadı!");
                return false;
            }

            // 1, 2, 3: Temel Alanlar
            ps.setString(1, tc);
            ps.setString(2, ad);
            ps.setString(3, soyad);

            // 4: Doğum Tarihi (Nullable)
            if (dogumTarihi != null && !dogumTarihi.isEmpty()) {
                ps.setDate(4, Date.valueOf(dogumTarihi));
            } else {
                ps.setNull(4, Types.DATE);
            }

            // 5: Telefon (Nullable)
            if (telefon != null && !telefon.isEmpty()) {
                ps.setString(5, telefon);
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            // 6: Kredi Puanı (Zorunlu)
            ps.setInt(6, krediPuani);


            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✓ Müşteri başarıyla kaydedildi: " + ad + " " + soyad);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("✗ SQL Hatası: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // --- Diğer DAO metodları (musteriVarMi, musteriGetir, tumMusterileriGetir) aynı kalabilir ---

    // TC Kimlik No ile müşteri var mı kontrol et
    public boolean musteriVarMi(String tc) {
        String sql = "SELECT COUNT(*) FROM Musteri WHERE TC_KimlikNo = ?";
        // ... (Kalan kod aynı) ...
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, tc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Kontrol hatası: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // TC ile müşteri bilgisi getir
    public ResultSet musteriGetir(String tc) {
        String sql = "SELECT * FROM Musteri WHERE TC_KimlikNo = ?";
        // ... (Kalan kod aynı) ...
        try {
            Connection conn = DbConnection.getConnection();
            if (conn == null) return null;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, tc);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.err.println("Müşteri getirme hatası: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Tüm müşterileri getir
    public ResultSet tumMusterileriGetir() {
        String sql = "SELECT TOP 1000 MusteriID, TC_KimlikNo, Ad, Soyad, DogumTarihi, " +
                "MusteriOlmaTarihi, Telefon, KrediPuani FROM Musteri ORDER BY MusteriID DESC";
        // ... (Kalan kod aynı) ...
        try {
            Connection conn = DbConnection.getConnection();
            if (conn == null) return null;
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("Müşteri listeleme hatası: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


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