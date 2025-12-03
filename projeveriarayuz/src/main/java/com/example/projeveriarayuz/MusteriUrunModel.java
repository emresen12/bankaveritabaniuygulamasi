package com.example.projeveriarayuz;

import javafx.beans.property.SimpleStringProperty;

public class MusteriUrunModel {
    private final SimpleStringProperty hesapNumarasi;
    private final SimpleStringProperty urunTipi;
    private final SimpleStringProperty aciklama;
    private final SimpleStringProperty baslangicTarihi;

    // Kurucu Metot (Constructor)
    public MusteriUrunModel(String hesapNumarasi, String urunTipi, String aciklama, String baslangicTarihi) {
        this.hesapNumarasi = new SimpleStringProperty(hesapNumarasi);
        this.urunTipi = new SimpleStringProperty(urunTipi);
        this.aciklama = new SimpleStringProperty(aciklama);
        this.baslangicTarihi = new SimpleStringProperty(baslangicTarihi);
    }

    // Getter Metotları (TableView sütunları bunlarla eşleşmelidir)
    public String getHesapNumarasi() {
        return hesapNumarasi.get();
    }

    public String getUrunTipi() {
        return urunTipi.get();
    }

    public String getAciklama() {
        return aciklama.get();
    }

    public String getBaslangicTarihi() {
        return baslangicTarihi.get();
    }
}
