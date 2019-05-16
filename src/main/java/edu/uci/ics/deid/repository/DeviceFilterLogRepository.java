package edu.uci.ics.deid.repository;

import edu.uci.ics.deid.model.MacAddress;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class DeviceFilterLogRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void addLog(Timestamp time, MacAddress mac, String reasons){
        logger.debug("Adding to Device Filter Log" );
        jdbcTemplate.update(
                "INSERT INTO device_filter_log (time, dev_mac, reasons) VALUES (?, ?, ?)",
                time,
                mac.getMacAddrLong(),
                reasons
        );
    }
}
