package org.oiue.service.osgi.rpc;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RPCServiceImpl extends UnicastRemoteObject implements RPCService {
    private static final long serialVersionUID = 1L;

    public RPCServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public Object call(Object proxy, Method method, Object[] args) throws RemoteException{
        return new Object();
    }

}
