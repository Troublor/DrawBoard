

import java.io.Serializable;

/**
 * Created by troub on 2017/10/21.
 */
public class Point implements Serializable, Cloneable{
    private double x;
    private double y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x){
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public Object clone() {
        Point newone = null;
        try{
            newone = (Point)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return newone;
    }
}
