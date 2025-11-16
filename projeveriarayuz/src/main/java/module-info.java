module com.example.projeveriarayuz {

    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.projeveriarayuz to javafx.fxml;
    exports com.example.projeveriarayuz;
    requires org.json;
    requires java.desktop;
}