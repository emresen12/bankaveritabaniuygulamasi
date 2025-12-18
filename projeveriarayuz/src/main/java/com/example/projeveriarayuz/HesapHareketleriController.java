package com.example.projeveriarayuz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HesapHareketleriController {

    @FXML private TableView<HesapHareket> hareketTable;
    @FXML private TableColumn<HesapHareket, String> islemTuruColumn;
    @FXML private TableColumn<HesapHareket, Double> islemTutariColumn;
    @FXML private TableColumn<HesapHareket, String> islemTarihiColumn;
    @FXML private ComboBox<String> cmbIslemTuru;

    private int musteriId;

    private ObservableList<HesapHareket> tumHareketler = FXCollections.observableArrayList();
    @FXML
    public void initialize() {

        if (AppSession.isUserLoggedIn()) {
            this.musteriId = AppSession.getActiveMusteriId();
        }

        // TableColumn eşleştirmeleri
        islemTuruColumn.setCellValueFactory(new PropertyValueFactory<>("islemTuru"));
        islemTutariColumn.setCellValueFactory(new PropertyValueFactory<>("islemTutari"));
        islemTarihiColumn.setCellValueFactory(new PropertyValueFactory<>("islemTarihi"));

        loadIslemTurleri();       // ComboBox doldur
        loadHesapHareketleri();   // Tablo doldur
    }

    private void loadIslemTurleri() {

        cmbIslemTuru.getItems().clear();
        cmbIslemTuru.getItems().add("Tümü"); // Default

        String sql =
                "SELECT DISTINCT IslemTuru " +
                        "FROM Islemler I " +
                        "INNER JOIN Hesaplar H ON I.KaynakHesapNo = H.HesapNo " +
                        "WHERE H.MusteriID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.musteriId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cmbIslemTuru.getItems().add(rs.getString("IslemTuru"));
                }
            }

            cmbIslemTuru.getSelectionModel().selectFirst();

            // Filtreleme bağlanıyor
            cmbIslemTuru.setOnAction(event ->
                    filtreleIslemTuruneGore(cmbIslemTuru.getValue())
            );

        } catch (SQLException e) {
            System.err.println("İşlem Türleri Yükleme Hatası: " + e.getMessage());
        }
    }
    private void filtreleIslemTuruneGore(String secilenTur) {

        if ("Tümü".equals(secilenTur)) {
            hareketTable.setItems(tumHareketler);
            return;
        }

        ObservableList<HesapHareket> filtrelenmisListe =
                FXCollections.observableArrayList();

        for (HesapHareket hareket : tumHareketler) {
            if (hareket.getIslemTuru().equalsIgnoreCase(secilenTur)) {
                filtrelenmisListe.add(hareket);
            }
        }

        hareketTable.setItems(filtrelenmisListe);
    }


    private void loadHesapHareketleri() {

        if (this.musteriId <= 0) {
            hareketTable.setItems(FXCollections.emptyObservableList());
            return;
        }

        tumHareketler.clear();

        String sql =
                "SELECT I.IslemTuru, I.IslemTutari, I.IslemTarihi " +
                        "FROM Islemler I " +
                        "INNER JOIN Hesaplar H ON I.KaynakHesapNo = H.HesapNo " +
                        "WHERE H.MusteriID = ? " +
                        "ORDER BY I.IslemTarihi DESC";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.musteriId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    String tur = rs.getString("IslemTuru");
                    Double tutar = rs.getDouble("IslemTutari");
                    String tarih = rs.getString("IslemTarihi");

                    tumHareketler.add(
                            new HesapHareket(tur, tutar, tarih)
                    );
                }
            }

            hareketTable.setItems(tumHareketler);

        } catch (SQLException e) {
            System.err.println("Veritabanı Hatası (Hesap Hareketleri): " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Hata", "Hesap hareketleri yüklenirken hata oluştu.");
        }
    }

    @FXML
    public void btnGeriDon(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("müsterianaekran.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Müşteri Ana Ekranı");
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
