package ${packageName};
import org.gelivable.dao.Entity;
import org.gelivable.dao.Id;
import org.gelivable.dao.Label;
import org.hibernate.validator.constraints.NotEmpty;
 <#if (importDate> 0)>
import java.util.Date;  
 </#if>
 
/**
 * <p>Title:  ${className}.java<／p>
 * <p>Description: <／p>
 * @author ${author}
 * @date ${date}
 * @version 1.0
 */
@Entity(tableName="${tableName}",logChange=true)
@Label("${tableComment}")
pulic class ${className} {
    <#list attrs as entity>     
     <#if (entity.primaryKey==entity.field)>
    @Id
    </#if>
     <#if (entity.nullable=="NO")>
    @NotEmpty
     </#if>
    @Label( "${entity.comment}" )
    private ${entity.type} ${entity.field};
    
    </#list>
     
    <#list attrs as entity>
    public void set${entity.field?cap_first}(${entity.type} ${entity.field}){
        this.${entity.field} = ${entity.field};
    }
   
    public ${entity.type} get${entity.field?cap_first}(){
        return this.${entity.field};
    }
     
    </#list>
}
