package org.hy.common.milvus.schema;

import java.util.List;

import org.hy.common.Help;
import org.hy.common.xml.log.Logger;

import io.milvus.v2.common.DataType;





/**
 * 字段的Json结构
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-07
 * @version     v1.0
 */
public class Field
{
    
    private static final Logger $Logger = new Logger(Field.class);
    
    
    
    /** 字段名称 */
    private String           name;
    
    /** 描述 */
    private String           description;
    
    /** 字段类型 */
    private DataType         dataType;
    
    /** 是否主键 */
    private Boolean          is_primary_key;
    
    /** 是否分区主键 */
    private Boolean          is_partition_key;
    
    /** 主键自动ID */
    private Boolean          autoID;
    
    /** 是否允许空 */
    private Boolean          nullable;
    
    /** 是否启动分析器 */
    private Boolean          enable_analyzer;
    
    /** 全文检索分析器参数 */
    private Analyzer         analyzer_params;
    
    /** 最大长度（用于VarChar类型的字段） */
    private Integer          max_length;
    
    /** 字段索引列表 */
    private List<FieldIndex> indexes;

    
    
    /**
     * 获取：字段名称
     */
    public String getName()
    {
        return name;
    }

    
    /**
     * 设置：字段名称
     * 
     * @param i_Name 字段名称
     */
    public void setName(String i_Name)
    {
        this.name = i_Name;
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
     * 获取：字段类型
     */
    public Integer getData_type()
    {
        if ( this.dataType != null )
        {
            return this.dataType.getCode();
        }
        else
        {
            return null;
        }
    }

    
    /**
     * 设置：字段类型
     * 
     * @param i_Data_type 字段类型
     */
    public void setData_type(Integer i_Data_type)
    {
        if ( i_Data_type == null )
        {
            this.dataType = null;
        }
        else
        {
            this.dataType = DataType.forNumber(i_Data_type);
            if ( this.dataType == null )
            {
                $Logger.error(i_Data_type + " is not find DataType value");
            }
        }
    }
    
    
    /**
     * 获取：字段类型的名称
     */
    public String getDataTypeName()
    {
        if ( this.dataType != null )
        {
            return this.dataType.name();
        }
        else
        {
            return null;
        }
    }

    
    /**
     * 设置：字段类型的名称
     * 
     * @param i_DataTypeName 字段类型的名称
     */
    public void setDataTypeName(String i_DataTypeName)
    {
        if ( Help.isNull(i_DataTypeName) )
        {
            this.dataType = null;
        }
        else
        {
            this.dataType = DataType.valueOf(i_DataTypeName);
            if ( this.dataType == null )
            {
                $Logger.error(i_DataTypeName + " is not find DataType");
            }
        }
    }
    
    
    /**
     * 获取：字段类型
     */
    public DataType getDataType()
    {
        return dataType;
    }
    
    
    /**
     * 设置：字段类型
     * 
     * @param i_DataType 字段类型
     */
    public void setDataType(DataType i_DataType)
    {
        this.dataType = i_DataType;
    }
    
    

    /**
     * 获取：是否主键
     */
    public Boolean getIs_primary_key()
    {
        return is_primary_key;
    }

    
    /**
     * 设置：是否主键
     * 
     * @param i_Is_primary_key 是否主键
     */
    public void setIs_primary_key(Boolean i_Is_primary_key)
    {
        this.is_primary_key = i_Is_primary_key;
    }

    
    /**
     * 获取：是否分区主键
     */
    public Boolean getIs_partition_key()
    {
        return is_partition_key;
    }

    
    /**
     * 设置：是否分区主键
     * 
     * @param i_Is_partition_key 是否分区主键
     */
    public void setIs_partition_key(Boolean i_Is_partition_key)
    {
        this.is_partition_key = i_Is_partition_key;
    }

    
    /**
     * 获取：主键自动ID
     */
    public Boolean getAutoID()
    {
        return autoID;
    }

    
    /**
     * 设置：主键自动ID
     * 
     * @param i_AutoID 主键自动ID
     */
    public void setAutoID(Boolean i_AutoID)
    {
        this.autoID = i_AutoID;
    }

    
    /**
     * 获取：是否允许空
     */
    public Boolean getNullable()
    {
        return nullable;
    }

    
    /**
     * 设置：是否允许空
     * 
     * @param i_Nullable 是否允许空
     */
    public void setNullable(Boolean i_Nullable)
    {
        this.nullable = i_Nullable;
    }

    
    /**
     * 获取：是否启动全文检索分析器
     */
    public Boolean getEnable_analyzer()
    {
        return enable_analyzer;
    }

    
    /**
     * 设置：是否启动全文检索分析器
     * 
     * @param i_Enable_analyzer 是否启动全文检索分析器
     */
    public void setEnable_analyzer(Boolean i_Enable_analyzer)
    {
        this.enable_analyzer = i_Enable_analyzer;
    }

    
    /**
     * 获取：全文检索分析器参数
     */
    public Analyzer getAnalyzer_params()
    {
        return analyzer_params;
    }

    
    /**
     * 设置：全文检索分析器参数
     * 
     * @param i_Analyzer_params 分析器参数
     */
    public void setAnalyzer_params(Analyzer i_Analyzer_params)
    {
        this.analyzer_params = i_Analyzer_params;
    }

    
    /**
     * 获取：最大长度（用于VarChar类型的字段）
     */
    public Integer getMax_length()
    {
        return max_length;
    }

    
    /**
     * 设置：最大长度（用于VarChar类型的字段）
     * 
     * @param i_Max_length 最大长度（用于VarChar类型的字段）
     */
    public void setMax_length(Integer i_Max_length)
    {
        this.max_length = i_Max_length;
    }

    
    /**
     * 获取：字段索引列表
     */
    public List<FieldIndex> getIndexes()
    {
        return indexes;
    }

    
    /**
     * 设置：字段索引列表
     * 
     * @param i_Indexes 字段索引列表
     */
    public void setIndexes(List<FieldIndex> i_Indexes)
    {
        this.indexes = i_Indexes;
    }
    
}
