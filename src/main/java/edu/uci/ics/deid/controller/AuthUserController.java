package edu.uci.ics.deid.controller;

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

import edu.uci.ics.deid.model.entity.AuthorizedUser;
import edu.uci.ics.deid.repository.AuthorizedUserRepository;

@RestController
@CrossOrigin()
@RequestMapping(value = "/auth_user")
public class AuthUserController {

    @Autowired
    AuthController authController;

    @Autowired
    AuthorizedUserRepository authorizedUserRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @CrossOrigin()
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseEntity<?> update(@RequestBody AuthorizedUser newUser,
            @CookieValue(value = "ucinetid_auth", defaultValue = "") String uciAuthCookieValue) {

        String operator = authController.getOperator(uciAuthCookieValue);
        if (operator.equals("")) {
            return new ResponseEntity<String>("No UCI Session Detected", HttpStatus.UNAUTHORIZED);
        }

        Boolean isAuthorized = authController.isAdmin(operator);
        if (isAuthorized == false) {
            return new ResponseEntity<Error>(HttpStatus.UNAUTHORIZED);
        }

        // First check if existed


        if (authController.isAuthorizedUser(newUser.getUcinetid())) {
            logger.debug("This user is already an authorized user: " + newUser.getUcinetid());
            return new ResponseEntity<>(HttpStatus.OK);
        }

        int ret = authorizedUserRepository.addAuthorizedUser(newUser);

        if (ret == 1) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<Error>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @CrossOrigin()
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<AuthorizedUser>> get(
            @CookieValue(value = "ucinetid_auth", defaultValue = "") String uciAuthCookieValue) {

        List<AuthorizedUser> authUserList= null;

        String operator = authController.getOperator(uciAuthCookieValue);
        if (operator.equals("")) {
            return new ResponseEntity<List<AuthorizedUser>>(authUserList, HttpStatus.UNAUTHORIZED);
        }

        Boolean isAuthorized = authController.isAdmin(operator);
        if (isAuthorized == false) {
            return new ResponseEntity<List<AuthorizedUser>>(authUserList, HttpStatus.UNAUTHORIZED);
        }

        authUserList = authorizedUserRepository.getAuthorizedUsers();

        return new ResponseEntity<List<AuthorizedUser>>(authUserList, HttpStatus.OK);
    }

}