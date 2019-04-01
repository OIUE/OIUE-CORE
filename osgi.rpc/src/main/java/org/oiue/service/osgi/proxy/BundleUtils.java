package org.oiue.service.osgi.proxy;

import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * 插件辅助类 作者博客：http://www.cnblogs.com/aaaSoft
 * 
 * @author aaa
 * 
 */
public class BundleUtils {
	/**
	 * 得到Bundle的类加载器
	 * 
	 * @param bundle
	 * @return
	 */
	public static ClassLoader getBundleClassLoader(Bundle bundle) {
		// 搜索Bundle中所有的class文件
		Enumeration<URL> classFileEntries = bundle.findEntries("/", "*.class", true);
		if (classFileEntries == null || !classFileEntries.hasMoreElements()) {
			throw new RuntimeException(String.format("Bundle[%s]中没有一个Java类！", bundle.getSymbolicName()));
		}
		// 得到其中的一个类文件的URL
		URL url = classFileEntries.nextElement();
		// 得到路径信息
		String bundleOneClassName = url.getPath();
		// 将"/"替换为"."，得到类名称
		bundleOneClassName = bundleOneClassName.replace("/", ".").substring(0, bundleOneClassName.lastIndexOf("."));
		// 如果类名以"."开头，则移除这个点
		while (bundleOneClassName.startsWith(".")) {
			bundleOneClassName = bundleOneClassName.substring(1);
		}
		Class<?> bundleOneClass = null;
		try {
			// 让Bundle加载这个类
			bundleOneClass = bundle.loadClass(bundleOneClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		// 得到Bundle的ClassLoader
		return bundleOneClass.getClassLoader();
	}
	
	@SuppressWarnings("rawtypes")
	public static void printServiceReference(ServiceReference ref) {
		String[] property = ref.getPropertyKeys();
		for (String key : property) {
			Object value = ref.getProperty(key);
			System.out.println("key:" + key + "|value=" + ((value instanceof String[]) ? Arrays.deepToString((Object[]) value) : value));
		}
	}
}
