package org.oiue.service.log;

import java.io.Serializable;

public interface LogService extends Serializable {
	@SuppressWarnings("rawtypes")
	public Logger getLogger(Class clazz);
	
	public Logger getLogger(String name);
}
