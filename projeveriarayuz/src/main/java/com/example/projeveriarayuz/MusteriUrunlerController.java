package com.example.projeveriarayuz;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class MusteriUrunlerController {

    private int musteriId;

    @FXML
    public void initialize() {
        if (AppSession.isUserLoggedIn()) {
            this.musteriId = AppSession.getActiveMusteriId();
        }

    }

    @FXML
    void btnHesapIslemleriClicked(ActionEvent event) throws IOException {

        loadScene(event, "hesaplarim.fxml");
    }

    @FXML
    void btnKrediIslemleriClicked(ActionEvent event) throws IOException {
        loadScene(event, "KrediBasvuru.fxml");
    }

    @FXML
    void btnKartIslemleriClicked(ActionEvent event) throws IOException {

        loadScene(event, "KartBasvuru.fxml");
    }

    @FXML
    void btnSigortaIslemleriClicked(ActionEvent event) throws IOException {
        loadScene(event, "sigortalarim.fxml");
    }


    @FXML
    public void getmusterianaekran(ActionEvent event) throws IOException {
        loadScene(event, "müsterianaekran.fxml");
    }

    private void loadScene(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
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