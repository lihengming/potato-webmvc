package cn.potato.helper;

import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

/**
 * 反射助手类
 * @author 李恒名
 * @since 2016年3月7日
 */
public class ReflectHelper {

	private ReflectHelper() {}
		
	public static 	Map<Class<?>, String> getMethodParameterInfo(Class<?> clazz, String methodName)
			throws Exception {
		Map<Class<?>, String> paramInfoMap = new HashMap<Class<?>, String>();

		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(clazz.getName());
		CtMethod ctMethod = ctClass.getDeclaredMethod(methodName);

		MethodInfo methodInfo = ctMethod.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
	
		CtClass[] parameterTypes = ctMethod.getParameterTypes();
		int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;
		for (int i = 0; i < parameterTypes.length; i++) {
			paramInfoMap.put(Class.forName(parameterTypes[i].getName()),
					attr.variableName(i + pos));
		}
		
		return paramInfoMap;
	}
	
}
