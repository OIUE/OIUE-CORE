package org.oiue.service.system.analyzer;

import java.util.Map;

@SuppressWarnings("rawtypes")
public interface AnalyzerService {
	public void updateProps(Map props);
	
	public TimeLogger getLogger(Class c);
}
