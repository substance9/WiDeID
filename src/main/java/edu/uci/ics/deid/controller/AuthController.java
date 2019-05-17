package edu.uci.ics.deid.controller;

import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.repository.AuthorizedUserRepository;
import edu.uci.ics.deid.repository.OptoutDeviceRepository;
import edu.uci.ics.deid.util.ParameterStringBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin()
@RequestMapping(value = "/auth")
public class AuthController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AuthorizedUserRepository authorizedUserRepo;
    
    @CrossOrigin()
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> auth(@CookieValue(value="ucinetid_auth", defaultValue="") String uciAuthCookieValue) {

        String operator = getOperator(uciAuthCookieValue);

        if (operator.equals("")){
            return new ResponseEntity<Error>(HttpStatus.UNAUTHORIZED);
        }

        Boolean isAuthorized = isAuthorizedUser(operator);

        if (isAuthorized == false){
            return new ResponseEntity<Error>(HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public Boolean isAuthorizedUser(String uciNetId){
        return authorizedUserRepo.isUciNetIdAuthorized(uciNetId);
    }

    public String getOperator(String uciAuthCookieValue){
        if (uciAuthCookieValue.length()<8){
            return "";
        }
        HttpURLConnection con = null;
        try {
            URL url = new URL("http://login.uci.edu/ucinetid/webauth_check");

            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
        } catch (Exception e){
            e.printStackTrace();
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("ucinetid_auth", uciAuthCookieValue);

        con.setDoOutput(true);
        try {
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
            out.flush();
            out.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        Map<String, String> authReturn = new HashMap<String, String>();

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String [] lineArr = inputLine.split("=",2);
                if (lineArr.length > 1) {
                    authReturn.put(lineArr[0], lineArr[1]);
                }
            }
            in.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        con.disconnect();

        return authReturn.get("ucinetid");
    }


}