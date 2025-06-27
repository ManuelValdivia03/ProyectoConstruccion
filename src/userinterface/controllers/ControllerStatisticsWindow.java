package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.daos.ProjectDAO;
import logic.daos.ProjectStudentDAO;
import logic.daos.StudentDAO;
import logic.logicclasses.Project;
import logic.logicclasses.Student;
import userinterface.windows.StatisticsWindow;
import logic.services.ExceptionManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ControllerStatisticsWindow implements EventHandler<ActionEvent> {
    private final StatisticsWindow view;
    private final StudentDAO studentDAO;
    private final ProjectStudentDAO projectStudentDAO;
    private final ProjectDAO projectDAO;
    private final Stage stage;
    private final Consumer<Void> refreshCallback;

    public ControllerStatisticsWindow(Stage parentStage, Consumer<Void> refreshCallback) {
        this.view = new StatisticsWindow();
        this.studentDAO = new StudentDAO();
        this.projectStudentDAO = new ProjectStudentDAO();
        this.projectDAO = new ProjectDAO();
        this.refreshCallback = refreshCallback;

        this.stage = new Stage();
        stage.initOwner(parentStage);
        stage.setScene(new Scene(view.getView(), 900, 700));
        stage.setTitle("Estadísticas Académicas");

        setupEventHandlers();
        loadStatistics();
    }

    private void setupEventHandlers() {
        view.getRefreshButton().setOnAction(this);
        view.getCloseButton().setOnAction(this);
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getRefreshButton()) {
            loadStatistics();
            if (refreshCallback != null) {
                refreshCallback.accept(null);
            }
        } else if (event.getSource() == view.getCloseButton()) {
            stage.close();
        }
    }

    private void loadStatistics() {
        try {
            loadProjectStatistics();

//            loadStudentsPerProject();
//
//            loadProjectCapacityStats();

            view.showMessage("Datos actualizados correctamente", false);

        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al cargar estadísticas: " + message, true);
        }
    }

    private void loadProjectStatistics() throws SQLException {
        List<Student> activeStudents = studentDAO.getSudentsByStatus('A');
        int withProject = 0;
        int withoutProject = 0;

        for (Student student : activeStudents) {
            Integer projectId = projectStudentDAO.getProyectByStudent(student.getIdUser());
            if (projectId != null && projectId > 0) {
                withProject++;
            } else {
                withoutProject++;
            }
        }

        view.updateProjectsChart(withProject, withoutProject);
    }

    private void loadStudentsPerProject() throws SQLException {
        Map<String, Integer> studentsPerProject = new HashMap<>();
        List<Project> projects = projectDAO.getAllProyects();

        for (Project project : projects) {
            List<Integer> studentIds = projectStudentDAO.getStudentsByProyect(project.getIdProyect());
            studentsPerProject.put(project.getTitle(), studentIds.size());
        }

        view.updateStudentsPerProjectChart(studentsPerProject);
    }

    private void loadProjectCapacityStats() throws SQLException {
        Map<String, int[]> capacityStats = new HashMap<>();
        List<Project> projects = projectDAO.getAllProyects();

        for (Project project : projects) {
            capacityStats.put(project.getTitle(),
                    new int[]{project.getCurrentStudents(), project.getCapacity()});
        }

        view.updateCapacityChart(capacityStats);
    }

    public void show() {
        stage.show();
    }
}