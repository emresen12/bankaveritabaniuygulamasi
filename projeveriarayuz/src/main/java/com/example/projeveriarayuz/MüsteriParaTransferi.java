package com.example.projeveriarayuz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MüsteriParaTransferi {

    // Geri Butonu (Müşteri Ana Ekranına Döner)
    @FXML
    public void getmusterianaekran(ActionEvent event) throws IOException {
        changeScene(event, "müsterianaekran.fxml");
    }

    // 1. Kayıtlı Kişiye Transfer Sayfasına Git
    @FXML
    public void goToKayitliKisi(ActionEvent event) throws IOException {
        changeScene(event, "KayitliKisiyeTransfer.fxml");
    }

    // 2. IBAN'a Transfer Sayfasına Git
    @FXML
    public void goToIbanTransfer(ActionEvent event) throws IOException {
        changeScene(event, "IbanaTransfer.fxml");
    }

    // 3. Son Transferler Sayfasına Git
    @FXML
    public void goToSonTransferler(ActionEvent event) throws IOException {
        changeScene(event, "SonTransferYapilanKisiler.fxml");
    }

    // Yardımcı Metot: Kod tekrarını önlemek için sahne geçişlerini yapan metot
    private void changeScene(ActionEvent event, String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}