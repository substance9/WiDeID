package edu.uci.ics.deid.repository;

import java.sql.Timestamp;
import java.util.Base64;

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
public class HistoricalSaltRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // TODO: Change to queryForList
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

    public void updateHistoricalSalt(byte[] lastSaltInByte, MacAddress macAddress, Timestamp now) {
        String lastSaltStr = Base64.getEncoder().encodeToString(lastSaltInByte);
        Long devMac = macAddress.getMacAddrLong();
        Timestamp lastEndTime = now;

        // TODO: What happens when update fails 
        jdbcTemplate.update("UPDATE historical_salt set end_time = ? where dev_mac = ? AND salt = ?", lastEndTime, devMac, lastSaltStr);
    }

    public void insertNewHistoricalSaltEntryFromCurrentSalt(CurrentSalt currentSalt) {
        Long mac = currentSalt.getMacAddress().getMacAddrLong();

        String saltStr = currentSalt.getSaltStr();
        Timestamp startTime = currentSalt.getLastChangeTime();
        Timestamp endTime = null;


        // TODO: Add Exception Handling - What happens when insertion fails, 
        jdbcTemplate.update("INSERT INTO historical_salt (dev_mac, salt, start_time, end_time) VALUES (?, ?, ?, ?)", mac, saltStr, startTime, endTime);

    }
}
