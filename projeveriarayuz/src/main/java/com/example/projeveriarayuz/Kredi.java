package com.example.projeveriarayuz;

public class Kredi {
    private int krediId;
    private int musteriId;
    private int basvuruId;
    private int urunId;
    private double anaPara;
    private double kalanBorc;

    public Kredi() {}

    // Getter ve Setterlar
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
}