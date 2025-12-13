package com.example.projeveriarayuz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class IslemDAO {

    /**
     * İşlemler tablosuna tek satırda kayıt ekler.
     * Parametreler: (KaynakHesap, HedefHesap, KartID, KrediID, Açıklama, Tutar)
     */
    public static boolean islemKaydet(String kaynakHesap, String hedefHesap, Integer kartId, Integer krediId, String tur, double tutar) {
        String sql = "INSERT INTO Islemler (KaynakHesapNo, HedefHesapNo, KartID, KrediID, IslemTuru, IslemTutari) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (kaynakHesap != null) ps.setString(1, kaynakHesap);
            else ps.setNull(1, Types.VARCHAR);

            if (hedefHesap != null) ps.setString(2, hedefHesap);
            else ps.setNull(2, Types.VARCHAR);

            if (kartId != null) ps.setInt(3, kartId);
            else ps.setNull(3, Types.INTEGER);

            if (krediId != null) ps.setInt(4, krediId);
            else ps.setNull(4, Types.INTEGER);

            ps.setString(5, tur);
            ps.setDouble(6, tutar);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}