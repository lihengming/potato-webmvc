package cn.potato.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.potato.annotation.Interceptor;
import cn.potato.core.MappingHolder;

import com.alibaba.fastjson.JSON;

/**
 * 类说明
 * 
 * @author 李恒名
 * @since 2016年3月4日
 */
public class Dispatcher extends HttpServlet {
	private static final long serialVersionUID = -1217007459141119979L;
	private static final String DEFAULT_CONTROLLER_METHOD = "index";
	private static final String DEFAULT_VIEW_PREFIX = "/jsp/";
	private static final String DEFAULT_VIEW_SUFFIX = ".jsp";
	private Map<String, Object> mapping;

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String requestURI = request.getRequestURI();
		String contextPath = request.getServletContext().getContextPath();
		String path = requestURI.substring(contextPath.length()).toLowerCase();
		String[] names = path.substring(1).split("/");
		Object controller = this.mapping.get("/" + names[0]);
		if (controller == null)
			throw new RuntimeException("没有找到[" + path + "]对应的控制器");
		Object result = null;
		String name = DEFAULT_CONTROLLER_METHOD;
		if (names.length > 1) {
			name = names[1];
		}
		try {
			Interceptor annotation = (Interceptor) controller.getClass()
					.getAnnotation(Interceptor.class);
			List<cn.potato.web.Interceptor> interceptors = new ArrayList<>();

			if (annotation != null) {
				Class<? extends cn.potato.web.Interceptor>[] classes = annotation
						.classes();
				for (Class<? extends cn.potato.web.Interceptor> clazz : classes) {
					interceptors.add(clazz.newInstance());
				}
			}

			Method controllerMethod = getControllerMethod(controller, name);
			if (controllerMethod != null) {
				Parameter[] parameters = controllerMethod.getParameters();
				Object[] objects = new Object[parameters.length];

				for (int i = 0; i < parameters.length; i++) {
					//String paramName = parameters[i].getName();
					
					
					String simpleClassName = parameters[i].getType()
							.getSimpleName();
					//String className = parameters[i].getType().getName();
					
					//Enumeration<String> parameterNames = request.getParameterNames();
				
					switch (simpleClassName) {
					case "HttpServletRequest":
						objects[i] = request;
						break;
					case "HttpServletResponse":
						objects[i] = response;
						break;
					case "HttpSession":
						objects[i] = request.getSession();
						break;
					default:
						//JDK自带的反射获取不到实际的参数名称(取到的为ags0、ags1、ags2)，表单数据自动封装Java对象通过方法参数注入的方式，暂时搁置。
						//据说javassist框架可以取到
						//现在很多框架使用的是注解来解决这个问题(例如(@Param("age") Integer age)),可以参考下面廖老师这篇博文
						//http://www.liaoxuefeng.com/article/00141999088629621039ee8c4614579bfedb78a5030bce3000
						/*while(parameterNames.hasMoreElements()){
							String pname = parameterNames.nextElement();
							if(pname.equals(paramName)){
								String value = request.getParameter(pname);
								if(simpleClassName.equals("Integer")){
									objects[i] = Integer.valueOf(value);
								}
							}
						}*/
						break;
					}
					//执行拦截器列表(正序)Before方法
					for (cn.potato.web.Interceptor interceptor : interceptors) {
						interceptor.before(request,response,controllerMethod);
					}
					result = controllerMethod.invoke(controller, objects);
					System.err.println("Request：[" + path + "]——>["+ controller.getClass().getName() + "."+ controllerMethod.getName() + "()]");
					System.err.println("Result："+result);

					Collections.reverse(interceptors);
					//执行拦截器列表(倒序)After方法
					for (cn.potato.web.Interceptor interceptor : interceptors) {
						interceptor.after(request,response,result);
					}
				}
			} else {
				throw new RuntimeException("没有找到[" + path + "]对应的控制器方法");
			}

			render(request, response, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init() {
		this.mapping = MappingHolder.getMapping("");
	}

	private Method getControllerMethod(Object controller, String name) {
		Method[] controllerMethods = controller.getClass().getDeclaredMethods();
		for (Method controllerMethod : controllerMethods) {
			String methodName = controllerMethod.getName();
			if (name.equals(methodName)) {
				return controllerMethod;
			}
		}
		return null;
	}

	private void render(HttpServletRequest request,
			HttpServletResponse response, Object object) throws Exception {
		if ((object instanceof Result)) {
			Result result = (Result) object;
			Map<String, Object> model = result.getModel();
			for (String key : model.keySet()) {
				request.setAttribute(key, model.get(key));
			}
			String viewPath = DEFAULT_VIEW_PREFIX + result.getViewName() + DEFAULT_VIEW_SUFFIX;
			request.getRequestDispatcher(viewPath).forward(request, response);
		} else {
			String className = "com.alibaba.fastjson.JSON";
			try {
				Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("返回JSON需要添加com.alibaba -> FastJson包的支持");
			}
			String JSONString = JSON.toJSONString(object);
			response.setHeader("application/json", "UTF-8");
			response.getWriter().write(JSONString);
		}
	}
}
