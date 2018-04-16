package org.hy.common.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hy.common.Help;
import org.hy.common.SplitSegment;
import org.hy.common.SplitSegment.InfoType;
import org.hy.common.StringHelp;
import org.hy.common.MethodReflect;





/**
 * 数据库占位符SQL的信息。
 * 
 * 主要对类似如下的SQL信息（我们叫它为:占位符SQL）进行分析后，并根据Java的 "属性类(或叫值对应类)" 转换为真实能被执行的SQL。
 * 
 * SELECT  * 
 *   FROM  Dual
 *  WHERE  BeginTime = TO_DATE(':BeginTime' ,'YYYY-MM-DD HH24:MI:SS')
 *    AND  EndTime   = TO_DATE(':EndTime'   ,'YYYY-MM-DD HH24:MI:SS')
 *    AND  Statue    = :Statue
 *    AND  UserName  = ':UserName'
 *    AND  CardNo    = ':CardNo'
 *    AND  CityCode  = 0910
 * 
 * 原理是这样的：上述占位符SQL中的 ":BeginTime" 为占位符。将用 "属性类" getBeginTime() 方法的返回值进行替换操作。
 * 
 *            1. 当 "属性类" 没有对应的 getBeginTime() 方法时，
 *               生成的可执行SQL中将不包括 "BeginTime = TO_DATE(':BeginTime' ,'YYYY-MM-DD HH24:MI:SS')" 的部分。
 *               
 *            2. 当 "属性类" 有对应的 getBeginTime() 方法时，但返回值为 null时，
 *               生成的可执行SQL中将不包括 "BeginTime = TO_DATE(':BeginTime' ,'YYYY-MM-DD HH24:MI:SS')" 的部分。
 *               
 *            3. ":BeginTime" 占位符的命名，要符合Java的驼峰命名规则，但首字母可以大写，也可以小写。
 *            
 *            4. ":Statue" 占位符对应 "属性类" getStatue() 方法的返回值类型为基础类型(int、double)时，
 *               不可能为 null 值的情况。即，此占位符在可执行SQL中是必须存在。
 *               如果想其可变，须使用 Integer、Double 类型的返回值类型。
 *               当然，我们还提供了一个 getSQL(Map<String ,Object> i_Values) 方法为解决这个问题。
 *    
 * @author      ZhengWei(HY)
 * @createDate  2012-10-31
 * @version     v1.0  
 *              v2.0  2014-07-29  1.使用简单替换方式(即Map之后，对象也使用此方式)  
 *                                2.支持动态SQL，即当 <[ ... ]> 符号内的占位符无传入值时，
 *                                  最终生成的执行SQL中，也无包含 <[ ... ]> 内的SQL语句 
 *                                  
 *              v3.0  2015-12-10  1. getSQL(Object) 方法，SQL语句生成时，对于占位符，可实现xxx.yyy.www(或getXxx.getYyy.getWww)全路径的解释。如，':shool.BeginTime'
 *              
 *              v4.0  2016-03-16  1. getSQL(Map) 方法中入参Map中的Map.key，将不再区分大小写的模式配置参数。在此之前是区分大小写的，不方便。
 *                                2. getSQL(Map) 方法中入参Map中的Map.value，当为MethodReflect类型时，再通过MethodReflect.invoke()方法获取最终的填充值。
 *                                   可实现SQL中的占位符，通过Java动态(或有业务时间逻辑的)填充值。比如，向数据库表插入数据时，通过Java生成主键的功能。
 *                                   
 *              v5.0  2016-07-29  1. getSQL(Map) 方法，实现xxx.yyy.www(或getXxx.getYyy.getWww)全路径的解释
 *                                2. getSQL(Object) 方法中参数Getter方法的返回值，当为MethodReflect类型时，再通过MethodReflect.invoke()方法获取最终的填充值。
 *                                   可实现SQL中的占位符，通过Java动态(或有业务时间逻辑的)填充值。比如，向数据库表插入数据时，通过Java生成主键的功能。
 *              v5.1  2016-08-09  1. 将数值中的单引号替换成两个单引号。单引号是数据库的字符串两边的限定符。
 *                                   如果占位符对应的数值中也存在单引号，会造成生成的SQL语句无法正确执行。
 *                                   是否替换可通过 this.keyReplace 属性控制。
 *              v6.0  2017-08-01  1. 添加：安全检查防止SQL注入。
 *              v6.1  2018-03-22  1. 优化：完善安全检查防止SQL注入，将'--形式的SQL放在整体SQL来判定。
 *              v6.2  2018-04-13  1. 修复：将所有Java原生的replace字符串替换方法，全部的废弃不用，而是改用StringHelp类是替换方法。原因是$符等特殊字符会出错。
 *                                        发现人：向以前。
 */
public class DBSQL
{
    public final static int   $DBSQL_TYPE_UNKNOWN   = -1;
    
    public final static int   $DBSQL_TYPE_SELECT    = 1;
    
    public final static int   $DBSQL_TYPE_INSERT    = 2;
    
    public final static int   $DBSQL_TYPE_UPDATE    = 3;
    
    public final static int   $DBSQL_TYPE_DELETE    = 4;
    
    public final static int   $DBSQL_TYPE_CALL      = 5;
    
    public final static int   $DBSQL_TYPE_DDL       = 6;
    
    
    private final static Map<String ,String> $ReplaceKeys = new HashMap<String ,String>();
    
    
    
    static 
    {
        $ReplaceKeys.put("\t"  ," ");
        $ReplaceKeys.put("\r"  ," ");
        $ReplaceKeys.put("\n"  ," ");
    }
    
    
    
    /** 占位符SQL */
    private String              sqlText;
    
    /** SQL类型 */
    private int                 sqlType;
    
    /** 替换数据库关键字。如，单引号替换成两个单引号。默认为：false，即不替换 */
    private boolean             keyReplace;
    
    /** 是否进行安全检查，防止SQL注入。默认为：true */
    private boolean             safeCheck;
    
    private DBSQLFill           dbSQLFill;
    
    /** 通过分析后的分段SQL信息 */
    private List<DBSQL_Split>   segments;
    
    /** JDBC原生态的"预解释的SQL" */
    private DBPreparedSQL       preparedSQL;
    
    
    
    /**
     * 构造器
     */
    public DBSQL()
    {
        this.sqlText     = "";
        this.sqlType     = $DBSQL_TYPE_UNKNOWN;
        this.segments    = new ArrayList<DBSQL_Split>();
        this.preparedSQL = new DBPreparedSQL();
        this.safeCheck   = true;
        this.setKeyReplace(false);
    }
    
    
    
    /**
     * 构造器
     * 
     * @param i_SQLText  完整的原始SQL文本
     */
    public DBSQL(String i_SQLText)
    {
        this.sqlType   = $DBSQL_TYPE_UNKNOWN;
        this.safeCheck = true;
        this.setSqlText(i_SQLText);
        this.setKeyReplace(false);
    }
    
    
    
    /**
     * 分析SQL（私有）
     * 
     * @param i_SQL
     * @return
     */
    private void parser()
    {
        if ( Help.isNull(this.sqlText) )
        {
            return;
        }
        
        
        this.parser_SQLType();
        
        
        // 匹配 <[ ... ]> 的字符串
//      List<SplitSegment> v_Segments = StringHelp.Split("[ \\s]?<\\[[^(?!((<\\[)|(\\]>)))]+\\]>[ \\s]?" ,this.sqlText);  无法包含< ( >三种特殊字符
        List<SplitSegment> v_Segments = StringHelp.Split("[ \\s]?<\\[((?!<\\[|\\]>).)*\\]>[ \\s]?"       ,this.sqlText);
        for (SplitSegment v_SplitSegment : v_Segments)
        {
            DBSQL_Split v_DBSQL_Segment = new DBSQL_Split(v_SplitSegment);
            
            String v_Info = v_DBSQL_Segment.getInfo();
            v_Info = v_Info.replaceFirst("<\\[" ,"  ");
            v_Info = v_Info.replaceFirst("\\]>" ,"  ");
            
            v_DBSQL_Segment.setInfo(v_Info);
            v_DBSQL_Segment.parsePlaceholders();
            
            this.segments.add(v_DBSQL_Segment);
        }
        
        this.preparedSQL = this.parserPreparedSQL();
    }
    
    
    
    /**
     * 识别SQL语句的类型
     */
    private void parser_SQLType()
    {
        if ( Help.isNull(this.sqlText) )
        {
            return;
        }
        
        
        Pattern v_Pattern = null;
        Matcher v_Matcher = null;
        
        
        v_Pattern = Pattern.compile("^( )*[Ss][Ee][Ll][Ee][Cc][Tt][ ]+");
        v_Matcher = v_Pattern.matcher(this.sqlText);
        if ( v_Matcher.find() )
        {
            this.sqlType = $DBSQL_TYPE_SELECT;
            return;
        }
        
        
        v_Pattern = Pattern.compile("^( )*[Ii][Nn][Ss][Ee][Rr][Tt][ ]+");
        v_Matcher = v_Pattern.matcher(this.sqlText);
        if ( v_Matcher.find() )
        {
            this.sqlType = $DBSQL_TYPE_INSERT;
            return;
        }
        
        
        v_Pattern = Pattern.compile("^( )*[Uu][Pp][Dd][Aa][Tt][Ee][ ]+");
        v_Matcher = v_Pattern.matcher(this.sqlText);
        if ( v_Matcher.find() )
        {
            this.sqlType = $DBSQL_TYPE_UPDATE;
            return;
        }
        
        
        v_Pattern = Pattern.compile("^( )*[Dd][Ee][Ll][Ee][Tt][Ee][ ]+");
        v_Matcher = v_Pattern.matcher(this.sqlText);
        if ( v_Matcher.find() )
        {
            this.sqlType = $DBSQL_TYPE_DELETE;
            return;
        }
    }
    
    
    
    /**
     * 填充或设置占位符SQL
     * 
     * @param sqlText
     */
    public synchronized void setSqlText(String i_SQLText) 
    {
        this.sqlText = StringHelp.replaceAll(Help.NVL(i_SQLText).trim() ,$ReplaceKeys);
        
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
     * 获取占位符SQL
     * 
     * @return
     */
    public synchronized String getSqlText() 
    {
        return sqlText;
    }
    
    
    
    /**
     * 获取可执行的SQL语句，并按 i_Obj 填充有数值。
     * 
     * @param i_Obj
     * @return
     */
    @SuppressWarnings("unchecked")
    public String getSQL(Object i_Obj)
    {
        if ( i_Obj == null )
        {
            return null;
        }
        
        if ( Help.isNull(this.segments) )
        {
            return this.sqlText;
        }
        
        if ( i_Obj instanceof Map )
        {
            return this.getSQL((Map<String ,?>)i_Obj);
        }
        
        
        StringBuilder         v_SQL     = new StringBuilder();
        Iterator<DBSQL_Split> v_Ierator = this.segments.iterator();
        
        
        while ( v_Ierator.hasNext() )
        {
            DBSQL_Split         v_DBSQL_Segment = v_Ierator.next();
            Map<String ,Object> v_Placeholders  = v_DBSQL_Segment.getPlaceholders();
            
            if ( Help.isNull(v_Placeholders) )
            {
                v_SQL.append(v_DBSQL_Segment.getInfo());
            }
            else
            {
                Iterator<String> v_IterPlaceholders = v_Placeholders.keySet().iterator();
                String           v_Info             = v_DBSQL_Segment.getInfo();
                int              v_ReplaceCount     = 0;
                
                if ( this.sqlType != $DBSQL_TYPE_INSERT )
                {
                    while ( v_IterPlaceholders.hasNext() )
                    {
                        String        v_PlaceHolder   = v_IterPlaceholders.next();
                        MethodReflect v_MethodReflect = null;
                        /*
                                                在实现全路径的解释功能之前的老方法  ZhengWei(HY) Del 2015-12-10
                        Method        v_Method        = MethodReflect.getGetMethod(i_Obj.getClass() ,v_PlaceHolder ,true);
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
                        
                        if ( v_MethodReflect != null )
                        {
                            try
                            {
                                Object v_GetterValue = v_MethodReflect.invoke();
                                
                                // getter 方法有返回值时
                                if ( v_GetterValue != null )
                                {
                                    if ( MethodReflect.class.equals(v_GetterValue.getClass()) )
                                    {
                                        boolean v_IsReplace = false;
                                        
                                        while ( v_Info.indexOf(":" + v_PlaceHolder) >= 0 )
                                        {
                                            // 可实现SQL中的占位符，通过Java动态(或有业务时间逻辑的)填充值。 ZhengWei(HY) Add 2016-03-18
                                            Object v_MRValue = ((MethodReflect)v_GetterValue).invoke();
                                            
                                            if ( v_MRValue != null )
                                            {
                                                if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_MRValue.toString()) )
                                                {
                                                    v_Info = this.dbSQLFill.fillFirst(v_Info ,v_PlaceHolder ,v_MRValue.toString());
                                                    v_IsReplace = true;
                                                }
                                                else
                                                {
                                                    throw new DBSQLSafeException(this.getSqlText());
                                                }
                                            }
                                            else
                                            {
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
                                        if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_GetterValue.toString()) )
                                        {
                                            v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_GetterValue.toString());
                                            v_ReplaceCount++;
                                        }
                                        else
                                        {
                                            throw new DBSQLSafeException(this.getSqlText());
                                        }
                                    }
                                }
                            }
                            catch (DBSQLSafeException exce)
                            {
                                throw new RuntimeException(exce.getMessage());
                            }
                            catch (Exception exce)
                            {
                                exce.printStackTrace();
                            }
                        }
                    }
                }
                else
                {
                    while ( v_IterPlaceholders.hasNext() )
                    {
                        String        v_PlaceHolder   = v_IterPlaceholders.next();
                        MethodReflect v_MethodReflect = null;
                        /*
                                                在实现全路径的解释功能之前的老方法  ZhengWei(HY) Del 2015-12-10
                        Method        v_Method        = MethodReflect.getGetMethod(i_Obj.getClass() ,v_PlaceHolder ,true);
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
                        
                        if ( v_MethodReflect != null )
                        {
                            try
                            {
                                Object v_GetterValue = v_MethodReflect.invoke();
                                
                                // getter 方法有返回值时
                                if ( v_GetterValue != null )
                                {
                                    if ( MethodReflect.class.equals(v_GetterValue.getClass()) )
                                    {
                                        boolean v_IsReplace = false;
                                        
                                        while ( v_Info.indexOf(":" + v_PlaceHolder) >= 0 )
                                        {
                                            // 可实现SQL中的占位符，通过Java动态(或有业务时间逻辑的)填充值。 ZhengWei(HY) Add 2016-03-18
                                            Object v_MRValue = ((MethodReflect)v_GetterValue).invoke();
                                            
                                            if ( v_MRValue != null )
                                            {
                                                if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_MRValue.toString()) )
                                                {
                                                    v_Info = this.dbSQLFill.fillFirst(v_Info ,v_PlaceHolder ,v_MRValue.toString());
                                                    v_IsReplace = true;
                                                }
                                                else
                                                {
                                                    throw new DBSQLSafeException(this.getSqlText());
                                                }
                                            }
                                            else
                                            {
                                                String v_Value = Help.toObject(((MethodReflect)v_GetterValue).getReturnType()).toString();
                                                if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_Value) )
                                                {
                                                    v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_Value);
                                                    v_IsReplace = true;
                                                }
                                                else
                                                {
                                                    throw new DBSQLSafeException(this.getSqlText());
                                                }
                                            }
                                        }
                                        
                                        if ( v_IsReplace )
                                        {
                                            v_ReplaceCount++;
                                        }
                                    }
                                    else
                                    {
                                        if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_GetterValue.toString()) )
                                        {
                                            v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_GetterValue.toString());
                                            v_ReplaceCount++;
                                        }
                                        else
                                        {
                                            throw new DBSQLSafeException(this.getSqlText());
                                        }
                                    }
                                }
                                else
                                {
                                    String v_Value = Help.toObject(v_MethodReflect.getReturnType()).toString();
                                    if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_Value) )
                                    {
                                        v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_Value);
                                        v_ReplaceCount++;
                                    }
                                    else
                                    {
                                        return "";
                                    }
                                }
                            }
                            catch (DBSQLSafeException exce)
                            {
                                throw new RuntimeException(exce.getMessage());
                            }
                            catch (Exception exce)
                            {
                                exce.printStackTrace();
                            }
                        }
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
        if ( DBSQLSafe.isSafe_SQLComment(v_SQLRet) )
        {
            return v_SQLRet;
        }
        else
        {
            throw new RuntimeException(DBSQLSafe.sqlAttackLog(v_SQLRet));
        }
    }
    
    
    
    /**
     * 获取可执行的SQL语句，并按 Map<String ,Object> 填充有数值。
     * 
     * Map.key  即为占位符。
     *     2016-03-16 将不再区分大小写的模式配置参数。
     * 
     * @param i_Obj
     * @return
     */
    public String getSQL(Map<String ,?> i_Values)
    {
        if ( Help.isNull(i_Values) )
        {
            return null;
        }
        
        if ( Help.isNull(this.segments) )
        {
            return this.sqlText;
        }
        
        
        StringBuilder         v_SQL     = new StringBuilder();
        Iterator<DBSQL_Split> v_Ierator = this.segments.iterator();
        

        while ( v_Ierator.hasNext() )
        {
            DBSQL_Split         v_DBSQL_Segment = v_Ierator.next();
            Map<String ,Object> v_Placeholders  = v_DBSQL_Segment.getPlaceholders();
            
            if ( Help.isNull(v_Placeholders) )
            {
                v_SQL.append(v_DBSQL_Segment.getInfo());
            }
            else
            {
                Iterator<String> v_IterPlaceholders = v_Placeholders.keySet().iterator();
                String           v_Info             = v_DBSQL_Segment.getInfo();
                int              v_ReplaceCount     = 0;
                
                if ( this.sqlType != $DBSQL_TYPE_INSERT )
                {
                    while ( v_IterPlaceholders.hasNext() )
                    {
                        String v_PlaceHolder = v_IterPlaceholders.next();
                        
                        try
                        {
                            Object v_MapValue = MethodReflect.getMapValue(i_Values ,v_PlaceHolder);
                            
                            if ( v_MapValue != null )
                            {
                                if ( MethodReflect.class.equals(v_MapValue.getClass()) )
                                {
                                    boolean v_IsReplace = false;
                                    
                                    while ( v_Info.indexOf(":" + v_PlaceHolder) >= 0 )
                                    {
                                        // 可实现SQL中的占位符，通过Java动态(或有业务时间逻辑的)填充值。 ZhengWei(HY) Add 2016-03-18
                                        Object v_GetterValue = ((MethodReflect)v_MapValue).invoke();
                                        
                                        // getter 方法有返回值时
                                        if ( v_GetterValue != null )
                                        {
                                            if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_GetterValue.toString()) )
                                            {
                                                v_Info = this.dbSQLFill.fillFirst(v_Info ,v_PlaceHolder ,v_GetterValue.toString());
                                                v_IsReplace = true;
                                            }
                                            else
                                            {
                                                throw new DBSQLSafeException(this.getSqlText());
                                            }
                                        }
                                        else
                                        {
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
                                    if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_MapValue.toString()) )
                                    {
                                        v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_MapValue.toString());
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
                                    v_Info = this.dbSQLFill.fillSpace(v_Info ,v_PlaceHolder);
                                    v_ReplaceCount++;
                                }
                            }
                        }
                        catch (DBSQLSafeException exce)
                        {
                            throw new RuntimeException(exce.getMessage());
                        }
                        catch (Exception exce)
                        {
                            exce.printStackTrace();
                        }
                    }
                }
                else
                {
                    while ( v_IterPlaceholders.hasNext() )
                    {
                        String v_PlaceHolder = v_IterPlaceholders.next();
                        
                        try
                        {
                            Object v_MapValue = MethodReflect.getMapValue(i_Values ,v_PlaceHolder);
                            
                            if ( v_MapValue != null )
                            {
                                if ( MethodReflect.class.equals(v_MapValue.getClass()) )
                                {
                                    boolean v_IsReplace = false;
                                    
                                    while ( v_Info.indexOf(":" + v_PlaceHolder) >= 0 )
                                    {
                                        // 可实现SQL中的占位符，通过Java动态(或有业务时间逻辑的)填充值。 ZhengWei(HY) Add 2016-03-18
                                        Object v_GetterValue = ((MethodReflect)v_MapValue).invoke();
                                        
                                        // getter 方法有返回值时
                                        if ( v_GetterValue != null )
                                        {
                                            if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_GetterValue.toString()) )
                                            {
                                                v_Info = this.dbSQLFill.fillFirst(v_Info ,v_PlaceHolder ,v_GetterValue.toString());
                                                v_IsReplace = true;
                                            }
                                            else
                                            {
                                                throw new DBSQLSafeException(this.getSqlText());
                                            }
                                        }
                                        else
                                        {
                                            String v_Value = Help.toObject(((MethodReflect)v_MapValue).getReturnType()).toString();
                                            if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_Value) )
                                            {
                                                v_Info = this.dbSQLFill.fillFirst(v_Info ,v_PlaceHolder ,v_Value);
                                                v_IsReplace = true;
                                            }
                                            else
                                            {
                                                throw new DBSQLSafeException(this.getSqlText());
                                            }
                                        }
                                    }
                                    
                                    if ( v_IsReplace )
                                    {
                                        v_ReplaceCount++;
                                    }
                                }
                                else
                                {
                                    if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_MapValue.toString()) )
                                    {
                                        v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_MapValue.toString());
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
                                v_Info = this.dbSQLFill.fillSpace(v_Info ,v_PlaceHolder);
                                v_ReplaceCount++;
                            }
                        }
                        catch (DBSQLSafeException exce)
                        {
                            throw new RuntimeException(exce.getMessage());
                        }
                        catch (Exception exce)
                        {
                            exce.printStackTrace();
                        }
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
        if ( DBSQLSafe.isSafe_SQLComment(v_SQLRet) )
        {
            return v_SQLRet;
        }
        else
        {
            throw new RuntimeException(DBSQLSafe.sqlAttackLog(v_SQLRet));
        }
    }
    
    
    
    /**
     * 获取JDBC原生态的"预解释的SQL"
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-03
     * @version     v1.0
     *
     * @return
     */
    public DBPreparedSQL getPreparedSQL()
    {
        return this.preparedSQL;
    }
    
    
    
    /**
     * 解释SQL模板为：JDBC原生态的"预解释的SQL"
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-03
     * @version     v1.0
     *
     * @return
     */
    private DBPreparedSQL parserPreparedSQL()
    {
        if ( Help.isNull(this.segments) )
        {
            return new DBPreparedSQL(this.sqlText);
        }
        
        
        DBPreparedSQL         v_Ret     = new DBPreparedSQL();
        StringBuilder         v_SQL     = new StringBuilder();
        Iterator<DBSQL_Split> v_Ierator = this.segments.iterator();

        while ( v_Ierator.hasNext() )
        {
            DBSQL_Split         v_DBSQL_Segment = v_Ierator.next();
            Map<String ,Object> v_Placeholders  = v_DBSQL_Segment.getPlaceholdersSequence();
            
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
                    
                    while ( v_Info.indexOf(":" + v_PlaceHolder) >= 0 )
                    {
                        v_Info = StringHelp.replaceFirst(v_Info ,":" + v_PlaceHolder ,"?");
                        v_Ret.getPlaceholders().add(v_PlaceHolder);
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
        
        v_Ret.setSQL(v_SQL.toString());
        return v_Ret;
    }
    
    
    
    /**
     * 获取可执行的SQL语句，无填充项的情况。
     * 
     * @return
     */
    public String getSQL()
    {
        // 具体功能待后期实现
        return this.sqlText;
    }
    
    
    
    public int getSQLType() 
    {
        return sqlType;
    }
    
    
    
    /**
     * 获取：替换数据库关键字。如，单引号替换成两个单引号。默认为：false，即不替换
     */
    public boolean isKeyReplace()
    {
        return keyReplace;
    }


    
    /**
     * 设置：替换数据库关键字。如，单引号替换成两个单引号。默认为：false，即不替换
     * 
     * 采用类似工厂方法构造 DBSQLFill，惟一的目的就是为了生成SQL时，减少IF判断，提高速度。
     * 
     * @param i_KeyReplace 
     */
    public void setKeyReplace(boolean i_KeyReplace)
    {
        if ( i_KeyReplace )
        {
            this.dbSQLFill = DBSQLFillKeyReplace.getInstance();
        }
        else
        {
            this.dbSQLFill = DBSQLFillDefault.getInstance();
        }
        
        this.keyReplace = i_KeyReplace;
    }


    
    /**
     * 获取：是否进行安全检查，防止SQL注入。默认为：true
     */
    public boolean isSafeCheck()
    {
        return safeCheck;
    }


    
    /**
     * 设置：是否进行安全检查，防止SQL注入。默认为：true
     * 
     * @param safeCheck 
     */
    public void setSafeCheck(boolean safeCheck)
    {
        this.safeCheck = safeCheck;
    }



    public String toString()
    {
        return this.sqlText;
    }
    
    
    
    /*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
    protected void finalize() throws Throwable 
    {
        this.segments.clear();
        
        super.finalize();
    }
    */
    
}





/**
 * 填充占位符的类 
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-08-09
 * @version     v1.0
 */
interface DBSQLFill
{
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换首个占位符
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String fillFirst(String i_Info ,String i_PlaceHolder ,String i_Value);
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String fillAll(String i_Info ,String i_PlaceHolder ,String i_Value);
    
    
    
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
    public String fillSpace(String i_Info ,String i_PlaceHolder);
    
}





/**
 * 将占位符替换成数值。
 * 
 * 采用：单例模式
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-08-09
 * @version     v1.0
 */
class DBSQLFillDefault implements DBSQLFill
{
    private static DBSQLFill $MySelf;
    
    
    public synchronized static DBSQLFill getInstance()
    {
        if ( $MySelf == null )
        {
            $MySelf = new DBSQLFillDefault();
        }
        
        return $MySelf;
    }
    
    
    private DBSQLFillDefault()
    {
        
    }
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换首个占位符
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String fillFirst(String i_Info ,String i_PlaceHolder ,String i_Value)
    {
        return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,i_Value);
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String fillAll(String i_Info ,String i_PlaceHolder ,String i_Value)
    {
        return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,i_Value);
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
    public String fillSpace(String i_Info ,String i_PlaceHolder)
    {
        return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,"");
    }
}





/**
 * 将数值中的单引号替换成两个单引号。单引号是数据库的字符串两边的限定符。
 * 如果占位符对应的数值中也存在单引号，会造成生成的SQL语句无法正确执行。
 * 是否替换可通过 DBSQL.keyReplace 属性控制。
 * 
 * 采用：单例模式
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-08-09
 * @version     v1.0
 */
class DBSQLFillKeyReplace implements DBSQLFill
{
    
    public  static final String    $FillReplace   = "'";
    
    public  static final String    $FillReplaceBy = "''";
    
    private static       DBSQLFill $MySelf;
    
    
    public synchronized static DBSQLFill getInstance()
    {
        if ( $MySelf == null )
        {
            $MySelf = new DBSQLFillKeyReplace();
        }
        
        return $MySelf;
    }
    
    
    private DBSQLFillKeyReplace()
    {
        
    }
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换首个占位符
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String fillFirst(String i_Info ,String i_PlaceHolder ,String i_Value)
    {
        try
        {
            return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,StringHelp.replaceAll(i_Value ,$FillReplace ,$FillReplaceBy));
        }
        catch (Exception exce)
        {
            return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(StringHelp.replaceAll(i_Value ,$FillReplace ,$FillReplaceBy)));
        }
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @return
     */
    public String fillAll(String i_Info ,String i_PlaceHolder ,String i_Value)
    {
        try
        {
            return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,StringHelp.replaceAll(i_Value ,$FillReplace ,$FillReplaceBy));
        }
        catch (Exception exce)
        {
            return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(StringHelp.replaceAll(i_Value ,$FillReplace ,$FillReplaceBy)));
        }
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
    public String fillSpace(String i_Info ,String i_PlaceHolder)
    {
        return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,"");
    }
    
}
