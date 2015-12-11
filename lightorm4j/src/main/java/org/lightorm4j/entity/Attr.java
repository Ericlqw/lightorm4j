package org.lightorm4j.entity;
/**
 * <p>Title: Attrs.java<／p>
 * <p>Description: <／p>
 * @author qiaowei liu
 * @date 2015-12-10
 * @version 1.0
 */

	public class Attr{
	    private String field;
	    private String type;
	    private String primaryKey;
	    private String comment;
	    private String nullable;
	    
	    public String getField(){
	        return this.field;
	    }
	     
	    public String getType(){
	        return this.type;
	    }
	     
	    public void setField(String field){
	        this.field = field;
	    }
	     
	    public void setType(String type){
	        this.type = type;
	    }

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String getPrimaryKey() {
			return primaryKey;
		}

		public void setPrimaryKey(String primaryKey) {
			this.primaryKey = primaryKey;
		}

		public String getNullable() {
			return nullable;
		}

		public void setNullable(String nullable) {
			this.nullable = nullable;
		}		
		
	}
