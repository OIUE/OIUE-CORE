package org.oiue.service.cache.tree;

import java.util.List;

public interface ChangeEvent {
	public String getName();
	public void change(List<String> data);
}