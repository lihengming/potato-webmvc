package cn.potato.web;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Date;

/**
 * 类说明
 * @author 李恒名
 * @since 2016年3月4日
 */
public class Tester {
	public static void main(String[] args) {
		Method[] methods = Tester.class.getDeclaredMethods();
		for (Method method : methods) {
			System.out.println(method.getName());
			Parameter[] parameters = method.getParameters();
			for (Parameter parameter : parameters) {
				System.out.println(parameter.getName());
				System.out.println(parameter.getType().getName());
			}
		}
	}
	public void method1(String p1,Date p2){
		
	}
}

