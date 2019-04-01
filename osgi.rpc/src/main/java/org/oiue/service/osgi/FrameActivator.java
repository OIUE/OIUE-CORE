package org.oiue.service.osgi;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.oiue.service.osgi.proxy.ServiceProxy;
import org.oiue.service.osgi.proxy.ServicesManager;
import org.oiue.tools.string.StringUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class FrameActivator implements BundleActivator, ServiceTrackerCustomizer {

	private BundleContext context;
	private boolean isProxy = false;

	private ServiceTracker tracker;
	private MulitServiceTrackerCustomizer trackerCustomizer = null;
	private Set<String> classNameSet = new HashSet<>();
	private Set<ServiceRegistration<?>> regConfigurator = new HashSet<>();

	private final ManagedService conf = new ManagedService() {

		@Override
		public void updated(Dictionary<String, ?> conf) throws ConfigurationException {
			if (conf != null && !conf.isEmpty() && conf.size() > 0) {
				try {
					Enumeration<String> confks = conf.keys();
					Map confm = new HashMap();
					while (confks.hasMoreElements()) {
						String key = confks.nextElement();
						confm.put(key, conf.get(key));
					}
					trackerCustomizer.updatedConf(confm);

				} catch (Throwable e) {
					System.out.println(trackerCustomizer.getClass().getName());
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
	};

	@Override
	public final void start(BundleContext context) {
		this.context = context;
		isProxy = StringUtil.isTrue(getProperty("count_call_service") + "");
		start();
	}

	@Override
	public final void stop(BundleContext context) {
		synchronized (regConfigurator) {
			for (ServiceRegistration<?> serviceRegistration : regConfigurator) {
				serviceRegistration.unregister();
			}
			regConfigurator.clear();
		}
		stop();
		if (trackerCustomizer != null) {
			trackerCustomizer.removedService();
			trackerCustomizer.initialize = false;
		}
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
		T service = ((T) ServicesManager.getServiceByName(classname));
		return service;
	}

	public abstract void start();

	public abstract void stop();

	public final void start(MulitServiceTrackerCustomizer mstc, Class... cs) {
		try {
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
			} else {
				synchronized (this) {
					if (!trackerCustomizer.initialize) {
						trackerCustomizer.addingService();
						Dictionary<String, String> props = new Hashtable<>();
						props.put("service.pid", this.getClass().getName().split("\\$")[0]);
						regConfigurator.add(context.registerService(ManagedService.class.getName(), conf, props));
						trackerCustomizer.initialize = true;
					}
				}
			}
			ServicesManager.addAllARS(classNameSet);
		} catch (Throwable e) {
			throw new RuntimeException(e);
//			e.printStackTrace();
		}
	}

	@Override
	public final Object addingService(ServiceReference reference) {
		try {
			Object obj = context.getService(reference);
			String classNames[] = ((String[]) reference.getProperty("objectClass"));
			ServicesManager.putService(classNames[0], obj);
			if (ServicesManager.getAllStartService().containsAll(classNameSet)) {
				synchronized (this) {
					if (!trackerCustomizer.initialize) {
						trackerCustomizer.addingService();
						Dictionary<String, String> props = new Hashtable<>();
						props.put("service.pid", this.getClass().getName().split("\\$")[0]);
						regConfigurator.add(context.registerService(ManagedService.class.getName(), conf, props));
						trackerCustomizer.initialize = true;
						ServicesManager.putRelationService(this, new ArrayList<>(classNameSet));
					}
				}
			}
			return obj;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public final void modifiedService(ServiceReference reference, Object object) {
	}

	@Override
	public final void removedService(ServiceReference reference, Object object) {
	}

	public final void registerService(Class c, Object object) {
		try {
			String name = c.getName();
			if (isProxy && !c.getName().startsWith("org.oiue.service.odp")) {
				object = Proxy.newProxyInstance(c.getClassLoader(), c.getInterfaces(), new ServiceProxy(object));
				ServicesManager.putService(name, object);
				synchronized (regConfigurator) {
					regConfigurator.add(context.registerService(name, object, null));
				}
			} else {
				ServicesManager.putService(name, object);
				synchronized (regConfigurator) {
					regConfigurator.add(context.registerService(name, object, null));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public final String getProperty(String key) {
		return context.getProperty(key);
	}

	public final <T> T getServiceForce(String serviceName) {
		return (T) ServicesManager.getServiceByName(serviceName);
	}
	
	class TaskThread implements Callable<Map<Object, Object>> {
		Map per = null;
		Thread t;

		public TaskThread(Map per) {
			this.per = per;
		}

		@Override
		public Map<Object, Object> call() throws Exception {
			return null;
		}
		
	}
}
