package customhotkeysapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 *
 * @author vonabe
 */
public class SynchronizationProfile {

    private JSONObject object = null;

    private FileEdit fileedit = null;
    private final String property = System.getProperty("user.home");
    private final String pCore = "/ShortcutProfile", pImages = "/images", pProfile = "/profile.json", pData = "/user.data";
    private File fileProfileJson = null, fileDirImages = null;

    private static SynchronizationProfile instance = null;

    public static SynchronizationProfile getInstance() {
        return instance;
    }

    public SynchronizationProfile() {
        SynchronizationProfile.instance = this;
        boolean createProfile = this.createProfile();
        if (createProfile) {
            this.fileedit = new FileEdit(this.fileProfileJson.getAbsolutePath());
            try {
                String readFile = this.fileedit.readFile();
                if (readFile != null) {
                    JSONParser parser = new JSONParser();
                    this.object = (JSONObject) parser.parse(readFile);
                }
            } catch (IOException | ParseException ex) {
                this.object = new JSONObject();
            }
        }
    }

    public JSONObject getProfile() {
        if (this.object != null) {
            return this.object;
        }
        return null;
    }

    public boolean deleteImg(String imgName){
          if (this.fileDirImages != null) {
            File[] listFiles = this.fileDirImages.listFiles((File pathname) -> pathname.getAbsolutePath().endsWith(".png") || pathname.getAbsolutePath().endsWith(".jpeg") || pathname.getAbsolutePath().endsWith(".jpg"));
            if(listFiles==null)return false;
            for (File f : listFiles) {
                String filename = f.getName();
                filename = filename.substring(0, filename.lastIndexOf("."));
                if (filename.equals(imgName)) {
                    return f.delete();
                }
            }
        }
        return false;
    }
    
    public byte[] getImageByte(String imgName){
        if (this.fileDirImages != null) {
            File[] listFiles = 
                    this.fileDirImages.listFiles((File pathname) -> pathname.getAbsolutePath().endsWith(".png") 
                            || pathname.getAbsolutePath().endsWith(".jpeg")
                                || pathname.getAbsolutePath().endsWith(".jpg"));
            for (File f : listFiles) {
                String filename = f.getName();
                filename = filename.substring(0, filename.lastIndexOf("."));
                if (filename.equals(imgName)) {
                    try {
                        Path get = Paths.get(f.getAbsolutePath());
                        return Files.readAllBytes(get);
                    } catch (FileNotFoundException ex) {} catch (IOException ex) {}
                }
            }
        }
        return null;
    }
    
    public Image getImage(String imgName, StringBuilder format) {
        if (this.fileDirImages != null) {
            File[] listFiles = this.fileDirImages.listFiles((File pathname) -> pathname.getAbsolutePath().endsWith(".png") || pathname.getAbsolutePath().endsWith(".jpeg") || pathname.getAbsolutePath().endsWith(".jpg"));
            for (File f : listFiles) {
                String filename = f.getName();
                filename = filename.substring(0, filename.lastIndexOf("."));
                if (filename.equals(imgName)) {
                    Image img = new Image("file:" + f.getAbsolutePath(), false);
                    format.append(f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".")+1, f.getAbsolutePath().length()));
                    return img;
                }
            }
        }
        return null;
    }

    public boolean renameImg(String oldName, String newName) {
        if (this.fileDirImages != null) {
            File[] listFiles = this.fileDirImages.listFiles((File pathname) -> pathname.getAbsolutePath().endsWith(".png") || pathname.getAbsolutePath().endsWith(".jpeg") || pathname.getAbsolutePath().endsWith(".jpg"));
            for (File f : listFiles) {
                String filename = f.getName();
                filename = filename.substring(0, filename.lastIndexOf("."));
                String format = f.getName().substring(f.getName().lastIndexOf(".")+1, f.getName().length());
                if (filename.equals(oldName)) {
                    f.renameTo(new File(this.fileDirImages.getAbsolutePath() + "/" + newName + "." + format));
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean removeTab(String tab_id) {
        if (this.object != null && tab_id != null && this.object.containsKey(tab_id)) {
            this.object.remove(tab_id);
            try {
                this.fileedit.write(this.object.toJSONString());
            } catch (IOException ex) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    public boolean saveTab(JSONObject tab) {
        if (this.object != null && tab != null && tab.containsKey("info_tab")) {
            tab = (JSONObject) tab.get("info_tab");
            String tab_id = tab.get("tab_id").toString();
            boolean save = true;
            if (this.object.containsKey(tab_id)) {
                if(this.object.get(tab_id).toString().hashCode()==tab.toJSONString().hashCode())save=false;
                if(save)this.object.replace(tab_id, tab);
            } else {
                this.object.put(tab_id, tab);
            }
            try {
                if(save)this.fileedit.write(this.object.toJSONString());
            } catch (IOException ex) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean saveImage(Image image, String name, String format) {
        this.deleteImg(name);
        File file = new File(this.fileDirImages.getAbsolutePath() + "/" + name +"."+ format);
        if (!image.isError()) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), format, file);
                return true;
            } catch (IOException ex) {
                return false;
            }
        }
        return false;
    }

    private boolean createProfile() {
        boolean profile = false;

        File fImages = new File(property + this.pCore + this.pImages);
        File fProfile = new File(property + this.pCore + this.pProfile);
        File fData = new File(property + this.pCore + this.pData);

        if (!fImages.isDirectory()) {
            fImages.mkdirs();
        }
        if (fImages.isDirectory()) {
            profile = true;
            if (!fProfile.isFile()) {
                try {
                    boolean createNewFile = fProfile.createNewFile();
                    if (!createNewFile) {
                        profile = false;
                    }
                } catch (IOException ex) {
                }
            }
            if(!fData.isFile()){
                try {
                    boolean createNewFile = fData.createNewFile();
                    if(!createNewFile){
                        profile = false;
                    }
                } catch (IOException ex) {}
            }
        }
        if (fProfile.isFile()) {
            this.fileProfileJson = fProfile;
        }
        if (fImages.isDirectory()) {
            this.fileDirImages = fImages;
        }
        return profile;
    }

}
