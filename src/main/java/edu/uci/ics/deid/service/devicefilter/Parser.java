package edu.uci.ics.deid.service.devicefilter;

import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Properties;

import edu.uci.ics.deid.model.RawConnectionEvent;
import org.springframework.util.StringUtils;


@Component
public class Parser {

    //Properties
    private static String PARSER_PROPERTIES_FILE = "parser.properties";
    private static Properties props;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //Parsing hash map
    private static HashMap<String, String> parsingMap = new HashMap<String, String>();

    public Parser(){
        // Read properties file
        InputStream propStream = Parser.class.getClassLoader().getResourceAsStream(PARSER_PROPERTIES_FILE);
        props = new Properties();
        try {
            props.load(propStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    private void init(){
        // Populate parsing hash map
        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            parsingMap.put(key, value);
        }
    }

    public RawConnectionEvent parse(String evtStr){
        RawConnectionEvent rawEvt = new RawConnectionEvent();
        HashMap<String, String> rawDict = new HashMap<String, String>();

        // First split the event string line into different attribute pieces. The splitter is tab character.
        String[] evtStrArray = evtStr.split("\t");
//        for (String s : evtStrArray){
//            logger.debug(s + '\n');
//        }

        // Iterate through all attribute pieces, each attribute is a string that represents key value pair connected by "="
        // keyValStr example:  SNMPv2-SMI::enterprises.14179.2.6.2.36.0 = INTEGER: 0
        for (String keyValStr : evtStrArray){
            keyValStr = keyValStr.trim();

            // Split each key value pair
            String[] keyValPairArray = keyValStr.split("=", 2);

            // Check the availability of this attribute
            if (keyValPairArray.length == 2){
                rawDict.put(keyValPairArray[0].trim(), keyValPairArray[1].trim());
            } else if (keyValPairArray.length == 1) {
                rawDict.put(keyValPairArray[0].trim(), "");
            } else {
                logger.error("number of parts (divided by =) for section is wrong: ");
                logger.error(evtStr);
                return null;
            }
        }

        if (!rawDict.containsKey(parsingMap.get("apId"))){
            logger.error("Cannot Find SNMP key for apId");
            return null;
        }
        if (!rawDict.containsKey(parsingMap.get("apMac"))){
            logger.error("Cannot Find SNMP key for apMac");
            return null;
        }
        if (!rawDict.containsKey(parsingMap.get("clientMac"))){
            logger.error("Cannot Find SNMP key for clientMac");
            return null;
        }
        

        String apIdRaw = rawDict.get(parsingMap.get("apId"));
        String apIdParsed = StringUtils.trimTrailingCharacter(
                            StringUtils.trimLeadingCharacter(
                            apIdRaw.split(":")[1].trim(),
                                    '\"'),
                                    '\"');
        rawEvt.setApId(apIdParsed);

        String apMacRaw = rawDict.get(parsingMap.get("apMac"));
        String apMacParsed = apMacRaw.split(":")[1].trim();
        rawEvt.setApMacWithStr(apMacParsed);

        String clientMacRaw = rawDict.get(parsingMap.get("clientMac"));
        String clientMacParsed = clientMacRaw.split(":")[1].trim();
        rawEvt.setClientMacWithStr(clientMacParsed);
        rawEvt.getClientMac().setInitHashId();

        return rawEvt;
    }
}