/**
 * Created by troub on 2017/10/7.
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DrawBoard extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DrawBoard.fxml"));
        Parent root = loader.load();
        primaryStage.getIcons().add(new Image("img/icon.png"));
        primaryStage.setTitle("DrawBoard");
        DrawBoardController controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (!controller.isSaved()) {
                    Alert alert = new Alert(
                            Alert.AlertType.CONFIRMATION,
                            "未保存的工程",
                            new ButtonType("确定", ButtonBar.ButtonData.YES),
                            new ButtonType("保存", ButtonBar.ButtonData.OTHER),
                            new ButtonType("取消", ButtonBar.ButtonData.NO)
                    );
                    alert.setContentText("当前工程没有保存，确定退出吗？");
                    switch (alert.showAndWait().get().getButtonData()) {
                        case YES:
                            break;
                        case OTHER:
                            controller.Save(new ActionEvent());
                            break;
                        case NO:
                            event.consume();
                    }
                }


            }
        });
        primaryStage.setMinWidth(1250);
        primaryStage.setMinHeight(1050);
        primaryStage.setMaxHeight(1050);
        primaryStage.setMaxWidth(1250);
        primaryStage.setScene(new Scene(root, 1200, 1000));
        primaryStage.show();
    }
}
