package edu.uci.ics.deid.repository;

import edu.uci.ics.deid.model.entity.Building;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import edu.uci.ics.deid.repository.mapper.BuildingRowMapper;


@Repository
public class BuildingRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Building getById(int id) {
        logger.debug("Try Looking for Building with ID: " + String.valueOf(id));
        Building building = null;
        try {
            building = (Building) jdbcTemplate.queryForObject("SELECT id, 'name' FROM building WHERE id = ?",
                    new Object[]{id},
                    new BuildingRowMapper());
        } catch (EmptyResultDataAccessException e){
            logger.debug("No Building Data in DB");
        }

        return building;
    }
}
