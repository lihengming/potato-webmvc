package cn.potato.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 反射助手类
 * 
 * @author 李恒名
 * @since 2016年3月7日
 */
public class ReflectHelper {

	private ReflectHelper() {
	}

	public static Object changeStringToObject(Class<?> type, String value) {
		Object object = null;
		if ((type == Byte.TYPE) || (type == Byte.class)) {
			object = Byte.valueOf(Byte.parseByte(value));
		} else if ((type == Short.TYPE) || (type == Short.class)) {
			object = Short.valueOf(Short.parseShort(value));
		} else if ((type == Integer.TYPE) || (type == Integer.class)) {
			object = Integer.valueOf(Integer.parseInt(value));
		} else if ((type == Long.TYPE) || (type == Long.class)) {
			object = Long.valueOf(Long.parseLong(value));
		} else if ((type == Double.TYPE) || (type == Double.class)) {
			object = Double.valueOf(Double.parseDouble(value));
		} else if ((type == Float.TYPE) || (type == Float.class)) {
			object = Float.valueOf(Float.parseFloat(value));
		} else if ((type == Boolean.TYPE) || (type == Boolean.class)) {
			object = Boolean.valueOf(Boolean.parseBoolean(value));
		} else if (type == Date.class) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				object = dateFormat.parse(value);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (type == String.class) {
			object = value;
		}
		return object;
	}

}
