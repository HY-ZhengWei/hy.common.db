package org.hy.common.db;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.hy.common.Date;
import org.hy.common.Execute;
import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.XJavaID;





/**
 * 数据库连接池组信息
 * 
 * @author      ZhengWei(HY)
 * @createDate  2012-11-25
 * @version     v1.0  
 *              v2.0   2016-01-22  实现 Comparable 接口
 *              v3.0   2016-02-22  添加：数据库产品名称、版本信息
 *                                 添加：数据库产品类型。目前可识别Oracle、MySQL、SQL Server、DB2、SQLite常用的数据库。
 *                                 这三类信息，只访问数据库一次，一但获取到，就一直保存在对象中。
 *              v4.0   2016-03-02  添加：主备数据库都出现异常时，暂停一小段时间(10秒)后，才允许尝试重新获取数据库连接
 *              v4.1   2016-12-22  修改：allowReconnection()改为public，否则无法在线程中调用成功，也就无法实现10秒后尝试重新获取数据库连接的功能。
 *              v5.0   2017-07-12  添加：isException() 异常状态标记，可通过 http://IP:Port/WebName/analyses/analyseObject?DSG=Y 页面全局查看所有的数据库连接池组信息（支持集群）。
 *                                 添加：统一第三监控接口
 *              v6.0   2018-03-09  添加：实现XJavaID接口，在数据库异常时，能更精确的报出异常的数据库是谁。
 *              v7.0   2019-03-12  添加：获取数据库连接信息。
 */
public final class DataSourceGroup implements Comparable<DataSourceGroup> ,XJavaID ,Serializable
{
    private static final long serialVersionUID = -1707674417006083407L;

    public static final String $DBType_Oracle     = "ORACLE";
    
    public static final String $DBType_MySQL      = "MYSQL";
    
    public static final String $DBType_SQLServer  = "SQLSERVER";
    
    public static final String $DBType_DB2        = "DB2";
    
    public static final String $DBType_SQLite     = "SQLITE";
    
    public static final String $DBType_PostgreSQL = "POSTGRESQL";
    
    
    
	private List<DataSource>   dataSources;
	
	/**
	 * 当前有效的数据源连接池的索引号。
	 * 初始值为 - 1，表示没有任何可用的数据库连接池。
	 * 有效最小下标从 0 开始。 
	 */
	private int                validDSIndex;
	
	/** 唯一标示，主用于对比等操作 */
    private String             uuid;
    
    /** XJava池中对象的ID标识 */
    private String             xjavaID;
    
    /** 数据库产品名称 */
    private String             dbProductName;
    
    /** 数据库产品版本 */
    private String             dbProductVersion;
    
    /** 数据库产品类型 */
    private String             dbProductType;
    
    /** 数据库连接信息 */
    private List<String>       dbURLs;
    
    /** 是否执行了允许重新获取数据库连接的方法（主要用于主备数据库都出现异常时） */
    private boolean            isRunReConn;
    
    /** 是否出现异常。非连接断连异常也会为true */
    private boolean            isException;
    
    /** 最后一次正常连接的时间 */
    private Date               connLastTime;
    
    /** 活动连接数量（不包括连接池中预先初始化的连接数量） */
    private long               connActiveCount;
    
    /** 连接使用峰值（不包括连接池中预先初始化的连接数量） */
    private long               connMaxUseCount;
    
	
	
	public DataSourceGroup()
	{
		this.dataSources     = new ArrayList<DataSource>();
		this.validDSIndex    = -1;
		this.uuid            = StringHelp.getUUID();
		this.isRunReConn     = false;
		this.isException     = false;
		this.connLastTime    = null;
		this.connActiveCount = 0;
		this.connMaxUseCount = 0;
	}
	
	
	
	/**
	 * 向组中添加数据库连接池
	 * 
	 * @param i_DataSource
	 */
	public synchronized void add(DataSource i_DataSource)
	{
		if ( i_DataSource == null )
		{
			throw new NullPointerException("DataSource is null.");
		}
		
		this.dataSources.add(i_DataSource);
		
		if ( this.validDSIndex < 0 )
		{
			this.validDSIndex = 0;
		}
	}
	
	
	
	/**
	 * 获取数据库连接。
	 * 
	 * 1. 依次尝试组中每一个元素，直到获取到可用的数据库连接。
	 *    并且，记忆有效的数据库连接池索引号。
	 *    方便下次的使用。
	 *    
	 * 2. 如果遍历所有元素，均无法获取可用的数据库连接，则返回 null。
	 *    并且，标记 this.validDSIndex 有效索引号为:Integer.MAX_VALUE。
	 *    即，所有数据库连接池都不可用，下次获取连接时，不再一一尝试获取连接。
	 * 
	 * @return
	 */
	public synchronized java.sql.Connection getConnection()
	{
		int v_Size  = this.dataSources.size();
		
		if ( v_Size <= 0 )
		{
			return null;
		}
		
		
		for (; this.validDSIndex<v_Size; this.validDSIndex++)
		{
			try
			{
			    java.sql.Connection v_OutsideConn = this.dataSources.get(this.validDSIndex).getConnection();
			    Connection          v_Conn        = new Connection(v_OutsideConn ,this);
			    
			    this.connActiveCount++;
			    if ( this.connActiveCount > this.connMaxUseCount )
			    {
			        this.connMaxUseCount = this.connActiveCount;
			    }
			    this.isException  = false;
			    this.connLastTime = new Date();
			    
			    return v_Conn;
			}
			catch (Exception exce)
			{
			    exce.printStackTrace();
			    this.isException = true;
			    System.err.println("\n" + Date.getNowTime().getFull() + " 编号：" + this.validDSIndex + " 的数据库连接池" + Help.NVL(this.getXJavaID()) + "失效。尝试获取下一个数据库连接池中的连接。");
			}
		}
		
		if ( !this.isRunReConn )
		{ 
		    System.err.println("\n" + Date.getNowTime().getFull() + " 所有的数据库连接池" + Help.NVL(this.getXJavaID()) + "失效，系统将等待 10秒后尝试重新连接。");
		    
		    this.isRunReConn  = true;  // 防止重复执行
		    this.validDSIndex = Integer.MAX_VALUE;
		    new Execute(this ,"allowReconnection").startDelayed(10 * 1000);
		}
		return null;
	}
	
	
	
	/**
	 * 允许重新尝试获取连接
	 * 
	 * 主备数据库都出现异常时，暂停一小段时间(10秒)后，才允许尝试重新获取数据库连接。
	 * 
	 * 在暂停的一小段时间内，外界调用 getConnection() 方法都返回 null。
	 * 
	 * @author      ZhengWei(HY)
	 * @createDate  2016-03-02
	 * @version     v1.0
	 */
	public synchronized void allowReconnection()
	{
	    System.out.println("\n" + Date.getNowTime().getFull() + " 数据库连接池组" + Help.NVL(this.getXJavaID()) + "编号从0重新遍历，尝试重新连接。");
	    
	    this.validDSIndex = 0;
	    this.isRunReConn  = false;
	}
	
	
	
	/**
	 * 获取数据库产品信息
	 * 
	 * @author      ZhengWei(HY)
	 * @createDate  2016-02-22
	 * @version     v1.0
	 *
	 */
	private synchronized void getDBProductInfo()
	{
	    if ( null != this.dbProductType )
	    {
	        return;
	    }
	    
	    java.sql.Connection v_Conn = null;
        try
        {
            v_Conn = this.getConnection();
            if ( v_Conn == null )
            {
                return;
            }
            DatabaseMetaData v_DBMetaData = v_Conn.getMetaData();
            
            this.dbProductName    = v_DBMetaData.getDatabaseProductName();  
            this.dbProductVersion = v_DBMetaData.getDatabaseProductVersion();
            
            String v_DBName = this.dbProductName.toUpperCase();
            if ( v_DBName.indexOf("ORACLE") >= 0 )
            {
                this.dbProductType = $DBType_Oracle;
            }
            else if ( v_DBName.indexOf("MYSQL") >= 0 )
            {
                this.dbProductType = $DBType_MySQL;
            }
            else if ( v_DBName.indexOf("MICROSOFT") >= 0 )
            {
                this.dbProductType = $DBType_SQLServer;
            }
            else if ( v_DBName.indexOf("DB2") >= 0 )
            {
                this.dbProductType = $DBType_DB2;
            }
            else if ( v_DBName.indexOf("SQLITE") >= 0 )
            {
                this.dbProductType = $DBType_SQLite;
            }
            else if ( v_DBName.indexOf("POSTGRESQL") >= 0 )
            {
                this.dbProductType = $DBType_PostgreSQL;
            }
            else
            {
                this.dbProductType = "";
            }
        }
        catch (Exception exce)
        {
            exce.printStackTrace();
        }
        finally 
        {
            try
            {
                if ( v_Conn != null )
                {
                    v_Conn.close();
                    v_Conn = null;
                }
            }
            catch (Exception exce)
            {
                throw new RuntimeException(exce.getMessage());
            }
            finally
            {
                v_Conn = null;
            }
        }
	}
	
	
	
	/**
	 * 数据库连接池组中，是否有可用的数据库池
	 * 
	 * @return
	 */
	public boolean isValid()
	{
		return this.validDSIndex >= 0 && this.validDSIndex < this.dataSources.size();
	}
	
	
	
	/**
	 * 获取组中池的数量
	 * 
	 * @return
	 */
	public int size()
	{
		return this.dataSources.size();
	}
	
	
	
    /**
     * 获取：是否出现异常。非连接断连异常也会为true
     */
    public boolean isException()
    {
        return isException;
    }

    
    
    /**
     * 设置：是否出现异常。非连接断连异常也会为true
     * 
     * @param isException 
     */
    public void setException(boolean isException)
    {
        this.isException = isException;
    }
    


    /**
     * 获取：最后一次正常连接的时间
     */
    public Date getConnLastTime()
    {
        return connLastTime;
    }



    public String getObjectID()
    {
        return this.uuid;
    }
	
	
    
    /**
     * 获取：数据库产品名称
     */
    public String getDbProductName()
    {
        this.getDBProductInfo();
        return dbProductName;
    }


    
    /**
     * 获取：数据库产品版本
     */
    public String getDbProductVersion()
    {
        this.getDBProductInfo();
        return dbProductVersion;
    }


    
    /**
     * 获取：数据库产品类型
     */
    public String getDbProductType()
    {
        this.getDBProductInfo();
        return dbProductType;
    }
    
    
    
    /**
     * 获取数据库连接信息（只有IP地址、端口、数据库名称等，不涉及其它安全信息）。
     * 
     * 如：jdbc:oracle:thin:@127.0.0.1:1521:数据库实例名称
     *     jdbc:mysql://127.0.0.1:3306/数据库实例名称?useUnicode=true&characterEncoding=utf-8
     *     jtds:sqlserver://127.0.0.1:1433;DatabaseName=数据库实例名称
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-03-12
     * @version     v1.0
     *
     * @return
     */
    public synchronized List<String> getUrls()
    {
        if ( !Help.isNull(this.dbURLs) && this.dbURLs.size() == this.dataSources.size() )
        {
            return this.dbURLs;
        }
        
        this.dbURLs = new ArrayList<String>();
        
        if ( !Help.isNull(this.dataSources) )
        {
            for (DataSource v_DS : this.dataSources)
            {
                boolean   v_IsFind  = false;
                Method [] v_Methods = v_DS.getClass().getMethods();
                
                // 先尝试猜想
                if ( !Help.isNull(v_Methods) )
                {
                    for (Method v_Item : v_Methods)
                    {
                        String v_Name = v_Item.getName().toLowerCase();
                        
                        if ( v_Name.startsWith("get") && (v_Name.indexOf("url")  >= 0 || v_Name.indexOf("jdbc") >= 0) )
                        {
                            if ( v_Item.getParameterTypes().length == 0 )
                            {
                                try
                                {
                                    Object v_Value = v_Item.invoke(v_DS);
                                    if ( v_Value != null && !Help.isNull(v_Value.toString()) )
                                    {
                                        this.dbURLs.add(v_Value.toString());
                                        v_IsFind = true;
                                    }
                                }
                                catch (Exception exce)
                                {
                                    exce.printStackTrace();
                                }
                            }
                        }
                    }
                }
                
                // 没猜出来，再通过数据库连接获取
                if ( !v_IsFind )
                {
                    java.sql.Connection v_Conn = null;
                    try
                    {
                        v_Conn = v_DS.getConnection();
                        if ( v_Conn != null )
                        {
                            DatabaseMetaData v_DBMeta = v_Conn.getMetaData();
                            if ( v_DBMeta != null && !Help.isNull(v_DBMeta.getURL()) )
                            {
                                this.dbURLs.add(v_DBMeta.getURL());
                            }
                        }
                    }
                    catch (Exception exce)
                    {
                        exce.printStackTrace();
                    }
                    finally
                    {
                        try
                        {
                            if ( v_Conn != null )
                            {
                                v_Conn.close();
                            }
                        }
                        catch (Exception exce)
                        {
                            // Nothing.
                        }
                    }
                }
            }
        }
        
        return this.dbURLs;
    }


    
    /**
     * 获取：活动连接数量（不包括连接池中预先初始化的连接数量）
     */
    public long getConnActiveCount()
    {
        return this.connActiveCount;
    }
    
    
    /**
     * 连接关闭时触发
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-07-13
     * @version     v1.0
     *
     */
    protected synchronized void connClosed()
    {
        this.connActiveCount--;
    }

    
    /**
     * 获取：连接使用峰值（不包括连接池中预先初始化的连接数量）
     */
    public long getConnMaxUseCount()
    {
        return connMaxUseCount;
    }
    
    
    
    /**
     * 设置XJava池中对象的ID标识。此方法不用用户调用设置值，是自动的。
     * 
     * @param i_XJavaID
     */
    public void setXJavaID(String i_XJavaID)
    {
        this.xjavaID = i_XJavaID;
    }
    
    
    
    /**
     * 获取XJava池中对象的ID标识。
     * 
     * @return
     */
    public String getXJavaID()
    {
        return this.xjavaID;
    }



    public int hashCode()
    {
        return this.getObjectID().hashCode();
    }
    
    
    
    public boolean equals(Object i_Other)
    {
        if ( null == i_Other )
        {
            return false;
        }
        else if ( this == i_Other )
        {
            return true;
        }
        else if ( i_Other instanceof DataSourceGroup )
        {
            return this.getObjectID().equals(((DataSourceGroup)i_Other).getObjectID());
        }
        else
        {
            return false;
        }
    }
	
	
	
	public int compareTo(DataSourceGroup i_Other)
	{
	    if ( null == i_Other )
        {
            return 1;
        }
        else if ( this == i_Other )
        {
            return 0;
        }
        else
        {
            return this.getObjectID().compareTo(i_Other.getObjectID());
        }
	}



    /**
     * 显示基本信息
     *
     * @author      ZhengWei(HY)
     * @createDate  2017-07-12
     * @version     v1.0
     *
     * @return
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder v_Buffer = new StringBuilder();
        
        v_Buffer.append(this.isException ? "<font color='red'>异常</font>" : "正常").append("  ");
        
        if ( this.connLastTime == null )
        {
            v_Buffer.append("0000-00-00 00:00:00");
        }
        else
        {
            v_Buffer.append(this.connLastTime.getFull());
        }
        
        v_Buffer.append("  连接活动数:").append(this.connActiveCount);
        v_Buffer.append("  使用峰值:").append(this.connMaxUseCount);
        v_Buffer.append("  主备连接池数:").append(this.dataSources.size());
        
        return v_Buffer.toString();
    }
	
	
	
	/*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
	protected void finalize() throws Throwable 
	{
		this.dataSources.clear();
		
		super.finalize();
	}
	*/
	
}
