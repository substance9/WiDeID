package edu.uci.ics.deid.model;

import javax.annotation.Nonnull;
import javax.xml.bind.DatatypeConverter;

import com.google.common.base.Preconditions;

import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class MacAddress {
    /**
     * Class for 6 byte mac address.
     * part learned from MacAddress class in Floodlight project written by Andreas Wundsam
    */

    static final int MAC_ADDR_LEN = 6;
    static final int MAC_SEC_LEN = 2;
    static final int MAC_STRING_LENGTH = MAC_ADDR_LEN * MAC_SEC_LEN + (MAC_ADDR_LEN - 1) ;
    static final char SEPARATOR = ' ';


    static final String FORMAT_ERROR = "Mac address is not well-formed. " +
            "It must consist of 6 hex digit pairs separated by spaces: ";

    private String macAddrStr;
    private Long macAddrLong;
    private String initHashId;
    private byte[] macAddrBytes;

    /**

     * create instance based on mac address string.
     * 6 hex bytes separated by space (01 02 03 04 05 06)
     * , alsp generate initial hashed id.
     * 
     * (Updated in V2) initial hashed id is deprecated
     *
     * @param macString - MAC address in string format
     */
    public MacAddress(@Nonnull final String macString) throws IllegalArgumentException {
        Preconditions.checkNotNull(macString, "macString must not be null");
        Preconditions.checkArgument(macString.length() == MAC_STRING_LENGTH,
                FORMAT_ERROR + macString);
        char sep = macString.charAt(2);
        Preconditions.checkArgument(sep == MacAddress.SEPARATOR,
                FORMAT_ERROR + macString + " (invalid separator)");

        String capMacString = macString.toUpperCase();

        setMacAddrStr(capMacString);
        this.macAddrBytes = calculateMacInBytes(capMacString);
    }

    public MacAddress(Long rawVal){
        setMacAddrLong(rawVal);
    }

    public String getMacAddrStr() {
        return macAddrStr;
    }

    public long getMacAddrLong() {
        return macAddrLong;
    }

    public void setMacAddrStr(String macString) {
        this.macAddrStr = macString;
        this.macAddrLong = calculateMacInLong(this.macAddrStr);
        this.macAddrBytes = calculateMacInBytes(macString);
    }

    public void setMacAddrLong(long macLong) {
        this.macAddrLong = macLong;
        String macAddrStrRaw = String.format("%012X", this.macAddrLong);

        StringBuilder builder = new StringBuilder(MAC_STRING_LENGTH);
        for (int i = 0; i < macAddrStrRaw.length(); i += 2) {
            if (i != 0) {
                builder.append(SEPARATOR);
            }
            builder.append(macAddrStrRaw.substring(i, i + 2));
        }
        this.macAddrStr = builder.toString();
        this.macAddrBytes = calculateMacInBytes(this.macAddrStr);
    }

    public void setMacAddrByte(byte[] macBytes){
        this.macAddrBytes = macBytes;
    }

    public byte[] getMacAddrByte(){
        return this.macAddrBytes;
    }

    public String getInitHashId(){
        if(initHashId == null){
            setInitHashId();
        }
        return initHashId;
    }

    public void setInitHashId(){
        this.initHashId = calculateInitHashId(this.macAddrStr);
    }

    public static String calculateInitHashId(String macString){
        // Learned from giraam/string-to-sha1.java on github, written by Gilberto Ramos
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(macString.getBytes("UTF-8"), 0, macString.length());
            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha1;
    }

    public long calculateMacInLong(String macString){
        int index = 0;
        int shift = 40;
        long raw = 0;

        while (shift >= 0) {
            int digit1 = Character.digit(macString.charAt(index++), 16);
            int digit2 = Character.digit(macString.charAt(index++), 16);
            if ((digit1 < 0) || (digit2 < 0))
                throw new IllegalArgumentException(FORMAT_ERROR + macString);
            raw |= ((long) (digit1 << 4 | digit2)) << shift;

            if (shift == 0) {
                break;
            }

            // Iterate over separators
            if (macString.charAt(index++) != SEPARATOR) {
                throw new IllegalArgumentException(FORMAT_ERROR + macString +
                        " (inconsistent separators");
            }

            shift -= 8;
        }
        return raw;
    }

    public byte[] calculateMacInBytes(String macString){
        byte[] macBytes = new byte[6];

        for (int i = 0; i < MAC_STRING_LENGTH; i += 3){
            macBytes[i/3] = (byte) ((Character.digit(macString.charAt(i), 16) << 4)
            + Character.digit(macString.charAt(i+1), 16));
        }

        return macBytes;
    }
}
