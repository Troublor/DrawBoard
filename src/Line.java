
import javafx.scene.canvas.GraphicsContext;

/**
 * Created by troub on 2017/10/20.
 */
public class Line extends Graph {
    //定义四个角的宏
    public class LinePoint {
        public static final int LEFT_TOP = 1;
        public static final int RIGHT_TOP = 2;
        public static final int LEFT_BOTTOM = 3;
        public static final int RIGHT_BOTTOM = 4;
    }

    //直线的起点
    private int startPoint;
    //直线的终点
    private int endPoint;
    Line(){
        super(new Point(0, 0), new Point(0, 0));
    }
    Line(Point lt, Point rb){
        super(lt, rb);
    }
    Line(double ltx, double lty, double rbx, double rby){
        this(new Point(ltx, lty), new Point(rbx, rby));
    }

    public void setStartPoint(int startPoint) {
        this.startPoint = startPoint;
    }

    public void setEndPoint(int endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public void draw(GraphicsContext gc){
        gc.setLineDashes(0);
        gc.setFill(toolKit.getPencilColor());
        gc.setStroke(toolKit.getPencilColor());
        gc.setLineWidth(toolKit.getPencilSize());
        Point start = new Point(0, 0), end = new Point(0, 0);
        switch (startPoint) {
            case LEFT_TOP:
                start = ltPoint;
                break;
            case RIGHT_TOP:
                start = new Point(rbPoint.getX(), ltPoint.getY());
                break;
            case LEFT_BOTTOM:
                start = new Point(ltPoint.getX(), rbPoint.getY());
                break;
            case RIGHT_BOTTOM:
                start = rbPoint;
                break;
        }
        switch (endPoint) {
            case LEFT_TOP:
                end = ltPoint;
                break;
            case RIGHT_TOP:
                end = new Point(rbPoint.getX(), ltPoint.getY());
                break;
            case LEFT_BOTTOM:
                end = new Point(ltPoint.getX(), rbPoint.getY());
                break;
            case RIGHT_BOTTOM:
                end = rbPoint;
                break;
        }
        gc.strokeLine(start.getX(), start.getY(), end.getX(), end.getY());
    }
}
