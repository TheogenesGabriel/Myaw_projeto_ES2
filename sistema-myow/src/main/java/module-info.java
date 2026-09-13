module com.myow {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.myow to javafx.fxml;
    exports com.myow;
}
