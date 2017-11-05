

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class DrawBoardController implements Initializable{
    //Stage
    private Stage stage;

    //备忘录
    private CareTaker careTaker;

    //保存文件路径
    private String savePath;
    //是否有新的操作没有保存到文件
    private boolean saved;

    private Tool tool;
    private ToolKit toolKit, globalToolKit;
    private GraphicsContext gc, draft_gc;
    private Graph selectedGraph, newGraph;
    private Point newGraph_startPoint;
    private boolean isDragged;
    private boolean hasEdited;
    private int editMotion;

    //图层
    private ArrayList<Layer> layers;

    //用于拖拽移动的相对坐标
    //如果用于移动，表示相对于矩形框左上角的坐标
    //如果用于缩放，表示相对于拖拽角对角的坐标
    private double relativeX;
    private double relativeY;

    //用于存储各种画出来的图形，以备保存
    private ArrayList<Graph> graphics;

    //用于复制粘贴
    private Graph copiedGraph;

    @FXML
    private Button selectPencil, pencil, rectanglePencil, linePencil, circlePencil, textPencil, eraser, image;

    @FXML
    private Button circleHead, rectangleHead, triangleHead, lineHead;

    @FXML
    private ComboBox<Double> headSize, fontSize;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Canvas drawBoard;

    @FXML
    private Canvas draft;

    @FXML
    private GridPane root;

    @FXML
    private TextField textField;

    @FXML
    private GridPane inputPane;

    @FXML
    private Button undoButton, redoButton, moveUpButton, moveDownButton;

    //以下是矩形的偏好设置的控件
    @FXML
    private Pane DrawBoardPane, rectPreferencePane;

    @FXML
    private ColorPicker rectFillColorPicker, rectGradientColorPicker;

    @FXML
    private ComboBox<Double> rectArcRadiusComboBox;

    //以下是圆形偏好设置的控件
    @FXML
    private Pane ovelPreferencePane;

    @FXML
    private ColorPicker ovelFillColorPicker, ovelGradientColorPicker, ovelShadowColorPicker;

    @FXML
    private ComboBox<Double> ovelShadowWidthComboBox;

    //以下是文本偏好设置的控件
    @FXML
    private Pane textPreferencePane;

    @FXML
    private ComboBox<String> textFontComboBox;

    //图层
    @FXML
    private ListView<String> layerList;

    @FXML
    private ColorPicker layerBackgroundColor;

    //显示信息的文本
    @FXML
    private Label message;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources){
        //初始设置
        careTaker = new CareTaker();
        graphics = new ArrayList<Graph>();
        for (double i = 1; i <= 30; i++){
            headSize.getItems().add(i);
        }
        headSize.setValue(2.0);
        for (double i = 10; i <= 100; i+=5) {
            fontSize.getItems().add(i);
        }
        fontSize.setValue(Font.getDefault().getSize());
        for (double i = 0; i <= 200; i += 20) {
            rectArcRadiusComboBox.getItems().add(i);
        }
        rectArcRadiusComboBox.setValue(5.0);
        for (double i = 0; i <= 100; i += 5) {
            ovelShadowWidthComboBox.getItems().add(i);
        }
        rectArcRadiusComboBox.setValue(2.0);
        for (String family : Font.getFontNames()) {
            textFontComboBox.getItems().add(family);
        }
        textFontComboBox.setValue(Font.getDefault().getName());
        colorPicker.setValue(Color.BLACK);
        gc = drawBoard.getGraphicsContext2D();
        draft_gc = draft.getGraphicsContext2D();

        //图层
        layers = new ArrayList<>();
        Layer layer = new Layer("图层0");
        layer.setGraphics(graphics);
        layers.add(layer);
        layerList.getItems().add("图层0");
        layerList.getSelectionModel().select(0);

        isDragged = false;
        editMotion = -1;
        savePath = null;
        saved = false;

        //文本输入事件监听
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (selectedGraph != null && selectedGraph instanceof Text){
                    Text t = (Text)selectedGraph;
                    t.setText(textField.getText());
                    clearArea(draft);
                    selectedGraph.draw(draft_gc);
                    selectedGraph.select(draft_gc);
                }
            }
        });

        //初始化
        toolKit = new ToolKit();
        tool = new Tool();
        globalToolKit = toolKit;
        selectedGraph = null;

        save();
        undoButton.setDisable(!careTaker.undoable());
        redoButton.setDisable(!careTaker.redoable());

    }

    @FXML
    private void Redo(ActionEvent event) {
        Memento memento = careTaker.getNextMemento();
        if (memento == null) {
            return;
        }

        recover(memento);
    }

    private void recover(Memento memento){
        tool = memento.getTool();
        toolKit = memento.getToolKit();
        globalToolKit = memento.getGlobalToolKit();
        selectedGraph = memento.getSelectedGraph();
        graphics = memento.getGraphics();
        layers = memento.getLayers();
        //刷新页面
        clearArea(drawBoard);
        clearArea(draft);
        layerList.getItems().clear();
        for (Layer layer : layers) {
            layerList.getItems().add(layer.getName());
        }
        layerList.getSelectionModel().select(memento.getLayerIndex());
        for (Graph graph : graphics) {
            if (selectedGraph != null && graph.isSelected(selectedGraph.ltPoint.getX(), selectedGraph.getLtPoint().getY())) {
                selectedGraph = graph;
                graph.draw(draft_gc);
                graph.select(draft_gc);
                switchToDraft();
                toolKit = graph.getToolKit();
                showToolKit(toolKit);
                //如果是文本框
                if (graph instanceof Text) {
                    Text t = (Text) graph;
                    textField.setText(t.getText());
                    t.select(draft_gc);
                    inputPane.setVisible(true);
                }
                continue;
            }
            graph.draw(gc);
        }
        if (selectedGraph != null) {
            switchToDraft();
        }else {
            switchToDrawBoard();
        }

        //恢复button
        resetPencils();
        Button b = null;
        if (tool.getPencilType() == Tool.SELECTPENCIL) {
            b = selectPencil;
        } else if (tool.getPencilType() == Tool.PENCIL) {
            b = pencil;
        } else if (tool.getPencilType() == Tool.LINEPENCIL) {
            b = linePencil;
        } else if (tool.getPencilType() == Tool.RECTANGLEPENCIL) {
            b = rectanglePencil;
        } else if (tool.getPencilType() == Tool.CIRCLEPENCIL) {
            b = circlePencil;
        } else if (tool.getPencilType() == Tool.TEXTPENCIL) {
            b = textPencil;
        }
        if (b != null) {
            b.setStyle("-fx-background-color: greenyellow");
        }

        //恢复颜色,大小
        colorPicker.setValue(toolKit.getPencilColor());
        headSize.setValue(toolKit.getPencilSize());

        undoButton.setDisable(!careTaker.undoable());
        redoButton.setDisable(!careTaker.redoable());
    }

    @FXML
    private void Undo(ActionEvent event) {
        Memento memento = careTaker.getLastMemento();
        if (memento == null) {
            return;
        }

        recover(memento);

    }

    @FXML
    private void NewProject(ActionEvent event) {
        try {
            //打开一个新的窗口
            new DrawBoard().start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void Save(ActionEvent event) {
        if (savePath == null) {
            SaveAs(event);
        } else {
            try {
                ObjectOutputStream os = new ObjectOutputStream(
                        new FileOutputStream(savePath));
                os.writeObject(layers);// 将graphics写入文件
                os.close();
                message.setText("已保存");

                saved = true;
                stage.setTitle("DrawBoard - [" + savePath + "]"  + " - [已保存]");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @FXML
    private void SaveAs(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        try{
            fileChooser.setTitle("另存为...");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("DrawBoard", "*.dbd")
            );
            File file = fileChooser.showSaveDialog(root.getScene().getWindow());
            if (file != null){
                savePath = file.getAbsolutePath();
                ObjectOutputStream os = new ObjectOutputStream(
                        new FileOutputStream(file.getAbsolutePath()));
                os.writeObject(layers);// 将layers写入文件
                os.close();
                message.setText("已另存为");

                saved = true;
                stage.setTitle("DrawBoard - [" + savePath + "]"  + " - [已保存]");
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void Open(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        try {
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("DrawBoard", "*.dbd")
            );
            File file = fileChooser.showOpenDialog(root.getScene().getWindow());
            if (file == null) {
                return;
            }
            savePath = file.getAbsolutePath();
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(
                    file.getAbsolutePath()));
            ArrayList<Layer> temp = (ArrayList<Layer>) is.readObject();// 从流中读取layers的数据
            layers = temp;
            is.close();

            saved = true;
            stage.setTitle("DrawBoard - [" + savePath + "]" + " - [已保存]");

            graphics = new ArrayList<>();
            //显示所有图层
            layerList.getItems().clear();
            for (Layer layer : layers) {
                layerList.getItems().add(layer.getName());
            }
            ShowAllLayers(event);
        /*for (Layer layer : layers) {
            graphics.addAll(layer.getGraphics());
        }*/
            selectedGraph = null;
            clearArea(draft);
            clearArea(drawBoard);
            for (Graph graph : graphics) {
                graph.draw(gc);
            }
            switchToDrawBoard();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "打开文件失败");
            alert.setContentText("打开失败\n失败原因：" + e.getClass());
            alert.showAndWait();
        }

    }

    @FXML
    private void ExportCurrentLayer(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        try{
            fileChooser.setTitle("导出");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG图片", "*.png"),
                    new FileChooser.ExtensionFilter("JPG图片", "*.jpg")
            );
            File file = fileChooser.showSaveDialog(root.getScene().getWindow());
            if (file != null){
                WritableImage image = DrawBoardPane.snapshot(new SnapshotParameters(), null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                message.setText("已导出");
            }

        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void SelectGraph(MouseEvent event){
        if (isDragged){
            isDragged = false;
            return;
        }
        //刷新页面
        clearArea(drawBoard);
        clearArea(draft);
        rectPreferencePane.setVisible(false);
        ovelPreferencePane.setVisible(false);
        moveDownButton.setDisable(true);
        moveUpButton.setDisable(true);
        //显示相应类型图形的偏好
        if (tool.getPencilType() == Tool.RECTANGLEPENCIL) {
            rectPreferencePane.setVisible(true);
        } else {
            rectPreferencePane.setVisible(false);
        }
        if (tool.getPencilType() == Tool.CIRCLEPENCIL) {
            ovelPreferencePane.setVisible(true);
        } else {
            ovelPreferencePane.setVisible(false);
        }
        if (tool.getPencilType() == Tool.TEXTPENCIL) {
            textPreferencePane.setVisible(true);
        } else {
            textPreferencePane.setVisible(false);
        }
        //查找点击的是哪一个图形
        Graph graph;
        boolean hasSelected = false;
        for (int i = graphics.size() - 1; i >= 0; i--){
            graph = graphics.get(i);
            if (tool.getPencilType() == Tool.SELECTPENCIL && !hasSelected){
                if (graph.isSelected(event.getX(), event.getY())){
                    graph.draw(draft_gc);
                    graph.select(draft_gc);
                    selectedGraph = graph;
                    switchToDraft();
                    toolKit = graph.getToolKit();
                    showToolKit(toolKit);
                    hasSelected = true;
                    moveDownButton.setDisable(false);
                    moveUpButton.setDisable(false);

                    //如果是文本框
                    if (graph instanceof Text) {
                        Text t = (Text)graph;
                        textField.setText(t.getText());
                        inputPane.setVisible(true);
                        t.select(draft_gc);
                    }
                    if (graph instanceof Rect) {
                        rectPreferencePane.setVisible(true);
                    }
                    if (graph instanceof Ovel) {
                        ovelPreferencePane.setVisible(true);
                    }
                    if (graph instanceof Text) {
                        textPreferencePane.setVisible(true);
                    }
                    continue;
                }
            }
        }
        for (Graph graph1 : graphics) {
            if (graph1 != selectedGraph) {
                graph1.draw(gc);
            }
        }
        if (!hasSelected){
            switchToDrawBoard();
            selectedGraph = null;
            toolKit = globalToolKit;
            showToolKit(toolKit);
        }

    }

    @FXML
    private void EditExit(MouseEvent event){
        if (selectedGraph != null && !selectedGraph.isSelected(event.getX(), event.getY())) {
            clearArea(draft);
            selectedGraph.draw(gc);
            switchToDrawBoard();
            inputPane.setVisible(false);
            textField.setText("");

            SelectGraph(event);
        }
    }

    @FXML
    private void CursorChange(MouseEvent event){
        if (selectedGraph != null) {
            if (selectedGraph.getCursorLocation(event.getX(), event.getY()) == Graph.LEFT_TOP) {
                draft.setCursor(Cursor.NW_RESIZE);
            } else if (selectedGraph.getCursorLocation(event.getX(), event.getY()) == Graph.RIGHT_TOP) {
                draft.setCursor(Cursor.NE_RESIZE);
            } else if (selectedGraph.getCursorLocation(event.getX(), event.getY()) == Graph.LEFT_BOTTOM) {
                draft.setCursor(Cursor.SW_RESIZE);
            } else if (selectedGraph.getCursorLocation(event.getX(), event.getY()) == Graph.RIGHT_BOTTOM) {
                draft.setCursor(Cursor.SE_RESIZE);
            } else if (selectedGraph.getCursorLocation(event.getX(), event.getY()) == Graph.INSIDE) {
                draft.setCursor(Cursor.MOVE);
            }else {
                draft.setCursor(Cursor.DEFAULT);
            }
        }else {
            draft.setCursor(Cursor.DEFAULT);
        }
    }

    @FXML
    private void EditDetected(MouseEvent event){
        if (selectedGraph == null) {
            return;
        }
        if (selectedGraph.getCursorLocation(event.getX(), event.getY()) == Graph.INSIDE) {
            relativeX = event.getX() - selectedGraph.getLtPoint().getX();
            relativeY = event.getY() - selectedGraph.getLtPoint().getY();
            editMotion = Graph.INSIDE;
        } else if (selectedGraph.getCursorLocation(event.getX(), event.getY()) == Graph.LEFT_TOP) {
            relativeX = selectedGraph.getRbPoint().getX();
            relativeY = selectedGraph.getRbPoint().getY();
            editMotion = Graph.LEFT_TOP;
        } else if (selectedGraph.getCursorLocation(event.getX(), event.getY()) == Graph.RIGHT_TOP) {
            relativeX = selectedGraph.getLtPoint().getX();
            relativeY = selectedGraph.getRbPoint().getY();
            editMotion = Graph.RIGHT_TOP;
        } else if (selectedGraph.getCursorLocation(event.getX(), event.getY()) == Graph.LEFT_BOTTOM) {
            relativeX = selectedGraph.getRbPoint().getX();
            relativeY = selectedGraph.getLtPoint().getY();
            editMotion = Graph.LEFT_BOTTOM;
        } else if (selectedGraph.getCursorLocation(event.getX(), event.getY()) == Graph.RIGHT_BOTTOM) {
            relativeX = selectedGraph.getLtPoint().getX();
            relativeY = selectedGraph.getLtPoint().getY();
            editMotion = Graph.RIGHT_BOTTOM;
        }
    }

    @FXML
    private void EditOver(MouseEvent event){
        if (editMotion == Graph.INSIDE) {
            clearArea(draft);
            selectedGraph.move(relativeX, relativeY, event.getX(), event.getY());
            selectedGraph.draw(draft_gc);
            selectedGraph.select(draft_gc);
            hasEdited = true;
        } else if (editMotion == Graph.LEFT_TOP) {
            //只有在不颠倒左上角和右下角顺序时才能edit
            if (event.getX() <= selectedGraph.getRbPoint().getX() &&
                    event.getY() <= selectedGraph.getRbPoint().getY()) {
                clearArea(draft);
                selectedGraph.zoom(Graph.LEFT_TOP, relativeX - event.getX(), relativeY - event.getY());
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
                hasEdited = true;
            }
        } else if (editMotion == Graph.RIGHT_TOP) {
            if (event.getX() >= selectedGraph.getLtPoint().getX() &&
                    event.getY() <= selectedGraph.getRbPoint().getY()) {
                clearArea(draft);
                selectedGraph.zoom(Graph.RIGHT_TOP, event.getX() - relativeX , relativeY - event.getY());
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
                hasEdited = true;
            }
        } else if (editMotion == Graph.LEFT_BOTTOM) {
            if (event.getX() <= selectedGraph.getRbPoint().getX() &&
                    event.getY() >= selectedGraph.getLtPoint().getY()) {
                clearArea(draft);
                selectedGraph.zoom(Graph.LEFT_BOTTOM, relativeX - event.getX() , event.getY() - relativeY);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
                hasEdited = true;
            }
        } else if (editMotion == Graph.RIGHT_BOTTOM) {
            if (event.getX() >= selectedGraph.getLtPoint().getX() &&
                    event.getY() >= selectedGraph.getLtPoint().getY()) {
                clearArea(draft);
                selectedGraph.zoom(Graph.RIGHT_BOTTOM, event.getX() - relativeX , event.getY() - relativeY);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
                hasEdited = true;
            }
        }
    }

    @FXML
    private void CloseInputPane(ActionEvent event){
        //刷新页面
        clearArea(drawBoard);
        clearArea(draft);
        selectedGraph = null;
        switchToDrawBoard();
        for (Graph graph : graphics) {
            graph.draw(gc);
        }
        textField.clear();
        inputPane.setVisible(false);
        //保存备忘录
        save();
        undoButton.setDisable(!careTaker.undoable());
        redoButton.setDisable(!careTaker.redoable());
    }

    @FXML
    private void EditDone(MouseEvent event){
        if (hasEdited){
            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }
        editMotion = -1;
        hasEdited = false;
    }

    @FXML
    private void DrawDetected(MouseEvent event){
        if (tool.getPencilType() != Tool.SELECTPENCIL && tool.getPencilType() != Tool.ERASER) {
            gc.setFill(toolKit.getPencilColor());
            gc.setStroke(toolKit.getPencilColor());
            if (tool.getPencilType() == Tool.PENCIL) {
                newGraph = new FreeLine(event.getX(), event.getY(), event.getX(), event.getY());
            } else if (tool.getPencilType() == Tool.RECTANGLEPENCIL) {
                newGraph = new Rect(event.getX(), event.getY(), event.getX(), event.getY());
            } else if (tool.getPencilType() == Tool.CIRCLEPENCIL) {
                newGraph = new Ovel(event.getX(), event.getY(), event.getX(), event.getY());
            } else if (tool.getPencilType() == Tool.LINEPENCIL) {
                newGraph = new Line(event.getX(), event.getY(), event.getX(), event.getY());
            } else if (tool.getPencilType() == Tool.TEXTPENCIL) {
                newGraph = new Text(event.getX(), event.getY(), event.getX(), event.getY(), "");
            }
            newGraph_startPoint = new Point(event.getX(), event.getY());
            newGraph.setToolKit(toolKit);
        }

    }

    @FXML
    private void DrawOver(MouseEvent event){
        if (tool.getPencilType() == Tool.PENCIL){
            clearArea(draft);
            FreeLine freeLine = (FreeLine)newGraph;
            freeLine.draw(draft_gc);
            freeLine.addPoint(new Point(event.getX(), event.getY()));
            /*switch (toolKit.getPencilHead()){
                case ToolKit.CIRCLEHEAD:
                    gc.fillOval(event.getX() - toolKit.getPencilSize() / 2,
                            event.getY() - toolKit.getPencilSize() / 2,
                            toolKit.getPencilSize(),
                            toolKit.getPencilSize());
                    break;
                case ToolKit.RECTANGLEHEAD:
                    gc.fillRect(event.getX() - toolKit.getPencilSize() / 2,
                            event.getY() - toolKit.getPencilSize() / 2,
                            toolKit.getPencilSize(),
                            toolKit.getPencilSize());
                    break;
                case ToolKit.TRIANGLEHEAD:
                    gc.fillPolygon(
                            new double[]{
                                    event.getX(),
                                    event.getX() - toolKit.getPencilSize() / 2,
                                    event.getX() + toolKit.getPencilSize() / 2},
                            new double[]{
                                    event.getY() + toolKit.getPencilSize() + Math.sqrt(3) / 2,
                                    event.getY() - toolKit.getPencilSize() / 2 / Math.sqrt(3),
                                    event.getY() - toolKit.getPencilSize() / 2 / Math.sqrt(3)
                            },
                            3
                    );
                    break;
                case ToolKit.LINEHEAD:
                    gc.strokeLine(
                            event.getX(),
                            event.getY() - toolKit.getPencilSize() / 2,
                            event.getX(),
                            event.getY() + toolKit.getPencilSize()
                    );
            }*/
        }else {
            if (tool.getPencilType() == Tool.ERASER) {
                EraserMessage(event);
                boolean a, b, c, d;
                for (Graph graph : graphics) {
                    if (graph instanceof FreeLine) {
                        FreeLine freeLine = (FreeLine) graph;
                        freeLine.erase(event.getX(), event.getY(), 10);
                        clearArea(drawBoard);
                        for (Graph graph1 : graphics) {
                            graph1.draw(gc);
                        }
                    }
                }
            } else if (tool.getPencilType() != Tool.SELECTPENCIL) {
                clearArea(draft);
                //如果鼠标轨迹不是按照从左上到右下的顺序
                //更新左上和右下点
                Point newLtPoint,newRbPoint;
                if (event.getX() < newGraph_startPoint.getX()) {
                    if (event.getY() < newGraph_startPoint.getY()) {
                        newLtPoint = new Point(event.getX(), event.getY());
                        newRbPoint = new Point(newGraph_startPoint.getX(), newGraph_startPoint.getY());
                        if (newGraph instanceof Line) {
                            Line newLine = (Line) newGraph;
                            newLine.setStartPoint(Line.LinePoint.RIGHT_BOTTOM);
                            newLine.setEndPoint(Line.LinePoint.LEFT_TOP);
                        }
                    } else {
                        newLtPoint = new Point(event.getX(), newGraph_startPoint.getY());
                        newRbPoint = new Point(newGraph_startPoint.getX(), event.getY());
                        if (newGraph instanceof Line) {
                            Line newLine = (Line) newGraph;
                            newLine.setStartPoint(Line.LinePoint.RIGHT_TOP);
                            newLine.setEndPoint(Line.LinePoint.LEFT_BOTTOM);
                        }
                    }
                } else {
                    if (event.getY() < newGraph_startPoint.getY()) {
                        newLtPoint = new Point(newGraph_startPoint.getX(), event.getY());
                        newRbPoint = new Point(event.getX(), newGraph_startPoint.getY());
                        if (newGraph instanceof Line) {
                            Line newLine = (Line) newGraph;
                            newLine.setStartPoint(Line.LinePoint.LEFT_BOTTOM);
                            newLine.setEndPoint(Line.LinePoint.RIGHT_TOP);
                        }
                    } else {
                        newLtPoint = newGraph_startPoint;
                        newRbPoint = new Point(event.getX(), event.getY());
                        if (newGraph instanceof Line) {
                            Line newLine = (Line) newGraph;
                            newLine.setStartPoint(Line.LinePoint.LEFT_TOP);
                            newLine.setEndPoint(Line.LinePoint.RIGHT_BOTTOM);
                        }
                    }
                }
                newGraph.setLtPoint(newLtPoint.getX(), newLtPoint.getY());
                newGraph.setRbPoint(newRbPoint.getX(), newRbPoint.getY());
                if (tool.getPencilType() == Tool.TEXTPENCIL) {
                    newGraph.select(draft_gc);
                }
                newGraph.draw(draft_gc);
            }
        }
    }

    @FXML
    private void DrawDone(MouseEvent event){

        if (tool.getPencilType() != Tool.SELECTPENCIL && tool.getPencilType() != Tool.ERASER) {
            try {
                clearArea(draft);
                graphics.add(newGraph);
                if (tool.getPencilType() == Tool.TEXTPENCIL) {
                    isDragged = true;
                    inputPane.setVisible(true);
                    newGraph.draw(draft_gc);
                    newGraph.select(draft_gc);
                    selectedGraph = newGraph;
                    switchToDraft();
                    toolKit = newGraph.getToolKit();
                    showToolKit(toolKit);
                    event.consume();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }
    }

    @FXML
    private void DeleteGraph(ActionEvent event){
        if (selectedGraph != null) {
            graphics.remove(selectedGraph);
            switchToDrawBoard();

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("删除失败");
            alert.setContentText("没有选中任何图形");
            alert.showAndWait();
        }
    }

    @FXML
    private void ClearDrawBoard(ActionEvent event) {
        graphics.clear();
        selectedGraph = null;
        switchToDrawBoard();
        clearArea(draft);
        clearArea(drawBoard);

        //保存备忘录
        save();
        undoButton.setDisable(!careTaker.undoable());
        redoButton.setDisable(!careTaker.redoable());
    }

    @FXML
    private void switchPencilType(MouseEvent event){
        resetPencils();
        ClearMessage(event);
        Button button = (Button)event.getSource();
        button.setStyle("-fx-background-color: greenyellow");
        try{
            Class reflect = Class.forName("Tool");
            Field field = reflect.getField(button.getId().toUpperCase());
            tool.setPencilType(field.getInt(reflect));
            toolKit = globalToolKit;
            showToolKit(toolKit);
            if (tool.getPencilType() == Tool.ERASER) {
                drawBoard.setCursor(Cursor.CLOSED_HAND);
            } else if (tool.getPencilType() != Tool.SELECTPENCIL) {
                drawBoard.setCursor(Cursor.CROSSHAIR);
            } else {
                drawBoard.setCursor(Cursor.DEFAULT);
            }

            //显示相应类型图形的偏好
            if (tool.getPencilType() == Tool.RECTANGLEPENCIL) {
                rectPreferencePane.setVisible(true);
            } else {
                rectPreferencePane.setVisible(false);
            }
            if (tool.getPencilType() == Tool.CIRCLEPENCIL) {
                ovelPreferencePane.setVisible(true);
            } else {
                ovelPreferencePane.setVisible(false);
            }
            if (tool.getPencilType() == Tool.TEXTPENCIL) {
                textPreferencePane.setVisible(true);
            } else {
                textPreferencePane.setVisible(false);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        switchToDrawBoard();
    }

    @FXML
    private void setPencilSize(ActionEvent event){
        try{
            toolKit.setPencilSize((Double)headSize.getValue());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            System.out.println(e.getClass() + " in setPencilSize");
        }
    }

    @FXML
    private void setColor(ActionEvent event){
        toolKit.setPencilColor((Color)colorPicker.getValue());
        if (selectedGraph != null) {
            clearArea(draft);
            selectedGraph.draw(draft_gc);
            selectedGraph.select(draft_gc);
        }

        //保存备忘录
        save();
        undoButton.setDisable(!careTaker.undoable());
        redoButton.setDisable(!careTaker.redoable());
    }

    @FXML
    private void setFontSize(ActionEvent event) {
        try {
            ComboBox<Double> fontSizeComboBox = (ComboBox<Double>) event.getSource();
            toolKit.setFontSize(fontSizeComboBox.getValue());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        } catch (Exception e) {
            System.out.println(e.getClass() + " in setFontSize");
        }

    }

    @FXML
    private void AddImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("图片");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("GIF", "*.gif"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        try{
            File file = fileChooser.showOpenDialog(root.getScene().getWindow());
            if (file == null) {
                return;
            }
            newGraph = new Img(draft.getLayoutX() + 100, draft.getLayoutY() + 100, draft.getLayoutX() + 300, draft.getLayoutY() + 300, file.getAbsolutePath());
            clearArea(draft);
            clearArea(drawBoard);
            for (Graph graph : graphics) {
                graph.draw(gc);
            }
            graphics.add(newGraph);
            newGraph.draw(draft_gc);
            newGraph.select(draft_gc);
            selectedGraph = newGraph;
            switchToDraft();
            toolKit = newGraph.getToolKit();
            showToolKit(toolKit);
            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    private void MoveUp(ActionEvent event) {
        if (selectedGraph != null) {
            int currentIndex = graphics.indexOf(selectedGraph);
            graphics.remove(currentIndex);
            graphics.add(selectedGraph);
            clearArea(draft);
            clearArea(drawBoard);
            for (Graph graph : graphics) {
                if (graph == selectedGraph) {
                    continue;
                }
                graph.draw(gc);
            }
            selectedGraph.draw(draft_gc);
            selectedGraph.select(draft_gc);
            message.setText("当前图形已处于最顶层");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("操作失败");
            alert.setContentText("需要选中一个图形");
            alert.showAndWait();
        }
    }

    @FXML
    private void MoveDown(ActionEvent event) {
        if (selectedGraph != null) {
            int currentIndex = graphics.indexOf(selectedGraph);
            graphics.remove(currentIndex);
            graphics.add(0, selectedGraph);
            clearArea(draft);
            clearArea(drawBoard);
            for (Graph graph : graphics) {
                if (graph == selectedGraph) {
                    continue;
                }
                graph.draw(gc);
            }
            selectedGraph.draw(draft_gc);
            selectedGraph.select(draft_gc);
            message.setText("当前图形已处于最底层");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("操作失败");
            alert.setContentText("需要选中一个图形");
            alert.showAndWait();
        }
    }

    private void resetPencils(){
        pencil.setStyle("-fx-background-color: gray");
        linePencil.setStyle("-fx-background-color: gray");
        rectanglePencil.setStyle("-fx-background-color: gray");
        circlePencil.setStyle("-fx-background-color: gray");
        selectPencil.setStyle("-fx-background-color: gray");
        textPencil.setStyle("-fx-background-color: gray");
        eraser.setStyle("-fx-background-color: gray");
    }

    private void resetPencilHeads(){
        circleHead.setStyle("-fx-background-color: white");
        rectangleHead.setStyle("-fx-background-color: white");
        triangleHead.setStyle("-fx-background-color: white");
        lineHead.setStyle("-fx-background-color: white");
    }

    private void clearArea(Canvas area){
        GraphicsContext gc = area.getGraphicsContext2D();
        gc.clearRect(area.getLayoutX(), area.getLayoutY(), area.getWidth(), area.getHeight());
    }

    private void switchToDraft(){
        draft.setScaleZ(1);
        drawBoard.setScaleZ(0);
    }

    private void switchToDrawBoard(){
        if (selectedGraph != null){
            selectedGraph = null;
            clearArea(draft);
            clearArea(drawBoard);
            for (Graph graph : graphics) {
                graph.draw(gc);
            }
        }
        draft.setScaleZ(0);
        drawBoard.setScaleZ(1);
    }

    private void showToolKit(ToolKit toolKit){
        //pencilSize
        headSize.setValue(toolKit.getPencilSize());

        //color
        colorPicker.setValue(toolKit.getPencilColor());

        //显示矩形的偏好设置
        GridPane preferenceArea = (GridPane)rectPreferencePane.getChildren().get(1);
        CheckBox checkBox = (CheckBox)preferenceArea.getChildren().get(0);
        checkBox.setSelected(toolKit.getRectFill());
        ColorPicker cp= (ColorPicker)preferenceArea.getChildren().get(1);
        cp.setValue(toolKit.getRectFillColor());
        if (checkBox.isSelected()) {
            cp.setDisable(false);
        } else {
            cp.setDisable(true);
        }
        checkBox = (CheckBox) preferenceArea.getChildren().get(2);
        checkBox.setSelected(toolKit.getRectGradient());
        if (checkBox.isSelected()) {
            cp.setDisable(false);
        } else {
            cp.setDisable(true);
        }
        cp = (ColorPicker) preferenceArea.getChildren().get(3);
        cp.setValue(toolKit.getRectGradientColor());
        checkBox = (CheckBox) preferenceArea.getChildren().get(4);
        checkBox.setSelected(toolKit.getRectRound());
        ComboBox<Double> comboBox = (ComboBox<Double>) preferenceArea.getChildren().get(5);
        comboBox.setValue(toolKit.getRectArcRadius());
        comboBox.setDisable(!checkBox.isSelected());


        //显示Ovel的偏好设置
        preferenceArea = (GridPane)ovelPreferencePane.getChildren().get(1);
        checkBox = (CheckBox)preferenceArea.getChildren().get(0);
        checkBox.setSelected(toolKit.getOvelFill());
        cp = (ColorPicker)preferenceArea.getChildren().get(1);
        cp.setValue(toolKit.getOvelFillColor());
        if (checkBox.isSelected()) {
            cp.setDisable(false);
        } else {
            cp.setDisable(true);
        }
        checkBox = (CheckBox) preferenceArea.getChildren().get(2);
        checkBox.setSelected(toolKit.getOvelGradient());
        cp = (ColorPicker)preferenceArea.getChildren().get(3);
        cp.setValue(toolKit.getOvelGradientColor());
        if (checkBox.isSelected()) {
            cp.setDisable(false);
        } else {
            cp.setDisable(true);
        }
        checkBox = (CheckBox) preferenceArea.getChildren().get(4);
        checkBox.setSelected(toolKit.getOvelShadow());
        comboBox = (ComboBox<Double>)preferenceArea.getChildren().get(5);
        comboBox.setValue(toolKit.getOvelShadowWidth());
        cp = (ColorPicker) preferenceArea.getChildren().get(6);
        cp.setValue(toolKit.getOvelShadowColor());
        comboBox.setDisable(!checkBox.isSelected());
        cp.setDisable(!checkBox.isSelected());

        //显示textPreferencePane偏好设置
        preferenceArea = (GridPane) textPreferencePane.getChildren().get(1);
        ComboBox<String> fontFamily = (ComboBox<String>) preferenceArea.getChildren().get(1);
        fontFamily.setValue(toolKit.getTextFont());
        checkBox = (CheckBox) preferenceArea.getChildren().get(2);
        checkBox.setSelected(toolKit.getTextBold());
        checkBox = (CheckBox) preferenceArea.getChildren().get(3);
        checkBox.setSelected(toolKit.getTextItalic());
        RadioButton radioButton;
        radioButton = (RadioButton) preferenceArea.getChildren().get(4);
        radioButton.setSelected(toolKit.getTextFuzzy());
        radioButton = (RadioButton) preferenceArea.getChildren().get(5);
        radioButton.setSelected(toolKit.getTextReflect());
        radioButton = (RadioButton) preferenceArea.getChildren().get(6);
        radioButton.setSelected(toolKit.getTextOutsideShadow());
        radioButton = (RadioButton) preferenceArea.getChildren().get(7);
        radioButton.setSelected(toolKit.getTextInsideShadow());
    }

    @FXML
    private void SelectLayer(MouseEvent event) {
        //关掉所有图形偏好设置
        rectPreferencePane.setVisible(false);
        ovelPreferencePane.setVisible(false);
        moveDownButton.setDisable(true);
        moveUpButton.setDisable(true);

        layerBackgroundColor.setDisable(false);
        String selectedItem = layerList.getSelectionModel().getSelectedItem();
        for (Layer layer : layers) {
            if (layer.getName().equals(selectedItem)) {
                graphics = layer.getGraphics();
                layerBackgroundColor.setValue(layer.getBackgroundColor());
                DrawBoardPane.setBackground(new Background(new BackgroundFill(layer.getBackgroundColor(),null,null)));
                switchToDrawBoard();
                selectedGraph = null;
                clearArea(draft);
                clearArea(drawBoard);
                break;
            }
        }
        for (Graph graph : graphics) {
            graph.draw(gc);
        }
        message.setText("");
        image.setDisable(false);
        pencil.setDisable(false);
        rectanglePencil.setDisable(false);
        linePencil.setDisable(false);
        circlePencil.setDisable(false);
        eraser.setDisable(false);
        textPencil.setDisable(false);
    }

    @FXML
    private void AddLayer(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("图层" + layers.size());
        dialog.setTitle("添加图层");
        dialog.setHeaderText("新图层");
        dialog.setContentText("图层名称");

        // 传统的获取输入值的方法
        Optional result = dialog.showAndWait();
        if (!result.isPresent()) {
            return;
        }

        //判断是否有重复的图层
        for (Layer layer : layers) {
            if (layer.getName().equals(result.get())) {
                Alert alert=new Alert(Alert.AlertType.ERROR,"失败");
                alert.setHeaderText("添加图层失败");
                alert.setContentText("有重复的图层");
                alert.showAndWait();
                return;
            }
        }

        message.setText("");
        image.setDisable(false);
        pencil.setDisable(false);
        rectanglePencil.setDisable(false);
        linePencil.setDisable(false);
        circlePencil.setDisable(false);
        eraser.setDisable(false);
        textPencil.setDisable(false);
        Layer layer = new Layer(result.get().toString());
        graphics = layer.getGraphics();
        selectedGraph = null;
        clearArea(draft);
        clearArea(drawBoard);
        layerList.getItems().add(layer.getName());
        layerList.getSelectionModel().select(layer.getName());
        layers.add(layer);
        layerBackgroundColor.setValue(layer.getBackgroundColor());
        DrawBoardPane.setBackground(new Background(new BackgroundFill(layerBackgroundColor.getValue(),null,null)));
        save();
    }

    @FXML
    private void DeleteLayer(ActionEvent event) {
        if (layerList.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("删除失败");
            alert.setContentText("没有选中任何图层");
            alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "删除图层", ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText("即将删除本图层");
        alert.setContentText("删除图层将同时删除图层内所有图形");
        Optional result=alert.showAndWait();
        if (result.isPresent()) {
            if (result.get().equals(ButtonType.CANCEL)) {
                return;
            }
        }
        if (layerList.getItems().size() == 1) {
            ClearDrawBoard(event);
            return;
        }
        for (Layer layer : layers) {
            if (layer.getGraphics() == graphics) {
                layers.remove(layer);
                layerList.getItems().remove(layer.getName());
                break;
            }
        }
        ShowAllLayers(event);
        save();
    }

    @FXML
    private void SetLayerBackgroundColor(ActionEvent event) {
        String selectedItem = layerList.getSelectionModel().getSelectedItem();
        for (Layer layer : layers) {
            if (layer.getName().equals(selectedItem)) {
                layer.setBackgroundColor(layerBackgroundColor.getValue());
                DrawBoardPane.setBackground(new Background(new BackgroundFill(layerBackgroundColor.getValue(),null,null)));
                break;
            }
        }
    }

    @FXML
    private void ShowAllLayers(ActionEvent event) {
        DrawBoardPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,null,null)));
        layerBackgroundColor.setDisable(true);
        graphics = new ArrayList<>();
        for (Layer layer : layers) {
            graphics.addAll(layer.getGraphics());
        }
        selectedGraph = null;
        switchToDrawBoard();
        clearArea(draft);
        clearArea(drawBoard);
        for (Graph graph : graphics) {
            graph.draw(gc);
        }
        layerList.getSelectionModel().clearSelection();

        message.setText("显示所有图层的情况下 不能添加图形");
        //显示全部图层的情况下不能添加，只能修改
        resetPencils();
        tool.setPencilType(Tool.SELECTPENCIL);
        selectPencil.setStyle("-fx-background-color: greenyellow");
        drawBoard.setCursor(Cursor.DEFAULT);
        image.setDisable(true);
        pencil.setDisable(true);
        rectanglePencil.setDisable(true);
        linePencil.setDisable(true);
        circlePencil.setDisable(true);
        eraser.setDisable(true);
        textPencil.setDisable(true);
        rectPreferencePane.setVisible(false);
        ovelPreferencePane.setVisible(false);
        textPreferencePane.setVisible(false);
    }

    //以下是rectPreferencePane内的事件处理函数
    @FXML
    private void SetRectFill(ActionEvent event) {
        try{
            CheckBox checkBox = (CheckBox)event.getSource();
            if (checkBox.isSelected()) {
                rectFillColorPicker.setDisable(false);
            } else {
                rectFillColorPicker.setDisable(true);
            }
            toolKit.setRectFill(checkBox.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void SetRectFillColor(ActionEvent event) {
        try{
            ColorPicker colorPicker = (ColorPicker) event.getSource();
            toolKit.setRectFillColor(colorPicker.getValue());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();

        }

    }

    @FXML
    private void SetRectGradient(ActionEvent event) {
        try{
            CheckBox checkBox = (CheckBox)event.getSource();
            if (checkBox.isSelected()) {
                rectGradientColorPicker.setDisable(false);
            }
            else {
                rectGradientColorPicker.setDisable(true);
            }
            toolKit.setRectGradient(checkBox.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    private void SetRectGradientColor(ActionEvent event) {
        try{
            ColorPicker colorPicker = (ColorPicker) event.getSource();
            toolKit.setRectGradientColor(colorPicker.getValue());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    private void SetRectRound(ActionEvent event) {
        try{
            CheckBox checkBox = (CheckBox)event.getSource();
            if (checkBox.isSelected()) {
                rectArcRadiusComboBox.setDisable(false);
            } else {
                rectArcRadiusComboBox.setDisable(true);
            }
            toolKit.setRectRound(checkBox.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    private void SetRectArcRadius(ActionEvent event) {
        try{
            ComboBox<Double> comboBox=(ComboBox)event.getSource();
            toolKit.setRectArcRadius(comboBox.getValue());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    //以下是ovelPreferencePane内的事件处理函数
    @FXML
    private void SetOvelFill(ActionEvent event) {
        try{
            CheckBox checkBox = (CheckBox)event.getSource();
            if (checkBox.isSelected()) {
                ovelFillColorPicker.setDisable(false);
            } else {
                ovelFillColorPicker.setDisable(true);
            }
            toolKit.setOvelFill(checkBox.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void SetOvelFillColor(ActionEvent event) {
        try{
            ColorPicker colorPicker = (ColorPicker) event.getSource();
            toolKit.setOvelFillColor(colorPicker.getValue());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    private void SetOvelGradient(ActionEvent event) {
        try{
            CheckBox checkBox = (CheckBox)event.getSource();
            if (checkBox.isSelected()) {
                ovelGradientColorPicker.setDisable(false);
            }
            else {
                ovelGradientColorPicker.setDisable(true);
            }
            toolKit.setOvelGradient(checkBox.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    private void SetOvelGradientColor(ActionEvent event) {
        try{
            ColorPicker colorPicker = (ColorPicker) event.getSource();
            toolKit.setOvelGradientColor(colorPicker.getValue());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    private void SetOvelShadow(ActionEvent event) {
        try{
            CheckBox checkBox = (CheckBox)event.getSource();
            if (checkBox.isSelected()) {
                ovelShadowColorPicker.setDisable(false);
                ovelShadowWidthComboBox.setDisable(false);
            } else {
                ovelShadowColorPicker.setDisable(true);
                ovelShadowWidthComboBox.setDisable(true);
            }
            toolKit.setOvelShadow(checkBox.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    private void SetOvelShadowWidth(ActionEvent event) {
        try{
            ComboBox<Double> comboBox=(ComboBox)event.getSource();
            toolKit.setOvelShadowWidth(comboBox.getValue());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    private void SetOvelShadowColor(ActionEvent event) {
        try{
            ColorPicker colorPicker = (ColorPicker) event.getSource();
            toolKit.setOvelShadowColor(colorPicker.getValue());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    //以下是textPreferencePane的事件处理函数
    @FXML
    private void SetTextFont(ActionEvent event) {
        try{
            ComboBox<String> comboBox = (ComboBox<String>) event.getSource();
            toolKit.setTextFont(comboBox.getValue());
            if (selectedGraph instanceof Text) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void SetTextBold(ActionEvent event) {
        try{
            CheckBox checkBox = (CheckBox)event.getSource();
            toolKit.setTextBold(checkBox.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void SetTextItalic(ActionEvent event) {
        try{
            CheckBox checkBox = (CheckBox)event.getSource();
            toolKit.setTextItalic(checkBox.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void SetTextNoEffect(ActionEvent event) {
        try{
            resetTextEffect();
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void SetTextFuzzy(ActionEvent event) {
        try{
            resetTextEffect();
            RadioButton radioButton = (RadioButton) event.getSource();
            toolKit.setTextFuzzy(radioButton.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void SetTextReflect(ActionEvent event) {
        try{
            resetTextEffect();
            RadioButton radioButton = (RadioButton) event.getSource();
            toolKit.setTextReflect(radioButton.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void SetTextOutsideShadow(ActionEvent event) {
        try{
            resetTextEffect();
            RadioButton radioButton = (RadioButton) event.getSource();
            toolKit.setTextOutsideShadow(radioButton.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private void SetTextInsideShadow(ActionEvent event) {
        try{
            resetTextEffect();
            RadioButton radioButton = (RadioButton) event.getSource();
            toolKit.setTextInsideShadow(radioButton.isSelected());
            if (selectedGraph != null) {
                clearArea(draft);
                selectedGraph.draw(draft_gc);
                selectedGraph.select(draft_gc);
            }

            //保存备忘录
            save();
            undoButton.setDisable(!careTaker.undoable());
            redoButton.setDisable(!careTaker.redoable());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void resetTextEffect() {
        toolKit.setTextFuzzy(false);
        toolKit.setTextReflect(false);
        toolKit.setTextOutsideShadow(false);
        toolKit.setTextInsideShadow(false);
    }

    //以下是显示信息的事件相应
    @FXML
    private void ClearMessage(MouseEvent event) {
        message.setText("");
    }

    @FXML
    private void EraserMessage(MouseEvent event) {
        message.setText("橡皮只能擦除自由画笔画出的线");
    }

    @FXML
    private void MoveUpMessage(MouseEvent event) {
        message.setText("在本图层内将当前选中元素移到最上层");
    }

    @FXML
    private void MoveDownMessage(MouseEvent event) {
        message.setText("在本图层内将当前选中元素移到最下层");
    }

    @FXML
    private void DeleteLayerMessage(MouseEvent event) {
        message.setText("删除当前选中图层");
    }

    //备忘录模式方法
    private void save(){
        Memento memento = new Memento();
        memento.setTool(tool);
        memento.setToolKit(toolKit);
        memento.setGlobalToolKit(globalToolKit);
        memento.setSelectedGraph(selectedGraph);
        memento.setGraphics(graphics);
        memento.setLayers(layers);
        memento.setLayerIndex(layerList.getSelectionModel().getSelectedIndex());
        careTaker.addMemento(memento);
        //有新的操作没有保存到文件
        saved = false;
        if (stage != null) {
            stage.setTitle("DrawBoard - [" + savePath + "]"  + " - [未保存]");
        }

    }

    @FXML
    private void ShowAuthor(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.titleProperty().set("关于作者");
        alert.headerTextProperty().set("作者：张无奇 Aaron Zhang");
        alert.contentTextProperty().set("邮箱：troublor@live.com\nGithub: github.com/Troublor\n个人网站: www.aaromine.me");
        alert.showAndWait();
    }

    @FXML
    private void CutSelectedGraph(ActionEvent event) {
        if (selectedGraph != null) {
            CopySelectedGraph(event);
            graphics.remove(selectedGraph);
            clearArea(drawBoard);
            clearArea(draft);
            for (Graph graph : graphics) {
                graph.draw(gc);
            }
            switchToDrawBoard();
            message.setText("已剪切");
        }

    }

    @FXML
    private void CopySelectedGraph(ActionEvent event) {
        if (selectedGraph != null) {
            copiedGraph = selectedGraph.clone();
            message.setText("已复制");
        }
    }

    @FXML
    private void PasteGraph(ActionEvent event) {
        if (copiedGraph != null) {
            resetPencils();
            //将新粘贴进来的图形放到最左上角
            copiedGraph.move(1, 1, 10, 10);

            //切换到选择模式
            selectPencil.setStyle("-fx-background-color: greenyellow");
            tool.setPencilType(Tool.SELECTPENCIL);
            toolKit = globalToolKit;
            showToolKit(toolKit);
            drawBoard.setCursor(Cursor.DEFAULT);

            //关掉所有图形偏好设置
            rectPreferencePane.setVisible(false);
            ovelPreferencePane.setVisible(false);
            moveDownButton.setDisable(true);
            moveUpButton.setDisable(true);

            clearArea(drawBoard);
            clearArea(draft);
            copiedGraph.draw(draft_gc);
            copiedGraph.select(draft_gc);
            selectedGraph = copiedGraph;
            switchToDraft();
            toolKit = copiedGraph.getToolKit();
            showToolKit(toolKit);

            moveDownButton.setDisable(false);
            moveUpButton.setDisable(false);

            //如果是文本框
            if (copiedGraph instanceof Text) {
                Text t = (Text) copiedGraph;
                textField.setText(t.getText());
                inputPane.setVisible(true);
                t.select(draft_gc);
            }
            if (copiedGraph instanceof Rect) {
                rectPreferencePane.setVisible(true);
            }
            if (copiedGraph instanceof Ovel) {
                ovelPreferencePane.setVisible(true);
            }
            if (copiedGraph instanceof Text) {
                textPreferencePane.setVisible(true);
            }


            for (Graph graph : graphics) {
                graph.draw(gc);
            }
            switchToDraft();
            graphics.add(copiedGraph);
            //如果要重复粘贴的话，复制一份
            copiedGraph = copiedGraph.clone();
            message.setText("已粘贴");
        }
    }

    /**
     * 从controller传递stage进来
     * @param s stage
     */
    public void setStage(Stage s) {
        stage = s;
    }

    /**
     * 是否已保存
     * @return saved
     */
    public boolean isSaved() {
        return saved;
    }
}

