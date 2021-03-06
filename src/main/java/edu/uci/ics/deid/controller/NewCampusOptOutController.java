package edu.uci.ics.deid.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.entity.OptoutChangeLog;
import edu.uci.ics.deid.repository.OptoutDeviceChangeHistoryRepository;
import edu.uci.ics.deid.repository.OptoutDeviceRepository;

@RestController
@CrossOrigin()
@RequestMapping(value = "/optout_new")
public class NewCampusOptOutController {

    @Autowired
    AuthController authController;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    OptoutDeviceRepository optoutDevRepo;

    @Autowired
    OptoutDeviceChangeHistoryRepository optoutLogRepo;

    @CrossOrigin()
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> update(@RequestBody List<String> macStrs,
            @CookieValue(value = "ucinetid_auth", defaultValue = "") String uciAuthCookieValue) {

        String operator = authController.getOperator(uciAuthCookieValue);
        if (operator.equals("")) {
            return new ResponseEntity<String>("No UCI Session Detected", HttpStatus.UNAUTHORIZED);
        }

        Boolean isAuthorized = (authController.isAuthorizedUser(operator) || authController.isAdmin(operator));
        if (isAuthorized == false) {
            return new ResponseEntity<Error>(HttpStatus.UNAUTHORIZED);
        }
        OptoutResponseBody responseBody = new OptoutResponseBody();

        MacAddress newMac = null;
        MacAddress exitstedMac = null;

        for (int i = 0; i < macStrs.size(); i++) {
            logger.debug("mac: " + macStrs.get(i));
            newMac = new MacAddress(macStrs.get(i));

            // if (newMac == null){
            // logger.error("Cannot Parse MAC: " + macStrs.get(i));
            // continue;
            // }

            // First check if existed
            exitstedMac = null;
            exitstedMac = optoutDevRepo.getByMacLong(newMac.getMacAddrLong());

            if (exitstedMac != null) {
                logger.debug("Duplicated Optout Device Detected: " + exitstedMac.getMacAddrStr());
                responseBody.addToExistedList(exitstedMac.getMacAddrStr());
                continue;
            }

            int ret = optoutDevRepo.addMac(newMac);

            if (ret == 1) {
                // Log the operation
                Timestamp now = new Timestamp(System.currentTimeMillis());
                optoutLogRepo.addLog(newMac, operator, now, "INSERT");

                // Generate HTTP success response
                responseBody.addToSuccessList(newMac.getMacAddrStr());
            } else {
                responseBody.addToFailedList(newMac.getMacAddrStr());
            }
        }
        return new ResponseEntity<OptoutResponseBody>(responseBody, HttpStatus.OK);
    }

    @CrossOrigin()
    @RequestMapping(value = "/log", method = RequestMethod.GET)
    public ResponseEntity<List<OptoutLogResponse>> getLogs(
            @CookieValue(value = "ucinetid_auth", defaultValue = "") String uciAuthCookieValue) {

        List<OptoutLogResponse> logsReponseList = new ArrayList<OptoutLogResponse>();

        String operator = authController.getOperator(uciAuthCookieValue);
        if (operator.equals("")) {
            return new ResponseEntity<List<OptoutLogResponse>>(logsReponseList, HttpStatus.UNAUTHORIZED);
        }

        Boolean isAuthorized = authController.isAdmin(operator);
        if (isAuthorized == false) {
            return new ResponseEntity<List<OptoutLogResponse>>(logsReponseList, HttpStatus.UNAUTHORIZED);
        }

        logger.debug("authorized admin");

        List<OptoutChangeLog> logsList = optoutLogRepo.getLogs();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        for (int i = 0; i < logsList.size(); i++) {
            OptoutChangeLog log = logsList.get(i);
            OptoutLogResponse logResponse = new OptoutLogResponse();
            logResponse.setMacStr(log.getDevMac().getMacAddrStr());
            logResponse.setOperator(log.getOperator());
            logResponse.setAction(log.getAction());
            Date date = new Date();
            date.setTime(log.getTime().getTime());
            logResponse.setTimeStr(sdf.format(date));

            logsReponseList.add(logResponse);
        }

        return new ResponseEntity<List<OptoutLogResponse>>(logsReponseList, HttpStatus.OK);
    }

    @CrossOrigin()
    @RequestMapping(value = "/dev_status", method = RequestMethod.GET)
    public @ResponseBody String getDevStatus(
            @CookieValue(value = "ucinetid_auth", defaultValue = "") String uciAuthCookieValue,
            @RequestParam(value = "mac_addr") String macString) {

        MacAddress inputMacAddr = new MacAddress(macString);

        MacAddress returnMacAddr = optoutDevRepo.getByMacLong(inputMacAddr.getMacAddrLong());

        String retString = "1";

        if (returnMacAddr == null) {
            retString = "0";
        }

        return retString;
    }

    @CrossOrigin()
    @RequestMapping(value = "/all_reg_devs_by_ucinetid", method = RequestMethod.GET)
    public @ResponseBody String getAllDevicesByUciId(
            @CookieValue(value = "ucinetid_auth", defaultValue = "") String uciAuthCookieValue,
            @RequestParam(value = "ucinetid") String ucinetid) {

        RestTemplate restTemplate = new RestTemplate();

        String reqString = "https://apps.oit.uci.edu/mobileaccess/splunk/lookup.php?ucinetid=".concat(ucinetid);
        ResponseEntity<String> response = restTemplate.getForEntity(reqString, String.class);

        String retString = "";

        if (HttpStatus.OK == response.getStatusCode()) {
            String macStrs = response.getBody();
            macStrs = macStrs.toUpperCase();
            for (String macStrRaw : macStrs.split(",")) {
                macStrRaw = macStrRaw.trim();
                String macStr = macStrRaw.replaceAll("..(?!$)", "$0 ");
                retString = retString.concat(macStr);
                retString = retString.concat("\n");
            }
            retString = retString.substring(0, retString.length() - 1);
        } else {
            retString = "0";
        }

        return retString;
    }

    @CrossOrigin()
    @RequestMapping(value = "/self_optout_add", method = RequestMethod.GET)
    public @ResponseBody String addSelfOptoutDev(
            @CookieValue(value = "ucinetid_auth", defaultValue = "") String uciAuthCookieValue,
            @RequestParam(value = "ucinetid") String ucinetid, @RequestParam(value = "mac_addr") String macString) {

        MacAddress newMac = new MacAddress(macString);
        String operator = ucinetid;

        MacAddress exitstedMac = null;

        exitstedMac = optoutDevRepo.getByMacLong(newMac.getMacAddrLong());

        String retString = "0";

        if (exitstedMac != null) {
            logger.debug("Duplicated Optout Device Detected: " + exitstedMac.getMacAddrStr());
            retString = "1";
            return retString;
        }

        int ret = optoutDevRepo.addMac(newMac);

        if (ret == 1) {
            // Log the operation
            Timestamp now = new Timestamp(System.currentTimeMillis());
            optoutLogRepo.addLog(newMac, operator, now, "INSERT");
            // Generate HTTP success response
            retString = "1";
        } else {
            retString = "0";
        }

        return retString;
    }

    @CrossOrigin()
    @RequestMapping(value = "/self_optout_remove", method = RequestMethod.GET)
    public @ResponseBody String removeSelfOptoutDev(
            @CookieValue(value = "ucinetid_auth", defaultValue = "") String uciAuthCookieValue,
            @RequestParam(value = "ucinetid") String ucinetid, @RequestParam(value = "mac_addr") String macString) {

        MacAddress newMac = new MacAddress(macString);
        String operator = ucinetid;

        MacAddress exitstedMac = null;

        exitstedMac = optoutDevRepo.getByMacLong(newMac.getMacAddrLong());

        String retString = "0";

        if (exitstedMac == null) {
            logger.debug("Optout device not existed: " + exitstedMac.getMacAddrStr());
            retString = "1";
            return retString;
        }

        //has optout entry for the mac
        int ret = optoutDevRepo.removeMac(newMac);

        if (ret == 1) {
            // Log the operation
            Timestamp now = new Timestamp(System.currentTimeMillis());
            optoutLogRepo.addLog(newMac, operator, now, "DELETE");
            // Generate HTTP success response
            retString = "1";
        } else {
            retString = "0";
        }

        return retString;
    }

    private class OptoutLogResponse {
        private String macStr;
        private String operator;
        private String timeStr;
        private String action;

        /**
         * @return the macStr
         */
        public String getMacStr() {
            return macStr;
        }

        /**
         * @param macStr the macStr to set
         */
        public void setMacStr(String macStr) {
            this.macStr = macStr;
        }

        /**
         * @return the operator
         */
        public String getOperator() {
            return operator;
        }

        /**
         * @param operator the operator to set
         */
        public void setOperator(String operator) {
            this.operator = operator;
        }

        /**
         * @return the timeStr
         */
        public String getTimeStr() {
            return timeStr;
        }

        /**
         * @param timeStr the timeStr to set
         */
        public void setTimeStr(String timeStr) {
            this.timeStr = timeStr;
        }

        /**
         * @return the action
         */
        public String getAction() {
            return action;
        }

        /**
         * @param action the action to set
         */
        public void setAction(String action) {
            this.action = action;
        }
    }

    private class OptoutResponseBody {
        private List<String> successInsertionList;
        private List<String> existedList;
        private List<String> failedList;

        public OptoutResponseBody() {
            successInsertionList = new ArrayList<String>();
            existedList = new ArrayList<String>();
            failedList = new ArrayList<String>();
        }

        public void addToSuccessList(String macStr) {
            successInsertionList.add(macStr);
        }

        public void addToExistedList(String macStr) {
            existedList.add(macStr);
        }

        public void addToFailedList(String macStr) {
            failedList.add(macStr);
        }

        /**
         * @return the successInsertionList
         */
        public List<String> getSuccessInsertionList() {
            return successInsertionList;
        }

        /**
         * @param successInsertionList the successInsertionList to set
         */
        public void setSuccessInsertionList(List<String> successInsertionList) {
            this.successInsertionList = successInsertionList;
        }

        /**
         * @return the existedList
         */
        public List<String> getExistedList() {
            return existedList;
        }

        /**
         * @param existedList the existedList to set
         */
        public void setExistedList(List<String> existedList) {
            this.existedList = existedList;
        }

        /**
         * @return the failedList
         */
        public List<String> getFailedList() {
            return failedList;
        }

        /**
         * @param failedList the failedList to set
         */
        public void setFailedList(List<String> failedList) {
            this.failedList = failedList;
        }

    }

}