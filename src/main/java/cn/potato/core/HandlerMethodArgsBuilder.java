package cn.potato.core;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.potato.helper.ReflectHelper;

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
				Map<String, Object> object = new HashMap<>();
				for (String subKey : keySet) {
					if (subKey.startsWith(fs[0])) {
						String[] subFs = subKey.split("\\.");
						object.put(subFs[1],
								(String[]) parameterMap.get(subKey));
					}
				}
				requestParamInfo.put(fs[0], object);
			} else {
				requestParamInfo.put(key, parameterMap.get(key));
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
		Object object = null;
		Set<Class<?>> keySet = methodParamInfo.keySet();
		try {
			for (Class<?> clazz : keySet) {
				String key = (String) methodParamInfo.get(clazz);
				if (requestParamInfo.containsKey(key)) {
					Object param = requestParamInfo.get(key);
					if ((param instanceof String[])) {
						String[] value = (String[]) param;
						object = ReflectHelper.changeStringToObject(clazz,
								value[0]);
					} else if ((param instanceof Map)) {
						@SuppressWarnings("unchecked")
						Map<String, Object> map = (Map<String, Object>) param;
						object = clazz.newInstance();
						for (String name : map.keySet()) {
							Field field = null;
							try {
								field = object.getClass()
										.getDeclaredField(name);
							} catch (NoSuchFieldException | SecurityException localNoSuchFieldException) {
							}
							if (field != null) {
								PropertyDescriptor pd = new PropertyDescriptor(
										field.getName(), object.getClass());
								pd.getWriteMethod().invoke(object,
										new Object[] { ReflectHelper.changeStringToObject(pd.getPropertyType(),
										((String[]) map.get(name))[0]) });
							}
						}
					}
				}
				args.add(object);
			}
		} catch (Exception e) {
			log.error("Handler method args inject fail!", e);
		}
	}
}