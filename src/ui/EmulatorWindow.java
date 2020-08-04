package ui;

import chip.Chip;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class EmulatorWindow extends Application {

    private static final String TITLE = "CHIP-8 Emulator for Java";
    private static final double MIN_HEIGHT = 340;
    private static final double MIN_WIDTH = 660;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(TITLE);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);

        StackPane layout = new StackPane();

        Scene scene = new Scene(layout, MIN_WIDTH, MIN_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
