package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.windows.RegistProyectWindow;
import userinterface.controllers.ControllerRegistProyectWindow;

public class RegistProyectTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        RegistProyectWindow registWindow = new RegistProyectWindow();
        new ControllerRegistProyectWindow(registWindow);
        Scene scene = new Scene(registWindow.getView(), 500, 400);
        primaryStage.setTitle("Registrar Proyecto");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}