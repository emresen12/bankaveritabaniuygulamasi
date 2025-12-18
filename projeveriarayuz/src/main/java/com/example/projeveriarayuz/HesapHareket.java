// Dosya: HesapHareket.java
package com.example.projeveriarayuz;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.DoubleProperty;

public class HesapHareket {

    // İşlem Türü (IslemTuru - NVARCHAR)
    private final StringProperty islemTuru;

    // İşlem Tutarı (IslemTutari - DECIMAL)
    private final DoubleProperty islemTutari;

    // İşlem Tarihi (IslemTarihi - DATETIME)
    private final StringProperty islemTarihi;

    public HesapHareket(String islemTuru, double islemTutari, String islemTarihi) {
        this.islemTuru = new SimpleStringProperty(islemTuru);
        this.islemTutari = new SimpleDoubleProperty(islemTutari);
        this.islemTarihi = new SimpleStringProperty(islemTarihi);
    }

    /* ===== Property Metotları (TableView için yeterlidir) ===== */

    public StringProperty islemTuruProperty() {
        return islemTuru;
    }

    public DoubleProperty islemTutariProperty() {
        return islemTutari;
    }

    public StringProperty islemTarihiProperty() {
        return islemTarihi;
    }

    /* ===== Opsiyonel Getter'lar (buton, filtre, kontrol için) ===== */

    public String getIslemTuru() {
        return islemTuru.get();
    }

    public double getIslemTutari() {
        return islemTutari.get();
    }

    public String getIslemTarihi() {
        return islemTarihi.get();
    }
}
