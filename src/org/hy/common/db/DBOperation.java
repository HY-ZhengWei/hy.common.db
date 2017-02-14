package org.hy.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;





/**
 * 数据库操作封装类
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2011-04-17
 */
public class DBOperation 
{
	private static DBOperation  DBOpr  = null;
	
	/** 数据库信息 */
	public static DatabaseInfo DATABASEINFO;


  
	/**
	 * 获取实例
	 * 
	 * @return
	 * @throws Exception
	 */
	public synchronized static DBOperation getInstance() throws Exception 
	{
		if ( DBOpr == null ) 
		{
			DBOpr = new DBOperation();
		}

		return DBOpr;
	}

	
	/**
	 * 私有的构造器	
	 */
	private DBOperation()
	{
		if ( DATABASEINFO == null )
		{
			throw new java.lang.NullPointerException("Database info is null.");
		}
	}
	
	
	/**
	 * 得到数据库连接对象
	 * 
	 * @return
	 * @throws Exception
	 */
	public synchronized Connection getConnection() throws Exception 
	{
		if ( DATABASEINFO == null )
		{
			throw new java.lang.NullPointerException("Database info is null.");
		}

		Class.forName(DATABASEINFO.getDriver()).newInstance();
		Connection v_Conn = DriverManager.getConnection(DATABASEINFO.getUrl() ,DATABASEINFO.getUser() ,DATABASEINFO.getPassword());
		
		return v_Conn;
	}
	
	
	/**
	 * 关闭外部所有与数据有关的连接
	 * 
	 * @param i_Resultset
	 * @param i_Statement
	 * @param i_Conn
	 * @throws Exception
	 */
	public synchronized void closeDB(ResultSet i_Resultset ,Statement i_Statement ,Connection i_Conn) throws Exception 
	{
    	try
    	{
	    	if ( i_Resultset != null )
		    {
	    		i_Resultset.close();
	    		i_Resultset = null;
		    }
    	}
    	catch (Exception err)
    	{
    		throw err;
    	}
    	finally
    	{
    		i_Resultset = null;
    	}
    	
    	
    	try
    	{
	    	if ( i_Statement != null )
		    {
		    	i_Statement.close();
		    	i_Statement = null;
		    }
    	}
    	catch (Exception err)
    	{
    		err.printStackTrace();
    	}
    	finally
    	{
    		i_Statement = null;
    	}
    	
    	
    	try
    	{
	    	if ( i_Conn != null )
		    {
	    		i_Conn.close();
	    		i_Conn = null;
		    }
    	}
    	catch (Exception err)
    	{
    		err.printStackTrace();
    	}
    	finally
    	{
    		i_Conn = null;
    	}
	}


	/**
	 * 析构子
	 */
	protected void finalize() 
	{
		
	}

	
	/**
	* 执行无返回记录集的所有数据库操作(可进行批次的操作)。
	*
	* @param sql  合法的 SQL 语句
	* @return
	* @throws java.lang.Exception
	*/
	public synchronized int executeUpdate(String i_SQL ,Connection i_Conn) throws Exception 
	{
	    int v_Answer = -1;
	
	    Connection v_Conn      = null;
	    Statement  v_Statement = null;
	    
	    if ( i_Conn == null )
    	{
	    	v_Conn = this.getConnection();
    	}
	    else
	    {
	    	v_Conn = i_Conn;
	    }
	    
	    try
	    {
		    v_Statement = v_Conn.createStatement();
		    v_Answer    = v_Statement.executeUpdate(i_SQL);
	    }
	    catch (Exception err)
	    {
	    	err.printStackTrace();
	    }
	    finally
	    {
	    	// 注意此处的判断，并没有判断 v_Conn 是否为空
	    	if ( i_Conn == null )
	    	{
	    		this.closeDB(null ,v_Statement ,v_Conn);
	    	}
	    	else
	    	{
	    		this.closeDB(null ,v_Statement ,null);
	    	}
	    }
	
	    return v_Answer;
	}
	
	
	/**
	* 执行无返回记录集的所有数据库操作(每次操作都有新的连接)。
	*
	* @param sql  合法的 SQL 语句
	* @return
	* @throws java.lang.Exception
	*/
	public synchronized int executeUpdate(String i_SQL) throws Exception
	{
		return this.executeUpdate(i_SQL, null);
	}

	
	/**
	 * 查询语句的执行
	 * 
	 * @param i_SQL
	 * @return
	 * @throws Exception
	 */
	public synchronized DBTable querySQL(String i_SQL) throws Exception
	{
		Connection v_Conn      = this.getConnection();
		Statement  v_Statement = null;
	    ResultSet  v_Resultset = null;
	    DBTable    v_DBTable   = new DBTable();
	    
	    try
	    {
	    	// 实测: ResultSet.TYPE_FORWARD_ONLY 是性能最快的，耗内存最小的，也是生成JVM中OLD区最小的
	    	v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
	    	v_Resultset = v_Statement.executeQuery(i_SQL);
	    	
	    	// 解释数据集元数据
	    	v_DBTable.setMetaData(v_Resultset.getMetaData());
	    	int v_ColCount = v_DBTable.getColumnSize();
	    	
	    	while ( v_Resultset.next() ) 
	    	{
	    		DBRowInfo v_DBRowInfo = new DBRowInfo(v_DBTable);
	    		
	    		for (int v_ColIndex=1; v_ColIndex<=v_ColCount; v_ColIndex++)
	    		{
	    			String v_Value = v_Resultset.getString(v_ColIndex);
	    			
	    			v_Value = v_Value == null ? "" : v_Value;
	    			
	    			v_DBRowInfo.addColumnValue(v_Value);
	    		}
	    		
	    		v_DBTable.addRowInfo(v_DBRowInfo);
	    	}
	    }
	    catch (Exception err)
	    {
	    	throw err;
	    }
	    finally
	    {
	    	this.closeDB(v_Resultset ,v_Statement ,v_Conn);
	    }
    	
    	
    	return v_DBTable;
	}
	
	
	/**
	 * 查询语句的执行
	 * 
	 * @param i_SQL
	 * @return
	 * @throws Exception
	 */
	public synchronized ResultSet querySQLOfResultSet(String i_SQL) throws Exception
	{
		Connection v_Conn      = this.getConnection();
		Statement  v_Statement = null;
	    ResultSet  v_Resultset = null;
	    
	    try
	    {
	    	// 实测: ResultSet.TYPE_FORWARD_ONLY 是性能最快的，耗内存最小的，也是生成JVM中OLD区最小的
	    	v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
	    	v_Resultset = v_Statement.executeQuery(i_SQL);
	    }
	    catch (Exception err)
	    {
	    	throw err;
	    }
    	
	    return v_Resultset;
	}
	
	
	/**
	 * 查询语句的执行。
	 * 主要用于大结果集的情况。
	 * 可按PL/SQL工具一样，可分段显示查询结果。
	 * 
	 * @param i_SQL      查询语句
	 * @return
	 * @throws Exception
	 */
	public synchronized DBTableBigger queryBiggerSQL(String i_SQL) throws Exception
	{
		return this.queryBiggerSQL(i_SQL ,500);
	}
	
	
	/**
	 * 查询语句的执行。
	 * 主要用于大结果集的情况。
	 * 可按PL/SQL工具一样，可分段显示查询结果。
	 * 
	 * @param i_SQL      查询语句
	 * @param i_PerSize  每次返回记录的数量
	 * @return
	 * @throws Exception
	 */
	public synchronized DBTableBigger queryBiggerSQL(String i_SQL ,long i_PerSize) throws Exception
	{
		int v_RowSize = this.getSQLCount("SELECT COUNT(1) FROM (" + i_SQL + ") HY");
		
		Connection    v_Conn          = this.getConnection();
		Statement     v_Statement     = null;
	    ResultSet     v_Resultset     = null;
	    DBTableBigger v_DBTableBigger = null;
	    
	    try
	    {
	    	// 实测: ResultSet.TYPE_FORWARD_ONLY 是性能最快的，耗内存最小的，也是生成JVM中OLD区最小的
	    	v_Statement = v_Conn.createStatement(ResultSet.TYPE_FORWARD_ONLY ,ResultSet.CONCUR_READ_ONLY);
	    	v_Resultset = v_Statement.executeQuery(i_SQL);
	    	
	    	v_DBTableBigger = new DBTableBigger(v_Resultset ,v_RowSize ,i_PerSize ,this);
	    }
	    catch (Exception err)
	    {
	    	this.closeDB(v_Resultset ,v_Statement ,v_Conn);
	    	throw err;
	    }
	    
	    return v_DBTableBigger;
	}
	
	
	/**
	 * 查询记录总数
	 * 
	 * @param i_SQL
	 * @return
	 * @throws Exception
	 */
	public synchronized int getSQLCount(String i_SQL) throws Exception
	{
		Connection v_Conn      = this.getConnection();
		Statement  v_Statement = null;
	    ResultSet  v_Resultset = null;
	    int        v_SQLCount  = 0;
	    
	    try
	    {
	    	v_Statement = v_Conn.createStatement();
	    	v_Resultset = v_Statement.executeQuery(i_SQL);
	    	
	    	if ( v_Resultset.next() ) 
	    	{
	    		v_SQLCount = v_Resultset.getInt(1);
	    	}
	    }
	    catch (Exception err)
	    {
	    	throw err;
	    }
	    finally
	    {
	    	this.closeDB(v_Resultset ,v_Statement ,v_Conn);
	    }
    	
    	
    	return v_SQLCount;
	}
	
	
	/**
	* 执行SQL
	*
	* @param sql  合法的 SQL 语句
	* @return
	* @throws java.lang.Exception
	*/
	public synchronized boolean execute(String i_SQL) throws Exception 
	{
	    boolean    v_Answer    = false;
	    Connection v_Conn      = null;
	    Statement  v_Statement = null;
	    
	    v_Conn = this.getConnection();
	    
	    try
	    {
		    v_Statement = v_Conn.createStatement();
		    v_Answer    = v_Statement.execute(i_SQL);
	    }
	    catch (Exception err)
	    {
	    	err.printStackTrace();
	    }
	    finally
	    {
	    	this.closeDB(null ,v_Statement ,v_Conn);
	    }
	    
	    return v_Answer;
	}
	
};