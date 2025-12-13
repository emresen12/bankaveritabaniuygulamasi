package com.example.projeveriarayuz;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MusteriUrunlerController {


    @FXML private TableView<MusteriUrunModel> sahipOlunanUrunlerTable;
    @FXML private TableColumn<MusteriUrunModel, String> hesapNoColumn;
    @FXML private TableColumn<MusteriUrunModel, String> urunTipiColumn;
    @FXML private TableColumn<MusteriUrunModel, String> baslangicTarihiColumn;

    private int activeMusteriId = 0;

    private MusteriUrunDAO musteriUrunDAO = new MusteriUrunDAO();


    @FXML
    public void initialize() {
        setupProductTable();

        if (AppSession.isUserLoggedIn()) {
            this.activeMusteriId = AppSession.getActiveMusteriId();
            loadMusteriUrunleri();
        }
    }

    private void setupProductTable() {
        hesapNoColumn.setCellValueFactory(new PropertyValueFactory<>("hesapNumarasi"));
        urunTipiColumn.setCellValueFactory(new PropertyValueFactory<>("urunTipi"));
        baslangicTarihiColumn.setCellValueFactory(new PropertyValueFactory<>("baslangicTarihi"));


    }

    public void setMusteriId(int musteriId) {
        this.activeMusteriId = musteriId;
        loadMusteriUrunleri();
    }



    private void loadMusteriUrunleri() {
        ObservableList<MusteriUrunModel> musteriUrunListesi = FXCollections.observableArrayList();

        if (activeMusteriId <= 0) {
            sahipOlunanUrunlerTable.setItems(musteriUrunListesi);
            return;
        }

        try (ResultSet rs = musteriUrunDAO.musteriUrunleriniGetir(activeMusteriId)) {
            while (rs != null && rs.next()) {
                String hesapNo = rs.getString("HesapNumarasi");
                String urunTipi = rs.getString("UrunTipi");
                String aciklama = rs.getString("Aciklama");
                String baslangicTarihi = rs.getString("BaslangicTarihi");

                musteriUrunListesi.add(new MusteriUrunModel(hesapNo, urunTipi, aciklama, baslangicTarihi));
            }
        } catch (SQLException e) {
            System.err.println("Müşteriye ait ürünler yüklenemedi: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Veritabanı Hatası", "Müşteri ürünleri yüklenemedi.");
        }

        sahipOlunanUrunlerTable.setItems(musteriUrunListesi);
    }

    // ⭐ KALDIRILDI: @FXML public void urunSatinAlmaIslemi(ActionEvent event) { ... }

    @FXML
    public void getmusterianaekran(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("müsterianaekran.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
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