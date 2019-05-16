package edu.uci.ics.deid.service.devicefilter.filter;

import java.sql.Timestamp;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.cache.LRUCache;
import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.RawConnectionEvent;
import edu.uci.ics.deid.repository.OptoutDeviceRepository;

@Component
public class OptoutDeviceFilter extends Filter implements Callable<Boolean> {
    @Value("${device_filter.optout_filter.filter_reason}")
    private String filterReason;

    @Value("${device_filter.optout_filter.cache_expiration_time}")
    private Long cacheExpirationIntervalMs;

    @Value("${device_filter.optout_filter.cache_size}")
    private Integer cacheSize;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private RawConnectionEvent eventForChecking;
    private LRUCache<Long, OptoutFilterCacheItem> devInfoCache;

    @Autowired
    OptoutDeviceRepository optoutDeviceRepo;

    public class OptoutFilterCacheItem {
        public boolean isOptout;
        public Timestamp lastCheckTime;
    }

    public OptoutDeviceFilter() {
        eventForChecking = null;
    }

    /**
     * @param eventForChecking the eventForChecking to set, type: RawConnectionEvent
     */
    public void setEventForChecking(RawConnectionEvent eventForChecking) {
        this.eventForChecking = eventForChecking;
    }

    @PostConstruct
    private void init(){
        logger.debug("Using cache size for optout dev cache: "+ String.valueOf(cacheSize));
        devInfoCache = new LRUCache<Long, OptoutFilterCacheItem>(cacheSize);
    }

    @Override
    public Boolean call() throws Exception {
        return isForwarding(eventForChecking);
    }

    @Override
    public boolean isForwarding(RawConnectionEvent evt) {
        MacAddress devMac = evt.getClientMac();
        OptoutFilterCacheItem devCacheItem = devInfoCache.get(devMac.getMacAddrLong());
        long nowEpoch = System.currentTimeMillis();
        if (devCacheItem != null) {
            logger.debug("Found Cache for Dev:"+devMac.getMacAddrStr());
            if (devCacheItem.lastCheckTime.getTime() + cacheExpirationIntervalMs > nowEpoch) {
                // Cache is not expired yet
                if (devCacheItem.isOptout == true) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        logger.debug("Missing or Expired Cache for Dev:"+devMac.getMacAddrStr() + " Fetch from DB");
        logger.debug("mac long:"+Long.toString(devMac.getMacAddrLong()));
        MacAddress ret = optoutDeviceRepo.getByMacLong(devMac.getMacAddrLong());
        boolean optoutState = true;
        if (ret == null) {
            optoutState = false;
        }
        logger.debug("Opt-out status from DB of device: "+devMac.getMacAddrLong() + " is :" + String.valueOf(optoutState));
        OptoutFilterCacheItem newDevCache = new OptoutFilterCacheItem();
        newDevCache.isOptout = optoutState;
        newDevCache.lastCheckTime = new Timestamp(System.currentTimeMillis());

        logger.debug("newDevCache: isOptout: " + String.valueOf(newDevCache.isOptout) + " lastCheckTime: " + String.valueOf(newDevCache.lastCheckTime.getTime()));

        devInfoCache.set(devMac.getMacAddrLong(), newDevCache);

        logger.debug("Finish setting in opt-out cache");

        if (optoutState == true){
            return false;
        } else {
            return true;
        }
    }

    /**
     * @return the filterReason
     */
    public String getFilterReason() {
        return filterReason;
    }

}
