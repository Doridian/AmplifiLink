package net.doridian.amplifi.packets;

public class PacketPayload<T> {
    public T value;
    public String msgpack;

    public PacketPayload() {

    }

    public PacketPayload(T payload) {
        if (payload instanceof String) {
            this.msgpack = (String)payload;
        } else {
            this.value = payload;
        }
    }
}
