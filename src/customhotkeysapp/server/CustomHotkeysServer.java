package customhotkeysapp.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import customhotkeysapp.FXMLDocumentController;

/**
 *
 * @author vonabe
 */
public class CustomHotkeysServer extends Listener {

    private final int PORT = 4040;
    private ConcurrentHashMap<Integer, Connection> hashmapClient = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, VerificationClient> clientVerification = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, StringCrypter> clientCrypter = new ConcurrentHashMap<>();

    private boolean start = false;
    private Server server = null;

    private static CustomHotkeysServer instance = null;

    public static CustomHotkeysServer getInstance() {
        return CustomHotkeysServer.instance;
    }

    public CustomHotkeysServer() {
        CustomHotkeysServer.instance = this;
        try {
            this.server = new Server(1024 * 1024, 1024 * 1024);
            this.server.start();
            this.server.bind(this.PORT,this.PORT);

            Kryo kryo = server.getKryo();
            kryo.register(byte[].class);

            this.server.addListener(this);

            this.start = true;
        } catch (IOException ex) {
            this.start = false;
        }

    }

    public boolean isStart() {
        return this.start;
    }

    @Override
    public void connected(Connection connection) {
        String name = connection.getRemoteAddressTCP().toString();
        System.out.println("connect "+getClass().getName()+"> "+name);
        if (!this.clientVerification.containsKey(connection.getID())) {
            this.clientVerification.put(connection.getID(), new VerificationClient(connection));
        } else {
            System.out.println("verification error "+getClass().getName()+"> " + name);
            this.removeClient(connection.getID());
        }
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof byte[]) {
            byte[] bytes = (byte[]) object;
            int id = connection.getID();
            if (this.clientVerification.containsKey(id)) {
                boolean verifi = this.clientVerification.get(id).key(bytes);
                if (verifi) {
                    this.clientCrypter.put(id, this.clientVerification.get(id).crypterPublic);
                    this.clientVerification.remove(id);
                    this.hashmapClient.put(id, connection);
                    System.out.println("verification success "+getClass().getName()+"> " + connection.getRemoteAddressTCP());
                    byte[] info = FXMLDocumentController.getInfo();
                    this.send(connection, info);
                }
            } else if (this.hashmapClient.containsKey(id)) {
                FXMLDocumentController.readShortcutServer(this.clientCrypter.get(id).decrypt(new String(bytes)).getBytes());
            }
        }else{
            
        }
    }

    @Override
    public void disconnected(Connection connection) {
        System.out.println("disconnect "+getClass().getName()+"> "+connection.getID());
        this.removeClient(connection.getID());
    }

    public void send(Connection client, byte[] message) {
    	int id = client.getID();
    	byte[] send;
		try {
			send = this.clientCrypter.get(id).encrypt(new String(message,"UTF-8")).getBytes();
	    	this.hashmapClient.get(id).sendTCP(send);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void sendbroadcast(byte[] message) {
        Iterator<Integer> iterator = this.hashmapClient.keySet().iterator();
        while (iterator.hasNext()) {
            int id = iterator.next();
            byte[] send;
			try {
				send = this.clientCrypter.get(id).encrypt(new String(message,"UTF-8")).getBytes();
	            this.hashmapClient.get(id).sendTCP(send);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    private void removeClient(int id) {
        this.clientVerification.remove(id);
        this.hashmapClient.remove(id);
        this.clientCrypter.remove(id);
    }

}
