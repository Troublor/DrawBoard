

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class Rect extends Graph {

    Rect(Point lt, Point rb){
        super(lt, rb);
    }
    Rect(double ltx, double lty, double rbx, double rby){
        this(new Point(ltx, lty), new Point(rbx, rby));
    }

    @Override
    public void draw(GraphicsContext gc){


        gc.setLineDashes(0);
        gc.setFill(toolKit.getPencilColor());
        gc.setStroke(toolKit.getPencilColor());
        gc.setLineWidth(toolKit.getPencilSize());

        if (toolKit.getRectFill()){
            gc.setFill(toolKit.getRectFillColor());
            if (toolKit.getRectGradient()) {
                LinearGradient lg = new LinearGradient(0, 0, 1, 1, true,
                        CycleMethod.REFLECT,
                        new Stop(0.0, toolKit.getRectFillColor()),
                        new Stop(1.0, toolKit.getRectGradientColor()));
                gc.setFill(lg);
            }
            if (toolKit.getRectRound()){
                gc.fillRoundRect(ltPoint.getX(), ltPoint.getY(), rbPoint.getX() - ltPoint.getX(), rbPoint.getY() - ltPoint.getY(), toolKit.getRectArcRadius(), toolKit.getRectArcRadius());
            }else {
                gc.fillRect(ltPoint.getX(), ltPoint.getY(), rbPoint.getX() - ltPoint.getX(), rbPoint.getY() - ltPoint.getY());
            }
        }else {
            if (toolKit.getRectRound()){
                gc.strokeRoundRect(ltPoint.getX(), ltPoint.getY(), rbPoint.getX() - ltPoint.getX(), rbPoint.getY() - ltPoint.getY(), toolKit.getRectArcRadius(), toolKit.getRectArcRadius());
            }else {
                gc.strokeRect(ltPoint.getX(), ltPoint.getY(), rbPoint.getX() - ltPoint.getX(), rbPoint.getY() - ltPoint.getY());
            }
        }
    }

    public void showPreferences(Pane preferencePane) {
        GridPane preferenceArea = (GridPane)preferencePane.getChildren().get(1);
        CheckBox checkBox = (CheckBox)preferenceArea.getChildren().get(0);
        checkBox.setSelected(toolKit.getRectFill());
        ColorPicker colorPicker = (ColorPicker)preferenceArea.getChildren().get(1);
        colorPicker.setValue(toolKit.getRectFillColor());
        checkBox = (CheckBox) preferenceArea.getChildren().get(2);
        checkBox.setSelected(toolKit.getRectGradient());
        colorPicker = (ColorPicker) preferenceArea.getChildren().get(3);
        colorPicker.setValue(toolKit.getRectGradientColor());
        checkBox = (CheckBox) preferenceArea.getChildren().get(4);
        checkBox.setSelected(toolKit.getRectRound());
        ComboBox<Double> comboBox = (ComboBox<Double>) preferenceArea.getChildren().get(5);
        comboBox.setValue(toolKit.getRectArcRadius());
    }

}
