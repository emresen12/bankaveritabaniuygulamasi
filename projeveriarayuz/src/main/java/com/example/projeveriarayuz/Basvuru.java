// Dosya: Basvuru.java
package com.example.projeveriarayuz;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;

public class Basvuru {


    private final IntegerProperty basvuruID;
    private final StringProperty urunAdi;

    // BasvuruTarihi (SQL tablosundaki DATETIME)
    private final StringProperty basvuruTarihi;

    // BasvuruDurumu (SQL tablosundaki NVARCHAR(20))
    private final StringProperty basvuruDurumu;

    public Basvuru(int basvuruID, String urunAdi, String basvuruTarihi, String basvuruDurumu) {
        this.basvuruID = new SimpleIntegerProperty(basvuruID);
        this.urunAdi = new SimpleStringProperty(urunAdi);
        this.basvuruTarihi = new SimpleStringProperty(basvuruTarihi);
        this.basvuruDurumu = new SimpleStringProperty(basvuruDurumu);
    }

    // Getter/Setter'lar (TableView için sadece Property metotları yeterlidir)

    public IntegerProperty basvuruIDProperty() { return basvuruID; }
    public StringProperty urunAdiProperty() { return urunAdi; }
    public StringProperty basvuruTarihiProperty() { return basvuruTarihi; }
    public StringProperty basvuruDurumuProperty() { return basvuruDurumu; }

    // Eğer butona bağlamak isterseniz String olarak değeri döndüren getter'lar
    public String getUrunAdi() { return urunAdi.get(); }
    public String getBasvuruDurumu() { return basvuruDurumu.get(); }
}