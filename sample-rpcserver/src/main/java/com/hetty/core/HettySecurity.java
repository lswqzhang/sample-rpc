package com.hetty.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.hetty.object.Application;

public class HettySecurity {
	
	private static final Map<String, Application> applicationMap = new HashMap<String, Application> ();
	
	public static void addToApplicationMap(Application app){
		applicationMap.put(app.getUser(), app);
	}
	
	/**
	 * check permission
	 * @param user
	 * @param password
	 */
	public static boolean checkPermission(String user,String password){
		if(StringUtils.isEmpty(user) || StringUtils.isEmpty(password)){
			throw new IllegalArgumentException("user or password is null,please check.");
		}
		if(applicationMap.containsKey(user) && applicationMap.get(user).getPassword().equals(password)){
			return true;
		}else{
			return false;
		}
	}
}
