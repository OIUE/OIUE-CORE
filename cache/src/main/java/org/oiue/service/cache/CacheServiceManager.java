package org.oiue.service.cache;


public interface CacheServiceManager extends CacheService {

    public boolean registerCacheService(String name,CacheService cache);
    
    public boolean unRegisterCacheService(String name);
    
    public CacheService getCacheService(String name);
}
