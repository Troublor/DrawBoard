

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.*;

/**
 * Created by troub on 2017/10/19.
 */
public class ToolKit implements Cloneable, Serializable{


    //笔头类型
    public static final int CIRCLEHEAD = 1;
    public static final int RECTANGLEHEAD = 2;
    public static final int TRIANGLEHEAD = 3;
    public static final int LINEHEAD = 4;

    //笔头大小
    private double pencilSize;

    //笔头类型
    private int pencilHead;

    //颜色
    transient private Color pencilColor;

    //字体大小
    private double fontSize;



    //矩形偏好
    //是否填充
    private boolean rectFill;

    //填充颜色
    transient private Color rectFillColor;

    //是否渐变色
    private boolean rectGradient;

    //渐变到的颜色
    transient private Color rectGradientColor;

    //是否圆角
    private boolean rectRound;

    //圆角半径
    private double rectArcRadius;


    //圆形偏好
    //是否填充
    private boolean ovelFill;

    //填充颜色
    transient private Color ovelFillColor;

    //是否渐变色
    private boolean ovelGradient;

    //渐变到的颜色
    transient private Color ovelGradientColor;

    //是否阴影
    private boolean ovelShadow;

    //阴影宽度
    private double ovelShadowWidth;

    //阴影颜色
    transient private Color ovelShadowColor;

    //文本偏好
    //字体
    private String textFont;

    //粗体
    private boolean textBold;

    //斜体
    private boolean textItalic;

    //文字模糊
    private boolean textFuzzy;

    //文字倒影
    private boolean textReflect;

    //文字外阴影
    private boolean textOutsideShadow;

    //文字内阴影
    private boolean textInsideShadow;


    ToolKit(){
        //默认圆形笔头
        this.pencilHead = CIRCLEHEAD;
        //默认粗细为2
        this.pencilSize = 2;
        //默认画笔/字体/填充颜色为
        this.pencilColor = Color.BLACK;
        //默认字号为
        this.fontSize=Font.getDefault().getSize();

        //矩形偏好设置
        //矩形默认不填充
        this.rectFill = false;
        //矩形默认填充颜色为白色
        this.rectFillColor=Color.WHITE;
        //矩形默认不渐变色
        this.rectGradient = false;
        //矩形默认渐变到的颜色为白色
        this.rectGradientColor = Color.WHITE;
        //矩形默认不是圆角
        this.rectRound = false;
        //矩形默认圆角半径为5
        this.rectArcRadius = 5;

        //圆形偏好设置
        //圆形默认不填充
        this.ovelFill = false;
        //圆形默认填充白色
        this.ovelFillColor = Color.WHITE;
        //圆形默认不渐变色
        this.ovelGradient = false;
        //圆形默认渐变到白色
        this.ovelGradientColor = Color.WHITE;
        //圆形默认没有阴影
        this.ovelShadow = false;
        //圆形默认阴影宽度为5
        this.ovelShadowWidth = 5;
        //圆形默认阴影颜色为黑色
        this.ovelShadowColor = Color.BLACK;

        //字体偏好设置
        //默认字体为默认
        this.textFont = Font.getDefault().getFamily();
        //默认不加粗
        this.textBold = false;
        //默认不斜体
        this.textItalic = false;
        //默认没有模糊
        this.textFuzzy = false;
        //默认没有倒影
        this.textReflect = false;
        //默认没有内阴影
        this.textInsideShadow = false;
        //默认没有外阴影
        this.textOutsideShadow = false;
    }


    @Override
    public ToolKit clone() {
        //利用序列化进行深复制
        try{
            // 将对象写入流中
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            // 将对象从流中读出
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (ToolKit)ois.readObject();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置笔头
     * @param pencilHead int 笔头类型
     */
    public void setPencilHead(int pencilHead){
        this.pencilHead = pencilHead;
    }

    /**
     * 获取笔头类型
     * @return pencilHead
     */
    public int getPencilHead(){
        return pencilHead;
    }

    /**
     * 设置笔大小
     * @param pencilSize double 笔大小
     */
    public void setPencilSize(double pencilSize){
        this.pencilSize = pencilSize;
    }

    /**
     * 获取笔大小
     * @return pencilSize
     */
    public double getPencilSize(){
        return pencilSize;
    }

    /**
     * 设置笔颜色
     * @param pencilColor Color 笔颜色
     */
    public void setPencilColor(Color pencilColor){
        this.pencilColor = pencilColor;
    }

    /**
     * 获取笔颜色
     * @return pencilColor
     */
    public Color getPencilColor(){
        return pencilColor;
    }

    /**
     * 获取字号
     * @return fontSize
     */
    public double getFontSize() {
        return fontSize;
    }

    /**
     * 设置字号
     * @param fontSize double 字号
     */
    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    //以下矩形偏好的访问器与设置器
    public boolean getRectFill() {
        return rectFill;
    }
    public boolean getRectGradient() {
        return rectGradient;
    }
    public Color getRectFillColor() {
        return rectFillColor;
    }
    public Color getRectGradientColor() {
        return rectGradientColor;
    }
    public boolean getRectRound() {
        return rectRound;
    }
    public double getRectArcRadius() {
        return rectArcRadius;
    }
    public void setRectArcRadius(double arcRadius) {
        this.rectArcRadius = arcRadius;
    }
    public void setRectFill(boolean fill) {
        this.rectFill = fill;
    }
    public void setRectFillColor(Color fillColor) {
        this.rectFillColor = fillColor;
    }
    public void setRectGradient(boolean gradient) {
        this.rectGradient = gradient;
    }
    public void setRectGradientColor(Color gradientColor) {
        this.rectGradientColor = gradientColor;
    }
    public void setRectRound(boolean round) {
        this.rectRound = round;
    }


    //以下是圆形偏好的访问器与设置器
    public boolean getOvelFill() {
        return ovelFill;
    }

    public Color getOvelFillColor() {
        return ovelFillColor;
    }

    public boolean getOvelGradient() {
        return ovelGradient;
    }

    public Color getOvelGradientColor() {
        return ovelGradientColor;
    }

    public boolean getOvelShadow() {
        return ovelShadow;
    }

    public Color getOvelShadowColor() {
        return ovelShadowColor;
    }

    public double getOvelShadowWidth() {
        return ovelShadowWidth;
    }

    public void setOvelFill(boolean ovelFill) {
        this.ovelFill = ovelFill;
    }

    public void setOvelFillColor(Color ovelFillColor) {
        this.ovelFillColor = ovelFillColor;
    }

    public void setOvelGradient(boolean ovelGradient) {
        this.ovelGradient = ovelGradient;
    }

    public void setOvelGradientColor(Color ovelGradientColor) {
        this.ovelGradientColor = ovelGradientColor;
    }

    public void setOvelShadow(boolean ovelShadow) {
        this.ovelShadow = ovelShadow;
    }

    public void setOvelShadowColor(Color ovelShadowColor) {
        this.ovelShadowColor = ovelShadowColor;
    }

    public void setOvelShadowWidth(double ovelShadowWidth) {
        this.ovelShadowWidth = ovelShadowWidth;
    }


    //以下是字体偏好的设置器和访问器
    public String getTextFont() {
        return textFont;
    }
    public boolean getTextBold() {
        return textBold;
    }
    public boolean getTextItalic() {
        return textItalic;
    }
    public boolean getTextFuzzy() {
        return textFuzzy;
    }
    public boolean getTextReflect() {
        return textReflect;
    }
    public boolean getTextOutsideShadow() {
        return textOutsideShadow;
    }
    public boolean getTextInsideShadow() {
        return textInsideShadow;
    }
    public void setTextFont(String fontFamily) {
        this.textFont = fontFamily;
    }
    public void setTextBold(boolean textBold) {
        this.textBold = textBold;
    }
    public void setTextItalic(boolean textItalic) {
        this.textItalic = textItalic;
    }
    public void setTextFuzzy(boolean textFuzzy) {
        this.textFuzzy = textFuzzy;
    }
    public void setTextReflect(boolean textReflect) {
        this.textReflect = textReflect;
    }
    public void setTextOutsideShadow(boolean textOutsideShadow) {
        this.textOutsideShadow = textOutsideShadow;
    }
    public void setTextInsideShadow(boolean textInsideShadow) {
        this.textInsideShadow = textInsideShadow;
    }
    //Serializable相关函数
    private void writeObject(ObjectOutputStream out) throws IOException
    {
/**
 *必须通过调用defaultWriteObject()方法来写入
 *对象的描述以及那些可以被序列化的字段
 */
        out.defaultWriteObject();
        out.writeDouble(pencilColor.getRed());
        out.writeDouble(pencilColor.getGreen());
        out.writeDouble(pencilColor.getBlue());

        out.writeDouble(rectFillColor.getRed());
        out.writeDouble(rectFillColor.getGreen());
        out.writeDouble(rectFillColor.getBlue());
        out.writeDouble(rectGradientColor.getRed());
        out.writeDouble(rectGradientColor.getGreen());
        out.writeDouble(rectGradientColor.getBlue());

        out.writeDouble(ovelFillColor.getRed());
        out.writeDouble(ovelFillColor.getGreen());
        out.writeDouble(ovelFillColor.getBlue());
        out.writeDouble(ovelGradientColor.getRed());
        out.writeDouble(ovelGradientColor.getGreen());
        out.writeDouble(ovelGradientColor.getBlue());
        out.writeDouble(ovelShadowColor.getRed());
        out.writeDouble(ovelShadowColor.getGreen());
        out.writeDouble(ovelShadowColor.getBlue());


    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
    {
/**
 *必须调用defaultReadObject()方法
 */
        in.defaultReadObject();
        double r, g, b;
        r = in.readDouble();
        g = in.readDouble();
        b = in.readDouble();
        pencilColor = new Color(r, g, b, 1);

        rectFillColor = new Color(in.readDouble(), in.readDouble(), in.readDouble(), 1);
        rectGradientColor = new Color(in.readDouble(), in.readDouble(), in.readDouble(), 1);

        ovelFillColor = new Color(in.readDouble(), in.readDouble(), in.readDouble(), 1);
        ovelGradientColor = new Color(in.readDouble(), in.readDouble(), in.readDouble(), 1);
        ovelShadowColor = new Color(in.readDouble(), in.readDouble(), in.readDouble(), 1);

    }


}

