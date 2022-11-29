module com.johanpmeert.wachtwoordv4 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.johanpmeert.wachtwoordv4 to javafx.fxml;
    exports com.johanpmeert.wachtwoordv4;
}