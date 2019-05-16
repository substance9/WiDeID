package edu.uci.ics.deid.service.deidengine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;

import edu.uci.ics.deid.cache.LRUCache;
import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.RawConnectionEvent;
import edu.uci.ics.deid.model.entity.CurrentSalt;
import edu.uci.ics.deid.repository.CurrentSaltRepository;
import edu.uci.ics.deid.repository.HistoricalSaltRepository;

@Component
public class SaltManager {
    // Salt Change Interval in miliseconds
    @Value("${deid_engine.salt_manager.salt_changing_interval}")
    private Long saltChangeInterval;

    @Value("${deid_engine.salt_manager.cache_size}")
    private Integer cacheSize;
    
    @Autowired
    CurrentSaltRepository currentSaltRepo;

    @Autowired
    HistoricalSaltRepository historicalSaltRepo;

    @Autowired
    HashOperator hashOperator;

    // Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // LRU Cache for salt
    LRUCache<Long, SaltCacheItem> devSaltCache;

    public class SaltCacheItem {
        public byte[] currentSalt;
        public Timestamp lastChangeTime;
    }

    public SaltManager() {
        
    }

    @PostConstruct
    private void init() {
        devSaltCache = new LRUCache<Long, SaltCacheItem>(cacheSize);
    }

    public byte[] getSalt(RawConnectionEvent rawEvt) {
        MacAddress devMac = rawEvt.getClientMac();
        SaltCacheItem saltCacheItem = devSaltCache.get(devMac.getMacAddrLong());
        long nowEpoch = System.currentTimeMillis();
        Timestamp now = new Timestamp(nowEpoch);

        CurrentSalt newSaltEntity = null;

        byte[] retSalt = null;
        if (saltCacheItem != null) {
            // Found Cache

            if (!isExpired(saltCacheItem.lastChangeTime, now)) {
                // The salt is still valid
                retSalt = saltCacheItem.currentSalt;
            } else {
                // The salt is expired
                // 1. Generate new Salt, 2. Update in DB 3. Update in cache
                newSaltEntity = generateNewSaltForMac(devMac, now);
                currentSaltRepo.updateCurrentSalt(newSaltEntity);
                historicalSaltRepo.updateHistoricalSalt(saltCacheItem.currentSalt, devMac, now);
                historicalSaltRepo.insertNewHistoricalSaltEntryFromCurrentSalt(newSaltEntity);
                updateSaltCache(devMac, newSaltEntity.getSalt(), now);

                retSalt = newSaltEntity.getSalt();
            }
        } else {
            // Cache Missing

            CurrentSalt currentSaltFromDB = currentSaltRepo.getByMac(devMac);
            if (currentSaltFromDB == null) {
                // There is also NO salt information in DB for the device
                // This is the first time the system sees the device
                // Generate salt for the device
                newSaltEntity = generateNewSaltForMac(devMac, now);
                currentSaltRepo.insertNewCurrentSaltEntry(newSaltEntity);
                historicalSaltRepo.insertNewHistoricalSaltEntryFromCurrentSalt(newSaltEntity);
                updateSaltCache(devMac, newSaltEntity.getSalt(), now);

                retSalt = newSaltEntity.getSalt();

            } else {
                // There is salt information in DB
                // Check if the salt information is expired
                if (!isExpired(currentSaltFromDB.getLastChangeTime(), now)) {
                    // Not Expired: Set in the cache and return
                    byte[] newSalt = hashOperator.getNewRandomSalt();
                    updateSaltCache(devMac, newSalt, now);
                    retSalt = newSalt;
                } else {
                    // Expired:
                    // 1. Generate new Salt, 2. Update in DB 3. Update in cache
                    newSaltEntity = generateNewSaltForMac(devMac, now);
                    currentSaltRepo.updateCurrentSalt(newSaltEntity);
                    historicalSaltRepo.updateHistoricalSalt(currentSaltFromDB.getSalt(), devMac, now);
                    historicalSaltRepo.insertNewHistoricalSaltEntryFromCurrentSalt(newSaltEntity);
                    updateSaltCache(devMac, newSaltEntity.getSalt(), now);

                    retSalt = newSaltEntity.getSalt();
                }
            }
        }

        return retSalt;
    }

    public void updateSaltCache(MacAddress macAddr, byte[] salt, Timestamp now) {
        SaltCacheItem cacheValue = new SaltCacheItem();
        cacheValue.currentSalt = salt;
        cacheValue.lastChangeTime = now;
        devSaltCache.set(macAddr.getMacAddrLong(), cacheValue);
    }

    public CurrentSalt generateNewSaltForMac(MacAddress macAddr, Timestamp now) {
        // Generate new salt byte[]
        byte[] newSalt = hashOperator.getNewRandomSalt();

        CurrentSalt newSaltEntity = new CurrentSalt();
        newSaltEntity.setMacAddress(macAddr);
        newSaltEntity.setSalt(newSalt);
        newSaltEntity.setLastChangeTime(now);

        return newSaltEntity;
    }

    public boolean isExpired(Timestamp target, Timestamp now) {
        if (target.getTime() + this.saltChangeInterval < now.getTime()) {
            return true;
        } else {
            return false;
        }
    }
}