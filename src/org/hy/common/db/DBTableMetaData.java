package org.hy.common.db;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hy.common.Help;





/**
 * 数据库表的元数据信息
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-11-07
 */
public class DBTableMetaData 
{
    
    /** List.key = 字段索引号 List.value = 字段名称 */
    private List<String>            col_ByIndex;
    
    /** Map.key = 字段名称      Map.value = 字段索引号 */
    private Map<String  ,String>    col_ByName;
    
    /** 字段名称的样式 */
    private DBNameStyle             col_NameStyle;
    
    
    
    public DBTableMetaData(DBNameStyle i_ColNameStyle)
    {
        this.col_ByIndex   = new ArrayList<String>();
        this.col_ByName    = new HashMap<String ,String>();
        this.col_NameStyle = i_ColNameStyle;
    }
    
    
    
    /**
     * 填充（设置）元数据
     * 
     * @param i_ResultSetMetaData
     */
    public synchronized void set(ResultSetMetaData i_ResultSetMetaData)
    {
        this.clear();
        
        try
        {
            int v_ColCount = i_ResultSetMetaData.getColumnCount();
            
            for (int v_ColIndex=1; v_ColIndex<=v_ColCount; v_ColIndex++)
            {
//              System.out.print(  i_ResultSetMetaData.getColumnType     (v_ColIndex) + "   ");
//              System.out.print(  i_ResultSetMetaData.getColumnTypeName (v_ColIndex) + "   ");
//              System.out.println(i_ResultSetMetaData.getColumnClassName(v_ColIndex));
                
                // ZhengWei(HY) Edit 修复字段重新别名后，无法XJava的问题 2014-04-02
                // getColumnName()  与 getColumnLabel() 的区别是：
                // getColumnName()  为数据库字段的真实名称
                // getColumnLabel() 为字段的别名，即 AS 关键字后的部分
                this.addColumnInfo(v_ColIndex - 1 ,i_ResultSetMetaData.getColumnLabel(v_ColIndex));
            }
        }
        catch (Exception exce)
        {
            throw new java.lang.RuntimeException(exce.getMessage());
        }
    }
    
    
    
    /**
     * 添加表的字段信息
     * 
     * @param i_ColIndex  字段索引号。下标从0开始
     * @param i_ColName   字段的名称
     */
    public void addColumnInfo(int i_ColIndex ,String i_ColName)
    {
        String v_ColName = "";
        
        if ( Help.isNull(i_ColName) )
        {
            v_ColName = "COLNAME_" + i_ColIndex;
        }
        else
        {
            // ZhengWei(HY) Add 2015-10-09 可由外部控制字段名称的样式
            if ( this.col_NameStyle == DBNameStyle.$Upper )
            {
                v_ColName = i_ColName.trim().toUpperCase();
            }
            else if ( this.col_NameStyle == DBNameStyle.$Normal )
            {
                v_ColName = i_ColName.trim();
            }
            else
            {
                v_ColName = i_ColName.trim().toLowerCase();
            }
            
            if ( this.col_ByName.containsKey(v_ColName) )
            {
                v_ColName = v_ColName + i_ColIndex;
            }
        }
        
        this.col_ByIndex.add(i_ColIndex ,v_ColName);
        this.col_ByName.put(v_ColName   ,String.valueOf(i_ColIndex));
    }
    
    
    
    /**
     * 获取字段名称（根据字段下标，从零开始）
     * 
     * @param i_ColIndex  字段索引号
     * @return
     */
    public String getColumnName(int i_ColIndex)
    {
        /*
         * 不判断是为性能
        if ( i_ColIndex < 0 || this.col_ByIndex.size() <= i_ColIndex )
        {
            return "";
        }
        */
        
        return this.col_ByIndex.get(i_ColIndex);
    }
    
    
    
    /**
     * 获取字段索引号（根据字段下标，从零开始）
     * 
     * @param i_ColName  字段名称
     * @return
     */
    public int getColumnIndex(String i_ColName)
    {
        /*
         * 不判断是为性能
        if ( Help.isNull(i_ColName) )
        {
            return -1;
        }
        */
        
        
        String v_ColName = i_ColName.trim().toUpperCase();
        
        if ( this.col_ByName.containsKey(v_ColName) )
        {
            return Integer.parseInt(this.col_ByName.get(v_ColName));
        }
        else
        {
            return -1;
        }
    }
    
    
    
    /**
     * 返回字段个数
     * 
     * @return
     */
    public int getColumnSize()
    {
        return this.col_ByIndex.size();
    }
    
    
    
    /**
     * 清除集合数据
     */
    public synchronized void clear()
    {
        this.col_ByIndex.clear();
        this.col_ByName.clear();
    }
    
    
    
    public int hashCode()
    {
        return this.col_ByIndex.hashCode();
    }



    public boolean equals(Object i_Other) 
    {
        if ( i_Other == null )
        {
            return false;
        }
        else if ( i_Other instanceof DBTableMetaData )
        {
            return this.col_ByIndex.equals(((DBTableMetaData)i_Other).col_ByIndex);
        }
        else
        {
            return false;
        }
    }
    
    
    
    /*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
    protected void finalize() throws Throwable 
    {
        this.clear();
        
        this.col_ByIndex = null;
        this.col_ByName  = null;
        
        super.finalize();
    }
    */
    
}
