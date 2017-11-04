

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class Ovel extends Graph{

    Ovel(){
        super(new Point(0, 0), new Point(0, 0));
    }

    Ovel(Point lt, Point rb){
        super(lt, rb);
    }
    Ovel(double ltx, double lty, double rbx, double rby){
        this(new Point(ltx, lty), new Point(rbx, rby));
    }

    @Override
    public void draw(GraphicsContext gc){
        gc.setLineDashes(0);
        gc.setLineWidth(toolKit.getPencilSize());
        gc.setEffect(null);
        if (toolKit.getOvelFill()){
            gc.setFill(toolKit.getOvelFillColor());
            if (toolKit.getOvelGradient()) {
                gc.setFill(new RadialGradient(0, 0, 0.5, 0.5, 1, true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0.0, toolKit.getOvelFillColor()),
                        new Stop(1.0, toolKit.getOvelGradientColor())));
            }
            if (toolKit.getOvelShadow()) {
                gc.setEffect(new DropShadow(toolKit.getOvelShadowWidth(), 0, 0, toolKit.getOvelShadowColor()));
            }
            gc.fillOval(ltPoint. getX(), ltPoint.getY(), rbPoint.getX() - ltPoint.getX(), rbPoint.getY() - ltPoint.getY());
        }else {
            gc.setStroke(toolKit.getPencilColor());
            if (toolKit.getOvelShadow()) {
                gc.setEffect(new DropShadow(toolKit.getOvelShadowWidth(), 0, 0, toolKit.getOvelShadowColor()));
            }
            gc.strokeOval(ltPoint.getX(), ltPoint.getY(), rbPoint.getX() - ltPoint.getX(), rbPoint.getY() - ltPoint.getY());
        }
        gc.setEffect(null);
    }
}
