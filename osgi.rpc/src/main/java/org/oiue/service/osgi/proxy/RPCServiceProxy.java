package org.oiue.service.osgi.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;

import org.oiue.service.cache.tree.CacheTreeService;

@SuppressWarnings("rawtypes")
public class RPCServiceProxy implements InvocationHandler {

    Class o = null;
    Dictionary<String, ?> config = null;
    CacheTreeService cacheTreeService = null;
    String path = null;
    int rpcPort = 1099;
    String serviceName = null;

    public RPCServiceProxy(Class o, Dictionary<String, ?> config, CacheTreeService cacheTreeService) {
        this.o = o;
        this.config = config;
        this.cacheTreeService = cacheTreeService;

        path = config.get("rootPath") + "";
        path = path.endsWith("/") ? path : path + "/";
        rpcPort = Integer.valueOf(config.get("rpcPort") + "");

        serviceName = o.getName().replace(".", "_");
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object rtn = null;
        if (method.equals("toString")) {
            try {
                rtn = method.getName() + "toString()";
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return rtn;
        } else
            System.out.println(o.toString() + ">>>serviceProxy " + "method:" + method.getName() + "[" + (args == null ? "" : Arrays.asList(args)) + "]");
        
        List<Object> list = cacheTreeService.getChildren(path+"Services/"+serviceName);
        if(list!=null)
        for (Object object : list) {
            System.out.println(object);
        }
        return new Object();
    }
}
