package com.example.projeveriarayuz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Kredi {
    private int krediId;
    private int musteriId;
    private int basvuruId;
    private int urunId;
    private double anaPara;
    private double kalanBorc;
    private int vadeSayisi;
    private double faizOrani;
    private String hesapNo;

    public Kredi() {}

    // --- DAO METODU (STATİK) ---
    public static List<Kredi> getMusteriKredileri(int musteriId) {
        List<Kredi> krediler = new ArrayList<>();
        String sql = "SELECT * FROM Krediler WHERE MusteriID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, musteriId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Kredi k = new Kredi();
                k.setKrediId(rs.getInt("KrediID"));
                k.setMusteriId(rs.getInt("MusteriID"));
                k.setBasvuruId(rs.getInt("BasvuruID"));
                k.setAnaPara(rs.getDouble("AnaPara"));
                k.setKalanBorc(rs.getDouble("KalanBorc"));
                k.setVadeSayisi(rs.getInt("VadeSayisi"));
                k.setFaizOrani(rs.getDouble("FaizOrani"));
                k.setHesapNo(rs.getString("HesapNo"));
                krediler.add(k);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return krediler;
    }

    // Getter ve Setterlar...
    public int getKrediId() { return krediId; }
    public void setKrediId(int krediId) { this.krediId = krediId; }
    public int getMusteriId() { return musteriId; }
    public void setMusteriId(int musteriId) { this.musteriId = musteriId; }
    public int getBasvuruId() { return basvuruId; }
    public void setBasvuruId(int basvuruId) { this.basvuruId = basvuruId; }
    public int getUrunId() { return urunId; }
    public void setUrunId(int urunId) { this.urunId = urunId; }
    public double getAnaPara() { return anaPara; }
    public void setAnaPara(double anaPara) { this.anaPara = anaPara; }
    public double getKalanBorc() { return kalanBorc; }
    public void setKalanBorc(double kalanBorc) { this.kalanBorc = kalanBorc; }
    public int getVadeSayisi() { return vadeSayisi; }
    public void setVadeSayisi(int vadeSayisi) { this.vadeSayisi = vadeSayisi; }
    public double getFaizOrani() { return faizOrani; }
    public void setFaizOrani(double faizOrani) { this.faizOrani = faizOrani; }
    public String getHesapNo() { return hesapNo; }
    public void setHesapNo(String hesapNo) { this.hesapNo = hesapNo; }
}