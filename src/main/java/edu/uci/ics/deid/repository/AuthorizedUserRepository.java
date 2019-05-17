package edu.uci.ics.deid.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.uci.ics.deid.model.entity.AuthorizedUser;
import edu.uci.ics.deid.repository.mapper.AuthorizedUserMapper;

@Repository
public class AuthorizedUserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Boolean isUciNetIdAuthorized(String uciNetId) {
        logger.debug("Try Looking for UCInetID in Authorized User DB: " + uciNetId);
        AuthorizedUser user = null;
        try {
            user = (AuthorizedUser) jdbcTemplate.queryForObject(
                    "SELECT ucinetid, name FROM authorized_user WHERE ucinetid = ?",
                    new Object[] { uciNetId }, new AuthorizedUserMapper());
        } catch (EmptyResultDataAccessException e) {
            logger.debug("User's UCI netid is not in authorized table");
            return false;
        }

        return true;
    }

    public Boolean isUciNetIdAdmin(String uciNetId) {
        logger.debug("Try Looking for UCInetID in admin DB: " + uciNetId);
        AuthorizedUser user = null;
        try {
            user = (AuthorizedUser) jdbcTemplate.queryForObject(
                    "SELECT ucinetid, name FROM admin WHERE ucinetid = ?",
                    new Object[] { uciNetId }, new AuthorizedUserMapper());
        } catch (EmptyResultDataAccessException e) {
            logger.debug("User's UCI netid is not in admin table");
            return false;
        }

        return true;
    }

    public int addAuthorizedUser(AuthorizedUser user){
        logger.debug("Try Add new Authorized user: " + user.getUcinetid() );
        int ret = jdbcTemplate.update(
                "INSERT INTO authorized_user (ucinetid, name, department) VALUES (?, ?, ?)",
                user.getUcinetid(),
                user.getName(),
                user.getDepartment()
        );

        return ret;
    }

    public List<AuthorizedUser> getAuthorizedUsers(){
        String sql = "SELECT ucinetid, name, department FROM authorized_user";

        List<AuthorizedUser> users = new ArrayList<AuthorizedUser>();

        List<Map<String,Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map row : rows) {
            AuthorizedUser user = new AuthorizedUser();
            user.setUcinetid((String)row.get("ucinetid"));
            user.setName((String)row.get("name"));
            user.setDepartment((String)row.get("department"));
            users.add(user);
        }

        return users;
    }
}
