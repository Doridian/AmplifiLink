package net.doridian.amplifi.packets;

import com.google.gson.Gson;

public class PacketEncap<T> {
    public String iface;
    public String method;
    public int seqId;
    public PacketPayload<T> payload;
    public String type;

    private static int lastSeqId = 0;

    public static <P> PacketEncap<P> makeCommand(String iface, String method, PacketPayload<P> payload) {
        PacketEncap res = new PacketEncap<P>();
        res.iface = iface;
        res.method = method;
        if (payload.value != null || payload.msgpack != null) {
            res.payload = payload;
        }
        res.type = "call";
        res.seqId = ++lastSeqId;
        return res;
    }

    public String encode() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
