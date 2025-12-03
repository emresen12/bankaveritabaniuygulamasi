
package com.example.projeveriarayuz;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    private static final String SERVER_NAME = "DESKTOP-9PU864R";
    private static final String DATABASE_NAME = "bankaveritabanı";

    private static final String USER = "sa";
    private static final String PASSWORD = "elif1234";


    private static final String DB_URL =
            "jdbc:sqlserver://" + SERVER_NAME +
                    ";databaseName=" + DATABASE_NAME +
                    ";trustServerCertificate=true;"; // SSL sertifikası hatasını önlemek için (zorunlu değilse kaldırılabilir)

    /**
     * SQL Server veritabanına bir bağlantı (Connection) nesnesi döndürür.
     * * @return Bağlantı nesnesi (Connection)
     * @throws SQLException Bağlantı hatası oluşursa
     */
    public static Connection getConnection() throws SQLException {
        // SQL Server JDBC sürücüsünü yükler (modern Java'da genellikle bu satır gereksizdir,
        // ancak eski projeler için alışkanlık olarak tutulur).
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("SQL Server JDBC Sürücüsü bulunamadı. Projenize eklediğinizden emin olun.");
            throw new SQLException("JDBC Sürücüsü Hatası", e);
        }

        System.out.println("Veritabanına bağlanılıyor...");
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }


    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ SQL Server bağlantısı başarılı!");

        } catch (SQLException e) {
            System.err.println(" SQL Server bağlantısı başarısız!");
            e.printStackTrace();
        }
    }
}