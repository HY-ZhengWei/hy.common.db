package org.hy.common.milvus.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.milvus.MilvusData;
import org.hy.common.milvus.MilvusHelp;
import org.hy.common.milvus.MilvusResult;
import org.hy.common.milvus.schema.Collection;
import org.hy.common.xml.XJSON;
import org.junit.Test;

import com.google.gson.Gson;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;





/**
 * 测试单元：Milvus向量库
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-07-28
 * @version     v1.0
 */
public class JU_Milvus
{
    
    private MilvusHelp milvusHelp;
    
    
    
    public JU_Milvus()
    {
        ConnectConfig v_MilvusConfig = ConnectConfig.builder()
                                                    .uri("http://10.0.2.12:19530")
                                                    .username("root")
                                                    .password("lpssys@2026")
                                                    .dbName("Common")
                                                    .connectTimeoutMs(10 * 1000)
                                                    .build();
        this.milvusHelp = new MilvusHelp(new MilvusClientV2(v_MilvusConfig) ,60 * 1000L);
    }
    
    
    
    @Test
    public void test_isValid()
    {
        System.out.println(this.milvusHelp.isValid());
    }
    
    
    
    @Test
    public void test_createSchema()
    {
        XJSON  v_XJson    = new XJSON();
        String v_JsonText = """
                            {
                              "collection_name": "DemoFullText202608013",
                              "description": "测试",
                              "fields": [
                                {
                                  "name": "id",
                                  "data_type": 5,
                                  "description": "",
                                  "is_primary_key": true,
                                  "autoID": false,
                                  "is_partition_key": false,
                                  "nullable": false,
                                  "indexes": [
                                    {
                                      "index_name": "id",
                                      "index_type": "AUTOINDEX",
                                      "indexParameterPairs": [
                                        {
                                          "key": "mmap.enabled",
                                          "value": "false"
                                        },
                                        {
                                          "key": "index_type",
                                          "value": "AUTOINDEX"
                                        }
                                      ]
                                    }
                                  ]
                                },
                                {
                                  "name": "text",
                                  "data_type": 21,
                                  "description": "",
                                  "is_primary_key": false,
                                  "is_partition_key": false,
                                  "nullable": false,
                                  "max_length": 4000,
                                  "enable_analyzer": true,
                                  "analyzer_params": {
                                    "type": "standard"
                                  }
                                },
                                {
                                  "name": "sparseVector",
                                  "data_type": 104,
                                  "description": "",
                                  "is_primary_key": false,
                                  "is_partition_key": false,
                                  "nullable": false,
                                  "indexes": [
                                    {
                                      "index_name": "sparseVector",
                                      "index_type": "AUTOINDEX",
                                      "metric_type": "BM25",
                                      "indexParameterPairs": [
                                        {
                                          "key": "index_type",
                                          "value": "AUTOINDEX"
                                        },
                                        {
                                          "key": "metric_type",
                                          "value": "BM25"
                                        }
                                      ]
                                    }
                                  ]
                                }
                              ],
                              "functions": [
                                {
                                  "name": "textbm25_emb",
                                  "description": "",
                                  "type": 1,
                                  "input_field_names": [
                                    "text"
                                  ],
                                  "output_field_names": [
                                    "sparseVector"
                                  ],
                                  "params": []
                                }
                              ],
                              "consistency_level": "Bounded",
                              "shards_num": 1,
                              "enableDynamicField": false,
                              "properties": {
                                "timezone": "Asia/Shanghai"
                              }
                            }
                            """;
        
        // Gson v_Gson = new Gson();
        // v_Collection = v_Gson.fromJson(v_JsonText ,Collection.class);  它转换时报错
        
        Collection v_Collection = (Collection) v_XJson.toJava(v_JsonText ,Collection.class);
        this.milvusHelp.createCollection(v_Collection);
    }
    
    
    
    @Test
    public void test_querySchema()
    {
        // 查Schema中的字段信息
        Help.print(this.milvusHelp.querySchema("Demo"));
        
        // 查主键名称
        System.out.println(this.milvusHelp.queryPrimaryKey("Demo"));
    }
    
    
    
    @Test
    public void test_Insert()
    {
        // 按Java Bean写入一行数据（主键自动生成）
        JU_MilvusData v_Data = new JU_MilvusData();
        v_Data.setVector(Arrays.asList(0.3580376395471989F, -0.6023495712049978F));
        v_Data.setComment("注释");
        this.milvusHelp.insert("Demo" ,v_Data);
        
        // 按Json字符串写入一行数据
        Gson   v_Gson     = new Gson();
        String v_DataJson = v_Gson.toJson(v_Data);
        this.milvusHelp.insert("Demo" ,v_DataJson);
        
        // 按List集合写入多行数据
        List<JU_MilvusData> v_DataList = new ArrayList<JU_MilvusData>();
        v_DataList.add(v_Data);
        v_DataList.add(v_Data);
        this.milvusHelp.insert("Demo" ,v_DataList);
        
        // 按Set集合写入多行数据
        Set<JU_MilvusData> v_DataSet = new HashSet<JU_MilvusData>();
        v_DataSet.add(v_Data);
        v_DataSet.add(v_Data);
        this.milvusHelp.insert("Demo" ,v_DataSet);
        
        // 按Map集合写入多行数据
        Map<String ,JU_MilvusData> v_DataMap = new HashMap<String ,JU_MilvusData>();
        v_DataMap.put("1" ,v_Data);
        v_DataMap.put("2" ,v_Data);
        this.milvusHelp.insert("Demo" ,v_DataMap);
        
        // 按数组写入多行数据
        JU_MilvusData [] v_DataArray = new JU_MilvusData[2];
        v_DataArray[0] = v_Data;
        v_DataArray[1] = v_Data;
        this.milvusHelp.insert("Demo" ,v_DataArray);
        
        // 按Java Bean写入一行数据（自定义主键）
        v_Data.setId(Date.getNowTime().getFull_ID());
        this.milvusHelp.insert("Demo_NoID" ,v_Data);
    }
    
    
    
    @Test
    public void test_Insert_For()
    {
        for (int x=1; x<=10; x++)
        {
            JU_MilvusData v_Data = new JU_MilvusData();
            v_Data.setId(Date.getNowTime().getFullMilli_ID());
            v_Data.setVector(    Arrays.asList(Help.random(Integer.MIN_VALUE ,Integer.MAX_VALUE) / 10000F ,Help.random(Integer.MIN_VALUE ,Integer.MAX_VALUE) / 10000F));
            v_Data.setVectorElse(Arrays.asList(0.0F + x, 0.0F - x));
            v_Data.setComment("注释");
            this.milvusHelp.insert("Demo_NoAutoID_2Vector" ,v_Data);
        }
    }
    
    
    
    @Test
    public void test_Update()
    {
        // 主键对应的数据不存在：按Java Bean写入一行数据（主键自动生成）
        JU_MilvusData v_Data = new JU_MilvusData();
        v_Data.setId("123456");
        v_Data.setVector(Arrays.asList(0.3580376395471989F, -0.6023495712049978F));
        v_Data.setComment("注释");
        this.milvusHelp.upsert("Demo_NoID" ,v_Data);
        
        // 主键对应的数据已存在：按Java Bean更新一行数据
        v_Data.setVector(Arrays.asList(0.1F, -0.1F));
        v_Data.setComment("修改");
        this.milvusHelp.upsert("Demo_NoID" ,v_Data);
    }
    
    
    
    @Test
    public void test_Delete()
    {
        // 删除一行数据
        this.milvusHelp.delete("Demo" ,465153463178152668L);
        
        // 删除全表数据
        this.milvusHelp.deletes("Demo");
    }
    
    
    
    @Test
    public void test_QueryByID() throws ClassNotFoundException
    {
        MilvusResult v_MilvusResult = new MilvusResult();
        MilvusData   v_MilvusData   = null;
        
        // 默认List<List>显示查询结果
        v_MilvusData = this.milvusHelp.queryByID("Demo_NoID" ,123456L ,v_MilvusResult);
        System.out.println(v_MilvusData);
        
        
        // 转List<对象>显示查询结果
        v_MilvusResult.setRow("org.hy.common.milvus.junit.JU_MilvusData");
        v_MilvusResult.setCfill("setter(colValue)");
        v_MilvusResult.setCstyle("NORMAL");
        v_MilvusData = this.milvusHelp.queryByID("Demo_NoID" ,123456L ,v_MilvusResult);
        System.out.println(v_MilvusData);
        
        
        // 转Map<对象>显示查询结果
        v_MilvusResult.setTable("java.util.HashMap");
        v_MilvusResult.setFill("put(row.id ,row)");
        v_MilvusResult.setRow("org.hy.common.milvus.junit.JU_MilvusData");
        v_MilvusResult.setCfill("setter(colValue)");
        v_MilvusResult.setCstyle("NORMAL");
        v_MilvusData = this.milvusHelp.queryByID("Demo_NoID" ,123456L ,v_MilvusResult);
        System.out.println(v_MilvusData);
        
        
        // 转List<Map>显示查询结果
        v_MilvusResult.setRow("java.util.HashMap");
        v_MilvusResult.setCfill("put(colname ,colValue)");
        v_MilvusResult.setCstyle("NORMAL");
        v_MilvusData = this.milvusHelp.queryByID("Demo_NoID" ,123456L ,v_MilvusResult);
        System.out.println(v_MilvusData);
        
        
        // 转Map<Map>显示查询结果
        v_MilvusResult.setTable("java.util.HashMap");
        v_MilvusResult.setFill("put(row.id ,row)");
        v_MilvusResult.setRow("java.util.HashMap");
        v_MilvusResult.setCfill("put(colname ,colValue)");
        v_MilvusResult.setCstyle("NORMAL");
        v_MilvusData = this.milvusHelp.queryByID("Demo_NoID" ,123456L ,v_MilvusResult);
        System.out.println(v_MilvusData);
        
        
        // 查多个ID对应的记录
        v_MilvusData = this.milvusHelp.queryByID("Demo_NoID" ,Arrays.asList(123456L ,20260729164405L) ,v_MilvusResult);
        System.out.println(v_MilvusData);
    }
    
    
    
    @Test
    public void test_Query()
    {
        MilvusResult v_MilvusResult = new MilvusResult();
        MilvusData   v_MilvusData   = null;
        
        // 标量过滤查询
        v_MilvusData = this.milvusHelp.query("Demo_NoID" ,"comment == \"注释\"" ,v_MilvusResult);
        System.out.println(v_MilvusData);
        
        // 标量过滤查询
        v_MilvusData = this.milvusHelp.query("Demo_NoID" ,"id == 123456" ,v_MilvusResult);
        System.out.println(v_MilvusData);
        
        // 标量过滤查询
        v_MilvusData = this.milvusHelp.query("Demo_NoID" ,"id in [123456 ,20260730155529]" ,v_MilvusResult);
        System.out.println(v_MilvusData);
        
        // 标量过滤查询
        v_MilvusData = this.milvusHelp.query("Demo_NoID" ,"comment like \"注释\"" ,v_MilvusResult);
        System.out.println(v_MilvusData);
        
        // 全表查询
        v_MilvusData = this.milvusHelp.query("Demo_NoID" ,v_MilvusResult);
        System.out.println(v_MilvusData);
    }
    
    
    
    @Test
    public void test_QueryVector()
    {
        MilvusResult v_MilvusResult = new MilvusResult();
        MilvusData   v_MilvusData   = null;
        
        // 单个向量的神经网络搜索
        FloatVec         v_Vector  = new FloatVec(new float[]{5.0F ,-5.0F});
        List<BaseVector> v_Vectors = Collections.singletonList(v_Vector);
        v_MilvusData = this.milvusHelp.queryVector("Demo_NoAutoID_2Vector" ,"vectorElse" ,v_Vectors ,3 ,v_MilvusResult);
        System.out.println(v_MilvusData);
        
        // 单个向量的神经网络搜索
        v_Vectors    = Arrays.asList(v_Vector ,v_Vector);
        v_MilvusData = this.milvusHelp.queryVector("Demo_NoID" ,"vector" ,v_Vectors ,3 ,v_MilvusResult);
        System.out.println(v_MilvusData);
    }
    
}
