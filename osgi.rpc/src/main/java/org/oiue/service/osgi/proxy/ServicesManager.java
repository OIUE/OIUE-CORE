package org.oiue.service.osgi.proxy;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.oiue.service.cache.tree.CacheTreeService;
import org.oiue.service.osgi.FrameActivator;
import org.oiue.service.osgi.rpc.RPCService;
import org.oiue.service.osgi.rpc.RPCServiceImpl;
import org.oiue.tools.string.StringUtil;
import org.osgi.framework.BundleContext;

public class ServicesManager {
	
	private static Map<String, Object> allService = null; // 所有服务
	private static Map<FrameActivator, List<Object>> relationService = null;
	private static Set<String> allRelationService = null;
	private static Map<String, Object> allStartService = null;
	private static CacheTreeService cacheTreeService = null;
	public static void setCacheTreeService(CacheTreeService cacheTreeService) {
		ServicesManager.cacheTreeService = cacheTreeService;
	}

	static {
		allService = new ConcurrentHashMap<>();
		relationService = new ConcurrentHashMap<>();
		allRelationService = new CopyOnWriteArraySet<>();
		allStartService = new ConcurrentHashMap<>();
	}
	
	public static void addAllARS(Collection<? extends String> c) {
		allRelationService.addAll(c);
	}
	
	public static void putRelationService(FrameActivator serviceName, List<Object> relation) {
		relationService.put(serviceName, relation);
	}
	
	public static Set<String> getStartService() {
		return allStartService.keySet();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getServiceByName(String serviceName) {
		return (T) allService.get(serviceName);
	}
	
	public static void putService(String key, Object o) {
		if (allStartService.containsKey(key))
			return;
		allStartService.put(key, o);
		allService.put(key, o);
	}
	
	public static Map<String, Object> getAllService() {
		return allService;
	}
	
	public static Map<FrameActivator, List<Object>> getRelationService() {
		return relationService;
	}
	
	public static Set<String> getAllRelationService() {
		return allRelationService;
	}
	
	public static Set<String> getAllStartService() {
		return allStartService.keySet();
	}
	
	public static Set<String> getRpcServices() {
		Set<String> allRelationServiceTmp = new HashSet<>();
		allRelationServiceTmp.addAll(allRelationService);
		allRelationServiceTmp.removeAll(allStartService.keySet());
		return allRelationServiceTmp;
	}
	
	public static void startRPC(BundleContext context, Map<String, ?> config) {
		
		String local = config.get("localIp") + "";
		String path = config.get("rootPath") + "";
		int rpcPort = Integer.valueOf(config.get("rpcPort") + "");
		if (StringUtil.isEmptys(path) || StringUtil.isEmptys(local)) {
			throw new RuntimeException("config localURL and rootPath not null!");
		}
		try {
			path = path.endsWith("/") ? path : path + "/";
			
			URLClassLoader uc = (URLClassLoader) ClassLoader.getSystemClassLoader();
			Method add = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
			add.setAccessible(true);
			add.invoke(uc, new Object[] { RPCService.class.getProtectionDomain().getCodeSource().getLocation() });
			
			LocateRegistry.createRegistry(rpcPort);
			RPCService rpc = new RPCServiceImpl();
			String url = "rmi://0.0.0.0:" + rpcPort + "/RPCService";
			Naming.rebind(url, rpc);
			
			ServicesManager.registerRPC(path, local);
			// System.out.println("start all service:" + ServicesManager.getAllService());
			// System.out.println("start all local service:" + ServicesManager.getAllStartService());
			// System.out.println("start all relation service:" + ServicesManager.getRelationService());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		/*
		 * 对于本地需要依赖却未部署的服务，启用远程调用
		 */
		List<Exception> es = new ArrayList<>();
		for (String serviceName : ServicesManager.getRpcServices()) {
			try {
				Object service = ProxyManager.proxyService(serviceName, cacheTreeService, context, config);
				context.registerService(serviceName, service, null);
			} catch (Exception e) {
				es.add(e);
			}
		}
		if (es.size() > 0) {
			
		}
	}
	
	public static void registerRPC(String path, String local) {
		if (cacheTreeService == null) {
			
		} else {
			List<Exception> es = new ArrayList<>();
			cacheTreeService.createTemp(path + "ServerStatus/" + local.replace(".", "_"), local);
			for (String serviceName : ServicesManager.getStartService()) {
				try {
					cacheTreeService.createTemp(path + "Services/" + serviceName.replace(".", "_"), local);
				} catch (Exception e) {
					es.add(e);
				}
			}
			if (es.size() > 0) {
				
			}
		}
	}
}
