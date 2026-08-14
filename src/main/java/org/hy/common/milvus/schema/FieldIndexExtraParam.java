package org.hy.common.milvus.schema;





/**
 * 字段索引额外参数的Json结构
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-13
 * @version     v1.0
 */
public class FieldIndexExtraParam
{
    
    /** 参数名称 */
    private String key;
    
    /** 参数数值 */
    private Object value;

    
    
    /**
     * 获取：参数名称
     */
    public String getKey()
    {
        return key;
    }

    
    /**
     * 设置：参数名称
     * 
     * @param i_Key 参数名称
     */
    public void setKey(String i_Key)
    {
        this.key = i_Key;
    }

    
    /**
     * 获取：参数数值
     */
    public Object getValue()
    {
        return value;
    }

    
    /**
     * 设置：参数数值
     * 
     * @param i_Value 参数数值
     */
    public void setValue(Object i_Value)
    {
        this.value = i_Value;
    }
    
}
