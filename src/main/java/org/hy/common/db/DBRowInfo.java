package org.hy.common.db;

import org.hy.common.file.FileSerializable;





/**
 * 数据库行信息
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2009-08-12
 */
public class DBRowInfo implements FileSerializable
{
	
	private static final long serialVersionUID = 4103490602267546804L;

	private DBTable             dbTable;
	
	/** List.key = 字段索引号 List.value = 字段值 */
	private String []           rowData;
	
	private int                 nextIndex;
	
	/** 行号 */
	private long                rowIndex;
	
	
	
	/**
	 * 构造器
	 */
	public DBRowInfo(DBTable i_DBTable)
	{
		this.dbTable   = i_DBTable;
		this.rowData   = new String[this.dbTable.getColumnSize()];
		this.nextIndex = 0;
	}
	
	
	
	/**
	 * 顺序添加字段信息
	 * 
	 * @param i_Value    字段值
	 */
	public void addColumnValue(String i_Value)
	{
		this.rowData[this.nextIndex++] = i_Value;
	}
	
	
	
	/**
	 * 添加字段信息
	 * 
	 * @param i_ColIndex  字段索引号。下标从0开始。
	 * @param i_Value     字段值
	 */
	public void addColumnValue(int i_ColIndex ,String i_Value)
	{
		this.rowData[i_ColIndex] = i_Value;
		this.nextIndex = i_ColIndex + 1;
	}
	
	
	
	/**
	 * 添加字段信息
	 * 
	 * @param i_ColumnName  字段名称
	 * @param i_Value       字段值
	 */
	public void addColumnValue(String i_ColumnName ,String i_Value)
	{
		int v_ColIndex = this.dbTable.getColumnIndex(i_ColumnName);
		
		if ( v_ColIndex >= 0 )
		{
			this.addColumnValue(v_ColIndex ,i_Value);
		}
	}
	
	
	
	/**
	 * 获取字段个数
	 * 
	 * @return
	 */
	public int getColumnSize()
	{
		return this.rowData.length;
	}
	
	
	
	/**
	 * 获取字段值（根据字段名称）
	 * 
	 * @param i_ColumnName  字段名称
	 * @return
	 */
	public String getColumnValue(String i_ColumnName)
	{
		/*
		 * 不判断是为性能
		if ( Help.isNull(i_ColumnName) )
		{
			return "";
		}
		*/
		
		int v_ColIndex = this.dbTable.getColumnIndex(i_ColumnName);
		
		if ( v_ColIndex >= 0 )
		{
			return this.rowData[v_ColIndex];
		}
		else
		{
			return "";
		}
	}
	
	
	
	/**
	 * 获取字段值（根据字段下标，从零开始）
	 * 
	 * @param i_ColIndex
	 * @return
	 */
	public String getColumnValue(int i_ColIndex)
	{
		/*
		 * 不判断是为性能
		if ( i_ColIndex < 0 || this.rowData.size() <= i_ColIndex )
		{
			return "";
		}
		*/
		return this.rowData[i_ColIndex];
	}
	
	
	
	/**
	 * 获取字段名称（根据字段下标，从零开始）
	 * 
	 * @param i_ColIndex
	 * @return
	 */
	public String getColumnName(int i_ColIndex)
	{
		return this.dbTable.getColumnName(i_ColIndex);
	}
	
	
	
	/**
	 * 清除集合数据
	 */
	public void clear()
	{
		for (int i=this.rowData.length - 1; i>=0; i--)
		{
			@SuppressWarnings("unused")
			String v_Value = this.rowData[i];
			v_Value = null;
		}
		
		this.nextIndex = 0;
	}

	

	public long getRowIndex() 
	{
		return rowIndex;
	}


	
	public void setRowIndex(long rowIndex)
	{
		this.rowIndex = rowIndex;
	}
	
	
	
	/**
	 * 获取属性的数量
	 * 
	 * @return
	 */
	public int gatPropertySize()
	{
		return this.rowData.length;
	}
	
	
	
	/**
	 * 获取指定顺序上的属性名称
	 * 
	 * @param i_PropertyIndex  下标从0开始
	 * @return
	 */
	public String gatPropertyName(int i_PropertyIndex)
	{
		return this.getColumnName(i_PropertyIndex);
	}
	
	
	
	/**
	 * 获取指定顺序上的属性值
	 * 
	 * @param i_PropertyIndex  下标从0开始
	 * @return
	 */
	public Object gatPropertyValue(int i_PropertyIndex)
	{
		return this.getColumnValue(i_PropertyIndex);
	}
	
	
	
	/**
	 * 释放资源
	 */
	public void freeResource()
	{
		this.clear();
	}
	
	
	/*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
	protected void finalize() throws Throwable 
	{
		this.clear();
		
		this.rowData = null;
		this.dbTable = null;
		
		super.finalize();
	}
	*/
	
}
