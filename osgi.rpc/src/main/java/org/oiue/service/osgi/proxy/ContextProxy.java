package org.oiue.service.osgi.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.oiue.tools.StatusResult;
import org.osgi.framework.BundleContext;

class ContextProxy implements InvocationHandler {

	BundleContext context = null;

	public ContextProxy(BundleContext context) {
		this.context = context;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ("registerService".equals(method.getName())) {
			try {
				Object service = args[1];
				if (service == null)
					throw new RuntimeException("registerService error:" + args);
				Class cls = service.getClass();
				try {
					if(!cls.getName().startsWith("org.oiue.service.odp"))
						args[1] = Proxy.newProxyInstance(cls.getClassLoader(), cls.getInterfaces(), new ServiceProxy(service));
				} catch (Throwable e) {
					StatusResult sr = new StatusResult();
					sr.setResult(StatusResult._SUCCESS);
					System.out.println(":"+e.getMessage()+","+cls.getSimpleName()+","+cls.getClassLoader()+","+Arrays.asList(cls.getInterfaces()));
				}
				Object argso =args[0];
				if(argso instanceof String){
					ServicesManager.putService((String) argso, args[1]);
				}else if(argso instanceof String[]){
					//                    System.out.println(argso.getClass().getName());
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return method.invoke(context, args);
	}

}
