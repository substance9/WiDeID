package edu.uci.ics.deid.repository.mapper;

import edu.uci.ics.deid.model.entity.AuthorizedUser;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthorizedUserMapper implements RowMapper
{
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuthorizedUser aUser = new AuthorizedUser();
        aUser.setUcinetid(rs.getString("ucinetid"));
        aUser.setName(rs.getString("name"));
        return aUser;
    }

}