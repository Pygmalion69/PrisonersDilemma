module eu.sergehelfrich.prisoner {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens eu.sergehelfrich.prisoner to javafx.fxml;
    exports eu.sergehelfrich.prisoner;
}