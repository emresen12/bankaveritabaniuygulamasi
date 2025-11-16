package com.example.projeveriarayuz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class müsterianaekrancontroller {
    @FXML
    private Label bakiyeLabel;
    @FXML
    private Label isimLabel;
    @FXML
    private Label tcLabel;

    private final String pathMusteriler = "C:\\Users\\Admin\\OneDrive\\Masaüstü\\projeler\\projeveri-main\\projeveri-main\\veriyapilariprojee\\database.json";
    private String kullaniciTC;

    public void setKullaniciTC(String tc) {
        this.kullaniciTC = tc;
        initialize();
        System.out.println("Ana ekran controller'a TC geldi: " + tc);
    }

    // --- Buttonlar ---
    @FXML private Button ödemeİşlemleriButton;
    @FXML private Button paraTransferiButton;
    @FXML private Button sigortalarımButton;
    @FXML private Button hesaplarımButton;
    @FXML private Button ürünlerButton;

    // --- ImageView'ler ---
    @FXML private ImageView imgOdeme;
    @FXML private ImageView imgTransfer;
    @FXML private ImageView imgSigorta;
    @FXML private ImageView imgHesap;
    @FXML private ImageView imgUrun;


    // Genel fonksiyon (bütün ImageView'lar için çalışır)


    public void initialize() {
        if (kullaniciTC != null) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(pathMusteriler)), StandardCharsets.UTF_8);
                JSONArray musteriArray = new JSONArray(content);

                String bakiye = "0";
                String isim = "Bilinmiyor";

                for (int i = 0; i < musteriArray.length(); i++) {
                    JSONObject musteri = musteriArray.getJSONObject(i);
                    if (musteri.getString("Tc").equals(kullaniciTC)) {
                        bakiye = musteri.optString("bakiye", "0");
                        isim = musteri.optString("isim", "Bilinmiyor");
                        if (bakiye.isEmpty()) {
                            bakiye = "0";
                        }
                        break;
                    }

                }
                bindImageToButton(imgOdeme, ödemeİşlemleriButton);
                bindImageToButton(imgTransfer, paraTransferiButton);
                bindImageToButton(imgSigorta, sigortalarımButton);
                bindImageToButton(imgHesap, hesaplarımButton);
                bindImageToButton(imgUrun, ürünlerButton);

                bakiyeLabel.setText(bakiye + "$");
                isimLabel.setText(isim);
                tcLabel.setText(kullaniciTC);

            } catch (IOException e) {
                e.printStackTrace();
                bakiyeLabel.setText("0$");
                isimLabel.setText("Bilinmiyor");
                tcLabel.setText("-");
            }
        } else {
            bakiyeLabel.setText("0$");
            isimLabel.setText("Bilinmiyor");
            tcLabel.setText("-");
        }
    }
    public void bindImageToButton(ImageView imageView, Button button) {
        imageView.setOnMouseClicked(e -> button.fire());
    }

    public void getmusteriparacekme(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hesaplarim.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void getmusteriparayatırma(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MüsteriParaTransferi.fxml"));
        Parent root = loader.load();

        // Controller'ı al ve TC'yi aktar
        MüsteriParaTransferi controller = loader.getController();
        controller.setKullaniciTC(kullaniciTC); // <-- BURASI ÖNEMLİ

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    public void getMüsteriÜrünler(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MüsteriÜrünler.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
    public void getOdemeİslemleri(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MusteriOdemeislemleri.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
    public void getsigortalarim(ActionEvent event) throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sigortalarim.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }
}
