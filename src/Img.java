

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Img extends Graph {
    private String src;

    Img(Point lt, Point rb, String s) {
        super(lt, rb);
        src = s;
    }

    Img(double ltx, double lty, double rbx, double rby, String s) {
        this(new Point(ltx, lty), new Point(rbx, rby), s);
    }

    @Override
    public void draw(GraphicsContext gc){
        gc.setLineDashes(0);
        gc.setFill(toolKit.getPencilColor());
        gc.setStroke(toolKit.getPencilColor());
        gc.setLineWidth(toolKit.getPencilSize());

        gc.drawImage(new Image("file:///" + src), ltPoint.getX(), ltPoint.getY(), rbPoint.getX() - ltPoint.getX(), rbPoint.getY() - ltPoint.getY());
    }
}
