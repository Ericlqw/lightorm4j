package org.lightorm4j.utils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
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
	 private static final String URL ="jdbc:mysql://192.168.75.100:3311/pcbest";
	 private static final String USERNAME = "pcbest";
	 private static final String PASS = "pcbest";
	 private static final String DRIVER ="com.mysql.jdbc.Driver";
    //���·��
	 static String filePath="D:\\myspace\\entity\\";
	//ģ��·��
	 static String templatePath="src\\main\\webapp\\WEB-INF\\tmp";
	 //����	 
	 static String packageName="org.test.freemarker";
	 //����
	 static String author="qiaowei liu";
	//Ŀ�����ݿ��ǰ׺
	 static String table_prefix="pcbest";

	//��ȡ���б���
	static String tableNamesql="show tables";
	// �����ע��
	String commentsql = "SELECT TABLE_COMMENT FROM INFORMATION_SCHEMA.Tables WHERE table_name=?";
	// ��Ҫ����ʵ����ı�       
	String entitysql = "SELECT column_name,column_comment,data_type,column_key,is_nullable FROM INFORMATION_SCHEMA.Columns WHERE table_name= ?";
	static int total=0;		
	 //����import
	 int importDate=0;
	/*
	 * ���캯��
	 */
	public GenEntity() throws TemplateException, IOException {
		
		String debug="";
     	// ��������
		Connection con = null;
		PreparedStatement pStemt = null;
		ResultSet rs = null;	
		try {		
			con = getConn();
			/***************��ȡ���б���**************/
			List<String> tableNameList=getTableName(con);
			
			for(String tablename:tableNameList){
				debug=tablename;
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
				
				
				/***************�����ע��S**************/
				pStemt = con.prepareStatement(commentsql);
				pStemt.setString(1, tablename);
				rs = pStemt.executeQuery();
				String tableComment = "";
				while (rs.next()) {
					tableComment = rs.getString("TABLE_COMMENT");
				}
				
				/***************�����ע��E**************/

				
				/***************����field��type S**************/
				pStemt = con.prepareStatement(entitysql);
				pStemt.setString(1, tablename);
				rs = pStemt.executeQuery();		
				// ����field
				List<Attr> list = new ArrayList<Attr>();

				while (rs.next()) {
					Attr attr = new Attr();
					attr.setField(rs.getString("column_name"));
					attr.setType(sqlType2JavaType(rs.getString("data_type")));
					attr.setComment(rs.getString("column_comment"));
					attr.setPrimaryKey(rs.getString("column_key"));
					attr.setNullable(rs.getString("is_nullable"));
					list.add(attr);
				}
				
				/***************����field��type E**************/
				
				/***************����import S**************/
			 	
				for (Attr attr : list) {
					
					if (null!=attr.getType() && attr.getType().equalsIgnoreCase("Date")) {
						importDate++;
					}
				}
				
				/***************����import E**************/

				
				/***************freemarker ����Entity**************/
				Configuration cfg = new Configuration();
				// ����FreeMarker��ģ���ļ���λ��
				cfg.setDirectoryForTemplateLoading(new File(templatePath));

				Template t = cfg.getTemplate("beanTemplate.ftl");
				Map<String, Object> root = new HashMap<String, Object>();
				root.put("packageName",packageName);
				root.put("author", author);
				root.put("className", className);
				root.put("tableName", tablename);
				root.put("tableComment", tableComment);
				root.put("importDate", importDate);
				root.put("date",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
				root.put("attrs", list);

				File targetFile = new File(filePath + className+ ".java");
				FileWriter fw = new FileWriter(targetFile);
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
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.print(debug);
		} finally {
			try {
				if(rs!=null){
					rs.close();
				}
				if (pStemt != null)
					pStemt.close();
				if (con != null)
					con.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	 * @return
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
		}else if(sqlType.equalsIgnoreCase("bigint")){
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
		}else if(sqlType.equalsIgnoreCase("datetime")|| sqlType.equalsIgnoreCase("timestamp")){
			return "Date";
		}else if(sqlType.equalsIgnoreCase("image")){
			return "Blod";
		}
		
		return null;
	}
	
	public static List<String> getTableName(Connection conn) throws SQLException{ 	
    	PreparedStatement	ps = (PreparedStatement) conn.prepareStatement(tableNamesql);
		ResultSet rs = ps.executeQuery();
		List<String> tableNameList = new ArrayList<String>();
		while (rs.next()) {
			if(rs.getString(1).startsWith(table_prefix)){
			tableNameList.add(rs.getString(1));
			}
		}		
		return tableNameList;
	}
	
	public static Connection getConn(){
		Connection conn=null;
		try{
			Class.forName(DRIVER);
			conn=DriverManager.getConnection(URL, USERNAME, PASS);
			if(conn!=null){
				System.out.println("The connection to database is successful!");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return conn;
	}
    
	public static void main(String[] args) {
		
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
