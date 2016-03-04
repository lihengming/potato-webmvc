package cn.potato.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类说明
 * @author 李恒名
 * @since 2016年3月4日
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Interceptor
{
  public abstract Class<? extends cn.potato.web.Interceptor>[] classes();
}