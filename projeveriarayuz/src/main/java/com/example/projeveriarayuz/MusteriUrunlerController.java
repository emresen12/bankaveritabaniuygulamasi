package com.example.projeveriarayuz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MusteriUrunlerController {

    @FXML private TableView<MusteriUrunModel> sahipOlunanUrunlerTable;
    @FXML private TableColumn<MusteriUrunModel, String> hesapNoColumn;
    @FXML private TableColumn<MusteriUrunModel, String> urunTipiColumn;
    @FXML private TableColumn<MusteriUrunModel, String> baslangicTarihiColumn;
    @FXML private ComboBox<String> urunBasvuruComboBox;

    private int musteriId;
    private Map<String, Integer> urunAdiToIdMap = new HashMap<>();
    private MusteriUrunDAO musteriUrunDAO = new MusteriUrunDAO();

    @FXML
    public void initialize() {
        setupProductTable();

        if (AppSession.isUserLoggedIn()) {
            this.musteriId = AppSession.getActiveMusteriId();
            loadMusteriUrunleri();
            loadAvailableProducts();
        }
    }

    private void setupProductTable() {
        hesapNoColumn.setCellValueFactory(new PropertyValueFactory<>("hesapNumarasi"));
        urunTipiColumn.setCellValueFactory(new PropertyValueFactory<>("urunTipi"));
        baslangicTarihiColumn.setCellValueFactory(new PropertyValueFactory<>("baslangicTarihi"));
    }

    private void loadMusteriUrunleri() {
        ObservableList<MusteriUrunModel> list = FXCollections.observableArrayList();
        if (musteriId <= 0) {
            sahipOlunanUrunlerTable.setItems(list);
            return;
        }

        try (ResultSet rs = musteriUrunDAO.musteriUrunleriniGetir(musteriId)) {
            while (rs != null && rs.next()) {
                list.add(new MusteriUrunModel(
                        rs.getString("HesapNumarasi"),
                        rs.getString("UrunTipi"),
                        rs.getString("Aciklama"),
                        rs.getString("BaslangicTarihi")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Müşteri Ürünleri Yükleme Hatası: " + e.getMessage());
            e.printStackTrace();
        }
        sahipOlunanUrunlerTable.setItems(list);
    }

    private void loadAvailableProducts() {
        ObservableList<String> urunler = FXCollections.observableArrayList();
        String sql = "SELECT UrunID, UrunTipi, Aciklama FROM Product";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("UrunID");
                String tipi = rs.getString("UrunTipi");
                String aciklama = rs.getString("Aciklama");

                String birlesikUrunMetni = tipi + " (" + aciklama + ")";

                urunler.add(birlesikUrunMetni);
                urunAdiToIdMap.put(birlesikUrunMetni, id);
            }
            urunBasvuruComboBox.setItems(urunler);

        } catch (SQLException e) {
            System.err.println("Veritabanı Hatası (Ürünler): " + e.getMessage());
        }
    }

    @FXML
    private void basvuruYapmaIslemi(ActionEvent event) {
        String secilenUrunAdi = urunBasvuruComboBox.getValue();
        if (secilenUrunAdi == null || secilenUrunAdi.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen başvurmak istediğiniz ürünü seçin.");
            return;
        }

        Integer urunID = urunAdiToIdMap.get(secilenUrunAdi);
        if (urunID == null) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Seçilen ürün ID'si bulunamadı.");
            return;
        }


        String kontrolSql = "SELECT COUNT(*) FROM Basvuru WHERE MusteriID = ? AND UrunID = ? AND BasvuruDurumu = 'Inceleniyor'";
        String insertSql = "INSERT INTO Basvuru (MusteriID, UrunID) VALUES (?, ?)";

        try (Connection conn = DbConnection.getConnection()) {
            // 1. Beklemedeki Başvuru Kontrolü
            try (PreparedStatement kontrolStmt = conn.prepareStatement(kontrolSql)) {
                kontrolStmt.setInt(1, this.musteriId);
                kontrolStmt.setInt(2, urunID);
                try (ResultSet rs = kontrolStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Başvuru Mevcut",
                                secilenUrunAdi + " ürünü için zaten beklemede bir başvurunuz var.");
                        return;
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, this.musteriId);
                insertStmt.setInt(2, urunID);

                int affectedRows = insertStmt.executeUpdate();

                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Başvurunuz başarıyla alındı. Başvuru durumunuzu 'Başvurularım' sayfasından takip edebilirsiniz.");
                    loadMusteriUrunleri();

                } else {
                    showAlert(Alert.AlertType.ERROR, "Hata", "Başvuru kaydı başarısız.");
                }
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Veritabanı Hatası", e.getMessage());
        }
    }

    @FXML
    public void getmusterianaekran(ActionEvent event) throws IOException {
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