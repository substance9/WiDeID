package edu.uci.ics.deid.repository;

import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.entity.Ap;
import edu.uci.ics.deid.model.entity.Building;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import edu.uci.ics.deid.repository.mapper.ApRowMapper;


@Repository
public class ApRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    BuildingRepository buildingRepository;

    private final static int DefaultBuildingId = 1 ;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Ap getByMac(MacAddress apMac) {
        logger.debug("Try Looking for AP: " + apMac.getMacAddrStr());
        Ap ap = null;
        boolean successFlag = true;
        try {
            ap = (Ap) jdbcTemplate.queryForObject("SELECT mac, 'name', building_id FROM ap WHERE mac = ?",
                    new Object[]{apMac.getMacAddrLong()},
                    new ApRowMapper());
        } catch (EmptyResultDataAccessException e){
            logger.debug("No AP Data in DB");
            successFlag = false;
        }

        if(successFlag) {
            Building building = buildingRepository.getById(ap.getBuildingId());
            ap.setBuilding(building);
        }

        return ap;
    }

    // Test code
    public Ap addWithDefaultBuilding(MacAddress apMac, String apId) {
        Long mac = apMac.getMacAddrLong();

        Building defaultBuilding = buildingRepository.getById(DefaultBuildingId);

        jdbcTemplate.update(
                "INSERT INTO ap (mac, name, building_id) VALUES (?, ?, ?)",
                mac, apId, defaultBuilding.getId()
        );

        Ap ap = new Ap();
        ap.setMacAddress(apMac);
        ap.setBuilding(defaultBuilding);
        ap.setName(apId);
        ap.setBuildingId(defaultBuilding.getId());

        return ap;
    }


}
