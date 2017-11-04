

import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class Layer implements Serializable {
    private String name;
    transient private Color backgroundColor;
    private ArrayList<Graph> graphics;

    Layer(String n) {
        name = n;
        graphics = new ArrayList<>();
        backgroundColor= Color.WHITE;
    }

    public ArrayList<Graph> getGraphics() {
        return graphics;
    }

    public void setGraphics(ArrayList<Graph> graphics) {
        this.graphics = graphics;
    }

    public String getName() {
        return name;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    //Serializable相关函数
    private void writeObject(ObjectOutputStream out) throws IOException
    {
/**
 *必须通过调用defaultWriteObject()方法来写入
 *对象的描述以及那些可以被序列化的字段
 */
        out.defaultWriteObject();
        out.writeDouble(backgroundColor.getRed());
        out.writeDouble(backgroundColor.getGreen());
        out.writeDouble(backgroundColor.getBlue());
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
    {
/**
 *必须调用defaultReadObject()方法
 */
        in.defaultReadObject();
        backgroundColor = new Color(in.readDouble(), in.readDouble(), in.readDouble(), 1);
    }


}
