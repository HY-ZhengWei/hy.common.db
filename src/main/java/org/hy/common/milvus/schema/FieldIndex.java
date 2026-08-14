package org.hy.common.milvus.schema;

import java.util.List;

import org.hy.common.Help;
import org.hy.common.xml.log.Logger;

import io.milvus.v2.common.IndexParam.IndexType;
import io.milvus.v2.common.IndexParam.MetricType;





/**
 * 字段索引的Json结构
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-07
 * @version     v1.0
 */
public class FieldIndex
{
    
    private static final Logger $Logger = new Logger(FieldIndex.class);
    
    
    
    /** 索引对应字段的名称 */
    private String                     index_name;
    
    /** 索引类型 */
    private IndexType                  index_type;
    
    /** 度量类型（适用于SparseFloatVector） */
    private MetricType                 metric_type;
    
    /** 索引额外参数 */
    private List<FieldIndexExtraParam> indexParameterPairs;

    
    
    /**
     * 获取：索引对应字段的名称
     */
    public String getIndex_name()
    {
        return index_name;
    }

    
    /**
     * 设置：索引对应字段的名称
     * 
     * @param i_Index_name 索引对应字段的名称
     */
    public void setIndex_name(String i_Index_name)
    {
        this.index_name = i_Index_name;
    }
    
    
    /**
     * 获取：索引类型
     */
    public IndexType getIndexType()
    {
        return this.index_type;
    }
    
    
    /**
     * 设置：索引类型
     * 
     * @param i_IndexType 索引类型
     */
    public void setIndexType(IndexType i_IndexType)
    {
        this.index_type = i_IndexType;
    }

    
    /**
     * 获取：索引类型
     */
    public String getIndex_type()
    {
        if ( this.index_type != null )
        {
            return this.index_type.name();
        }
        else
        {
            return null;
        }
    }

    
    /**
     * 设置：索引类型
     * 
     * @param i_Index_type 索引类型
     */
    public void setIndex_type(String i_Index_type)
    {
        if ( Help.isNull(i_Index_type) )
        {
            this.index_type = null;
        }
        else
        {
            this.index_type = IndexType.valueOf(i_Index_type);
            if ( this.index_type == null )
            {
                $Logger.error(i_Index_type + " is not find IndexType");
            }
        }
    }
    
    
    /**
     * 获取：度量类型（适用于SparseFloatVector）
     */
    public MetricType getMetricType()
    {
        return this.metric_type;
    }
    
    
    /**
     * 设置：度量类型（适用于SparseFloatVector）
     * 
     * @param i_MetricType 度量类型（适用于SparseFloatVector）
     */
    public void setMetricType(MetricType i_MetricType)
    {
        this.metric_type = i_MetricType;
    }

    
    /**
     * 获取：度量类型（适用于SparseFloatVector）
     */
    public String getMetric_type()
    {
        if ( this.metric_type != null )
        {
            return this.metric_type.name();
        }
        else
        {
            return null;
        }
    }

    
    /**
     * 设置：度量类型（适用于SparseFloatVector）
     * 
     * @param i_Metric_type 度量类型（适用于SparseFloatVector）
     */
    public void setMetric_type(String i_Metric_type)
    {
        if ( Help.isNull(i_Metric_type) )
        {
            this.metric_type = null;
        }
        else
        {
            this.metric_type = MetricType.valueOf(i_Metric_type);
            if ( this.metric_type == null )
            {
                $Logger.error(i_Metric_type + " is not find MetricType");
            }
        }
    }

    
    /**
     * 获取：索引额外参数
     */
    public List<FieldIndexExtraParam> getIndexParameterPairs()
    {
        return indexParameterPairs;
    }


    /**
     * 设置：索引额外参数
     * 
     * @param i_IndexParameterPairs 索引额外参数
     */
    public void setIndexParameterPairs(List<FieldIndexExtraParam> i_IndexParameterPairs)
    {
        this.indexParameterPairs = i_IndexParameterPairs;
    }
    
}
