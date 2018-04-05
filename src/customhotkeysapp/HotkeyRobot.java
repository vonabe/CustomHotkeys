package customhotkeysapp;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.input.KeyCode;

/**
 *
 * @author vonabe
 */
public class HotkeyRobot {

    private static Robot robot = null;
    private static ArrayList<String> tmp_down = new ArrayList<>();
    
    private static HotkeyRobot instance = null;
    public static HotkeyRobot getInstance() {
        return (instance==null)?new HotkeyRobot():instance;
    }
    
    private HotkeyRobot() {
        try {
            HotkeyRobot.robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(HotkeyRobot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    synchronized public void up(String hotkey){
        String[] split = hotkey.split("\\+");
        for(String cod : split){
            if(!cod.isEmpty()){
                KeyCode keyCode = KeyCode.getKeyCode(cod);
                HotkeyRobot.robot.keyRelease(keyCode.impl_getCode());
                HotkeyRobot.tmp_down.remove(cod);
            }else{
//                System.out.println("up cod empty");
            }
        }
    }
    
    synchronized public void down(String hotkey){
         String[] split = hotkey.split("\\+");
         for(String cod : split){
            if(!cod.isEmpty()){
                KeyCode keyCode = KeyCode.getKeyCode(cod);
                HotkeyRobot.robot.keyPress(keyCode.impl_getCode());
                HotkeyRobot.tmp_down.add(cod);
            }else{
//                System.out.println("down cod empty");
            }
        }
    }
    
    synchronized public void click(String hotkey){
        
        String[] split = hotkey.split("\\+");
        
        for(String cod : split){
            if(!cod.isEmpty()){
                KeyCode keyCode = KeyCode.getKeyCode(cod);
                HotkeyRobot.robot.keyPress(keyCode.impl_getCode());
            }else{
//                System.out.println("press cod empty");
            }
        }
        
        HotkeyRobot.robot.delay(100);
        
        for(String cod:split){
            if(!cod.isEmpty()){
                KeyCode keyCode = KeyCode.getKeyCode(cod);
                HotkeyRobot.robot.keyRelease(keyCode.impl_getCode());
            }else{
//                System.out.println("release cod empty");
            }
        }
    }
    
}
