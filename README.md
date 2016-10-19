## 简介
Potato WebMVC 是用Java实现的一个简单的MVC框架，由于其简单性所以只俱备基本MVC框架的功能和以下特性~

1. 约定优于配置，实现最简甚至零配置
2. Model表单、请求参数、依赖对象自动构造注入所请求的HandleMethod
3. 拦截器
4. JSP View 、JSON响应支持

## 如何使用？

1.引入Maven 依赖
```xml
 <dependency>
    <groupId>com.github.lihengming</groupId>
    <artifactId>potato-webmvc</artifactId>
    <version>1.0.1</version>
</dependency>

<!--如果你需要打印日志的话，引入任何一种SLF4J的实现，例如logback。-->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.1.7</version>
</dependency>
```

2.配置你的web.xml
```xml
<servlet>
    <servlet-name>DispatcherServlet</servlet-name>
    <servlet-class>cn.potato.web.Dispatcher</servlet-class>
    <!--下面的init-param并不是必须的，如果你要使用默认值的话。-->
    <init-param>
      <param-name>basePackage</param-name>
      <!--默认值是根包-->
      <param-value>example.web.controller</param-value>
    </init-param>
    <init-param>
      <param-name>viewPrefix</param-name>
      <!--这是默认值-->
      <param-value>/WEB-INF/views/</param-value>
    </init-param>
    <init-param>
      <param-name>viewSuffix</param-name>
      <!--这是默认值-->
      <param-value>.jsp</param-value>
    </init-param>
  </servlet>

  <servlet-mapping>
    <servlet-name>DispatcherServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>
```

3.创建Controller
```java
package example.web.controller;

public class HomeController {

    //index()映射路径 -  >  ‘/home’
    public Result index(Result result){
        result.setViewName("home");//视图
        result.addData("name", "Potato");//模型
        return result;
    }
}
```

4.创建JSP
Reference:WEB-INF/views/home.jsp
```java
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
  <head>
      <title>Title</title>
      <script src="//cdn.bootcss.com/jquery/1.9.1/jquery.min.js"></script>
  </head>
  <body>
  This Home Page,Hello ${name}!
  </body>
  </html>

```

5.部署后访问它
请求：[http://localhost/home](http://localhost/home)
响应：This Home Page,Hello Potato!
## 使用其他特性
- 使用JSON响应支持构造REST API ，以User服务为例

1.创建Model
```java
public class User {
    private Long id;
    private String username;
    private Integer age;
    //省略getter、setter方法
}
```
2.创建UserController
```java
package example.web.controller;
public class UserController {

    //模拟持久层
    private Map<Long, User> repository = new HashMap<Long, User>();

    //映射路径add() - > ‘/user/add’
    public boolean add(User user) {
        repository.put(user.getId(), user);
        return true;
    }

    //映射路径add() - > ‘/user/list’
    public Collection list() {
        return  repository.entrySet();
    }

    //映射路径add() - > ‘/user/find’
    public User find(Long id) {
        return repository.get(id);
    }

}

```
3.使用AJAX调用API
```javascript
   function add(){
        var url = '/user/add';
        var data = {
            'user.id': 1,
            'user.username': 'potato',
            'user.age': '24'
        };
        $.post(url, data).done(function (success) {
            console.log(success);

        });
    }
    function query(){
        var url = '/user/find';
        $.get(url, {id: 1}).done(function (result) {
            console.log(result);
        });
    }
    add();//console out：true
    query();//console out：Object {age: 24, id: 1, username: "potato"}

```

- 使用拦截器支持做统一的处理

1.创建拦截器Class并实现Interceptor接口
```java
package example.web.interceptor;
/**
 * 示例拦截器，统计方法执行时间
 */
public class ExampleInterceptor implements Interceptor {
    private final ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();

    public void before(HttpServletRequest request, HttpServletResponse response, Method handleMethod) {
        System.out.println("执行方法之前，方法名称：" + handleMethod.getName());
        threadLocal.set(System.currentTimeMillis());
    }

    public void after(HttpServletRequest request, HttpServletResponse response, Object result) {
        long time = System.currentTimeMillis() - threadLocal.get();
        System.out.println("方法执行之后，返回Result：" + result + ",耗时" + time + "毫秒");
    }
}
```
2.在Controller使用@EnableIntercept注解启动拦截器
```java
@EnableIntercept(classes=ExampleInterceptor.class)
public class UserController {}
```
3.测试

请求：[http://localhost/user/list](http://localhost/user/list)
查看控制台输出：
```shell
执行方法之前，方法名称：list
14:13:43.951 [http-bio-80-exec-9] DEBUG cn.potato.web.Dispatcher - Interceptor: invoke example.web.interceptor.ExampleInterceptor.before()
14:13:43.952 [http-bio-80-exec-9] DEBUG cn.potato.web.Dispatcher - ##------------Service Begin--------------##
14:13:43.952 [http-bio-80-exec-9] DEBUG cn.potato.web.Dispatcher - Request: [/user/list] --> [example.web.controller.UserController.list()]
14:13:43.952 [http-bio-80-exec-9] DEBUG cn.potato.web.Dispatcher - Handle Finish!
方法执行之后，返回Result：[1=example.model.User@6e3114f5],耗时1毫秒
14:13:43.952 [http-bio-80-exec-9] DEBUG cn.potato.web.Dispatcher - Interceptor: invoke example.web.interceptor.ExampleInterceptor.after()
14:13:43.952 [http-bio-80-exec-9] DEBUG cn.potato.web.Dispatcher - Response: [Type：JSON],[Data：[{"key":1,"value":{"age":24,"id":1,"username":"potato"}}]]
14:13:43.952 [http-bio-80-exec-9] DEBUG cn.potato.web.Dispatcher - ##------------Service End--------------##
```
## 使用建议
本框架仅限于娱乐，因为它可能只是一个简简单单的玩具轮子，并且随时可能会爆胎，所以并不建议你使用它上路~。
