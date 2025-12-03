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
import java.sql.SQLException; // Eksik olan bu import'u eklemeliyiz.

public class KayıtController {

    @FXML private TextField tckNoField;
    @FXML private TextField adField;
    @FXML private TextField soyadField;
    @FXML private TextField phoneField;
    @FXML private TextField dogumTarihiField;

    // MusteriOlmaTarihi artık FXML'de yok, bu yüzden kaldırıldı
    // @FXML private TextField musteriOlmaTarihiField;
    @FXML private TextField krediPuaniField;


    @FXML
    public void initialize() {
        // Hata alan 30. satır burasıdır. DbConnection.getConnection() bir SQLException fırlatabilir.
        try {
            if (DbConnection.getConnection() != null) {
                System.out.println("✓ Kayıt sayfası hazır - Veritabanı bağlantısı OK");
            } else {
                // Eğer bağlantı null dönerse (getConnection'da hata yakalanıp null dönülüyorsa)
                System.err.println("✗ UYARI: Veritabanı bağlantısı kurulamadı (Connection object is null)!");
            }
        } catch (SQLException e) {
            // Eğer getConnection() metodunda bir SQLException fırlatılırsa, burada yakalanır.
            System.err.println("✗ HATA: Veritabanı bağlantısı kurulurken SQL hatası oluştu: " + e.getMessage());
            // showAlert(Alert.AlertType.ERROR, "Kritik Hata", "Veritabanı bağlantısı kurulamadı: " + e.getMessage());
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

        String krediPuaniStr = krediPuaniField.getText().trim();


        if (tc.isEmpty() || ad.isEmpty() || soyad.isEmpty() || krediPuaniStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Eksik Bilgi",
                    "TC Kimlik No, Ad, Soyad ve Kredi Puanı alanları zorunludur!");
            return;
        }


        if (!tc.matches("\\d{11}")) {
            showAlert(Alert.AlertType.ERROR, "Geçersiz TC",
                    "TC Kimlik No 11 haneli sayı olmalıdır!\nÖrnek: 12345678901");
            tckNoField.requestFocus();
            return;
        }


        if (!dogumTarihi.isEmpty() && !dogumTarihi.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showAlert(Alert.AlertType.ERROR, "Geçersiz Tarih Formatı",
                    "Doğum tarihi YYYY-MM-DD formatında olmalıdır!\nÖrnek: 1990-05-15");
            dogumTarihiField.requestFocus();
            return;
        }


        if (!krediPuaniStr.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Geçersiz Kredi Puanı",
                    "Kredi Puanı sadece sayı olmalıdır!");
            krediPuaniField.requestFocus();
            return;
        }

        int krediPuani;
        try {
            krediPuani = Integer.parseInt(krediPuaniStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Kredi Puanı geçersiz bir sayı formatıdır.");
            return;
        }


        CustomerDAO dao = new CustomerDAO();


        boolean success = dao.musteriEkle(tc, ad, soyad, dogumTarihi, telefon, krediPuani);

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

        krediPuaniField.clear();
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}