package org.hy.common.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hy.common.file.FileBiggerMemory;
import org.hy.common.file.FileSerializable;





/**
 * 数据库超级大结果集信息
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-04-10
 */
public class DBTableBigger implements FileBiggerMemory
{
	private DBOperation            dbOpr;
	
	private ResultSet              resultSet;
	
	private DBTable                dbTable;
	
	/** 每次返回记录的数量 */
	private long                   perSize;
	
	/** 总记录数量 */
	private long                   rowSize;
	
	/** 获取字段数量 */
	private int                    columnSize;
	
	/** 超级大结果集的存储器接口 */
	private DBTableBiggerMemory    memory;
	
	
	
	public DBTableBigger(ResultSet i_ResultSet ,long i_RowSize ,DBOperation i_SourceObj) throws SQLException
	{
		this(i_ResultSet ,i_RowSize ,500 ,i_SourceObj);
	}
	
	
	
	public DBTableBigger(ResultSet i_ResultSet ,long i_RowSize ,long i_PerSize ,DBOperation i_SourceObj) throws SQLException
	{
		this.resultSet = i_ResultSet;
		this.setPerSize(i_PerSize);
		
		this.columnSize = this.resultSet.getMetaData().getColumnCount();
		this.rowSize    = i_RowSize;
		this.dbOpr      = i_SourceObj;
		this.dbTable    = new DBTable();
		
		int v_ColCount = i_ResultSet.getMetaData().getColumnCount();
    	for (int v_ColIndex=1; v_ColIndex<=v_ColCount; v_ColIndex++)
    	{
    		this.dbTable.addColumnInfo(v_ColIndex - 1,i_ResultSet.getMetaData().getColumnName(v_ColIndex));
    	}
	}
	
	
	
	/**
	 * 顺次返回一定的记录数据
	 * 
	 * @param i_PerSize  每次返回记录的数量
	 * @return
	 * @throws SQLException
	 */
	public Object getData(long i_PerSize) throws SQLException
	{
		this.setPerSize(i_PerSize);
		
		return this.getData();
	}
	
	
	
	/**
	 * 顺次返回一定的记录数据
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Object getData() throws SQLException
	{
		long      v_RowIndex  = this.perSize;
		DBRowInfo v_DBRowInfo = new DBRowInfo(this.dbTable);
		
		
    	while ( v_RowIndex-- > 0 && this.resultSet.next() ) 
    	{
    		for (int v_ColIndex=1; v_ColIndex<=this.columnSize; v_ColIndex++)
    		{
    			String v_Value = this.resultSet.getString(v_ColIndex);
    			
    			v_Value = v_Value == null ? "" : v_Value;
    			
    			v_DBRowInfo.addColumnValue(v_Value);
    			v_DBRowInfo.setRowIndex(this.resultSet.getRow());
    		}
    		
    		this.memory.setRowInfo(v_DBRowInfo);
    		
    		v_DBRowInfo.clear();
    	}
    	
    	v_DBRowInfo.clear();
		v_DBRowInfo = null;
    	
    	return this.memory.getTableInfo();
	}
	
	
	
	/**
	 * 获取一行的数据信息
	 * 
	 * @param i_RowIndex  行号。下标从0开始
	 * @return
	 * @throws SQLException
	 */
	public FileSerializable getRowInfo(long i_RowIndex) throws Exception
	{
		this.resultSet.next();	
		
		DBRowInfo v_DBRowInfo = new DBRowInfo(this.dbTable);
		v_DBRowInfo.setRowIndex(i_RowIndex);
		
		for (int v_ColIndex=1; v_ColIndex<=this.columnSize; v_ColIndex++)
		{
			String v_Value = this.resultSet.getString(v_ColIndex);
			
			if ( v_Value == null )
			{
				v_Value = "";
			}
			
			v_DBRowInfo.addColumnValue(v_Value);
		}
		
		return v_DBRowInfo;
	}
	
	
	
	/**
	 * 获取总的查询记录数
	 * 
	 * @return
	 */
	public long getRowSize() 
	{
		return rowSize;
	}
	
	
	
	/**
	 * 超级大结果集的存储器
	 * 
	 * @return
	 */
	public DBTableBiggerMemory getMemory() 
	{
		return memory;
	}

	

	/**
	 * 设置超级大结果集的存储器
	 * 
	 * @param memory
	 */
	public void setMemory(DBTableBiggerMemory memory) 
	{
		this.memory = memory;
	}
	

	
	/**
	 * 获取字段数量
	 * 
	 * @return
	 */
	public int getColumnSize() 
	{
		return columnSize;
	}
	
	
	
	/**
	 * 获取当前行号。下标从0开始
	 * 
	 * @return
	 */
	public long getRowIndex()
	{
		try 
		{
			return this.resultSet.getRow();
		} 
		catch (SQLException e) 
		{
			return this.rowSize;
		}
	}
	
	
	
	/**
	 * 每次返回记录的数量
	 * 
	 * @return
	 */
	public long getPerSize() 
	{
		return perSize;
	}

	

	public void setPerSize(long perSize) 
	{
		this.perSize = perSize;
	}
	
	
	
	/**
	 * 关闭数据库连接
	 * 
	 * @throws SQLException
	 * @throws Exception
	 */
	public void close() throws SQLException, Exception
	{
		this.dbOpr.closeDB(this.resultSet ,this.resultSet.getStatement() ,this.resultSet.getStatement().getConnection());
	}



	/*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
	protected void finalize() throws Throwable 
	{
		super.finalize();
		
		this.close();
	}
	*/
	
}
