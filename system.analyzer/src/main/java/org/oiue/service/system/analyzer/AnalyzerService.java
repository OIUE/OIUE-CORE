package org.oiue.service.system.analyzer;

import java.util.Dictionary;

@SuppressWarnings("rawtypes")
public interface AnalyzerService {
	public void updateProps(Dictionary props);
	public TimeLogger getLogger(Class c);
}
