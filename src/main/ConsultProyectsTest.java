package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import userinterface.controllers.ControllerConsultProyectsWindow;
import userinterface.windows.ConsultProyectsWindow;


public class ConsultProyectsTest extends Application{
    @Override
    public void start(Stage stage) throws Exception {
        ConsultProyectsWindow consultWindow = new ConsultProyectsWindow();
        new ControllerConsultProyectsWindow(consultWindow);

        Stage consultStage = new Stage();
        consultStage.setScene(new Scene(consultWindow.getView(), 800, 600));
        consultStage.setTitle("Consulta de Proyectos");
        consultStage.show();
    }
}
