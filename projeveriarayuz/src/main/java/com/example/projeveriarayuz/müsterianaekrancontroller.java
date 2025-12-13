package com.example.projeveriarayuz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class müsterianaekrancontroller {

    // FXML Labels
    @FXML private Label bakiyeLabel;
    @FXML private Label isimLabel;
    @FXML private Label tcLabel;

    // FXML Buttons/ImageViews
    @FXML private Button ödemeİşlemleriButton;
    @FXML private Button paraTransferiButton;
    @FXML private Button sigortalarımButton;
    @FXML private Button hesaplarımButton;
    @FXML private Button ürünlerButton;

    @FXML private ImageView imgOdeme;
    @FXML private ImageView imgTransfer;
    @FXML private ImageView imgSigorta;
    @FXML private ImageView imgHesap;
    @FXML private ImageView imgUrun;

    public void setKullaniciVerileri(int musteriId, String tc, String isim) {
        System.out.println("Ana Ekrana Ulaşıldı.");
        updateScreenData();
    }

    @FXML
    public void initialize() {
        bindImageToButton(imgOdeme, ödemeİşlemleriButton);
        bindImageToButton(imgTransfer, paraTransferiButton);
        bindImageToButton(imgSigorta, sigortalarımButton);
        bindImageToButton(imgHesap, hesaplarımButton);
        bindImageToButton(imgUrun, ürünlerButton);

        updateScreenData();
    }

    private void updateScreenData() {
        int activeMusteriId = AppSession.getActiveMusteriId(); // ID'yi AppSession'dan al

        if (activeMusteriId > 0) {
            isimLabel.setText(AppSession.getKullaniciIsim());
            tcLabel.setText(AppSession.getKullaniciTC());

            // 1. TOPLAM BAKİYEYİ HESAPLA VE YAZ
            double toplamBakiye = toplamBakiyeyiGetir(activeMusteriId);
            bakiyeLabel.setText(String.format("%.2f TL", toplamBakiye));

            // 2. GİRİŞ LOGU OLUŞTUR
            girisLogunuKaydet(activeMusteriId);

        } else {
            isimLabel.setText("Giriş Yapılmadı");
            tcLabel.setText("-");
            bakiyeLabel.setText("0.00 TL");
        }
    }

    // --- VERİTABANI: TOPLAM BAKİYE HESAPLAMA ---
    private double toplamBakiyeyiGetir(int musteriId) {
        double toplam = 0.0;
        // Hesaplar tablosundaki Bakiye sütununu toplar
        String sql = "SELECT SUM(Bakiye) as ToplamBakiye FROM Hesaplar WHERE MusteriID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, musteriId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                toplam = rs.getDouble("ToplamBakiye");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Bakiye hesaplanırken hata oluştu.");
        }
        return toplam;
    }

    // --- VERİTABANI: LOG KAYDI ---
    private void girisLogunuKaydet(int musteriId) {
        // KullaniciLog tablosuna kayıt atar. GETDATE() SQL Server'ın o anki zamanıdır.
        String sql = "INSERT INTO KullaniciLog (MusteriID, IslemTipi, IPAdres, Tarih) VALUES (?, ?, ?, GETDATE())";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, musteriId);
            ps.setString(2, "Ana Ekrana Giriş");
            ps.setString(3, "Desktop App"); // IP Adresi yerine sabit değer veya localhost yazabiliriz

            ps.executeUpdate();
            System.out.println("Giriş logu veritabanına işlendi.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Log kaydedilirken hata oluştu. (Tablo 'KullaniciLog' var mı?)");
        }
    }

    public void bindImageToButton(ImageView imageView, Button button) {
        if (imageView != null && button != null) {
            imageView.setOnMouseClicked(e -> button.fire());
        }
    }

    public void getMüsteriÜrünler(ActionEvent event) throws IOException{
        int currentId = AppSession.getActiveMusteriId();

        if (currentId <= 0) {
            System.err.println("Müşteri ID aktarılmadı. İşlem iptal edildi.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("MüsteriÜrünler.fxml"));
        Parent root = loader.load();
        MusteriUrunlerController urunlerController = loader.getController();
        urunlerController.setMusteriId(currentId);

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void getmusteriparacekme(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hesaplarim.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void getmusteriparayatırma(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MüsteriParaTransferi.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void getOdemeİslemleri(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MusteriOdemeislemleri.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    public void getsigortalarim(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sigortalarim.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    public void handleLogout(ActionEvent event) throws IOException {
        AppSession.clearSession(); // Oturumu temizle
        FXMLLoader loader = new FXMLLoader(getClass().getResource("müsterigirissekmesi.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Müşteri Girişi");
        stage.show();
    }
}