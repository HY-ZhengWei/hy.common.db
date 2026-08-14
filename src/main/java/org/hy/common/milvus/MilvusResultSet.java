package org.hy.common.milvus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.xml.log.Logger;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp.SearchResult;





/**
 * Milvus结果集
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-02
 * @version     v1.0
 */
public class MilvusResultSet
{
    
    private static final Logger $Logger = new Logger(MilvusResultSet.class);
    
    
    
    /** 库名称。当milvus客户端中配置有库名称时，此值可免填写 */
    private String                                dbName;
    
    /** 表名称，必填项。即：向量库中Collection的名称 */
    private String                                tableName;
    
    /** 主键名称 */
    private String                                primaryKey;
    
    /** 表中字段元信息 */
    private List<CreateCollectionReq.FieldSchema> fields;
    
    /** 标量查询结果集 */
    private List<QueryResp.QueryResult>           results;
    
    /** 人工神经网络搜索结果集 */
    private List<List<SearchResult>>              searchResult;
    
    /** 人工神经网络搜索结果集下标。有效下标从0开始 */
    private int                                   searchIndex;
    
    /** 人工神经网络搜索结果集的总数量 */
    private int                                   searchTotalCount;
    
    /** 读取行号。有效下标从0开始 */
    private int                                   rowIndex;
    
    
    
    /**
     * 构造器（适合于标量查询结果集）
     *
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_DBName     库名称
     * @param i_TableName  表名称。即：向量库中Collection的名称
     * @param i_Fields     表中字段元信息
     * @param i_Results    标量查询结果集
     */
    public MilvusResultSet(String i_DBName ,String i_TableName ,List<CreateCollectionReq.FieldSchema> i_Fields ,List<QueryResp.QueryResult> i_Results)
    {
        this.dbName       = i_DBName;
        this.tableName    = i_TableName;
        this.fields       = i_Fields;
        this.primaryKey   = MilvusHelp.queryPrimaryKey(this.fields);
        this.results      = i_Results;
        this.searchResult = null;
        this.searchIndex  = 0;
        this.rowIndex     = -1;
    }
    
    
    
    /**
     * 构造器（适合于人工神经网络搜索结果集）
     *
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_DBName        库名称
     * @param i_TableName     表名称。即：向量库中Collection的名称
     * @param i_Fields        表中字段元信息
     * @param i_SearchResult  人工神经网络搜索结果集
     */
    public MilvusResultSet(String i_DBName ,String i_TableName ,List<CreateCollectionReq.FieldSchema> i_Fields ,boolean i_Search ,List<List<SearchResult>> i_SearchResult)
    {
        this.dbName           = i_DBName;
        this.tableName        = i_TableName;
        this.fields           = i_Fields;
        this.primaryKey       = MilvusHelp.queryPrimaryKey(this.fields);
        this.results          = null;
        this.searchResult     = i_SearchResult;
        this.searchIndex      = 0;
        this.rowIndex         = -1;
        this.searchTotalCount = 0;
        
        for (List<SearchResult> v_Rows : this.searchResult)
        {
            this.searchTotalCount += v_Rows.size();
        }
    }
    
    
    
    /**
     * 定位到有效行
     * 
     * @see java.sql.ResultSet 的 absolute 方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_RowIndex  如果大于结果集最大行数将被取模运算
     *                    如果小于0表示对结果集从倒数的偏移量定位到有效行
     * @return
     */
    public synchronized boolean absolute(int i_RowIndex)
    {
        // 适合于标量查询结果集
        if ( this.results != null )
        {
            this.rowIndex = i_RowIndex % this.results.size();
            if ( this.rowIndex < 0 )
            {
                this.rowIndex = this.results.size() + this.rowIndex;
            }
        }
        // 适合于人工神经网络搜索结果集
        else
        {
            this.rowIndex    = i_RowIndex % this.searchTotalCount;
            this.searchIndex = 0;
            if ( this.rowIndex < 0 )
            {
                this.rowIndex = this.searchTotalCount + this.rowIndex;
            }
            
            for (List<SearchResult> v_Rows : this.searchResult)
            {
                if ( this.rowIndex >= v_Rows.size() )
                {
                    this.searchIndex++;
                    this.rowIndex -= v_Rows.size();
                }
                else
                {
                    break;
                }
            }
        }
        return true;
    }
    
    
    
    /**
     * 查询结果集的下一行
     * 
     * @see java.sql.ResultSet 的 next 方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @return
     */
    public synchronized boolean next()
    {
        // 适合于标量查询结果集
        if ( this.results != null )
        {
            return ++this.rowIndex < this.results.size();
        }
        // 适合于人工神经网络搜索结果集
        else
        {
            for (; this.searchIndex<this.searchResult.size(); this.searchIndex++)
            {
                if ( ++this.rowIndex < this.searchResult.get(this.searchIndex).size() )
                {
                    return true;
                }
                else 
                {
                    this.rowIndex = -1;
                }
            }
            
            return false;
        }
    }
    
    
    /**
     * 按字段下标获取字段值
     * 
     * @see java.sql.ResultSet 的 getObject 方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public Object getObject(int i_ColumnIndex)
    {
        return this.getObject(this.getFieldName(i_ColumnIndex));
    }
    
    
    
    /**
     * 按字段名称获取字段值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnName  字段名称
     * @return
     */
    public Object getObject(String i_ColumnName)
    {
        // 适合于标量查询结果集
        if ( this.results != null )
        {
            QueryResp.QueryResult v_Row = this.results.get(this.rowIndex);
            return v_Row.getEntity().get(i_ColumnName);
        }
        // 适合于人工神经网络搜索结果集
        else
        {
            SearchResult v_Row = this.searchResult.get(this.searchIndex).get(this.rowIndex);
            return v_Row.getEntity().get(i_ColumnName);
        }
    }
    
    
    
    /**
     * 按字段下标获取字段名称
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public String getFieldName(int i_ColumnIndex)
    {
        CreateCollectionReq.FieldSchema v_Field = this.fields.get(i_ColumnIndex % this.fields.size());
        return v_Field.getName();
    }
    
    
    
    /**
     * 获取指定位置上字段的值
     * 
     * @see java.sql.ResultSet 的 getString 方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public String getString(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            return v_Value.toString();
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值
     * 
     * @see java.sql.ResultSet 的 getString 方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public Integer getInt(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            Class<?> v_Calss = v_Value.getClass();
            if ( Integer.class.equals(v_Calss) || int.class.equals(v_Calss) )
            {
                return (Integer) v_Value;
            }
            else if ( String.class.equals(v_Calss) )
            {
                return Integer.parseInt(v_Value.toString());
            }
            else
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not Integer");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public Float getFloat(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            Class<?> v_Calss = v_Value.getClass();
            if ( Float.class.equals(v_Calss) || float.class.equals(v_Calss) )
            {
                return (Float) v_Value;
            }
            else if ( String.class.equals(v_Calss) )
            {
                return Float.parseFloat(v_Value.toString());
            }
            else
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not Float");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值
     * 
     * @see java.sql.ResultSet 的 getDouble 方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public Double getDouble(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            Class<?> v_Calss = v_Value.getClass();
            if ( Double.class.equals(v_Calss) || double.class.equals(v_Calss) )
            {
                return (Double) v_Value;
            }
            else if ( String.class.equals(v_Calss) )
            {
                return Double.parseDouble(v_Value.toString());
            }
            else
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not Double");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值
     * 
     * @see java.sql.ResultSet 的 getLong 方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public Long getLong(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            Class<?> v_Calss = v_Value.getClass();
            if ( Long.class.equals(v_Calss) || long.class.equals(v_Calss) )
            {
                return (Long) v_Value;
            }
            else if ( String.class.equals(v_Calss) )
            {
                return Long.parseLong(v_Value.toString());
            }
            else
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not Long");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值
     * 
     * @see java.sql.ResultSet 的 getShort 方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public Short getShort(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            Class<?> v_Calss = v_Value.getClass();
            if ( Short.class.equals(v_Calss) || short.class.equals(v_Calss) )
            {
                return (Short) v_Value;
            }
            else if ( String.class.equals(v_Calss) )
            {
                return Short.parseShort(v_Value.toString());
            }
            else
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not Short");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值
     * 
     * @see java.sql.ResultSet 的 getByte 方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public Byte getByte(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            Class<?> v_Calss = v_Value.getClass();
            if ( Byte.class.equals(v_Calss) || byte.class.equals(v_Calss) )
            {
                return (Byte) v_Value;
            }
            else if ( String.class.equals(v_Calss) )
            {
                return Byte.parseByte(v_Value.toString());
            }
            else
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not Byte");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值
     * 
     * @see java.sql.ResultSet 的 getBoolean 方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public Boolean getBoolean(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            Class<?> v_Calss = v_Value.getClass();
            if ( Boolean.class.equals(v_Calss) || boolean.class.equals(v_Calss) )
            {
                return (Boolean) v_Value;
            }
            else if ( String.class.equals(v_Calss) )
            {
                return Boolean.parseBoolean(v_Value.toString());
            }
            else
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not Boolean");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值
     * 
     * @see java.sql.ResultSet 的 getBigDecimal 方法。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public BigDecimal getBigDecimal(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            Class<?> v_Calss = v_Value.getClass();
            if ( Integer.class.equals(v_Calss) || int.class.equals(v_Calss) )
            {
                return new BigDecimal((Integer) v_Value);
            }
            else if ( Long.class.equals(v_Calss) || long.class.equals(v_Calss) )
            {
                return new BigDecimal((Long) v_Value);
            }
            else if ( Float.class.equals(v_Calss) || float.class.equals(v_Calss) )
            {
                return new BigDecimal((Float) v_Value);
            }
            else if ( Double.class.equals(v_Calss) || double.class.equals(v_Calss) )
            {
                return new BigDecimal((Double) v_Value);
            }
            else if ( String.class.equals(v_Calss) )
            {
                return new BigDecimal(v_Value.toString());
            }
            else
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not BigDecimal");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public Date getDate(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            try
            {
                return new Date(v_Value.toString());
            }
            catch (Exception exce)
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not Date");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值（带时区时间戳，如2026-08-02T10:30:00+08:00）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public OffsetDateTime getTimestamptz(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            try
            {
                return OffsetDateTime.parse(v_Value.toString());
            }
            catch (Exception exce)
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not Timestamptz");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值（遵循OGC OpenGIS标准，用于存储二维空间几何对象（点位、线段、多边形、围栏区域等））
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    public Geometry getGeometry(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            try
            {
                return new WKTReader().read(v_Value.toString());
            }
            catch (Exception exce)
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not Geometry");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String ,Object> getJson(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            try
            {
                return (Map<String ,Object>) v_Value;
            }
            catch (Exception exce)
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not Json");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取指定位置上字段的值（多组向量Float数据）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_ColumnIndex  字段下标值，从0开始
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Float> getVectorFloat(int i_ColumnIndex)
    {
        Object v_Value = this.getObject(i_ColumnIndex);
        if ( v_Value == null )
        {
            return null;
        }
        else
        {
            try
            {
                return (List<Float>) v_Value;
            }
            catch (Exception exce)
            {
                RuntimeException v_Exce = new RuntimeException(this.dbName + "." + this.tableName + "." 
                                                             + this.getObject(this.primaryKey).toString() + "." + this.getFieldName(i_ColumnIndex) 
                                                             + " = " + v_Value.toString() + " is not VectorFloat");
                $Logger.error(v_Exce);
                throw v_Exce;
            }
        }
    }
    
    
    
    /**
     * 获取：标量查询结果集
     */
    public List<QueryResp.QueryResult> getResults()
    {
        return results;
    }

    
    
    /**
     * 获取：人工神经网络搜索结果集
     */
    public List<List<SearchResult>> getSearchResult()
    {
        return searchResult;
    }
    
    
    
    /**
     * 获取：库名称。当milvus客户端中配置有库名称时，此值可免填写
     */
    public String getDbName()
    {
        return dbName;
    }

    
    
    /**
     * 获取：表名称，必填项。即：向量库中Collection的名称
     */
    public String getTableName()
    {
        return tableName;
    }
    
    
    
    /**
     * 获取：主键名称
     */
    public String getPrimaryKey()
    {
        return primaryKey;
    }


    
    /**
     * 获取：人工神经网络搜索结果集下标。有效下标从0开始
     */
    public int getSearchIndex()
    {
        return searchIndex;
    }


    
    /**
     * 获取：人工神经网络搜索结果集的总数量
     */
    public int getSearchTotalCount()
    {
        return searchTotalCount;
    }


    
    /**
     * 获取：读取行号。有效下标从0开始
     */
    public int getRowIndex()
    {
        return rowIndex;
    }
    

    
    /**
     * 获取：表中字段元信息
     */
    public List<CreateCollectionReq.FieldSchema> getFields()
    {
        return fields;
    }

    

    /**
     * 清理
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     */
    public void clear()
    {
        this.fields .clear();
        this.results.clear();
        this.fields  = null;
        this.results = null;
    }

}
