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
	
	    private final Map<Class, String> tableNameMap = new HashMap<Class, String>();
	    private final Map<Class, Table> tableMap = new HashMap<Class, Table>();
	    private final Map<Class, String> getterMap = new HashMap<Class, String>();

	    private final Map<Class, String> setterMap = new HashMap<Class, String>();

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
		 
	        prepareTableMap(type);

	       /* prepareCacheMap(type);

	        prepareIdAllocSizeMap(type);

	        prepareKeyMap(type);

	        prepareSetterGetterMap(type);

	        prepareTableMap(type);

	        prepareGetIdMap(type);
	        prepareSetIdMap(type);

	        prepareCreateSqlMap(type);
	        prepareUpdateSqlMap(type);
	        prepareDeleteSqlMap(type);
	        prepareSelectSqlMap(type);

	        prepareDisplayMethodMap(type);

	        prepareNotOperationMap(type);

	        prepareLabelMap(type);
	        entityMap.put(type.getSimpleName().toUpperCase(), type);
	        setDaoListener(new GeliDaoListener(type));*/
	    }
	 
	  private void prepareTableMap(Class type) {
	        Entity annotation = (Entity) type.getAnnotation(Entity.class);
	        Table table = null;
	        String tableName = null;
	        if (annotation != null) {
	            if (!"".equals(annotation.tableName())) {
	                tableName = annotation.tableName();
	            }

//	            if (annotation.logChange()) {
//	                logChangeMap.put(type, Boolean.TRUE);
//	            }

	            String split = annotation.split();
	            if (!"".equals(split)) {
	                table = new Table(split);
	              /*  List<ValueGetter> getterList = getterMap.get(type);
	                for (ValueGetter getter : getterList) {
	                    if (getter.getFieldName().equals(table.getSplitField())) {
	                        table.setSplitMethod(getter.getMethod());
	                        break;
	                    }
	                }*/
	                if (table.getSplitMethod() == null) {
	                    throw new IllegalStateException(type.getSimpleName()
	                            + " split field:" + table.getSplitField() + 
	                            " not found!");
	                }
	            }
	        } 

	       /* if (tableName == null) {
	            String className = type.getSimpleName();
	            tableName = tablePrefix + Character.toLowerCase(className.charAt(0))
	                    + className.substring(1);
	        }*/

	        tableNameMap.put(type, tableName);
	        if (table != null) {
	            table.setName(tableName);
	            tableMap.put(type, table);
	        }
	    }

	 /* private void prepareSetterGetterMap(Class type) {
	        List<ValueSetter> list = new ArrayList<ValueSetter>();
	        List<ValueGetter> list2 = new ArrayList<ValueGetter>();
	        List<String> fieldList = new ArrayList<String>();
	        List<String> columnList = new ArrayList<String>();
	        List<String> transientList = new ArrayList<String>();

	        for (Field field : type.getDeclaredFields()) {
	            int modifiers = field.getModifiers();
	            Transient tt = field.getAnnotation(Transient.class);
	            if (! isSupportedProperty(field.getType())) {
	                continue;
	            } else {
	                if (tt != null && tt.cache()) {
	                    transientList.add(field.getName());
	                }
	            }

	            if (tt == null && !Modifier.isTransient(modifiers)
	                    && !Modifier.isStatic(modifiers)) {

	                String fieldName = field.getName();
	                String columnName = fieldName;
	                Method getter = getter(type, field);
	                Method setter = setter(type, field);
	                Column annotation = (Column) field.getAnnotation(Column.class);
	                Label label = (Label) field.getAnnotation(Label.class);
	                if (annotation != null) {
	                    columnName = annotation.name();
	                    field2columnMap.put(type.getSimpleName() + '-' + fieldName,
	                            columnName);
	                }
	                if (label != null) {
	                    field2labelMap.put(type.getSimpleName() + '-' + fieldName,
	                            label.value());
	                }
	                if (getter != null && setter != null) {
	                    ValueSetter vs = new ValueSetter(setter, fieldName, columnName);
	                    ValueGetter vg = new ValueGetter(getter, fieldName, columnName);

	                    Refer refer = field.getAnnotation(Refer.class);
	                    if (refer != null) {
	                        vs.setFieldPath(refer.fieldPath());
	                        vg.setFieldPath(refer.fieldPath());
	                        vg.setReferType(refer.type());
	                    }
	                    vs.setGetter(vg);
	                    if (vs.isCreateAt()) { 
	                        createAtMap.put(type, vs); 
	                        addNotOperation(type, fieldName, notCreateMap);
	                        addNotOperation(type, fieldName, notUpdateMap);
	                    }
	                    if (vs.isCreateById()) { 
	                        createByIdMap.put(type, vs); 
	                        addNotOperation(type, fieldName, notCreateMap);
	                        addNotOperation(type, fieldName, notUpdateMap);
	                    }
	                    if (vs.isUpdateAt()) { 
	                        updateAtMap.put(type, vs); 
	                        addNotOperation(type, fieldName, notCreateMap);
	                        addNotOperation(type, fieldName, notUpdateMap);
	                    }
	                    if (vs.isUpdateById()) {
	                        updateByIdMap.put(type, vs); 
	                        addNotOperation(type, fieldName, notCreateMap);
	                        addNotOperation(type, fieldName, notUpdateMap);
	                    }
	                    list.add(vs);
	                    list2.add(vg);
	                    fieldList.add(fieldName);
	                    columnList.add(columnName);
	                    
	                    if (field.getAnnotation(NotCreate.class) != null) {
	                        addNotOperation(type, fieldName, notCreateMap);
	                    }
	                    if (field.getAnnotation(NotUpdate.class) != null) {
	                        addNotOperation(type, fieldName, notUpdateMap);
	                    }
	                }
	            }
	        }
	        if (list.size() > 0) {
	            setterMap.put(type, list);
	            getterMap.put(type, list2);
	            fieldListMap.put(type, fieldList);
	            columnListMap.put(type, columnList);
	            String[] props = new String[list.size() + transientList.size()];
	            int i = 0;
	            for (; i < list.size(); i ++) {
	                props[i] = list.get(i).getFieldName();
	            }
	            for (int l = i; i < props.length; i ++) {
	                props[i] = transientList.get(i - l);
	            }
	            SimplePropertyPreFilter pf = new SimplePropertyPreFilter(props);
	            propPreFilterMap.put(type, pf);
	        }
	    }*/
}
