package org.lightorm4j.dao;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

/**
 * <p>Title: LightOrm.java<／p>
 * <p>Description: <／p>
 * @author qiaowei liu
 * @date 2015-12-3
 * @version 1.0
 */
public class LightOrm {

	 public void setEntityPackageList(List<String> entityPackageList) {
	        for (String entityPackage : entityPackageList) {
	            setEntityPackage(entityPackage);
	        }
	    }
	    
	 public void setEntityPackage(String entityPackage) {
	        ClassPathScanningCandidateComponentProvider scan = 
	                new ClassPathScanningCandidateComponentProvider(false);

	        scan.addIncludeFilter(new TypeFilter() {
	            public boolean match(MetadataReader metadataReader, 
	                    MetadataReaderFactory metadataReaderFactory) throws IOException {
	                return true;
	            }				
	        });

	        List<Class> list = new ArrayList<Class>();
	        for (BeanDefinition candidate : scan.findCandidateComponents(entityPackage)) {
	            try {
	                Class cls = ClassUtils.resolveClassName(candidate.getBeanClassName(),
	                        ClassUtils.getDefaultClassLoader());
	                list.add((Class) cls);
	            } catch (IllegalArgumentException ex) {
	                throw new RuntimeException(ex);
	            }
	        }

	        setEntityList(list);
	    }
	 
	 public void setEntityList(List<Class> typeList) {
	        for (Class type : typeList) {
	            registerEntity(type);
	        }
	    }
	 
	 public void registerEntity(Class type) {
		 
	     //TODO
	    }
	 
	
}
