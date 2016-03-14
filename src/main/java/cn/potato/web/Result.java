package cn.potato.web;

import java.util.HashMap;
import java.util.Map;

/**
 * 类说明
 * @author 李恒名
 * @since 2016年3月4日
 */
public class Result {
	private String viewName;
	private Map<String,Object> model = new HashMap<String, Object>();
	
	public void addData(String key,String value){
		model.put(key, value);
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public Map<String, Object> getModel() {
		return model;
	}
	
	
}
