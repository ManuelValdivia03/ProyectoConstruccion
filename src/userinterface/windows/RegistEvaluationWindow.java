package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import logic.logicclasses.Project;
import logic.logicclasses.Student;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistEvaluationWindow {
    private final ComboBox<Project> projectComboBox = new ComboBox<>();
    private final ComboBox<Student> studentComboBox = new ComboBox<>();
    private final Map<String, ToggleGroup> rubricGroups = new LinkedHashMap<>();
    private final TextArea commentsArea = new TextArea();
    private final Label gradeLabel = new Label("Calificación: 0");
    private final Button submitButton = new Button("Registrar Evaluación");
    private final VBox root = new VBox(15);

    private static final String[] CRITERIA = {
            "Dominio del tema",
            "Uso adecuado de principios de la disciplina de la IS",
            "Ortografía",
            "Rigor metodológico",
            "Congruencia de la presentación"
    };

    public RegistEvaluationWindow() {
        root.setPadding(new Insets(20));
        root.getChildren().add(new Label("Selecciona un proyecto:"));
        root.getChildren().add(projectComboBox);

        root.getChildren().add(new Label("Selecciona un integrante:"));
        root.getChildren().add(studentComboBox);

        GridPane rubricGrid = new GridPane();
        rubricGrid.setHgap(10);
        rubricGrid.setVgap(10);
        rubricGrid.setPadding(new Insets(10, 0, 10, 0));

        // Header
        rubricGrid.add(new Label("Criterio"), 0, 0);
        for (int i = 1; i <= 5; i++) {
            rubricGrid.add(new Label(String.valueOf(i)), i, 0);
        }

        // Rows for each criterion
        for (int row = 0; row < CRITERIA.length; row++) {
            String criterion = CRITERIA[row];
            rubricGrid.add(new Label(criterion), 0, row + 1);

            ToggleGroup group = new ToggleGroup();
            rubricGroups.put(criterion, group);

            for (int col = 1; col <= 5; col++) {
                RadioButton rb = new RadioButton();
                rb.setUserData(col);
                rb.setToggleGroup(group);
                rubricGrid.add(rb, col, row + 1);

                rb.setOnAction(e -> updateGrade());
            }
        }

        root.getChildren().add(new Label("Rúbrica de evaluación:"));
        root.getChildren().add(rubricGrid);

        root.getChildren().add(new Label("Comentarios:"));
        commentsArea.setPrefRowCount(3);
        root.getChildren().add(commentsArea);

        root.getChildren().add(gradeLabel);
        root.getChildren().add(submitButton);
    }

    public Parent getView() {
        return root;
    }

    public ComboBox<Project> getProjectComboBox() {
        return projectComboBox;
    }

    public ComboBox<Student> getStudentComboBox() {
        return studentComboBox;
    }

    public Map<String, ToggleGroup> getRubricGroups() {
        return rubricGroups;
    }

    public TextArea getCommentsArea() {
        return commentsArea;
    }

    public Label getGradeLabel() {
        return gradeLabel;
    }

    public Button getSubmitButton() {
        return submitButton;
    }

    public void setProjects(ObservableList<Project> projects) {
        projectComboBox.setItems(projects);
    }

    public void setStudents(ObservableList<Student> students) {
        studentComboBox.setItems(students);
    }

    public void updateGrade() {
        int total = 0;
        int count = 0;
        for (ToggleGroup group : rubricGroups.values()) {
            Toggle selected = group.getSelectedToggle();
            if (selected != null) {
                total += (int) selected.getUserData();
                count++;
            }
        }
        double avg = count == CRITERIA.length ? (total / (double) count) * 2 : 0;
        gradeLabel.setText("Calificación: " + String.format("%.1f", avg));
    }
}

