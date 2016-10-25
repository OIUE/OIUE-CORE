package org.oiue.service.osgi.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.oiue.service.cache.tree.CacheTreeService;
import org.oiue.service.osgi.FrameActivator;

import org.osgi.framework.BundleContext;

public class ServicesManager {

	private static Map<String, Object> allService = null;
	private static Map<FrameActivator, List<Object>> relationService = null;
	private static Set<String> allRelationService = null;
	private static Set<String> allStartService = null;

	static {
		allService = new ConcurrentHashMap<>();
		relationService = new ConcurrentHashMap<>();
		allRelationService = new CopyOnWriteArraySet<>();
		allStartService = new CopyOnWriteArraySet<>();
	}

	public static void addAllARS(Collection<? extends String> c) {
		allRelationService.addAll(c);
	}

	public static void putRelationService(FrameActivator serviceName, List<Object> relation) {
		relationService.put(serviceName, relation);
	}

	public static void addAllASS(Collection<? extends String> c) {
		allStartService.addAll(c);
	}

	public static Set<String> getStartService() {
		return allStartService;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getServiceByName(String serviceName) {
		return (T) allService.get(serviceName);
	}

	public static void putService(String key, Object o) {
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
		return allStartService;
	}

	public static Set<String> getRpcServices() {
		Set<String> allRelationServiceTmp = new HashSet<>();
		allRelationServiceTmp.addAll(allRelationService);
		allRelationServiceTmp.removeAll(allStartService);
		return allRelationServiceTmp;
	}

	public static void startRPC(CacheTreeService cacheTreeService, BundleContext context, Dictionary<String, ?> config) {
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

	public static void registerRPC(CacheTreeService cacheTreeService, String path, String local) {
		List<Exception> es = new ArrayList<>();
		for (String serviceName : ServicesManager.getStartService()) {
			try {
				cacheTreeService.createTemp(path + "Services/" + serviceName.replace(".", "_"), local);
			} catch (Exception e) {
				es.add(e);
			}
		}
	}
}
