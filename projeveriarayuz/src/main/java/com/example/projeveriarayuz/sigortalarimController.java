package com.example.projeveriarayuz;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
import java.util.Optional;
import java.util.ResourceBundle;

public class sigortalarimController implements Initializable {

    @FXML
    private VBox sigortaListesiContainer;

    @FXML
    private ComboBox<String> cmbSigortaTuru;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // GÜVENLİK KONTROLÜ
        if (!AppSession.isUserLoggedIn()) {
            System.err.println("HATA: Oturum açılmamış!");
            Label lblHata = new Label("Lütfen önce giriş yapınız.");
            lblHata.setTextFill(Color.RED);
            sigortaListesiContainer.getChildren().add(lblHata);
            return;
        }

        // Ana Filtre ComboBox Seçenekleri
        cmbSigortaTuru.getItems().addAll("Tümü", "DASK", "Konut", "Kasko", "Trafik", "Sağlık", "BES");
        cmbSigortaTuru.getSelectionModel().select("Tümü");

        // Seçim değiştiğinde mevcut poliçeleri listele
        cmbSigortaTuru.setOnAction(event -> loadMevcutSigortalar(cmbSigortaTuru.getValue()));

        // İlk açılışta mevcut poliçeleri yükle
        loadMevcutSigortalar("Tümü");
    }

    // --- BAŞVURU BUTONU ---
    @FXML
    void btnSatinAlClicked(ActionEvent event) {
        if (!AppSession.isUserLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Oturum süreniz dolmuş.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Sigorta Başvurusu");
        dialog.setHeaderText("Başvuru yapmak istediğiniz sigorta türünü seçiniz.");

        ButtonType basvuruButtonType = new ButtonType("Başvuru Yap", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(basvuruButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> cmbTurSecimi = new ComboBox<>();
        // Not: Bu isimlerin Veritabanındaki 'SigortaTurleri' tablosundaki 'SigortaAdi' ile BİREBİR AYNI olması lazım.
        cmbTurSecimi.getItems().addAll("DASK", "Kasko", "Trafik", "Sağlık", "Konut Sigortası", "BES");
        cmbTurSecimi.getSelectionModel().selectFirst();

        Label lblFiyat = new Label();
        lblFiyat.setTextFill(Color.GREEN);
        lblFiyat.setFont(Font.font("System", FontWeight.BOLD, 14));

        cmbTurSecimi.setOnAction(e -> {
            double fiyat = getTahminiFiyat(cmbTurSecimi.getValue());
            lblFiyat.setText("Tahmini Tutar: " + fiyat + " TL");
        });
        lblFiyat.setText("Tahmini Tutar: " + getTahminiFiyat(cmbTurSecimi.getValue()) + " TL");

        grid.add(new Label("Sigorta Türü:"), 0, 0);
        grid.add(cmbTurSecimi, 1, 0);
        grid.add(new Label("Yıllık Prim:"), 0, 1);
        grid.add(lblFiyat, 1, 1);

        Label lblBilgi = new Label("(Başvurunuz onaylandığında ödeme alınacaktır)");
        lblBilgi.setFont(Font.font("System", 10));
        lblBilgi.setTextFill(Color.GRAY);
        grid.add(lblBilgi, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(cmbTurSecimi::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == basvuruButtonType) {
                return cmbTurSecimi.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(secilenTur -> {
            basvuruYap(secilenTur);
        });
    }

    // --- VERİTABANI İŞLEMLERİ (BAŞVURU KAYDI) ---
    private void basvuruYap(String sigortaTuru) {
        // GÜNCELLEME: Artık Product tablosundan değil, SigortaTurleri tablosundan ID alıyoruz.
        int sigortaTurID = findSigortaTurIdByName(sigortaTuru);

        if (sigortaTurID == -1) {
            showAlert(Alert.AlertType.ERROR, "Hata",
                    "Seçilen sigorta türü (" + sigortaTuru + ") veritabanında 'SigortaTurleri' tablosunda bulunamadı.\nLütfen veritabanı tablosundaki isimleri kontrol edin.");
            return;
        }

        // GÜNCELLEME: UrunID yerine SigortaTurID kullanıyoruz.
        String kontrolSql = "SELECT COUNT(*) FROM Basvuru WHERE MusteriID = ? AND SigortaTurID = ? AND BasvuruDurumu = 'Inceleniyor'";
        String insertSql = "INSERT INTO Basvuru (MusteriID, SigortaTurID, BasvuruTarihi, BasvuruDurumu) VALUES (?, ?, GETDATE(), 'Inceleniyor')";

        try (Connection conn = DbConnection.getConnection()) {

            // A. Mükerrer Başvuru Kontrolü
            try (PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql)) {
                kontrolStmt.setInt(1, AppSession.getActiveMusteriId());
                kontrolStmt.setInt(2, sigortaTurID);
                try (ResultSet rs = kontrolStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Başvuru Mevcut",
                                sigortaTuru + " için zaten değerlendirme aşamasında bir başvurunuz var.");
                        return;
                    }
                }
            }

            // B. Başvuruyu Ekleme
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, AppSession.getActiveMusteriId());
                insertStmt.setInt(2, sigortaTurID);

                int affectedRows = insertStmt.executeUpdate();

                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Başarılı",
                            sigortaTuru + " başvurunuz başarıyla alındı. \n'Başvurularım' sayfasından durumu takip edebilirsiniz.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Hata", "Başvuru oluşturulamadı.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", "Sistem hatası: " + e.getMessage());
        }
    }

    // --- YARDIMCI METOTLAR ---

    // GÜNCELLEME: Yeni tabloya göre ID bulma
    private int findSigortaTurIdByName(String sigortaAdi) {
        // Tam eşleşme arıyoruz
        String sql = "SELECT SigortaTurID FROM SigortaTurleri WHERE SigortaAdi = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sigortaAdi);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("SigortaTurID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Bulunamadı
    }

    private double getTahminiFiyat(String tur) {
        if (tur == null) return 0.0;
        // Basit bir switch-case ile tahmini fiyatlar
        if (tur.contains("DASK")) return 450.0;
        if (tur.contains("Kasko")) return 12000.0;
        if (tur.contains("Trafik")) return 3500.0;
        if (tur.contains("Sağlık")) return 8000.0;
        if (tur.contains("Konut")) return 2500.0;
        if (tur.contains("BES")) return 1500.0;
        return 1000.0;
    }

    // --- MEVCUT POLİÇELERİ LİSTELEME ---
    private void loadMevcutSigortalar(String filtreTuru) {
        sigortaListesiContainer.getChildren().clear();

        // Mevcut aktif poliçeler 'Sigorta' tablosundan çekiliyor.
        String query = "SELECT SigortaTuru, SigortaSirketi, Policeno, BitisTarihi, PrimTutari FROM Sigorta WHERE MusteriID = ?";
        if (!"Tümü".equals(filtreTuru)) {
            query += " AND SigortaTuru = ?";
        }

        try (Connection connect = DbConnection.getConnection();
             PreparedStatement listele = connect.prepareStatement(query)) {

            listele.setInt(1, AppSession.getActiveMusteriId());
            if (!"Tümü".equals(filtreTuru)) {
                listele.setString(2, filtreTuru);
            }

            ResultSet result = listele.executeQuery();
            boolean kayitVarmi = false;

            while (result.next()) {
                kayitVarmi = true;
                String tur = result.getString("SigortaTuru");
                String sirket = result.getString("SigortaSirketi");
                String policeNo = result.getString("Policeno");
                String bitis = result.getString("BitisTarihi");
                double tutar = result.getDouble("PrimTutari");

                sigortaListesiContainer.getChildren().add(createSigortaCard(tur, sirket, policeNo, bitis, tutar));
            }

            if(!kayitVarmi) {
                Label lbl = new Label("Aktif poliçeniz bulunmamaktadır.");
                lbl.setTextFill(Color.GRAY);
                sigortaListesiContainer.getChildren().add(lbl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createSigortaCard(String tur, String sirket, String policeNo, String bitis, double tutar) {
        VBox card = new VBox();
        card.setSpacing(5);
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: #182332; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        Label lblTur = new Label(tur);
        lblTur.setTextFill(Color.web("#0077cc"));
        lblTur.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label lblSirket = new Label(sirket);
        lblSirket.setTextFill(Color.web("#aaaaaa"));
        lblSirket.setFont(Font.font("System", 12));

        Label lblPolice = new Label("Poliçe No: " + policeNo);
        lblPolice.setTextFill(Color.WHITE);

        Label lblTarih = new Label("Bitiş: " + bitis);
        lblTarih.setTextFill(Color.WHITE);

        Label lblTutar = new Label(tutar + " TL");
        lblTutar.setTextFill(Color.web("#4CAF50"));
        lblTutar.setFont(Font.font("System", FontWeight.BOLD, 14));

        card.getChildren().addAll(lblTur, lblSirket, lblPolice, lblTarih, lblTutar);
        return card;
    }

    @FXML
    void getmusterianaekran(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("müsterianaekran.fxml"));
        Parent root = loader.load();
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