package com.example.projeveriarayuz;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory; // Sütun eşleştirme için eklendi
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MusteriUrunlerController {

    @FXML private ComboBox<ProductModel> urunSecimComboBox;
    @FXML private TableView<MusteriUrunModel> sahipOlunanUrunlerTable;
    @FXML private TableColumn<MusteriUrunModel, String> hesapNoColumn;
    @FXML private TableColumn<MusteriUrunModel, String> urunTipiColumn;
    @FXML private TableColumn<MusteriUrunModel, String> baslangicTarihiColumn;
    private int activeMusteriId = 0;
    private ProductDAO productDAO = new ProductDAO();
    private MusteriUrunDAO musteriUrunDAO = new MusteriUrunDAO();
    private ObservableList<ProductModel> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupProductTable();
        loadProducts();
    }

    private void setupProductTable() {


        hesapNoColumn.setCellValueFactory(new PropertyValueFactory<>("hesapNumarasi"));
        urunTipiColumn.setCellValueFactory(new PropertyValueFactory<>("urunTipi"));
        baslangicTarihiColumn.setCellValueFactory(new PropertyValueFactory<>("baslangicTarihi"));

        sahipOlunanUrunlerTable.getColumns().clear();
        sahipOlunanUrunlerTable.getColumns().addAll(hesapNoColumn, urunTipiColumn, baslangicTarihiColumn);
    }

    public void setMusteriId(int musteriId) {
        this.activeMusteriId = musteriId;
        loadMusteriUrunleri();
    }


    private void loadProducts() {
        productList.clear();
        try (ResultSet rs = productDAO.getAllProducts()) {
            while (rs != null && rs.next()) {
                int id = rs.getInt("UrunID");
                String tipi = rs.getString("UrunTipi");
                String aciklama = rs.getString("Aciklama");
                // ProductModel.java'da Açıklama (Aciklama) kullanılmıyorsa bile, çekilebilir.
                productList.add(new ProductModel(id, tipi, aciklama));
            }
        } catch (SQLException e) {
            System.err.println("Ürün kataloğu yüklenirken hata: " + e.getMessage());
        }
        urunSecimComboBox.setItems(productList);
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
                String aciklama = rs.getString("Aciklama"); // Açıklama Model'e geçiyor
                String baslangicTarihi = rs.getString("BaslangicTarihi");

                musteriUrunListesi.add(new MusteriUrunModel(hesapNo, urunTipi, aciklama, baslangicTarihi));
            }
        } catch (SQLException e) {
            System.err.println("Müşteriye ait ürünler yüklenemedi: " + e.getMessage());
        }

        sahipOlunanUrunlerTable.setItems(musteriUrunListesi);
    }
    @FXML
    public void urunSatinAlmaIslemi(ActionEvent event) {
        ProductModel selectedProduct = urunSecimComboBox.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Seçim Eksik", "Lütfen satın almak istediğiniz bir ürünü seçin.");
            return;
        }

        if (activeMusteriId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Müşteri kimliği belirlenemedi. Giriş yapın.");
            return;
        }
        int urunID = selectedProduct.getUrunID();

        if (musteriUrunDAO.urunSatinAl(activeMusteriId, urunID)) {
            showAlert(Alert.AlertType.INFORMATION, "Başarılı", selectedProduct.getUrunTipi() + " başarıyla satın alındı!");
            loadMusteriUrunleri(); // Tabloyu yenile
        } else {
            showAlert(Alert.AlertType.ERROR, "Hata", "Satın alma işlemi başarısız oldu (Aynı ürün zaten kayıtlı olabilir / SQL Hatası).");
        }
    }

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