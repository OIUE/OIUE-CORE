package org.oiue.service.osgi.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;

public class ServiceProxy implements InvocationHandler {
	
	Object o = null;
	
	public ServiceProxy(Object o) {
		this.o = o;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		Object rtn = null;
		try {
			rtn = method.invoke(o, args);
			return rtn;
		} catch (Throwable e) {
			throw new OIUEException(StatusResult._blocking_errors, proxy + "." + method, e);
		}
	}
	
}
