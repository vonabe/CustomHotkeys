package customhotkeysapp;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vonabe
 */
public class Autorun {

    private static final String KEY  = "Software\\Microsoft\\Windows\\CurrentVersion\\Run\\";
    private static final String NAME = "CustomHotkeys";
//    private static final String PATH = "\""+System.getProperty("java.class.path")+"\" ";
    private static String PATH = "\""+CustomHotkeys.class.getProtectionDomain().getCodeSource().getLocation().getFile().substring(1).replace("/", "\\")+"\" ";

    public static boolean registration(){
        if(!Autorun.isRegistration()){
            try {
                WinRegistry.writeStringValue(
                        WinRegistry.HKEY_CURRENT_USER,
                        Autorun.KEY,
                        Autorun.NAME,
                        Autorun.PATH);
                return Autorun.isRegistration();
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(Autorun.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }

    public static boolean unregistration(){
        if(Autorun.isRegistration()){
            try {
                WinRegistry.deleteValue(
                        WinRegistry.HKEY_CURRENT_USER,
                        Autorun.KEY,
                        Autorun.NAME);
                return true;
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(Autorun.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }

    public static boolean isRegistration(){
        try {
            String path = WinRegistry.readString(
                    WinRegistry.HKEY_CURRENT_USER,
                    Autorun.KEY,
                    Autorun.NAME);
            if(path!=null)return true;
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(Autorun.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return false;
    }


    public static boolean addParamHide(){
        if(Autorun.isRegistration() && !isParameter()){
            try {
                if(!unregistration())return false;
                WinRegistry.writeStringValue(
                        WinRegistry.HKEY_CURRENT_USER,
                        Autorun.KEY,
                        Autorun.NAME,
                        Autorun.PATH+"/hide");
                return Autorun.isRegistration()&&Autorun.isParameter();
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(Autorun.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }

       public static boolean deleteParamHide(){
        if(Autorun.isRegistration()&&isParameter()){
            try {
                if(!unregistration())return false;
                WinRegistry.writeStringValue(
                        WinRegistry.HKEY_CURRENT_USER,
                        Autorun.KEY,
                        Autorun.NAME,
                        Autorun.PATH);
                return Autorun.isRegistration()&&!Autorun.isParameter();
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
                Logger.getLogger(Autorun.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }


    public static boolean isParameter(){
        try {
            String path = WinRegistry.readString(
                    WinRegistry.HKEY_CURRENT_USER,
                    Autorun.KEY,
                    Autorun.NAME);
//            System.out.println("isParameter - "+path);
            if(path!=null&&path.endsWith("/hide"))return true;
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            Logger.getLogger(Autorun.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return false;
    }

}
