package net.doridian.amplifi;

import com.google.gson.JsonObject;
import com.nimbusds.srp6.SRP6ClientCredentials;
import com.nimbusds.srp6.SRP6ClientSession;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Exception;
import net.doridian.amplifi.ubntsrp.UbntClientEvidenceRoutine;
import net.doridian.amplifi.ubntsrp.UbntServerEvidenceRoutine;
import net.doridian.amplifi.ubntsrp.UbntXRoutine;
import net.doridian.amplifi.packets.PacketAuthStep1;
import net.doridian.amplifi.packets.PacketAuthUser;
import org.bouncycastle.util.encoders.Base64;

import java.math.BigInteger;
import java.net.URISyntaxException;

public class Authenticator {
    public Authenticator(String ip, String username, String password) {
        this.ip = ip;
        this.username = username;
        this.password = password;
    }

    private static final String AUTH_IFACE = "com.ubnt.UnifiHome.Auth";
    private static final String AUTH_METHOD = "SrpServerAuth";

    private String ip;
    private String username;
    private String password;
    private SRP6ClientSession srp6ClientSession;
    private WebSocket aws;

    public WebSocket connect() throws InterruptedException, URISyntaxException {
        JsonObject payload;
        aws = new WebSocket(ip);
        aws.connectBlocking();

        PacketAuthUser packetAuthUser = new PacketAuthUser();
        packetAuthUser.user = username;
        payload = aws.sendCommandJSONSync(AUTH_IFACE, AUTH_METHOD, packetAuthUser);

        BigInteger B = Utils.decodeBase64BigInt(payload, "B");
        BigInteger s = Utils.decodeBase64BigInt(payload, "s");

        srp6ClientSession = new SRP6ClientSession();
        srp6ClientSession.setXRoutine(new UbntXRoutine());
        srp6ClientSession.setClientEvidenceRoutine(new UbntClientEvidenceRoutine());
        srp6ClientSession.setServerEvidenceRoutine(new UbntServerEvidenceRoutine());

        srp6ClientSession.step1(username, password);

        SRP6CryptoParams cryptoParams = SRP6CryptoParams.getInstance(2048, "SHA-256");

        PacketAuthStep1 packetAuthStep1 = new PacketAuthStep1();
        try {
            SRP6ClientCredentials credentials = srp6ClientSession.step2(cryptoParams, s, B);

            packetAuthStep1.A = Base64.toBase64String(credentials.A.toByteArray());
            packetAuthStep1.M = Base64.toBase64String(credentials.M1.toByteArray());
        } catch (SRP6Exception e) {
            throw new RuntimeException(e);
        }

        payload = aws.sendCommandJSONSync(AUTH_IFACE, AUTH_METHOD, packetAuthStep1);

        BigInteger HAMK = Utils.decodeBase64BigInt(payload, "HAMK");
        try {
            srp6ClientSession.step3(HAMK);
            if (srp6ClientSession.getState() != SRP6ClientSession.State.STEP_3) {
                throw new RuntimeException("Not state 3");
            }
        } catch(SRP6Exception e) {
            throw new RuntimeException(e);
        }

        return aws;
    }
}
