package org.oiue.service.osgi.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceProxy implements InvocationHandler {

    Object o = null;

    public ServiceProxy(Object o) {
        this.o = o;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object rtn = null;
        try {
            rtn = method.invoke(o, args);
        } catch (Throwable e) {
//            System.out.println("proxy:"+proxy+"|method:"+method+"|args:"+args);
//            e.printStackTrace();
            throw e;
        }
        return rtn;
    }

}
