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
	
	public Result addData(String key,Object value){
		model.put(key, value);
		return this;
	}

	public String getViewName() {
		return viewName;
	}

	public Result setViewName(String viewName) {
		this.viewName = viewName;
		return this;
	}

    Map<String, Object> getModel() {
		return model;
	}

	public static Result instance(){
		return new Result();
	}
	
}
