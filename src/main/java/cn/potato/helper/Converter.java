package cn.potato.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 类型转换器
 * 
 * @author 李恒名
 * @since 2016年3月7日
 */
public class Converter {

	private Converter() {
	}

	public static Object convertStringToObject(Class<?> targetType, String value) {
		Object object = null;
		if ((targetType == Byte.TYPE) || (targetType == Byte.class)) {
			object = Byte.valueOf(Byte.parseByte(value));
		} else if ((targetType == Short.TYPE) || (targetType == Short.class)) {
			object = Short.valueOf(Short.parseShort(value));
		} else if ((targetType == Integer.TYPE) || (targetType == Integer.class)) {
			object = Integer.valueOf(Integer.parseInt(value));
		} else if ((targetType == Long.TYPE) || (targetType == Long.class)) {
			object = Long.valueOf(Long.parseLong(value));
		} else if ((targetType == Double.TYPE) || (targetType == Double.class)) {
			object = Double.valueOf(Double.parseDouble(value));
		} else if ((targetType == Float.TYPE) || (targetType == Float.class)) {
			object = Float.valueOf(Float.parseFloat(value));
		} else if ((targetType == Boolean.TYPE) || (targetType == Boolean.class)) {
			object = Boolean.valueOf(Boolean.parseBoolean(value));
		} else if (targetType == Date.class) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				object = dateFormat.parse(value);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (targetType == String.class) {
			object = value;
		}
		return object;
	}

	public static Object convertNumberType(Object number,Class<?> targetType){
		if(targetType == Byte.class||targetType == Byte.TYPE){
			number = ((Number)number).byteValue();
		}else if(targetType == Short.class||targetType == Short.TYPE){
			number = ((Number)number).shortValue();
		}else if(targetType == Integer.class||targetType == Integer.TYPE){
			number = ((Number)number).intValue();
		}else if(targetType == Long.class||targetType == Long.TYPE){
			number = ((Number)number).longValue();
		}else if(targetType == Float.class||targetType == Float.TYPE){
			number = ((Number)number).floatValue();
		}else if(targetType == Double.class||targetType == Double.TYPE){
			number = ((Double)number).doubleValue();
		}else{
			throw new RuntimeException("不支持["+targetType.getName()+"]类型的转换!");
		}
		return number;
	}
	
}
