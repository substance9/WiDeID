package edu.uci.ics.deid.repository;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.entity.CurrentSalt;
import edu.uci.ics.deid.repository.mapper.CurrentSaltRowMapper;

@Repository
public class CurrentSaltRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CurrentSalt getByMac(MacAddress clientMac) {
        logger.debug("Try Looking for Device in DB: " + clientMac.getMacAddrStr());
        CurrentSalt currSalt = null;
        try {
            currSalt = (CurrentSalt) jdbcTemplate.queryForObject(
                    "SELECT dev_mac, salt, last_change_time FROM current_salt WHERE dev_mac = ?",
                    new Object[] { clientMac.getMacAddrLong() }, new CurrentSaltRowMapper());
        } catch (EmptyResultDataAccessException e) {
            logger.debug("No Device Data in Current Salt Table");
            return null;
        }

        return currSalt;
    }

    public void updateCurrentSalt(CurrentSalt currSalt) {
        String saltStr = currSalt.getSaltStr();
        Long devMac = currSalt.getMacAddress().getMacAddrLong();
        Timestamp lastChangeTime = currSalt.getLastChangeTime();

        // TODO: What happens when update fails 
        jdbcTemplate.update("UPDATE current_salt set salt = ? , last_change_time = ? where dev_mac = ?", saltStr, lastChangeTime, devMac);

    }

    public void insertNewCurrentSaltEntry(CurrentSalt currSalt) {
        Long mac = currSalt.getMacAddress().getMacAddrLong();

        String saltStr = currSalt.getSaltStr();
        Timestamp lastChangeTime = currSalt.getLastChangeTime();

        // TODO: Add Exception Handling - What happens when insertion fails, 
        jdbcTemplate.update("INSERT INTO current_salt (dev_mac, salt, last_change_time) VALUES (?, ?, ?)", mac, saltStr, lastChangeTime);

    }
}
