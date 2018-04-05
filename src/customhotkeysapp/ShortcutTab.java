package customhotkeysapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.MotionBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author vonabe
 */
public class ShortcutTab {

    private LinkedHashMap<Integer, String> hash_combinations = new LinkedHashMap<>();
    private LinkedHashMap<Integer, String> hash_description = new LinkedHashMap<>();
    private LinkedHashMap<Integer, String> hash_images_name = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Image>  hash_images = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Preview>  hash_images_prev = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Integer>  hash_button_height = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Integer>  hash_button_collumns = new LinkedHashMap<>();

    private HashMap<Integer, Integer>      hash_hotkeys_id = new HashMap<>();
    private HashMap<Integer, Integer>      hash_tabs_id = new HashMap<>();

    private ListView<HBox> list = new ListView<>();
    private AnchorPane anch = null;
    private BorderPane border = null, borderTwo = null;
    private FlowPane flow = null;
    private ImageView img0 = null, img1 = null, img2 = null;
    private TextArea areaDescription = null;
    private Slider sliderHeight = null, sliderColumns, sliderBtnColumns = null, sliderMargin = null;
    private Text txtSliderValue = null, txtSliderColumns = null, txtSliderBtnColumns = null, txtSliderMargin = null;

    private String tab_name = "unknown", color = "#ffffff";
    private Tab tab = null;
    final private int tab_id, order;
    private int selectHashItem = -1, select = -1, columns = 5, height = 100, margin = 0;
    private boolean remove = false, visible = true, closeTab = false;

    public ShortcutTab(final Tab tab, final int tab_id, final int order, final JSONObject object) {
        this.tab = tab;
        this.order = order;
        this.tab_id = tab_id;
        this.hash_tabs_id.put(this.tab_id, this.tab_id);
        this.tab.setText(this.tab_name);

        // Обработка закрытия и удаления таба.
        this.tab.setOnCloseRequest((Event event) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            Stage window = (Stage) alert.getDialogPane().getScene().getWindow();
            window.getIcons().add(new Image("res/ic_launcher.png"));
            alert.setTitle("Диалоговое окно");
            alert.setHeaderText("Подтверждение закрытия");
            alert.setContentText("Вы уверены, что хотите закрыть окно и удалить все хоткеи?");
            Optional<ButtonType> show = alert.showAndWait();
            if (show.get() == ButtonType.OK) {
                this.closeTab();
            } else {
                event.consume();
            }
        });

        // Конструкция смены имени таба.
        final TextField field_rename = new TextField();
        field_rename.setPromptText("rename shortcut");
        field_rename.setOnAction((ActionEvent event) -> {
            String name = field_rename.getText();
            if (!name.replace(" ", "").isEmpty()) {
                this.renameTab(name);
            } else {
                field_rename.setText("");
            }
        });
        final MenuItem item_rename_tab = new MenuItem("", field_rename);
        final ContextMenu context_menu_tab = new ContextMenu(item_rename_tab);
        this.tab.setContextMenu(context_menu_tab);

        this.areaDescription = new TextArea();
        this.areaDescription.prefHeightProperty().set(45);
        this.areaDescription.setWrapText(true);
        this.areaDescription.setPromptText("description hotkey ...");
        this.areaDescription.focusedProperty().addListener((ObservableValue<? extends Boolean> observable1, Boolean oldValue, Boolean newValue) -> {
            this.focusArea(newValue);
        });

        // инициализация с загрузкой ресурсов.
        this.list = new ListView<>();

        MenuItem item_new_hotkey = new MenuItem("add hotkey");
        item_new_hotkey.setOnAction((ActionEvent event) -> {
            this.addHotkey();
        });

        MenuItem item_remove_hotkey = new MenuItem("remove hotkey");
        item_remove_hotkey.setOnAction((ActionEvent event) -> {
            this.removeHotkey();
        });


        ContextMenu context_menu_listview = new ContextMenu(item_new_hotkey, item_remove_hotkey);

//        final KeyCodeCombination keyCodeCombinationInser = new KeyCodeCombination(KeyCode.INSERT);
//        final KeyCodeCombination keyCodeCombinationDelete = new KeyCodeCombination(KeyCode.DELETE);
//        item_remove_hotkey.setAccelerator(keyCodeCombinationDelete);
//        item_new_hotkey.setAccelerator(keyCodeCombinationInser);

        this.list.setContextMenu(context_menu_listview);

        this.list.focusedProperty().addListener((ObservableValue<? extends Boolean> observable1, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
            }else{
            }
            this.focusList(newValue);
        });

        this.list.getSelectionModel().getSelectedIndices().addListener((ListChangeListener.Change<? extends Integer> c) -> {
            this.selectList(this.list.getSelectionModel().getSelectedIndex());
        });

        BorderPane.setAlignment(this.list, Pos.CENTER);
        this.img0 = new ImageView();
        this.img0.setPreserveRatio(true);
        this.img0.setSmooth(true);
        this.img0.fitWidthProperty().set(36.0d);
        this.img0.fitHeightProperty().set(36.0d);

        this.img1 = new ImageView();
        this.img1.setPreserveRatio(true);
        this.img1.setSmooth(true);
        this.img1.fitWidthProperty().set(48.0d);
        this.img1.fitHeightProperty().set(48.0d);

        this.img2 = new ImageView();
        this.img2.setPreserveRatio(true);
        this.img2.setSmooth(true);
        this.img2.fitWidthProperty().set(96.0d);
        this.img2.fitHeightProperty().set(96.0d);

        this.flow = new FlowPane(Orientation.HORIZONTAL, 15.0d, 15.0d, this.img0, this.img1, this.img2);
        this.flow.setAlignment(Pos.CENTER);
        this.flow.setPadding(new Insets(10.0d));
        this.flow.setRowValignment(VPos.BOTTOM);

        BorderStroke borderStrokeBlack = new BorderStroke(Color.BLACK, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT);
        BorderStroke borderStrokeRed = new BorderStroke(Color.RED, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT);
        BorderStroke borderStrokeGreen = new BorderStroke(Color.GREEN, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT);
        Border bBlack = new Border(borderStrokeBlack);
        Border bGreen = new Border(borderStrokeGreen);
        Border bRed = new Border(borderStrokeRed);
        this.flow.setBorder(bBlack);

        BorderPane.setMargin(this.flow, new Insets(5.0d, 5.0d, 5.0d, 5.0d));
        BorderPane.setAlignment(this.flow, Pos.CENTER);

        this.flow.setOnDragOver((DragEvent event) -> {
            Dragboard dragboard = event.getDragboard();
            if (event.getGestureSource() != this.flow && dragboard.hasUrl()) {
                String url = dragboard.getUrl();
                if (url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".JPG") || url.endsWith(".JPEG") || url.endsWith(".jpeg") || url.endsWith(".gif")){
                    this.flow.setBorder(bGreen);
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }else{
                    this.flow.setBorder(bRed);
                }
            }
            event.consume();
        });

        this.flow.setOnDragExited((DragEvent event) -> {
            this.flow.setBorder(bBlack);
            event.consume();
        });

        this.flow.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean dragdrop = false;
            if (db.hasUrl()) {
                String url = db.getUrl();
                Image img = new Image(url, false);
                    boolean error = img.isError();
                    if (!error) {
                        String format = url.substring(url.lastIndexOf(".")+1, url.length());
                        boolean save = SynchronizationProfile.getInstance().saveImage(img, "image_" + this.hash_hotkeys_id.get(selectHashItem), format);
                        if (save) {
                            Image image = SynchronizationProfile.getInstance()
                                    .getImage("image_" + this.hash_hotkeys_id.get(selectHashItem), new StringBuilder());
                            if (image != null) {
                                this.hash_images_name.replace(this.selectHashItem, "image_" + this.hash_hotkeys_id.get(selectHashItem));
                                this.hash_images.replace(this.selectHashItem, image);
                                this.hash_images_prev.get(this.selectHashItem).build(image);

                                this.setImg(image);
                                dragdrop = true;
                                SynchronizationProfile.getInstance().saveTab(this.saveInfo());

                                JSONObject objData = new JSONObject();
                                    objData.put("tab_id", this.tab_id);
                                    objData.put("hotkey_id", this.selectHashItem);
                                    objData.put("img", this.hash_images_name.get(this.selectHashItem));
                                JSONObject obj = new JSONObject();
                                    obj.put("img_hotkey", objData);
                                FXMLDocumentController.send(obj.toJSONString());
                            }
                        } else {
                            System.out.println("не удалось сохранить изображение");
                        }
                    }
            }
            event.setDropCompleted(dragdrop);
            event.consume();
        });

        final Label lblHotkeys = new Label("Hotkeys");
        lblHotkeys.setEffect(new MotionBlur(0.0d, 4.14d));
        lblHotkeys.setTextFill(Color.web("#111111"));

        this.txtSliderBtnColumns = new Text("columns button 1");
        this.sliderBtnColumns = new Slider(1.0d, 15.0D, 1.0D);
        this.sliderBtnColumns.setMajorTickUnit(1D);
        this.sliderBtnColumns.setMinorTickCount(1);
        this.sliderBtnColumns.setSnapToPixel(true);
        this.sliderBtnColumns.setSnapToTicks(true);
        this.sliderBtnColumns.setBlockIncrement(1.0D);
        this.sliderBtnColumns.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            int value = newValue.intValue();
            this.txtSliderBtnColumns.setText("columns button "+value);

//            this.sliderBtnColumns.setValue(this.hash_button_collumns.get(this.selectHashItem));

            if(this.selectHashItem!=-1&&this.hash_button_collumns.get(this.selectHashItem)!=value){
                this.hash_button_collumns.replace(this.selectHashItem, value);
                JSONObject objData = new JSONObject();
                    objData.put("tab_id", this.tab_id);
                    objData.put("hotkey_id", this.selectHashItem);
                    objData.put("value", this.hash_button_collumns.get(this.selectHashItem));
                JSONObject o = new JSONObject();
                    o.put("btnColumns", objData);
                FXMLDocumentController.send(o.toJSONString());
            }
        });

        this.txtSliderColumns = new Text("max columns "+this.columns);
        this.sliderColumns = new Slider(1.0d, 15.0D, 1.0D);
        this.sliderColumns.setMajorTickUnit(1D);
        this.sliderColumns.setMinorTickCount(1);
        this.sliderColumns.setSnapToPixel(true);
        this.sliderColumns.setSnapToTicks(true);
        this.sliderColumns.setBlockIncrement(1.0D);
        this.sliderColumns.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            int value = newValue.intValue();
            this.columns = value;
            this.txtSliderColumns.setText("max columns "+value);
            this.sliderBtnColumns.setMax(value);

            Iterator<Integer> iterator = this.hash_button_collumns.keySet().iterator();
            while(iterator.hasNext()){
                Integer next = iterator.next();
                Integer c = this.hash_button_collumns.get(next);
                if(c>value){
                    this.hash_button_collumns.replace(next, value);
                }
            }

            JSONObject objData = new JSONObject();
                objData.put("tab_id", this.tab_id);
                objData.put("value", this.columns);
            JSONObject o = new JSONObject();
                o.put("columns", objData);
            FXMLDocumentController.send(o.toJSONString());
        });
        this.sliderColumns.setValue(this.columns);

        this.txtSliderMargin = new Text("margin "+this.margin);
        this.sliderMargin = new Slider(0D, 50D, this.margin);
        this.sliderMargin.setMajorTickUnit(1D);
        this.sliderMargin.setMinorTickCount(1);
        this.sliderMargin.setSnapToPixel(true);
        this.sliderMargin.setSnapToTicks(true);
        this.sliderMargin.setBlockIncrement(1.0D);
        this.sliderMargin.valueProperty().addListener(new ChangeListener<Number>() {
        	@Override
        	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        		margin = newValue.intValue();
        		txtSliderMargin.setText("margin "+margin);

                JSONObject objData = new JSONObject();
	                objData.put("tab_id", ShortcutTab.this.tab_id);
	                objData.put("margin", ShortcutTab.this.margin);
	            JSONObject o = new JSONObject();
	                o.put("replace_margin", objData);
	            FXMLDocumentController.send(o.toJSONString());
        	}
		});
        this.sliderMargin.setValue(this.margin);

        Button btnAllHeight = new Button("All Height");
        btnAllHeight.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				height = (int)sliderHeight.getValue();
				Iterator<Integer> it = hash_button_height.keySet().iterator();
				while(it.hasNext()){
					Integer id = it.next();
					hash_button_height.replace(id, height);
				     JSONObject objData = new JSONObject();
	                    objData.put("tab_id", tab_id);
	                    objData.put("hotkey_id", id);
	                    objData.put("value", height);
	                JSONObject o = new JSONObject();
	                    o.put("btnHeight", objData);
	                FXMLDocumentController.send(o.toJSONString());
				}
			}
		});
        btnAllHeight.widthProperty().add(Double.MAX_VALUE);

        this.txtSliderValue = new Text("height button "+this.height);
        this.sliderHeight = new Slider();
        this.sliderHeight.setMin(10);this.sliderHeight.setMax(1000);
        this.sliderHeight.setMajorTickUnit(1D);
        this.sliderHeight.setMinorTickCount(1);
        this.sliderHeight.setSnapToPixel(true);
        this.sliderHeight.setSnapToTicks(true);
        this.sliderHeight.setBlockIncrement(1.0D);
        this.sliderHeight.widthProperty().add(Double.MAX_VALUE);
        this.sliderHeight.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            int value = (int) this.sliderHeight.getValue();
            this.txtSliderValue.setText("height button "+value);
            if(this.selectHashItem!=-1){
                this.hash_button_height.replace(this.selectHashItem, value);
                JSONObject objData = new JSONObject();
                    objData.put("tab_id", this.tab_id);
                    objData.put("hotkey_id", this.selectHashItem);
                    objData.put("value", this.hash_button_height.get(this.selectHashItem));
                JSONObject o = new JSONObject();
                    o.put("btnHeight", objData);
                FXMLDocumentController.send(o.toJSONString());
            }
        });

        HBox hboxHeight = new HBox();
        hboxHeight.setAlignment(Pos.CENTER);
        HBox.setHgrow(this.sliderHeight, Priority.ALWAYS);
        hboxHeight.getChildren().addAll(this.sliderHeight,btnAllHeight);

        Color web = null;
        try{
            web = Color.valueOf(this.color);
        }catch(IllegalArgumentException ex){System.out.println("error parse color "+this.color);web = Color.WHITE;}

        Text txtPicker = new Text("background color");
        ColorPicker picker = new ColorPicker(web);
        picker.setMaxWidth(Double.MAX_VALUE);
        picker.setOnAction((ActionEvent event) -> {
            String color = picker.getValue().toString();
            color = color.substring(0,color.length()-2).replaceAll("0x", "#");
            this.color = color;

            JSONObject objData = new JSONObject();
	        objData.put("tab_id", this.tab_id);
                objData.put("color", color);
            JSONObject obj = new JSONObject();
                    obj.put("background_color", objData);
            FXMLDocumentController.send(obj.toJSONString());
            SynchronizationProfile.getInstance().saveTab(this.saveInfo());
        });

        Label lblSeparator1 = new Label("Tab Setting");
        lblSeparator1.setAlignment(Pos.CENTER_LEFT);
        Separator separator1 = new Separator();

        Label lblSeparator0 = new Label("Hotkey Setting");
        lblSeparator0.setAlignment(Pos.CENTER_LEFT);
        Separator separator0 = new Separator();

        VBox boxedit = new VBox(this.areaDescription,
                lblSeparator1, separator1,
                this.txtSliderColumns, this.sliderColumns,
                this.txtSliderMargin, this.sliderMargin,
                txtPicker, picker,
                lblSeparator0, separator0,
                this.txtSliderValue,hboxHeight,
                this.txtSliderBtnColumns,this.sliderBtnColumns);
        boxedit.setAlignment(Pos.CENTER);

        this.borderTwo = new BorderPane(this.flow, boxedit, null, null, null);
        this.borderTwo.setPadding(new Insets(0, 0, 0, 10));

        this.border = new BorderPane(this.borderTwo, lblHotkeys, null, null, this.list);
        this.border.setPadding(new Insets(10.0d));
        AnchorPane.setTopAnchor(this.border, 0.0d);
        AnchorPane.setBottomAnchor(this.border, 0.0d);
        AnchorPane.setLeftAnchor(this.border, 0.0d);
        AnchorPane.setRightAnchor(this.border, 0.0d);

        this.anch = new AnchorPane(this.border);
        this.tab.setContent(this.anch);

        if (this.list.getSelectionModel().getSelectedIndex() < 0) {
            this.visibleImg(false);
        }

        if (object != null) {
            this.hash_tabs_id.replace(this.tab_id, Integer.parseInt(object.get("tab_id").toString()));
            this.tab_name = object.get("name_tab").toString();
            this.tab.setText(this.tab_name);

            String margin = "0";
            if(object.containsKey("margin")){
            	margin = object.get("margin").toString();
            }

            String column = "5";
            if(object.containsKey("columns")){
                column = object.get("columns").toString();
            }

            if(object.containsKey("background_color")){
                this.color = object.get("background_color").toString();
                Color web1 = null;
                try{
                    web1 = Color.valueOf(this.color);
                }catch(IllegalArgumentException ex){System.out.println("error parse color "+this.color);web1 = Color.WHITE;}
                picker.setValue(web1);
            }

            this.columns = Integer.parseInt(column);
            this.sliderColumns.setValue(this.columns);

            this.margin = Integer.parseInt(margin);
            this.sliderMargin.setValue(this.margin);

            JSONArray a = (JSONArray) object.get("hotkeys");
            Iterator<?> iterator = a.iterator();
            while (iterator.hasNext()) {
                JSONObject hotkey = (JSONObject) iterator.next();
                String img = hotkey.get("img").toString();
                Object description = hotkey.get("description");
                String combination = hotkey.get("combination").toString();
                String hotkey_id = hotkey.get("hotkey_id").toString();

                String btnheight = ""+height;
                if(hotkey.containsKey("btnHeight")){
                    btnheight = hotkey.get("btnHeight").toString();
                }

                String btnColumns = "1";
                if(hotkey.containsKey("btnColumns")){
                    btnColumns = hotkey.get("btnColumns").toString();
                }

                String descriptiontxt = (description == null) ? "" : description.toString();

                int h_id = Integer.parseInt(hotkey_id);
                int btn_height = Integer.parseInt(btnheight);
                int btn_columns = Integer.parseInt(btnColumns);


                this.addHotkey(img, descriptiontxt, combination, h_id, btn_height, btn_columns);
            }
        }

        JSONObject objData = new JSONObject();
        objData.put("name_tab", this.tab_name);
        objData.put("tab_id", this.tab_id);
        objData.put("visible", this.visible);
            objData.put("columns", this.columns);
            objData.put("margin", this.margin);
            objData.put("background_color", this.color);
	    JSONObject obj = new JSONObject();
	    	obj.put("newtab", objData);
	    FXMLDocumentController.send(obj.toJSONString());

    }

    public void visibleImg(boolean visible) {
        this.img0.setVisible(visible);
        this.img1.setVisible(visible);
        this.img2.setVisible(visible);
        this.borderTwo.setVisible(visible);
    }

    public void focusArea(boolean focus) {
        if (focus) {

        } else {
        	if(!this.hash_description.get(this.selectHashItem).equals(this.areaDescription.getText())){

        		this.hash_description.replace(this.selectHashItem, this.areaDescription.getText());
	            SynchronizationProfile.getInstance().saveTab(this.saveInfo());

	            JSONObject objData = new JSONObject();
		            objData.put("tab_id", this.tab_id);
		            objData.put("hotkey_id", this.selectHashItem);
		            objData.put("description", this.hash_description.get(this.selectHashItem));
	            JSONObject objSend = new JSONObject();
	            	objSend.put("replace_description", objData);
	            FXMLDocumentController.send(objSend.toJSONString());
        	}
        }
    }

    public void selectList(final int select) {
        if (!this.remove && this.select > -1) {
            HBox get = this.list.getItems().get(this.select);
            String replace = this.hash_description.replace(
                    get.hashCode(),
                    this.areaDescription.getText()
            );
        }
        this.remove = false;

        this.select = select;
        if (select > -1) {
            this.selectHashItem = this.list.getItems().get(select).hashCode();
        }
        if (this.select > -1 && this.selectHashItem != -1) {
            if(this.hash_images.containsKey(this.selectHashItem) &&
                    this.hash_description.containsKey(this.selectHashItem)
                        && this.hash_button_height.containsKey(this.selectHashItem)
                            && this.hash_button_collumns.containsKey(this.selectHashItem)){

                Image img = this.hash_images.get(this.selectHashItem);
                this.areaDescription.setText(this.hash_description.get(this.selectHashItem));
//                this.areaDescription.setText(this.selectHashItem+"");
                this.sliderHeight.setValue(this.hash_button_height.get(this.selectHashItem));
                this.sliderBtnColumns.setValue(this.hash_button_collumns.get(this.selectHashItem));

                this.setImg(img);
                this.visibleImg(true);

                JSONObject objData = new JSONObject();
                    objData.put("tab_id", this.tab_id);
                    objData.put("hotkey_id", this.selectHashItem);
                JSONObject object = new JSONObject();
                    object.put("select_button", objData);
                FXMLDocumentController.send(object.toJSONString());
            }else{
                System.out.println("don't contains > "
                        +this.hash_images.containsKey(this.selectHashItem)+","
                        +this.hash_description.containsKey(this.selectHashItem)+","
                        +this.hash_button_height.containsKey(this.selectHashItem)+","
                        +this.hash_button_collumns.containsKey(this.selectHashItem)
                );
            }

        } else {
            this.visibleImg(false);
        }

    }

    public void focusList(final boolean focus) {
        if (focus) {
            if (this.select > -1) {
                JSONObject objData = new JSONObject();
	                objData.put("tab_id", this.tab_id);
	                objData.put("hotkey_id", this.selectHashItem);
                JSONObject object = new JSONObject();
                	object.put("select_button", objData);
                FXMLDocumentController.send(object.toJSONString());
                SynchronizationProfile.getInstance().saveTab(this.saveInfo());
            }
        } else {
            JSONObject object = new JSONObject();
            	object.put("focus", null);
            FXMLDocumentController.send(object.toJSONString());
            if(!this.closeTab)SynchronizationProfile.getInstance().saveTab(this.saveInfo());
        }
    }

    public void addHotkey(String img, String description, String combination, int h_id, int btn_height, int btn_collumns) {

        ArrayList<String> keysPressed = new ArrayList<>();
        ArrayList<String> keysReleased = new ArrayList<>();

        final TextField fieldShortcut = new TextField(combination);
        fieldShortcut.setOnKeyPressed((KeyEvent event1) -> {
            KeyCode code = event1.getCode();
            if (!keysPressed.contains(code.getName())) {
                keysPressed.add(code.getName());
            }
            fieldShortcut.setText(keysPressed.toString());
            event1.consume();
        });

        fieldShortcut.setOnKeyReleased((KeyEvent event1) -> {

            KeyCode code = event1.getCode();
            fieldShortcut.setText(keysPressed.toString());
            keysReleased.add(code.getName());

            if (keysReleased.size() == keysPressed.size()) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < keysPressed.size(); i++) {
                    String key = keysPressed.get(i);
                    builder.append(key);
                    if (i < keysPressed.size() - 1) {
                        builder.append("+");
                    }
                }

                try {
                    KeyCombination combin = KeyCombination.keyCombination(builder.toString());
                } catch (IllegalArgumentException ex) {
                    System.out.println("error not combinet key");
                    builder.delete(0, builder.length());
                }
//                if(this.select>-1){
                    if(this.hash_combinations.containsKey(this.selectHashItem))
                        this.hash_combinations.replace(this.selectHashItem, builder.toString());
                    else
                        this.hash_combinations.put(this.selectHashItem, builder.toString());
//                }else{
//                    System.out.println("selec<-1 combinet not save");
//                }
                fieldShortcut.setText(builder.toString());

                keysPressed.clear();
                keysReleased.clear();
            }
            if (keysReleased.size() >= 5) {
                keysPressed.clear();
                keysReleased.clear();
                fieldShortcut.setText(keysPressed.toString());
            }
        });
        fieldShortcut.setEditable(false);

        ImageView imgPrev = new ImageView();
        imgPrev.fitWidthProperty().set(24);
        imgPrev.fitHeightProperty().set(24);
        imgPrev.setSmooth(true);
        imgPrev.setPreserveRatio(true);

        HBox hbox = new HBox(imgPrev, fieldShortcut);
        hbox.setPadding(new Insets(2.0d));
        hbox.setSpacing(10.0d);
        this.list.getItems().add(hbox);

        fieldShortcut.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if(newValue)this.list.getSelectionModel().select(hbox);
        });

        this.selectHashItem = hbox.hashCode();
        this.hash_hotkeys_id.put(this.selectHashItem, (h_id == -1) ? this.selectHashItem : h_id);

        StringBuilder format = new StringBuilder();
        Image image = SynchronizationProfile.getInstance().getImage(img, format);
        if (image == null || image.isError()) {
            image = new Image("res/drag_and_drop_ru.png", false);
            format.append("png");
            SynchronizationProfile.getInstance().saveImage(image,
                    "image_" + this.hash_hotkeys_id.get(this.selectHashItem), format.toString());
        }
        this.hash_images_prev.put(this.selectHashItem, new Preview(imgPrev,image,this.selectHashItem));
        imgPrev.setImage(image);
//        SynchronizationProfile.getInstance().renameImg(img, "image_" + this.selectHashItem);
//        SynchronizationProfile.getInstance().deleteImg(img);
//        SynchronizationProfile.getInstance().saveImage(image, "image_" + this.selectHashItem, format.toString());

        this.hash_images.put(this.selectHashItem, image);
        this.hash_images_name.put(this.selectHashItem, "image_" + this.hash_hotkeys_id.get(this.selectHashItem));
        this.hash_combinations.put(this.selectHashItem, combination);
        this.hash_description.put(this.selectHashItem, description);
        this.hash_button_height.put(this.selectHashItem, btn_height);
        this.hash_button_collumns.put(this.selectHashItem, btn_collumns);

        JSONObject objData = new JSONObject();
            objData.put("tab_id", this.tab_id);
            objData.put("hotkey_id", this.selectHashItem);
            objData.put("img", this.hash_images_name.get(this.selectHashItem));
            objData.put("description", this.hash_description.get(this.selectHashItem));
            objData.put("btnHeight", this.hash_button_height.get(this.selectHashItem));
            objData.put("btnColumns",  this.hash_button_collumns.get(this.selectHashItem));
        JSONObject object = new JSONObject();
            object.put("newhotkey", objData);
        FXMLDocumentController.send(object.toJSONString());

        SynchronizationProfile.getInstance().saveTab(this.saveInfo());

        this.list.getSelectionModel().select(hbox);

    }

    public JSONObject saveInfo() {
    	JSONObject objData = new JSONObject();
            objData.put("name_tab", this.tab_name);
            objData.put("tab_id", this.hash_tabs_id.get(this.tab_id));
            objData.put("order", this.order);
            objData.put("visible", this.visible);
            objData.put("columns", this.columns);
            objData.put("margin", this.margin);
            objData.put("background_color", this.color);

       JSONArray aData = new JSONArray();
       for (int i = 0; i < this.list.getItems().size(); i++) {
    	   JSONObject hData = new JSONObject();
    	   int hashcode = this.list.getItems().get(i).hashCode();
                hData.put("hotkey_id", 	 this.hash_hotkeys_id.get(hashcode));
                hData.put("img", 		 this.hash_images_name.get(hashcode));
                hData.put("description", this.hash_description.get(hashcode));
                hData.put("combination", this.hash_combinations.get(hashcode));
                hData.put("btnHeight", this.hash_button_height.get(hashcode));
                hData.put("btnColumns",  this.hash_button_collumns.get(hashcode));
           aData.add(hData);
       }
       objData.put("hotkeys", aData);

       JSONObject object = new JSONObject();
       	   object.put("info_tab", objData);
       return object;
    }

    public JSONObject getInfo() {
        JSONObject objData = new JSONObject();
	        objData.put("name_tab", this.tab_name);
	        objData.put("tab_id", this.tab_id);
	        objData.put("order", this.order);
	        objData.put("visible", this.visible);
                objData.put("columns", this.columns);
                objData.put("margin", this.margin);
                objData.put("background_color", this.color);

        JSONArray aData = new JSONArray();
        for (int i = 0; i < this.list.getItems().size(); i++) {
            JSONObject hData = new JSONObject();
            int hashcode = this.list.getItems().get(i).hashCode();
	            hData.put("hotkey_id", 	 hashcode);
	            hData.put("img", 		 this.hash_images_name.get(hashcode));
	            hData.put("description", this.hash_description.get(hashcode));
	            hData.put("combination", this.hash_combinations.get(hashcode));
                    hData.put("btnHeight", this.hash_button_height.get(hashcode));
                    hData.put("btnColumns",  this.hash_button_collumns.get(hashcode));
	        aData.add(hData);
        }
        objData.put("hotkeys", aData);

        JSONObject object = new JSONObject();
            object.put("info_tab", objData);
        return object;
    }

    public void removeHotkey() {
        if(this.select!=-1){
//            System.out.println("delete "+this.select
//                    +",equals> "+this.list.getItems().get(this.select).hashCode()+", "+this.selectHashItem);

            JSONObject objData = new JSONObject();
                objData.put("tab_id", this.tab_id);
                objData.put("hotkey_id", this.selectHashItem);
            JSONObject object = new JSONObject();
                object.put("remove_hotkey", objData);
            FXMLDocumentController.send(object.toJSONString());

            this.remove = true;
            Image img = this.hash_images.get(this.selectHashItem);
            if(img!=null)img.cancel();else System.out.println("img null remove hotkey");

            this.hash_button_height.remove(this.selectHashItem);
            this.hash_button_collumns.remove(this.selectHashItem);
            this.hash_images.remove(this.selectHashItem);
            this.hash_description.remove(this.selectHashItem);
            this.hash_combinations.remove(this.selectHashItem);
            this.hash_hotkeys_id.remove(this.selectHashItem);
            this.hash_tabs_id.remove(this.selectHashItem);

            String get_img_name = this.hash_images_name.get(this.selectHashItem);
            this.hash_images_name.remove(this.selectHashItem);

            this.list.getItems().remove(this.select);
//            this.list.getSelectionModel().clearSelection();

            SynchronizationProfile.getInstance().saveTab(this.saveInfo());
            boolean deleteImg = SynchronizationProfile.getInstance().deleteImg(get_img_name);
        }
    }

    public void addHotkey() {
        this.addHotkey("res/drag_and_drop_en.png", "", "", -1, height, 1);
    }

    public void renameTab(String name) {
        this.tab_name = name;
        this.tab.setText(this.tab_name);
        JSONObject objData = new JSONObject();
            objData.put("name_tab", this.tab_name);
            objData.put("tab_id", this.tab_id);
        JSONObject object = new JSONObject();
            object.put("rename_tab", objData);
        FXMLDocumentController.send(object.toJSONString());

        SynchronizationProfile.getInstance().saveTab(this.saveInfo());
    }

    public void visible(boolean vis) {
        this.visible = vis;
    }

    public void closeTab() {
        JSONObject objData = new JSONObject();
        	objData.put("tab_id", this.tab_id);
        JSONObject object = new JSONObject();
        	object.put("remove_tab", objData);
        FXMLDocumentController.send(object.toJSONString());

        FXMLDocumentController.remove(this.tab_id, this.tab, this);
        SynchronizationProfile.getInstance().removeTab("" + this.hash_tabs_id.get(this.tab_id));

        Iterator<Integer> iterator = this.hash_images_name.keySet().iterator();
        while(iterator.hasNext()){
            Integer next = iterator.next();
            String name_img = this.hash_images_name.get(next);
            SynchronizationProfile.getInstance().deleteImg(name_img);
        }

        this.hash_combinations.clear();
        this.hash_description.clear();
        this.hash_hotkeys_id.clear();
        this.hash_images.clear();
        this.hash_images_name.clear();
        this.hash_tabs_id.clear();
        this.hash_images_prev.clear();
        this.hash_button_height.clear();
        this.closeTab = true;
    }

    public void drag(final String hotkey_id, final int from, final int to){
        Platform.runLater(() -> {
            list.getSelectionModel().select(-1);
            ObservableList<HBox> items = this.list.getItems();
            items.set(from, items.set(to, items.get(from)));
//            this.list.getItems().sorted().set(to, this.list.getItems().get(from));
//            this.list.editingIndexProperty().
//            System.out.println(this.list.getItems().get(from).hashCode()+", "+hotkey_id);
//            HBox set = this.list.getItems().set(to, this.list.getItems().get(from));
//            this.list.getItems().set(to, set);
        });
    }

    public void setImg(Image img) {
        this.img0.setImage(img);
        this.img1.setImage(img);
        this.img2.setImage(img);
    }

    public int getTabId() {
        return this.tab_id;
    }

    public String getCombination(int hotkey_id){
        return this.hash_combinations.get(hotkey_id);
    }

    private class Preview {
    	ImageView view = null;
    	Image img = null;
    	int hash = -1;

    	public Preview(ImageView view, Image img, int hash) {
            this.view = view;
            this.img = img;
            this.hash = hash;
        }

    	void build(Image img){
            this.img = img;
            this.view.setImage(this.img);
    	}
    }


}
