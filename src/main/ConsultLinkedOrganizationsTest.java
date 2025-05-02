package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.controllers.ControllerConsultLinkedOrganizationsWindow;
import userinterface.windows.ConsultLinkedOrganizationsWindow;

public class ConsultLinkedOrganizationsTest extends Application {
    public void start(Stage primaryStage) {
        ConsultLinkedOrganizationsWindow consultWindow = new ConsultLinkedOrganizationsWindow();
        Stage stage = new Stage();

        new ControllerConsultLinkedOrganizationsWindow(consultWindow, stage);

        Scene scene = new Scene(consultWindow.getView(), 800, 600);
        stage.setScene(scene);
        stage.setTitle("Consultar Organizaciones Vinculadas");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}