package net.doridian.amplifi.ubntsrp;

import com.nimbusds.srp6.*;

import java.math.BigInteger;
import java.security.MessageDigest;

public class UbntServerEvidenceRoutine extends SRP6Routines implements ServerEvidenceRoutine {
    public BigInteger computeServerEvidence(SRP6CryptoParams cryptoParams, SRP6ServerEvidenceContext ctx) {
        // This differs from the vanilla algorithm only in that it hashes S prior to putting it in
        MessageDigest digest = cryptoParams.getMessageDigestInstance();
        byte[] hashedS = digest.digest(BigIntegerUtils.bigIntegerToBytes(ctx.S));
        return this.computeServerEvidence(digest, ctx.A, ctx.M1, new BigInteger(1, hashedS));
    }
}
