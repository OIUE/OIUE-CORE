package org.oiue.service.log;

import java.io.Serializable;

public interface Logger extends Serializable {
	public static final int LOG_ERROR = 1;
	public static final int LOG_WARNING = 2;
	public static final int LOG_INFO = 3;
	public static final int LOG_DEBUG = 4;
	
	public void log(int level, String msg, Throwable throwable);
	
	public boolean isTraceEnabled();
	
	public void trace(String msg, Object... arguments);
	
	public void trace(String msg, Throwable t);
	
	public boolean isDebugEnabled();
	
	public void debug(String msg, Object... arguments);
	
	public void debug(String msg, Throwable t);
	
	public boolean isInfoEnabled();
	
	public void info(String msg, Object... arguments);
	
	public void info(String msg, Throwable t);
	
	public boolean isWarnEnabled();
	
	public void warn(String msg, Object... arguments);
	
	public void warn(String msg, Throwable t);
	
	public boolean isErrorEnabled();
	
	public void error(String msg, Object... arguments);
	
	public void error(String msg, Throwable t);
}
