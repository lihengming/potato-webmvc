package cn.potato.helper;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;


/**
 * Bean操作助手
 * @author 李恒名
 * @since 2016年3月18日
 */
public class BeanHelper extends Helper{
	private BeanHelper(){}
	
	public static void setProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException{
		 BeanUtils.setProperty(bean, name, value);
	  }
}
