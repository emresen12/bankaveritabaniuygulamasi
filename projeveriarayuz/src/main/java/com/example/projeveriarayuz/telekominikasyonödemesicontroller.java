package com.example.projeveriarayuz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class telekominikasyonödemesicontroller implements Initializable {

    @FXML private TextField txtTelefonNo;
    @FXML private TextField txtTutar;
    @FXML private ComboBox<String> cmbHesaplar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hesaplariGetir();
    }

    private void hesaplariGetir() {
        String sql = "SELECT HesapNo FROM Hesaplar WHERE MusteriID = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, AppSession.getActiveMusteriId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cmbHesaplar.getItems().add(rs.getString("HesapNo"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void odemeYap(ActionEvent event) {
        String secilenHesap = cmbHesaplar.getValue();
        String tutarStr = txtTutar.getText();

        if (secilenHesap == null || tutarStr.isEmpty()) {
            showAlert("Hata", "Eksik bilgi.");
            return;
        }

        double tutar;
        try { tutar = Double.parseDouble(tutarStr); }
        catch (NumberFormatException e) { showAlert("Hata", "Geçersiz Tutar"); return; }

        if (bakiyeYeterliMi(secilenHesap, tutar)) {
            // YENİ EKLENEN KISIM
            odemeIsleminiTamamla(secilenHesap, tutar, "Telekomünikasyon Ödemesi");

            showAlert("Başarılı", "Ödeme tamamlandı ve işlemlere kaydedildi.");
            try { getodemeler(event); } catch (IOException e) { e.printStackTrace(); }
        } else {
            showAlert("Yetersiz Bakiye", "Bakiye yetersiz.");
        }
    }

    private boolean bakiyeYeterliMi(String hesapNo, double tutar) {
        String sql = "SELECT Bakiye FROM Hesaplar WHERE HesapNo = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hesapNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("Bakiye") >= tutar;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // --- BURASI DEĞİŞTİ ---
    private void odemeIsleminiTamamla(String hesapNo, double tutar, String aciklama) {
        String updateSql = "UPDATE Hesaplar SET Bakiye = Bakiye - ? WHERE HesapNo = ?";
        String insertSql = "INSERT INTO Islemler (KaynakHesapNo, HedefHesapNo, KartID, KrediID, IslemTuru, IslemTutari) VALUES (?, NULL, NULL, NULL, ?, ?)";

        try (Connection conn = DbConnection.getConnection()) {
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setDouble(1, tutar);
                psUpdate.setString(2, hesapNo);
                psUpdate.executeUpdate();
            }
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setString(1, hesapNo);
                psInsert.setString(2, aciklama);
                psInsert.setDouble(3, tutar);
                psInsert.executeUpdate();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void getodemeler(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MusteriOdemeislemleri.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}