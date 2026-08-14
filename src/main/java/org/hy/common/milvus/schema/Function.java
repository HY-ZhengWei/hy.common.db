package org.hy.common.milvus.schema;

import java.util.List;

import org.hy.common.Help;
import org.hy.common.xml.log.Logger;

import io.milvus.common.clientenum.FunctionType;





/**
 * 函数的Json结构
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-07
 * @version     v1.0
 */
public class Function
{
    
    private static final Logger $Logger = new Logger(Function.class);
    
    
    
    /** 函数名称 */
    private String       name;
    
    /** 描述 */
    private String       description;
    
    /** 函数类型 */
    private FunctionType type;
    
    /** 输入字段 */
    private List<String> input_field_names;
    
    /** 输出字段 */
    private List<String> output_field_names;

    
    
    /**
     * 获取：函数名称
     */
    public String getName()
    {
        return name;
    }

    
    /**
     * 设置：函数名称
     * 
     * @param i_Name 函数名称
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
     * 获取：函数类型
     */
    public FunctionType getFunctionType()
    {
        return this.type;
    }
    
    
    /**
     * 设置：函数类型
     * 
     * @param i_FunctionType 函数类型
     */
    public void setType(FunctionType i_FunctionType)
    {
        this.type = i_FunctionType;
    }

    
    /**
     * 获取：函数类型
     */
    public Integer getType()
    {
        if ( this.type != null )
        {
            return this.type.getCode();
        }
        else
        {
            return null;
        }
    }

    
    /**
     * 设置：函数类型
     * 
     * @param i_Type 函数类型
     */
    public void setType(Integer i_Type)
    {
        if ( Help.isNull(i_Type) )
        {
            this.type = null;
        }
        else
        {
            this.type = FunctionType.fromCode(i_Type);
            if ( this.type == null )
            {
                $Logger.error(i_Type + " is not find FunctionType");
            }
        }
    }
    
    
    /**
     * 获取：函数类型
     */
    public String getTypeName()
    {
        if ( this.type != null )
        {
            return this.type.getName();
        }
        else
        {
            return null;
        }
    }

    
    /**
     * 设置：函数类型名称
     * 
     * @param i_Type 函数类型名称
     */
    public void setTypeName(String i_TypeName)
    {
        if ( Help.isNull(i_TypeName) )
        {
            this.type = null;
        }
        else
        {
            this.type = FunctionType.fromName(i_TypeName);
            if ( this.type == null )
            {
                $Logger.error(i_TypeName + " is not find FunctionType");
            }
            else if ( this.type.equals(FunctionType.UNKNOWN) )
            {
                $Logger.error(i_TypeName + " is UNKNOWN FunctionType");
            }
        }
    }

    
    /**
     * 获取：输入字段
     */
    public List<String> getInput_field_names()
    {
        return input_field_names;
    }

    
    /**
     * 设置：输入字段
     * 
     * @param i_Input_field_names 输入字段
     */
    public void setInput_field_names(List<String> i_Input_field_names)
    {
        this.input_field_names = i_Input_field_names;
    }

    
    /**
     * 获取：输出字段
     */
    public List<String> getOutput_field_names()
    {
        return output_field_names;
    }

    
    /**
     * 设置：输出字段
     * 
     * @param i_Output_field_names 输出字段
     */
    public void setOutput_field_names(List<String> i_Output_field_names)
    {
        this.output_field_names = i_Output_field_names;
    }
    
}
