module com.example.monitorscreen {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.monitorscreen to javafx.fxml;
    exports com.example.monitorscreen;
}