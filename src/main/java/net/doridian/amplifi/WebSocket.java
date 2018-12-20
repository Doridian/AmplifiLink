package net.doridian.amplifi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.doridian.amplifi.packets.PacketEncap;
import net.doridian.amplifi.packets.PacketPayload;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class WebSocket extends WebSocketClient {
    public static boolean MESSAGE_DEBUG = false;

    public interface IResponder {
        void gotResponse(String iface, String method, JsonObject payload);
    }

    private final HashMap<Integer, IResponder> responders = new HashMap<>();

    public WebSocket(String ip) throws URISyntaxException {
        super(new URI("wss://" + ip + ":9016/"));
        this.setMySocket();
    }


    public void sendCommand(String iface, String method, Object payload, IResponder responder) {
        PacketEncap command = PacketEncap.makeCommand(iface, method, new PacketPayload<>(payload));
        synchronized (responders) {
            responders.put(command.seqId, responder);
        }
        this.send(command.encode());
    }

    private class CommandSender implements IResponder {
        private JsonObject jsonObject = null;
        private Thread waitThread;

        CommandSender(String iface, String method, Object payload) {
            waitThread = Thread.currentThread();
            sendCommand(iface, method, payload, this);
        }

        private JsonObject getResponseBlocking() throws InterruptedException {
            if (Thread.currentThread() != waitThread) {
                throw new RuntimeException("Wrong thread");
            }

            while (jsonObject == null) {
                synchronized (waitThread) {
                    waitThread.wait(5000);
                }
            }

            return jsonObject;
        }

        @Override
        public void gotResponse(String iface, String method, JsonObject payload) {
            jsonObject = payload;
            synchronized (waitThread) {
                waitThread.notify();
            }
        }
    }

    public JsonObject sendCommandSync(String iface, String method, Object payload) throws InterruptedException {
        CommandSender sender = new CommandSender(iface, method, payload);
        return sender.getResponseBlocking();
    }

    private void setMySocket() {
        try {
            this.setSocket(Utils.getAllTrustFactory().createSocket());
        } catch(IOException io) {
            throw new RuntimeException(io);
        }
    }

    public void onOpen(ServerHandshake serverHandshake) {

    }

    public void onMessage(String s) {
        if (MESSAGE_DEBUG) {
            System.out.println(s);
        }

        Gson gson = new Gson();
        Type packetType = new TypeToken<PacketEncap<JsonObject>>() { }.getType();
        PacketEncap<JsonObject> packet = gson.fromJson(s, packetType);

        if (!packet.type.equals("response")) {
            return;
        }

        IResponder responder;
        synchronized (responders) {
            responder = responders.remove(packet.seqId);
        }
        if (responder != null) {
            try {
                responder.gotResponse(packet.iface, packet.method, packet.payload.value);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void onClose(int i, String s, boolean b) {

    }

    public void onError(Exception e) {

    }
}
