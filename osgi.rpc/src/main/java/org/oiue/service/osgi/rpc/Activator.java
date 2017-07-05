package org.oiue.service.osgi.rpc;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import org.oiue.service.cache.tree.CacheTreeService;
import org.oiue.service.osgi.MulitServiceTracker;
import org.oiue.service.osgi.SystemMulitServiceTrackerCustomizer;
import org.oiue.service.osgi.proxy.ServicesManager;
import org.oiue.tools.string.StringUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class Activator implements BundleActivator {

	private MulitServiceTracker tracker = null;
	private CacheTreeService cacheTreeService;
	private BundleContext context;

	private class Configurator implements ManagedService {

		@Override
		public void updated(final Dictionary<String, ?> props) throws ConfigurationException {

			new Thread(this.getClass().getName()) {
				@Override
				public void run() {
					System.out.println("not start services:" + ServicesManager.getRpcServices());

					if (props != null) {
						boolean startRpc = StringUtil.isTrue(props.get("startRpc") + "");
						boolean shareService = StringUtil.isTrue(props.get("shareService") + "");
						if (startRpc)
							try {
								String local = props.get("localURL") + "";
								String path = props.get("rootPath") + "";
								int rpcPort = Integer.valueOf(props.get("rpcPort") + "");
								if (StringUtil.isEmptys(path) || StringUtil.isEmptys(local)) {
									throw new RuntimeException("config localURL and rootPath not null!");
								}
								path = path.endsWith("/") ? path : path + "/";
								RPCService rpc = new RPCServiceImpl();
								URLClassLoader uc = (URLClassLoader) ClassLoader.getSystemClassLoader();
								Method add = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
								add.setAccessible(true);
								add.invoke(uc, new Object[] { RPCService.class.getProtectionDomain().getCodeSource().getLocation() });

								String url = "rmi://" + local + ":" + rpcPort + "/RPCService";
								LocateRegistry.createRegistry(rpcPort);
								// System.setSecurityManager(new
								// RMISecurityManager());
								Naming.rebind(url, rpc);
								// cacheTreeService.delete(path.substring(0,
								// path.length()-1), 0);

								cacheTreeService.createTemp(path + "ServerStatus/" + local.replace(".", "_"), local);
								ServicesManager.registerRPC(cacheTreeService, path, local);

								System.out.println("start all service:" + ServicesManager.getAllService());
								System.out.println("start all local service:" + ServicesManager.getAllStartService());
								System.out.println("start all relation service:" + ServicesManager.getRelationService());

							} catch (Throwable e) {
								e.printStackTrace();
							}
						if (shareService)
							ServicesManager.startRPC(cacheTreeService, context, props);
					}
				}
			}.start();
		}
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		this.context = context;
		context.addBundleListener(new BundleListener() {
			@Override
			public void bundleChanged(BundleEvent be) {}
		});

		context.addServiceListener(new ServiceListener() {

			@Override
			public void serviceChanged(ServiceEvent event) {
				switch (event.getType()) {
				case ServiceEvent.REGISTERED:
					// 获取Service引用
					ServiceReference<?> ref = event.getServiceReference();

					// 获取Service实例
					Object service = context.getService(ref);
					if (service != null) {
						// 调用Service方法
						// 释放Service，在此之后不应该再继续使用Service实例
						context.ungetService(ref);
					}
					// System.out.println(ref + "serviceChanged:" + service);
					// BundleUtils.printServiceReference(ref);
					break;
				case ServiceEvent.UNREGISTERING:
					System.out.println("serviceChanged:[" + event.getType() + "]" + event);

					break;
				}
			}
		});

		context.addFrameworkListener(new FrameworkListener() {

			@Override
			public void frameworkEvent(FrameworkEvent event) {

				switch (event.getType()) {
				case ServiceEvent.REGISTERED:
					System.out.println("frameworkEvent REGISTERED:[" + event.getType() + "]" + event);
					String classNames[] = { CacheTreeService.class.getName() };

					try {
						tracker = new MulitServiceTracker(context, classNames, new SystemMulitServiceTrackerCustomizer() {
							private ServiceRegistration<?> regConfigurator;

							@Override
							public void removedService(MulitServiceTracker tracker) {
								regConfigurator.unregister();
							}

							@Override
							public void addingService(MulitServiceTracker tracker) {
								cacheTreeService = tracker.getService(CacheTreeService.class.getName());

								Dictionary<String, String> props = new Hashtable<>();
								props.put("service.pid", Activator.class.getName());
								regConfigurator = tracker.getContext().registerService(ManagedService.class, new Configurator(), props);
							}
						});
						tracker.open();
					} catch (Throwable e) {
						e.printStackTrace();
					}

					System.out.println("not start services:" + ServicesManager.getRpcServices());
					System.out.println("start RPC over");
					break;

				case ServiceEvent.UNREGISTERING:
					System.out.println("frameworkEvent UNREGISTERING:[" + event.getType() + "]" + event);

					break;

				default:
					System.out.println("frameworkEvent:[" + event.getType() + "]" + event);
					break;
				}
			}
		});

		System.out.println("all bundles:" + Arrays.asList(context.getBundles()));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (tracker != null)
			tracker.close();
	}
}
