package org.oiue.service.osgi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

import org.oiue.service.osgi.proxy.ProxyManager;
import org.oiue.service.osgi.proxy.ServicesManager;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class MulitServiceTracker implements ServiceTrackerCustomizer {
    private BundleContext context;
    private Hashtable<String, Object> services = new Hashtable<String, Object>();
    private int trackerCount = 0;
    private ServiceTracker tracker = null;
    private SystemMulitServiceTrackerCustomizer trackerCustomizer = null;
    private HashSet<String> classNameSet = new HashSet<String>();

    public MulitServiceTracker(BundleContext context, String[] classNames, SystemMulitServiceTrackerCustomizer mulitServiceTrackerCustomizer) throws InvalidSyntaxException {
        trackerCount = classNames.length;
        trackerCustomizer = mulitServiceTrackerCustomizer;
        this.context = ProxyManager.proxyContext(context);
        
        StringBuffer filterString = new StringBuffer();
        filterString.append("(|");
        for (int i = 0; i < classNames.length; i++) {
            if (!classNameSet.contains(classNames[i])) {
                classNameSet.add(classNames[i]);
                filterString.append("(objectClass=").append(classNames[i]).append(")");
            }
        }
        filterString.append(")");
        tracker = new ServiceTracker(context, context.createFilter(filterString.toString()), this);
        ServicesManager.addAllARS(classNameSet);
    }

    public BundleContext getContext() {
        return context;
    }

    public void open() {
        tracker.open(true);
    }

    public void close() {
        tracker.close();
    }

    @Override
    public Object addingService(ServiceReference ref) {

        Object obj = context.getService(ref);
        String classNames[] = ((String[]) ref.getProperty("objectClass"));

        ServicesManager.addAllASS(Arrays.asList(classNames));
        for (String e : classNames) {
            if (classNameSet.contains(e)) {
                services.put(e, obj);
            }
        }

        if (services.size() == trackerCount) {
            trackerCustomizer.addingService(this);
        }
        return obj;
    }

    @Override
    public void modifiedService(ServiceReference ref, Object object) {}

    @Override
    public void removedService(ServiceReference ref, Object object) {
        String classNames[] = ((String[]) ref.getProperty("objectClass"));
        if (services.size() == trackerCount) {
            trackerCustomizer.removedService(this);
        }
        for (String e : classNames) {
            if (classNameSet.contains(e)) {
                services.remove(e);
            }
        }
    }


    public <T> T getService(String className) {
        if (!classNameSet.contains(className)) {
            throw new RuntimeException("get service exception, class[" + className + "] not in classNames.");
        }
        T service = (T) services.get(className);
        //        if (service != null && !service.getClass().getName().startsWith("com.sun.proxy")) {
        //            Class cls = service.getClass();
        //            service = (T) Proxy.newProxyInstance(cls.getClassLoader(), cls.getInterfaces(), new serviceProxy(service));
        //        }
        return service;
    }

    public <T> T getServiceForce(String serviceName) {
        return (T) ServicesManager.getServiceByName(serviceName);
    }

}
