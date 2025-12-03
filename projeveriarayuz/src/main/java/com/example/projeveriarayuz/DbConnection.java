package com.example.projeveriarayuz;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {


    private static final String SERVER_NAME = "DESKTOP-9PU864R";
    private static final String DATABASE_NAME = "bankaveritabanı";

    // SQL Server Authentication (SA) Bilgileri
    private static final String USER = "sa";
    private static final String PASSWORD = "elif1234";


    private static final String DB_URL =
            "jdbc:sqlserver://" + SERVER_NAME +
                    ";databaseName=" + DATABASE_NAME +
                    ";trustServerCertificate=true;"; // SSL hatası için

    public static Connection getConnection() throws SQLException {
        try {

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("SQL Server JDBC Sürücüsü bulunamadı. Projenizi kontrol edin.");
            throw new SQLException("JDBC Sürücüsü Hatası", e);
        }

        // Bağlantıyı kullanıcı adı ve şifre ile kur
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
}