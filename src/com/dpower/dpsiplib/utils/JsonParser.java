package com.dpower.dpsiplib.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * JSON解析类
 * 
 * @author LXZ
 * @date
 */
public class JsonParser {
	private Gson gson;

	public JsonParser() {
		gson = new Gson();
	}

	public <T> T getObject(String jsonString, Class<T> cls) {
		T t = null;
		try {
			t = gson.fromJson(jsonString, cls);
		} catch (Exception e) {
			// throw new RuntimeException();
			System.out.println(e.toString());
			return null;
		}
		return t;
	}

	public <T> List<T> getObjectList(String jsonString, Class<T> cls) {
		List<T> list = new ArrayList<T>();
		try {
			list = (List) gson.fromJson(jsonString, new TypeToken() {
			}.getType());
		} catch (Exception e) {
			throw new RuntimeException();
		}
		return list;
	}

	public List<Map<String, Object>> listKeyMaps(String jsonString) {
		List<Map<String, Object>> list = new ArrayList();
		try {
			list = (List) gson.fromJson(jsonString, new TypeToken() {
			}.getType());
		} catch (Exception e) {
			throw new RuntimeException();
		}
		return list;
	}

	/**
	 * 将指定规则字符串转换成map
	 * 
	 * @param msg
	 *            :以","分隔的"="键值字符串，例如"name=java,age=10"
	 * @return
	 */
	public Map<String, String> getMapFromString(String msg) {
		Map<String, String> map = new HashMap<String, String>();
		if (msg == null) {
			return map;
		}
		String[] arrayString = msg.split(",");
		for (String s : arrayString) {
			if (s != null) {
				String[] keyValue = s.split("=");
				if (keyValue.length == 2) {
					map.put(keyValue[0], keyValue[1]);
				}
			}
		}
		return map;
	}

}
