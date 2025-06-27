package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Toggle;
import javafx.stage.Stage;
import javafx.util.Callback;
import logic.daos.EvaluationDAO;
import logic.daos.PresentationDAO;
import logic.daos.ProjectDAO;
import logic.daos.ProjectStudentDAO;
import logic.daos.StudentDAO;
import logic.logicclasses.Academic;
import logic.logicclasses.Evaluation;
import logic.logicclasses.Presentation;
import logic.logicclasses.Project;
import logic.logicclasses.Student;
import logic.enums.PresentationType;
import logic.services.ExceptionManager;
import userinterface.windows.RegistEvaluationWindow;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

public class ControllerRegistEvaluationWindow {
    private final RegistEvaluationWindow view;
    private final Stage stage;
    private final Academic evaluator;

    public ControllerRegistEvaluationWindow(RegistEvaluationWindow view, Stage stage, Academic evaluator) {
        this.view = view;
        this.stage = stage;
        this.evaluator = evaluator;
        setupListeners();
        loadProjectsAndSetupComboBox();
    }

    private void setupListeners() {
        view.getSubmitButton().setOnAction(e -> handleSaveEvaluation());
    }

    private void loadProjectsAndSetupComboBox() {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            java.util.List<Project> projects = projectDAO.getAllProyects();
            ObservableList<Project> observableProjects = FXCollections.observableArrayList(projects);
            view.setProjects(observableProjects);

            Callback<ListView<Project>, ListCell<Project>> projectCellFactory = lv -> new ListCell<>() {
                @Override
                protected void updateItem(Project item, boolean empty) {
                    super.updateItem(item, empty);
                    setText((empty || item == null) ? "" : item.getTitle());
                }
            };
            view.getProjectComboBox().setCellFactory(projectCellFactory);
            view.getProjectComboBox().setButtonCell(projectCellFactory.call(null));

            view.getProjectComboBox().setOnAction(ev -> loadStudentsForSelectedProject());
        } catch (Exception ex) {
            String message = logic.services.ExceptionManager.handleException(ex);
            showMessage(message, true);
        }
    }

    private void loadStudentsForSelectedProject() {
        Project selectedProject = view.getProjectComboBox().getValue();
        if (selectedProject != null) {
            try {
                ProjectStudentDAO projectStudentDAO = new ProjectStudentDAO();
                StudentDAO studentDAO = new StudentDAO();
                java.util.List<Integer> studentIds = projectStudentDAO.getStudentsByProyect(selectedProject.getIdProyect());
                java.util.List<Student> students = new java.util.ArrayList<>();
                for (Integer id : studentIds) {
                    Student s = studentDAO.getStudentById(id);
                    if (s != null && s.getIdUser() > 0) {
                        students.add(s);
                    }
                }
                ObservableList<Student> observableStudents = FXCollections.observableArrayList(students);
                view.setStudents(observableStudents);

                Callback<ListView<Student>, ListCell<Student>> studentCellFactory = lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(Student item, boolean empty) {
                        super.updateItem(item, empty);
                        setText((empty || item == null) ? "" : item.getFullName());
                    }
                };
                view.getStudentComboBox().setCellFactory(studentCellFactory);
                view.getStudentComboBox().setButtonCell(studentCellFactory.call(null));
            } catch (Exception ex) {
                String message = logic.services.ExceptionManager.handleException(ex);
                showMessage(message, true);
            }
        } else {
            view.setStudents(FXCollections.observableArrayList());
        }
    }

    private void handleSaveEvaluation() {
        try {
            Project project = view.getProjectComboBox().getValue();
            Student student = view.getStudentComboBox().getValue();
            if (project == null || student == null) {
                showMessage("Selecciona un proyecto y un estudiante.", true);
                return;
            }

            Map<String, javafx.scene.control.ToggleGroup> rubricGroups = view.getRubricGroups();
            int total = 0;
            int count = 0;
            for (var group : rubricGroups.values()) {
                Toggle selected = group.getSelectedToggle();
                if (selected == null) {
                    showMessage("Debes calificar todos los criterios.", true);
                    return;
                }
                total += (int) selected.getUserData();
                count++;
            }
            double grade = (total / (double) count) * 2;

            String comments = view.getCommentsArea().getText();

            PresentationDAO presentationDAO = new PresentationDAO();
            Presentation presentation = null;
            for (Presentation p : presentationDAO.getPresentationsByStudent(student.getIdUser())) {
                if (p.getPresentationType() == PresentationType.Parcial) {
                    presentation = p;
                    break;
                }
            }

            if (presentation == null) {
                presentation = new Presentation();
                presentation.setPresentationType(PresentationType.Parcial);
                presentation.setPresentationDate(Timestamp.from(Instant.now()));
                presentation.setStudent(student);
                boolean created = presentationDAO.addPresentation(presentation);
                if (!created) {
                    showMessage("No se pudo crear la presentación parcial para este estudiante.", true);
                    return;
                }
            }

            Evaluation evaluation = new Evaluation();
            evaluation.setCalification((int) Math.round(grade));
            evaluation.setDescription(comments);
            evaluation.setEvaluationDate(Timestamp.from(Instant.now()));
            evaluation.setAcademic(evaluator);
            evaluation.setPresentation(presentation);

            EvaluationDAO evaluationDAO = new EvaluationDAO();
            if (evaluationDAO.addEvaluation(evaluation)) {
                showMessage("Evaluación registrada correctamente.", false);
                stage.close();
            } else {
                showMessage("No se pudo registrar la evaluación.", true);
            }
        } catch (Exception ex) {
            String message = ExceptionManager.handleException(ex);
            showMessage(message, true);
        }
    }

    private void showMessage(String message, boolean error) {
        view.getGradeLabel().setText((error ? "❌ " : "✔ ") + message);
        if (!error) {
            view.getGradeLabel().setStyle("-fx-text-fill: #388e3c;");
        } else {
            view.getGradeLabel().setStyle("-fx-text-fill: #d32f2f;");
        }
    }
}
