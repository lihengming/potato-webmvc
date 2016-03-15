package cn.potato.core;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 访问路径与控制器对象的映射关系持有者
 * @author 李恒名
 * @since 2016年3月4日
 */
public class MappingHolder{
	private static final Logger log = LoggerFactory.getLogger(MappingHolder.class);
	private static final String FILE_SUFFIX = "Controller.class";//要扫描的文件后缀
	private static String baseFilePath;//扫描文件的基础路径
	private static String basePackage;//要扫描的包
	private static List<String> ControllerClassNameList;
	private static Map<String, Object> mapping ;
	
	private MappingHolder() {}
	
	public static Map<String, Object> getMapping(String basePackage) {
		MappingHolder.basePackage = basePackage!=null?basePackage:"";
		if(mapping==null)
			initMapping();
		return mapping;
	}

	private static void initMapping() {
		log.info("Initialize controller mapping begin.");
		log.info("Controller basePackage: "+basePackage);
		mapping = new HashMap<>();
		ControllerClassNameList = new ArrayList<>() ;
		String basePath = basePackage.replaceAll("\\.", "/");
		try {
			URI uri = Thread.currentThread().getContextClassLoader()
					.getResource(basePath).toURI();
			File file = new File(uri);
			baseFilePath = file.getPath();
			initControllerClassNameList(file);
			for (String className : ControllerClassNameList) {
				Class<?> clazz = Class.forName(className);
				String requestPath = "/"
						+ className.substring(className.lastIndexOf(".") + 1,
								className.lastIndexOf("Controller")).toLowerCase();
				mapping.put(requestPath, clazz.newInstance());
				log.info("Add in mapping: ["+requestPath+" -> "+clazz.getName()+"]");
			}
			log.info("Initialize controller mapping finish.");
		} catch (Exception e) {
			throw new RuntimeException("[控制器路径映射,初始化失败!]->"+e.getMessage());
		}
		
	}
	//
	private  static void initControllerClassNameList(File file) {
		if (file.isDirectory()) {
			File[] listFiles = file.listFiles();
			for (File childFile : listFiles)
				initControllerClassNameList(childFile);
		} else {
			String path = file.getPath();
			if (path.endsWith(FILE_SUFFIX)) {
				path = path.substring(baseFilePath.length(),
						path.lastIndexOf("."));
				path = path.replaceAll("\\\\", ".");
				String className =basePackage+path;
				if(className.startsWith("."))
					className=className.substring(1);
				ControllerClassNameList.add(className);
			}
		}
	}
}
