package com.example.projeveriarayuz;

public class Product {
    private int urunId;
    private String urunAdi;
    private String urunTipi;
    private String aciklama;

    public Product(int urunId, String urunAdi, String urunTipi, String aciklama) {
        this.urunId = urunId;
        this.urunAdi = urunAdi;
        this.urunTipi = urunTipi;
        this.aciklama = aciklama;
    }

    // ComboBox'ta ürünün adının düzgün görünmesi için bu metot şarttır
    @Override
    public String toString() {
        return urunAdi + " (" + urunTipi + ")";
    }

    public int getUrunId() { return urunId; }
    public String getUrunAdi() { return urunAdi; }
    public String getUrunTipi() { return urunTipi; }
    public String getAciklama() { return aciklama; }
}