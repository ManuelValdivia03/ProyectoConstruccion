package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegistProyectWindow {
    private final VBox view;
    private final TextField titleTextField;
    private final TextField descriptionTextField;
    private final TextField maxStudentsTextField;
    private final DatePicker dateStartPicker;
    private final DatePicker dateEndPicker;
    private final Button registerButton;
    private final Button cancelButton;
    private final Label resultLabel;

    public RegistProyectWindow() {
        Label titleLabel = new Label("Registrar Proyecto");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        titleTextField = new TextField();
        descriptionTextField = new TextField();
        maxStudentsTextField = new TextField();

        dateStartPicker = new DatePicker();
        dateStartPicker.setPromptText("Seleccione fecha");
        dateEndPicker = new DatePicker();
        dateEndPicker.setPromptText("Seleccione fecha");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dateStartPicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, formatter);
                } else {
                    return null;
                }
            }
        });

        dateEndPicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, formatter);
                } else {
                    return null;
                }
            }
        });

        registerButton = new Button("Registrar Proyecto");
        registerButton.setStyle("-fx-background-color: #0A1F3F; -fx-text-fill: white;");
        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(25, 25, 25, 25));

        grid.add(new Label("Título:"), 0, 0);
        grid.add(titleTextField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descriptionTextField, 1, 1);
        grid.add(new Label("Cupo máximo:"), 0, 2);
        grid.add(maxStudentsTextField, 1, 2);
        grid.add(new Label("Fecha de Inicio:"), 0, 3);
        grid.add(dateStartPicker, 1, 3);
        grid.add(new Label("Fecha de Fin:"), 0, 4);
        grid.add(dateEndPicker, 1, 4);

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, registerButton, cancelButton, resultLabel);
    }

    public VBox getView() {
        return view;
    }

    public TextField getTitleTextField() {
        return titleTextField;
    }

    public TextField getDescriptionTextField() {
        return descriptionTextField;
    }

    public TextField getMaxStudentsTextField() {
        return maxStudentsTextField;
    }

    public DatePicker getDateStartPicker() {
        return dateStartPicker;
    }

    public DatePicker getDateEndPicker() {
        return dateEndPicker;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Label getResultLabel() {
        return resultLabel;
    }
}
