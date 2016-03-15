package cn.potato.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.potato.annotation.Intercept;
import cn.potato.core.HandlerMethodArgsBuilder;
import cn.potato.core.MappingHolder;

import com.alibaba.fastjson.JSON;

/**
 * 分配器
 * 通过请求地址，分发请求到对应的控制器。
 * @author 李恒名
 * @since 2016年3月4日
 */
public class Dispatcher extends HttpServlet {
	private static final long serialVersionUID = -1217007459141119979L;
	private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);
	private static final String DEFAULT_CONTROLLER_METHOD = "index";
	private static final String DEFAULT_VIEW_PREFIX = "/jsp/";
	private static final String DEFAULT_VIEW_SUFFIX = ".jsp";
	private Map<String, Object> mapping;
	
	//初始化Controller路径映射
	@Override
	public void init() {
		String basePackage = getInitParameter("basePackage");
		this.mapping = MappingHolder.getMapping(basePackage);
	}
	
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String requestURI = request.getRequestURI();
		String contextPath = request.getServletContext().getContextPath();
		String path = requestURI.substring(contextPath.length()).toLowerCase();
		String[] names = path.substring(1).split("/");
		Object controller = this.mapping.get("/" + names[0]);
		String name = names.length > 1 ? names[1] : DEFAULT_CONTROLLER_METHOD;// /user -〉defult /user/list -〉list
		try {
			Method handlerMethod = gethandlerMethod(controller, name);
			if (handlerMethod != null) {
				 doDispatch(request, response, controller, handlerMethod);
			} else {
				log.error("没有找到[" + path + "]对应的处理方法");
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
	
	
	
	
	
	/**
	 * 将请求分配给对应的处理方法进行处理
	 * @author 李恒名
	 * @since 2016年3月15日
	 * @param request
	 * @param response
	 * @param controller
	 * @param handlerMethod
	 * @throws Exception
	 */
	private void doDispatch(HttpServletRequest request,
			HttpServletResponse response, Object controller,
		 Method handlerMethod)
			throws Exception {
		//取得拦截器列表
		List<Interceptor> interceptors = getInterceptors(controller);
		//执行拦截器列表(正序)Before方法
		for (Interceptor interceptor : interceptors) {
			interceptor.before(request,response,handlerMethod);
			log.info("Interceptor: invoke "+interceptor.getClass().getName()+".before()");
		}
		
		//构造HandlerMethod的参数
		Object[] args = new HandlerMethodArgsBuilder(request,handlerMethod,controller).build();
		
		log.info("Request: ["+request.getRequestURI()+"] --> ["+controller.getClass().getName()+"."+handlerMethod.getName()+"()]");
		Object result = handlerMethod.invoke(controller, args);
		log.info("Handle Finish!");
		
		Collections.reverse(interceptors);
		//执行拦截器列表(倒序)After方法
		for (Interceptor interceptor : interceptors) {
			interceptor.after(request,response,result);
			log.info("Interceptor: invoke "+interceptor.getClass().getName()+".after()");
		}
		
		//渲染数据进行响应
		render(request,response,result);
		
	}
	
	/**
	 * 获得拦截器列表
	 * @author 李恒名
	 * @since 2016年3月15日
	 * @param controller
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private List<Interceptor> getInterceptors(Object controller)
			throws InstantiationException, IllegalAccessException {
		Intercept annotation = (Intercept) controller.getClass()
				.getAnnotation(Intercept.class);
		List<Interceptor> interceptors = new ArrayList<>();

		if (annotation != null) {
			Class<? extends Interceptor>[] classes = annotation
					.classes();
			for (Class<? extends Interceptor> clazz : classes) {
				interceptors.add(clazz.newInstance());
			}
		}
		return interceptors;
	}
	
	/**
	 * 获得对应路径的处理方法
	 * @author 李恒名
	 * @since 2016年3月15日
	 * @param controller
	 * @param name
	 * @return
	 */
	private Method gethandlerMethod(Object controller, String name) {
		if(controller!=null){
			Method[] handlerMethods = controller.getClass().getDeclaredMethods();
			for (Method handlerMethod : handlerMethods) {
				String methodName = handlerMethod.getName();
				if (name.equals(methodName)) {
					return handlerMethod;
				}
			}
		}
		return null;
	}
	
	/**
	 * 方法说明
	 * 根据Result进行响应
	 * @author 李恒名
	 * @since 2016年3月15日
	 * @param request
	 * @param response
	 * @param object
	 * @throws Exception
	 */
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
			log.info("Response: [Type：PageView],[Path："+viewPath+"]");
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
			log.info("Response: [Type：JSON],[Data："+JSONString+"]");
		}
	}
	
}
