package prisoner;

import java.util.LinkedList;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.converter.NumberStringConverter;

/**
 *
 * @author helfrich
 */
public class Prisoner extends Application {

    private World world;
    private final IntegerProperty delay = new SimpleIntegerProperty(100);
    private final Color c[][] = new Color[3][3];
    Color blue = Color.rgb(0, 0, 255);
    Color red = Color.rgb(255, 0, 0);
    Color green = Color.rgb(0, 255, 0);
    Color yellow = Color.rgb(255, 255, 0);
    private final int cellWidth = 5;
    private int offsetX = 0;
    private int offsetY = 0;
    private TextField textB;
    private TextField textP;
    private TextField textDelay;
    private Button buttonRun;
    private Button buttonStop;
    private Button buttonPause;
    private BorderPane root;
    private GraphicsContext gc;
    private Button buttonInfo;
    private final LinkedList<HistoryItem> llHistory = new LinkedList();
    private Button buttonChart;

    private final int historySize = 1000;

    private enum State {

        STOPPED, RUNNING, PAUSED
    };

    State state = State.STOPPED;

    @Override
    public void start(Stage primaryStage) {

        root = new BorderPane();
        HBox hbox = addHBox();
        //root.getChildren().add(btn);

        Canvas canvas = new Canvas(512, 512);

        gc = canvas.getGraphicsContext2D();

        root.setTop(hbox);
        root.setCenter(canvas);

        Scene scene = new Scene(root, 680, 600);

        primaryStage.setTitle("The Prisoner's Dilemma");
        primaryStage.setScene(scene);
        primaryStage.show();

        root.requestFocus();

        buttonRun.setOnAction((ActionEvent event) -> {
            switch (state) {
                case PAUSED:
                    state = State.RUNNING;
                    break;
                case STOPPED:
                    world.init();
                    llHistory.clear();
                    state = State.RUNNING;
                    break;
            }
            updateUi();
        });

        buttonStop.setOnAction((ActionEvent event) -> {
            state = State.STOPPED;
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            updateUi();
        });

        buttonPause.setOnAction((ActionEvent event) -> {
            state = State.PAUSED;
            updateUi();
        });

        buttonChart.setOnAction((ActionEvent event) -> {
            buttonChart.setDisable(true);
            showChart();
        });

        buttonInfo.setOnAction((ActionEvent event) -> {
            buttonInfo.setDisable(true);
            showInfo();
        });

        world = new World();
        world.init();
        state = State.RUNNING;

        textP.textProperty().bindBidirectional(world.getP(), new NumberStringConverter());
        textB.textProperty().bindBidirectional(world.getB(), new NumberStringConverter());
        textDelay.textProperty().bindBidirectional(delay, new NumberStringConverter());

        updateUi();

        c[1][1] = blue;
        c[2][2] = red;
        c[1][2] = green;
        c[2][1] = yellow;

        calculateOffsets(canvas);

        new AnimationTimer() {
            private long lastFrameNanos;

            @Override
            public void handle(long now) {
                long deltaMillis = (now - lastFrameNanos) / (int) 1e6;
                if (deltaMillis >= delay.get() && state == State.RUNNING) {
                    if (!world.isNewWorld()) {
                        world.nextGeneration();
                    }
                    world.update();
                    renderWorld(true);
                    lastFrameNanos = now;
                } else if (state != State.STOPPED) {
                    renderWorld(false); // always provide a frame, even when rendered before
                }
            }
        }.start();

    }

    private synchronized void renderWorld(boolean toHistory) {
        HistoryItem historyItem = null;
        if (toHistory) {
            historyItem = new HistoryItem();
            historyItem.generation = world.getGeneration();
        }
        int n = world.getN();
        int[][] s = world.getS();
        int[][] sn = world.getSn();
        {
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    Color fill = c[sn[i][j]][s[i][j]];
                    gc.setFill(fill);
                    gc.fillRect(offsetX + i * cellWidth, offsetY + j * cellWidth, cellWidth, cellWidth);
                    if (toHistory && historyItem != null) {
                        updateHistoryItem(historyItem, fill); // We keep track of the actual rendering
                    }                    // s[i][j] = sn[i][j];
                }
            }
        }
        if (toHistory && historyItem != null) {
            llHistory.add(historyItem);
            if (llHistory.size() > historySize) {
                llHistory.removeFirst();
            }
        }
    }

    private void updateHistoryItem(HistoryItem historyItem, Color fill) {
        if (fill.equals(blue)) {
            historyItem.blueCount++;
        }
        if (fill.equals(red)) {
            historyItem.redCount++;
        }
        if (fill.equals(green)) {
            historyItem.greenCount++;
        }
        if (fill.equals(yellow)) {
            historyItem.yellowCount++;
        }
    }

    public HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 0, 12));
        hbox.setSpacing(10);
        //hbox.setStyle("-fx-background-color: #336699;");

        Label labelB = new Label("b = ");
        textB = new TextField();
        textB.prefWidth(68);
        textB.setMaxWidth(68);

        Label labelP = new Label("p = ");
        textP = new TextField();
        textP.prefWidth(68);
        textP.setMaxWidth(68);

        Label labelDelay = new Label("delay: ");
        textDelay = new TextField();
        textDelay.prefWidth(68);
        textDelay.setMaxWidth(68);

        buttonRun = new Button();
        Image imagePlay = new Image(getClass().getResourceAsStream("images/play.png"));
        ImageView imageView = new ImageView(imagePlay);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        buttonRun.setGraphic(imageView);

        buttonStop = new Button();
        Image imageStop = new Image(getClass().getResourceAsStream("images/stop.png"));
        imageView = new ImageView(imageStop);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        buttonStop.setGraphic(imageView);

        buttonPause = new Button();
        Image imagePause = new Image(getClass().getResourceAsStream("images/pause.png"));
        imageView = new ImageView(imagePause);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        buttonPause.setGraphic(imageView);

        buttonChart = new Button();
        Image imageChart = new Image(getClass().getResourceAsStream("images/line-chart.png"));
        imageView = new ImageView(imageChart);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        buttonChart.setGraphic(imageView);

        buttonInfo = new Button();
        Image imageInfo = new Image(getClass().getResourceAsStream("images/question-circle.png"));
        imageView = new ImageView(imageInfo);
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        buttonInfo.setGraphic(imageView);
        //HBox.setMargin(buttonInfo, new Insets(0, 0, 0, 24));

        hbox.getChildren().addAll(labelB, textB, labelP, textP, labelDelay, textDelay, buttonRun, buttonStop, buttonPause, buttonChart, buttonInfo);

        hbox.setAlignment(Pos.CENTER);
        return hbox;
    }

    private void updateUi() {
        switch (state) {
            case STOPPED:
                textB.setDisable(false);
                textP.setDisable(false);
                textDelay.setDisable(false);
                buttonRun.setDisable(false);
                buttonStop.setDisable(true);
                buttonPause.setDisable(true);
                break;
            case RUNNING:
                textB.setDisable(true);
                textP.setDisable(true);
                textDelay.setDisable(true);
                buttonRun.setDisable(true);
                buttonStop.setDisable(false);
                buttonPause.setDisable(false);
                break;
            case PAUSED:
                textB.setDisable(true);
                textP.setDisable(true);
                textDelay.setDisable(true);
                buttonRun.setDisable(false);
                buttonStop.setDisable(false);
                buttonPause.setDisable(true);
                break;
            default:
                throw new AssertionError(state.name());

        }
        root.requestFocus();
    }

    private void calculateOffsets(Canvas canvas) {
        offsetX = (int) Math.round(((canvas.getWidth() / 2f) - (world.getN() / 2f) * (double) cellWidth));
        offsetY = (int) Math.round(((canvas.getHeight() / 2f) - (world.getN() / 2f) * (double) cellWidth));
    }

    private void showChart() {
        Stage chartStage = new Stage();
        chartStage.setOnCloseRequest((WindowEvent we) -> {
            buttonChart.setDisable(false);
        });

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Generation");
        final LineChart<String, Number> lineChart
                = new LineChart<>(xAxis, yAxis);
        lineChart.setCreateSymbols(false);

        XYChart.Series seriesBlue = new XYChart.Series();
        seriesBlue.setName("is cooperating, did cooperate");

        XYChart.Series seriesRed = new XYChart.Series();
        seriesRed.setName("is defecting, did defect");

        XYChart.Series seriesGreen = new XYChart.Series();
        seriesGreen.setName("is cooperating, did defect");

        XYChart.Series seriesYellow = new XYChart.Series();
        seriesYellow.setName("is defecting, did cooperate");

        llHistory.stream().forEach((historyItem) -> {
            String generation = String.valueOf(historyItem.generation);
            seriesBlue.getData().add(new XYChart.Data(generation, historyItem.blueCount));
            seriesRed.getData().add(new XYChart.Data(generation, historyItem.redCount));
            seriesGreen.getData().add(new XYChart.Data(generation, historyItem.greenCount));
            seriesYellow.getData().add(new XYChart.Data(generation, historyItem.yellowCount));
        });

        Scene scene = new Scene(lineChart, 680, 600);
        scene.getStylesheets().add(getClass().getResource("css/chart.css").toExternalForm());
        if (llHistory.size() > 100) {
            scene.getStylesheets().add(getClass().getResource("css/nogrid.css").toExternalForm());
        }
        lineChart.getData().addAll(seriesBlue, seriesRed, seriesGreen, seriesYellow);

        chartStage.setTitle("The Prisoner's Dilemma");
        chartStage.setScene(scene);
        chartStage.show();
    }

    private void showInfo() {
        Stage infoStage = new Stage();
        infoStage.setOnCloseRequest((WindowEvent we) -> {
            buttonInfo.setDisable(false);
        });
        Pane rootInfo = new VBox();
        rootInfo.setPadding(new Insets(15, 12, 15, 12));
        Scene scene = new Scene(rootInfo, 600, 600);

        Label lbInfo = new Label("The spatial variant of the iterated prisoner's dilemma is a simple yet powerful model for the problem of cooperation versus conflict in groups. The app demonstrates the spread of 'altruism' and 'exploitation for personal gain' in an interacting population of individuals learning from each other by experience. Initially the population consists of cooperators and a certain amount of defectors (a fraction represented by p). The advantage of defection is determined by the the value of b in the 'payoff matrix' (see below) which is used to calculate the payoff after each round for each 'player' on the basis of its strategy. For the next round a player determines its new strategy by selecting the most favourable strategy from itself and its direct neighbours. (Ref. A.L. Lloyd, Sci. Amer., June 1995, 80-83");
        lbInfo.setWrapText(true);

        ImageView ivPayoff = new ImageView();
        ivPayoff.setImage(new Image(getClass().getResourceAsStream("images/payoff.png")));
        ivPayoff.setFitWidth(250);
        ivPayoff.setPreserveRatio(true);
        VBox vbPayoff = new VBox();
        vbPayoff.setAlignment(Pos.CENTER);
        VBox.setMargin(vbPayoff, new Insets(15, 0, 15, 0));
        vbPayoff.getChildren().add(ivPayoff);

        Label lbB = new Label("b: advantage for defection when opponent cooperates");
        VBox.setMargin(lbB, new Insets(0, 0, 15, 0));

        Label lbP = new Label("p: fraction (0..1) of defectors in the first round");
        VBox.setMargin(lbP, new Insets(0, 0, 15, 0));

        HBox hbLegendBlue = new HBox();
        Rectangle blueSquare = new Rectangle(8, 8, blue);
        Label lbBlue = new Label("is cooperating, did cooperate");
        HBox.setMargin(blueSquare, new Insets(5, 6, 0, 0));
        hbLegendBlue.getChildren().addAll(blueSquare, lbBlue);
        VBox.setMargin(hbLegendBlue, new Insets(0, 0, 15, 0));

        HBox hbLegendRed = new HBox();
        Rectangle redSquare = new Rectangle(8, 8, red);
        Label lbRed = new Label("is defecting, did defect");
        HBox.setMargin(redSquare, new Insets(5, 6, 0, 0));
        hbLegendRed.getChildren().addAll(redSquare, lbRed);
        VBox.setMargin(hbLegendRed, new Insets(0, 0, 15, 0));

        HBox hbLegendGreen = new HBox();
        Rectangle greenSquare = new Rectangle(8, 8, green);
        Label lbGreen = new Label("is cooperating, did defect");
        HBox.setMargin(greenSquare, new Insets(5, 6, 0, 0));
        hbLegendGreen.getChildren().addAll(greenSquare, lbGreen);
        VBox.setMargin(hbLegendGreen, new Insets(0, 0, 15, 0));

        HBox hbLegendYellow = new HBox();
        Rectangle yellowSquare = new Rectangle(8, 8, yellow);
        Label lbYellow = new Label("is defecting, did cooperate");
        HBox.setMargin(yellowSquare, new Insets(5, 6, 0, 0));
        hbLegendYellow.getChildren().addAll(yellowSquare, lbYellow);
        VBox.setMargin(hbLegendYellow, new Insets(0, 0, 15, 0));

        rootInfo.getChildren().addAll(lbInfo, vbPayoff, lbB, lbP, hbLegendBlue, hbLegendRed, hbLegendGreen, hbLegendYellow);
        infoStage.setTitle("The Prisoner's Dilemma");
        infoStage.setScene(scene);
        infoStage.show();
    }
}
