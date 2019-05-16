package edu.uci.ics.deid.repository;

import edu.uci.ics.deid.model.entity.Building;
import edu.uci.ics.deid.model.entity.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import edu.uci.ics.deid.repository.mapper.ConsumerRowMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Repository
public class ConsumerRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Consumer getById(int id) {
        logger.debug("Try Looking for Consumer with ID: " + String.valueOf(id));
        Consumer consumer = null;
        try {
            consumer = (Consumer) jdbcTemplate.queryForObject("SELECT id, 'name', method, uri FROM consumer WHERE id = ?",
                    new Object[]{id},
                    new ConsumerRowMapper());
        } catch (EmptyResultDataAccessException e){
            logger.debug("No Consumer Data in DB");
        }

        return consumer;
    }

    public List<Integer> getConsumerIdsByBuilding(Building building){
        String sql = "SELECT consumer_id  FROM request WHERE building_id = " + String.valueOf(building.getId());

        List<Integer> consumerIds = new ArrayList<>();

        List<Map<String,Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map row : rows) {
            consumerIds.add((int) (row.get("consumer_id")));
        }

        return consumerIds;
    }

    public List<Consumer> getAll(){
        String sql = "SELECT id, name, method, uri FROM consumer";
        List<Consumer> consumers = new ArrayList<>();
        List<Map<String,Object>> rows = jdbcTemplate.queryForList(sql);
        for (Map row : rows) {
            Consumer consumer = new Consumer();
            consumer.setId((int)row.get("id"));
            consumer.setName((String)row.get("name"));
            consumer.setMethod((String)row.get("method"));
            consumer.setUri((String)row.get("uri"));
            consumers.add(consumer);
        }

        return consumers;
    }

}
