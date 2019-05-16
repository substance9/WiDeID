package edu.uci.ics.deid.repository.mapper;

import edu.uci.ics.deid.model.entity.Building;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BuildingRowMapper implements RowMapper
{
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        Building building = new Building();
        building.setId(rs.getInt("id"));
        building.setName(rs.getString("name"));
        return building;
    }

}