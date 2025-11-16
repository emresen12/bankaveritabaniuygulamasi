package com.example.projeveriarayuz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import java.awt.*;
import java.io.IOException;

public class müsterigirissekmesicontroller {
    @FXML
    TextField name;
    @FXML
    PasswordField tcField;
    @FXML
    public void getmusterianaekran(ActionEvent event) throws IOException {

        try {
            String tc = tcField.getText().trim();
            String isim = name.getText().trim();

            // Dosya yolları
            String pathKuyruk = "C:\\Users\\Admin\\OneDrive\\Masaüstü\\projeler\\projeveri-main\\projeveri-main\\veriyapilariprojee\\paracekmekuyruğu.json";
            String pathDatabase = "C:\\Users\\Admin\\OneDrive\\Masaüstü\\projeler\\projeveri-main\\projeveri-main\\veriyapilariprojee\\database.json";

            // JSON dizilerini oku
            JSONArray kuyrukArray = loadJsonArray(pathKuyruk);
            JSONArray databaseArray = loadJsonArray(pathDatabase);

            // Kuyruğu yükle
            Kuyruk musteriKuyrugu = oncelikliKuyruguHazirla(kuyrukArray);

            // Yeni müşteri objesi oluştur
            JSONObject yeniMusteri = new JSONObject();
            yeniMusteri.put("isim", isim);
            yeniMusteri.put("Tc", tc);
            yeniMusteri.put("öncelik", false);  // Normal müşteri
            yeniMusteri.put("bakiye", "0");

            // Kuyruğa ekle (sonuna)
            musteriKuyrugu.enqueue(new Node(yeniMusteri));

            // Kuyruğu JSON dosyasına yaz
            saveKuyrukToJson(pathKuyruk, musteriKuyrugu);

            // Eğer TC database'de yoksa ekle
            if (!tcVarMi(databaseArray, tc)) {
                databaseArray.put(yeniMusteri);
                saveJsonArray(pathDatabase, databaseArray);
            }
            // FXML yüklemesi ve TC aktarımı
            FXMLLoader loader = new FXMLLoader(getClass().getResource("müsterianaekran.fxml"));
            Parent root = loader.load();

             // Doğru controller sınıfını kullan
            müsterianaekrancontroller controller = loader.getController(); // DOĞRU
            controller.setKullaniciTC(tc);
            // Sahneyi değiştir
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();


        } catch (Exception e) {

            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        // Sayfa geçişi

    }

    private JSONArray loadJsonArray(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
                return new JSONArray(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    private void saveJsonArray(String path, JSONArray array) {
        try {
            Files.write(Paths.get(path), array.toString(2).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean tcVarMi(JSONArray array, String tc) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj.getString("Tc").equals(tc)) {
                return true;
            }
        }
        return false;
    }

    private Kuyruk oncelikliKuyruguHazirla(JSONArray jsonArray) {
        Kuyruk kuyruk = new Kuyruk();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject musteri = jsonArray.getJSONObject(i);
            Node yeniNode = new Node(musteri);

            if (musteri.optBoolean("öncelik", false)) {
                // Önceliklileri başa ekle
                yeniNode.next = kuyruk.front;
                kuyruk.front = yeniNode;
                if (kuyruk.rear == null) {
                    kuyruk.rear = yeniNode;
                }
            } else {
                // Normal müşterileri sona ekle
                kuyruk.enqueue(yeniNode);
            }
        }

        return kuyruk;
    }

    private void saveKuyrukToJson(String path, Kuyruk kuyruk) {
        JSONArray array = new JSONArray();
        Node current = kuyruk.front;
        while (current != null) {
            array.put(current.veri);
            current = current.next;
        }
        saveJsonArray(path, array);
    }
}