package customhotkeysapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import customhotkeysapp.server.CustomHotkeysServer;
import customhotkeysapp.server.ServerVerification;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.stage.Stage;

/**
 *
 * @author vonabe
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private TabPane tab_pane;

    private static CustomHotkeysServer shortcutServer = null;

    private static LinkedHashMap<Integer, ShortcutTab> hashmapShortcutTabID = new LinkedHashMap<>();
    private static ConcurrentHashMap<Integer, ShortcutTab> hashmapShortcutTab = new ConcurrentHashMap<>();

    private static SynchronizationProfile profile = null;
    private static ServerVerification verification = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        FXMLDocumentController.profile = new SynchronizationProfile();
        FXMLDocumentController.verification = ServerVerification.getInstance();

        final ArrayList<JSONObject> loadingTab = new ArrayList<>();

        this.tab_pane.getSelectionModel().selectedIndexProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {

            if(newValue.intValue()==this.tab_pane.getTabs().size()-1){
                if(loadingTab.size() > 0){
                    JSONObject get = loadingTab.get(0);
                    loadingTab.remove(get);
                    loadingTab.trimToSize();
                    ShortcutTab addTab = addTab(this.tab_pane.getTabs().size()-1, get);
                }else{
                    ShortcutTab addTab = addTab(this.tab_pane.getTabs().size()-1, null);
                }
            }

            FXMLDocumentController.hashmapShortcutTab.keySet().stream().map((in) ->
                    FXMLDocumentController.hashmapShortcutTab.get(in)).forEachOrdered((get) -> {
                get.visible(false);
            });

            int selectedIndex = this.tab_pane.getSelectionModel().getSelectedIndex();
            int hashCode = this.tab_pane.getTabs().get(selectedIndex).hashCode();

            if(FXMLDocumentController.hashmapShortcutTab.containsKey(hashCode)){
                ShortcutTab get = FXMLDocumentController.hashmapShortcutTab.get(hashCode);
                if(get!=null)get.visible(true);

                JSONObject oData = new JSONObject();
                    oData.put("tab_id", get.getTabId());
                JSONObject obj = new JSONObject();
                    obj.put("select_tab", oData);
                FXMLDocumentController.send(obj.toJSONString());

                FXMLDocumentController.saveAllTab();
            }else{
                System.out.println("hashcode not found - "+hashCode+", "+Arrays.toString(FXMLDocumentController.hashmapShortcutTab.keySet().toArray()));
            }

        });

        JSONObject prof = SynchronizationProfile.getInstance().getProfile();
        Iterator iterator = prof.keySet().iterator();
        while(iterator.hasNext()){
            Object next = iterator.next();
            JSONObject get = (JSONObject) prof.get(next);
//            if(FXMLDocumentController.hashmapShortcutTab.containsKey(Integer.parseInt(next.toString()))){
//                ShortcutTab0 tab = FXMLDocumentController.hashmapShortcutTab.get(Integer.parseInt(next.toString()));
////                tab.recreate(get);
//            }else{
                loadingTab.add(get);
//            }
        }

        loadingTab.sort((JSONObject o1, JSONObject o2) -> {
            return (Integer.valueOf(o1.get("order").toString()) > Integer.valueOf(o2.get("order").toString()))? 1 : -1;
        });

        if(this.tab_pane.getSelectionModel().getSelectedIndex() == this.tab_pane.getTabs().size()-1){
            JSONObject obj = null;
            if(loadingTab.size()>0){
                obj = loadingTab.get(0);
                loadingTab.remove(obj);
                loadingTab.trimToSize();
            }
            addTab(this.tab_pane.getTabs().size()-1, obj);
        }

        Object[] toArray = loadingTab.toArray();
        for(Object o : toArray){
            this.tab_pane.getSelectionModel().selectLast();
        }

//        MenuItem item = new MenuItem("new hotkey");
//        item.setOnAction((ActionEvent event) -> {});
//        ContextMenu menu = new ContextMenu(item);
//        this.listview.setContextMenu(menu);
        FXMLDocumentController.shortcutServer = new CustomHotkeysServer();
        if(!FXMLDocumentController.shortcutServer.isStart()){
        	Alert alert = new Alert(AlertType.WARNING);
        	// Get the Stage.
        	Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        	Image img = new Image("res/ic_launcher.png",false);
        	// Add a custom icon.
        	stage.getIcons().add(img);

        	alert.setTitle("server error");
        	alert.setHeaderText("Failed to start the server");
        	alert.setContentText("Check, dont if two instances of the application are running. Probably, the necessary ports are already occupied by TCP / UDP: 4040.");
        	alert.showAndWait();
        	System.exit(1);
        }
//        FXMLDocumentController.imgServer = new ImageServer();
    }

    public static void saveAllTab(){
        Iterator<Integer> iterator = FXMLDocumentController.hashmapShortcutTabID.keySet().iterator();
        while(iterator.hasNext()){
            Integer id = iterator.next();
            ShortcutTab get = FXMLDocumentController.hashmapShortcutTabID.get(id);
            SynchronizationProfile.getInstance().saveTab(get.saveInfo());
        }
    }

//    ImageIO.write(SwingFXUtils.fromFXImage(wim, null), "png", file);
    public static void remove(int id, Tab tab, ShortcutTab stab){
        boolean remove = FXMLDocumentController.hashmapShortcutTabID.remove(id, stab);
        boolean remove1 = FXMLDocumentController.hashmapShortcutTab.remove(tab.hashCode(), stab);
//        System.out.println("remove tab - "+remove+", size hash> "+FXMLDocumentController.hashmapShortcutTab.size());
    }

    public static void readShortcutServer(byte[] buffer){
        if(buffer!=null){
            try {
                JSONObject object = (JSONObject) new JSONParser().parse(new String(buffer));
                if(object!=null){

                    if(object.containsKey("up")){

                        JSONObject click = (JSONObject) object.get("up");
                            String tab_id = click.get("tab_id").toString();
                            String hotkey_id = click.get("hotkey_id").toString();

                            int hotkeyID = Integer.parseInt(hotkey_id);
                            int tabID = Integer.parseInt(tab_id);

                        ShortcutTab shortcut = FXMLDocumentController.hashmapShortcutTabID.get(tabID);
                        String combination = shortcut.getCombination(hotkeyID);
                        HotkeyRobot.getInstance().up(combination);

                    }else if(object.containsKey("down")){

                        JSONObject click = (JSONObject) object.get("down");
                            String tab_id = click.get("tab_id").toString();
                            String hotkey_id = click.get("hotkey_id").toString();

                            int hotkeyID = Integer.parseInt(hotkey_id);
                            int tabID = Integer.parseInt(tab_id);

                        ShortcutTab get = FXMLDocumentController.hashmapShortcutTabID.get(tabID);
                        String combination = get.getCombination(hotkeyID);
                        HotkeyRobot.getInstance().down(combination);

                    }else if(object.containsKey("loading")){

                        JSONObject loading = (JSONObject) object.get("loading");
                        String img_name = loading.get("img_name").toString();
                        byte[] imageByte = SynchronizationProfile.getInstance().getImageByte(img_name);
                        if(imageByte!=null){
                        	JSONObject objSend = new JSONObject();
                            JSONObject objData = new JSONObject();
                                objData.put("img_name", img_name);
                                objData.put("img", Base64.encodeBase64String(imageByte));
                            objSend.put("image", objData);
                            byte[] bytes = objSend.toJSONString().getBytes();
                            CustomHotkeysServer.getInstance().sendbroadcast(bytes);
                        }else{
                            System.out.println("img byte[] null - "+img_name);
                        }

                    }else if(object.containsKey("sort")){
                        JSONObject sort = (JSONObject) object.get("sort");
                        String hotkey_id = sort.get("hotkey_id").toString();
                        String tab_id = sort.get("tab_id").toString();
                        int from = Integer.parseInt(sort.get("from").toString());
                        int to = Integer.parseInt(sort.get("to").toString());
                        ShortcutTab get = hashmapShortcutTab.get(Integer.parseInt(tab_id));
                        get.drag(hotkey_id, from, to);
                    }

                }
            } catch (ParseException ex) {}
        }
    }

    public static void readImageServer(byte[] buffer){
        if(buffer!=null){
            JSONParser parse = new JSONParser();
            String string = new String(buffer);
            try {
                JSONObject obj = (JSONObject) parse.parse(string);
                if(obj.containsKey("loading")){
                    JSONObject loading = (JSONObject) obj.get("loading");
                    String img_name = loading.get("img_name").toString();
                    byte[] imageByte = SynchronizationProfile.getInstance().getImageByte(img_name);
                    if(imageByte!=null){
                        JSONObject objSend = new JSONObject();
                            objSend.put("img_name", img_name);
                            objSend.put("img", Base64.encodeBase64String(imageByte));
                        byte[] bytes = objSend.toJSONString().getBytes();
//                        ImageServer.getInstance().sendbroadcast(bytes);
//                        System.out.println("send img data length> "+bytes.length);
                    }else{
                        System.out.println("img byte[] null - "+img_name);
                    }
//                    Image image = SynchronizationProfile.getInstance().getImage(img_name);
//                    if(image!=null&&!image.isError()){
//                        byte[] img_buffer = converImageToByte(image);
//                        ImageServer.getInstance().sendbroadcast(img_buffer);
//                    }else{
//                        byte[] img_buffer = converImageToByte(new Image("res/drag_and_drop.png",false));
//                        ImageServer.getInstance().sendbroadcast(img_buffer);
//                    }
                }
            } catch (ParseException ex) {System.out.println("error parse image server > "+string);}
        }
    }

    public static byte[] getInfo(){
        try {
            JSONArray aData = new JSONArray();
            Iterator<Integer> iterator = FXMLDocumentController.hashmapShortcutTabID.keySet().iterator();
            int count = 0;
            while(iterator.hasNext()){
                Integer id = iterator.next();
                JSONObject info = FXMLDocumentController.hashmapShortcutTabID.get(id).getInfo();
                aData.add(count, info);
                count++;
            }
            JSONObject object = new JSONObject();
                object.put("allTab", aData);

//                System.out.println(object.toJSONString());

            return object.toJSONString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {return null;}
    }

    private ShortcutTab addTab(int select, JSONObject obj) {
        this.tab_pane.getSelectionModel().select(select);

        Tab selectedTab = this.tab_pane.getSelectionModel().getSelectedItem();
        selectedTab.setClosable(select>0);

        int hashCode = selectedTab.hashCode();
        ShortcutTab tab = new ShortcutTab(selectedTab, hashCode, select, obj);

        FXMLDocumentController.hashmapShortcutTabID.put(hashCode, tab);
        FXMLDocumentController.hashmapShortcutTab.put(hashCode, tab);

        Tab newtab = new Tab("+");
        newtab.setClosable(false);

        this.tab_pane.getTabs().add(newtab);
        return tab;
    }

    public static void send(String message){
        if(FXMLDocumentController.shortcutServer!=null && FXMLDocumentController.shortcutServer.isStart())
            try {FXMLDocumentController.shortcutServer.sendbroadcast(message.getBytes("UTF-8"));} catch (IOException ex) {}
    }

    private static byte[] converImageToByte(Image img){
        int w = (int)img.getWidth();
        int h = (int)img.getHeight();
        // Create a new Byte Buffer, but we'll use BGRA (1 byte for each channel) //
        byte[] buf = new byte[w * h * 4];
        /* Since you can get the output in whatever format with a WritablePixelFormat,
           we'll use an already created one for ease-of-use. */
        img.getPixelReader().getPixels(0, 0, w, h, PixelFormat.getByteBgraInstance(), buf, 0, w * 4);
        return buf;
    }

}
