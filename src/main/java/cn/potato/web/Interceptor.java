package cn.potato.web;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 类说明
 * @author 李恒名
 * @since 2016年3月4日
 */
public interface Interceptor {
	    void before(HttpServletRequest request,HttpServletResponse response,Method controMethod);
	    void after(HttpServletRequest request,HttpServletResponse response,Object result);
}
