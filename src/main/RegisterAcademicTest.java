package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.controllers.ControllerCreateAcademicWindow;
import userinterface.windows.CreateAcademicWindow;

public class RegisterAcademicTest extends Application {
    @Override
    public void start(Stage primaryStage) {
        CreateAcademicWindow window = new CreateAcademicWindow();
        ControllerCreateAcademicWindow controller = new ControllerCreateAcademicWindow(window);
        Scene scene = new Scene(window.getView(), 600, 400);
        primaryStage.setTitle("Registrar Académico");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}