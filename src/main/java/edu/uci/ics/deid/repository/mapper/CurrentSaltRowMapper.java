package edu.uci.ics.deid.repository.mapper;

import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.entity.CurrentSalt;
import edu.uci.ics.deid.model.entity.Device;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrentSaltRowMapper implements RowMapper
{
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        CurrentSalt currSalt = new CurrentSalt();
        currSalt.setMacAddress(new MacAddress(rs.getLong("dev_mac")));
        currSalt.setSaltStr(rs.getString("salt"));
        currSalt.setLastChangeTime(rs.getTimestamp("last_change_time"));
        return currSalt;
    }

}