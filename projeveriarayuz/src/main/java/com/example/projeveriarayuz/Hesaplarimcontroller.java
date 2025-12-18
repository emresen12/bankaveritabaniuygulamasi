package com.example.projeveriarayuz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Hesaplarimcontroller implements Initializable {

    @FXML private VBox hesapListesiContainer;
    @FXML private ComboBox<String> cmbHesapTuru;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMevcutHesaplar();
        loadHesapTurleri(); // ComboBox'ı dolduran metot eklendi
    }

    // --- COMBOBOX'I VERİTABANINDAN DOLDURMA ---
    private void loadHesapTurleri() {
        String sql = "SELECT Ad FROM HesapTurleri";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            cmbHesapTuru.getItems().clear();
            while (rs.next()) {
                cmbHesapTuru.getItems().add(rs.getString("Ad"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- HESAP LİSTELEME ---
    private void loadMevcutHesaplar() {
        hesapListesiContainer.getChildren().clear();
        String sql = "SELECT HesapNo, HesapTuru, Bakiye FROM Hesaplar WHERE MusteriID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, AppSession.getActiveMusteriId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                hesapListesiContainer.getChildren().add(createHesapCard(
                        rs.getString("HesapNo"),
                        rs.getString("HesapTuru"),
                        rs.getDouble("Bakiye")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    void btnHesapOlusturClicked(ActionEvent event) {
        String secilenHesapTuru = cmbHesapTuru.getValue();

        if (secilenHesapTuru == null || secilenHesapTuru.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen bir hesap türü seçiniz.");
            return;
        }

        basvuruYap(secilenHesapTuru);
    }

    private void basvuruYap(String hesapTuruAdi) {
        int hesapTurID = findHesapTurIdByName(hesapTuruAdi);
        final int HESAP_GENEL_URUN_ID = 3;

        if (hesapTurID == -1) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Hesap türü bulunamadı.");
            return;
        }

        String kontrolSql = "SELECT COUNT(*) FROM Basvuru WHERE MusteriID = ? AND UrunID = ? AND AltUrunID = ? AND BasvuruDurumu = 'Inceleniyor'";
        String insertSql = "INSERT INTO Basvuru (MusteriID, UrunID, AltUrunID, BasvuruTarihi, BasvuruDurumu) VALUES (?, ?, ?, GETDATE(), 'Inceleniyor')";

        try (Connection conn = DbConnection.getConnection()) {
            try (PreparedStatement kps = conn.prepareStatement(kontrolSql)) {
                kps.setInt(1, AppSession.getActiveMusteriId());
                kps.setInt(2, HESAP_GENEL_URUN_ID);
                kps.setInt(3, hesapTurID);
                ResultSet rs = kps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.WARNING, "Uyarı", "Bu türde bekleyen bir başvurunuz zaten var.");
                    return;
                }
            }
            try (PreparedStatement ips = conn.prepareStatement(insertSql)) {
                ips.setInt(1, AppSession.getActiveMusteriId());
                ips.setInt(2, HESAP_GENEL_URUN_ID);
                ips.setInt(3, hesapTurID);
                if (ips.executeUpdate() > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Hesap başvurunuz alındı.");
                    loadHesapTurleri(); // Listeyi yenilemek için opsiyonel
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private int findHesapTurIdByName(String ad) {
        String sql = "SELECT HesapTurID FROM HesapTurleri WHERE Ad = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ad);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("HesapTurID");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    private List<String> getBostaOlanKartlar() {
        List<String> kartlar = new ArrayList<>();
        String sql = "SELECT KartNumarasi FROM Kartlar WHERE MusteriID = ? AND HesapNo IS NULL";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, AppSession.getActiveMusteriId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) kartlar.add(rs.getString("KartNumarasi"));
        } catch (SQLException e) { e.printStackTrace(); }
        return kartlar;
    }

    private boolean kartHesabaBagla(String kartNo, String hesapNo) {
        String sql = "UPDATE Kartlar SET HesapNo = ? WHERE KartNumarasi = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hesapNo);
            ps.setString(2, kartNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private VBox createHesapCard(String hesapNo, String tur, double bakiye) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #182332; -fx-background-radius: 10; -fx-padding: 15;");

        Label lblTur = new Label(tur);
        lblTur.setTextFill(Color.WHITE);
        lblTur.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label lblNo = new Label("Hesap No: " + hesapNo);
        lblNo.setTextFill(Color.LIGHTGRAY);

        Label lblBakiye = new Label(String.format("%.2f TL", bakiye));
        lblBakiye.setTextFill(Color.web("#4CAF50"));
        lblBakiye.setFont(Font.font("System", FontWeight.BOLD, 16));

        Button btnBagla = new Button("Kart Bağla");
        btnBagla.setOnAction(e -> handleKartBagla(hesapNo));

        card.getChildren().addAll(lblTur, lblNo, lblBakiye, btnBagla);
        return card;
    }

    private void handleKartBagla(String hesapNo) {
        List<String> bostaKartlar = getBostaOlanKartlar();
        if (bostaKartlar.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Bilgi", "Bağlanabilecek boşta kartınız bulunamadı.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(bostaKartlar.get(0), bostaKartlar);
        dialog.setTitle("Kart Bağla");
        dialog.setHeaderText(hesapNo + " nolu hesaba kart bağla.");
        dialog.setContentText("Kart Seçiniz:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(kartNo -> {
            if (kartHesabaBagla(kartNo, hesapNo)) {
                showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Kart hesaba bağlandı.");
            }
        });
    }

    @FXML
    void getmusterianaekran(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("müsterianaekran.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}