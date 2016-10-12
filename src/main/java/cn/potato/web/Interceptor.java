package cn.potato.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 拦截器接口
 * @author 李恒名
 * @since 2016年3月4日
 */
public interface Interceptor {
	    void before(HttpServletRequest request,HttpServletResponse response,Method controMethod);
	    void after(HttpServletRequest request,HttpServletResponse response,Object result);
}
