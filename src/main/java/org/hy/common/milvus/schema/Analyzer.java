package org.hy.common.milvus.schema;

import org.hy.common.Help;
import org.hy.common.xml.log.Logger;





/**
 * 全文检索分析器的Json结构
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-07
 * @version     v1.0
 */
public class Analyzer
{
    
    private static final Logger $Logger = new Logger(Analyzer.class);
    
    
    
    /** 分析器类型 */
    private AnalyzerType type;

    
    
    /**
     * 获取：分析器类型
     */
    public String getType()
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
     * 设置：分析器类型
     * 
     * @param i_Type 分析器类型
     */
    public void setType(String i_Type)
    {
        if ( Help.isNull(i_Type) )
        {
            this.type = null;
        }
        else
        {
            this.type = AnalyzerType.fromName(i_Type);
            if ( this.type == null )
            {
                $Logger.error(i_Type + " is not find AnalyzerType");
            }
        }
    }
    
}
