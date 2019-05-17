package edu.uci.ics.deid.repository;

import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.entity.OptoutChangeLog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class OptoutDeviceChangeHistoryRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public int addLog(MacAddress devMac, String operator, Timestamp time, String action){
        logger.debug("Logging the operation of opt-out device list: ");
        int ret = jdbcTemplate.update(
                "INSERT INTO optout_device_change_history (dev_mac, dev_mac_str, operator, time, action) VALUES (?, ?, ?, ?, ?)",
                devMac.getMacAddrLong(),
                devMac.getMacAddrStr(),
                operator,
                time,
                action
        );

        return ret;
    }

    public List<OptoutChangeLog> getLogs(){
        String sql = "SELECT dev_mac, operator, time, action FROM optout_device_change_history";

        List<OptoutChangeLog> logs = new ArrayList<OptoutChangeLog>();

        List<Map<String,Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map row : rows) {
            OptoutChangeLog log = new OptoutChangeLog();
            MacAddress mac = new MacAddress((Long)row.get("dev_mac"));
            log.setDevMac(mac);
            log.setOperator((String)row.get("operator"));
            log.setAction((String)row.get("action"));
            log.setTime((Timestamp)row.get("time"));
            logs.add(log);
        }

        return logs;
    }
}
