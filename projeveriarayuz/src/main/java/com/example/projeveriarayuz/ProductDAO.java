package com.example.projeveriarayuz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDAO {

    /**
     * Product tablosundaki tüm ürünleri ResultSet olarak döndürür.
     * Bu metot, müşterinin satın alabileceği ürün kataloğunu listelemek için kullanılır.
     * * @return ResultSet: UrunID, UrunTipi ve Aciklama sütunlarını içerir.
     */
    public ResultSet getAllProducts() {

        String sql = "SELECT UrunID, UrunTipi, Aciklama FROM Product ORDER BY UrunID";

        try {
            Connection conn = DbConnection.getConnection();
            if (conn == null) {
                System.err.println("❌ Veritabanı bağlantısı kurulamadı!");
                return null;
            }

            PreparedStatement ps = conn.prepareStatement(sql);
            // ResultSet'i döndürür. Bu ResultSet, çağıran metot tarafından kapatılmalıdır.
            return ps.executeQuery();

        } catch (SQLException e) {
            System.err.println("❌ Ürünleri Getirme Hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
