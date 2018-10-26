package customhotkeysapp;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import customhotkeysapp.server.UpdateVersion;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author vonabe
 */
public class CustomHotkeys extends Application {

    private static TrayIcon trayIcon = null;
    final public static String version = "v0.0.3";
    final public static String name_version = "CustomHotkeys " + version;
    private MenuItem item0 = null, item1 = null;

    public static String PATH_FILE = CustomHotkeys.class.getProtectionDomain().getCodeSource().getLocation().getFile().substring(1).replace("/", "\\");
    public static String PATH = (System.getProperty("user.dir") + "\\").replace("\\", "/");
    public static String java_home = System.getProperty("java.home");

    @Override
    public void start(final Stage stage) throws Exception {
        Platform.setImplicitExit(false);
        System.out.println(PATH);

        List<String> args = getParameters().getRaw();
        boolean hide = false;
        if (args.size() > 0) {
            String info0 = args.get(0);

            if (info0.equals("/hide")) {
                hide = true;
            }
        }
        
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
        Scene scene = new Scene(root);
        stage.setTitle(name_version);
        stage.getIcons().add(new Image("res/ic_launcher.png", false));
        stage.setScene(scene);
        
        if (hide) {
            stage.hide();
        } else {
            stage.show();
        }
        
        stage.setOnCloseRequest((WindowEvent event) -> {
            FXMLDocumentController.saveAllTab();
            System.exit(0);
        });
        
        try {
            if (SystemTray.isSupported()) {

                String autorun = null;
                if (Autorun.isRegistration()) {
                    autorun = "off";
                } else {
                    autorun = "on";
                }

                SystemTray tray = SystemTray.getSystemTray();
                PopupMenu popup = new PopupMenu();
                java.awt.Image imgTray = SwingFXUtils.fromFXImage(new javafx.scene.image.Image("res/ic_launcher.png"), null);
                int trayIconWidth = tray.getTrayIconSize().width;

                trayIcon = new TrayIcon(imgTray.getScaledInstance(trayIconWidth, -1, java.awt.Image.SCALE_SMOOTH), name_version, popup);
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);

                this.item0 = new MenuItem("Autorun " + autorun);
                this.item0.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        boolean reg = Autorun.isRegistration();
                        editRegAutorun(!reg);
                        editParamReg(!reg);
                    }
                });
                popup.add(this.item0);

                this.item1 = new MenuItem("Exit");
                this.item1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        FXMLDocumentController.saveAllTab();
                        System.exit(0);
                    }
                });
                popup.add(this.item1);

                stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (newValue) {
                                    stage.hide();
                                }
                            }
                        });
                    }
                });

                trayIcon.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                stage.show();
                                stage.setIconified(false);
                            }
                        });
                    }
                });
            }
            new UpdateVersion().GET_UPDATE();

        } catch (Exception e) {
            System.out.println("exception SystemTray or WinRegistry" + e.getMessage());
        }

    }

    private void editParamReg(boolean parambool) {
        if (parambool) {
            boolean reg = Autorun.addParamHide();
        } else {
            boolean reg = Autorun.deleteParamHide();
        }
    }

    private void editRegAutorun(boolean autorun) {
        if (autorun) {
            boolean reg = Autorun.registration();
            this.item0.setLabel("Autorun " + ((reg) ? "off" : "on"));
        } else {
            boolean unreg = Autorun.unregistration();
            this.item0.setLabel("Autorun " + ((!unreg) ? "off" : "on"));
        }
    }

    public static void message(Object title, Object message, TrayIcon.MessageType type) {
//        try {
        if (trayIcon != null) {
            trayIcon.displayMessage(title.toString(), message.toString(), type);
        }
//        } catch (UnsupportedEncodingException ex) {
//            System.err.println("error encoding string");
//        }
    }

    @Override
    public void init() throws Exception {
//        Preloader.ProgressNotification progress = new Preloader.ProgressNotification(0D);
//        notifyPreloader(progress);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
