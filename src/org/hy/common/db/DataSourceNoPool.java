package org.hy.common.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;





/**
 * 无数据库连接池概念的数据源。
 * 
 * 主要用于手机App这样的微型应用。一个单例的数据库连接就可以满足了，不需要庞大的数据库连接池。
 * 
 * 主要目地是：在手机App这样的微型应用上，可最低配置要求的情况下使用XJava、XSQL框架。
 *           当然，在不久的未来，我相信手机App也是可以不用太多顾虑的使用数据库连接池(XJava、XSQL框架原本就支持)。
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-03-02
 * @version     v1.0
 */
public class DataSourceNoPool implements DataSource
{
    
    /** 是否加载过数据库连接驱动 */
    private boolean isLoadingDriver;
    
    /** 数据库连接驱动 */
    private String driverClass;
    
    /** 数据库连接具体数据库时的 url */
    private String jdbcUrl;
    
    /** 连接数据库用户名 */
    private String user;
    
    /** 连接数据库密码 */
    private String password;
    
    
    
    public DataSourceNoPool()
    {
        this.isLoadingDriver = false;
    }
    
    
    
    /**
     * 加载数据库连接驱动（只加载一次）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-03-02
     * @version     v1.0
     */
    private synchronized void loadingDriver() throws SQLException
    {
        if ( !this.isLoadingDriver )
        {
            try
            {
                Class.forName(this.driverClass).newInstance();
                this.isLoadingDriver = true;
            }
            catch (Exception exce)
            {
                throw new SQLException(exce.getMessage());
            }
        }
    }
    
    
    
    /**
     * 注意：由使用方负责关闭数据库连接
     */
    @Override
    public Connection getConnection() throws SQLException
    {
        this.loadingDriver();
        return DriverManager.getConnection(this.jdbcUrl ,this.user ,this.password);
    }

    
    
    /**
     * 注意：由使用方负责关闭数据库连接
     */
    @Override
    public Connection getConnection(String i_Username ,String i_Password) throws SQLException
    {
        this.user     = i_Username;
        this.password = i_Password;
        
        return this.getConnection();
    }
    
    
    
    @Override
    public PrintWriter getLogWriter() throws SQLException
    {
        // Nothing.
        return null;
    }

    
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException
    {
        // Nothing.
    }

    
    @Override
    public void setLoginTimeout(int seconds) throws SQLException
    {
        // Nothing.
    }

    
    @Override
    public int getLoginTimeout() throws SQLException
    {
        // Nothing.
        return 0;
    }

    
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        // Nothing.
        return null;
    }

    
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        // Nothing.
        return null;
    }

    
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        // Nothing.
        return false;
    }


    
    /**
     * 获取：数据库连接驱动
     */
    public String getDriverClass()
    {
        return driverClass;
    }


    
    /**
     * 设置：数据库连接驱动
     * 
     * @param driverClass 
     */
    public void setDriverClass(String driverClass)
    {
        this.driverClass = driverClass;
    }


    
    /**
     * 获取：数据库连接具体数据库时的 url
     */
    public String getJdbcUrl()
    {
        return jdbcUrl;
    }


    
    /**
     * 设置：数据库连接具体数据库时的 url
     * 
     * @param jdbcUrl 
     */
    public void setJdbcUrl(String jdbcUrl)
    {
        this.jdbcUrl = jdbcUrl;
    }


    
    /**
     * 获取：连接数据库用户名
     */
    public String getUser()
    {
        return user;
    }


    
    /**
     * 设置：连接数据库用户名
     * 
     * @param user 
     */
    public void setUser(String user)
    {
        this.user = user;
    }


    
    /**
     * 获取：连接数据库密码
     */
    public String getPassword()
    {
        return password;
    }


    
    /**
     * 设置：连接数据库密码
     * 
     * @param password 
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
    
}
