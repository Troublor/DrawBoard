
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.*;


/**
 * Created by troub on 2017/10/19.
 */
public abstract class Graph implements Serializable, Cloneable{
    //zoom方向标识宏
    public static final int LEFT_TOP = 1;
    public static final int RIGHT_TOP = 2;
    public static final int LEFT_BOTTOM = 3;
    public static final int RIGHT_BOTTOM = 4;
    public static final int INSIDE = 5;

    //外边距(选择图形时用）
    static double padding = 10;

    //画图工具
    protected ToolKit toolKit;

    protected Point ltPoint;
    protected Point rbPoint;
//    protected Circle ltZoomButton, rtZoomButton, lbZoomButton, rbZoomButton;
    private double zoomButtonRadius;

    Graph(Point lt, Point rb){
        ltPoint = lt;
        rbPoint = rb;
        zoomButtonRadius = 7;
        toolKit = new ToolKit();
        /*ltZoomButton = new Circle(7, Color.rgb(228, 228, 228));
        ltZoomButton.setCursor(Cursor.NW_RESIZE);
        rtZoomButton = new Circle(7, Color.rgb(228, 228, 228));
        rtZoomButton.setCursor(Cursor.NE_RESIZE);
        lbZoomButton = new Circle(7, Color.rgb(228, 228, 228));
        lbZoomButton.setCursor(Cursor.SW_RESIZE);
        rbZoomButton = new Circle(7, Color.rgb(228, 228, 228));
        rbZoomButton.setCursor(Cursor.SE_RESIZE);*/
    }

    /**
     * 绘出图形
     * @param gc GraphicsContext
     */
    public abstract void draw(GraphicsContext gc);

    /**
     * 判断当前坐标是否选中
     * @param x 当前坐标x
     * @param y 当前坐标y
     * @return boolean
     */
    public boolean isSelected(double x, double y){
        return (x >= ltPoint.getX() - padding) && (x <= rbPoint.getX() + padding) &&
                (y >= ltPoint.getY() - padding) && (y <= rbPoint.getY() + padding);

    }

    /**
     * 被选中
     * @param gc GraphicsContext
     */
    public void select(GraphicsContext gc){
        //绘制虚线矩形
        gc.setLineDashes(new double[]{10.0, 5.0});
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        gc.strokeRect(
                ltPoint.getX(),
                ltPoint.getY(),
                rbPoint.getX() - ltPoint.getX(),
                rbPoint.getY() - ltPoint.getY()
        );

        //绘制zoombutton
        gc.setFill(Color.GRAY);
        gc.fillOval(
                ltPoint.getX() - zoomButtonRadius,
                ltPoint.getY() - zoomButtonRadius,
                zoomButtonRadius * 2,
                zoomButtonRadius * 2
        );
        gc.fillOval(
                rbPoint.getX() - zoomButtonRadius,
                ltPoint.getY() - zoomButtonRadius,
                zoomButtonRadius * 2,
                zoomButtonRadius * 2
        );
        gc.fillOval(
                ltPoint.getX() - zoomButtonRadius,
                rbPoint.getY() - zoomButtonRadius,
                zoomButtonRadius * 2,
                zoomButtonRadius * 2
        );
        gc.fillOval(
                rbPoint.getX() - zoomButtonRadius,
                rbPoint.getY() - zoomButtonRadius,
                zoomButtonRadius * 2,
                zoomButtonRadius * 2
        );
    }

    /**
     * 拖动
     * @param relativeX 开始移动的位置的x
     * @param relativeY 开始移动的位置的y
     * @param ToX 移动到的位置的x
     * @param ToY 移动到的位置的y
     */
    public void move(double relativeX, double relativeY, double ToX, double ToY){
        rbPoint = new Point(
                rbPoint.getX() - ltPoint.getX() - relativeX + ToX,
                rbPoint.getY() - ltPoint.getY() - relativeY + ToY
        );
        ltPoint = new Point(
                ToX - relativeX,
                ToY - relativeY
        );
    }

    /**
     * 缩放
     * 用于缩放，表示相对于拖拽角对角的坐标，且永远取绝对值
     * @param direction int 标识从哪一个角缩放
     * @param relativeX 缩放到的x
     * @param relativeY 缩放到的y
     */
    public void zoom(int direction, double relativeX, double relativeY){
        if (direction == LEFT_TOP){
            ltPoint = new Point(rbPoint.getX() - relativeX, rbPoint.getY() - relativeY);
        }else if (direction == RIGHT_TOP){
            ltPoint = new Point(ltPoint.getX(), rbPoint.getY() - relativeY);
            rbPoint = new Point(ltPoint.getX() + relativeX, rbPoint.getY());
        }else if (direction == LEFT_BOTTOM){
            ltPoint = new Point(rbPoint.getX() - relativeX, ltPoint.getY());
            rbPoint = new Point(rbPoint.getX(), ltPoint.getY() + relativeY);
        }else if (direction == RIGHT_BOTTOM){
            rbPoint = new Point(ltPoint.getX() + relativeX, ltPoint.getY() + relativeY);
        }
    }

    /**
     * 获得光标位置
     * @param x 光标坐标x
     * @param y 光标坐标y
     * @return 光标位置宏
     */
    public int getCursorLocation(double x, double y) {

        if ((x > (ltPoint.getX() - zoomButtonRadius)) && (x < ltPoint.getX() + zoomButtonRadius) &&
                (y > ltPoint.getY() - zoomButtonRadius) && (y < ltPoint.getY() + zoomButtonRadius)) {
            return LEFT_TOP;
        } else if ((x > (rbPoint.getX() - zoomButtonRadius)) && (x < rbPoint.getX() + zoomButtonRadius) &&
                (y > ltPoint.getY() - zoomButtonRadius) && (y < ltPoint.getY() + zoomButtonRadius)) {
            return RIGHT_TOP;
        } else if ((x > (ltPoint.getX() - zoomButtonRadius)) && (x < ltPoint.getX() + zoomButtonRadius) &&
                (y > rbPoint.getY() - zoomButtonRadius) && (y < rbPoint.getY() + zoomButtonRadius)) {
            return LEFT_BOTTOM;
        } else if ((x > (rbPoint.getX() - zoomButtonRadius)) && (x < rbPoint.getX() + zoomButtonRadius) &&
                (y > rbPoint.getY() - zoomButtonRadius) && (y < rbPoint.getY() + zoomButtonRadius)) {
            return RIGHT_BOTTOM;
        } else if ((x > ltPoint.getX() - padding) && (x < rbPoint.getX() + padding) &&
                (y > ltPoint.getY() - padding) && (y < rbPoint.getY() + padding)) {
            return INSIDE;
        } else {
            return -1;
        }
    }

    public Point getLtPoint(){
        return ltPoint;
    }

    public Point getRbPoint(){
        return rbPoint;
    }

    public void setToolKit(ToolKit toolKit){
        this.toolKit = (ToolKit)toolKit.clone();
    }

    public ToolKit getToolKit(){
        return toolKit;
    }

    public void setLtPoint(double x, double y){
        ltPoint = new Point(x, y);
    }

    public void setRbPoint(double x, double y){
        rbPoint = new Point(x, y);
    }

    @Override
    public Graph clone() {
        //利用序列化进行深复制
        try{
            // 将对象写入流中
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            // 将对象从流中读出
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Graph) ois.readObject();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}

