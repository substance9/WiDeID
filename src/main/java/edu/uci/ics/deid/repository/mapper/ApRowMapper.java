package edu.uci.ics.deid.repository.mapper;

import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.entity.Ap;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ApRowMapper implements RowMapper
{
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        Ap ap = new Ap();
        ap.setMacAddress(new MacAddress(rs.getLong("mac")));
        ap.setName(rs.getString("name"));
        ap.setBuildingId(rs.getInt("building_id"));
        return ap;
    }

}