package com.example.projeveriarayuz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class KayıtController {

    @FXML private TextField tckNoField;
    @FXML private TextField adField;
    @FXML private TextField soyadField;
    @FXML private TextField phoneField;
    @FXML private TextField dogumTarihiField;

    // ❗ KrediPuanı alanı FXML'den kaldırıldığı için buradaki referans da kaldırıldı.
    // @FXML private TextField krediPuaniField;


    @FXML
    public void initialize() {
        try {
            if (DbConnection.getConnection() != null) {
                System.out.println("✓ Kayıt sayfası hazır - Veritabanı bağlantısı OK");
            } else {
                System.err.println("✗ UYARI: Veritabanı bağlantısı kurulamadı (Connection object is null)!");
            }
        } catch (SQLException e) {
            System.err.println("✗ HATA: Veritabanı bağlantısı kurulurken SQL hatası oluştu: " + e.getMessage());
        }
    }

    private void goToLoginPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("müsterigirissekmesi.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", "Giriş ekranı yüklenirken bir sorun oluştu.");
        }
    }

    @FXML
    public void geriDon(ActionEvent event) {
        goToLoginPage(event);
    }

    @FXML
    public void kayitTamamla(ActionEvent event) {


        String tc = tckNoField.getText().trim();
        String ad = adField.getText().trim();
        String soyad = soyadField.getText().trim();
        String telefon = phoneField.getText().trim();
        String dogumTarihi = dogumTarihiField.getText().trim();

        // ❗ Kredi puanı okuma ve işleme kodları kaldırıldı.


        // Zorunlu alanlar (Kredi Puanı artık zorunlu değil)
        if (tc.isEmpty() || ad.isEmpty() || soyad.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Eksik Bilgi",
                    "TC Kimlik No, Ad ve Soyad alanları zorunludur!");
            return;
        }


        // TC format kontrolü
        if (!tc.matches("\\d{11}")) {
            showAlert(Alert.AlertType.ERROR, "Geçersiz TC",
                    "TC Kimlik No 11 haneli sayı olmalıdır!\nÖrnek: 12345678901");
            tckNoField.requestFocus();
            return;
        }


        // Doğum Tarihi format kontrolü
        if (!dogumTarihi.isEmpty() && !dogumTarihi.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showAlert(Alert.AlertType.ERROR, "Geçersiz Tarih Formatı",
                    "Doğum tarihi YYYY-MM-DD formatında olmalıdır!\nÖrnek: 1990-05-15");
            dogumTarihiField.requestFocus();
            return;
        }


        CustomerDAO dao = new CustomerDAO();

        // ❗ musteriEkle metodunun çağrısı güncellendi: Sadece gerekli 5 parametre gönderiliyor.
        boolean success = dao.musteriEkle(tc, ad, soyad, dogumTarihi, telefon);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Başarılı",
                    "✓ Müşteri başarıyla kaydedildi!\n\n" +
                            "TC: " + tc + "\n" +
                            "Ad Soyad: " + ad + " " + soyad + "\n\n" +
                            "Artık giriş yapabilirsiniz.");


            temizle();

            goToLoginPage(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Kayıt Başarısız",
                    "Kayıt işlemi başarısız oldu!\n\n" +
                            "Olası nedenler:\n" +
                            "• Bu TC Kimlik No zaten kayıtlı\n" +
                            "• Veritabanı bağlantı/SQL hatası\n" +
                            "• Geçersiz tarih formatları\n\n" +
                            "Lütfen bilgileri kontrol edip tekrar deneyin.");
        }
    }

    private void temizle() {
        tckNoField.clear();
        adField.clear();
        soyadField.clear();
        dogumTarihiField.clear();
        phoneField.clear();

    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}