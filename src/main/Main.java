package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.controllers.ControllerCreateStudentWindow;
import userinterface.windows.CreateStudentWindow;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        CreateStudentWindow window = new CreateStudentWindow();
        new ControllerCreateStudentWindow(window);

        Scene scene = new Scene(window.getView(), 500, 500);
        primaryStage.setTitle("Registro de Estudiantes");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}