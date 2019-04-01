package org.oiue.service.osgi.rpc;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RPCService extends Remote, Serializable {
	public Object call(Object proxy, Method method, Object[] args) throws RemoteException;
}
