package edu.uci.ics.deid.repository.mapper;

import edu.uci.ics.deid.model.entity.Consumer;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsumerRowMapper implements RowMapper
{
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        Consumer consumer = new Consumer();
        consumer.setId(rs.getInt("id"));
        consumer.setName(rs.getString("name"));
        consumer.setMethod(rs.getString("method"));
        consumer.setUri(rs.getString("uri"));
        return consumer;
    }

}