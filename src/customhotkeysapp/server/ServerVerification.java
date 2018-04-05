package customhotkeysapp.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author vonabe
 */
public class ServerVerification {

//    final private static String IP = "46.173.6.49";
    private static ServerVerification instance = null;
    final private static byte[] key = new byte[]{8,1,9,4,5,3,3,2};
    private static StringCrypter crypter;
//    private Client client = new Client();

    private static final String property = System.getProperty("user.home");
    private static final String pCore = "/ShortcutProfile", pData = "/user.data";
    private static byte[] verification = null;

//    private boolean connect = false;

    public static ServerVerification getInstance() {
        return (instance==null)?instance = new ServerVerification():instance;
    }

    private ServerVerification() {
        crypter = new StringCrypter(key);
        verification = this.getKey();
//        this.client.start();
//        this.client.addListener(new Listener(){
//            @Override
//            public void connected(Connection cnctn) {
//                connect = true;
//            }
//            @Override
//            public void disconnected(Connection cnctn) {
//                connect = false;
//                new Timer().schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        try {
//                            if(connect)super.cancel();
//                                else client.reconnect();
//                        } catch (IOException e) {}
//                    }
//                }, 1000, 10_000);
//            }
//            @Override
//            public void received(Connection cnctn, Object o) {
//                if(o instanceof byte[]){
//                    try {
//                        byte[]buffer = (byte[]) o;
//                        String string = new String(buffer);
//                        String encrypt = crypter.encrypt(string);
//                        if(encrypt!=null){
//                            JSONObject obj = (JSONObject) new JSONParser().parse(encrypt);
//                            if(obj!=null){
//                                if(obj.containsKey("key")){
//                                      String key = obj.get("key").toString();
//                                      byte[] backToBytes = Base64.decodeBase64(key);
//                                      writeKey(backToBytes);
//                                }
//                            }
//                        }
//                    } catch (ParseException ex) {}
//                }
//            }
//        });
//        Kryo kryo = this.client.getKryo();
//        kryo.register(byte[].class);

//        new Thread(){
//            @Override
//            public void run() {
//                while(!connect){
//                    try {
//                        client.connect(15_000, IP, 3000);
//                    } catch (IOException ex) {}}
//            }
//        }.start();

    }

    public static byte[] getKeyVerification(){
        return verification;
    }

    private static void writeKey(byte[]key_){
        File file = new File(property+pCore+pData);
        if(file.exists()){
            try {
                verification = key_;
                Path get = Paths.get(file.getAbsolutePath());
                JSONObject obj = new JSONObject();
                String encode = Base64.encodeBase64String(key_);
                    obj.put("key", encode);
                String encrypt = crypter.encrypt(obj.toJSONString());
                Files.write(get, encrypt.getBytes());
            } catch (IOException ex) {}
        }
    }

    private byte[] getKey(){
        File file = new File(property+pCore+pData);
        if(file.exists()){

            try {
                Path get = Paths.get(file.getAbsolutePath());
                byte[] readAllBytes = Files.readAllBytes(get);
                if(readAllBytes!=null){
                    String string = new String(readAllBytes);
                    if(string.replaceAll(" ", "").isEmpty()){
                        JSONObject obj = new JSONObject();
                            final byte[] bytea = new byte[]{6, 9, 6, 9, 0, 0, 1, 4};
                            String encode = Base64.encodeBase64String(bytea);
                            obj.put("key", encode);
                        String toJSONString = obj.toJSONString();
                        String encrypt = crypter.encrypt(toJSONString);
                        Files.write(get, encrypt.getBytes());
                        return bytea;
                    } else {
                        String decrypt = crypter.decrypt(string);
                        if(decrypt!=null){
                            JSONParser parse = new JSONParser();
                            JSONObject obj = (JSONObject) parse.parse(decrypt);
                            if(obj.containsKey("key")){
                                String key = obj.get("key").toString();
                                byte[] backToBytes = Base64.decodeBase64(key);
                                return backToBytes;
                            }
                        }
                    }
                }
            } catch (IOException | ParseException ex) {
                Logger.getLogger(ServerVerification.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return null;
    }


}