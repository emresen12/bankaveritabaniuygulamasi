package com.example.projeveriarayuz;

public class Kart {
    private int kartId;
    private int musteriId;
    private int urunId;
    private String hesapNo;     // Nullable (Sadece banka kartlarında dolu olur)
    private Integer basvuruId;  // Nullable
    private String kartNumarasi;
    private String kartTipi;    // "Kredi Kartı" veya "Banka Kartı"

    public Kart() {}

    // Getter ve Setterlar
    public int getKartId() { return kartId; }
    public void setKartId(int kartId) { this.kartId = kartId; }

    public int getMusteriId() { return musteriId; }
    public void setMusteriId(int musteriId) { this.musteriId = musteriId; }

    public int getUrunId() { return urunId; }
    public void setUrunId(int urunId) { this.urunId = urunId; }

    public String getHesapNo() { return hesapNo; }
    public void setHesapNo(String hesapNo) { this.hesapNo = hesapNo; }

    public Integer getBasvuruId() { return basvuruId; }
    public void setBasvuruId(Integer basvuruId) { this.basvuruId = basvuruId; }

    public String getKartNumarasi() { return kartNumarasi; }
    public void setKartNumarasi(String kartNumarasi) { this.kartNumarasi = kartNumarasi; }

    public String getKartTipi() { return kartTipi; }
    public void setKartTipi(String kartTipi) { this.kartTipi = kartTipi; }
}