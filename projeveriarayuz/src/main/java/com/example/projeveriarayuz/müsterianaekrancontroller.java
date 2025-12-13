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

    public void setKullaniciVerileri(int musteriId, String tc, String isim) {
        System.out.println(" Ana Ekrana Ulasıldı.");
        updateScreenData();
    }
    @FXML
    public void initialize() {
        bindImageToButton(imgOdeme, ödemeİşlemleriButton);
        bindImageToButton(imgTransfer, paraTransferiButton);
        bindImageToButton(imgSigorta, sigortalarımButton);
        bindImageToButton(imgHesap, hesaplarımButton);
        bindImageToButton(imgUrun, ürünlerButton);

        updateScreenData();
    }

    private void updateScreenData() {
        int activeMusteriId = AppSession.getActiveMusteriId(); // ID'yi AppSession'dan al

        if (activeMusteriId > 0) {
            isimLabel.setText(AppSession.getKullaniciIsim());
            tcLabel.setText(AppSession.getKullaniciTC());
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
        int currentId = AppSession.getActiveMusteriId(); // ID'yi Session'dan al

        if (currentId <= 0) {
            System.err.println("Müşteri ID aktarılmadı. İşlem iptal edildi.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("MüsteriÜrünler.fxml"));
        Parent root = loader.load();
        MusteriUrunlerController urunlerController = loader.getController();
        // ID'yi alt Controller'a aktar
        urunlerController.setMusteriId(currentId);

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void getmusteriparacekme(ActionEvent event) throws IOException {
        // ID kontrolü gerekliyse: AppSession.getActiveMusteriId() kullanın.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("hesaplarim.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void getmusteriparayatırma(ActionEvent event) throws IOException {
        // ID kontrolü gerekliyse: AppSession.getActiveMusteriId() kullanın.
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
        // ID kontrolü gerekliyse: AppSession.getActiveMusteriId() kullanın.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sigortalarim.fxml"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    public void handleLogout(ActionEvent event) throws IOException {
        AppSession.clearSession(); // Oturumu temizle
        FXMLLoader loader = new FXMLLoader(getClass().getResource("müsterigirissekmesi.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Müşteri Girişi");
        stage.show();
    }
}