package org.hy.common.db;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.hy.common.Counter;





/**
 * 将第三方的Connection对象二次封装，统一监控接口
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-07-13
 * @version     v1.0
 *              v2.0  2024-03-29  添加：caller连接使用者的信息
 */
public class Connection implements java.sql.Connection
{
    
    /** 连接计数器。辅助分析连接使用情况Map.key为使用连接者的信息 */
    public static final Counter<String> $ConnectionCounter = new Counter<String>();
    
    
    
    /** 所属的数据库连接池组 */
    private DataSourceGroup     dataSourceGroup;
    
    /** 第三方的连接对象实例 */
    private java.sql.Connection conn;
    
    /** 连接使用者的信息 */
    private String              caller;
    
    
    
    public Connection(String i_Caller ,final java.sql.Connection i_Connection ,final DataSourceGroup i_DataSourceGroup)
    {
        this.conn            = i_Connection;
        this.dataSourceGroup = i_DataSourceGroup;
        this.caller          = i_Caller;
        $ConnectionCounter.put(i_Caller);
    }
    

    
    /**
     * 获取：所属的数据库连接池组
     */
    public DataSourceGroup getDataSourceGroup()
    {
        return dataSourceGroup;
    }
    

    
    /**
     * 获取：第三方的连接对象实例
     */
    public java.sql.Connection getConnection()
    {
        return conn;
    }
    
    
    
    @Override
    public void close() throws SQLException
    {
        $ConnectionCounter.putMinus(this.caller);
        
        if ( this.dataSourceGroup != null )
        {
            this.dataSourceGroup.connClosed();
        }
        
        this.conn.close();
        this.conn = null;
    }


    
    /**
     * 获取：连接使用者的信息
     */
    public String getCaller()
    {
        return caller;
    }



    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return this.conn.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return this.conn.isWrapperFor(iface);
    }

    @Override
    public Statement createStatement() throws SQLException
    {
        return this.conn.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        return this.conn.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException
    {
        return this.conn.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException
    {
        return this.conn.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException
    {
        this.conn.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException
    {
        return this.conn.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException
    {
        this.conn.commit();
    }

    @Override
    public void rollback() throws SQLException
    {
        this.conn.rollback();
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        return this.conn.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return this.conn.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException
    {
        this.conn.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException
    {
        return this.conn.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException
    {
        this.conn.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException
    {
        return this.conn.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException
    {
        this.conn.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException
    {
        return this.conn.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        return this.conn.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException
    {
        this.conn.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType ,int resultSetConcurrency) throws SQLException
    {
        return this.conn.createStatement(resultSetType ,resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql ,int resultSetType ,int resultSetConcurrency) throws SQLException
    {
        return this.conn.prepareStatement(sql ,resultSetType ,resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql ,int resultSetType ,int resultSetConcurrency) throws SQLException
    {
        return this.conn.prepareCall(sql ,resultSetType ,resultSetConcurrency);
    }

    @Override
    public Map<String ,Class<?>> getTypeMap() throws SQLException
    {
        return this.conn.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String ,Class<?>> map) throws SQLException
    {
        this.conn.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException
    {
        this.conn.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException
    {
        return this.conn.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException
    {
        return this.conn.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException
    {
        return this.conn.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException
    {
        this.conn.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
        this.conn.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType ,int resultSetConcurrency ,int resultSetHoldability) throws SQLException
    {
        return this.conn.createStatement(resultSetType ,resultSetConcurrency ,resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql ,int resultSetType ,int resultSetConcurrency ,int resultSetHoldability) throws SQLException
    {
        return this.conn.prepareStatement(sql ,resultSetType ,resultSetConcurrency ,resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql ,int resultSetType ,int resultSetConcurrency ,int resultSetHoldability) throws SQLException
    {
        return this.conn.prepareCall(sql ,resultSetType ,resultSetConcurrency ,resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql ,int autoGeneratedKeys) throws SQLException
    {
        return this.conn.prepareStatement(sql ,autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql ,int [] columnIndexes) throws SQLException
    {
        return this.conn.prepareStatement(sql ,columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql ,String [] columnNames) throws SQLException
    {
        return this.conn.prepareStatement(sql ,columnNames);
    }

    @Override
    public Clob createClob() throws SQLException
    {
        return this.conn.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException
    {
        return this.conn.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException
    {
        return this.conn.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException
    {
        return this.conn.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException
    {
        return this.conn.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name ,String value) throws SQLClientInfoException
    {
        this.conn.setClientInfo(name ,value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
        this.conn.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException
    {
        return this.conn.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException
    {
        return this.conn.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName ,Object [] elements) throws SQLException
    {
        return this.conn.createArrayOf(typeName ,elements);
    }

    @Override
    public Struct createStruct(String typeName ,Object [] attributes) throws SQLException
    {
        return this.conn.createStruct(typeName ,attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException
    {
        this.conn.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException
    {
        return this.conn.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException
    {
        this.conn.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor ,int milliseconds) throws SQLException
    {
        this.conn.setNetworkTimeout(executor ,milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException
    {
        return this.conn.getNetworkTimeout();
    }

}
