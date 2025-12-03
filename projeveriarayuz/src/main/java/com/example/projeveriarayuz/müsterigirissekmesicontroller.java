package com.example.projeveriarayuz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class müsterigirissekmesicontroller {

    // FXML'deki name alanı artık Soyad girişi için kullanılacak
    @FXML private TextField name;
    // FXML'deki tcField alanı artık TC Kimlik No girişi için kullanılacak
    @FXML private PasswordField tcField;

    // --- Giriş Kontrol Metodu (Veritabanı kontrolünü yapar) ---
    public boolean musteriGirisKontrol(String tc, String soyad) {
        CustomerDAO dao = new CustomerDAO();

        // ResultSet'i try-with-resources ile kapatıyoruz
        try (ResultSet rs = dao.musteriGetir(tc)) {

            if (rs != null && rs.next()) {
                // Veritabanındaki Soyad bilgisini al
                String dbSoyad = rs.getString("Soyad");

                // Girilen Soyad ile veritabanındaki Soyadı karşılaştır
                if (dbSoyad != null && dbSoyad.equalsIgnoreCase(soyad)) {
                    return true; // TC ve Soyad eşleşti
                }
            }
        } catch (SQLException e) {
            System.err.println("Giriş Kontrolü SQL Hatası: " + e.getMessage());
            // Bağlantı veya sorgu hatası durumunda girişi başarısız say
            showAlert(Alert.AlertType.ERROR, "Veritabanı Hatası", "Giriş sırasında sunucu hatası oluştu.");
        }
        return false; // TC bulunamadı veya Soyad eşleşmedi
    }


    @FXML
    public void getmusterianaekran(ActionEvent event) {

        String soyad = name.getText().trim();
        String tcNo = tcField.getText().trim();

        if (soyad.isEmpty() || tcNo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Giriş Hatası", "Lütfen tüm alanları doldurun.");
            return;
        }


        if (!tcNo.matches("\\d{11}")) {
            showAlert(Alert.AlertType.ERROR, "Geçersiz TC", "TC Kimlik No 11 haneli sayı olmalıdır!");
            tcField.requestFocus(); // Hatalı alana odaklan
            return;
        }

        if (musteriGirisKontrol(tcNo, soyad)) {

            // --- Başarılı Girişten Sonra Ana Ekrana Yönlendirme ---
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("müsterianaekran.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Müşteri Ana Ekranı");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Hata", "Ana ekran yüklenirken bir sorun oluştu.");
            }

        } else {
            showAlert(Alert.AlertType.ERROR, "Giriş Başarısız", "Girilen TC Kimlik No veya Soyad hatalı.");
        }
    }

    @FXML
    public void getkayitsekmesi(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("kayıtekranı.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Yeni Müşteri Kayıt");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", "Kayıt ekranı yüklenirken bir sorun oluştu.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}