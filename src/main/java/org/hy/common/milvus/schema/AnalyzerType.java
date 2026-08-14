package org.hy.common.milvus.schema;





/**
 * 全文检索分析器的类型
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-07
 * @version     v1.0
 */
public enum AnalyzerType
{
    
    Standard("standard" ,0),
    
    English ("english"  ,1),
    
    Chinese ("chinese"  ,2),
    
    Custom  ("custom"   ,3),
    ;

    
    
    private final String name;
    
    private final int    code;



    AnalyzerType(String i_Name ,int i_Code)
    {
        this.name = i_Name;
        this.code = i_Code;
    }



    public String getName()
    {
        return name;
    }



    public int getCode()
    {
        return code;
    }
    
    
    
    private static final AnalyzerType [] $AnalyzerTypes = values();
    
    
    
    public static AnalyzerType get(int i_Code)
    {
        if ( i_Code >= 0 && i_Code < $AnalyzerTypes.length )
        {
            return $AnalyzerTypes[i_Code];
        }
        return null;
    }
    
    
    
    public static AnalyzerType fromName(String i_Name) 
    {
        for (AnalyzerType v_AnalyzerType : AnalyzerType.values())
        {
            if ( v_AnalyzerType.getName().equalsIgnoreCase(i_Name) )
            {
                return v_AnalyzerType;
            }
        }
        return null;
    }
    
}
