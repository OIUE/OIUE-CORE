package org.oiue.service.osgi;

import java.util.Map;

public abstract class MulitServiceTrackerCustomizer  {
	public abstract void addingService();
	
	public abstract void removedService();
	
	public abstract void updatedConf(final Map<String, ?> props);
	
	public boolean initialize = false;
}