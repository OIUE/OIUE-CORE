package org.oiue.service.system.analyzer;

import java.util.Map;

public interface TimeLogger {
	
	public abstract boolean isDebugEnabled();
	
	public abstract void debug(Map<String, Object> param);
}
