package com.example.projeveriarayuz;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    // "localhost" demek "Bu bilgisayar" demektir.
    // Böylece Ahmet'in bilgisayarında Ahmet'in SQL'ine,
    // Ayşe'nin bilgisayarında Ayşe'nin SQL'ine bağlanır.
    // Eğer hata alırsanız "\\SQLEXPRESS" kısmını silip sadece "localhost" deneyin.
    // Eğer 1433 portu çalışmıyorsa instance ismini doğrudan verin:
// Not: "\" işareti Java'da özel karakter olduğu için "\\" şeklinde yazılır.
    private static final String SERVER_NAME = "localhost";

    private static final String DATABASE_NAME = "bankaveritabani";

    // Takım Standardı: Herkesin şifresi aynı olursa kod değiştirmeye gerek kalmaz.
    private static final String USER = "sa";
    private static final String PASSWORD = "elif1234";

    private static final String DB_URL =
            "jdbc:sqlserver://" + SERVER_NAME +":1433"+
                    ";databaseName=" + DATABASE_NAME +
                    ";encrypt=true;trustServerCertificate=true;"; // SSL ve Güvenlik ayarları

    public static Connection getConnection() throws SQLException {
        try {
            // Sürücüyü yükle
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Sürücüsü bulunamadı! Kütüphaneyi eklediğinizden emin olun.");
            throw new SQLException("Sürücü Hatası", e);
        }

        // Bağlantıyı döndür
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
}