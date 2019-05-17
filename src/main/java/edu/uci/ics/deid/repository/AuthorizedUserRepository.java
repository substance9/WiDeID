package edu.uci.ics.deid.repository;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.entity.AuthorizedUser;
import edu.uci.ics.deid.model.entity.CurrentSalt;
import edu.uci.ics.deid.repository.mapper.AuthorizedUserMapper;
import edu.uci.ics.deid.repository.mapper.CurrentSaltRowMapper;

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

    
}
