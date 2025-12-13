package com.example.projeveriarayuz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MusteriBasvurulariController {

    @FXML private TableView<Basvuru> basvuruTable;
    @FXML private TableColumn<Basvuru, String> urunAdiColumn;
    @FXML private TableColumn<Basvuru, String> tarihColumn;
    @FXML private TableColumn<Basvuru, String> durumColumn;

    private int musteriId;

    public void setMusteriId(int musteriId) {
        this.musteriId = musteriId;
    }

    @FXML
    public void initialize() {
        if (AppSession.isUserLoggedIn()) {
            this.musteriId = AppSession.getActiveMusteriId();
        }

        // Tablo Sütun Eşleştirmeleri (Basvuru sınıfındaki getter isimleriyle aynı olmalı)
        urunAdiColumn.setCellValueFactory(new PropertyValueFactory<>("urunAdi"));
        tarihColumn.setCellValueFactory(new PropertyValueFactory<>("basvuruTarihi"));
        durumColumn.setCellValueFactory(new PropertyValueFactory<>("basvuruDurumu"));

        loadBasvurular();
    }

    private void loadBasvurular() {
        if (this.musteriId <= 0) {
            basvuruTable.setItems(FXCollections.observableArrayList());
            return;
        }

        ObservableList<Basvuru> basvuruListesi = FXCollections.observableArrayList();

        // GÜNCELLEME: LEFT JOIN ile hem Product hem SigortaTurleri tablosunu bağlıyoruz.
        // Eğer UrunID doluysa Product tablosundan alır, değilse SigortaTurleri tablosundan alır.
        String sql = "SELECT " +
                " B.BasvuruID, " +
                " B.BasvuruTarihi, " +
                " B.BasvuruDurumu, " +
                " P.UrunTipi, " +
                " P.Aciklama, " +
                " S.SigortaAdi " +
                "FROM Basvuru B " +
                "LEFT JOIN Product P ON B.UrunID = P.UrunID " +
                "LEFT JOIN SigortaTurleri S ON B.SigortaTurID = S.SigortaTurID " +
                "WHERE B.MusteriID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.musteriId);
            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int basvuruID = rs.getInt("BasvuruID");
                    String tarih = rs.getString("BasvuruTarihi");
                    String durum = rs.getString("BasvuruDurumu");

                    // İsimlendirme Mantığı:
                    String urunTipi = rs.getString("UrunTipi");
                    String aciklama = rs.getString("Aciklama");
                    String sigortaAdi = rs.getString("SigortaAdi");

                    String gorunecekAd;

                    if (urunTipi != null) {
                        // Eğer banka ürünü ise
                        gorunecekAd = urunTipi + " (" + (aciklama != null ? aciklama : "") + ")";
                    } else if (sigortaAdi != null) {
                        // Eğer sigorta başvurusu ise
                        gorunecekAd = sigortaAdi + " (Sigorta)";
                    } else {
                        gorunecekAd = "Diğer Başvuru";
                    }

                    basvuruListesi.add(new Basvuru(basvuruID, gorunecekAd, tarih, durum));
                }
            }
            basvuruTable.setItems(basvuruListesi);

        } catch (SQLException e) {
            System.err.println("Veritabanı Hatası (Başvurular): " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Hata", "Başvurular yüklenirken hata oluştu.");
        }
    }

    @FXML
    public void getmusterianaekran(ActionEvent event) throws IOException {
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