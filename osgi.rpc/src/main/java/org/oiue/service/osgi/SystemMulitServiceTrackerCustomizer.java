package org.oiue.service.osgi;

public interface SystemMulitServiceTrackerCustomizer {
	public void addingService(MulitServiceTracker tracker);
	public void removedService(MulitServiceTracker tracker);
}