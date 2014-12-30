package factory;

import java.net.MalformedURLException;

import org.springframework.beans.factory.FactoryBean;

import classLoader.ClassUtils;

import com.caucho.hessian.client.HessianProxyFactory;

/**
 * author: chao.hua
 * createTime: Dec 30, 2014 2:26:28 PM
 */

public class RpcIfaceFactory implements FactoryBean {
	
	//input
	private String url;
	private String user;
	private String password;
	private boolean overloadEnabled;
	private boolean debug;
	private String iface;
	
	//internal
	private Class<?> objType;
	private Object obj;
	
	public void init() throws ClassNotFoundException, MalformedURLException{
		HessianProxyFactory hessianFactory = new HessianProxyFactory();
		hessianFactory.setUser(user);
		hessianFactory.setPassword(password);
		hessianFactory.setDebug(debug);
		hessianFactory.setOverloadEnabled(overloadEnabled);
		objType = ClassUtils.loadClass(iface);
		obj = hessianFactory.create(objType, url);
	}

	public Object getObject() throws Exception {
		return obj;
	}

	public Class<?> getObjectType() {
		return objType;
	}

	public boolean isSingleton() {
		return false;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isOverloadEnabled() {
		return overloadEnabled;
	}

	public void setOverloadEnabled(boolean overloadEnabled) {
		this.overloadEnabled = overloadEnabled;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getIface() {
		return iface;
	}

	public void setIface(String iface) {
		this.iface = iface;
	}

}
