package net.doridian.amplifi.ubntsrp;

import com.nimbusds.srp6.SRP6Routines;
import com.nimbusds.srp6.XRoutine;
import java.math.BigInteger;
import java.security.MessageDigest;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

public class UbntXRoutine extends SRP6Routines implements XRoutine {
    private static final int PBKDF2_ITERATIONS = 42000;
    private static final int PBKDF2_DIGEST_SIZE = new SHA256Digest().getDigestSize() * 8;

    public BigInteger computeX(MessageDigest digest, byte[] salt, byte[] username, byte[] password) {
        // This analog to the CSRP implementation except that is performs PBKDF2 on the password first
        // We cannot do this elsewhere as the library treats password as a string otherwise
        // Which would not work out well with binary data
        // See: https://github.com/cocagne/csrp/blob/master/srp.c#L325

        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
        generator.init(password, salt, PBKDF2_ITERATIONS);
        KeyParameter hashedPasswordKeyParam = (KeyParameter)generator.generateDerivedMacParameters(PBKDF2_DIGEST_SIZE);
        byte[] hashedPassword = hashedPasswordKeyParam.getKey();

        // This is mostly what this library does by default
        // Except the inner hash is just H(password)
        // While we do H(username:PBKDF2(password))
        byte[] rawData = new byte[username.length + 1 + hashedPassword.length];
        System.arraycopy(username, 0, rawData, 0, username.length);
        rawData[username.length] = ':';
        System.arraycopy(hashedPassword, 0, rawData, username.length + 1, hashedPassword.length);
        return this.computeX(digest, salt, rawData);
    }
}
