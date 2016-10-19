package cn.potato.annotation;

import cn.potato.web.Interceptor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 如果控制器需要拦截器可以使用该注解
 * @author 李恒名
 * @since 2016年3月4日
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableIntercept
{
  public abstract Class<? extends Interceptor>[] classes();
}