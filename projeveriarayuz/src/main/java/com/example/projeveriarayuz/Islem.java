package com.example.projeveriarayuz;

import java.sql.Timestamp;

public class Islem {
    private int islemId;
    private String kaynakHesapNo; // Nullable
    private String hedefHesapNo;  // Nullable
    private Integer kartId;       // Nullable (int yerine Integer kullandık çünkü null olabilir)
    private Integer krediId;      // Nullable
    private String islemTuru;
    private double islemTutari;
    private Timestamp islemTarihi;

    // Boş Constructor
    public Islem() {}

    // Dolu Constructor
    public Islem(String kaynakHesapNo, String hedefHesapNo, Integer kartId, Integer krediId, String islemTuru, double islemTutari) {
        this.kaynakHesapNo = kaynakHesapNo;
        this.hedefHesapNo = hedefHesapNo;
        this.kartId = kartId;
        this.krediId = krediId;
        this.islemTuru = islemTuru;
        this.islemTutari = islemTutari;
    }

    // Getter ve Setterlar
    public int getIslemId() { return islemId; }
    public void setIslemId(int islemId) { this.islemId = islemId; }

    public String getKaynakHesapNo() { return kaynakHesapNo; }
    public void setKaynakHesapNo(String kaynakHesapNo) { this.kaynakHesapNo = kaynakHesapNo; }

    public String getHedefHesapNo() { return hedefHesapNo; }
    public void setHedefHesapNo(String hedefHesapNo) { this.hedefHesapNo = hedefHesapNo; }

    public Integer getKartId() { return kartId; }
    public void setKartId(Integer kartId) { this.kartId = kartId; }

    public Integer getKrediId() { return krediId; }
    public void setKrediId(Integer krediId) { this.krediId = krediId; }

    public String getIslemTuru() { return islemTuru; }
    public void setIslemTuru(String islemTuru) { this.islemTuru = islemTuru; }

    public double getIslemTutari() { return islemTutari; }
    public void setIslemTutari(double islemTutari) { this.islemTutari = islemTutari; }

    public Timestamp getIslemTarihi() { return islemTarihi; }
    public void setIslemTarihi(Timestamp islemTarihi) { this.islemTarihi = islemTarihi; }
}