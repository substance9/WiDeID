package edu.uci.ics.deid.service.deidengine;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.model.MacAddress;

@Component
public class HashOperator {

    // Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public HashOperator() {
    }

    public byte[] getNewRandomSalt(){
        SecureRandom sr = null;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        if (sr==null){
            return null;
        }

        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    public String getHashedID(MacAddress macAddress, byte[] salt){
        MessageDigest msgDigest = null;
        try {
            msgDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Cannot find SHA-256 Algorithm");
            e.printStackTrace();
            return null;
        }
        msgDigest.reset();
        msgDigest.update(salt);

        byte[] hashedID = null;

        try {
            hashedID = msgDigest.digest(macAddress.getMacAddrByte());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String hashedIDStr = Base64.getEncoder().encodeToString(hashedID);
        return hashedIDStr;
    }

}