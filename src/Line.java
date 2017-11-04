
import javafx.scene.canvas.GraphicsContext;

/**
 * Created by troub on 2017/10/20.
 */
public class Line extends Graph {
    Line(){
        super(new Point(0, 0), new Point(0, 0));
    }
    Line(Point lt, Point rb){
        super(lt, rb);
    }
    Line(double ltx, double lty, double rbx, double rby){
        this(new Point(ltx, lty), new Point(rbx, rby));
    }

    @Override
    public void draw(GraphicsContext gc){
        gc.setLineDashes(0);
        gc.setFill(toolKit.getPencilColor());
        gc.setStroke(toolKit.getPencilColor());
        gc.setLineWidth(toolKit.getPencilSize());
        gc.strokeLine(ltPoint.getX(), ltPoint.getY(), rbPoint.getX(), rbPoint.getY());
    }
}
