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

public class KrediBasvuruController implements Initializable {

    @FXML private ComboBox<String> cmbKrediTuru;
    @FXML private Label lblAciklama;
    @FXML private VBox krediListesiContainer;

    private Map<String, Integer> urunAdiToIdMap = new HashMap<>();
    private Map<String, String> urunAdiToAciklamaMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadAvailableKrediProducts(); // Başvuru ComboBox'ını yükler
        loadMevcutKrediler();         // Onaylanmış Kredileri listeler (YENİ)

        cmbKrediTuru.setOnAction(event -> {
            String secilenAd = cmbKrediTuru.getValue();
            if (secilenAd != null && urunAdiToAciklamaMap.containsKey(secilenAd)) {
                lblAciklama.setText(urunAdiToAciklamaMap.get(secilenAd));
            } else {
                lblAciklama.setText("Lütfen bir kredi türü seçiniz.");
            }
        });
    }

    private void loadAvailableKrediProducts() {
        ObservableList<String> urunler = FXCollections.observableArrayList();

        String sql = "SELECT KrediTurID, Ad, Aciklama FROM KrediTurleri";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("KrediTurID");
                String ad = rs.getString("Ad");
                String aciklama = rs.getString("Aciklama");
                String birlesikUrunMetni = ad + " - " + aciklama.substring(0, Math.min(aciklama.length(), 30)) + "...";

                urunler.add(birlesikUrunMetni);
                urunAdiToIdMap.put(birlesikUrunMetni, id);
                urunAdiToAciklamaMap.put(birlesikUrunMetni, aciklama);
            }
            cmbKrediTuru.setItems(urunler);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Veritabanı Hatası", "Kredi ürünleri yüklenemedi: " + e.getMessage());
        }
    }

    @FXML
    void basvuruYap(ActionEvent event) {

        String secilenUrunMetni = cmbKrediTuru.getSelectionModel().getSelectedItem();
        if (secilenUrunMetni == null || secilenUrunMetni.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen başvurmak istediğiniz bir Kredi Türü seçiniz.");
            return;
        }

        Integer krediTurID = urunAdiToIdMap.get(secilenUrunMetni);

        if (krediTurID == null) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Seçilen Kredi türüne ait ID bulunamadı.");
            return;
        }

        final int KREDI_GENEL_URUN_ID = 1;

        String kontrolSql = "SELECT COUNT(*) FROM Basvuru WHERE MusteriID = ? AND UrunID = ? AND KrediTurID = ? AND BasvuruDurumu = 'Inceleniyor'";
        String insertSql = "INSERT INTO Basvuru (MusteriID, UrunID, KrediTurID, BasvuruTarihi, BasvuruDurumu) VALUES (?, ?, ?, GETDATE(), 'Inceleniyor')";

        try (Connection conn = DbConnection.getConnection()) {

            try (PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql)) {
                kontrolStmt.setInt(1, AppSession.getActiveMusteriId());
                kontrolStmt.setInt(2, KREDI_GENEL_URUN_ID);
                kontrolStmt.setInt(3, krediTurID);
                try (ResultSet rs = kontrolStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Başvuru Mevcut",
                                secilenUrunMetni + " için zaten değerlendirme aşamasında bir başvurunuz var.");
                        return;
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, AppSession.getActiveMusteriId());
                insertStmt.setInt(2, KREDI_GENEL_URUN_ID);
                insertStmt.setInt(3, krediTurID);

                int affectedRows = insertStmt.executeUpdate();

                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Başarılı",
                            secilenUrunMetni + " başvurunuz başarıyla alındı.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Hata", "Başvuru oluşturulamadı.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", "Sistem hatası: " + e.getMessage());
        }
    }

    private void loadMevcutKrediler() {
        if (krediListesiContainer == null) return;
        krediListesiContainer.getChildren().clear();
        String query = "SELECT KrediID, AnaPara, KalanBorc, VadeSayisi, FaizOrani, UrunID " +
                "FROM Krediler WHERE MusteriID = ?";

        try (Connection connect = DbConnection.getConnection();
             PreparedStatement listele = connect.prepareStatement(query)) {

            listele.setInt(1, AppSession.getActiveMusteriId());

            ResultSet result = listele.executeQuery();
            boolean kayitVarmi = false;

            while (result.next()) {
                kayitVarmi = true;
                int krediId = result.getInt("KrediID");
                double anaPara = result.getDouble("AnaPara");
                double kalanBorc = result.getDouble("KalanBorc");
                int vade = result.getInt("VadeSayisi");
                double faiz = result.getDouble("FaizOrani");

                krediListesiContainer.getChildren().add(createKrediCard(krediId, anaPara, kalanBorc, vade, faiz));
            }

            if(!kayitVarmi) {
                Label lbl = new Label("Onaylanmış aktif bir krediniz bulunmamaktadır.");
                lbl.setTextFill(Color.GRAY);
                krediListesiContainer.getChildren().add(lbl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private VBox createKrediCard(int krediId, double anaPara, double kalanBorc, int vade, double faiz) {
        VBox card = new VBox();
        card.setSpacing(5);
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: #182332; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        Label lblBaslik = new Label("Kredi Hesabı #" + krediId);
        lblBaslik.setTextFill(Color.web("#0077cc"));
        lblBaslik.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label lblAnaPara = new Label("Ana Para: " + String.format("%.2f", anaPara) + " TL");
        lblAnaPara.setTextFill(Color.WHITE);

        Label lblKalan = new Label("Kalan Borç: " + String.format("%.2f", kalanBorc) + " TL");
        lblKalan.setTextFill(Color.web("#CF5050"));

        Label lblVade = new Label("Vade Sayısı: " + vade + " Ay");
        lblVade.setTextFill(Color.web("#aaaaaa"));

        Label lblFaiz = new Label("Faiz Oranı: %" + String.format("%.2f", faiz * 100));
        lblFaiz.setTextFill(Color.web("#4CAF50"));

        card.getChildren().addAll(lblBaslik, lblAnaPara, lblKalan, lblVade, lblFaiz);
        return card;
    }

    @FXML
    void getUrunlerAnaEkran(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("müsterianaekran.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

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