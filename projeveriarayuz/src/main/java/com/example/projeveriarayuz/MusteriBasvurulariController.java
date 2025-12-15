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

public class MusteriBasvurulariController {

    @FXML private TableView<Basvuru> basvuruTable;
    @FXML private TableColumn<Basvuru, String> urunAdiColumn;
    @FXML private TableColumn<Basvuru, String> tarihColumn;
    @FXML private TableColumn<Basvuru, String> durumColumn;
    @FXML private ComboBox<String> cmbBasvuruTuru;

    private int musteriId;

    private ObservableList<Basvuru> tumBasvurular = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (AppSession.isUserLoggedIn()) {
            this.musteriId = AppSession.getActiveMusteriId();
        }

        // Tablo Sütun Eşleştirmeleri
        urunAdiColumn.setCellValueFactory(new PropertyValueFactory<>("urunAdi"));
        tarihColumn.setCellValueFactory(new PropertyValueFactory<>("basvuruTarihi"));
        durumColumn.setCellValueFactory(new PropertyValueFactory<>("basvuruDurumu"));

        setupComboBox();
        loadBasvurular();
    }

    private void setupComboBox() {
        // Müşterinin başvurabileceği genel türleri ekliyoruz
        cmbBasvuruTuru.getItems().addAll("Tümü", "Kredi", "Kart", "Hesap", "Sigorta");
        cmbBasvuruTuru.getSelectionModel().selectFirst();

        // Filtreleme olayını bağlama
        cmbBasvuruTuru.setOnAction(event -> filtreleBasvurular(cmbBasvuruTuru.getValue()));
    }

    private void loadBasvurular() {
        if (this.musteriId <= 0) {
            basvuruTable.setItems(FXCollections.emptyObservableList());
            return;
        }

        tumBasvurular.clear();

        // SQL: Tüm başvuruları çekerken 4 farklı tabloya JOIN yapıyoruz (Product, KrediTurleri, KartTurleri, SigortaTurleri)
        String sql = "SELECT " +
                " B.BasvuruID, " +
                " B.BasvuruTarihi, " +
                " B.BasvuruDurumu, " +
                " P.UrunTipi, " +
                " P.Aciklama, " + // Product'tan genel açıklama (Hesap başvurusu gibi durumlarda kullanılabilir)
                " KT.Ad AS KrediAdi, " +
                " KA.Ad AS KartAdi, " +
                " S.SigortaAdi " +
                "FROM Basvuru B " +
                "LEFT JOIN Product P ON B.UrunID = P.UrunID " +
                "LEFT JOIN KrediTurleri KT ON B.KrediTurID = KT.KrediTurID " +
                "LEFT JOIN KartTurleri KA ON B.KartTurID = KA.KartTurID " +
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

                    String urunTipi = rs.getString("UrunTipi");
                    String krediAdi = rs.getString("KrediAdi");
                    String kartAdi = rs.getString("KartAdi");
                    String sigortaAdi = rs.getString("SigortaAdi");
                    String gorunecekAd;


                    if (krediAdi != null) {
                        gorunecekAd = "Kredi Başvurusu: " + krediAdi;
                    } else if (kartAdi != null) {
                        gorunecekAd = "Kart Başvurusu: " + kartAdi;
                    } else if (sigortaAdi != null) {
                        gorunecekAd = "Sigorta Başvurusu: " + sigortaAdi;
                    } else if (urunTipi != null) {
                        gorunecekAd = urunTipi + " Başvurusu";
                    } else {
                        gorunecekAd = "Tanımsız Başvuru";
                    }
                    tumBasvurular.add(new Basvuru(basvuruID, gorunecekAd, tarih, durum));
                }
            }

            basvuruTable.setItems(tumBasvurular);

        } catch (SQLException e) {
            System.err.println("Veritabanı Hatası (Başvurular): " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Hata", "Başvurular yüklenirken hata oluştu.");
        }
    }

    private void filtreleBasvurular(String secilenTur) {
        if ("Tümü".equals(secilenTur)) {
            basvuruTable.setItems(tumBasvurular);
            return;
        }

        ObservableList<Basvuru> filtrelenmisListe = FXCollections.observableArrayList();
        String filtre = secilenTur.toLowerCase();

        for (Basvuru basvuru : tumBasvurular) {

            if (basvuru.getUrunAdi().toLowerCase().contains(filtre)) {
                filtrelenmisListe.add(basvuru);
            }
        }

        basvuruTable.setItems(filtrelenmisListe);
    }

    @FXML
    void btnYeniBasvuruYapClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MusteriUrunler.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
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