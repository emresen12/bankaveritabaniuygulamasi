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

public class KayitliKisiyeTransferController implements Initializable {

    @FXML private ComboBox<String> cmbKayitliKisiler;
    @FXML private ComboBox<String> cmbKaynakHesap;
    @FXML private TextField txtHedefHesap;
    @FXML private TextField txtTutar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        kaynakHesaplariGetir();
        kayitliKisileriGetir();
        cmbKayitliKisiler.setOnAction(e -> {
            String secilenIsim = cmbKayitliKisiler.getValue();
            if(secilenIsim != null) hedefHesabiBul(secilenIsim);
        });
    }

    private void kaynakHesaplariGetir() {
        String sql = "SELECT HesapNo FROM Hesaplar WHERE MusteriID = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, AppSession.getActiveMusteriId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) cmbKaynakHesap.getItems().add(rs.getString("HesapNo"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void kayitliKisileriGetir() {
        String sql = "SELECT Ad, Soyad FROM Musteri WHERE MusteriID != ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, AppSession.getActiveMusteriId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cmbKayitliKisiler.getItems().add(rs.getString("Ad") + " " + rs.getString("Soyad"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void hedefHesabiBul(String adSoyad) {
        String[] parca = adSoyad.split(" ");
        String ad = parca[0];
        String sql = "SELECT TOP 1 h.HesapNo FROM Hesaplar h JOIN Musteri m ON h.MusteriID = m.MusteriID WHERE m.Ad = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ad);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) txtHedefHesap.setText(rs.getString("HesapNo"));
            else txtHedefHesap.setText("Hesap Bulunamadı");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void transferYap(ActionEvent event) {
        String kaynak = cmbKaynakHesap.getValue();
        String hedef = txtHedefHesap.getText();
        String tutarStr = txtTutar.getText();
        String adSoyad = cmbKayitliKisiler.getValue();

        if (kaynak == null || hedef == null || hedef.equals("Hesap Bulunamadı") || tutarStr.isEmpty() || adSoyad == null) {
            showAlert("Hata", "Eksik bilgi.");
            return;
        }

        if (kaynak.equals(hedef)) {
            showAlert("Hata", "Gönderen ve Alıcı hesap aynı olamaz.");
            return;
        }

        try {
            double tutar = Double.parseDouble(tutarStr);
            if (checkBakiye(kaynak, tutar)) {
                performTransfer(kaynak, hedef, tutar);
                showAlert("Başarılı", adSoyad + " adlı kişiye " + tutar + " TL transfer yapıldı.");
                geriDon(event);
            } else {
                showAlert("Yetersiz Bakiye", "Bakiye yetersiz.");
            }
        } catch (Exception e) { e.printStackTrace(); }
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
            try (PreparedStatement ps = conn.prepareStatement(sqlDus)) {
                ps.setDouble(1, tutar);
                ps.setString(2, kaynak);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlEkle)) {
                ps.setDouble(1, tutar);
                ps.setString(2, hedef);
                ps.executeUpdate();
            }
            IslemDAO.islemKaydet(kaynak, hedef, null, null, "Kayıtlı Kişiye Transfer", tutar);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void geriDon(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MüsteriParaTransferi.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setContentText(m); a.show();
    }
}