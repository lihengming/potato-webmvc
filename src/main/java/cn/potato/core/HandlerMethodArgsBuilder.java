package cn.potato.core;

import cn.potato.helper.ConvertHelper;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;

/**
 * HandlerMethodArgsBuilder 通过反射实现控制器方法参数的注入，由于JDK 1.7 反射无法获取参数名称故采用 javassist。
 */
public class HandlerMethodArgsBuilder {
	private Map<String, Object> requestParamInfo;
	private Map<Class<?>, String> methodParamInfo;
	private List<Object> args;
	private static final Logger log = LoggerFactory.getLogger(HandlerMethodArgsBuilder.class);

	public HandlerMethodArgsBuilder(HttpServletRequest request, Method handler,
			Object controller) {
		initRequestParamInfo(request);
		initMethodParamInfo(controller, handler.getName());
	}

	public Object[] build() {
		inject();
		return args.toArray();
	}

	private void initRequestParamInfo(HttpServletRequest request) {
		requestParamInfo = new HashMap<String, Object>();
		Map<String, String[]> parameterMap = request.getParameterMap();
		Set<String> keySet = parameterMap.keySet();
		for (String key : keySet)
			if (key.contains(".")) {
				String[] fs = key.split("\\.");
				Map<String, Object> beanMap = new HashMap<>();
				for (String subKey : keySet) {
					if (subKey.startsWith(fs[0])) {
						String[] subFs = subKey.split("\\.");
						beanMap.put(subFs[1],parameterMap.get(subKey)[0]);
					}
				}
				requestParamInfo.put(fs[0], beanMap);
			} else {
				requestParamInfo.put(key, parameterMap.get(key)[0]);
			}
	}

	private void initMethodParamInfo(Object controller, String methodName) {
		methodParamInfo = new LinkedHashMap<Class<?>, String>();
		Class<?> clazz = controller.getClass();
		try {
			ClassPool classPool = ClassPool.getDefault();
			classPool.insertClassPath(new ClassClassPath(clazz));
			CtClass ctClass = classPool.get(clazz.getName());
			CtMethod ctMethod = ctClass.getDeclaredMethod(methodName);

			MethodInfo methodInfo = ctMethod.getMethodInfo();
			CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
			LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
					.getAttribute("LocalVariableTable");

			CtClass[] parameterTypes = ctMethod.getParameterTypes();
			int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;
			for (int i = 0; i < parameterTypes.length; i++)
				methodParamInfo.put(Class.forName(parameterTypes[i].getName()),
						attr.variableName(i + pos));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private void inject() {
		args = new ArrayList<Object>();
		Set<Class<?>> paramTypeClasses = methodParamInfo.keySet();
		try {
			for (Class<?> clazz : paramTypeClasses) {
				String key = (String) methodParamInfo.get(clazz);
				Object object;
				if (requestParamInfo.containsKey(key)) {
					Object value = requestParamInfo.get(key);
					if ((value instanceof Map)) {//如果为Map型的转换为Bean
						@SuppressWarnings("unchecked")
						Map<String, Object> map = (Map<String, Object>) value;
						object  = ConvertHelper.mapConvertToBean(map, clazz);
					}else{
						object = ConvertHelper.convert(value, clazz);
					}
				}else{
					object = clazz.newInstance();
				}
				args.add(object);
			}
		} catch (Exception e) {
			log.error("Handler method args inject fail!", e);
		}
	}
}