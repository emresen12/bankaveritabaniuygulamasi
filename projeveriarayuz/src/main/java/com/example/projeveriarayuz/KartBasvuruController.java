package com.example.projeveriarayuz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class KartBasvuruController implements Initializable {

    @FXML private ComboBox<String> cmbKartTuru;
    @FXML private Label lblAciklama;
    @FXML private VBox kartListesiContainer;

    private Map<String, Integer> urunAdiToIdMap = new HashMap<>();
    private Map<String, String> urunAdiToAciklamaMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadAvailableKartProducts();
        loadMevcutKartlar();

        cmbKartTuru.setOnAction(event -> {
            String secilenAd = cmbKartTuru.getValue();
            if (secilenAd != null && urunAdiToAciklamaMap.containsKey(secilenAd)) {
                lblAciklama.setText(urunAdiToAciklamaMap.get(secilenAd));
            } else {
                lblAciklama.setText("Lütfen bir kart türü seçiniz.");
            }
        });
    }

    private void loadAvailableKartProducts() {
        ObservableList<String> urunler = FXCollections.observableArrayList();
        String sql = "SELECT KartTurID, Ad, Aciklama FROM KartTurleri";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("KartTurID");
                String ad = rs.getString("Ad");
                String aciklama = rs.getString("Aciklama");

                String birlesikUrunMetni = ad + " - " + aciklama.substring(0, Math.min(aciklama.length(), 30)) + "...";

                urunler.add(birlesikUrunMetni);
                urunAdiToIdMap.put(birlesikUrunMetni, id);
                urunAdiToAciklamaMap.put(birlesikUrunMetni, aciklama);
            }
            cmbKartTuru.setItems(urunler);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Veritabanı Hatası", "Kart ürünleri yüklenemedi: " + e.getMessage());
        }
    }

    @FXML
    void basvuruYap(ActionEvent event) {
        String secilenUrunMetni = cmbKartTuru.getSelectionModel().getSelectedItem();

        if (secilenUrunMetni == null || secilenUrunMetni.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen başvurmak istediğiniz bir Kart Türü seçiniz.");
            return;
        }

        Integer kartTurID = urunAdiToIdMap.get(secilenUrunMetni);
        if (kartTurID == null) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Seçilen Kart türüne ait ID bulunamadı.");
            return;
        }

        final int KART_GENEL_URUN_ID = 2;

        // GÜNCELLEME: KartTurID sütunu silindiği için AltUrunID kullanıyoruz
        String kontrolSql = "SELECT COUNT(*) FROM Basvuru WHERE MusteriID = ? AND UrunID = ? AND AltUrunID = ? AND BasvuruDurumu = 'Inceleniyor'";
        String insertSql = "INSERT INTO Basvuru (MusteriID, UrunID, AltUrunID, BasvuruTarihi, BasvuruDurumu) VALUES (?, ?, ?, GETDATE(), 'Inceleniyor')";

        try (Connection conn = DbConnection.getConnection()) {
            // Kontrol İşlemi
            try (PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql)) {
                kontrolStmt.setInt(1, AppSession.getActiveMusteriId());
                kontrolStmt.setInt(2, KART_GENEL_URUN_ID);
                kontrolStmt.setInt(3, kartTurID);
                try (ResultSet rs = kontrolStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Başvuru Mevcut",
                                secilenUrunMetni + " için zaten değerlendirme aşamasında bir başvurunuz var.");
                        return;
                    }
                }
            }

            // Ekleme İşlemi
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, AppSession.getActiveMusteriId());
                insertStmt.setInt(2, KART_GENEL_URUN_ID);
                insertStmt.setInt(3, kartTurID);

                int affectedRows = insertStmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Başarılı",
                            secilenUrunMetni + " Başvurunuz başarıyla alındı.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Hata", "Başvuru oluşturulamadı.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", "Sistem hatası: " + e.getMessage());
        }
    }

    private void loadMevcutKartlar() {
        kartListesiContainer.getChildren().clear();
        String query = "SELECT KartNumarasi, KartTipi, Limit, GuncelBorc FROM Kartlar WHERE MusteriID = ?";

        try (Connection connect = DbConnection.getConnection();
             PreparedStatement listele = connect.prepareStatement(query)) {

            listele.setInt(1, AppSession.getActiveMusteriId());
            ResultSet result = listele.executeQuery();
            boolean kayitVarmi = false;

            while (result.next()) {
                kayitVarmi = true;
                String kartNo = result.getString("KartNumarasi");
                String tip = result.getString("KartTipi");
                double limit = result.getDouble("Limit");
                double borc = result.getDouble("GuncelBorc");
                kartListesiContainer.getChildren().add(createKartCard(kartNo, tip, limit, borc));
            }

            if(!kayitVarmi) {
                Label lbl = new Label("Henüz onaylanmış bir kartınız bulunmamaktadır.");
                lbl.setTextFill(Color.GRAY);
                kartListesiContainer.getChildren().add(lbl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createKartCard(String kartNo, String tip, double limit, double borc) {
        VBox card = new VBox(5);
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: #182332; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        Label lblTip = new Label(tip);
        lblTip.setTextFill(Color.web("#0077cc"));
        lblTip.setFont(Font.font("System", FontWeight.BOLD, 16));

        String gizliKartNo = "**** **** **** " + (kartNo.length() > 4 ? kartNo.substring(kartNo.length() - 4) : "****");
        Label lblNo = new Label("Kart No: " + gizliKartNo);
        lblNo.setTextFill(Color.WHITE);

        Label lblLimit = new Label("Limit: " + String.format("%.2f", limit) + " TL");
        lblLimit.setTextFill(Color.web("#aaaaaa"));

        Label lblBorc = new Label("Borç: " + String.format("%.2f", borc) + " TL");
        lblBorc.setTextFill(borc > 0 ? Color.RED : Color.web("#4CAF50"));

        card.getChildren().addAll(lblTip, lblNo, lblLimit, lblBorc);
        return card;
    }

    @FXML
    void getUrunlerAnaEkran(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("müsterianaekran.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
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