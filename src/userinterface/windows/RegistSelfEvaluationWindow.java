package userinterface.windows;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegistSelfEvaluationWindow {
    private final Map<String, ToggleGroup> rubricGroups = new LinkedHashMap<>();
    private final TextArea commentsArea = new TextArea();
    private final Label gradeLabel = new Label("Calificación: 0");
    private final Button submitButton = new Button("Registrar Autoevaluación");
    private final VBox root = new VBox(15);

    private static final String[] CRITERIA = {
            "Mi participación en la organización vinculada fue productiva",
            "Logré la aplicación de los conocimientos de la IS",
            "Me sentí seguro al realizar las actividades encomendadas"
    };

    public RegistSelfEvaluationWindow() {
        root.setPadding(new Insets(20));
        root.getChildren().add(new Label("Autoevaluación"));

        GridPane rubricGrid = new GridPane();
        rubricGrid.setHgap(10);
        rubricGrid.setVgap(10);
        rubricGrid.setPadding(new Insets(10, 0, 10, 0));

        rubricGrid.add(new Label("Criterio"), 0, 0);
        for (int i = 1; i <= 5; i++) {
            rubricGrid.add(new Label(String.valueOf(i)), i, 0);
        }

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

        root.getChildren().add(new Label("Rúbrica de autoevaluación:"));
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

