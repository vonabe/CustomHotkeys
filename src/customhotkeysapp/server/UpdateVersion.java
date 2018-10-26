package customhotkeysapp.server;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import customhotkeysapp.CustomHotkeys;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class UpdateVersion {

    public void GET_UPDATE() {
        Document doc = null;
        try {
            String urlUPDATE = "http://customhotkeys.xyz/downloading/";
            doc = Jsoup.connect(urlUPDATE).get();
            String url = doc.select("li").get(1).getElementsByTag("a").get(0).attr("href");
            if (!url.contains(CustomHotkeys.version)) {
                urlUPDATE += url;
                Document doc2 = Jsoup.connect(urlUPDATE).get();
                String filename = doc2.select("li").get(1).getElementsByTag("a").get(0).ownText();
                urlUPDATE += filename;
                if (urlUPDATE.endsWith(".jar")) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    // Get the Stage.
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    Image img = new Image("res/ic_launcher.png", false);
                    // Add a custom icon.
                    stage.getIcons().add(img);

                    alert.setTitle("Update");
                    alert.setHeaderText("A new update is available.");
//		        	alert.setContentText(filename);
                    ButtonType btnYES = new ButtonType("YES");
                    ButtonType btnNO = new ButtonType("NO");
                    ButtonType btnSITE = new ButtonType("VISIT WEBSITE");
                    alert.getButtonTypes().setAll(btnYES, btnNO, btnSITE);

                    Optional<ButtonType> type = alert.showAndWait();

                    if (type.get() == btnYES) {
                        download(new File(CustomHotkeys.PATH + "/UpdateCustomHotkeys.jar"));
                    } else if (type.get() == btnSITE) {
                        Desktop.getDesktop().browse(URI.create("http://customhotkeys.xyz/"));
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (new File(CustomHotkeys.PATH + "/UpdateCustomHotkeys.jar").exists()) {
                System.out.println("delete updatefile> " + new File(CustomHotkeys.PATH + "/UpdateCustomHotkeys.jar").delete());
            }
        }
    }
    
    public void download(File file) throws IOException {
        InputStream stream = this.getClass().getResourceAsStream("/res/UpdateCustomHotkeys.jar");
        try {
            BufferedInputStream bis = new BufferedInputStream(stream);
            FileOutputStream fis = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = bis.read(buffer, 0, 1024)) != -1) {
                fis.write(buffer, 0, count);
            }
            fis.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final String exec = "cmd.exe start /C java -jar " + file.getAbsolutePath() + " " + CustomHotkeys.version + " " + CustomHotkeys.PATH_FILE.substring(CustomHotkeys.PATH_FILE.lastIndexOf(".") + 1);
        print(exec);
        Process process = Runtime.getRuntime().exec(exec);
        System.exit(0);
    }
    
    public static void print(String msg) {
        System.out.println(msg);
    }

}
