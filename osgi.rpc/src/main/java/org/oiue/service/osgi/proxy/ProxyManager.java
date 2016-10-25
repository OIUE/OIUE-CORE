package org.oiue.service.osgi.proxy;

import java.lang.reflect.Proxy;
import java.util.Dictionary;

import org.oiue.service.cache.tree.CacheTreeService;

import org.osgi.framework.BundleContext;


@SuppressWarnings("rawtypes")
public class ProxyManager {

    public static BundleContext proxyContext(BundleContext context){
        Class cls = context.getClass();
        return (BundleContext) Proxy.newProxyInstance(cls.getClassLoader(), cls.getInterfaces(), new ContextProxy(context));
    }
    
    public static Object proxyService(String serviceName,CacheTreeService cacheTreeService, BundleContext context, Dictionary<String, ?> config) throws ClassNotFoundException{
        Class cls= Class.forName(serviceName, false, BundleUtils.getBundleClassLoader(context.getBundle()));
        return Proxy.newProxyInstance(cls.getClassLoader(), new Class[] { cls }, new RPCServiceProxy(cls, config, cacheTreeService));
    }
}
