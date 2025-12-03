package com.example.projeveriarayuz;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;


public class ProductModel {
    private final SimpleIntegerProperty urunID;
    private final SimpleStringProperty urunTipi;
    private final SimpleStringProperty aciklama;

    public ProductModel(int urunID, String urunTipi, String aciklama) {
        this.urunID = new SimpleIntegerProperty(urunID);
        this.urunTipi = new SimpleStringProperty(urunTipi);
        this.aciklama = new SimpleStringProperty(aciklama);
    }

    public int getUrunID() { return urunID.get(); }
    public String getUrunTipi() { return urunTipi.get(); }
    public String getAciklama() { return aciklama.get(); }

    @Override
    public String toString() {
        return getUrunTipi() + " - " + getAciklama().substring(0, Math.min(getAciklama().length(), 25)) + "...";
    }
}