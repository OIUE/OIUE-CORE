package org.oiue.service.cache.tree;

import java.io.Serializable;
import java.util.List;

public interface CacheTreeService extends Serializable {
	
	public String create(String path, Object data);
	public String createTemp(String path, Object data);
	public List<Object> getChildren(String path);
	public byte[] getData(String path);
	public boolean setData(String path, byte[] data, int version);
	public boolean delete(String path, int version);
	
	public boolean registerChangeEvent(String path,String eventName,ChangeEvent event);
	public boolean unRegisterChangeEvent(String path,String eventName);
	
	public void stop();
}
