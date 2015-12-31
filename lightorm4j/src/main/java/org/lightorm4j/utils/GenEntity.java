package org.lightorm4j.utils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.lightorm4j.entity.Attr;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/** 
* @author qiaowei liu
* @date 2015��12��11�� ����4:10:54 
* @Description 
*/ 

public class GenEntity {
	
    //���ݿ�����
	 private static String DBURL ;
	 private static  String USERNAME ;
	 private static  String PASSWORD ;
	 private static  String DRIVER;
	 private static String properties;
    //���·��
	 static String outputPath="entity/";
	//ģ��·��
	 static String templatePath="tmp/";
	//ģ������
	 static String templateName;
	 //����	 
	 static String packageName;
	 
	 //Ŀ���
	 static String  tableList;
	 //����
	 static String author="admin";
	 static String classNamePrefix;

	// �����ע��,dbMetaDat�ó����ı�ע���ǿյģ�ֻ�ܷ�sqlȥ��
	 String commentsql = "SELECT TABLE_COMMENT FROM INFORMATION_SCHEMA.Tables WHERE table_name=?";
	 static int total=0;		
	 //����import
	 int importDate=0;
	 int importTimestamp=0;
	 int importBlob=0;
	 
	 public void init() throws IOException{
		    Properties prop = new Properties();   		    
	        InputStream in =  new FileInputStream(properties);   	       
	            prop.load(in);  
	            DRIVER=prop.getProperty("DRIVER").trim();
	            DBURL=prop.getProperty("DBURL").trim();
	            USERNAME=prop.getProperty("USERNAME").trim();
	            PASSWORD=prop.getProperty("PASSWORD").trim();	
	            
	            tableList=prop.getProperty("TableList");		          	          
	            templateName = prop.getProperty("templateName"); 
	            packageName= prop.getProperty("packageName"); 
	            classNamePrefix=prop.getProperty("classNamePrefix");             
	            author= prop.getProperty("author");                
	 }
	  
	 public String  handleClassName(String tablename){

			/***************����className S**************/
			String className = "";
			String[] splitClaseName = tablename.split("_");

			if (splitClaseName.length > 0) {
				for (int i = 1; i < splitClaseName.length; i++) {
					className += initcap(splitClaseName[i]);
				}
			} else {
				className = initcap(tablename);
			}
			/***************����className E**************/
			
			return className;
	 }
	 
	 public List<Attr> handleColumns(Connection con,String tablename){
		List<Attr> list = new ArrayList<Attr>();
		//��������
		  ResultSet dateSet = null;
		try {
			DatabaseMetaData dbMetaData = con.getMetaData(); 
			dateSet = dbMetaData.getPrimaryKeys(null,null,tablename);
		
		    String pkKey="";
			while(dateSet.next()){						
				pkKey=dateSet.getString("COLUMN_NAME");
				break;
			}	
			//��������field
			dateSet = dbMetaData.getColumns(null, null,tablename, "%");    
			while (dateSet.next()) {
				Attr attr = new Attr();
				attr.setField(dateSet.getString("COLUMN_NAME"));
				attr.setType(sqlType2JavaType(dateSet.getString("TYPE_NAME").toLowerCase()));
				attr.setComment(dateSet.getString("REMARKS"));
				attr.setPrimaryKey(pkKey);			
				attr.setNullable(dateSet.getString("IS_NULLABLE"));
				list.add(attr);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(null!=dateSet)
				try {
					dateSet.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}				
			return list;
	 }
	 
	 /** 
	* @Description ˼·�Ƕ���һ�������������Date������;�++�����ģ�����ʱ�ñ���>0���������
	* @author qiaowei liu
	* @date 2015��12��16�� ����4:34:36 
	* @param list
	*/ 
	public void handleImport(List<Attr> list){
		 for (Attr attr : list) {
				
				if (null!=attr.getType() && attr.getType().equalsIgnoreCase("Date")) {
					importDate++;
				}
				if (null!=attr.getType() && attr.getType().equalsIgnoreCase("Timestamp")) {
					importTimestamp++;
				}
				if (null!=attr.getType() && attr.getType().equalsIgnoreCase("Blob")) {
					importBlob++;
				}
			}
	 }
	
	
	public void createEntity(String className,String tablename,String tableComment,List<Attr> list) throws IOException, TemplateException{
		/***************freemarker ����Entity**************/
		Configuration cfg = new Configuration();
		if(null!=classNamePrefix && classNamePrefix.length()>0){
			className=classNamePrefix+className;
		}
		File targetFile = new File(outputPath + className+ ".java");
		FileWriter fw = new FileWriter(targetFile);
		
		
		// ����FreeMarker��ģ���ļ���λ��
		cfg.setDirectoryForTemplateLoading(new File(templatePath));

		Template t = cfg.getTemplate(templateName);
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("packageName",packageName);
		root.put("author", author);
		root.put("className", className);
		root.put("tableName", tablename);
		root.put("tableComment", tableComment);
		root.put("importDate", importDate);
		root.put("importTimestamp", importTimestamp);	
		root.put("importBlob", importBlob);		
		root.put("date",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		root.put("attrs", list);

		try {
			BufferedWriter bw = new BufferedWriter(fw);
			t.process(root, bw);
			total++;
			System.out.println("create Entity "+className+".java successful");	
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fw.close();
		}
		
	}
	 
	/*
	 * ���캯��
	 */
	public GenEntity() throws TemplateException, IOException {	
		
		init();
		Connection con = null;
		ResultSet tablesSet=null;
		con = getConn();
		  
        String [] tables = null;
        if(null!=tableList && tableList.length()>0){
        tables =tableList.split(",");
        }
		try {		
			if(tables!=null && tables.length>0){
				for(String tablename:tables){
					 genEntityByTable(tablename,con);	
				}			
			}else{			
			   DatabaseMetaData dbMetaData = con.getMetaData();   
			   String[] types = { "TABLE" };    
			   tablesSet= dbMetaData.getTables(null, null,null, types);    
			   while (tablesSet.next()) {    
			       String tablename = tablesSet.getString("TABLE_NAME");  //����    				    			  
			       genEntityByTable(tablename,con);			
			   	}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {				
				if(tablesSet!=null) tablesSet.close();				
				if (con != null) con.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public void genEntityByTable(String tablename,Connection con) {
		
		PreparedStatement pStemt = null;
		ResultSet dateSet = null;
		
		
		if(!tablename.startsWith("gl_")){
		try{	
			//����className 
			String className = handleClassName(tablename);
						
			/***************�����ע��S**************/
			pStemt = con.prepareStatement(commentsql);
			pStemt.setString(1, tablename);
			
			dateSet = pStemt.executeQuery();
			String tableComment = "";
			while (dateSet.next()) {
				tableComment = dateSet.getString("TABLE_COMMENT");
			}
							
			/***************�����ע��E**************/		
			//����Columns			
			List<Attr> list = handleColumns(con,tablename);
			//����import				
			handleImport(list);
			//����Entity
			createEntity(className, tablename, tableComment, list);												
		 }
		catch(Exception e){
			 e.printStackTrace();
		}finally{
			try {
			if(null!=dateSet) dateSet.close();
			if(null!=pStemt) pStemt.close();

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		   }
		}
	}
	
	/**
	 * ���ܣ��������ַ���������ĸ�ĳɴ�д
	 * @param str
	 * @return
	 */
	private String initcap(String str) {
		char[] ch = str.toCharArray();
		if (ch[0] >= 'a' && ch[0] <= 'z') {
			ch[0] = (char) (ch[0] - 32);
		}
		return new String(ch);
	}

	/**
	 * ���ܣ�����е���������
	 * @param sqlType
	 * @return ���û��Ӧ�����ͣ�ֱ�ӷ���SQLType���������ӽ���
	 */
	private String sqlType2JavaType(String sqlType) {
		
		if(sqlType.equalsIgnoreCase("bit")){
			return "boolean";
		}else if(sqlType.equalsIgnoreCase("tinyint")){
			return "byte";
		}else if(sqlType.equalsIgnoreCase("smallint")){
			return "short";
		}else if(sqlType.equalsIgnoreCase("int")){
			return "int";
		}else if(sqlType.equalsIgnoreCase("bigint") || sqlType.equalsIgnoreCase("int unsigned") ){
			return "long";
		}else if(sqlType.equalsIgnoreCase("float")){
			return "float";
		}else if(sqlType.equalsIgnoreCase("decimal") || sqlType.equalsIgnoreCase("numeric") 
				|| sqlType.equalsIgnoreCase("real") || sqlType.equalsIgnoreCase("money") 
				|| sqlType.equalsIgnoreCase("smallmoney") || sqlType.equalsIgnoreCase("double")){
			return "double";
		}else if(sqlType.equalsIgnoreCase("varchar") || sqlType.equalsIgnoreCase("char") 
				|| sqlType.equalsIgnoreCase("nvarchar") || sqlType.equalsIgnoreCase("nchar") 
				|| sqlType.equalsIgnoreCase("text")){
			return "String";
		}else if( sqlType.equalsIgnoreCase("date")|| sqlType.equalsIgnoreCase("year")
				|| sqlType.equalsIgnoreCase("datetime") || sqlType.equalsIgnoreCase("timestamp")){
			return "Date";
			
		}else if(sqlType.equalsIgnoreCase("Blob")){
			return "byte[]";
		}
		return sqlType;		
	}
		
	public static Connection getConn(){
		Connection conn=null;
		try{
			Class.forName(DRIVER);
			conn=DriverManager.getConnection(DBURL, USERNAME, PASSWORD);
			if(conn!=null){
				System.out.println("The connection to database is successful!");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return conn;
	}
    
	public static void main(String[] args) {
		
		  if(args.length>0){
			  properties=  args[0];
			  System.out.println(properties);
		  }else{
			  System.out.println("����ȷ����properties��·��");
		  }
		  
		 //ҪdebugԴ������ָ�� ���¼���·��
	    /*	properties="C:\\Users\\tpy\\Desktop\\entityGenerator\\conf\\genentity.properties";
		  //���·��
		  outputPath="C:\\Users\\tpy\\Desktop\\entityGenerator\\entity\\";
		  //ģ��λ��
		  templatePath="C:\\Users\\tpy\\Desktop\\entityGenerator\\tmp";*/
		  
		try {
			new GenEntity();
			System.out.println("total:"+total);
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
