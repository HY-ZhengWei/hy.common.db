package org.hy.common.milvus.schema;

import java.util.List;

import org.hy.common.Help;
import org.hy.common.xml.log.Logger;

import io.milvus.v2.common.ConsistencyLevel;





/**
 * 表的Json结构。与Milvus官网页面中的 “代码” 页面中的Json格式保持一致。
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-07
 * @version     v1.0
 */
public class Collection
{
    
    private static final Logger $Logger = new Logger(Collection.class);
    
    
    
    /** 表名称，向量库中Collection的名称 */
    private String               collection_name;
    
    /** 描述 */
    private String               description;
    
    /** 一致性 */
    private ConsistencyLevel     consistencyLevel;
    
    /** 分片数量 */
    private Integer              shards_num;
    
    /** 表属性 */
    private CollectionProperties properties;
    
    /** 字段列表 */
    private List<Field>          fields;
    
    /** 函数列表 */
    private List<Function>       functions;
    
    
    
    public Collection()
    {
        this.consistencyLevel = ConsistencyLevel.BOUNDED;
        this.setShards_num(1);
        this.properties = new CollectionProperties("Asia/Shanghai");
    }
    
    
    
    /**
     * 获取：表名称，向量库中Collection的名称
     */
    public String getCollection_name()
    {
        return collection_name;
    }

    
    /**
     * 设置：表名称，向量库中Collection的名称
     * 
     * @param i_Collection_name 表名称，向量库中Collection的名称
     */
    public void setCollection_name(String i_Collection_name)
    {
        this.collection_name = i_Collection_name;
    }

    
    /**
     * 获取：描述
     */
    public String getDescription()
    {
        return description;
    }

    
    /**
     * 设置：描述
     * 
     * @param i_Description 描述
     */
    public void setDescription(String i_Description)
    {
        this.description = i_Description;
    }

    
    /**
     * 获取：一致性
     */
    public String getConsistency_level()
    {
        if ( this.consistencyLevel != null )
        {
            return this.consistencyLevel.getName();
        }
        else
        {
            return null;
        }
    }

    
    /**
     * 设置：一致性
     * 
     * @param i_Consistency_level 一致性
     */
    public void setConsistency_level(String i_Consistency_level)
    {
        if ( Help.isNull(i_Consistency_level) )
        {
            this.consistencyLevel = null;
        }
        else
        {
            this.consistencyLevel = ConsistencyLevel.fromName(i_Consistency_level);
            if ( this.consistencyLevel == null )
            {
                $Logger.error(i_Consistency_level + " is not find ConsistencyLevelEnum");
            }
        }
    }
    
    
    /**
     * 获取：一致性
     */
    public ConsistencyLevel getConsistencyLevel()
    {
        return consistencyLevel;
    }

    
    /**
     * 设置：一致性
     * 
     * @param i_ConsistencyLevel 一致性
     */
    public void setConsistencyLevel(ConsistencyLevel i_ConsistencyLevel)
    {
        this.consistencyLevel = i_ConsistencyLevel;
    }


    /**
     * 获取：分片数量
     */
    public Integer getShards_num()
    {
        return shards_num;
    }

    
    /**
     * 设置：分片数量
     * 
     * @param i_Shards_num 分片数量
     */
    public void setShards_num(Integer i_Shards_num)
    {
        this.shards_num = i_Shards_num;
    }

    
    /**
     * 获取：表属性
     */
    public CollectionProperties getProperties()
    {
        return properties;
    }

    
    /**
     * 设置：表属性
     * 
     * @param i_Properties 表属性
     */
    public void setProperties(CollectionProperties i_Properties)
    {
        this.properties = i_Properties;
    }

    
    /**
     * 获取：字段列表
     */
    public List<Field> getFields()
    {
        return fields;
    }

    
    /**
     * 设置：字段列表
     * 
     * @param i_Fields 字段列表
     */
    public void setFields(List<Field> i_Fields)
    {
        this.fields = i_Fields;
    }

    
    /**
     * 获取：函数列表
     */
    public List<Function> getFunctions()
    {
        return functions;
    }

    
    /**
     * 设置：函数列表
     * 
     * @param i_Functions 函数列表
     */
    public void setFunctions(List<Function> i_Functions)
    {
        this.functions = i_Functions;
    }
    
}
