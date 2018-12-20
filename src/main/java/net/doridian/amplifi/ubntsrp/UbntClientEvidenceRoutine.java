package net.doridian.amplifi.ubntsrp;

import com.nimbusds.srp6.*;

import java.math.BigInteger;
import java.security.MessageDigest;

public class UbntClientEvidenceRoutine implements ClientEvidenceRoutine {
    public BigInteger computeClientEvidence(SRP6CryptoParams cryptoParams, SRP6ClientEvidenceContext ctx) {
        // This is the totally different client evidence computation from CSRP
        // See: https://github.com/cocagne/csrp/blob/master/srp.c#L363
        MessageDigest digest = cryptoParams.getMessageDigestInstance();
        byte[] hashedN = digest.digest(BigIntegerUtils.bigIntegerToBytes(cryptoParams.N));
        byte[] hashedG = digest.digest(BigIntegerUtils.bigIntegerToBytes(cryptoParams.g));
        byte[] hashedUsername = digest.digest(ctx.userID.getBytes());
        byte[] hashedXor = new byte[hashedN.length];
        for (int i = 0; i < hashedXor.length; i++) {
            hashedXor[i] = (byte)(hashedN[i] ^ hashedG[i]);
        }
        byte[] hashedS = digest.digest(BigIntegerUtils.bigIntegerToBytes(ctx.S));
        digest.update(hashedXor);
        digest.update(hashedUsername);
        digest.update(BigIntegerUtils.bigIntegerToBytes(ctx.s));
        digest.update(BigIntegerUtils.bigIntegerToBytes(ctx.A));
        digest.update(BigIntegerUtils.bigIntegerToBytes(ctx.B));
        digest.update(hashedS);
        return new BigInteger(1, digest.digest());
    }
}
