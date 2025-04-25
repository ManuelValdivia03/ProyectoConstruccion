package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.windows.CreateAcademicWindow;
import userinterface.controllers.ControllerCreateAcademicWindow;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        CreateAcademicWindow window = new CreateAcademicWindow();
        new ControllerCreateAcademicWindow(window);

        Scene scene = new Scene(window.getView(), 500, 500);
        primaryStage.setTitle("Registro de Académicos");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}