package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.controllers.ControllerCreateLinkedOrganizationWindow;
import userinterface.windows.CreateLinkedOrganizationWindow;

public class RegisterLinkedOreganizationTest extends Application {
    public void start(Stage primaryStage) {
        CreateLinkedOrganizationWindow createWindow = new CreateLinkedOrganizationWindow();
        ControllerCreateLinkedOrganizationWindow controller = new ControllerCreateLinkedOrganizationWindow(createWindow);
        Stage stage = new Stage();
        stage.setScene(new Scene(createWindow.getView(), 400, 300));
        stage.setTitle("Registrar Organización Vinculada");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
