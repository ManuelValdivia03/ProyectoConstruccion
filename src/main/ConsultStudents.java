package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.controllers.ControllerConsultStudentsWindow;
import userinterface.windows.ConsultStudentsWindow;

public class ConsultStudents extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        ConsultStudentsWindow consultWindow = new ConsultStudentsWindow();
        Stage consultStage = new Stage();

        new ControllerConsultStudentsWindow(consultWindow, consultStage);

        Scene scene = new Scene(consultWindow.getView(), 1200, 500);
        consultStage.setScene(scene);
        consultStage.setTitle("Consulta de Estudiantes");

        consultStage.show();
    }
}
