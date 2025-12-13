package com.example.projeveriarayuz;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class SonTransferlerController implements Initializable {

    @FXML private TableView<Islem> tableIslemler;
    @FXML private TableColumn<Islem, String> colTarih;
    @FXML private TableColumn<Islem, String> colTur;
    @FXML private TableColumn<Islem, String> colHedef;
    @FXML private TableColumn<Islem, Double> colTutar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colTarih.setCellValueFactory(new PropertyValueFactory<>("islemTarihi"));
        colTur.setCellValueFactory(new PropertyValueFactory<>("islemTuru"));
        colHedef.setCellValueFactory(new PropertyValueFactory<>("hedefHesapNo"));
        colTutar.setCellValueFactory(new PropertyValueFactory<>("islemTutari"));
        verileriYukle();
    }

    private void verileriYukle() {
        ObservableList<Islem> liste = FXCollections.observableArrayList();
        String sql = "SELECT i.* FROM Islemler i JOIN Hesaplar h ON i.KaynakHesapNo = h.HesapNo WHERE h.MusteriID = ? ORDER BY i.IslemTarihi DESC";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, AppSession.getActiveMusteriId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Islem islem = new Islem();
                islem.setIslemId(rs.getInt("IslemID"));
                islem.setKaynakHesapNo(rs.getString("KaynakHesapNo"));
                islem.setHedefHesapNo(rs.getString("HedefHesapNo"));
                islem.setIslemTuru(rs.getString("IslemTuru"));
                islem.setIslemTutari(rs.getDouble("IslemTutari"));
                islem.setIslemTarihi(rs.getTimestamp("IslemTarihi"));
                liste.add(islem);
            }
            tableIslemler.setItems(liste);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void geriDon(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MüsteriParaTransferi.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}