package org.hy.common.db;





/**
 * 字段名称的样式
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2015-10-09
 */
public enum DBNameStyle
{
    /** 字段名称全部大写 */
    $Upper( "UPPER")
    
    /** 
     * 字段名称按数据库默认样式显示
     * 
     * 即，SQL语句中是什么样式的就显示什么样式。
     * 如，SELECT id ,Name ,ABC FROM DEMO; 此SQL语句中生成的字段名称样式为：id ,Name ,ABC
     */
   ,$Normal("NORMAL")
   
   /** 字段名称全部小写 */
   ,$Lower( "LOWER");
    
    
    
    /** 定义枚举成员属性 */
    private final String style;
    
    
    
    /**
     * 通过字符串找枚举对象
     * 
     * @author      ZhengWei(HY)
     * @createDate  2015-10-09
     * @version     v1.0
     *
     * @param i_Style
     * @return
     */
    public static DBNameStyle get(String i_Style)
    {
        if ( i_Style == null )
        {
            return null;
        }
        
        for (DBNameStyle v_Enum : DBNameStyle.values()) 
        {
            if ( v_Enum.style.equals(i_Style.toUpperCase()) ) 
            {
                return v_Enum;
            }
        }
        
        return null;
    }
    
    
    
    private DBNameStyle(String i_Style)
    {
        this.style = i_Style;
    }


    
    public String getStyle()
    {
        return this.style;
    }
    
}
