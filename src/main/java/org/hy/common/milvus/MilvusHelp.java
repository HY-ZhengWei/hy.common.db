package org.hy.common.milvus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.StringHelp;
import org.hy.common.XJavaID;
import org.hy.common.milvus.schema.Collection;
import org.hy.common.milvus.schema.Field;
import org.hy.common.milvus.schema.FieldIndex;
import org.hy.common.milvus.schema.Function;
import org.hy.common.xml.log.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.common.IndexParam.IndexParamBuilder;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.AddFieldReq.AddFieldReqBuilder;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq.CreateCollectionReqBuilder;
import io.milvus.v2.service.collection.request.CreateCollectionReq.Function.FunctionBuilder;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.request.DescribeCollectionReq.DescribeCollectionReqBuilder;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq.DropCollectionReqBuilder;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq.GetLoadStateReqBuilder;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq.HasCollectionReqBuilder;
import io.milvus.v2.service.collection.request.ListCollectionsReq;
import io.milvus.v2.service.collection.request.ListCollectionsReq.ListCollectionsReqBuilder;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq.LoadCollectionReqBuilder;
import io.milvus.v2.service.collection.request.ReleaseCollectionReq;
import io.milvus.v2.service.collection.request.ReleaseCollectionReq.ReleaseCollectionReqBuilder;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import io.milvus.v2.service.vector.request.AnnSearchReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.DeleteReq.DeleteReqBuilder;
import io.milvus.v2.service.vector.request.GetReq;
import io.milvus.v2.service.vector.request.GetReq.GetReqBuilder;
import io.milvus.v2.service.vector.request.HybridSearchReq;
import io.milvus.v2.service.vector.request.HybridSearchReq.HybridSearchReqBuilder;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.InsertReq.InsertReqBuilder;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.QueryReq.QueryReqBuilder;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.SearchReq.SearchReqBuilder;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.UpsertReq.UpsertReqBuilder;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import io.milvus.v2.service.vector.response.UpsertResp;





/**
 * Milvus的帮助类
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-07-28
 * @version     v1.0
 */
public class MilvusHelp implements XJavaID
{
    
    private static final Logger $Logger = new Logger(MilvusHelp.class);
    
    
    
    /** 数据库客户端 */
    private MilvusClientV2 milvus;
    
    /** 逻辑ID */
    private String         xid;
    
    /** 注释 */
    private String         comment;
    
    /** 整体超时时长（单位：毫秒） */
    private Long           timeout;
    
    
    
    public MilvusHelp()
    {
        this(null);
    }
    
    
    
    public MilvusHelp(MilvusClientV2 i_Milvus)
    {
        this(i_Milvus ,null);
    }
    
    
    
    /**
     * 构造器
     *
     * @author      ZhengWei(HY)
     * @createDate  2026-08-17
     * @version     v1.0
     *
     * @param i_Milvus     
     * @param i_TimeoutMS  超时时长（单位：毫秒）
     */
    public MilvusHelp(MilvusClientV2 i_Milvus ,Long i_TimeoutMS)
    {
        super();
        this.setMilvus(i_Milvus);
        
        if ( i_TimeoutMS != null && i_TimeoutMS > 0 )
        {
            this.timeout = i_TimeoutMS;
            this.milvus.withTimeout(this.timeout ,TimeUnit.MILLISECONDS);
        }
        else
        {
            this.timeout = null;
        }
    }
    
    
    
    /**
     * 整体超时时长（单位：毫秒）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-17
     * @version     v1.0
     *
     * @return
     */
    public Long getTimeout()
    {
        return this.timeout;
    }
    
    
    
    /**
     * 创建表结构
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-13
     * @version     v1.0
     *
     * @param i_Collection 表的Json结构。与Milvus官网页面中的 “代码” 页面中的Json格式保持一致。
     * @return
     */
    public Boolean createCollection(Collection i_Collection)
    {
        return this.createCollection(null ,i_Collection);
    }
    
    
    
    /**
     * 创建表结构
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-13
     * @version     v1.0
     *
     * @param i_DBName     库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_Collection 表的Json结构。与Milvus官网页面中的 “代码” 页面中的Json格式保持一致。
     * @return             返回 NULL 表示发生异常
     */
    @SuppressWarnings("unchecked")
    public Boolean createCollection(String i_DBName ,Collection i_Collection)
    {
        if ( i_Collection == null )
        {
            return false;
        }
        if ( Help.isNull(i_Collection.getFields()) )
        {
            return false;
        }
        if ( Help.isNull(i_Collection.getCollection_name()) )
        {
            return false;
        }
        if ( Help.isNull(i_Collection.getFields()) )
        {
            return false;
        }
        
        // 配置字段
        String                               v_PKFieldName = null;
        boolean                              v_AutoID      = false;
        CreateCollectionReq.CollectionSchema v_Schema      = CreateCollectionReq.CollectionSchema.builder().build();
        List<IndexParam>                     v_IndexParams = new ArrayList<IndexParam>();
        for (Field v_Field : i_Collection.getFields())
        {
            if ( Help.isNull(v_Field.getName()) )
            {
                $Logger.error("Create Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_Collection.getCollection_name() + "] Field.name is null");
                return false;
            }
            if ( v_Field.getDataType() == null )
            {
                $Logger.error("Create Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_Collection.getCollection_name() + "] Field[" + v_Field.getName() + "].dataType is null");
                return false;
            }
            
            AddFieldReqBuilder<?> v_FieldReqBuilder = AddFieldReq.builder()
                                                                 .fieldName(v_Field.getName())
                                                                 .dataType(v_Field.getDataType());
            if ( !Help.isNull(v_Field.getDescription()) )
            {
                v_FieldReqBuilder.description(v_Field.getDescription());
            }
            if ( !Help.isNull(v_Field.getIs_primary_key()) )
            {
                v_FieldReqBuilder.isPrimaryKey(v_Field.getIs_primary_key());
                if ( v_Field.getIs_primary_key() )
                {
                    if ( !Help.isNull(v_PKFieldName) )
                    {
                        // 仅允许有一个主键
                        $Logger.error("Create Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_Collection.getCollection_name() + "] have two primary keys");
                        return false;
                    }
                    v_PKFieldName = v_Field.getName();
                }
            }
            if ( !Help.isNull(v_Field.getIs_partition_key()) )
            {
                v_FieldReqBuilder.isPartitionKey(v_Field.getIs_partition_key());
            }
            if ( !Help.isNull(v_Field.getAutoID()) )
            {
                v_FieldReqBuilder.autoID(v_Field.getAutoID());
                if ( v_Field.getAutoID() )
                {
                    v_AutoID = true;
                }
            }
            if ( !Help.isNull(v_Field.getNullable()) )
            {
                v_FieldReqBuilder.isNullable(v_Field.getNullable());
            }
            if ( !Help.isNull(v_Field.getEnable_analyzer()) )
            {
                v_FieldReqBuilder.enableAnalyzer(v_Field.getEnable_analyzer());
            }
            if ( v_Field.getAnalyzer_params() != null && !Help.isNull(v_Field.getAnalyzer_params().getType()) )
            {
                Map<String, Object> v_AnalyzerParams = new HashMap<String ,Object>();
                v_AnalyzerParams.put("type" ,v_Field.getAnalyzer_params().getType());
                v_FieldReqBuilder.analyzerParams(v_AnalyzerParams);
            }
            if ( !Help.isNull(v_Field.getMax_length()) && v_Field.getMax_length() > 0 )
            {
                v_FieldReqBuilder.maxLength(v_Field.getMax_length());
            }
            
            v_Schema.addField(v_FieldReqBuilder.build());
            
            // 配置索引
            if ( Help.isNull(v_Field.getIndexes()) )
            {
                continue;
            }
            for (FieldIndex v_FIndex :v_Field.getIndexes())
            {
                IndexParamBuilder v_IndexParamBuilder = IndexParam.builder().fieldName(v_Field.getName());
                
                if ( v_FIndex.getIndexType() != null )
                {
                    v_IndexParamBuilder.indexType(v_FIndex.getIndexType());
                }
                else
                {
                    // 索引类型必须有
                    $Logger.error("Create Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_Collection.getCollection_name() + "] Field[" + v_Field.getName() + "].indexType is null");
                    return false;
                }
                if ( !Help.isNull(v_FIndex.getIndex_name()) )
                {
                    v_IndexParamBuilder.indexName(v_FIndex.getIndex_name());
                }
                if ( v_FIndex.getMetricType() != null )
                {
                    v_IndexParamBuilder.metricType(v_FIndex.getMetricType());
                }
                if ( !Help.isNull(v_FIndex.getIndexParameterPairs()) )
                {
                    Map<String ,Object> v_ExtraParams = (Map<String ,Object>) Help.toMap(v_FIndex.getIndexParameterPairs() ,"key" ,"value");
                    v_ExtraParams.remove("index_type");   // 已通过上面 .indexType()  方法配置，重复配置会报错
                    v_ExtraParams.remove("metric_type");  // 已通过上面 .metricType() 方法配置，重复配置会报错
                    if ( !Help.isNull(v_ExtraParams) )
                    {
                        v_IndexParamBuilder.extraParams(v_ExtraParams);
                    }
                }
                
                v_IndexParams.add(v_IndexParamBuilder.build());
            }
        }
        
        // 配置函数
        if ( !Help.isNull(i_Collection.getFunctions()) )
        {
            for (Function v_Function : i_Collection.getFunctions())
            {
                FunctionBuilder<?> v_FunctionBuilder = io.milvus.v2.service.collection.request.CreateCollectionReq.Function.builder();
                
                if ( !Help.isNull(v_Function.getName()) )
                {
                    v_FunctionBuilder.name(v_Function.getName());
                }
                else
                {
                    // 函数名称必须有
                    $Logger.error("Create Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_Collection.getCollection_name() + "] Function.name is null");
                    return false;
                }
                if ( !Help.isNull(v_Function.getDescription()) )
                {
                    v_FunctionBuilder.description(v_Function.getDescription());
                }
                if ( v_Function.getFunctionType() != null )
                {
                    v_FunctionBuilder.functionType(v_Function.getFunctionType());
                }
                else
                {
                    // 函数类型必须有
                    $Logger.error("Create Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_Collection.getCollection_name() + "] Function[" + v_Function.getName() + "].type is null");
                    return false;
                }
                if ( !Help.isNull(v_Function.getInput_field_names()) )
                {
                    v_FunctionBuilder.inputFieldNames(v_Function.getInput_field_names());
                }
                if ( !Help.isNull(v_Function.getOutput_field_names()) )
                {
                    v_FunctionBuilder.outputFieldNames(v_Function.getOutput_field_names());
                }
                
                v_Schema.addFunction(v_FunctionBuilder.build());
            }
        }
        
        if ( Help.isNull(v_PKFieldName) )
        {
            // 必须有主键
            $Logger.error("Create Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_Collection.getCollection_name() + "] not have primary key");
            return false;
        }
        
        // 配置表
        CreateCollectionReqBuilder v_ReqBuilder = CreateCollectionReq.builder()
                                                                     .collectionName(i_Collection.getCollection_name())
                                                                     .collectionSchema(v_Schema)
                                                                     .primaryFieldName(v_PKFieldName)
                                                                     .autoID(v_AutoID);
        if ( !Help.isNull(v_IndexParams) )
        {
            v_ReqBuilder.indexParams(v_IndexParams);
        }
        if ( !Help.isNull(i_DBName) )
        {
            v_ReqBuilder.databaseName(i_DBName);
        }
        if ( !Help.isNull(i_Collection.getDescription()) )
        {
            v_ReqBuilder.description(i_Collection.getDescription());
        }
        if ( i_Collection.getConsistencyLevel() != null )
        {
            v_ReqBuilder.consistencyLevel(i_Collection.getConsistencyLevel());
        }
        if ( i_Collection.getShards_num() != null && i_Collection.getShards_num() >= 1 )
        {
            v_ReqBuilder.numShards(i_Collection.getShards_num());
        }
        if ( i_Collection.getProperties() != null && !Help.isNull(i_Collection.getProperties().getTimezone()) )
        {
            v_ReqBuilder.property("timezone" ,i_Collection.getProperties().getTimezone());
        }
        
        try
        {
            this.milvus.createCollection(v_ReqBuilder.build());
            return true;
        }
        catch (Exception exce)
        {
            $Logger.error("Create Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_Collection.getCollection_name() + "] is error" ,exce);
            return null;
        }
    }
    
    
    
    /**
     * Milvus连接正确，及客户端配置是否正确的效验
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-17
     * @version     v1.0
     *
     * @return
     */
    public boolean isValid()
    {
        Boolean v_Exists = this.exists(MilvusHelp.class.getSimpleName());
        return v_Exists != null;
    }
    
    
    
    /**
     * 查询表列表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-14
     * @version     v1.0
     *
     * @return  返回表名称列表
     *          返回 NULL 表示发生异常
     */
    public List<String> queryCollections()
    {
        return this.queryCollections(null);
    }
    
    
    
    /**
     * 查询表列表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-14
     * @version     v1.0
     *
     * @param i_DBName  库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @return          返回表名称列表
     *                  返回 NULL 表示发生异常
     */
    public List<String> queryCollections(String i_DBName)
    {
        ListCollectionsReqBuilder v_ListBuilder = ListCollectionsReq.builder();
        
        if ( !Help.isNull(i_DBName) )
        {
            v_ListBuilder.databaseName(i_DBName);
        }
        
        try
        {
            ListCollectionsResp v_ListResp = this.milvus.listCollectionsV2(v_ListBuilder.build());
            return v_ListResp.getCollectionNames();
        }
        catch (Exception exce)
        {
            $Logger.error("Query Collections[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "] is error" ,exce);
            return null;
        }
    }
    
    
    
    /**
     * 判定表是否存在
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-17
     * @version     v1.0
     *
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @return             返回是否加载成功
     *                     返回 NULL 表示发生异常
     */
    public Boolean exists(String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return false;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.exists(v_Names[0] ,v_Names[1]);
    }
    
    
    
    /**
     * 判定表是否存在
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-17
     * @version     v1.0
     *
     * @param i_DBName     库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称
     * @return             返回是否加载成功
     *                     返回 NULL 表示发生异常
     */
    public Boolean exists(String i_DBName ,String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return false;
        }
        
        HasCollectionReqBuilder v_HasLoadBuilder = HasCollectionReq.builder().collectionName(i_TableName);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_HasLoadBuilder.databaseName(i_DBName);
        }
        
        try
        {
            return this.milvus.hasCollection(v_HasLoadBuilder.build());
        }
        catch (Exception exce)
        {
            $Logger.error("exists Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_TableName + "] is error" ,exce);
            return null;
        }
    }
    
    
    
    /**
     * 获取表是否加载的状态
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-13
     * @version     v1.0
     *
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @return             返回是否加载成功
     *                     返回 NULL 表示发生异常
     */
    public Boolean isLoadCollection(String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return false;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.isLoadCollection(v_Names[0] ,v_Names[1]);
    }
    
    
    
    /**
     * 获取表是否加载的状态
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-13
     * @version     v1.0
     *
     * @param i_DBName     库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称
     * @return             返回是否加载成功
     *                     返回 NULL 表示发生异常
     */
    public Boolean isLoadCollection(String i_DBName ,String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return false;
        }
        
        GetLoadStateReqBuilder v_GetLoadBuilder = GetLoadStateReq.builder().collectionName(i_TableName);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_GetLoadBuilder.databaseName(i_DBName);
        }
        
        try
        {
            return this.milvus.getLoadState(v_GetLoadBuilder.build());
        }
        catch (Exception exce)
        {
            $Logger.error("IsLoad Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_TableName + "] is error" ,exce);
            return null;
        }
    }
    
    
    
    /**
     * 加载表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-13
     * @version     v1.0
     *
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @return             返回是否加载成功
     *                     返回 NULL 表示发生异常
     */
    public Boolean loadCollection(String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return false;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.loadCollection(v_Names[0] ,v_Names[1]);
    }
    
    
    
    /**
     * 加载表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-13
     * @version     v1.0
     *
     * @param i_DBName     库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称
     * @return             返回是否加载成功
     *                     返回 NULL 表示发生异常
     */
    public Boolean loadCollection(String i_DBName ,String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return false;
        }
        
        LoadCollectionReqBuilder v_LoadBuilder = LoadCollectionReq.builder().collectionName(i_TableName);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_LoadBuilder.databaseName(i_DBName);
        }
        
        try
        {
            this.milvus.loadCollection(v_LoadBuilder.build());
        }
        catch (Exception exce)
        {
            $Logger.error("Load Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_TableName + "] error" ,exce);
        }
        return this.isLoadCollection(i_DBName ,i_TableName);
    }
    
    
    
    /**
     * 释放表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-13
     * @version     v1.0
     *
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @return             返回是否释放成功
     *                     返回 NULL 表示发生异常
     */
    public Boolean releaseCollection(String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return false;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.releaseCollection(v_Names[0] ,v_Names[1]);
    }
    
    
    
    /**
     * 释放表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-13
     * @version     v1.0
     *
     * @param i_DBName     库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称
     * @return             返回是否释放成功
     *                     返回 NULL 表示发生异常
     */
    public Boolean releaseCollection(String i_DBName ,String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return false;
        }
        
        ReleaseCollectionReqBuilder v_ReleaseBuilder = ReleaseCollectionReq.builder().collectionName(i_TableName);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_ReleaseBuilder.databaseName(i_DBName);
        }
        
        try
        {
            this.milvus.releaseCollection(v_ReleaseBuilder.build());
        }
        catch (Exception exce)
        {
            $Logger.error("Release Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_TableName + "] error" ,exce);
        }
        
        Boolean v_Ret = this.isLoadCollection(i_DBName ,i_TableName);
        return v_Ret == null ? null : !v_Ret;
    }
    
    
    
    /**
     * 删除表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-14
     * @version     v1.0
     *
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @return             返回是否释放成功
     *                     返回 NULL 表示发生异常
     */
    public Boolean dropCollection(String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return false;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.dropCollection(v_Names[0] ,v_Names[1]);
    }
    
    
    
    /**
     * 删除表
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-14
     * @version     v1.0
     *
     * @param i_DBName     库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称
     * @return             返回是否删除成功
     *                     返回 NULL 表示发生异常
     */
    public Boolean dropCollection(String i_DBName ,String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return false;
        }
        
        DropCollectionReqBuilder v_DropBuilder = DropCollectionReq.builder().collectionName(i_TableName);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_DropBuilder.databaseName(i_DBName);
        }
        
        try
        {
            this.milvus.dropCollection(v_DropBuilder.build());
            return true;
        }
        catch (Exception exce)
        {
            $Logger.error("Drop Collection[" + Help.NVL(i_DBName ,this.milvus.currentUsedDatabase()) + "." + i_TableName + "] error" ,exce);
            return null;
        }
    }
    
    
    
    /**
     * 查询表中主键字段的名称
     * 
     * 注：到目前为此Milvus仅支持单主键，尚不支持组合主键，所以本方法返回类型为String
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-31
     * @version     v1.0
     *
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @return
     */
    public String queryPrimaryKey(String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.queryPrimaryKey(v_Names[0] ,v_Names[1]);
    }
    
    
    
    /**
     * 查询表中主键字段的名称
     * 
     * 注：到目前为此Milvus仅支持单主键，尚不支持组合主键，所以本方法返回类型为String
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-31
     * @version     v1.0
     *
     * @param i_DBName     库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称
     * @return
     */
    public String queryPrimaryKey(String i_DBName ,String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        List<CreateCollectionReq.FieldSchema> v_Fields = querySchema(i_DBName ,i_TableName);
        if ( Help.isNull(v_Fields) )
        {
            return null;
        }
        
        String v_PrimaryKey = MilvusHelp.queryPrimaryKey(v_Fields);
        
        v_Fields.clear();
        v_Fields = null;
        return v_PrimaryKey;
    }
    
    
    
    /**
     * 查询表中主键字段的名称
     * 
     * 注：到目前为此Milvus仅支持单主键，尚不支持组合主键，所以本方法返回类型为String
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_Fileds  字段元信息
     * @return
     */
    public static String queryPrimaryKey(List<CreateCollectionReq.FieldSchema> i_Fileds)
    {
        if ( Help.isNull(i_Fileds))
        {
            return null;
        }
        
        for (CreateCollectionReq.FieldSchema v_Field : i_Fileds)
        {
            if ( v_Field.getIsPrimaryKey() )
            {
                return v_Field.getName();
            }
        }
        
        return null;
    }
    
    
    
    /**
     * 查询表中所有字段的名称
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-05
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @return
     */
    public List<String> queryFields(String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.queryFields(v_Names[0] ,v_Names[1]);
    }
    
    
    
    /**
     * 查询表中所有字段的名称
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-05
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @return
     */
    public List<String> queryFields(String i_DBName ,String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        return this.queryFields_Core(i_DBName ,i_TableName ,true);
    }
    
    
    
    /**
     * 查询表中所有字段的名称
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-05
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @return
     */
    public static List<String> queryFields(List<CreateCollectionReq.FieldSchema> i_Fileds)
    {
        return MilvusHelp.queryFields(i_Fileds ,true);
    }
    
    
    
    /**
     * 查询表中所有字段的名称（不包含主键）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-05
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @return
     */
    public List<String> queryFieldsNotPK(String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.queryFieldsNotPK(v_Names[0] ,v_Names[1]);
    }
    
    
    
    /**
     * 查询表中所有字段的名称（不包含主键）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-05
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @return
     */
    public List<String> queryFieldsNotPK(String i_DBName ,String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        return this.queryFields_Core(i_DBName ,i_TableName ,false);
    }
    
    
    
    /**
     * 查询表中所有字段的名称（不包含主键）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-05
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @return
     */
    public static List<String> queryFieldsNotPK(List<CreateCollectionReq.FieldSchema> i_Fileds)
    {
        return MilvusHelp.queryFields(i_Fileds ,false);
    }
    
    
    
    /**
     * 查询表中所有字段的名称
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-05
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @param i_HavePrimaryKey 是否包含主键字段
     * @return
     */
    private List<String> queryFields_Core(String i_DBName ,String i_TableName ,boolean i_HavePrimaryKey)
    {
        List<CreateCollectionReq.FieldSchema> v_FieldScheams = querySchema(i_DBName ,i_TableName);
        if ( Help.isNull(v_FieldScheams) )
        {
            return null;
        }
        
        return MilvusHelp.queryFields(v_FieldScheams ,i_HavePrimaryKey);
    }
    
    
    
    /**
     * 查询表中所有字段的名称（不包含主键）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-05
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @param i_HavePrimaryKey 是否包含主键字段
     * @return
     */
    private static List<String> queryFields(List<CreateCollectionReq.FieldSchema> i_Fileds ,boolean i_HavePrimaryKey)
    {
        if ( Help.isNull(i_Fileds) )
        {
            return null;
        }
        
        List<String> v_Ret = new ArrayList<String>();
        if ( i_HavePrimaryKey )
        {
            for (CreateCollectionReq.FieldSchema v_Field : i_Fileds)
            {
                v_Ret.add(v_Field.getName());
            }
        }
        else
        {
            for (CreateCollectionReq.FieldSchema v_Field : i_Fileds)
            {
                if ( v_Field.getIsPrimaryKey() )
                {
                    continue;
                }
                v_Ret.add(v_Field.getName());
            }
        }
        return v_Ret;
    }
    
    
    
    /**
     * 查询表中所有字段的Schema
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @return
     */
    public List<CreateCollectionReq.FieldSchema> querySchema(String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.querySchema(v_Names[0] ,v_Names[1]);
    }
    
    
    
    /**
     * 查询表中所有字段的Schema
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @return
     */
    public List<CreateCollectionReq.FieldSchema> querySchema(String i_DBName ,String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        DescribeCollectionReqBuilder v_ReqBuilder = DescribeCollectionReq.builder().collectionName(i_TableName);
        if ( !Help.isNull(i_DBName) )
        {
            v_ReqBuilder.databaseName(i_DBName);
        }
        
        DescribeCollectionReq  v_Req  = v_ReqBuilder.build();
        DescribeCollectionResp v_Resp = this.milvus.describeCollection(v_Req);
        return v_Resp.getCollectionSchema().getFieldSchemaList();
    }
    
    
    
    /**
     * 添加一行或多行数据
     * 
     * 注1：向量库Schema中定义的主键是自动生成时，i_Data数据请不要包括主键信息。
     * 注2：向量字段维度Schema中定义多长，i_Data数据中的向量字段就要是多长的。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-29
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_Data           数据。支持Json String、Java对象、集合对象
     *                              注：Json String 默认按单行数据解析
     * @return                 返回添加数据的主键
     */
    public List<Object> insert(String i_TableName ,Object i_Data)
    {
        return this.insert(i_TableName ,null ,i_Data);
    }
    
    
    
    /**
     * 添加一行或多行数据
     * 
     * 注1：向量库Schema中定义的主键是自动生成时，i_Data数据请不要包括主键信息。
     * 注2：向量字段维度Schema中定义多长，i_Data数据中的向量字段就要是多长的。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-29
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_Data           数据。支持Json String、Java对象、集合对象
     *                              注：Json String 默认按单行数据解析
     * @return                 返回添加数据的主键
     */
    public List<Object> insert(String i_TableName ,String i_PartitionName ,Object i_Data)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.insert_Core(v_Names[0] ,v_Names[1] ,i_PartitionName ,i_Data ,true);
    }
    
    
    
    /**
     * 添加多行或一行数据
     * 
     * 注1：向量库Schema中定义的主键是自动生成时，i_Data数据请不要包括主键信息。
     * 注2：向量字段维度Schema中定义多长，i_Data数据中的向量字段就要是多长的。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_Data           数据。支持Json String、Java对象、集合对象
     *                              注：Json String 默认按多行数据解析
     * @return                 返回添加数据的主键
     */
    public List<Object> inserts(String i_TableName ,Object i_Data)
    {
        return this.inserts(i_TableName ,null ,i_Data);
    }
    
    
    
    /**
     * 添加多行或一行数据
     * 
     * 注1：向量库Schema中定义的主键是自动生成时，i_Data数据请不要包括主键信息。
     * 注2：向量字段维度Schema中定义多长，i_Data数据中的向量字段就要是多长的。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_Data           数据。支持Json String、Java对象、集合对象
     *                              注：Json String 默认按多行数据解析
     * @return                 返回添加数据的主键
     */
    public List<Object> inserts(String i_TableName ,String i_PartitionName ,Object i_Data)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.insert_Core(v_Names[0] ,v_Names[1] ,i_PartitionName ,i_Data ,false);
    }
    
    
    
    /**
     * 添加多行或一行数据
     * 
     * 注1：向量库Schema中定义的主键是自动生成时，i_Data数据请不要包括主键信息。
     * 注2：向量字段维度Schema中定义多长，i_Data数据中的向量字段就要是多长的。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_Data           数据。支持Json String、Java对象、集合对象
     *                              注：Json String 默认按多行数据解析
     * @param i_IsSingle       当数据是JsonString时，true是按单行数据处理，false是按多行数据处理
     * @return                 返回添加数据的主键
     */
    private List<Object> insert_Core(String i_DBName ,String i_TableName ,String i_PartitionName ,Object i_Data ,boolean i_IsSingle)
    {
        List<JsonObject> v_Datas      = MilvusHelp.toJsonObject(i_Data ,i_IsSingle);
        InsertReqBuilder v_ReqBuilder = InsertReq.builder().collectionName(i_TableName);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_ReqBuilder.databaseName(i_DBName);
        }
        if ( !Help.isNull(i_PartitionName) )
        {
            v_ReqBuilder.partitionName(i_PartitionName);
        }
        
        InsertReq  v_InsertReq  = v_ReqBuilder.data(v_Datas).build();
        InsertResp v_InsertResp = this.milvus.insert(v_InsertReq);
        if ( v_InsertResp != null )
        {
            return v_InsertResp.getPrimaryKeys();
        }
        else
        {
            return null;
        }
    }
    
    
    
    /**
     * 更新一行或多行数据
     * 
     * 注1：主键对应的数据不存在 → 执行 新增插入 (insert) 向量数据
     * 注2：主键对应的数据已存在 → 执行 更新 (update) 该行全部字段
     * 注3：主键信息必须存在于数据i_Data中
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_Data           数据。支持Json String、Java对象、集合对象
     *                              注：Json String 默认按单行数据解析
     * @return                 返回操作数据的主键
     */
    public List<Object> upsert(String i_TableName ,Object i_Data)
    {
        return this.upsert(i_TableName ,null ,i_Data);
    }
    
    
    
    /**
     * 更新一行或多行数据
     * 
     * 注1：主键对应的数据不存在 → 执行 新增插入 (insert) 向量数据
     * 注2：主键对应的数据已存在 → 执行 更新 (update) 该行全部字段
     * 注3：主键信息必须存在于数据i_Data中
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_Data           数据。支持Json String、Java对象、集合对象
     *                              注：Json String 默认按单行数据解析
     * @return                 返回操作数据的主键
     */
    public List<Object> upsert(String i_TableName ,String i_PartitionName ,Object i_Data)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.upsert_Core(v_Names[0] ,v_Names[1] ,i_PartitionName ,i_Data ,true);
    }
    
    
    
    /**
     * 更新多行或一行数据
     * 
     * 注1：主键对应的数据不存在 → 执行 新增插入 (insert) 向量数据
     * 注2：主键对应的数据已存在 → 执行 更新 (update) 该行全部字段
     * 注3：主键信息必须存在于数据i_Data中
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_Data           数据。支持Json String、Java对象、集合对象
     *                              注：Json String 默认按单行数据解析
     * @return                 返回操作数据的主键
     */
    public List<Object> upserts(String i_TableName ,Object i_Data)
    {
        return this.upserts(i_TableName ,null ,i_Data);
    }
    
    
    
    /**
     * 更新多行或一行数据
     * 
     * 注1：主键对应的数据不存在 → 执行 新增插入 (insert) 向量数据
     * 注2：主键对应的数据已存在 → 执行 更新 (update) 该行全部字段
     * 注3：主键信息必须存在于数据i_Data中
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_Data           数据。支持Json String、Java对象、集合对象
     *                              注：Json String 默认按单行数据解析
     * @return                 返回操作数据的主键
     */
    public List<Object> upserts(String i_TableName ,String i_PartitionName ,Object i_Data)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.upsert_Core(v_Names[0] ,v_Names[1] ,i_PartitionName ,i_Data ,false);
    }
    
    
    
    /**
     * 更新一行或多行数据
     * 
     * 注1：主键对应的数据不存在 → 执行 新增插入 (insert) 向量数据
     * 注2：主键对应的数据已存在 → 执行 更新 (update) 该行全部字段
     * 注3：主键信息必须存在于数据i_Data中
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_Data           数据。支持Json String、Java对象、集合对象
     *                              注：Json String 默认按单行数据解析
     * @param i_IsSingle       当数据是JsonString时，true是按单行数据处理，false是按多行数据处理
     * @return                 返回操作数据的主键
     */
    private List<Object> upsert_Core(String i_DBName ,String i_TableName ,String i_PartitionName ,Object i_Data ,boolean i_IsSingle)
    {
        List<JsonObject> v_Datas      = MilvusHelp.toJsonObject(i_Data ,i_IsSingle);
        UpsertReqBuilder v_ReqBuilder = UpsertReq.builder().collectionName(i_TableName);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_ReqBuilder.databaseName(i_DBName);
        }
        if ( !Help.isNull(i_PartitionName) )
        {
            v_ReqBuilder.partitionName(i_PartitionName);
        }
        
        UpsertReq  v_UpsertReq  = v_ReqBuilder.data(v_Datas).build();
        UpsertResp v_UpsertResp = this.milvus.upsert(v_UpsertReq);
        if ( v_UpsertResp != null )
        {
            return v_UpsertResp.getPrimaryKeys();
        }
        else
        {
            return null;
        }
    }
    
    
    
    /**
     * 删除一行数据（主键删除）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_ID             主键数据
     * @return                 返回操作数据的数量，异常时返回-1。
     */
    public long delete(String i_TableName ,Object i_ID)
    {
        return this.delete(i_TableName ,null ,i_ID);
    }
    
    
    
    /**
     * 删除一行数据（主键删除）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_ID             主键数据
     * @return                 返回操作数据的数量，异常时返回-1。
     */
    public long delete(String i_TableName ,String i_PartitionName ,Object i_ID)
    {
        if ( Help.isNull(i_TableName) )
        {
            return -1;
        }
        
        if ( i_ID == null )
        {
            return -1;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.delete_Core(v_Names[0] ,v_Names[1] ,i_PartitionName ,Arrays.asList(i_ID) ,null);
    }
    
    
    
    /**
     * 删除多行数据（全表删除）
     * 
     * 注1：标量过滤规则：https://milvus.io/docs/zh/boolean.md
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @return                 返回操作数据的数量，异常时返回-1。
     */
    public long deletes(String i_TableName)
    {
        if ( Help.isNull(i_TableName) )
        {
            return -1;
        }
        
        String v_PK = this.queryPrimaryKey(i_TableName);
        if ( Help.isNull(v_PK) )
        {
            // 主键是必须存在的
            return -1;
        }
        return this.deletes(i_TableName ,null ,v_PK + " IS NOT NULL");
    }
    
    
    
    /**
     * 删除多行数据（标量过滤条件删除）
     * 
     * 注1：标量过滤规则：https://milvus.io/docs/zh/boolean.md
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_Filter         通过筛选条件删除，仅对标题字段有效。可选项
     * @return                 返回操作数据的数量，异常时返回-1。
     */
    public long deletes(String i_TableName ,String i_Filter)
    {
        return this.deletes(i_TableName ,null ,i_Filter);
    }
    
    
    
    /**
     * 删除多行数据（主键删除）
     * 
     * 注1：标量过滤规则：https://milvus.io/docs/zh/boolean.md
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_IDs            主键数据。可选项
     * @return                 返回操作数据的数量，异常时返回-1。
     */
    public long deletes(String i_TableName ,List<Object> i_IDs)
    {
        return this.deletes(i_TableName ,null ,i_IDs);
    }
    
    
    
    /**
     * 删除多行数据（标量过滤条件删除）
     * 
     * 注1：标量过滤规则：https://milvus.io/docs/zh/boolean.md
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_Filter         通过筛选条件删除，仅对标题字段有效。可选项
     * @return                 返回操作数据的数量，异常时返回-1。
     */
    public long deletes(String i_TableName ,String i_PartitionName ,String i_Filter)
    {
        if ( Help.isNull(i_TableName) )
        {
            return -1;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.delete_Core(v_Names[0] ,v_Names[1] ,i_PartitionName ,null ,i_Filter);
    }
    
    
    
    /**
     * 删除多行数据（主键删除）
     * 
     * 注1：标量过滤规则：https://milvus.io/docs/zh/boolean.md
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_IDs            主键数据。可选项
     * @return                 返回操作数据的数量，异常时返回-1。
     */
    public long deletes(String i_TableName ,String i_PartitionName ,List<Object> i_IDs)
    {
        if ( Help.isNull(i_TableName) )
        {
            return -1;
        }
        
        String [] v_Names = MilvusHelp.parserDBTableName(i_TableName);
        return this.delete_Core(v_Names[0] ,v_Names[1] ,i_PartitionName ,i_IDs ,null);
    }
    
    
    
    /**
     * 删除多行数据（主键删除）
     * 
     * 注1：标量过滤规则：https://milvus.io/docs/zh/boolean.md
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_IDs            主键数据。可选项
     * @param i_Filter         通过筛选条件删除，仅对标题字段有效。可选项
     * @return                 返回操作数据的数量，异常时返回-1。
     */
    private long delete_Core(String i_DBName ,String i_TableName ,String i_PartitionName ,List<Object> i_IDs ,String i_Filter)
    {
        DeleteReqBuilder v_ReqBuilder = DeleteReq.builder().collectionName(i_TableName);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_ReqBuilder.databaseName(i_DBName);
        }
        if ( !Help.isNull(i_PartitionName) )
        {
            v_ReqBuilder.partitionName(i_PartitionName);
        }
        if ( !Help.isNull(i_IDs) )
        {
            v_ReqBuilder.ids(i_IDs);
        }
        if ( !Help.isNull(i_Filter) )
        {
            v_ReqBuilder.filter(i_Filter);
        }
        
        DeleteReq  v_DeleteReq  = v_ReqBuilder.build();
        DeleteResp v_DeleteResp = this.milvus.delete(v_DeleteReq);
        if ( v_DeleteResp != null )
        {
            return v_DeleteResp.getDeleteCnt();
        }
        else
        {
            return -1;
        }
    }
    
    
    
    /**
     * 按ID查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-04
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_IDs            主键数据。同时支持多个ID的查询，即 List<Object> 类型的多个 ID 值
     * @return                 查询结果按 List<List>> 结构返回
     */
    public MilvusData queryByID(String i_TableName ,Object i_ID)
    {
        return queryByID(i_TableName ,i_ID ,null);
    }
    
    
    
    /**
     * 按ID查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-04
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_ID             主键数据。同时支持多个ID的查询，即 List<Object> 类型的多个 ID 值
     * @param i_MilvusResult   查询结果的构建规则
     * @return
     */
    public MilvusData queryByID(String i_TableName ,Object i_ID ,MilvusResult i_MilvusResult)
    {
        return queryByID(i_TableName ,null ,i_ID ,i_MilvusResult);
    }
    
    
    
    /**
     * 按ID查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-04
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_ID             主键数据。同时支持多个ID的查询，即 List<Object> 类型的多个 ID 值
     * @param i_MilvusResult   查询结果的构建规则
     * @return
     */
    @SuppressWarnings("unchecked")
    public MilvusData queryByID(String i_TableName ,String i_PartitionName ,Object i_ID ,MilvusResult i_MilvusResult)
    {
        if ( i_ID == null )
        {
            return null;
        }
        
        if ( MethodReflect.isExtendImplement(i_ID ,List.class) )
        {
            return queryByIDs(i_TableName ,i_PartitionName ,(List<Object>) i_ID ,i_MilvusResult);
        }
        else if ( MethodReflect.isExtendImplement(i_ID ,Map.class) )
        {
            Map<Object ,Object> v_MapIDs = (Map<Object ,Object>) i_ID;
            List<Object>        v_IDs    = Help.toListKeys(v_MapIDs);
            try
            {
                return queryByIDs(i_TableName ,i_PartitionName ,v_IDs ,i_MilvusResult);
            }
            finally
            {
                if ( v_IDs != null )
                {
                    v_IDs.clear();
                    v_IDs = null;
                }
            }
        }
        else if ( MethodReflect.isExtendImplement(i_ID ,Set.class) )
        {
            List<Object> v_IDs = Help.toList((Set<Object>) i_ID);
            try
            {
                return queryByIDs(i_TableName ,i_PartitionName ,v_IDs ,i_MilvusResult);
            }
            finally
            {
                if ( v_IDs != null )
                {
                    v_IDs.clear();
                    v_IDs = null;
                }
            }
        }
        else if ( i_ID.getClass().isArray() )
        {
            List<Object> v_IDs = Help.toListByArray(i_ID);
            try
            {
                return queryByIDs(i_TableName ,i_PartitionName ,v_IDs ,i_MilvusResult);
            }
            finally
            {
                if ( v_IDs != null )
                {
                    v_IDs.clear();
                    v_IDs = null;
                }
            }
        }
        else
        {
            return queryByIDs(i_TableName ,i_PartitionName ,Arrays.asList(i_ID) ,i_MilvusResult);
        }
    }
    
    
    
    /**
     * 按多个ID查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-04
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_IDs            主键数据。
     * @return                 查询结果按 List<List>> 结构返回
     */
    public MilvusData queryByIDs(String i_TableName ,List<Object> i_IDs)
    {
        return queryByIDs(i_TableName ,i_IDs ,null);
    }
    
    
    
    /**
     * 按多个ID查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-04
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_IDs            主键数据。
     * @param i_MilvusResult   查询结果的构建规则
     * @return
     */
    public MilvusData queryByIDs(String i_TableName ,List<Object> i_IDs ,MilvusResult i_MilvusResult)
    {
        return queryByIDs(i_TableName ,i_IDs ,i_MilvusResult);
    }
    
    
    
    /**
     * 按ID查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-04
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_IDs            主键数据。
     * @param i_MilvusResult   查询结果的构建规则
     * @return
     */
    public MilvusData queryByIDs(String i_TableName ,String i_PartitionName ,List<Object> i_IDs ,MilvusResult i_MilvusResult)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        if ( Help.isNull(i_IDs) )
        {
            return null;
        }
        
        String []    v_Names        = MilvusHelp.parserDBTableName(i_TableName);
        MilvusResult v_MilvusResult = i_MilvusResult;
        if ( v_MilvusResult == null )
        {
            v_MilvusResult = new MilvusResult();
        }
        return this.queryByID_Core(v_Names[0] ,v_Names[1] ,i_PartitionName ,i_IDs ,v_MilvusResult);
    }
    
    
    
    /**
     * 按多个ID查询
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-02
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_IDs            主键数据。
     * @param i_MilvusResult   查询结果的构建规则
     * @return
     */
    private MilvusData queryByID_Core(String i_DBName ,String i_TableName ,String i_PartitionName ,List<Object> i_IDs ,MilvusResult i_MilvusResult)
    {
        GetReqBuilder v_ReqBuilder = GetReq.builder().collectionName(i_TableName).ids(i_IDs);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_ReqBuilder.databaseName(i_DBName);
        }
        if ( !Help.isNull(i_PartitionName) )
        {
            v_ReqBuilder.partitionName(i_PartitionName);
        }
        
        GetReq          v_GetReq    = v_ReqBuilder.build();
        GetResp         v_GetResp   = this.milvus.get(v_GetReq);
        MilvusResultSet v_ResultSet = new MilvusResultSet(Help.NVL(i_DBName ,this.milvus.currentUsedDatabase())
                                                        ,i_TableName
                                                        ,this.querySchema(i_DBName ,i_TableName)
                                                        ,v_GetResp.getGetResults());
        return i_MilvusResult.getDatas(v_ResultSet);
    }
    
    
    
    /**
     * 标量过滤查询（全表查询）
     * 
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-04
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @return
     */
    public MilvusData query(String i_TableName)
    {
        return this.query(i_TableName ,null ,null ,null);
    }
    
    
    
    /**
     * 标量过滤查询（全表查询）
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-04
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_MilvusResult   查询结果的构建规则
     * @return
     */
    public MilvusData query(String i_TableName ,MilvusResult i_MilvusResult)
    {
        return this.query(i_TableName ,null ,null ,i_MilvusResult);
    }
    
    
    
    /**
     * 标量过滤查询
     * 
     * 标量过滤规则： https://milvus.io/docs/zh/boolean.md
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-04
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_Where          标量过滤规则
     * @param i_MilvusResult   查询结果的构建规则
     * @return
     */
    public MilvusData query(String i_TableName ,String i_Where ,MilvusResult i_MilvusResult)
    {
        return this.query(i_TableName ,null ,i_Where ,i_MilvusResult);
    }
    
    
    
    /**
     * 标量过滤查询
     * 
     * 标量过滤规则： https://milvus.io/docs/zh/boolean.md
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-04
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_Where          标量过滤规则
     * @param i_MilvusResult   查询结果的构建规则
     * @return
     */
    public MilvusData query(String i_TableName ,String i_PartitionName ,String i_Where ,MilvusResult i_MilvusResult)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        String v_Where = i_Where;
        if ( Help.isNull(v_Where) )
        {
            // 查全表数据
            String v_PK = this.queryPrimaryKey(i_TableName);
            if ( Help.isNull(v_PK) )
            {
                // 主键是必须存在的
                return null;
            }
            v_Where = v_PK + " IS NOT NULL";
        }
        
        String []    v_Names        = MilvusHelp.parserDBTableName(i_TableName);
        MilvusResult v_MilvusResult = i_MilvusResult;
        if ( v_MilvusResult == null )
        {
            v_MilvusResult = new MilvusResult();
        }
        return this.query_Core(v_Names[0] ,v_Names[1] ,i_PartitionName ,v_Where ,v_MilvusResult);
    }
    
    
    
    /**
     * 标量过滤查询
     * 
     * 标量过滤规则： https://milvus.io/docs/zh/boolean.md
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-04
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_Where          标量过滤规则
     * @param i_MilvusResult   查询结果的构建规则
     * @return
     */
    private MilvusData query_Core(String i_DBName ,String i_TableName ,String i_PartitionName ,String i_Where ,MilvusResult i_MilvusResult)
    {
        QueryReqBuilder v_ReqBuilder = QueryReq.builder().collectionName(i_TableName).filter(i_Where);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_ReqBuilder.databaseName(i_DBName);
        }
        if ( !Help.isNull(i_PartitionName) )
        {
            v_ReqBuilder.partitionNames(Collections.singletonList(i_PartitionName));
        }
        
        QueryReq        v_QueryReq  = v_ReqBuilder.build();
        QueryResp       v_QueryResp = this.milvus.query(v_QueryReq);
        MilvusResultSet v_ResultSet = new MilvusResultSet(Help.NVL(i_DBName ,this.milvus.currentUsedDatabase())
                                                         ,i_TableName
                                                         ,this.querySchema(i_DBName ,i_TableName)
                                                         ,v_QueryResp.getQueryResults());
        return i_MilvusResult.getDatas(v_ResultSet);
    }
    
    
    
    /**
     * 单向量字段搜索（近似最近邻ANN）（仅限Schema中只有一个向量字段时）
     * 
     * 大部分 RAG 基础检索，不可代替 queryVectors() 的多向量搜索
     * 
     * 注：当 i_TopK=10 、i_VectorDatas.size=1时，理想状态下返回 10 * 1 = 10  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=2时，理想状态下返回 10 * 2 = 20  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=x时，理想状态下返回 10 * x = 10x 行数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_VectorDatas    向量值
     * @return
     */
    public MilvusData queryVector(String i_TableName ,List<BaseVector> i_VectorDatas )
    {
        return this.queryVector(i_TableName ,null ,null ,i_VectorDatas ,null ,null ,null);
    }
    
    
    
    /**
     * 单向量字段搜索（近似最近邻ANN）
     * 
     * 大部分 RAG 基础检索，不可代替 queryVectors() 的多向量搜索
     * 
     * 注：当 i_TopK=10 、i_VectorDatas.size=1时，理想状态下返回 10 * 1 = 10  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=2时，理想状态下返回 10 * 2 = 20  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=x时，理想状态下返回 10 * x = 10x 行数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_ANNFiledName   向量字段名称
     * @param i_VectorDatas    向量值
     * @return
     */
    public MilvusData queryVector(String i_TableName ,String i_ANNFiledName ,List<BaseVector> i_VectorDatas )
    {
        return this.queryVector(i_TableName ,null ,i_ANNFiledName ,i_VectorDatas ,null ,null ,null);
    }
    
    
    
    /**
     * 单向量字段搜索（近似最近邻ANN）
     * 
     * 大部分 RAG 基础检索，不可代替 queryVectors() 的多向量搜索
     * 
     * 注：当 i_TopK=10 、i_VectorDatas.size=1时，理想状态下返回 10 * 1 = 10  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=2时，理想状态下返回 10 * 2 = 20  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=x时，理想状态下返回 10 * x = 10x 行数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_ANNFiledName   向量字段名称
     * @param i_VectorDatas    向量值
     * @param i_MilvusResult   搜索结果的构建规则
     * @return
     */
    public MilvusData queryVector(String i_TableName ,String i_ANNFiledName ,List<BaseVector> i_VectorDatas ,MilvusResult i_MilvusResult)
    {
        return this.queryVector(i_TableName ,null ,i_ANNFiledName ,i_VectorDatas ,null ,null ,i_MilvusResult);
    }
    
    
    
    /**
     * 单向量字段搜索（近似最近邻ANN）
     * 
     * 大部分 RAG 基础检索，不可代替 queryVectors() 的多向量搜索
     * 
     * 注：当 i_TopK=10 、i_VectorDatas.size=1时，理想状态下返回 10 * 1 = 10  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=2时，理想状态下返回 10 * 2 = 20  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=x时，理想状态下返回 10 * x = 10x 行数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_ANNFiledName   向量字段名称
     * @param i_VectorDatas    向量值
     * @param i_TopK           搜索结果的数量。为空时默认为10个
     * @param i_MilvusResult   搜索结果的构建规则
     * @return
     */
    public MilvusData queryVector(String i_TableName ,String i_ANNFiledName ,List<BaseVector> i_VectorDatas ,Integer i_TopK ,MilvusResult i_MilvusResult)
    {
        return this.queryVector(i_TableName ,null ,i_ANNFiledName ,i_VectorDatas ,i_TopK ,null ,i_MilvusResult);
    }
    
    
    
    /**
     * 单向量字段搜索（近似最近邻ANN）
     * 
     * 大部分 RAG 基础检索，不可代替 queryVectors() 的多向量搜索
     * 
     * 注：当 i_TopK=10 、i_VectorDatas.size=1时，理想状态下返回 10 * 1 = 10  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=2时，理想状态下返回 10 * 2 = 20  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=x时，理想状态下返回 10 * x = 10x 行数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_ANNFiledName   向量字段名称
     * @param i_VectorDatas    向量值
     * @param i_TopK           搜索结果的数量。为空时默认为10个
     * @param i_SelectFields   搜索结果的输出字段名称。为空时默认输出所有字段
     * @param i_MilvusResult   搜索结果的构建规则
     * @return
     */
    public MilvusData queryVector(String i_TableName ,String i_ANNFiledName ,List<BaseVector> i_VectorDatas ,Integer i_TopK ,List<String> i_SelectFields ,MilvusResult i_MilvusResult)
    {
        return this.queryVector(i_TableName ,null ,i_ANNFiledName ,i_VectorDatas ,i_TopK ,i_SelectFields ,i_MilvusResult);
    }
    
    
    
    /**
     * 单向量字段搜索（近似最近邻ANN）
     * 
     * 大部分 RAG 基础检索，不可代替 queryVectors() 的多向量搜索
     * 
     * 注：当 i_TopK=10 、i_VectorDatas.size=1时，理想状态下返回 10 * 1 = 10  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=2时，理想状态下返回 10 * 2 = 20  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=x时，理想状态下返回 10 * x = 10x 行数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_ANNFiledName   向量字段名称
     * @param i_VectorDatas    向量值
     * @param i_TopK           搜索结果的数量。为空时默认为10个
     * @param i_SelectFields   搜索结果的输出字段名称。为空时默认输出所有字段
     * @param i_MilvusResult   搜索结果的构建规则
     * @return
     */
    public MilvusData queryVector(String i_TableName ,String i_PartitionName ,String i_ANNFiledName ,List<BaseVector> i_VectorDatas ,Integer i_TopK ,List<String> i_SelectFields ,MilvusResult i_MilvusResult)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        String []    v_Names        = MilvusHelp.parserDBTableName(i_TableName);
        MilvusResult v_MilvusResult = i_MilvusResult;
        if ( v_MilvusResult == null )
        {
            v_MilvusResult = new MilvusResult();
        }
        return this.queryVector_Core(v_Names[0] ,v_Names[1] ,i_PartitionName ,i_ANNFiledName ,i_VectorDatas ,i_TopK ,i_SelectFields ,v_MilvusResult);
    }
    
    
    
    /**
     * 单向量字段搜索（近似最近邻ANN）
     * 
     * 大部分 RAG 基础检索，不可代替 queryVectors() 的多向量搜索
     * 
     * 注：当 i_TopK=10 、i_VectorDatas.size=1时，理想状态下返回 10 * 1 = 10  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=2时，理想状态下返回 10 * 2 = 20  行数据
     *    当 i_TopK=10 、i_VectorDatas.size=x时，理想状态下返回 10 * x = 10x 行数据
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_ANNFiledName   向量字段名称
     * @param i_VectorDatas    向量值
     * @param i_TopK           搜索结果的数量。为空时默认为10个
     * @param i_SelectFields   搜索结果的输出字段名称。为空时默认输出所有字段
     * @param i_MilvusResult   搜索结果的构建规则
     * @return
     */
    private MilvusData queryVector_Core(String i_DBName ,String i_TableName ,String i_PartitionName ,String i_ANNFiledName ,List<BaseVector> i_VectorDatas ,Integer i_TopK ,List<String> i_SelectFields ,MilvusResult i_MilvusResult)
    {
        SearchReqBuilder v_ReqBuilder = SearchReq.builder().collectionName(i_TableName).data(i_VectorDatas);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_ReqBuilder.databaseName(i_DBName);
        }
        if ( !Help.isNull(i_PartitionName) )
        {
            v_ReqBuilder.partitionNames(Collections.singletonList(i_PartitionName));
        }
        if ( !Help.isNull(i_ANNFiledName) )
        {
            v_ReqBuilder.annsField(i_ANNFiledName);
        }
        if ( i_TopK == null )
        {
            v_ReqBuilder.limit(10L);
        }
        else if ( i_TopK <= 0 )
        {
            v_ReqBuilder.limit(10L);
        }
        else
        {
            v_ReqBuilder.limit(i_TopK);
        }
        
        List<CreateCollectionReq.FieldSchema> v_FieldSchemas = this.querySchema(i_DBName ,i_TableName);
        List<String>                          v_SelectFields = i_SelectFields;
        if ( Help.isNull(v_SelectFields) )
        {
            v_SelectFields = MilvusHelp.queryFields(v_FieldSchemas);
        }
        if ( !Help.isNull(v_SelectFields) )
        {
            v_ReqBuilder.outputFields(v_SelectFields);
        }
        
        SearchReq       v_SearchReq  = v_ReqBuilder.build();
        SearchResp      v_SearchResp = this.milvus.search(v_SearchReq);
        MilvusResultSet v_ResultSet  = new MilvusResultSet(Help.NVL(i_DBName ,this.milvus.currentUsedDatabase())
                                                          ,i_TableName
                                                          ,v_FieldSchemas
                                                          ,true
                                                          ,v_SearchResp.getSearchResults());
        return i_MilvusResult.getDatas(v_ResultSet);
    }
    
    
    
    /**
     * 多个向量字段的混合搜索（近似最近邻ANN）
     * 
     * 不可代替 queryVector() 的单向量搜索
     * 
     * 原理：先每个搜索出 i_TopK 个，再综合过滤出 i_TopK 个返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_ANNNameVectors 向量字段名称与搜索的向量值。Map.key为向量名称，Map.value为搜索向量值
     * @return
     */
    public MilvusData queryVectors(String i_TableName ,Map<String ,List<BaseVector>> i_ANNNameVectors)
    {
        return this.queryVectors(i_TableName ,null ,i_ANNNameVectors ,null ,null ,null);
    }
    
    
    
    /**
     * 多个向量字段的混合搜索（近似最近邻ANN）
     * 
     * 不可代替 queryVector() 的单向量搜索
     * 
     * 原理：先每个搜索出 i_TopK 个，再综合过滤出 i_TopK 个返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_ANNNameVectors 向量字段名称与搜索的向量值。Map.key为向量名称，Map.value为搜索向量值
     * @param i_MilvusResult   搜索结果的构建规则
     * @return
     */
    public MilvusData queryVectors(String i_TableName ,Map<String ,List<BaseVector>> i_ANNNameVectors ,MilvusResult i_MilvusResult)
    {
        return this.queryVectors(i_TableName ,null ,i_ANNNameVectors ,null ,null ,i_MilvusResult);
    }
    
    
    
    /**
     * 多个向量字段的混合搜索（近似最近邻ANN）
     * 
     * 不可代替 queryVector() 的单向量搜索
     * 
     * 原理：先每个搜索出 i_TopK 个，再综合过滤出 i_TopK 个返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_ANNNameVectors 向量字段名称与搜索的向量值。Map.key为向量名称，Map.value为搜索向量值
     * @param i_TopK           搜索结果的数量。为空时默认为10个
     * @param i_MilvusResult   搜索结果的构建规则
     * @return
     */
    public MilvusData queryVectors(String i_TableName ,Map<String ,List<BaseVector>> i_ANNNameVectors ,Integer i_TopK ,MilvusResult i_MilvusResult)
    {
        return this.queryVectors(i_TableName ,null ,i_ANNNameVectors ,i_TopK ,null ,i_MilvusResult);
    }
    
    
    
    /**
     * 多个向量字段的混合搜索（近似最近邻ANN）
     * 
     * 不可代替 queryVector() 的单向量搜索
     * 
     * 原理：先每个搜索出 i_TopK 个，再综合过滤出 i_TopK 个返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_ANNNameVectors 向量字段名称与搜索的向量值。Map.key为向量名称，Map.value为搜索向量值
     * @param i_TopK           搜索结果的数量。为空时默认为10个
     * @param i_SelectFields   搜索结果的输出字段名称。为空时默认输出所有字段
     * @param i_MilvusResult   搜索结果的构建规则
     * @return
     */
    public MilvusData queryVectors(String i_TableName ,Map<String ,List<BaseVector>> i_ANNNameVectors ,Integer i_TopK ,List<String> i_SelectFields ,MilvusResult i_MilvusResult)
    {
        return this.queryVectors(i_TableName ,null ,i_ANNNameVectors ,i_TopK ,i_SelectFields ,i_MilvusResult);
    }
    
    
    
    /**
     * 多个向量字段的混合搜索（近似最近邻ANN）
     * 
     * 不可代替 queryVector() 的单向量搜索
     * 
     * 原理：先每个搜索出 i_TopK 个，再综合过滤出 i_TopK 个返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称。同时支持：库名称.表名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_ANNNameVectors 向量字段名称与搜索的向量值。Map.key为向量名称，Map.value为搜索向量值
     * @param i_TopK           搜索结果的数量。为空时默认为10个
     * @param i_SelectFields   搜索结果的输出字段名称。为空时默认输出所有字段
     * @param i_MilvusResult   搜索结果的构建规则
     * @return
     */
    public MilvusData queryVectors(String i_TableName ,String i_PartitionName ,Map<String ,List<BaseVector>> i_ANNNameVectors ,Integer i_TopK ,List<String> i_SelectFields ,MilvusResult i_MilvusResult)
    {
        if ( Help.isNull(i_TableName) )
        {
            return null;
        }
        
        if ( Help.isNull(i_ANNNameVectors) )
        {
            return null;
        }
        
        String []    v_Names        = MilvusHelp.parserDBTableName(i_TableName);
        MilvusResult v_MilvusResult = i_MilvusResult;
        if ( v_MilvusResult == null )
        {
            v_MilvusResult = new MilvusResult();
        }
        return this.queryVectors_Core(v_Names[0] ,v_Names[1] ,i_PartitionName ,i_ANNNameVectors ,i_TopK ,i_SelectFields ,v_MilvusResult);
    }
    
    
    
    /**
     * 多个向量字段的混合搜索（近似最近邻ANN）
     * 
     * 不可代替 queryVector() 的单向量搜索
     * 
     * 原理：先每个搜索出 i_TopK 个，再综合过滤出 i_TopK 个返回
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-06
     * @version     v1.0
     *
     * @param i_DBName         库名称。当milvus客户端中配置有库名称时，此值可免填写。
     * @param i_TableName      表名称，必填项。即：向量库中Collection的名称
     * @param i_PartitionName  分区名称。可选项
     * @param i_ANNNameVectors 向量字段名称与搜索的向量值。Map.key为向量名称，Map.value为搜索向量值
     * @param i_TopK           搜索结果的数量。为空时默认为10个
     * @param i_SelectFields   搜索结果的输出字段名称。为空时默认输出所有字段
     * @param i_MilvusResult   搜索结果的构建规则
     * @return
     */
    private MilvusData queryVectors_Core(String i_DBName ,String i_TableName ,String i_PartitionName ,Map<String ,List<BaseVector>> i_ANNNameVectors ,Integer i_TopK ,List<String> i_SelectFields ,MilvusResult i_MilvusResult)
    {
        HybridSearchReqBuilder v_ReqBuilder = HybridSearchReq.builder().collectionName(i_TableName);
        
        if ( !Help.isNull(i_DBName) )
        {
            v_ReqBuilder.databaseName(i_DBName);
        }
        if ( !Help.isNull(i_PartitionName) )
        {
            v_ReqBuilder.partitionNames(Collections.singletonList(i_PartitionName));
        }
        
        Integer v_TopK = i_TopK;
        if ( v_TopK == null || i_TopK <= 0)
        {
            v_TopK = 10;
        }
        v_ReqBuilder.limit(v_TopK);
        
        if ( !Help.isNull(i_ANNNameVectors) )
        {
            List<AnnSearchReq> v_SearchANNNameVectors = new ArrayList<AnnSearchReq>();
            for (Map.Entry<String ,List<BaseVector>> v_ANNNameVector : i_ANNNameVectors.entrySet())
            {
                v_SearchANNNameVectors.add(AnnSearchReq.builder()
                                                       .vectorFieldName(v_ANNNameVector.getKey())
                                                       .vectors(v_ANNNameVector.getValue())
                                                       .limit(v_TopK)
                                                       .build());
            }
            v_ReqBuilder.searchRequests(v_SearchANNNameVectors);
        }
        
        List<CreateCollectionReq.FieldSchema> v_FieldSchemas = this.querySchema(i_DBName ,i_TableName);
        List<String>                          v_SelectFields = i_SelectFields;
        if ( Help.isNull(v_SelectFields) )
        {
            v_SelectFields = MilvusHelp.queryFields(v_FieldSchemas);
        }
        if ( !Help.isNull(v_SelectFields) )
        {
            v_ReqBuilder.outFields(v_SelectFields);
        }
        
        HybridSearchReq v_HybridReq  = v_ReqBuilder.build();
        SearchResp      v_HybridResp = this.milvus.hybridSearch(v_HybridReq);
        MilvusResultSet v_ResultSet  = new MilvusResultSet(Help.NVL(i_DBName ,this.milvus.currentUsedDatabase())
                                                          ,i_TableName
                                                          ,v_FieldSchemas
                                                          ,true
                                                          ,v_HybridResp.getSearchResults());
        return i_MilvusResult.getDatas(v_ResultSet);
    }
    
    
    
    /**
     * 解释表名称。支持两种形式
     *   
     *   形式1：库名称.表名称
     *   形式2：表名称
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-31
     * @version     v1.0
     *
     * @param i_TableName  表名称，必填项。即：向量库中Collection的名称
     * @return             返回0号元素为：库名称，当库名称不存在时，返回 ""
     *                     返回1号元素为：表名称
     */
    private static String [] parserDBTableName(String i_TableName)
    {
        String [] v_Names = StringHelp.split(i_TableName ,".");
        
        return v_Names.length >= 2 ? v_Names : new String [] {"" ,v_Names[0]};
    }
    
    
    
    /**
     * 数据转为List<JsonObject>集合
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-07-30
     * @version     v1.0
     *
     * @param i_Data      数据
     * @param i_IsSingle  当数据是JsonString时，true是按单行数据处理，false是按多行数据处理
     * @return
     */
    public static List<JsonObject> toJsonObject(Object i_Data ,boolean i_IsSingle)
    {
        if ( Help.isNull(i_Data) )
        {
            return null;
        }
        
        Gson             v_Gson = new Gson();
        List<JsonObject> v_List = null;
        if ( i_Data instanceof String )
        {
            String v_JsonString = i_Data.toString();
            if ( i_IsSingle )
            {
                JsonObject v_Data = v_Gson.fromJson(v_JsonString ,JsonObject.class);
                v_List = Arrays.asList(v_Data);
            }
            else
            {
                Type v_ListType = new TypeToken<List<JsonObject>>(){}.getType();
                v_List = v_Gson.fromJson(v_JsonString, v_ListType);
            }
        }
        else if ( MethodReflect.isExtendImplement(i_Data ,List.class) )
        {
            List<?> v_Datas = (List<?>) i_Data;
            v_List = v_Datas.stream()
                            .map(v_DataItem -> v_Gson.toJsonTree(v_DataItem).getAsJsonObject())
                            .collect(Collectors.toList());
        }
        else if ( MethodReflect.isExtendImplement(i_Data ,Set.class) )
        {
            Set<?> v_Datas = (Set<?>) i_Data;
            v_List = v_Datas.stream()
                            .map(v_DataItem -> v_Gson.toJsonTree(v_DataItem).getAsJsonObject())
                            .collect(Collectors.toList());
        }
        else if ( MethodReflect.isExtendImplement(i_Data ,Map.class) )
        {
            Map<? ,?> v_Datas = (Map<? ,?>) i_Data;
            v_List = v_Datas.values().stream()
                            .map(v_DataItem -> v_Gson.toJsonTree(v_DataItem).getAsJsonObject())
                            .collect(Collectors.toList());
        }
        else if ( i_Data.getClass().isArray() )
        {
            JsonArray v_JsonArray = v_Gson.toJsonTree(i_Data).getAsJsonArray();
            v_List = v_JsonArray.asList().stream()
                                .filter(JsonElement::isJsonObject)
                                .map(JsonElement::getAsJsonObject)
                                .collect(Collectors.toList());
        }
        else 
        {
            JsonObject v_Data = v_Gson.toJsonTree(i_Data).getAsJsonObject();
            v_List = Arrays.asList(v_Data);
        }
        
        return v_List;
    }
    
    
    
    /**
     * 获取：数据库客户端
     */
    public MilvusClientV2 getMilvus()
    {
        return milvus;
    }


    
    /**
     * 设置：数据库客户端
     * 
     * @param i_Milvus 数据库客户端
     */
    public void setMilvus(MilvusClientV2 i_Milvus)
    {
        this.milvus = i_Milvus;
    }



    /**
     * 设置XJava池中对象的ID标识。此方法不用用户调用设置值，是自动的。
     * 
     * @param i_XJavaID
     */
    public void setXJavaID(String i_XJavaID)
    {
        this.xid = i_XJavaID;
    }
    
    
    
    /**
     * 获取XJava池中对象的ID标识。
     * 
     * @return
     */
    public String getXJavaID()
    {
        return this.xid;
    }
    
    
    
    /**
     * 注释。可用于日志的输出等帮助性的信息
     * 
     * @param i_Comment
     */
    public void setComment(String i_Comment)
    {
        this.comment = i_Comment;
    }
    
    
    
    /**
     * 注释。可用于日志的输出等帮助性的信息
     *
     * @return
     */
    public String getComment()
    {
        return this.comment;
    }
    
}
