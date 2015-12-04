package org.lightorm4j.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Title: Entity.java<／p>
 * <p>Description: <／p>
 * @author qiaowei liu
 * @date 2015-12-3
 * @version 1.0
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Entity {
    boolean firstLevelCache() default false;
    boolean secondLevelCache() default true;
	String tableName() default "";
	    
	
	/**
	 * Split format as follow:
	 * Mod:splitField:tableCount
	 * Date:splitField:(y:m)
	 * @return split define
	 */
	String split() default "";

}
