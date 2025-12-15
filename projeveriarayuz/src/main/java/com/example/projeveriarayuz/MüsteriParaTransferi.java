package com.example.projeveriarayuz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MüsteriParaTransferi {

    @FXML
    public void getmusterianaekran(ActionEvent event) throws IOException {
        changeScene(event, "müsterianaekran.fxml");
    }

    @FXML
    public void goToKayitliKisi(ActionEvent event) throws IOException {
        changeScene(event, "KayitliKisiyeTransfer.fxml");
    }

    @FXML
    public void goToIbanTransfer(ActionEvent event) throws IOException {
        changeScene(event, "IbanaTransfer.fxml");
    }

    @FXML
    public void goToSonTransferler(ActionEvent event) throws IOException {
        changeScene(event, "SonTransferYapilanKisiler.fxml");
    }

    private void changeScene(ActionEvent event, String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}