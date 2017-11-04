

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;

/**
 * Created by troub on 2017/10/21.
 */
public class Text extends Graph {
    private String text;

    Text(Point lt, Point rb, String t) {
        super(lt, rb);
        text = t;
    }

    Text(double ltx, double lty, double rbx, double rby, String t){
        this(new Point(ltx, lty), new Point(rbx, rby), t);
    }

    @Override
    public void draw(GraphicsContext gc) {
        //预处理，超过行宽度换行
        int maxChar = (int) ((rbPoint.getX() - ltPoint.getX()) / toolKit.getFontSize());
        ArrayList<String> lines = new ArrayList<>();
        if (text.length() <= maxChar) {
            lines.add(text);
        } else {
            int i;
            for(i = 0; i <= text.length() - maxChar; i += maxChar) {
                lines.add(text.substring(i, i + maxChar));
            }
            if (i < text.length()) {
                lines.add(text.substring(i, text.length()));
            }
        }

        gc.setLineDashes(0);
        gc.setFill(toolKit.getPencilColor());
        gc.setStroke(toolKit.getPencilColor());
        gc.setLineWidth(toolKit.getPencilSize());
        if (toolKit.getTextBold()) {
            if (toolKit.getTextItalic()) {
                gc.setFont(Font.font(toolKit.getTextFont(), FontWeight.BOLD, FontPosture.ITALIC, toolKit.getFontSize()));
            } else {
                gc.setFont(Font.font(toolKit.getTextFont(), FontWeight.BOLD, toolKit.getFontSize()));
            }
        } else {
            if (toolKit.getTextItalic()) {
                gc.setFont(Font.font(toolKit.getTextFont(), FontPosture.ITALIC, toolKit.getFontSize()));
            } else {
                gc.setFont(Font.font(toolKit.getTextFont(), toolKit.getFontSize()));
            }
        }
        if (toolKit.getTextFuzzy()) {
            gc.setEffect(new GaussianBlur(toolKit.getFontSize() * 0.25));
        } else if (toolKit.getTextOutsideShadow()) {
            DropShadow dropShadow = new DropShadow();
            dropShadow.setOffsetY(toolKit.getFontSize() * 0.1);
            dropShadow.setColor(Color.BLACK);
            gc.setEffect(dropShadow);
        } else if (toolKit.getTextInsideShadow()) {
            InnerShadow innerShadow = new InnerShadow();
            innerShadow.setOffsetY(toolKit.getFontSize() * 0.01);
            innerShadow.setOffsetX(toolKit.getFontSize() * 0.01);
            gc.setEffect(innerShadow);
        } else if (toolKit.getTextReflect()) {
            Reflection r = new Reflection();
            r.setFraction(toolKit.getFontSize() * 0.5);
            gc.setEffect(r);
        }


        //输出多行
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            gc.fillText(line, ltPoint.getX(), ltPoint.getY() + toolKit.getFontSize() * (i + 1), rbPoint.getX() - ltPoint.getX());
        }
        gc.setEffect(null);
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
