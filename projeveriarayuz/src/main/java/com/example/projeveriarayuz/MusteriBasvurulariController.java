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
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MusteriBasvurulariController {

    @FXML private TableView<Basvuru> basvuruTable;
    @FXML private TableColumn<Basvuru, String> urunAdiColumn;
    @FXML private TableColumn<Basvuru, String> tarihColumn;
    @FXML private TableColumn<Basvuru, String> durumColumn;
    @FXML private ComboBox<String> urunBasvuruComboBox;

    private int musteriId;
    private Map<String, Integer> urunAdiToIdMap = new HashMap<>();

    public void setMusteriId(int musteriId) {
        this.musteriId = musteriId;
    }

    @FXML
    public void initialize() {
        if (AppSession.isUserLoggedIn()) {
            this.musteriId = AppSession.getActiveMusteriId();
        }

        // ⭐ PROPERTY DEĞERLERİ KONTROL EDİLDİ ⭐
        urunAdiColumn.setCellValueFactory(new PropertyValueFactory<>("urunAdi"));
        tarihColumn.setCellValueFactory(new PropertyValueFactory<>("basvuruTarihi"));
        durumColumn.setCellValueFactory(new PropertyValueFactory<>("basvuruDurumu"));

        loadAvailableProducts();
        loadBasvurular();
    }

    private void loadAvailableProducts() {
        ObservableList<String> urunler = FXCollections.observableArrayList();
        String sql = "SELECT UrunID, UrunTipi, Aciklama FROM Product";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int sayac = 0;
            while (rs.next()) {
                int id = rs.getInt("UrunID");
                String tipi = rs.getString("UrunTipi");
                String aciklama = rs.getString("Aciklama");

                String birlesikUrunMetni = tipi + " (" + aciklama + ")";

                urunler.add(birlesikUrunMetni);
                urunAdiToIdMap.put(birlesikUrunMetni, id);
                sayac++;
            }
            urunBasvuruComboBox.setItems(urunler);

            if (sayac == 0) {
                System.out.println("DEBUG: Product tablosunda hiç ürün bulunamadı.");
            }

        } catch (SQLException e) {
            System.err.println("Veritabanı Hatası (Ürünler): " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Veritabanı Hatası",
                    "Ürün listesi yüklenirken bağlantı sorunu oluştu.");
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
                                secilenUrunAdi + " ürünü için zaten beklemede bir başvurunuz bulunmaktadır.");
                        return;
                    }
                }
            }

            // 2. Yeni Başvuruyu Ekleme
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, this.musteriId);
                insertStmt.setInt(2, urunID);

                int affectedRows = insertStmt.executeUpdate();

                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Başvurunuz başarıyla alındı ve incelenmeye başlandı.");
                    loadBasvurular(); // ⭐ BAŞARILI İŞLEM SONRASI TABLO YENİLEME ⭐
                } else {
                    showAlert(Alert.AlertType.ERROR, "Hata", "Başvuru kaydı yapılırken bir sorun oluştu.");
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL Hatası: Başvuru yapılamadı. " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Veritabanı Hatası", "Başvuru sırasında sunucu hatası oluştu.");
        }
    }

    private void loadBasvurular() {
        if (this.musteriId <= 0) {
            basvuruTable.setItems(FXCollections.observableArrayList()); // Boş liste ata
            return;
        }

        ObservableList<Basvuru> basvuruListesi = FXCollections.observableArrayList();

        // SQL: Product tablosundan UrunTipi ve Aciklama çekiliyor
        String sql = "SELECT B.BasvuruID, B.BasvuruTarihi, B.BasvuruDurumu, P.UrunTipi, P.Aciklama " +
                "FROM Basvuru B " +
                "JOIN Product P ON B.UrunID = P.UrunID " +
                "WHERE B.MusteriID = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.musteriId);
            try (ResultSet rs = stmt.executeQuery()) {

                int sayac = 0;
                while (rs.next()) {
                    int basvuruID = rs.getInt("BasvuruID");
                    String urunTipi = rs.getString("UrunTipi");
                    String aciklama = rs.getString("Aciklama");

                    String urunTipiVeAciklama = urunTipi + " (" + aciklama + ")";

                    // Basit string çekimi
                    String tarih = rs.getString("BasvuruTarihi");

                    String durum = rs.getString("BasvuruDurumu");

                    basvuruListesi.add(new Basvuru(basvuruID, urunTipiVeAciklama, tarih, durum));
                    sayac++;
                }

                System.out.println("DEBUG: " + sayac + " adet başvuru yüklendi.");

            }
            basvuruTable.setItems(basvuruListesi); // Tabloya listeyi ata

        } catch (SQLException e) {
            System.err.println("Veritabanı Hatası (Başvurular): " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Hata", "Mevcut başvurular yüklenirken sunucu sorunu oluştu.");
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