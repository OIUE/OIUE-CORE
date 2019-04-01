package org.oiue.service.cache;

public interface CacheService {
	
	public void put(String name, Object object, Type type);
	
	public void put(String name, String key, Object object, Type type);
	
	public void put(String name, Object object, Type type, int expire);
	
	public void put(String name, String key, Object object, Type type, int expire);
	
	public void put(String name, String key, Type type, Object... objects);
	
	public Object get(String name);
	
	public boolean contains(String name, String... keys);
	
	public Object get(String name, String key);
	
	public long delete(String name);
	
	public long delete(String name, String... keys);
	
	public boolean exists(String name);
	
	public void swap(String nameA, String nameB);
}