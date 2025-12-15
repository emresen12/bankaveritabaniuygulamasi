package com.example.projeveriarayuz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class Kart {
    private int kartId;
    private int musteriId;
    private int urunId;
    private Integer basvuruId;
    private String hesapNo;
    private String kartNumarasi;
    private String kartTipi;
    private Date sonKullanmaTarihi;
    private int cvc;
    private double limit;
    private double guncelBorc;

    public Kart() {}

    public static List<Kart> getMusteriKartlari(int musteriId) {
        List<Kart> kartlar = new ArrayList<>();
        String sql = "SELECT * FROM Kartlar WHERE MusteriID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, musteriId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Kart k = new Kart();
                k.setKartId(rs.getInt("KartID"));
                k.setMusteriId(rs.getInt("MusteriID"));
                k.setHesapNo(rs.getString("HesapNo"));
                k.setKartNumarasi(rs.getString("KartNumarasi"));
                k.setKartTipi(rs.getString("KartTipi"));
                k.setSonKullanmaTarihi(rs.getDate("SonKullanmaTarihi"));
                k.setCvc(rs.getInt("CVC"));
                k.setLimit(rs.getDouble("Limit"));
                k.setGuncelBorc(rs.getDouble("GuncelBorc"));
                kartlar.add(k);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kartlar;
    }

    // Getter ve Setterlar...
    public int getKartId() { return kartId; }
    public void setKartId(int kartId) { this.kartId = kartId; }
    public int getMusteriId() { return musteriId; }
    public void setMusteriId(int musteriId) { this.musteriId = musteriId; }
    public int getUrunId() { return urunId; }
    public void setUrunId(int urunId) { this.urunId = urunId; }
    public Integer getBasvuruId() { return basvuruId; }
    public void setBasvuruId(Integer basvuruId) { this.basvuruId = basvuruId; }
    public String getHesapNo() { return hesapNo; }
    public void setHesapNo(String hesapNo) { this.hesapNo = hesapNo; }
    public String getKartNumarasi() { return kartNumarasi; }
    public void setKartNumarasi(String kartNumarasi) { this.kartNumarasi = kartNumarasi; }
    public String getKartTipi() { return kartTipi; }
    public void setKartTipi(String kartTipi) { this.kartTipi = kartTipi; }
    public Date getSonKullanmaTarihi() { return sonKullanmaTarihi; }
    public void setSonKullanmaTarihi(Date sonKullanmaTarihi) { this.sonKullanmaTarihi = sonKullanmaTarihi; }
    public int getCvc() { return cvc; }
    public void setCvc(int cvc) { this.cvc = cvc; }
    public double getLimit() { return limit; }
    public void setLimit(double limit) { this.limit = limit; }
    public double getGuncelBorc() { return guncelBorc; }
    public void setGuncelBorc(double guncelBorc) { this.guncelBorc = guncelBorc; }
}