

import java.io.*;

/**
 * Created by troub on 2017/10/21.
 */
public class Tool implements Cloneable, Serializable {
    //笔类型
    public static final int SELECTPENCIL = 0;
    public static final int PENCIL = 1;
    public static final int LINEPENCIL = 2;
    public static final int RECTANGLEPENCIL = 3;
    public static final int CIRCLEPENCIL = 4;
    public static final int TEXTPENCIL = 5;
    public static final int ERASER = 6;

    //笔类型
    private int pencilType;

    Tool(){
        pencilType = SELECTPENCIL;
    }

    /**
     * pencil类型设置器
     * @param pencilType int pencil
     */
    public void setPencilType(int pencilType){
        this.pencilType = pencilType;
    }

    /**
     * pencil类型访问器
     * @return pencilType
     */
    public int getPencilType(){
        return pencilType;
    }

    @Override
    public Tool clone() {
        //利用序列化进行深复制
        try{
            // 将对象写入流中
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            // 将对象从流中读出
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Tool) ois.readObject();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
