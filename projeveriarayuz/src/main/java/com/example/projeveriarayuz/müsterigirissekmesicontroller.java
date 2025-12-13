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

    @FXML private TextField name; // Soyad girişi için
    @FXML private PasswordField tcField; // TC Kimlik No girişi için

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
            tcField.requestFocus();
            return;
        }

        // CustomerDAO örneği, sizin veritabanı bağlantı sınıfınız olmalı
        CustomerDAO dao = new CustomerDAO();
        int musteriId = -1;
        String ad = null;
        boolean girisBasarili = false;

        try (ResultSet rs = dao.musteriGetir(tcNo)) {
            if (rs != null && rs.next()) {
                String dbSoyad = rs.getString("Soyad");

                if (dbSoyad != null && dbSoyad.equalsIgnoreCase(soyad)) {
                    musteriId = rs.getInt("MusteriID");
                    ad = rs.getString("Ad");
                    girisBasarili = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Giriş Kontrolü SQL Hatası: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Veritabanı Hatası", "Giriş sırasında sunucu hatası oluştu.");
            return;
        }

        if (girisBasarili && musteriId > 0) { // Giriş başarılı

            String tamAdSoyad = ad + " " + soyad;
            AppSession.setSession(musteriId, tcNo, tamAdSoyad);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("müsterianaekran.fxml"));
                Parent root = loader.load();

                // Artık anaEkranController.setKullaniciVerileri() metoduna gerek kalmadı
                // Çünkü Ana Ekran Controller verileri initialize'da AppSession'dan çekecek.

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("kayitekrani.fxml"));
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