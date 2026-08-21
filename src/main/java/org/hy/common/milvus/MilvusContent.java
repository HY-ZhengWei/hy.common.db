package org.hy.common.milvus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.PartitionMap;
import org.hy.common.SplitSegment;
import org.hy.common.SplitSegment.InfoType;
import org.hy.common.StringHelp;
import org.hy.common.db.DBCondition;
import org.hy.common.db.DBConditions;
import org.hy.common.db.DBSQL;
import org.hy.common.db.DBSQL_Split;
import org.hy.common.xml.log.Logger;





/**
 * 向量库占位符Content的信息。
 * 
 * 主要对Content信息（我们叫它为:占位符Content）进行分析后，并根据Java的 "属性类(或叫值对应类)" 转换为真实能被执行的Content。
 * 
 * 原理参考：org.hy.common.db.DBSQL
 * 
 * @author      ZhengWei(HY)
 * @createDate  2016-08-19
 * @version     v1.0
 */
public class MilvusContent implements Serializable
{
    
    private static final long                serialVersionUID    = 3214513196916952276L;

    private static final Logger              $Logger             = new Logger(MilvusContent.class ,true);
    
    /** 占位符是什么字符 */
    public  static final String              $Placeholder        = DBSQL.$Placeholder;
    
    /** 匹配 <[ ... ]> 的字符串           无法包含< ( >三种特殊字符   "[ \\s]?<\\[[^(?!((<\\[)|(\\]>)))]+\\]>[ \\s]?" */
    private final static String              $SQL_Find_Dynamic   = "[ \\s]?<\\[((?!<\\[|\\]>).)*\\]>[ \\s]?";
    
    
    
    /** 数据库中的Default关键字 */
    private final static String              $Default            = "DEFAULT";
    
    /** 数据库中的NULL关键字 */
    private final static String              $NULL               = "NULL";
                                                                 
    private final static Map<String ,String> $ReplaceKeys        = new HashMap<String ,String>();
    
    
    
    static
    {
        $ReplaceKeys.put("\t"  ," ");
        $ReplaceKeys.put("\r"  ," ");
        $ReplaceKeys.put("\n"  ," ");
    }
    
    
    /** 占位符SQL */
    private String                    contentText;
    
    private MilvusContentFill         milvusContentFill;
    
    /** 通过分析后的分段SQL信息 */
    private List<DBSQL_Split>         segments;
    
    /** 不是占位符的关键字的排除过滤。区分大小字。前缀无须冒号 */
    private Set<String>               notPlaceholders;
    
    /** 占位符取值条件 */
    private Map<String ,DBConditions> conditions;
    
    /**
     * 是否默认为NULL值写入到数据库。针对所有占位符做的统一设置。
     * 
     * 当 this.defaultNull = true 时，任何类型的值为null对象时，均以NULL值写入到数据库。
     * 当 this.defaultNull = false 时，
     *      1. String 类型的值，按 "" 空字符串写入到数据库 或 拼接成SQL语句
     *      2. 其它类型的值，以NULL值写入到数据库。
     * 
     * 默认为：false。
     */
    private boolean                   defaultNull;
    
    /**
     * 是否取数据库中表字段的默认值，当数据库未定义时写NULL值。
     * 
     * 当 this.defaultDB = true 时，任何类型的值为null对象时，均以数据库中表字段的默认值写入到数据库。
     * 当 this.defaultDB = false 时，采用 this.defaultNull 的规则。
     * 
     * 即：defaultDB 优先级大于 defaultNull
     * 
     * 默认为：false。
     */
    private boolean                   defaultDB;
    
    
    
    /**
     * 构造器
     */
    public MilvusContent()
    {
        this.contentText      = "";
        this.segments         = new ArrayList<DBSQL_Split>();
        this.conditions       = new HashMap<String ,DBConditions>();
        this.defaultNull      = false;
        this.defaultDB        = false;
        this.setNotPlaceholders("MI,SS,mi,ss,mm");
    }
    
    
    
    /**
     * 构造器
     * 
     * @param i_ContentText  完整的原始Content文本
     */
    public MilvusContent(String i_ContentText)
    {
        this();
        this.setContentText(i_ContentText);
    }
    
    
    
    /**
     * 分析SQL（私有）
     * 
     * @return
     */
    private void parser()
    {
        if ( Help.isNull(this.contentText) )
        {
            return;
        }
        
        // 匹配 <[ ... ]> 的字符串
        List<SplitSegment> v_Segments = StringHelp.Split($SQL_Find_Dynamic ,this.contentText);
        for (SplitSegment v_SplitSegment : v_Segments)
        {
            DBSQL_Split v_DBSQL_Segment = new DBSQL_Split(v_SplitSegment);
            
            String v_Info = v_DBSQL_Segment.getInfo();
            v_Info = v_Info.replaceFirst("<\\[" ,"");
            v_Info = v_Info.replaceFirst("\\]>" ,"");
            
            v_DBSQL_Segment.setInfo(v_Info);
            v_DBSQL_Segment.parsePlaceholders();
            
            this.segments.add(v_DBSQL_Segment);
        }
    }
    
    
    
    /**
     * 填充或设置占位符Content
     * 
     * @param i_ContentText
     */
    public synchronized void setContentText(String i_ContentText)
    {
        this.contentText = StringHelp.replaceAll(Help.NVL(i_ContentText).trim() ,$ReplaceKeys);
        
        if ( this.segments == null )
        {
            this.segments = new ArrayList<DBSQL_Split>();
        }
        else
        {
            this.segments.clear();
        }
        
        this.parser();
    }
    
    
    
    /**
     * 获取占位符Content
     * 
     * @return
     */
    public synchronized String getContentText()
    {
        return contentText;
    }
    
    
    
    /**
     * 获取可执行的Content语句，并按 i_Obj 填充有数值。
     * 
     * 入参类型是Map时，在处理NULL与入参类型是Object，是不同的。
     *   1. Map填充为""空的字符串。
     *   2. Object填充为 "NULL" ，可以支持空值针的写入。
     * 
     *   但上方两种均可以通过配置<condition><name>占位符名称<name></condition>，向数据库写入空值针。
     * 
     * @param i_Obj
     * @param i_DSG  数据库连接池组。可为空或NULL
     * @return
     */
    @SuppressWarnings("unchecked")
    public String getContent(Object i_Obj)
    {
        if ( i_Obj == null )
        {
            return null;
        }
        
        if ( Help.isNull(this.segments) )
        {
            return this.contentText;
        }
        
        if ( i_Obj instanceof Map )
        {
            return this.getContent((Map<String ,?>)i_Obj);
        }
        
        StringBuilder         v_Content = new StringBuilder();
        Iterator<DBSQL_Split> v_Ierator = this.segments.iterator();
        
        
        while ( v_Ierator.hasNext() )
        {
            DBSQL_Split                   v_DBSQL_Segment = v_Ierator.next();
            PartitionMap<String ,Integer> v_Placeholders  = v_DBSQL_Segment.getPlaceholders();
            
            if ( Help.isNull(v_Placeholders) )
            {
                v_Content.append(v_DBSQL_Segment.getInfo());
            }
            else
            {
                Iterator<String> v_IterPlaceholders = v_Placeholders.keySet().iterator();
                String           v_Info             = v_DBSQL_Segment.getInfo();
                int              v_ReplaceCount     = 0;
                
                // 不再区分 $DBSQL_TYPE_INSERT 类型，使所有的SQL类型均采有相同的占位符填充逻辑。ZhengWei(HY) Edit 2018-06-06
                while ( v_IterPlaceholders.hasNext() )
                {
                    String        v_PlaceHolder   = v_IterPlaceholders.next();
                    MethodReflect v_MethodReflect = null;
                    
                    // 排除不是占位符的变量，但它的形式可能是占位符的形式。ZhengWei(HY) Add 2018-06-14
                    if ( this.notPlaceholders.contains(v_PlaceHolder) )
                    {
                        v_ReplaceCount++;
                        continue;
                    }
                    
                    /*
                    在实现全路径的解释功能之前的老方法  ZhengWei(HY) Del 2015-12-10
                    Method v_Method = MethodReflect.getGetMethod(i_Obj.getClass() ,v_PlaceHolder ,true);
                    */
                    
                    // 可实现xxx.yyy.www(或getXxx.getYyy.getWww)全路径的解释  ZhengWei(HY) Add 2015-12-10
                    try
                    {
                        v_MethodReflect = new MethodReflect(i_Obj ,v_PlaceHolder ,true ,MethodReflect.$NormType_Getter);
                    }
                    catch (Exception exce)
                    {
                        // 有些:xx占位符可能找到对应Java的Getter方法，所以忽略。 ZhengWei(HY) Add 2-16-09-29
                        // Nothing.
                    }
                    
                    Object       v_GetterValue    = null;
                    DBConditions v_ConditionGroup = null;
                    boolean      v_IsCValue       = false;
                    try
                    {
                        if ( v_MethodReflect != null )
                        {
                            v_ConditionGroup = Help.getValueIgnoreCase(this.conditions ,v_PlaceHolder);
                            if ( v_ConditionGroup != null )
                            {
                                // 占位符取值条件  ZhengWei(HY) Add 2026-08-19
                                v_GetterValue = v_ConditionGroup.getValue(i_Obj ,false);
                                v_IsCValue    = true;
                            }
                            else
                            {
                                v_GetterValue = v_MethodReflect.invoke();
                            }
                        }
                        else
                        {
                            // 全局占位符 ZhengWei(HY) Add 2019-03-06
                            v_GetterValue = Help.getValueIgnoreCase(MilvusContentStaticParams.getInstance() ,v_PlaceHolder);
                        }
                    }
                    catch (Exception exce)
                    {
                        $Logger.error(exce);
                        throw new RuntimeException(exce.getMessage());
                    }
                    
                    try
                    {
                        // getter 方法有返回值时
                        if ( v_GetterValue != null )
                        {
                            if ( MethodReflect.class.equals(v_GetterValue.getClass()) )
                            {
                                boolean v_IsReplace = false;
                                
                                // 这里循环的原因是：每次((MethodReflect)v_GetterValue).invoke()执行后的返回值v_MRValue都可能不一样。
                                while ( v_Info.indexOf($Placeholder + v_PlaceHolder) >= 0 )
                                {
                                    // 可实现SQL中的占位符，通过Java动态(或有业务时间逻辑的)填充值。 ZhengWei(HY) Add 2016-03-18
                                    Object v_MRValue = ((MethodReflect)v_GetterValue).invoke();
                                    
                                    if ( v_MRValue != null )
                                    {
                                        if ( !this.isSafeCheck() || MilvusContentSafe.isSafe(v_MRValue.toString()) )
                                        {
                                            if ( v_IsCValue )
                                            {
                                                v_Info = this.milvusContentFill.onlyFillFirst(v_Info ,v_PlaceHolder ,v_MRValue.toString());
                                            }
                                            else
                                            {
                                                v_Info = this.milvusContentFill.fillFirst(v_Info ,v_PlaceHolder ,v_MRValue.toString());
                                            }
                                            v_IsReplace = true;
                                        }
                                        else
                                        {
                                            throw new MilvusContentSafeException(this.getContentText());
                                        }
                                    }
                                    else
                                    {
                                        String v_Value = Help.toObject(((MethodReflect)v_GetterValue).getReturnType()).toString();
                                        v_Info = this.milvusContentFill.fillAll(v_Info ,v_PlaceHolder ,v_Value);
                                        v_IsReplace = false;  // 为了支持动态占位符，这里设置为false
                                        // 同时也替换占位符，可对不是动态占位符的情况，也初始化值。  ZhengWei(HY) 2018-06-06
                                        
                                        break;
                                    }
                                }
                                
                                if ( v_IsReplace )
                                {
                                    v_ReplaceCount++;
                                }
                            }
                            else
                            {
                                if ( !this.isSafeCheck() || MilvusContentSafe.isSafe(v_GetterValue.toString()) )
                                {
                                    if ( v_IsCValue )
                                    {
                                        v_Info = this.milvusContentFill.onlyFillAll(v_Info ,v_PlaceHolder ,v_GetterValue.toString());
                                    }
                                    else
                                    {
                                        v_Info = this.milvusContentFill.fillAll(v_Info ,v_PlaceHolder ,v_GetterValue.toString());
                                    }
                                    v_ReplaceCount++;
                                }
                                else
                                {
                                    throw new MilvusContentSafeException(this.getContentText());
                                }
                            }
                        }
                        // 当占位符对应属性值为NULL时的处理
                        else
                        {
                            String v_Value = null;
                            if ( this.defaultDB )
                            {
                                v_Value = $Default;
                                v_Info  = this.milvusContentFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value);
                            }
                            else if ( v_ConditionGroup != null || this.defaultNull )
                            {
                                // 占位符取值条件。可实现NULL值写入到数据库的功能  ZhengWei(HY) Add 2026-08-19
                                v_Value = $NULL;
                                v_Info  = this.milvusContentFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value);
                            }
                            else if ( v_MethodReflect == null )
                            {
                                v_Value = $NULL;
                                v_Info  = this.milvusContentFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value);
                            }
                            else
                            {
                                Class<?> v_ReturnType = v_MethodReflect.getReturnType();
                                if ( v_ReturnType == null ||  v_ReturnType == String.class )
                                {
                                    v_Value = "";
                                }
                                else
                                {
                                    v_Value = $NULL;
                                    v_Info  = this.milvusContentFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value);
                                }
                                
                                // 2018-11-02 Del  废除默认值填充方式
                                // v_Value = Help.toObject(v_MethodReflect.getReturnType()).toString();
                            }
                            
                            // 这里必须再执行一次填充。因为第一次为 fillMark()，本次为 fillAll() 方法
                            v_Info = this.milvusContentFill.fillAll(v_Info ,v_PlaceHolder ,v_Value);
                            
                            // v_ReplaceCount++; 此处不要++，这样才能实现动态占位符的功能。
                            // 上面的代码同时也替换占位符，可对不是动态占位符的情况，也初始化值。  ZhengWei(HY) 2018-06-06
                        }
                    }
                    catch (MilvusContentSafeException exce)
                    {
                        throw new RuntimeException(exce.getMessage());
                    }
                    catch (Exception exce)
                    {
                        $Logger.error(exce);
                    }
                    
                    if ( v_MethodReflect != null )
                    {
                        v_MethodReflect.clearDestroy();
                        v_MethodReflect = null;
                    }
                }
                
                if ( InfoType.$TextInfo == v_DBSQL_Segment.getInfoType() )
                {
                    v_Content.append(v_Info);
                }
                else if ( v_ReplaceCount == v_DBSQL_Segment.getPlaceholderSize() )
                {
                    v_Content.append(v_Info);
                }
            }
        }
        
        // 2018-03-22  优化：完善安全检查防止SQL注入，将'--形式的SQL放在整体SQL来判定。
        String v_SQLRet = v_Content.toString();
        if ( MilvusContentSafe.isSafe_SQLComment(v_SQLRet) )
        {
            return v_SQLRet;
        }
        else
        {
            throw new RuntimeException(MilvusContentSafe.sqlAttackLog(v_SQLRet));
        }
    }
    
    
    
    /**
     * 获取可执行的SQL语句，并按 Map<String ,Object> 填充有数值。
     * 
     * Map.key  即为占位符。
     *     2016-03-16 将不再区分大小写的模式配置参数。
     * 
     * 入参类型是Map时，在处理NULL与入参类型是Object，是不同的。
     *   1. Map填充为""空的字符串。
     *   2. Object填充为 "NULL" ，可以支持空值针的写入。
     * 
     *   但上方两种均可以通过配置<condition><name>占位符名称<name></condition>，向数据库写入空值针。
     * 
     * @param i_Obj
     * @return
     */
    public String getContent(Map<String ,?> i_Values)
    {
        if ( i_Values == null )
        {
            return null;
        }
        
        if ( Help.isNull(this.segments) )
        {
            return this.contentText;
        }
        
        StringBuilder         v_Content = new StringBuilder();
        Iterator<DBSQL_Split> v_Ierator = this.segments.iterator();

        // 不再区分 $DBSQL_TYPE_INSERT 类型，使所有的SQL类型均采有相同的占位符填充逻辑。ZhengWei(HY) Edit 2018-06-06
        while ( v_Ierator.hasNext() )
        {
            DBSQL_Split                   v_DBSQL_Segment = v_Ierator.next();
            PartitionMap<String ,Integer> v_Placeholders  = v_DBSQL_Segment.getPlaceholders();
            
            if ( Help.isNull(v_Placeholders) )
            {
                v_Content.append(v_DBSQL_Segment.getInfo());
            }
            else
            {
                Iterator<String> v_IterPlaceholders = v_Placeholders.keySet().iterator();
                String           v_Info             = v_DBSQL_Segment.getInfo();
                int              v_ReplaceCount     = 0;
                
                while ( v_IterPlaceholders.hasNext() )
                {
                    String v_PlaceHolder = v_IterPlaceholders.next();
                    
                    // 排除不是占位符的变量，但它的形式可能是占位符的形式。ZhengWei(HY) Add 2018-06-14
                    if ( this.notPlaceholders.contains(v_PlaceHolder) )
                    {
                        v_ReplaceCount++;
                        continue;
                    }
                    
                    try
                    {
                        Object       v_MapValue       = null;
                        DBConditions v_ConditionGroup = Help.getValueIgnoreCase(this.conditions ,v_PlaceHolder);
                        boolean      v_IsCValue       = false;
                        if ( v_ConditionGroup != null )
                        {
                            // 占位符取值条件  ZhengWei(HY) Add 2026-08-19
                            v_MapValue = v_ConditionGroup.getValue(i_Values ,false);
                            v_IsCValue = true;
                        }
                        else
                        {
                            v_MapValue = MethodReflect.getMapValue(i_Values ,v_PlaceHolder);
                        }
                        
                        // 全局占位符 ZhengWei(HY) Add 2019-03-06
                        if ( v_MapValue == null )
                        {
                            v_MapValue = Help.getValueIgnoreCase(MilvusContentStaticParams.getInstance() ,v_PlaceHolder);
                        }
                        
                        if ( v_MapValue != null )
                        {
                            if ( MethodReflect.class.equals(v_MapValue.getClass()) )
                            {
                                boolean v_IsReplace = false;
                                
                                while ( v_Info.indexOf($Placeholder + v_PlaceHolder) >= 0 )
                                {
                                    // 可实现SQL中的占位符，通过Java动态(或有业务时间逻辑的)填充值。 ZhengWei(HY) Add 2016-03-18
                                    Object v_GetterValue = ((MethodReflect)v_MapValue).invoke();
                                    
                                    // getter 方法有返回值时
                                    if ( v_GetterValue != null )
                                    {
                                        if ( !this.isSafeCheck() || MilvusContentSafe.isSafe(v_GetterValue.toString()) )
                                        {
                                            if ( v_IsCValue )
                                            {
                                                v_Info = this.milvusContentFill.onlyFillFirst(v_Info ,v_PlaceHolder ,v_GetterValue.toString());
                                            }
                                            else
                                            {
                                                v_Info = this.milvusContentFill.fillFirst(v_Info ,v_PlaceHolder ,v_GetterValue.toString());
                                            }
                                            v_IsReplace = true;
                                        }
                                        else
                                        {
                                            throw new MilvusContentSafeException(this.getContentText());
                                        }
                                    }
                                    else
                                    {
                                        String v_Value = null;
                                        if ( this.defaultDB )
                                        {
                                            v_Value = $Default;
                                            v_Info  = this.milvusContentFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value);
                                        }
                                        else if ( v_ConditionGroup != null || this.defaultNull )
                                        {
                                            // 占位符取值条件。可实现NULL值写入到数据库的功能  ZhengWei(HY) Add 2026-08-19
                                            v_Value = $NULL;
                                            v_Info  = this.milvusContentFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value);
                                        }
                                        else
                                        {
                                            Class<?> v_ReturnType = ((MethodReflect)v_MapValue).getReturnType();
                                            if ( v_ReturnType == null ||  v_ReturnType == String.class )
                                            {
                                                v_Value = "";
                                            }
                                            else
                                            {
                                                v_Value = $NULL;
                                                v_Info = this.milvusContentFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value);
                                            }
                                            
                                            // 2018-11-02 Del  废除默认值填充方式
                                            // v_Value = Help.toObject(((MethodReflect)v_MapValue).getReturnType()).toString();
                                        }
                                        
                                        // 这里必须再执行一次填充。因为第一次为 fillMark()，本次为 fillAll() 方法
                                        v_Info = this.milvusContentFill.fillAll(v_Info ,v_PlaceHolder ,v_Value);
                                        
                                        v_IsReplace = false;  // 为了支持动态占位符，这里设置为false
                                        // 同时也替换占位符，可对不是动态占位符的情况，也初始化值。  ZhengWei(HY) 2018-06-06
                                        
                                        break;
                                    }
                                }
                                
                                if ( v_IsReplace )
                                {
                                    v_ReplaceCount++;
                                }
                            }
                            else
                            {
                                if ( !this.isSafeCheck() || MilvusContentSafe.isSafe(v_MapValue.toString()) )
                                {
                                    if ( v_IsCValue )
                                    {
                                        v_Info = this.milvusContentFill.onlyFillAll(v_Info ,v_PlaceHolder ,v_MapValue.toString());
                                    }
                                    else
                                    {
                                        v_Info = this.milvusContentFill.fillAll(v_Info ,v_PlaceHolder ,v_MapValue.toString());
                                    }
                                    v_ReplaceCount++;
                                }
                                else
                                {
                                    return "";
                                }
                            }
                        }
                        else
                        {
                            // 对于没有<[ ]>可选分段的SQL
                            if ( 1 == this.segments.size() )
                            {
                                if ( this.defaultDB )
                                {
                                    v_Info = this.milvusContentFill.fillAllMark(v_Info ,v_PlaceHolder ,$Default);
                                    v_Info = this.milvusContentFill.fillAll    (v_Info ,v_PlaceHolder ,$Default);
                                }
                                else if ( v_ConditionGroup != null || this.defaultNull )
                                {
                                    // 占位符取值条件。可实现NULL值写入到数据库的功能  ZhengWei(HY) Add 2026-08-19
                                    v_Info = this.milvusContentFill.fillAllMark(v_Info ,v_PlaceHolder ,$NULL);
                                    v_Info = this.milvusContentFill.fillAll    (v_Info ,v_PlaceHolder ,$NULL);
                                }
                                else
                                {
                                    v_Info = this.milvusContentFill.fillSpace(v_Info ,v_PlaceHolder);
                                }
                                v_ReplaceCount++;
                            }
                            else
                            {
                                String v_Value = null;
                                if ( v_ConditionGroup != null || this.defaultNull )
                                {
                                    // 占位符取值条件。可实现NULL值写入到数据库的功能  ZhengWei(HY) Add 2026-08-19
                                    v_Value = $NULL;
                                    v_Info  = this.milvusContentFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value);
                                }
                                else
                                {
                                    v_Value = "";
                                }
                                
                                // 这里必须再执行一次填充。因为第一次为 fillMark()，本次为 fillAll() 方法
                                v_Info = this.milvusContentFill.fillAll(v_Info ,v_PlaceHolder ,v_Value);
                            }
                        }
                    }
                    catch (MilvusContentSafeException exce)
                    {
                        throw new RuntimeException(exce.getMessage());
                    }
                    catch (Exception exce)
                    {
                        $Logger.error(exce);
                    }
                }
                
                if ( InfoType.$TextInfo == v_DBSQL_Segment.getInfoType() )
                {
                    v_Content.append(v_Info);
                }
                else if ( v_ReplaceCount == v_DBSQL_Segment.getPlaceholderSize() )
                {
                    v_Content.append(v_Info);
                }
            }
        }
        
        // 2018-03-22  优化：完善安全检查防止SQL注入，将'--形式的SQL放在整体SQL来判定。
        String v_SQLRet = v_Content.toString();
        if ( MilvusContentSafe.isSafe_SQLComment(v_SQLRet) )
        {
            return v_SQLRet;
        }
        else
        {
            throw new RuntimeException(MilvusContentSafe.sqlAttackLog(v_SQLRet));
        }
    }
    
    
    
    /**
     * 获取可执行的SQL语句，无填充项的情况。
     * 
     * @param i_DSG  数据库连接池组。可为空或NULL
     * @return
     */
    public String getContent()
    {
        if ( Help.isNull(this.segments) )
        {
            return this.contentText;
        }
        
        StringBuilder         v_SQL     = new StringBuilder();
        Iterator<DBSQL_Split> v_Ierator = this.segments.iterator();

        // 不再区分 $DBSQL_TYPE_INSERT 类型，使所有的SQL类型均采有相同的占位符填充逻辑。ZhengWei(HY) Edit 2018-06-06
        while ( v_Ierator.hasNext() )
        {
            DBSQL_Split                   v_DBSQL_Segment = v_Ierator.next();
            PartitionMap<String ,Integer> v_Placeholders  = v_DBSQL_Segment.getPlaceholders();
            
            if ( Help.isNull(v_Placeholders) )
            {
                v_SQL.append(v_DBSQL_Segment.getInfo());
            }
            else
            {
                Iterator<String> v_IterPlaceholders = v_Placeholders.keySet().iterator();
                String           v_Info             = v_DBSQL_Segment.getInfo();
                int              v_ReplaceCount     = 0;
                
                while ( v_IterPlaceholders.hasNext() )
                {
                    String v_PlaceHolder = v_IterPlaceholders.next();
                    
                    // 排除不是占位符的变量，但它的形式可能是占位符的形式。ZhengWei(HY) Add 2018-06-14
                    if ( this.notPlaceholders.contains(v_PlaceHolder) )
                    {
                        v_ReplaceCount++;
                        continue;
                    }
                    
                    try
                    {
                        // 全局占位符 ZhengWei(HY) Add 2019-03-06
                        Object v_MapValue = Help.getValueIgnoreCase(MilvusContentStaticParams.getInstance() ,v_PlaceHolder);
                        
                        if ( v_MapValue != null )
                        {
                            if ( MethodReflect.class.equals(v_MapValue.getClass()) )
                            {
                                boolean v_IsReplace = false;
                                
                                while ( v_Info.indexOf($Placeholder + v_PlaceHolder) >= 0 )
                                {
                                    // 可实现SQL中的占位符，通过Java动态(或有业务时间逻辑的)填充值。 ZhengWei(HY) Add 2016-03-18
                                    Object v_GetterValue = ((MethodReflect)v_MapValue).invoke();
                                    
                                    // getter 方法有返回值时
                                    if ( v_GetterValue != null )
                                    {
                                        if ( !this.isSafeCheck() || MilvusContentSafe.isSafe(v_GetterValue.toString()) )
                                        {
                                            v_Info = this.milvusContentFill.fillFirst(v_Info ,v_PlaceHolder ,v_GetterValue.toString());
                                            v_IsReplace = true;
                                        }
                                        else
                                        {
                                            throw new MilvusContentSafeException(this.getContentText());
                                        }
                                    }
                                    // else
                                    // {
                                        // 因为没有执行参数，所以不做任何替换  2019-03-13
                                    // }
                                }
                                
                                if ( v_IsReplace )
                                {
                                    v_ReplaceCount++;
                                }
                            }
                            else
                            {
                                if ( !this.isSafeCheck() || MilvusContentSafe.isSafe(v_MapValue.toString()) )
                                {
                                    v_Info = this.milvusContentFill.fillAll(v_Info ,v_PlaceHolder ,v_MapValue.toString());
                                    v_ReplaceCount++;
                                }
                                else
                                {
                                    return "";
                                }
                            }
                        }
                        // else
                        // {
                            // 因为没有执行参数，所以不做任何替换  2019-03-13
                        // }
                    }
                    catch (MilvusContentSafeException exce)
                    {
                        throw new RuntimeException(exce.getMessage());
                    }
                    catch (Exception exce)
                    {
                        $Logger.error(exce);
                    }
                }
                
                if ( InfoType.$TextInfo == v_DBSQL_Segment.getInfoType() )
                {
                    v_SQL.append(v_Info);
                }
                else if ( v_ReplaceCount == v_DBSQL_Segment.getPlaceholderSize() )
                {
                    v_SQL.append(v_Info);
                }
            }
        }
        
        // 2018-03-22  优化：完善安全检查防止SQL注入，将'--形式的SQL放在整体SQL来判定。
        String v_SQLRet = v_SQL.toString();
        if ( MilvusContentSafe.isSafe_SQLComment(v_SQLRet) )
        {
            return v_SQLRet;
        }
        else
        {
            throw new RuntimeException(MilvusContentSafe.sqlAttackLog(v_SQLRet));
        }
    }
    
    
    
    /**
     * 获取：是否进行安全检查，防止Content注入。默认为：true
     */
    public boolean isSafeCheck()
    {
        return true;
    }
    
    
    
    /**
     * 获取：不是占位符的关键字的排除过滤。区分大小字。前缀无须冒号
     */
    public Set<String> getNotPlaceholderSet()
    {
        return notPlaceholders;
    }
    
    
    
    /**
     * 获取：不是占位符的关键字的排除过滤。区分大小字。前缀无须冒号
     * 
     * @param i_NotPlaceholders
     */
    public void setNotPlaceholderSet(Set<String> i_NotPlaceholders)
    {
        this.notPlaceholders = i_NotPlaceholders;
    }
    

    
    /**
     * 设置：不是占位符的关键字的排除过滤。区分大小字。前缀无须冒号。
     * 
     * @param i_NotPlaceholders  多个间用,逗号分隔
     */
    public void setNotPlaceholders(String i_NotPlaceholders)
    {
        this.notPlaceholders = new HashSet<String>();
        
        String [] v_Arr = i_NotPlaceholders.split(",");
        if ( !Help.isNull(v_Arr) )
        {
            for (String v_Placeholder : v_Arr)
            {
                this.notPlaceholders.add(v_Placeholder.trim());
            }
        }
    }
    
    
    
    /**
     * 获取：占位符取值条件
     */
    public Map<String ,DBConditions> getConditions()
    {
        return conditions;
    }
    
    
    
    /**
     * 设置：占位符取值条件
     * 
     * @param conditions
     */
    public void setConditions(Map<String ,DBConditions> conditions)
    {
        this.conditions = conditions;
    }
    
    
    
    /**
     * 添加占位符取值条件
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Condition   条件
     */
    public void addCondition(DBCondition i_Condition)
    {
        if ( i_Condition == null || Help.isNull(i_Condition.getName()) )
        {
            return;
        }
        
        DBConditions v_ConditionGroup = new DBConditions();
        v_ConditionGroup.addCondition(i_Condition);
        
        this.addCondition(i_Condition.getName() ,v_ConditionGroup);
    }
    
    
    
    /**
     * 添加占位符取值的条件组
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_ConditionGroup   条件组
     */
    public void addCondition(DBConditions i_ConditionGroup)
    {
        if ( i_ConditionGroup == null
          || i_ConditionGroup.size() < 0
          || Help.isNull(i_ConditionGroup.getName()) )
        {
            return;
        }
        
        i_ConditionGroup.setName(i_ConditionGroup.getName());
        
        this.conditions.put(i_ConditionGroup.getName() ,i_ConditionGroup);
    }
    
    
    
    /**
     * 添加占位符取值的条件组
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_PlaceholderName  占位符名称（不含前缀的冒号:）
     * @param i_ConditionGroup   条件组
     */
    public void addCondition(String i_PlaceholderName ,DBConditions i_ConditionGroup)
    {
        if ( Help.isNull(i_PlaceholderName)
          || i_ConditionGroup == null
          || i_ConditionGroup.size() < 0 )
        {
            return;
        }
        
        i_ConditionGroup.setName(i_PlaceholderName);
        
        this.conditions.put(i_PlaceholderName ,i_ConditionGroup);
    }
    
    
    
    /**
     * 获取：是否默认为NULL值写入到数据库。针对所有占位符做的统一设置。
     * 
     * 当 this.defaultNull = true 时，任何类型的值为null对象时，均向以NULL值写入到数据库。
     * 当 this.defaultNull = false 时，
     *      1. String 类型的值，按 "" 空字符串写入到数据库 或 拼接成SQL语句
     *      2. 其它类型的值，以NULL值写入到数据库。
     * 
     * 默认为：false。
     */
    public boolean isDefaultNull()
    {
        return defaultNull;
    }

    
    
    /**
     * 设置：是否默认为NULL值写入到数据库。针对所有占位符做的统一设置。
     * 
     * 当 this.defaultNull = true 时，任何类型的值为null对象时，均向以NULL值写入到数据库。
     * 当 this.defaultNull = false 时，
     *      1. String 类型的值，按 "" 空字符串写入到数据库 或 拼接成SQL语句
     *      2. 其它类型的值，以NULL值写入到数据库。
     * 
     * 默认为：false。
     * 
     * @param defaultNull
     */
    public void setDefaultNull(boolean defaultNull)
    {
        this.defaultNull = defaultNull;
    }

    
    
    /**
     * 获取：是否取数据库中表字段的默认值，当数据库未定义时写NULL值。
     * 
     * 当 this.defaultDB = true 时，任何类型的值为null对象时，均以数据库中表字段的默认值写入到数据库。
     * 当 this.defaultDB = false 时，采用 this.defaultNull 的规则。
     * 
     * 即：defaultDB 优先级大于 defaultNull
     * 
     * 默认为：false。
     */
    public boolean isDefaultDB()
    {
        return defaultDB;
    }


    
    /**
     * 设置：是否取数据库中表字段的默认值，当数据库未定义时写NULL值。
     * 
     * 当 this.defaultDB = true 时，任何类型的值为null对象时，均以数据库中表字段的默认值写入到数据库。
     * 当 this.defaultDB = false 时，采用 this.defaultNull 的规则。
     * 
     * 即：defaultDB 优先级大于 defaultNull
     * 
     * 默认为：false。
     * 
     * @param i_DefaultDB  是否取数据库中表字段的默认值，当数据库未定义时写NULL值。
     */
    public void setDefaultDB(boolean i_DefaultDB)
    {
        this.defaultDB = i_DefaultDB;
    }



    @Override
    public String toString()
    {
        return this.contentText;
    }
    
}





/**
 * 填充占位符的类
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-19
 * @version     v1.0
 */
interface MilvusContentFill
{
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换首个占位符
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String fillFirst(String i_Info ,String i_PlaceHolder ,String i_Value);
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符。
     * 
     * 替换公式：i_Info.replaceAll(":" + i_PlaceHolder , i_Value.replaceAll("'" ,"''"));
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String fillAll(String i_Info ,String i_PlaceHolder ,String i_Value);
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符（前后带单引号的替换）
     * 
     * 替换公式：i_Info.replaceAll("':" + i_PlaceHolder + "'", i_Value.replaceAll("'" ,"''"));
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String fillAllMark(String i_Info ,String i_PlaceHolder ,String i_Value);
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换首个占位符
     * 
     * 只填充，不替换特殊字符。主要用于 “条件DBConditions” ，条件中的数值交由开发者来决定
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String onlyFillFirst(String i_Info ,String i_PlaceHolder ,String i_Value);
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符。
     * 
     * 替换公式：i_Info.replaceAll(":" + i_PlaceHolder , i_Value);
     * 
     * 只填充，不替换特殊字符。主要用于 “条件DBConditions” ，条件中的数值交由开发者来决定
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String onlyFillAll(String i_Info ,String i_PlaceHolder ,String i_Value);
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符（前后带单引号的替换）
     * 
     * 替换公式：i_Info.replaceAll("':" + i_PlaceHolder + "'", i_Value.replaceAll("'" ,"''"));
     * 
     * 只填充，不替换特殊字符。主要用于 “条件DBConditions” ，条件中的数值交由开发者来决定
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String onlyFillAllMark(String i_Info ,String i_PlaceHolder ,String i_Value);
    
    
    
    /**
     * 将占位符替换成空字符串
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @return
     */
    public String fillSpace(String i_Info ,String i_PlaceHolder);
    
}





/**
 * 将占位符替换成数值。
 * 
 * 采用：单例模式
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-19
 * @version     v1.0
 */
class MilvusContentFillDefault implements MilvusContentFill ,Serializable
{
    
    private static final long serialVersionUID = -6731131805735917597L;
    
    private static MilvusContentFill $MySelf;
    
    
    
    public synchronized static MilvusContentFill getInstance()
    {
        if ( $MySelf == null )
        {
            $MySelf = new MilvusContentFillDefault();
        }
        
        return $MySelf;
    }
    
    
    private MilvusContentFillDefault()
    {
        
    }
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换首个占位符
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    @Override
    public String fillFirst(String i_Info ,String i_PlaceHolder ,String i_Value)
    {
        try
        {
            return StringHelp.replaceFirst(i_Info ,DBSQL.$Placeholder + i_PlaceHolder ,i_Value);
        }
        catch (Exception exce)
        {
            return StringHelp.replaceAll(i_Info ,DBSQL.$Placeholder + i_PlaceHolder ,Matcher.quoteReplacement(i_Value));
        }
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换首个占位符
     * 
     * 只填充，不替换特殊字符。主要用于 “条件DBConditions” ，条件中的数值交由开发者来决定
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    @Override
    public String onlyFillFirst(String i_Info ,String i_PlaceHolder ,String i_Value)
    {
        return fillFirst(i_Info ,i_PlaceHolder ,i_Value);
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符
     * 
     * 替换公式：i_Info.replaceAll(":" + i_PlaceHolder , i_Value.replaceAll("'" ,"''"));
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    @Override
    public String fillAll(String i_Info ,String i_PlaceHolder ,String i_Value)
    {
        try
        {
            return StringHelp.replaceAll(i_Info ,DBSQL.$Placeholder + i_PlaceHolder ,i_Value);
        }
        catch (Exception exce)
        {
            return StringHelp.replaceAll(i_Info ,DBSQL.$Placeholder + i_PlaceHolder ,Matcher.quoteReplacement(i_Value));
        }
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符。
     * 
     * 替换公式：i_Info.replaceAll(":" + i_PlaceHolder , i_Value);
     * 
     * 只填充，不替换特殊字符。主要用于 “条件DBConditions” ，条件中的数值交由开发者来决定
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    @Override
    public String onlyFillAll(String i_Info ,String i_PlaceHolder ,String i_Value)
    {
        return fillAll(i_Info ,i_PlaceHolder ,i_Value);
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符（前后带单引号的替换）
     * 
     * 替换公式：i_Info.replaceAll("':" + i_PlaceHolder + "'", i_Value.replaceAll("'" ,"''"));
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    @Override
    public String fillAllMark(String i_Info ,String i_PlaceHolder ,String i_Value)
    {
        try
        {
            return StringHelp.replaceAll(i_Info ,"'" + DBSQL.$Placeholder + i_PlaceHolder + "'" ,i_Value);
        }
        catch (Exception exce)
        {
            return StringHelp.replaceAll(i_Info ,DBSQL.$Placeholder + i_PlaceHolder ,Matcher.quoteReplacement(i_Value));
        }
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符（前后带单引号的替换）
     * 
     * 替换公式：i_Info.replaceAll("':" + i_PlaceHolder + "'", i_Value.replaceAll("'" ,"''"));
     * 
     * 只填充，不替换特殊字符。主要用于 “条件DBConditions” ，条件中的数值交由开发者来决定
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    @Override
    public String onlyFillAllMark(String i_Info ,String i_PlaceHolder ,String i_Value)
    {
        return fillAllMark(i_Info ,i_PlaceHolder ,i_Value);
    }
    
    
    
    /**
     * 将占位符替换成空字符串
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @return
     */
    @Override
    public String fillSpace(String i_Info ,String i_PlaceHolder)
    {
        return StringHelp.replaceAll(i_Info ,DBSQL.$Placeholder + i_PlaceHolder ,"");
    }
    
}
