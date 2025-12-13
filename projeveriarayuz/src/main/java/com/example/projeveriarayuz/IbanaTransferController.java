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

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class IbanaTransferController implements Initializable {

    @FXML private ComboBox<String> cmbKaynakHesap;
    @FXML private TextField txtHedefIban;
    @FXML private TextField txtTutar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hesaplariDoldur();
    }

    private void hesaplariDoldur() {
        String sql = "SELECT HesapNo FROM Hesaplar WHERE MusteriID = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, AppSession.getActiveMusteriId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) cmbKaynakHesap.getItems().add(rs.getString("HesapNo"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void transferYap(ActionEvent event) {
        String kaynak = cmbKaynakHesap.getValue();
        String hedef = txtHedefIban.getText();
        String tutarStr = txtTutar.getText();

        if (kaynak == null || hedef.isEmpty() || tutarStr.isEmpty()) {
            showAlert("Hata", "Lütfen tüm alanları doldurun.");
            return;
        }

        if(kaynak.equals(hedef)) {
            showAlert("Hata", "Kendi hesabınıza bu menüden gönderemezsiniz.");
            return;
        }

        try {
            double tutar = Double.parseDouble(tutarStr);
            if (checkBakiye(kaynak, tutar)) {
                performTransfer(kaynak, hedef, tutar);
                showAlert("Başarılı", "Transfer başarıyla gerçekleşti.");
                geriDon(event);
            } else {
                showAlert("Yetersiz Bakiye", "Bakiye yetersiz.");
            }
        } catch (Exception e) { showAlert("Hata", "Geçersiz tutar."); }
    }

    private boolean checkBakiye(String hesapNo, double tutar) {
        String sql = "SELECT Bakiye FROM Hesaplar WHERE HesapNo = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hesapNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("Bakiye") >= tutar;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private void performTransfer(String kaynak, String hedef, double tutar) {
        String sqlDus = "UPDATE Hesaplar SET Bakiye = Bakiye - ? WHERE HesapNo = ?";
        String sqlEkle = "UPDATE Hesaplar SET Bakiye = Bakiye + ? WHERE HesapNo = ?";

        try (Connection conn = DbConnection.getConnection()) {
            // Gönderenden düş
            try (PreparedStatement ps = conn.prepareStatement(sqlDus)) {
                ps.setDouble(1, tutar);
                ps.setString(2, kaynak);
                ps.executeUpdate();
            }
            // Alıcıya ekle
            try (PreparedStatement ps = conn.prepareStatement(sqlEkle)) {
                ps.setDouble(1, tutar);
                ps.setString(2, hedef);
                ps.executeUpdate();
            }
            // IslemDAO ile Logla
            IslemDAO.islemKaydet(kaynak, hedef, null, null, "IBAN Para Transferi", tutar);

        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void geriDon(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MüsteriParaTransferi.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(title); a.setContentText(msg); a.show();
    }
}