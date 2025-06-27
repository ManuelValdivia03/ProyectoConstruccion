package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import logic.logicclasses.Group;

public class ConsultGroupsWindow {
    private final VBox view;
    private final TableView<Group> groupTable;
    private final Button backButton;

    public ConsultGroupsWindow() {
        groupTable = new TableView<>();
        groupTable.setStyle("-fx-font-size: 14px;");
        groupTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Group, Integer> nrcCol = new TableColumn<>("NRC");
        nrcCol.setCellValueFactory(new PropertyValueFactory<>("nrc"));
        nrcCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Group, String> nameCol = new TableColumn<>("Nombre del Grupo");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        nameCol.setStyle("-fx-alignment: CENTER;");

        groupTable.getColumns().addAll(nrcCol, nameCol);

        TableColumn<Group, Void> academicCol = new TableColumn<>("Académico Asignado");
        academicCol.setCellFactory(param -> new TableCell<>() {
            private final Button assignBtn = new Button();
            private final Label academicLabel = new Label();

            {
                assignBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
                assignBtn.setOnAction(event -> {
                    Group group = getTableView().getItems().get(getIndex());
                    if (group != null && assignAcademicHandler != null) {
                        assignBtn.setUserData(group);
                        assignAcademicHandler.handle(new ActionEvent(assignBtn, null));
                    }
                });
                academicLabel.setStyle("-fx-font-size: 14px; -fx-alignment: CENTER; -fx-text-fill: #1A5F4B; -fx-font-weight: bold;");
                academicLabel.setOnMouseEntered(e -> {
                    Group group = getTableView().getItems().get(getIndex());
                    if (group != null && group.getAcademic() != null) {
                        Tooltip.install(academicLabel, new Tooltip(group.getAcademic().getFullName()));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Group group = getTableView().getItems().get(getIndex());
                    if (group.getAcademic() != null) {
                        academicLabel.setText(group.getAcademic().getFullName());
                        assignBtn.setText("Cambiar Académico");
                        setGraphic(new HBox(8, academicLabel, assignBtn));
                    } else {
                        assignBtn.setText("Asignar Académico");
                        setGraphic(assignBtn);
                    }
                }
            }
        });
        academicCol.setPrefWidth(250);
        groupTable.getColumns().add(academicCol);

        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        HBox buttonBox = new HBox(15, backButton);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background-color: #f5f5f5;");
        view.getChildren().addAll(groupTable, buttonBox);
    }

    private EventHandler<ActionEvent> assignAcademicHandler;

    public void setAssignAcademicHandler(EventHandler<ActionEvent> handler) {
        this.assignAcademicHandler = handler;
    }

    public VBox getView() {
        return view;
    }

    public TableView<Group> getGroupTable() {
        return groupTable;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setGroupData(ObservableList<Group> groups) {
        groupTable.setItems(groups);
    }
}
