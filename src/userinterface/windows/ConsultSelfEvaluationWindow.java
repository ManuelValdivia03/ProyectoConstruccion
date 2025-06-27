package userinterface.windows;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import logic.logicclasses.SelfEvaluation;

public class ConsultSelfEvaluationWindow {
    private final VBox root = new VBox(15);
    private final Label gradeLabel = new Label();
    private final TextArea commentsArea = new TextArea();

    public ConsultSelfEvaluationWindow() {
        root.setPadding(new Insets(20));
        root.getChildren().add(new Label("Autoevaluación del estudiante"));

        root.getChildren().add(gradeLabel);

        commentsArea.setEditable(false);
        commentsArea.setWrapText(true);
        commentsArea.setPromptText("Comentarios");
        root.getChildren().add(new Label("Comentarios:"));
        root.getChildren().add(commentsArea);
    }

    public Parent getView() {
        return root;
    }

    public void setSelfEvaluation(SelfEvaluation selfEval) {
        if (selfEval == null) {
            gradeLabel.setText("No hay autoevaluación registrada.");
            commentsArea.setText("");
        } else {
            gradeLabel.setText("Calificación: " + String.format("%.1f", selfEval.getCalification()));
            commentsArea.setText(selfEval.getFeedBack());
        }
    }
}

