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
import java.io.IOException;

public class müsterianaekrancontroller {

    // FXML Labels
    @FXML private Label bakiyeLabel;
    @FXML private Label isimLabel;
    @FXML private Label tcLabel;
    // FXML Buttons/ImageViews
    @FXML private Button ödemeİşlemleriButton;
    @FXML private Button paraTransferiButton;
    @FXML private Button sigortalarımButton;
    @FXML private Button hesaplarımButton;
    @FXML private Button ürünlerButton;

    @FXML private ImageView imgOdeme;
    @FXML private ImageView imgTransfer;
    @FXML private ImageView imgSigorta;
    @FXML private ImageView imgHesap;
    @FXML private ImageView imgUrun;
    private int activeMusteriId = 0;
    private String kullaniciTC;
    private String kullaniciIsim;

    public void setKullaniciVerileri(int musteriId, String tc, String isim) {
        this.activeMusteriId = musteriId;
        this.kullaniciTC = tc;
        this.kullaniciIsim = isim;

        System.out.println(" Ana Ekrana Basarıyla Ulasan ID: " + this.activeMusteriId);
        updateScreenData();
    }

    @FXML
    public void initialize() {
        bindImageToButton(imgOdeme, ödemeİşlemleriButton);
        bindImageToButton(imgTransfer, paraTransferiButton);
        bindImageToButton(imgSigorta, sigortalarımButton);
        bindImageToButton(imgHesap, hesaplarımButton);
        bindImageToButton(imgUrun, ürünlerButton);

        if (activeMusteriId == 0) {
            updateScreenData();
        }
    }

    private void updateScreenData() {

        if (activeMusteriId > 0) {
            isimLabel.setText(kullaniciIsim);
            tcLabel.setText(kullaniciTC);
            bakiyeLabel.setText("1500.00 TL");
        } else {
            isimLabel.setText("Giriş Yapılmadı");
            tcLabel.setText("-");
            bakiyeLabel.setText("0$");
        }
    }
    public void bindImageToButton(ImageView imageView, Button button) {
        if (imageView != null && button != null) {
            imageView.setOnMouseClicked(e -> button.fire());
        }
    }
    public void getMüsteriÜrünler(ActionEvent event) throws IOException{

        if (activeMusteriId <= 0) {
            System.err.println(" Müşteri ID aktarılmadı. İşlem iptal edildi.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MüsteriÜrünler.fxml"));
        Parent root = loader.load();
        MusteriUrunlerController urunlerController = loader.getController();
        urunlerController.setMusteriId(activeMusteriId);
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
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
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
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