

/**
 * Created by troub on 2017/10/22.
 */

import java.io.*;
import java.util.ArrayList;

/**
 * 备忘录类
 */
public class Memento implements Serializable, Cloneable{

    /**
     * Controller类的镜像
     */
    private Tool tool;
    private ToolKit toolKit, globalToolKit;
    private Graph selectedGraph;
    private ArrayList<Graph> graphics;
    private ArrayList<Layer> layers;
    private int layerIndex;

    Memento() {
        graphics = new ArrayList<Graph>();
    }

    public void setTool(Tool tool) {
        this.tool = tool.clone();
    }

    public void setToolKit(ToolKit toolKit) {
        this.toolKit = toolKit.clone();
    }

    public void setGlobalToolKit(ToolKit globalToolKit) {
        this.globalToolKit = globalToolKit.clone();
    }

    public void setSelectedGraph(Graph selectedGraph) {
        if (selectedGraph == null) {
            this.selectedGraph = null;
            return;
        }
        this.selectedGraph = selectedGraph.clone();
    }

    public void setGraphics(ArrayList<Graph> graphics) {
       /* for (Graph graph : graphics) {
            this.graphics.add(graph.clone());
        }*/
       this.graphics = (ArrayList<Graph>)graphics.clone();
    }

    public void setLayers(ArrayList<Layer> layers) {
        this.layers = layers;
    }

    public void setLayerIndex(int layerIndex) {
        this.layerIndex = layerIndex;
    }

    public int getLayerIndex() {
        return layerIndex;
    }

    public ArrayList<Layer> getLayers() {
        return layers;
    }

    public Tool getTool() {
        return tool;
    }

    public ToolKit getToolKit() {
        return toolKit;
    }

    public ToolKit getGlobalToolKit() {
        return globalToolKit;
    }

    public Graph getSelectedGraph() {
        return selectedGraph;
    }

    public ArrayList<Graph> getGraphics() {
        return graphics;
    }

    @Override
    public Memento clone() {
        //利用序列化进行深复制
        try{
            // 将对象写入流中
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            // 将对象从流中读出
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Memento)ois.readObject();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
