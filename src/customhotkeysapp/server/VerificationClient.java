package customhotkeysapp.server;

import java.awt.TrayIcon.MessageType;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.esotericsoftware.kryonet.Connection;

import customhotkeysapp.CustomHotkeys;

/**
 *
 * @author vonabe
 */
public class VerificationClient {

    private final int constHashSALT = -202896896, constHashKEY = 585948151;
//    private long time = 0;
    private boolean verification = false;
    private Connection client = null;

    private byte[] question = null;
//    private int hashkey = -1;
    private byte[] key = null;
    private String name = null;
    private StringCrypter crypter = null;
    public StringCrypter crypterPublic = null;

    public VerificationClient(Connection ch) {
        this.key = ServerVerification.getKeyVerification();
        this.crypter = new StringCrypter(this.key);
        this.client = ch;
//        this.time = System.nanoTime();

//        String crypted = ""+this.time;
//        String encrypt = this.crypter.encrypt(crypted);
//        this.hashkey = crypted.hashCode();

//         try {
//            this.client.sendTCP(encrypt.getBytes("UTF-8"));
//        } catch (UnsupportedEncodingException ex) {
////            Logger.getLogger(VerificationClient.class.getName()).log(Level.SEVERE, null, ex);
//        }

        String publicKey = String.valueOf(constHashSALT)+String.valueOf(constHashKEY);

        int name = publicKey.length();

        byte[] keyrandom = new byte[8];
        byte[] keyrandomPublic = new byte[8];
        for(int i=0;i<keyrandom.length;i++){
            keyrandom[i]=(byte)randInt(0, name-1);
            keyrandomPublic[i] = (byte)publicKey.charAt(keyrandom[i]);
        }

        this.crypterPublic = new StringCrypter(keyrandomPublic);

        JSONObject objData = new JSONObject();
//            objData.put("encrypt", Base64.encodeBase64String(encrypt.getBytes()));
            objData.put("keys", Base64.encodeBase64String(keyrandom));
        JSONObject objSend = new JSONObject();
            objSend.put("verify", objData);

        try {
            this.client.sendTCP( this.crypter.encrypt(objSend.toJSONString()).getBytes("UTF-8") );
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(VerificationClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isVerification() {
        return this.verification;
    }

    public boolean key(byte[] k){
        this.question = k;
//        int parsekey;
//        try{
//            parsekey = Integer.parseInt(new String(k));
//        }catch(NumberFormatException ex){
//            System.out.println("error key parse - "+new String(k));
//            return this.verification;
//        }
//
//        if(parsekey == this.hashkey){
//            this.verification = true;
//        }else{
//            System.out.println("client bad key verification - "+new String(this.question)+", hashkey: "+this.hashkey);
//            this.client.close();
//        }
//        return this.verification;

//      ******************************************************************************
        try {
            String decrypt = this.crypter.decrypt(new String(k));
            JSONObject result = (JSONObject)new JSONParser().parse(decrypt);
            if(result.containsKey("SALT") && result.containsKey("KEY") &&  result.containsKey("name")){
                String toSALT = result.get("SALT").toString();
                String toKEY = result.get("KEY").toString();

                this.name = result.get("name").toString();

                int iSALT = Integer.parseInt(toSALT);
                int iKEY = Integer.parseInt(toKEY);

                if(iSALT == constHashSALT && iKEY == constHashKEY ){
                    this.verification = true;
                    CustomHotkeys.message("Подключение", "Подключился клиент "+this.name, MessageType.INFO);
                }else{
                    System.out.println("client bad key verification");
                    this.client.close();
                    this.verification = false;
                }
            }
        }catch(NumberFormatException ex){
            this.verification = false;
            System.out.println("error key parse - "+new String(k));
        } catch (ParseException ex) {
            this.verification = false;
        }
        return this.verification;
    }

    private static Random rand = new Random();
    public static int randInt(int min, int max) {
            rand.setSeed(System.nanoTime());
            int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }


}
