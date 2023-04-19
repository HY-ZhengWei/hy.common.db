package org.hy.common.db;

import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;





/**
 * 数据库表信息
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0
 * @createDate  2009-08-12
 */
public class DBTable
{
    /** Map.key = 行索引号    Map.value = 行数据对象 */
    private Map<String  ,DBRowInfo> tableMap;
    
    /** 表信息的元数据 */
    private DBTableMetaData         metaData;
    
    
    
    /**
     * 构造器
     */
    public DBTable()
    {
        this.tableMap = new HashMap<String ,DBRowInfo>();
        this.metaData = new DBTableMetaData(DBNameStyle.$Upper);
    }
    
    
    
    /**
     * 添加行信息
     * 
     * @param i_DBRowInfo  行数据
     */
    public void addRowInfo(DBRowInfo i_DBRowInfo)
    {
        this.tableMap.put(String.valueOf(this.tableMap.size()), i_DBRowInfo);
    }
    
    
    
    /**
     * 获取总行数
     * 
     * @return
     */
    public int getRowSize()
    {
        return this.tableMap.size();
    }
    
    
    
    /**
     * 获取行数据（根据行下标，从零开始）
     * 
     * @param i_RowIndex
     * @return
     */
    public DBRowInfo getRowValue(int i_RowIndex)
    {
        if ( i_RowIndex < 0 || this.tableMap.size() <= i_RowIndex )
        {
            return null;
        }
        
        return this.tableMap.get(String.valueOf(i_RowIndex));
    }
    
    
    
    /**
     * 获取字段值
     * 
     * @param i_RowIndex
     * @param i_ColIndex
     * @return
     */
    public String getColumnValue(int i_RowIndex ,int i_ColIndex)
    {
        DBRowInfo v_DBRowInfo = this.getRowValue(i_RowIndex);
        
        return v_DBRowInfo.getColumnValue(i_ColIndex);
    }
    
    
    
    /**
     * 获取字段值
     * 
     * @param i_RowIndex
     * @param i_ColumnName
     * @return
     */
    public String getColumnValue(int i_RowIndex ,String i_ColumnName)
    {
        return this.getColumnValue(i_RowIndex ,this.metaData.getColumnIndex(i_ColumnName));
    }
    
    
    
    /**
     * 添加表的字段信息
     * 
     * @param i_ColIndex  字段索引号。下标从0开始
     * @param i_ColName   字段的名称
     */
    public void addColumnInfo(int i_ColIndex ,String i_ColName)
    {
        this.metaData.addColumnInfo(i_ColIndex ,i_ColName);
    }
    
    
    
    /**
     * 获取字段名称（根据字段下标，从零开始）
     * 
     * @param i_ColIndex  字段索引号
     * @return
     */
    public String getColumnName(int i_ColIndex)
    {
        return this.metaData.getColumnName(i_ColIndex);
    }
    
    
    
    /**
     * 获取字段索引号（根据字段下标，从零开始）
     * 
     * @param i_ColName  字段名称
     * @return
     */
    public int getColumnIndex(String i_ColName)
    {
        return this.metaData.getColumnIndex(i_ColName);
    }
    
    
    
    /**
     * 返回字段个数
     * 
     * @return
     */
    public int getColumnSize()
    {
        return this.metaData.getColumnSize();
    }
    
    
    
    public DBTableMetaData getMetaData()
    {
        return metaData;
    }
    
    
    
    /**
     * 填充（设置）元数据
     * 
     * @param i_ResultSetMetaData
     */
    public void setMetaData(ResultSetMetaData i_ResultSetMetaData)
    {
        this.metaData.set(i_ResultSetMetaData);
    }
    
    
    
    /**
     * 清除集合数据
     */
    public void clear()
    {
        int v_RowSize = this.tableMap.size();
        
        for (int v_RowIndex=0; v_RowIndex<v_RowSize; v_RowIndex++)
        {
            tableMap.get(String.valueOf(v_RowIndex)).clear();
        }
        
        this.tableMap.clear();
        this.metaData.clear();
    }
    
    
    
    /*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
    protected void finalize() throws Throwable
    {
        this.clear();
        
        this.tableMap = null;
        this.metaData = null;
        
        super.finalize();
    }
    */
    
}
