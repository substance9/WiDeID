package edu.uci.ics.deid.repository.mapper;

import org.springframework.jdbc.core.RowMapper;

import edu.uci.ics.deid.model.MacAddress;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OptoutDeviceRowMapper implements RowMapper
{
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        MacAddress macAddr = new MacAddress(rs.getLong("dev_mac"));
        return macAddr;
    }

}