package com.example.projeveriarayuz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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
import java.sql.SQLException;
import java.util.*;

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
    private void bagliKartlariGosterDialog(String hesapNo) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Hesaba Bağlı Kartlar");
        dialog.setHeaderText(hesapNo + " nolu hesaba bağlı kartlar:");

        ButtonType closeButton = new ButtonType("Kapat", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        VBox listContainer = new VBox(10);
        listContainer.setPadding(new Insets(10));

        // Veritabanından bu hesaba bağlı kartları çek (Statik metodumuzu kullanıyoruz)
        // Not: Bu işlem için Kart.getMusteriKartlari metodunu filtreli hale getirmeli veya
        // aşağıdakine benzer bir lokal sorgu yazmalısınız.
        String sql = "SELECT KartNumarasi, KartTipi, Limit FROM Kartlar WHERE HesapNo = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hesapNo);
            ResultSet rs = ps.executeQuery();

            boolean kartBulundu = false;
            while (rs.next()) {
                kartBulundu = true;
                String no = rs.getString("KartNumarasi");
                String tip = rs.getString("KartTipi");
                double limit = rs.getDouble("Limit");

                Label lblKart = new Label("💳 " + tip + " - " + no + " (Limit: " + limit + " TL)");
                listContainer.getChildren().add(lblKart);
            }

            if (!kartBulundu) {
                listContainer.getChildren().add(new Label("Bu hesaba bağlı bir kart bulunamadı."));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog.getDialogPane().setContent(listContainer);
        dialog.showAndWait();
    }

    private VBox createHesapCard(String hesapNo, String tur, double bakiye) {
        VBox card = new VBox();
        card.setSpacing(10); // Boşlukları biraz artırdık
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: #182332; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        Label lblTur = new Label(tur);
        lblTur.setTextFill(Color.web("#0077cc"));
        lblTur.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label lblNo = new Label("No: " + hesapNo);
        lblNo.setTextFill(Color.web("#aaaaaa"));
        lblNo.setFont(Font.font("System", 12));

        Label lblTutar = new Label(bakiye + " TL");
        if(tur.contains("Dolar")) lblTutar.setText(bakiye + " $");
        else if(tur.contains("Euro")) lblTutar.setText(bakiye + " €");
        lblTutar.setTextFill(Color.web("#4CAF50"));
        lblTutar.setFont(Font.font("System", FontWeight.BOLD, 18));
        // createHesapCard metodunun içine eklenecek kısım
        Button btnKartBagla = new Button("Kart Bağla");
        btnKartBagla.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        btnKartBagla.setOnAction(e -> kartBaglaDialog(hesapNo));

// Kartın sonuna butonu ekleyin
        card.getChildren().add(btnKartBagla);

        // --- YENİ: Bağlı Kartları Göster Butonu ---
        Button btnKartlariGoster = new Button("Bağlı Kartları Gör");
        btnKartlariGoster.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-cursor: hand;");
        btnKartlariGoster.setOnAction(e -> bagliKartlariGosterDialog(hesapNo));

        card.getChildren().addAll(lblTur, lblNo, new Label("Bakiye:"), lblTutar, btnKartlariGoster);
        return card;
    }
    private void kartBaglaDialog(String hesapNo) {
        // 1. KONTROL: Bu hesaba bağlı kaç kart var? (Sınır: 3)
        int bagliKartSayisi = getBagliKartSayisi(hesapNo);
        if (bagliKartSayisi >= 3) {
            showAlert(Alert.AlertType.WARNING, "Sınır Aşıldı", "Bir hesaba en fazla 3 kart bağlanabilir.");
            return;
        }

        // 2. SEÇİM: Henüz bir hesaba bağlı olmayan kartları getir
        List<String> bostaOlanKartlar = getBostaOlanKartlar();
        if (bostaOlanKartlar.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Kart Yok", "Bağlanabilecek boşta kartınız bulunmamaktadır.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(bostaOlanKartlar.get(0), bostaOlanKartlar);
        dialog.setTitle("Kart Bağla");
        dialog.setHeaderText(hesapNo + " nolu hesaba kart bağla");
        dialog.setContentText("Kart Seçiniz:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(secilenKartNo -> {
            if (kartHesabaBagla(secilenKartNo, hesapNo)) {
                showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Kart başarıyla hesaba bağlandı.");
            }
        });
    }
    // Hesaba bağlı kart sayısını sorgular
    private int getBagliKartSayisi(String hesapNo) {
        String sql = "SELECT COUNT(*) FROM Kartlar WHERE HesapNo = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hesapNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // Boştaki (HesapNo'su NULL olan) kartları listeler
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

    // SQL UPDATE ile kartı hesaba bağlar
    private boolean kartHesabaBagla(String kartNo, String hesapNo) {
        String sql = "UPDATE Kartlar SET HesapNo = ? WHERE KartNumarasi = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hesapNo);
            ps.setString(2, kartNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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