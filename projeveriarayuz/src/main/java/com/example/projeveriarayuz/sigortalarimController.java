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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;

public class sigortalarimController implements Initializable {

    @FXML
    private VBox sigortaListesiContainer;

    @FXML
    private ComboBox<String> cmbSigortaTuru;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // GÜVENLİK KONTROLÜ: Kullanıcı giriş yapmamışsa uyar
        if (!AppSession.isUserLoggedIn()) {
            System.err.println("HATA: Oturum açılmamış! AppSession ID: 0");
            Label lblHata = new Label("Lütfen önce giriş yapınız.");
            lblHata.setTextFill(Color.RED);
            sigortaListesiContainer.getChildren().add(lblHata);
            return; // Kodun devamını çalıştırma
        }

        // ComboBox Seçeneklerini Doldur
        cmbSigortaTuru.getItems().addAll("Tümü", "DASK", "Konut", "Kasko", "Trafik", "Sağlık", "BES");
        cmbSigortaTuru.getSelectionModel().select("Tümü");

        // Seçim değiştiğinde listeyi güncelle
        cmbSigortaTuru.setOnAction(event -> {
            loadSigortalar(cmbSigortaTuru.getValue());
        });

        // Sayfa ilk açıldığında verileri yükle
        loadSigortalar("Tümü");

        System.out.println("Sigortalar Sayfası Yüklendi. Aktif Müşteri ID: " + AppSession.getActiveMusteriId());
    }

    // --- SATIN ALMA BUTONU ---
    @FXML
    void btnSatinAlClicked(ActionEvent event) {
        if (!AppSession.isUserLoggedIn()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setContentText("Oturum süreniz dolmuş veya giriş yapılmamış.");
            alert.show();
            return;
        }

        List<String> choices = new ArrayList<>();
        choices.add("DASK");
        choices.add("Kasko");
        choices.add("Trafik");
        choices.add("Sağlık");
        choices.add("Konut");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("DASK", choices);
        dialog.setTitle("Sigorta Satın Al");
        dialog.setHeaderText("Hangi sigortayı satın almak istersiniz?");
        dialog.setContentText("Sigorta Türü:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(selectedType -> {
            satinAlVeKaydet(selectedType);
        });
    }

    // --- VERİTABANINA KAYIT (AppSession Kullanarak) ---
    private void satinAlVeKaydet(String tur) {
        Random rand = new Random();
        String sirket = "Anadolu Sigorta";
        String policeNo = "POL-" + (10000 + rand.nextInt(90000));
        LocalDate baslangic = LocalDate.now();
        LocalDate bitis = baslangic.plusYears(1);

        double tutar = 0;
        switch (tur) {
            case "DASK": tutar = 450.0; break;
            case "Kasko": tutar = 12000.0; break;
            case "Trafik": tutar = 3500.0; break;
            case "Sağlık": tutar = 8000.0; break;
            default: tutar = 1000.0;
        }

        String query = "INSERT INTO Sigorta (MusteriID, SigortaTuru, SigortaSirketi, Policeno, BaslangicTarihi, BitisTarihi, PrimTutari) VALUES (?, ?, ?, ?, ?, ?, ?)";

        // DbConnection sınıfını kullanıyoruz
        try (Connection connect = DbConnection.getConnection();
             PreparedStatement ekle = connect.prepareStatement(query)) {

            // DİKKAT: Müşteri ID'sini AppSession'dan alıyoruz
            ekle.setInt(1, AppSession.getActiveMusteriId());

            ekle.setString(2, tur);
            ekle.setString(3, sirket);
            ekle.setString(4, policeNo);
            ekle.setDate(5, java.sql.Date.valueOf(baslangic));
            ekle.setDate(6, java.sql.Date.valueOf(bitis));
            ekle.setDouble(7, tutar);

            int sonuc = ekle.executeUpdate();

            if (sonuc > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("İşlem Başarılı");
                alert.setHeaderText(null);
                alert.setContentText(tur + " sigortası başarıyla satın alındı!");
                alert.showAndWait();

                // Listeyi güncelle
                loadSigortalar(cmbSigortaTuru.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setContentText("Veritabanı bağlantı hatası: " + e.getMessage());
            alert.show();
        }
    }

    // --- LİSTELEME (AppSession Kullanarak) ---
    private void loadSigortalar(String filtreTuru) {
        sigortaListesiContainer.getChildren().clear();

        String query = "SELECT SigortaTuru, SigortaSirketi, Policeno, BitisTarihi, PrimTutari FROM Sigorta WHERE MusteriID = ?";
        if (!"Tümü".equals(filtreTuru)) {
            query += " AND SigortaTuru = ?";
        }

        try (Connection connect = DbConnection.getConnection();
             PreparedStatement listele = connect.prepareStatement(query)) {

            // DİKKAT: Burada sorguya giriş yapan kişinin ID'sini veriyoruz
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

                VBox card = createSigortaCard(tur, sirket, policeNo, bitis, tutar);
                sigortaListesiContainer.getChildren().add(card);
            }

            if(!kayitVarmi) {
                Label lbl = new Label("Kayıt bulunamadı.");
                lbl.setTextFill(Color.WHITE);
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
        Parent root = FXMLLoader.load(getClass().getResource("müsterianaekran.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}