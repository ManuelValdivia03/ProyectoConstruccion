package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.controllers.ControllerCreateStudentWindow;
import userinterface.windows.CreateStudentWindow;

import static javafx.application.Application.launch;

public class RegisterStudentTest extends Application {
    public void start(Stage primaryStage) {
        CreateStudentWindow window = new CreateStudentWindow();
        ControllerCreateStudentWindow controller = new ControllerCreateStudentWindow(window);
        Scene scene = new Scene(window.getView(), 600, 400);
        primaryStage.setTitle("Registrar Estudiante");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
