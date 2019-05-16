package edu.uci.ics.deid.repository;

import java.sql.PreparedStatement;
import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Repository
public class DeviceRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    // public Device getByMac(MacAddress clientMac) {
    //     logger.debug("Try Looking for Device: " + clientMac.getMacAddrStr());
    //     Device dev = null;
    //     try {
    //         dev = (Device) jdbcTemplate.queryForObject("SELECT mac, initial_id, enc_key, current_salt, participation FROM device WHERE mac = ?",
    //                 new Object[]{clientMac.getMacAddrLong()},
    //                 new DeviceRowMapper());
    //     } catch (EmptyResultDataAccessException e){
    //         logger.debug("No Device Data in DB");
    //     }

    //     return dev;
    // }

    // public List<Device> getNonPariticipatingDevices(){
    //     String sql = "SELECT mac, initial_id, enc_key, current_salt, participation FROM device WHERE participation = 1";

    //     List<Device> devices = new ArrayList<Device>();

    //     List<Map<String,Object>> rows = jdbcTemplate.queryForList(sql);
    //     for (Map row : rows) {
    //         Device device = new Device();
    //         device.setMacAddress(new MacAddress((Long) (row.get("mac"))));
    //         device.setCurrentSaltStr((String)row.get("current_salt"));
    //         device.setEncKeyStr((String)row.get("enc_key"));
    //         device.setParticipation((boolean)row.get("participation"));
    //         devices.add(device);
    //     }

    //     return devices;
    // }

    // public int[] updateDevicesSalt(List<Device> devs){
    //     int[] updateCounts = jdbcTemplate.batchUpdate(
    //             "UPDATE device set current_salt = ? where mac = ?",
    //             new BatchPreparedStatementSetter() {
    //                 @Override
    //                 public void setValues(PreparedStatement ps, int i) throws SQLException {
    //                     ps.setString(1, devs.get(i).getCurrentSaltStr());
    //                     ps.setLong(2, devs.get(i).getMacAddress().getMacAddrLong());
    //                 }

    //                 @Override
    //                 public int getBatchSize() {
    //                     return devs.size();
    //                 }
    //             } );

    //     return updateCounts;
    // }

    // public void updateDeviceParticipation(Device dev, boolean participation){
    //     jdbcTemplate.update(
    //             "UPDATE device set participation = ? where mac = ?",
    //              participation, dev.getMacAddress().getMacAddrLong());

    // }

    // public void updateDeviceSalt(Device dev, String saltStr){
    //     jdbcTemplate.update(
    //             "UPDATE device set current_salt = ? where mac = ?",
    //             saltStr, dev.getMacAddress().getMacAddrLong());

    // }

    // public void updateDeviceKey(Device dev, String key){
    //     jdbcTemplate.update(
    //             "UPDATE device set enc_key = ? where mac = ?",
    //             key, dev.getMacAddress().getMacAddrLong());

    // }

    // public Device addWithDefaultPolicy(MacAddress clientMac) {
    //     Long mac = clientMac.getMacAddrLong();

    //     String initialId = clientMac.getInitHashId();

    //     boolean participation = true;

    //     byte[] salt = HashManager.generateSalt();
    //     String saltStr = DatatypeConverter.printHexBinary(salt);

    //     SecretKey secretKey = EncryptionManager.generateKey();


    //     Device dev = new Device();
    //     clientMac.setInitHashId();
    //     dev.setMacAddress(clientMac);
    //     dev.setCurrentSalt(salt);
    //     dev.setEncKey(secretKey);
    //     dev.setParticipation(participation);

    //     // get base64 encoded version of the key
    //     String keyStr = dev.getEncKeyStr();

    //     jdbcTemplate.update(
    //             "INSERT INTO device (mac, initial_id, enc_key, current_salt, participation) VALUES (?, ?, ?, ?, ?)",
    //             mac, initialId, keyStr, saltStr, participation
    //     );



    //     return dev;
    // }
}
