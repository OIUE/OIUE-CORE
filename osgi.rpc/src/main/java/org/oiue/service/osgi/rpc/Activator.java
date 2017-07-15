package org.oiue.service.osgi.rpc;

import java.util.Arrays;
import java.util.Dictionary;

import org.oiue.service.cache.tree.CacheTreeService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.MulitServiceTrackerCustomizer;
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

public class Activator implements BundleActivator {

	private FrameActivator tracker = null;
	private CacheTreeService cacheTreeService;

	@Override
	public void start(final BundleContext context) throws Exception {
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
						// 调用Service方法释放Service，在此之后不应该再继续使用Service实例
						context.ungetService(ref);
					}
					ServicesManager.putService(((String[])ref.getProperty("objectClass"))[0], service);
					// System.out.println(ref.getProperty("service.id")+ "|" +((String[])ref.getProperty("objectClass"))[0]+ "|" + service);
					// BundleUtils.printServiceReference(ref);
					break;
				case ServiceEvent.UNREGISTERING:
					// System.out.println("serviceChanged:[" + event.getType() + "]" + event);
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

					try {
						tracker = new FrameActivator() {
							@Override
							public void stop() throws Exception {}
							@Override
							public void start() throws Exception {
								this.start(new MulitServiceTrackerCustomizer() {
									@Override
									public void addingService() {
										cacheTreeService = getService(CacheTreeService.class);
									}
									@Override
									public void removedService() {}
									@Override
									public void updated(Dictionary<String, ?> props) {
										boolean startRpc = StringUtil.isTrue(props.get("startRpc") + "");
										boolean shareService = StringUtil.isTrue(props.get("shareService") + "");

										new Thread(this.getClass().getName()) {
											@Override
											public void run() {
												System.out.println("not start services:" + ServicesManager.getRpcServices());

												if (props != null) {
													//													if (startRpc)

													if (shareService)
														ServicesManager.startRPC(cacheTreeService, context, props);
												}
											}
										}.start();
									}
								},CacheTreeService.class);}

						};
						tracker.start(context);
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
	public void stop(BundleContext context) throws Exception {}
}
