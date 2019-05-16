package edu.uci.ics.deid.repository.mapper;

import edu.uci.ics.deid.model.HistoryEntry;
import edu.uci.ics.deid.model.MacAddress;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HistoryEntryRowMapper implements RowMapper
{
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setEncryptedMac((rs.getString("encrypted_mac")));
        historyEntry.setEncryptedId(rs.getString("encrypted_id"));
        historyEntry.setStartTime(rs.getTimestamp("start_time"));
        historyEntry.setEndTime(rs.getTimestamp("participation"));
        return historyEntry;
    }

}