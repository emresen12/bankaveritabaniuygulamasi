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
import java.util.Random;
import java.util.ResourceBundle;

public class Hesaplarimcontroller implements Initializable {

    @FXML
    private VBox hesapListesiContainer;

    @FXML
    private ComboBox<String> cmbHesapTuru;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Oturum kontrolü
        if (!AppSession.isUserLoggedIn()) {
            System.err.println("Hata: Kullanıcı girişi yapılmamış.");
            return;
        }

        // Hesap Türlerini Doldur
        cmbHesapTuru.getItems().addAll("Vadesiz TL Hesabı", "Dolar Hesabı", "Euro Hesabı", "Altın Hesabı", "Yatırım Hesabı");

        // Listeyi Yükle
        loadHesaplar();
    }

    // --- MEVCUT HESAPLARI LİSTELE ---
    private void loadHesaplar() {
        hesapListesiContainer.getChildren().clear();

        // ER Diyagramına uygun sorgu
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

                // Kart oluşturup ekle
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

    // --- YENİ HESAP OLUŞTURMA BUTONU ---
    @FXML
    void btnHesapOlusturClicked(ActionEvent event) {
        String secilenTur = cmbHesapTuru.getValue();

        if (secilenTur == null || secilenTur.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Uyarı");
            alert.setHeaderText(null);
            alert.setContentText("Lütfen bir hesap türü seçiniz!");
            alert.show();
            return;
        }

        // Yeni hesap oluşturma işlemi
        createNewAccount(secilenTur);
    }

    private void createNewAccount(String tur) {
        // Rastgele bir Hesap Numarası oluştur (Örn: TR-4921...)
        Random rand = new Random();
        String yeniHesapNo = "TR-" + (100000 + rand.nextInt(900000));

        String insertQuery = "INSERT INTO Hesaplar (HesapNo, MusteriID, HesapTuru, Bakiye) VALUES (?, ?, ?, 0)";

        try (Connection connect = DbConnection.getConnection();
             PreparedStatement ps = connect.prepareStatement(insertQuery)) {

            ps.setString(1, yeniHesapNo);
            ps.setInt(2, AppSession.getActiveMusteriId());
            ps.setString(3, tur);
            // Bakiye varsayılan olarak 0

            int result = ps.executeUpdate();

            if (result > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Başarılı");
                alert.setHeaderText(null);
                alert.setContentText(tur + " başarıyla oluşturuldu.\nHesap No: " + yeniHesapNo);
                alert.showAndWait();

                // Listeyi yenile
                loadHesaplar();
                cmbHesapTuru.getSelectionModel().clearSelection();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setContentText("Hesap oluşturulurken hata: " + e.getMessage());
            alert.show();
        }
    }

    // --- GÖRSEL KART OLUŞTURUCU ---
    private VBox createHesapCard(String hesapNo, String tur, double bakiye) {
        VBox card = new VBox();
        card.setSpacing(5);
        card.setPrefWidth(280);
        // Koyu mavi kart stili
        card.setStyle("-fx-background-color: #182332; -fx-background-radius: 15; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        // Hesap Türü Başlığı
        Label lblTur = new Label(tur);
        lblTur.setTextFill(Color.web("#0077cc"));
        lblTur.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Hesap Numarası
        Label lblNo = new Label("No: " + hesapNo);
        lblNo.setTextFill(Color.web("#aaaaaa"));
        lblNo.setFont(Font.font("System", 12));

        // Bakiye Kısmı
        Label lblBakiyeBaslik = new Label("Bakiye:");
        lblBakiyeBaslik.setTextFill(Color.WHITE);

        Label lblTutar = new Label(bakiye + " TL");
        // Döviz türüne göre simgeyi değiştirebilirsin (if tur.equals("Dolar")...)
        if(tur.contains("Dolar")) lblTutar.setText(bakiye + " $");
        else if(tur.contains("Euro")) lblTutar.setText(bakiye + " €");

        lblTutar.setTextFill(Color.web("#4CAF50")); // Yeşil renk
        lblTutar.setFont(Font.font("System", FontWeight.BOLD, 18));

        card.getChildren().addAll(lblTur, lblNo, lblBakiyeBaslik, lblTutar);
        return card;
    }

    // --- GERİ BUTONU ---
    @FXML
    void getmusterianaekran(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("müsterianaekran.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}