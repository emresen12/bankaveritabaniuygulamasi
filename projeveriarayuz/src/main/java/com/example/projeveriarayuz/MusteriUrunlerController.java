package com.example.projeveriarayuz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MusteriUrunlerController {

    // FXML'de tanımlanacak ComboBox (Katalogdaki ürünleri seçmek için)
    @FXML private ComboBox<ProductModel> urunSecimComboBox;
    // FXML'de tanımlanacak TableView (Müşterinin sahip olduğu ürünleri göstermek için)
    @FXML private TableView<MusteriUrunModel> sahipOlunanUrunlerTable;
    @FXML private VBox urunEklemeKutusu; // FXML'den ürün ekleme kutusunu gizlemek/göstermek için

    private int activeMusteriId = 1;

    private ProductDAO productDAO = new ProductDAO();
    private MusteriUrunDAO musteriUrunDAO = new MusteriUrunDAO();
    private ObservableList<ProductModel> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Müşteri ID'si ayarlandıktan sonra ürünler yüklenmeli
        loadProducts(); // Satın alınabilir ürünleri yükler
        loadMusteriUrunleri(); // Sahip olunan ürünleri yükler
    }

    // Giriş kontrolünden gelen müşteri ID'sini ayarlar
    public void setMusteriId(int musteriId) {
        this.activeMusteriId = musteriId;
        // Eğer initialize metodu zaten çalıştıysa, ID ayarlandıktan sonra tabloları yenile
        loadMusteriUrunleri();
    }


    private void loadProducts() {
        // ProductDAO'dan tüm ürünleri çek ve ComboBox'a doldur
        productList.clear();
        try (ResultSet rs = productDAO.getAllProducts()) {
            while (rs != null && rs.next()) {
                int id = rs.getInt("UrunID");
                String tipi = rs.getString("UrunTipi");
                String aciklama = rs.getString("Aciklama");
                productList.add(new ProductModel(id, tipi, aciklama));
            }
        } catch (SQLException e) {
            System.err.println("Ürün kataloğu yüklenirken hata: " + e.getMessage());
        }
        urunSecimComboBox.setItems(productList);
    }

    private void loadMusteriUrunleri() {
        // MusteriUrunDAO'dan müşterinin sahip olduğu ürünleri çek ve Tabloya doldur
        // (Bu kısım için MusteriUrunModel ve TableView sütun tanımları FXML'de yapılmalıdır)

        // Örnek olarak sadece konsola yazdıralım
        try (ResultSet rs = musteriUrunDAO.musteriUrunleriniGetir(activeMusteriId)) {
            while (rs != null && rs.next()) {
                System.out.println("Sahip olunan ürün: " + rs.getString("UrunTipi") +
                        " Hesap No: " + rs.getString("HesapNumarasi"));
            }
        } catch (SQLException e) {
            System.err.println("Müşteriye ait ürünler yüklenemedi: " + e.getMessage());
        }
    }

    @FXML
    public void urunSatinAlmaIslemi(ActionEvent event) {
        ProductModel selectedProduct = urunSecimComboBox.getSelectionModel().getSelectedItem();

        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Seçim Eksik", "Lütfen satın almak istediğiniz bir ürünü seçin.");
            return;
        }

        int urunID = selectedProduct.getUrunID();

        // Satın alma işlemini yap
        if (musteriUrunDAO.urunSatinAl(activeMusteriId, urunID)) {
            showAlert(Alert.AlertType.INFORMATION, "Başarılı", selectedProduct.getUrunTipi() + " başarıyla satın alındı!");
            loadMusteriUrunleri(); // Tabloyu yenile
        } else {
            showAlert(Alert.AlertType.ERROR, "Hata", "Satın alma işlemi başarısız oldu (TC zaten ürüne sahip olabilir / SQL Hatası).");
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