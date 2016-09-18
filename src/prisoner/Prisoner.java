package prisoner;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

/**
 *
 * @author helfrich
 */
public class Prisoner extends Application {

    private World world;
    private final IntegerProperty delay = new SimpleIntegerProperty(100);
    private final Color c[][] = new Color[3][3];
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

        Scene scene = new Scene(root, 600, 600);

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

        world = new World();
        world.init();
        state = State.RUNNING;

        textP.textProperty().bindBidirectional(world.getP(), new NumberStringConverter());
        textB.textProperty().bindBidirectional(world.getB(), new NumberStringConverter());
        textDelay.textProperty().bindBidirectional(delay, new NumberStringConverter());

        updateUi();

        c[1][1] = Color.rgb(0, 0, 255);    // blue
        c[2][2] = Color.rgb(255, 0, 0);    // red                                              
        c[1][2] = Color.rgb(0, 255, 0);    // green                                           
        c[2][1] = Color.rgb(255, 255, 0);  // yellow

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
                    renderWorld();
                    lastFrameNanos = now;
                } else if (state != State.STOPPED) {
                    renderWorld(); // always provide a frame, even when rendered before
                }
            }
        }.start();

    }

    private void renderWorld() {
        int n = world.getN();
        int[][] s = world.getS();
        int[][] sn = world.getSn();
        {
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    gc.setFill(c[sn[i][j]][s[i][j]]);
                    gc.fillRect(offsetX + i * cellWidth, offsetY + j * cellWidth, cellWidth, cellWidth);
                    // s[i][j] = sn[i][j];
                }
            }
        }
    }

    public HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
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

        hbox.getChildren().addAll(labelB, textB, labelP, textP, labelDelay, textDelay, buttonRun, buttonStop, buttonPause);

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
}
