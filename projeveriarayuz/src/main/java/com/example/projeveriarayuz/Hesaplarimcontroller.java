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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Hesaplarimcontroller implements Initializable {

    @FXML private VBox hesapListesiContainer;
    @FXML private ComboBox<String> cmbHesapTuru;

    // Yeni: Hesap Türü Adını ID ile eşleştirmek için
    private Map<String, Integer> hesapAdiToIdMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!AppSession.isUserLoggedIn()) {
            System.err.println("Hata: Kullanıcı girişi yapılmamış.");
            return;
        }

        // Hesap Türlerini Doldur (Artık DB'den çekilecek)
        loadAvailableHesapTurleri();

        // Mevcut Hesapları Listele
        loadHesaplar();
    }

    // --- YENİ METOT: DB'den Hesap Türlerini Yükle ---
    private void loadAvailableHesapTurleri() {
        cmbHesapTuru.getItems().clear();
        hesapAdiToIdMap.clear();

        // SQL: HesapTurleri tablosundan veri çekiyoruz
        String sql = "SELECT HesapTurID, Ad FROM HesapTurleri";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("HesapTurID");
                String ad = rs.getString("Ad");

                cmbHesapTuru.getItems().add(ad);
                hesapAdiToIdMap.put(ad, id);
            }
        } catch (Exception e) {
            System.err.println("Hesap Türleri yüklenirken hata: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Hata", "Hesap türleri yüklenemedi.");
        }
    }

    private void loadHesaplar() {
        hesapListesiContainer.getChildren().clear();

        String query = "SELECT HesapNo, HesapTuru, Bakiye FROM Hesaplar WHERE MusteriID = ?";

        try (Connection connect = DbConnection.getConnection();
             PreparedStatement st = connect.prepareStatement(query)) {

            st.setInt(1, AppSession.getActiveMusteriId());
            ResultSet rs = st.executeQuery();

            boolean kayitVarmi = false;
            while (rs.next()) {
                kayitVarmi = true;
                String hNo = rs.getString("HesapNo");
                String hTur = rs.getString("HesapTuru");
                double bakiye = rs.getDouble("Bakiye");

                hesapListesiContainer.getChildren().add(createHesapCard(hNo, hTur, bakiye));
            }

            if (!kayitVarmi) {
                Label lbl = new Label("Henüz açılmış bir hesabınız yok.");
                lbl.setTextFill(Color.GRAY);
                hesapListesiContainer.getChildren().add(lbl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnHesapOlusturClicked(ActionEvent event) {
        String secilenTur = cmbHesapTuru.getValue();

        if (secilenTur == null || secilenTur.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen bir hesap türü seçiniz!");
            return;
        }

        // 1. HesapTurID'yi al
        Integer hesapTurID = hesapAdiToIdMap.get(secilenTur);
        if (hesapTurID == null) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Seçilen hesap ID'si bulunamadı.");
            return;
        }

        // Product tablosunda Hesap'ın UrunID'si 3 olduğunu varsayalım.
        // Bu ID'yi veritabanı yapınıza göre kontrol edip değiştirin.
        final int HESAP_GENEL_URUN_ID = 3;

        // 2. Başvuru tablosuna kayıt at
        kayitBasvurusuYap(secilenTur, HESAP_GENEL_URUN_ID, hesapTurID);
    }

    // YENİ METOT: Başvuru Lojiği
    private void kayitBasvurusuYap(String turAdi, int genelUrunID, int hesapTurID) {

        // Hesap başvurularında HesapTurID kullanıldığı için kontrol ve insert sorgularına eklenir.
        String kontrolSql = "SELECT COUNT(*) FROM Basvuru WHERE MusteriID = ? AND UrunID = ? AND HesapTurID = ? AND BasvuruDurumu = 'Inceleniyor'";
        String insertSql = "INSERT INTO Basvuru (MusteriID, UrunID, HesapTurID, BasvuruTarihi, BasvuruDurumu) VALUES (?, ?, ?, GETDATE(), 'Inceleniyor')";

        try (Connection conn = DbConnection.getConnection()) {

            // A. Mükerrer Başvuru Kontrolü
            try (PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql)) {
                kontrolStmt.setInt(1, AppSession.getActiveMusteriId());
                kontrolStmt.setInt(2, genelUrunID);
                kontrolStmt.setInt(3, hesapTurID);
                try (ResultSet rs = kontrolStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Başvuru Mevcut",
                                turAdi + " için zaten değerlendirme aşamasında bir başvurunuz var.");
                        return;
                    }
                }
            }

            // B. Başvuruyu Ekleme
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, AppSession.getActiveMusteriId());
                insertStmt.setInt(2, genelUrunID);
                insertStmt.setInt(3, hesapTurID);

                int affectedRows = insertStmt.executeUpdate();

                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Başarılı",
                            turAdi + " için hesap açma başvurunuz başarıyla alındı. \n'Başvurularım' sayfasından durumu takip edebilirsiniz.");

                    cmbHesapTuru.getSelectionModel().clearSelection(); // Seçimi temizle
                } else {
                    showAlert(Alert.AlertType.ERROR, "Hata", "Başvuru oluşturulamadı.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", "Sistem hatası: " + e.getMessage());
        }
    }

    private VBox createHesapCard(String hesapNo, String tur, double bakiye) {
        VBox card = new VBox();
        card.setSpacing(5);
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: #182332; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        Label lblTur = new Label(tur);
        lblTur.setTextFill(Color.web("#0077cc"));
        lblTur.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label lblNo = new Label("No: " + hesapNo);
        lblNo.setTextFill(Color.web("#aaaaaa"));
        lblNo.setFont(Font.font("System", 12));

        Label lblBakiyeBaslik = new Label("Bakiye:");
        lblBakiyeBaslik.setTextFill(Color.WHITE);

        Label lblTutar = new Label(bakiye + " TL");
        if(tur.contains("Dolar")) lblTutar.setText(bakiye + " $");
        else if(tur.contains("Euro")) lblTutar.setText(bakiye + " €");

        lblTutar.setTextFill(Color.web("#4CAF50"));
        lblTutar.setFont(Font.font("System", FontWeight.BOLD, 18));

        card.getChildren().addAll(lblTur, lblNo, lblBakiyeBaslik, lblTutar);
        return card;
    }

    @FXML
    void getmusterianaekran(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("müsterianaekran.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
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