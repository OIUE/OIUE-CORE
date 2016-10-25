package org.oiue.service.osgi;

import java.util.Dictionary;

import org.osgi.service.cm.ManagedService;

public abstract class MulitServiceTrackerCustomizer implements ManagedService{
	public abstract void addingService();
	public abstract void removedService();
	public abstract void updated(final Dictionary<String, ?> props);
	
	public boolean initialize = false;
}