package edu.uci.ics.deid.repository;

import edu.uci.ics.deid.model.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import edu.uci.ics.deid.repository.mapper.OptoutDeviceRowMapper;


@Repository
public class OptoutDeviceRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MacAddress getByMacLong(long macAddressLong) {
        logger.debug("Try Looking for Device in Opt-out Table with MAC address: " + String.valueOf(macAddressLong));
        MacAddress macAddr = null;
        try {
            macAddr = (MacAddress) jdbcTemplate.queryForObject("SELECT dev_mac FROM optout_device WHERE dev_mac = ?",
                    new Object[]{macAddressLong},
                    new OptoutDeviceRowMapper());
        } catch (EmptyResultDataAccessException e){
            logger.debug("Device is not in Opt-out table");
            return null;
        }

        return macAddr;
    }

    public int addMac(MacAddress mac){
        logger.debug("Try Insert MAC address: " + String.valueOf(mac.getMacAddrStr()) + " in Opt-out Table" );
        int ret = jdbcTemplate.update(
                "INSERT INTO optout_device (dev_mac, dev_mac_str) VALUES (?, ?)",
                mac.getMacAddrLong(),
                mac.getMacAddrStr()
        );

        return ret;
    }
}
