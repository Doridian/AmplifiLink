package net.doridian.amplifi.calls.clusternode;

import net.doridian.amplifi.WebSocket;

public class GetWifiClientInfo {
    public static class WifiClientInfo {

    }

    public static WifiClientInfo run(WebSocket aws) throws InterruptedException {
        String msgpack = aws.sendCommandMsgpackSync("com.ubnt.UnifiHome.ClusterNode", "GetWifiClientInfo", "ksDA");
        return null;
    }
}
