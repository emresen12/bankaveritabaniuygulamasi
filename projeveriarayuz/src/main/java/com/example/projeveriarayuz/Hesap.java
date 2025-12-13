package com.example.projeveriarayuz;

public class Hesap {
    private String hesapNo;
    private int musteriId;
    private String hesapTuru;
    private double bakiye;

    public Hesap(String hesapNo, int musteriId, String hesapTuru, double bakiye) {
        this.hesapNo = hesapNo;
        this.musteriId = musteriId;
        this.hesapTuru = hesapTuru;
        this.bakiye = bakiye;
    }


    public String getHesapNo() { return hesapNo; }
    public void setHesapNo(String hesapNo) { this.hesapNo = hesapNo; }

    public int getMusteriId() { return musteriId; }
    public void setMusteriId(int musteriId) { this.musteriId = musteriId; }

    public String getHesapTuru() { return hesapTuru; }
    public void setHesapTuru(String hesapTuru) { this.hesapTuru = hesapTuru; }

    public double getBakiye() { return bakiye; }
    public void setBakiye(double bakiye) { this.bakiye = bakiye; }
}