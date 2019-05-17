package edu.uci.ics.deid.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RestController;

import edu.uci.ics.deid.model.MacAddress;
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
    public ResponseEntity<?> update(@RequestBody List<String> macStrs, @CookieValue(value="ucinetid_auth", defaultValue="") String uciAuthCookieValue) {

        String operator = authController.getOperator(uciAuthCookieValue);
        if (operator.equals("")){
            return new ResponseEntity<String>("No UCI Session Detected",HttpStatus.UNAUTHORIZED);
        }

        Boolean isAuthorized = authController.isAuthorizedUser(operator);
        if (isAuthorized == false){
            return new ResponseEntity<Error>(HttpStatus.UNAUTHORIZED);
        }
        OptoutResponseBody responseBody = new OptoutResponseBody();

        MacAddress newMac = null;
        MacAddress exitstedMac = null;

        for (int i = 0; i < macStrs.size(); i++){
            logger.debug("mac: " + macStrs.get(i));
            newMac = new MacAddress(macStrs.get(i));

            // if (newMac == null){
            //     logger.error("Cannot Parse MAC: " + macStrs.get(i));
            //     continue;
            // }

            //First check if existed
            exitstedMac = null;
            exitstedMac = optoutDevRepo.getByMacLong(newMac.getMacAddrLong());

            if(exitstedMac != null){
                logger.debug("Duplicated Optout Device Detected: " + exitstedMac.getMacAddrStr());
                responseBody.addToExistedList(exitstedMac.getMacAddrStr());
                continue;
            }

            int ret = optoutDevRepo.addMac(newMac);

            if (ret == 1){
                // Log the operation
                Timestamp now = new Timestamp(System.currentTimeMillis());
                optoutLogRepo.addLog(newMac, operator, now, "INSERT");

                // Generate HTTP success response
                responseBody.addToSuccessList(newMac.getMacAddrStr());
            }else{
                responseBody.addToFailedList(newMac.getMacAddrStr());
            }
        }
        return new ResponseEntity<OptoutResponseBody>(responseBody,HttpStatus.OK);
    }

    private class OptoutResponseBody{
        private List<String> successInsertionList;
        private List<String> existedList;
        private List<String> failedList;

        public OptoutResponseBody(){
            successInsertionList = new ArrayList<String>();
            existedList = new ArrayList<String>();
            failedList = new ArrayList<String>();
        }

        public void addToSuccessList(String macStr){
            successInsertionList.add(macStr);
        }

        public void addToExistedList(String macStr){
            existedList.add(macStr);
        }

        public void addToFailedList(String macStr){
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