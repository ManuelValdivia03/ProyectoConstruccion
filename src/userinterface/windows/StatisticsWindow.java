package userinterface.windows;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.Map;

public class StatisticsWindow {
    private final ScrollPane view;
    private final PieChart projectsChart;
    private final BarChart<String, Number> studentsPerProjectChart;
    private final BarChart<String, Number> capacityChart;
    private final Button refreshButton;
    private final Button closeButton;
    private final Label statusLabel;

    public StatisticsWindow() {
        projectsChart = new PieChart();
        projectsChart.setTitle("Estudiantes con/sin proyecto asignado");
        projectsChart.setLegendVisible(true);
        projectsChart.setLabelsVisible(true);
        projectsChart.setMinSize(400, 300);

        CategoryAxis studentsXAxis = new CategoryAxis();
        studentsXAxis.setLabel("Proyectos");
        NumberAxis studentsYAxis = new NumberAxis();
        studentsYAxis.setLabel("Cantidad de Estudiantes");

        studentsPerProjectChart = new BarChart<>(studentsXAxis, studentsYAxis);
        studentsPerProjectChart.setTitle("Estudiantes por Proyecto");
        studentsPerProjectChart.setLegendVisible(false);
        studentsPerProjectChart.setMinSize(800, 300);

        CategoryAxis capacityXAxis = new CategoryAxis();
        capacityXAxis.setLabel("Proyectos");
        NumberAxis capacityYAxis = new NumberAxis();
        capacityYAxis.setLabel("Cantidad");

        capacityChart = new BarChart<>(capacityXAxis, capacityYAxis);
        capacityChart.setTitle("Capacidad vs Estudiantes Actuales");
        capacityChart.setMinSize(800, 300);

        refreshButton = new Button("Actualizar");
        refreshButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        closeButton = new Button("Cerrar");
        closeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        HBox buttonsBox = new HBox(10, refreshButton, closeButton);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox chartsBox = new VBox(20,
                projectsChart,
                studentsPerProjectChart,
                capacityChart,
                buttonsBox,
                statusLabel
        );
        chartsBox.setPadding(new Insets(15));
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.setStyle("-fx-background-color: #f5f5f5;");

        view = new ScrollPane(chartsBox);
        view.setFitToWidth(true);
    }

    public ScrollPane getView() {
        return view;
    }

    public void updateProjectsChart(int withProject, int withoutProject) {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Con proyecto (" + withProject + ")", withProject),
                new PieChart.Data("Sin proyecto (" + withoutProject + ")", withoutProject)
        );
        projectsChart.setData(pieChartData);
    }

    public void updateStudentsPerProjectChart(Map<String, Integer> studentsPerProject) {
        studentsPerProjectChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Estudiantes");

        studentsPerProject.forEach((project, count) -> {
            series.getData().add(new XYChart.Data<>(project, count));
        });

        studentsPerProjectChart.getData().add(series);
    }

    public void updateCapacityChart(Map<String, int[]> capacityStats) {
        capacityChart.getData().clear();

        XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();
        currentSeries.setName("Estudiantes Actuales");

        XYChart.Series<String, Number> capacitySeries = new XYChart.Series<>();
        capacitySeries.setName("Capacidad Total");

        capacityStats.forEach((project, stats) -> {
            currentSeries.getData().add(new XYChart.Data<>(project, stats[0]));
            capacitySeries.getData().add(new XYChart.Data<>(project, stats[1]));
        });

        capacityChart.getData().addAll(currentSeries, capacitySeries);
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Button getCloseButton() {
        return closeButton;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public void showMessage(String message, boolean isError) {
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusLabel.setText(message);
    }
}