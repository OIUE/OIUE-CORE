package org.oiue.service.osgi;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.oiue.service.osgi.proxy.ServiceProxy;
import org.oiue.service.osgi.proxy.ServicesManager;
import org.oiue.tools.string.StringUtil;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class FrameActivator implements BundleActivator, ServiceTrackerCustomizer {

    private BundleContext context;
    private boolean isProxy = false;

    private Set<String> classNameSet = new HashSet<>();
    private Hashtable<String, Object> services = new Hashtable<String, Object>();
    private MulitServiceTrackerCustomizer trackerCustomizer = null;
    private ServiceTracker tracker;
    private Set<ServiceRegistration<?>> regConfigurator = new HashSet<>();

    public final void start(BundleContext context) throws Exception {
        this.context = context;
        isProxy = StringUtil.isTrue(getProperty("count_call_service") + "");
        start();
    }

    public final void stop(BundleContext context) throws Exception {
        synchronized (regConfigurator) {
            for (ServiceRegistration<?> serviceRegistration : regConfigurator) {
                serviceRegistration.unregister();
            }
            regConfigurator.clear();
        }
        stop();
        if (trackerCustomizer != null){
            trackerCustomizer.removedService();
            trackerCustomizer.initialize=false;
        }
        if (services != null)
            services.clear();
        if (tracker != null)
            tracker.close();
        trackerCustomizer = null;
        tracker = null;
        context = null;
    }

    public final <T> T getService(Class<T> c) {
        String classname = c.getName();
        if (!classNameSet.contains(classname)) {
            throw new RuntimeException("get service exception, class[" + c + "] not in classNames.");
        }
        T service = ((T) services.get(classname));
        return service;
    }

    public abstract void start() throws Exception;

    public abstract void stop() throws Exception;

    public final void start(MulitServiceTrackerCustomizer mstc, Class... cs) throws Exception {
        this.trackerCustomizer = mstc;
        StringBuffer filterString = new StringBuffer();
        filterString.append("(|");
        if (cs != null && cs.length > 0) {
            for (int i = 0; i < cs.length; i++) {
                String classname = cs[i].getName();
                if (!classNameSet.contains(classname)) {
                    classNameSet.add(classname);
                    filterString.append("(objectClass=").append(classname).append(")");
                }
            }
            filterString.append(")");
            tracker = new ServiceTracker(context, context.createFilter(filterString.toString()), this);
            tracker.open();
            ServicesManager.addAllARS(classNameSet);
        } else {
            synchronized (this) {
                if (!trackerCustomizer.initialize) {
                    trackerCustomizer.addingService();
                    Dictionary<String, String> props = new Hashtable<>();
                    props.put("service.pid", this.getClass().getName());
                    synchronized (regConfigurator) {
                        regConfigurator.add(context.registerService(ManagedService.class.getName(), trackerCustomizer, props));
                    }
                    trackerCustomizer.initialize=true;
                }
            }
        }
    }

    public final Object addingService(ServiceReference reference) {
        Object obj = context.getService(reference);
        String classNames[] = ((String[]) reference.getProperty("objectClass"));

        ServicesManager.addAllASS(Arrays.asList(classNames));
        for (String e : classNames) {
            if (classNameSet.contains(e)) {
                services.put(e, obj);
            }
        }

        if (services.size() == classNameSet.size()) {
            synchronized (this) {
                if (!trackerCustomizer.initialize) {
                    trackerCustomizer.addingService();
                    Dictionary<String, String> props = new Hashtable<>();
                    props.put("service.pid", this.getClass().getName());
                    synchronized (regConfigurator) {
                        regConfigurator.add(context.registerService(ManagedService.class.getName(), trackerCustomizer, props));
                    }
                    trackerCustomizer.initialize=true;
                    
                    ServicesManager.putRelationService(this, new ArrayList<>(classNameSet));
                }
            }
        }
        return obj;
    }

    public final void modifiedService(ServiceReference reference, Object object) {}

    public final void removedService(ServiceReference reference, Object object) {}

    public final void registerService(Class c, Object object) {
        synchronized (regConfigurator) {
            regConfigurator.add(context.registerService(c.getName(), object, null));
        }
        if (isProxy && !c.getName().startsWith("org.oiue.service.odp")) {
            ServicesManager.putService(c.getName(), Proxy.newProxyInstance(c.getClassLoader(), c.getInterfaces(), new ServiceProxy(object)));
        } else
            ServicesManager.putService(c.getName(), object);
    }

    public final String getProperty(String key) {
        return context.getProperty(key);
    }

    public <T> T getServiceForce(String serviceName) {
        return (T) ServicesManager.getServiceByName(serviceName);
    }
}
