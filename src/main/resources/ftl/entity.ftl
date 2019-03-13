package ${packageName};

import java.util.Date;
import Column;
import com.xinghuo.sysdata.po.ID;
import com.xinghuo.sysdata.po.Table;
import com.xinghuo.sysdata.po.CLOB;


@Table("${classInfo.table}")
public class ${classInfo.name}{

    <#list fieldList as field>
    <#if field.isPK=="true">@ID</#if><#if field.isClob=="true">@CLOB</#if>
    @Column(name = "${field.column}")
    private ${field.type} ${field.name};  <#if field.comments !="">//${field.comments}</#if>
    </#list>

    <#list fieldList as field>

    <#if field.comments !="">
    /**
    * 获取${field.comments}
    **/
    </#if>
    public ${field.type} get${field.getSetName}(){
        return this.${field.name};
    }

    <#if field.comments !="">
    /**
    * 设值${field.comments}
    **/
    </#if>
    public void set${field.getSetName}(${field.type} ${field.name}){
        this.${field.name} = ${field.name};
    }
    </#list>

}