
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * Created by troub on 2017/10/21.
 */
public class FreeLine extends Graph{
    /**
     * 描述自由线上的各个点
     */
    private ArrayList<Point> points;
    private Point basePoint;
    double relativeX, relativeY;
    double offsetX, offsetY;

    FreeLine(Point lt, Point rb){
        super(lt, rb);
        points = new ArrayList<>();
        //以第一个点为基准
        points.add(lt);
        basePoint = (Point)lt.clone();
        relativeX = 0;
        relativeY = 0;
    }

    FreeLine(double ltx, double lty, double rbx, double rby) {
        this(new Point(ltx, lty), new Point(rbx, rby));
    }

    @Override
    public void draw(GraphicsContext gc){
        if (points.isEmpty()){
            return;
        }
        gc.setLineDashes(0);
        gc.setFill(toolKit.getPencilColor());
        gc.setStroke(toolKit.getPencilColor());
        gc.setLineWidth(toolKit.getPencilSize());

        basePoint.setX(ltPoint.getX() - relativeX);
        basePoint.setY(ltPoint.getY() - relativeY);
        offsetX = points.get(0).getX() - basePoint.getX();
        offsetY = points.get(0).getY() - basePoint.getY();

        int j = 0;
        while (points.get(j) == null) {
            j++;
        }
        Point startP = points.get(j);
        Point nextP;
        for (int i = j + 1; i < points.size(); i++) {
            nextP = points.get(i);
            if (nextP == null) {
                startP = null;
                continue;
            }
            if (startP == null) {
                startP = nextP;
                continue;
            }
            gc.strokeLine(startP.getX() - offsetX, startP.getY() - offsetY, nextP.getX() - offsetX, nextP.getY() - offsetY);
            startP = nextP;
        }

    }

    /**
     * 添加描述轨迹的点
     * @param point 点
     */
    public void addPoint(Point point) {
        points.add(point);

        //更改有效区域
        if (point.getX() < ltPoint.getX()) {
            relativeX += point.getX() - ltPoint.getX();
            ltPoint = new Point(point.getX(), ltPoint.getY());
        } else if (point.getY() < ltPoint.getY()) {
            relativeY += point.getY() - ltPoint.getY();
            ltPoint = new Point(ltPoint.getX(), point.getY());

        } else if (point.getX() > rbPoint.getX()) {
            rbPoint = new Point(point.getX(), rbPoint.getY());
        } else if (point.getY() > rbPoint.getY()) {
            rbPoint = new Point(rbPoint.getX(), point.getY());
        }
    }

    /**
     * 输入橡皮擦的位置和橡皮擦大小，擦除点
     * @param x x坐标
     * @param y y坐标
     * @param width 橡皮擦大小
     */
    public void erase(double x, double y, double width) {
        //计算移动了之后的偏移量
        basePoint.setX(ltPoint.getX() - relativeX);
        basePoint.setY(ltPoint.getY() - relativeY);
        offsetX = points.get(0).getX() - basePoint.getX();
        offsetY = points.get(0).getY() - basePoint.getY();
        //将擦除的点设置为null，但不从ArrayList中删除
        for (int i = 1; i < points.size(); i++) {
            Point point = points.get(i);
            if (point == null) {
                continue;
            }
            if (Math.abs(point.getX() - offsetX - x) <= (width / 2) && Math.abs(point.getY() - offsetY - y) <= (width / 2)) {
                points.remove(i);
                points.add(i, null);
            }
        }
    }


    @Override
    public void zoom(int direction, double relativeX, double relativeY){
        //覆盖zoom方法
        //禁用缩放
    }
}
