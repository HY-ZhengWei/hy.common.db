package org.hy.common.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hy.common.Help;
import org.hy.common.MethodReflect;
import org.hy.common.PartitionMap;
import org.hy.common.SplitSegment;
import org.hy.common.SplitSegment.InfoType;
import org.hy.common.StringHelp;





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
 *              v6.3  2018-06-06  1. 修改：不再区分 $DBSQL_TYPE_INSERT 类型，使所有的SQL类型均采有相同的占位符填充逻辑
 *              v7.0  2018-06-14  1. 添加：不是占位符的关键字的排除过滤
 *              v8.0  2018-07-18  1. 添加：对Insert、Update语句解析出其表名称的功能。
 *              v9.0  2018-08-10  1. 添加：实现占位符X有条件的取值。占位符在满足条件时取值A，否则取值B。
 *                                        取值A、B，可以是占位符X、NULL值，另一个占位符Y或常量字符。
 *                                        类似于Mybatis IF条件功能。建议人：马龙
 *              v9.1  2018-11-02  1. 修复：入参类型是对象，并且对象的时间类型的属性为NULL时，自动按默认值（当前时间）填充SQL。发现人：邹德福
 *                                        修改为如下规则：
 *                                            1. 入参类型是Object时，占位符对应的值为NULL时，String类型按""空字符串填充，其它类型按"NULL"填充（可实现空指针写入数据库的功能）。
 *                                            2. 入参类型是Map时   ，占位符对应的值为NULL时，String类型按""空字符串填充。
 *              V10.0 2019-01-10  1. 添加：允许个别占位符不去替换数据库关键字(如单引号')。
 *              v11.0 2019-01-19  1. 添加：是否默认为NULL值写入到数据库。针对所有占位符做的统一设置。
 *                                2. 添加：占位符条件组。用于实现Java编程语言中的 if .. else if ... else ... 的多条件复杂判定。
 *                                         建议人：张宇
 *              v12.0 2019-03-06  1. 添加：全局占位符功能。详见 DBSQLStaticParams 类的说明。
 *                                         建议人：邹德福
 *              v13.0 2019-05-08  1. 添加：对MySQL数据库添加\号的转义。\号本身就是MySQL数据库的转义符。
 *                                         但写入文本信息时，\号多数时是想被直接当普通符号写入到数据库中。
 *                                         发现人：程志华
 *              v14.0 2019-05-31  1. 添加：使用<[]>来限定占位符的前后位置，格式如， <[:占位符]>
 *                                         即可实现占位符后拼接字符串的功能，如，<[:占位符]>xxx
 *                                         建议人：张顺
 *              v15.0 2019-07-05  1. 修改：keyReplace的默认值改为true。默认替换特殊字符
 *              v15.1 2019-08-23  1. 添加：是否允许替换字符串。防止如：'A' ,'B' ,'C' ... ,'Z'  这样格式的字符串被替换。
 *                                         一般用于由外界动态生成的在 IN 语法中，如 IN ('A' ,'B' ,'C' ... ,'Z')，此时这里的单引号就不应被替换。
 *              v16.0 2020-06-08  1. 添加：预解析的占位符，再也不能刻意注意点位符的顺序了。可以像常规SQL的占位符一样，任意摆放了。
 *                                2. 优化：预解析的占位符，不再要求去掉左右两边的单引号了。即与常规SQL的占位符一样。
 *              v17.0 2022-01-13  1. 添加：识别SQL语句的类型功能，支持对 WITH AS 的语句的识别
 *              v18.0 2022-10-28  1. 修改：parserPreparedSQL() 方法添加v_PBeing修复解释占位符的错误。发现人：程元丰
 */
public class DBSQL implements Serializable
{
    
    private static final long serialVersionUID = 6969242576876292691L;
    
    
    
    public final static int                  $DBSQL_TYPE_UNKNOWN = -1;
    
    public final static int                  $DBSQL_TYPE_SELECT  = 1;
    
    public final static int                  $DBSQL_TYPE_INSERT  = 2;
    
    public final static int                  $DBSQL_TYPE_UPDATE  = 3;
    
    public final static int                  $DBSQL_TYPE_DELETE  = 4;
    
    public final static int                  $DBSQL_TYPE_CALL    = 5;
    
    public final static int                  $DBSQL_TYPE_DDL     = 6;
    
    
    
    /** 数据库中的NULL关键字 */
    private final static String              $NULL        = "NULL";
    
    private final static String              $Insert      = "INSERT";
    
    private final static String              $Into        = "INTO";
    
    private final static String              $Update      = "UPDATE";
    
    private final static String              $From        = "FROM";
    
    private final static String              $Where       = "WHERE";
    
    private final static Map<String ,String> $ReplaceKeys = new HashMap<String ,String>();
    
    
    
    static
    {
        $ReplaceKeys.put("\t"  ," ");
        $ReplaceKeys.put("\r"  ," ");
        $ReplaceKeys.put("\n"  ," ");
    }
    
    
    /** 数据库连接池组 */
    private DataSourceGroup           dsg;
    
    /** 占位符SQL */
    private String                    sqlText;
    
    /** SQL类型 */
    private int                       sqlType;
    
    /** SQL语句操作的表名称。用于Insert、Update语句 */
    private String                    sqlTableName;
    
    /** 替换数据库关键字。如，单引号替换成两个单引号。默认为：true，即替换 */
    private boolean                   keyReplace;
    
    /** 当this.keyReplace=true时有效。表示个别不替换数据库关键字的占位符。前缀无须冒号 */
    private Set<String>               notKeyReplace;
    
    /** 是否进行安全检查，防止SQL注入。默认为：true */
    private boolean                   safeCheck;
    
    private DBSQLFill                 dbSQLFill;
    
    /** 通过分析后的分段SQL信息 */
    private List<DBSQL_Split>         segments;
    
    /** JDBC原生态的"预解释的SQL" */
    private DBPreparedSQL             preparedSQL;
    
    /** 不是占位符的关键字的排除过滤。区分大小字。前缀无须冒号 */
    private Set<String>               notPlaceholders;
    
    /** 占位符取值条件 */
    private Map<String ,DBConditions> conditions;
    
    /**
     * 是否默认为NULL值写入到数据库。针对所有占位符做的统一设置。
     * 
     * 当 this.defaultNull = true 时，任何类型的值为null对象时，均向以NULL值写入到数据库。
     * 当 this.defaultNull = false 时，
     *      1. String 类型的值，按 "" 空字符串写入到数据库 或 拼接成SQL语句
     *      2. 其它类型的值，以NULL值写入到数据库。
     * 
     * 默认为：false。
     */
    private boolean                   defaultNull;
    
    
    
    /**
     * 构造器
     */
    public DBSQL()
    {
        this.sqlText      = "";
        this.sqlType      = $DBSQL_TYPE_UNKNOWN;
        this.sqlTableName = null;
        this.segments     = new ArrayList<DBSQL_Split>();
        this.preparedSQL  = new DBPreparedSQL();
        this.safeCheck    = true;
        this.conditions   = new HashMap<String ,DBConditions>();
        this.defaultNull  = false;
        this.setNotPlaceholders("MI,SS,mi,ss");
        this.setKeyReplace(true);
    }
    
    
    
    /**
     * 构造器
     * 
     * @param i_SQLText  完整的原始SQL文本
     */
    public DBSQL(String i_SQLText)
    {
        this();
        this.setSqlText(i_SQLText);
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
            v_Info = v_Info.replaceFirst("<\\[" ,"");
            v_Info = v_Info.replaceFirst("\\]>" ,"");
            
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
        
        // 将INSERT ... SELECT语句也认定为查询语句，这样做是为了保证动态占位符功能  ZhengWei(HY) Add 2018-05-11
        /*
        v_Pattern = Pattern.compile("^( )*[Ii][Nn][Ss][Ee][Rr][Tt][\\s\\S]+[Ss][Ee][Ll][Ee][Cc][Tt][ ]+");
        v_Matcher = v_Pattern.matcher(this.sqlText);
        if ( v_Matcher.find() )
        {
            this.sqlType = $DBSQL_TYPE_SELECT;
            return;
        }
        */
        
        v_Pattern = Pattern.compile("^( )*[Ii][Nn][Ss][Ee][Rr][Tt][ ]+");
        v_Matcher = v_Pattern.matcher(this.sqlText);
        if ( v_Matcher.find() )
        {
            this.sqlType      = $DBSQL_TYPE_INSERT;
            this.sqlTableName = parserTableNameByInsert(this.sqlText);
            return;
        }
        
        
        v_Pattern = Pattern.compile("^( )*[Uu][Pp][Dd][Aa][Tt][Ee][ ]+");
        v_Matcher = v_Pattern.matcher(this.sqlText);
        if ( v_Matcher.find() )
        {
            this.sqlType      = $DBSQL_TYPE_UPDATE;
            this.sqlTableName = parserTableNameByUpdate(this.sqlText);
            return;
        }
        
        
        v_Pattern = Pattern.compile("^( )*[Dd][Ee][Ll][Ee][Tt][Ee][ ]+");
        v_Matcher = v_Pattern.matcher(this.sqlText);
        if ( v_Matcher.find() )
        {
            this.sqlType = $DBSQL_TYPE_DELETE;
            return;
        }
        
        
        String v_SQLText = this.sqlText.toUpperCase().trim();
        v_SQLText = " " + StringHelp.replaceAll(v_SQLText ,new String[] {"\n" ,"\r" ,"\t" ,"(" ,")"} ,new String [] {" "});
        if ( StringHelp.isContains(v_SQLText ,true ," WITH " ," AS ") )
        {
            if ( StringHelp.isContains(v_SQLText ," DELETE ") )
            {
                this.sqlType = $DBSQL_TYPE_DELETE;
                return;
            }
            else if ( StringHelp.isContains(v_SQLText ," UPDATE ") )
            {
                this.sqlType      = $DBSQL_TYPE_UPDATE;
                this.sqlTableName = parserTableNameByUpdate(this.sqlText);
                return;
            }
            else if ( StringHelp.isContains(v_SQLText ," INSERT ") )
            {
                this.sqlType      = $DBSQL_TYPE_INSERT;
                this.sqlTableName = parserTableNameByInsert(this.sqlText);
                return;
            }
            else
            {
                this.sqlType = $DBSQL_TYPE_SELECT;
                return;
            }
        }
    }
    
    
    
    /**
     * 解析Insert SQL语句的表名称
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-18
     * @version     v1.0
     *
     * @param i_SQL
     * @return
     */
    public static String parserTableNameByInsert(String i_SQL)
    {
        String    v_SQL    = StringHelp.replaceAll(Help.NVL(i_SQL).trim() ,$ReplaceKeys).toUpperCase();
        String [] v_SQLArr = v_SQL.split(" ");
        boolean   v_Insert = false;
        boolean   v_Into   = false;
        
        for (String v_Item : v_SQLArr)
        {
            if ( Help.isNull(v_Item) )
            {
                continue;
            }
            else if ( v_Into )
            {
                return v_Item;
            }
            else if ( v_Insert )
            {
                if ( $Into.equals(v_Item) )
                {
                    v_Into = true;
                }
            }
            else if ( $Insert.equals(v_Item) )
            {
                v_Insert = true;
            }
            else
            {
                v_Insert = false;
            }
        }
        
        return null;
    }
    
    
    
    /**
     * 解析Update SQL语句的表名称
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-07-18
     * @version     v1.0
     *
     * @param i_SQL
     * @return
     */
    public static String parserTableNameByUpdate(String i_SQL)
    {
        String    v_SQL        = StringHelp.replaceAll(Help.NVL(i_SQL).trim() ,$ReplaceKeys).toUpperCase();
        String [] v_SQLArr     = v_SQL.split(" ");
        int       v_FromIndex  = v_SQL.indexOf($From);
        int       v_WhereIndex = v_SQL.indexOf($Where);
        boolean   v_HaveFrom   = v_FromIndex > 0 && (v_FromIndex < v_WhereIndex || v_WhereIndex <= 0);
        boolean   v_Update     = false;
        boolean   v_From       = false;
        String    v_TableName  = null;
        
        for (String v_Item : v_SQLArr)
        {
            if ( Help.isNull(v_Item) )
            {
                continue;
            }
            else if ( v_From )
            {
                return v_Item;
            }
            else if ( v_Update )
            {
                if ( $From.equals(v_Item) )
                {
                    v_From = true;
                }
                else if ( !v_HaveFrom )
                {
                   return v_Item;
                }
            }
            else if ( $Update.equals(v_Item) )
            {
                v_Update = true;
            }
        }
        
        return v_TableName;
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
     * 入参类型是Map时，在处理NULL与入参类型是Object，是不同的。
     *   1. Map填充为""空的字符串。
     *   2. Object填充为 "NULL" ，可以支持空值针的写入。
     * 
     *   但上方两种均可以通过配置<condition><name>占位符名称<name></condition>，向数据库写入空值针。
     * 
     * @param i_Obj
     * @return
     */
    public String getSQL(Object i_Obj)
    {
        return this.getSQL(i_Obj ,this.dsg);
    }
    
    
    
    /**
     * 获取可执行的SQL语句，并按 i_Obj 填充有数值。
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
    public String getSQL(Object i_Obj ,DataSourceGroup i_DSG)
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
            return this.getSQL((Map<String ,?>)i_Obj ,i_DSG);
        }
        
        String v_DBType = null;
        if ( i_DSG != null )
        {
            v_DBType = i_DSG.getDbProductType();
        }
        
        StringBuilder         v_SQL     = new StringBuilder();
        Iterator<DBSQL_Split> v_Ierator = this.segments.iterator();
        
        
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
                                // 占位符取值条件  ZhengWei(HY) Add 2018-08-10
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
                            v_GetterValue = Help.getValueIgnoreCase(DBSQLStaticParams.getInstance() ,v_PlaceHolder);
                        }
                    }
                    catch (Exception exce)
                    {
                        exce.printStackTrace();
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
                                while ( v_Info.indexOf(":" + v_PlaceHolder) >= 0 )
                                {
                                    // 可实现SQL中的占位符，通过Java动态(或有业务时间逻辑的)填充值。 ZhengWei(HY) Add 2016-03-18
                                    Object v_MRValue = ((MethodReflect)v_GetterValue).invoke();
                                    
                                    if ( v_MRValue != null )
                                    {
                                        if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_MRValue.toString()) )
                                        {
                                            if ( v_IsCValue )
                                            {
                                                v_Info = this.dbSQLFill.onlyFillFirst(v_Info ,v_PlaceHolder ,v_MRValue.toString() ,v_DBType);
                                            }
                                            else
                                            {
                                                v_Info = this.dbSQLFill.fillFirst(v_Info ,v_PlaceHolder ,v_MRValue.toString() ,v_DBType);
                                            }
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
                                        v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
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
                                if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_GetterValue.toString()) )
                                {
                                    if ( v_IsCValue )
                                    {
                                        v_Info = this.dbSQLFill.onlyFillAll(v_Info ,v_PlaceHolder ,v_GetterValue.toString() ,v_DBType);
                                    }
                                    else
                                    {
                                        v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_GetterValue.toString() ,v_DBType);
                                    }
                                    v_ReplaceCount++;
                                }
                                else
                                {
                                    throw new DBSQLSafeException(this.getSqlText());
                                }
                            }
                        }
                        // 当占位符对应属性值为NULL时的处理
                        else
                        {
                            String v_Value = null;
                            if ( v_ConditionGroup != null || this.defaultNull )
                            {
                                // 占位符取值条件。可实现NULL值写入到数据库的功能  ZhengWei(HY) Add 2018-08-10
                                v_Value = $NULL;
                                v_Info  = this.dbSQLFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
                            }
                            else if ( v_MethodReflect == null )
                            {
                                v_Value = $NULL;
                                v_Info  = this.dbSQLFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
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
                                    v_Info  = this.dbSQLFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
                                }
                                
                                // 2018-11-02 Del  废除默认值填充方式
                                // v_Value = Help.toObject(v_MethodReflect.getReturnType()).toString();
                            }
                            
                            // 这里必须再执行一次填充。因为第一次为 fillMark()，本次为 fillAll() 方法
                            v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
                            
                            // v_ReplaceCount++; 此处不要++，这样才能实现动态占位符的功能。
                            // 上面的代码同时也替换占位符，可对不是动态占位符的情况，也初始化值。  ZhengWei(HY) 2018-06-06
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
                    
                    if ( v_MethodReflect != null )
                    {
                        v_MethodReflect.clearDestroy();
                        v_MethodReflect = null;
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
     * 入参类型是Map时，在处理NULL与入参类型是Object，是不同的。
     *   1. Map填充为""空的字符串。
     *   2. Object填充为 "NULL" ，可以支持空值针的写入。
     * 
     *   但上方两种均可以通过配置<condition><name>占位符名称<name></condition>，向数据库写入空值针。
     * 
     * @param i_Obj
     * @return
     */
    public String getSQL(Map<String ,?> i_Values)
    {
        return this.getSQL(i_Values ,this.dsg);
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
     * @param i_DSG  数据库连接池组。可为空或NULL
     * @return
     */
    public String getSQL(Map<String ,?> i_Values ,DataSourceGroup i_DSG)
    {
        if ( i_Values == null )
        {
            return null;
        }
        
        if ( Help.isNull(this.segments) )
        {
            return this.sqlText;
        }
        
        String v_DBType = null;
        if ( i_DSG != null )
        {
            v_DBType = i_DSG.getDbProductType();
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
                        Object       v_MapValue       = null;
                        DBConditions v_ConditionGroup = Help.getValueIgnoreCase(this.conditions ,v_PlaceHolder);
                        boolean      v_IsCValue       = false;
                        if ( v_ConditionGroup != null )
                        {
                            // 占位符取值条件  ZhengWei(HY) Add 2018-08-10
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
                            v_MapValue = Help.getValueIgnoreCase(DBSQLStaticParams.getInstance() ,v_PlaceHolder);
                        }
                        
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
                                            if ( v_IsCValue )
                                            {
                                                v_Info = this.dbSQLFill.onlyFillFirst(v_Info ,v_PlaceHolder ,v_GetterValue.toString() ,v_DBType);
                                            }
                                            else
                                            {
                                                v_Info = this.dbSQLFill.fillFirst(v_Info ,v_PlaceHolder ,v_GetterValue.toString() ,v_DBType);
                                            }
                                            v_IsReplace = true;
                                        }
                                        else
                                        {
                                            throw new DBSQLSafeException(this.getSqlText());
                                        }
                                    }
                                    else
                                    {
                                        String v_Value = null;
                                        if ( v_ConditionGroup != null || this.defaultNull )
                                        {
                                            // 占位符取值条件。可实现NULL值写入到数据库的功能  ZhengWei(HY) Add 2018-08-10
                                            v_Value = $NULL;
                                            v_Info = this.dbSQLFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
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
                                                v_Info = this.dbSQLFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
                                            }
                                            
                                            // 2018-11-02 Del  废除默认值填充方式
                                            // v_Value = Help.toObject(((MethodReflect)v_MapValue).getReturnType()).toString();
                                        }
                                        
                                        // 这里必须再执行一次填充。因为第一次为 fillMark()，本次为 fillAll() 方法
                                        v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
                                        
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
                                if ( !this.isSafeCheck() || DBSQLSafe.isSafe(v_MapValue.toString()) )
                                {
                                    if ( v_IsCValue )
                                    {
                                        v_Info = this.dbSQLFill.onlyFillAll(v_Info ,v_PlaceHolder ,v_MapValue.toString() ,v_DBType);
                                    }
                                    else
                                    {
                                        v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_MapValue.toString() ,v_DBType);
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
                                if ( v_ConditionGroup != null || this.defaultNull )
                                {
                                    // 占位符取值条件。可实现NULL值写入到数据库的功能  ZhengWei(HY) Add 2018-08-10
                                    String v_Value = $NULL;
                                    v_Info = this.dbSQLFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
                                    v_Info = this.dbSQLFill.fillAll    (v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
                                }
                                else
                                {
                                    v_Info = this.dbSQLFill.fillSpace(v_Info ,v_PlaceHolder);
                                }
                                v_ReplaceCount++;
                            }
                            else
                            {
                                String v_Value = null;
                                if ( v_ConditionGroup != null || this.defaultNull )
                                {
                                    // 占位符取值条件。可实现NULL值写入到数据库的功能  ZhengWei(HY) Add 2018-08-10
                                    v_Value = $NULL;
                                    v_Info = this.dbSQLFill.fillAllMark(v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
                                }
                                else
                                {
                                    v_Value = "";
                                }
                                
                                // 这里必须再执行一次填充。因为第一次为 fillMark()，本次为 fillAll() 方法
                                v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_Value ,v_DBType);
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
            DBSQL_Split                   v_DBSQL_Segment = v_Ierator.next();
            PartitionMap<String ,Integer> v_Placeholders  = v_DBSQL_Segment.getPlaceholders();
            
            if ( Help.isNull(v_Placeholders) )
            {
                v_SQL.append(v_DBSQL_Segment.getInfo());
            }
            else
            {
                String v_Info         = v_DBSQL_Segment.getInfo();
                int    v_ReplaceCount = 0;
                int    v_PSize        = v_Placeholders.rowCount();
                int    v_PBegin       = v_Ret.getPlaceholders().size();
                
                // 先初始化集合中的所有元素
                for (int i=1; i<=v_PSize; i++)
                {
                    v_Ret.getPlaceholders().add("");
                }
                
                for (Map.Entry<String ,List<Integer>> v_PlaceholderIndexes : v_Placeholders.entrySet() )
                {
                    for (Integer v_PIndex : v_PlaceholderIndexes.getValue())
                    {
                        String v_PKey      = ":" + v_PlaceholderIndexes.getKey();
                        String v_PKey2     = "'" + v_PKey + "'";
                        int    v_PKeyFind  = v_Info.indexOf(v_PKey);
                        int    v_PKeyFind2 = v_Info.indexOf(v_PKey2);
                        
                        // 支持 ':占位符'  形式的预解释
                        if ( v_PKeyFind2 >= 0 && v_PKeyFind2 + 1 == v_PKeyFind )
                        {
                            v_PKey = v_PKey2;
                        }
                        
                        v_Info = StringHelp.replaceFirst(v_Info ,v_PKey ,"?");
                        v_Ret.getPlaceholders().set(v_PBegin + v_PIndex ,v_PlaceholderIndexes.getKey());
                        v_ReplaceCount++;
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
        return this.getSQL(this.dsg);
    }
    
    
    
    /**
     * 获取可执行的SQL语句，无填充项的情况。
     * 
     * @param i_DSG  数据库连接池组。可为空或NULL
     * @return
     */
    public String getSQL(DataSourceGroup i_DSG)
    {
        if ( Help.isNull(this.segments) )
        {
            return this.sqlText;
        }
        
        String v_DBType = null;
        if ( i_DSG != null )
        {
            v_DBType = i_DSG.getDbProductType();
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
                        Object v_MapValue = null;
                        
                        // 全局占位符 ZhengWei(HY) Add 2019-03-06
                        if ( v_MapValue == null )
                        {
                            v_MapValue = Help.getValueIgnoreCase(DBSQLStaticParams.getInstance() ,v_PlaceHolder);
                        }
                        
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
                                            v_Info = this.dbSQLFill.fillFirst(v_Info ,v_PlaceHolder ,v_GetterValue.toString() ,v_DBType);
                                            v_IsReplace = true;
                                        }
                                        else
                                        {
                                            throw new DBSQLSafeException(this.getSqlText());
                                        }
                                    }
                                    else
                                    {
                                        // 因为没有执行参数，所以不做任何替换  2019-03-13
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
                                    v_Info = this.dbSQLFill.fillAll(v_Info ,v_PlaceHolder ,v_MapValue.toString() ,v_DBType);
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
                            // 因为没有执行参数，所以不做任何替换  2019-03-13
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
    
    
    
    public int getSQLType()
    {
        return sqlType;
    }
    
    
    
    /**
     * 获取：SQL语句操作的表名称。用于Insert、Update语句
     */
    public String getSqlTableName()
    {
        return sqlTableName;
    }
    

    
    /**
     * 设置：SQL语句操作的表名称。用于Insert、Update语句
     * 
     * @param sqlTableName
     */
    public void setSqlTableName(String sqlTableName)
    {
        this.sqlTableName = sqlTableName;
    }
    
    

    /**
     * 获取：替换数据库关键字。如，单引号替换成两个单引号。默认为：true，即替换
     */
    public boolean isKeyReplace()
    {
        return keyReplace;
    }


    
    /**
     * 设置：替换数据库关键字。如，单引号替换成两个单引号。默认为：true，即替换
     * 
     * 采用类似工厂方法构造 DBSQLFill，惟一的目的就是为了生成SQL时，减少IF判断，提高速度。
     * 
     * @param i_KeyReplace
     */
    public void setKeyReplace(boolean i_KeyReplace)
    {
        if ( i_KeyReplace )
        {
            this.dbSQLFill = DBSQLFillKeyReplace.getInstance(this.notKeyReplace);
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
     * 获取：当this.keyReplace=true时有效。表示个别不替换数据库关键字的占位符。前缀无须冒号
     */
    public Set<String> getNotKeyReplaceSet()
    {
        return notKeyReplace;
    }
    

    
    /**
     * 设置：当this.keyReplace=true时有效。表示个别不替换数据库关键字的占位符。前缀无须冒号
     * 
     * @param notKeyReplace
     */
    public void setNotKeyReplaceSet(Set<String> notKeyReplace)
    {
        this.notKeyReplace = notKeyReplace;
    }
    
    
    
    /**
     * 设置：当this.keyReplace=true时有效。表示个别不替换数据库关键字的占位符。前缀无须冒号。
     * 
     * @param notKeyReplaces  多个间用,逗号分隔
     */
    public void setNotKeyReplaces(String notKeyReplaces)
    {
        this.notKeyReplace = new HashSet<String>();
        
        String [] v_Arr = notKeyReplaces.split(",");
        if ( !Help.isNull(v_Arr) )
        {
            for (String v_NotKeyReplace : v_Arr)
            {
                this.notKeyReplace.add(v_NotKeyReplace.trim());
            }
        }
        
        this.setKeyReplace(this.isKeyReplace());
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
     * @createDate  2018-08-10
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
     * @createDate  2019-01-20
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
     * @createDate  2019-01-19
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
     * 获取：数据库连接池组
     */
    public DataSourceGroup getDataSourceGroup()
    {
        return dsg;
    }


    
    /**
     * 设置：数据库连接池组
     * 
     * @param i_DataSourceGroup
     */
    public void setDataSourceGroup(DataSourceGroup i_DataSourceGroup)
    {
        this.dsg = i_DataSourceGroup;
    }



    @Override
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
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    public String fillFirst(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType);
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符。
     * 
     * 替换公式：i_Info.replaceAll(":" + i_PlaceHolder , i_Value.replaceAll("'" ,"''"));
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    public String fillAll(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType);
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符（前后带单引号的替换）
     * 
     * 替换公式：i_Info.replaceAll("':" + i_PlaceHolder + "'", i_Value.replaceAll("'" ,"''"));
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-10
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    public String fillAllMark(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType);
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换首个占位符
     * 
     * 只填充，不替换特殊字符。主要用于 “条件DBConditions” ，条件中的数值交由开发者来决定
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-10-11
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    public String onlyFillFirst(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType);
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符。
     * 
     * 替换公式：i_Info.replaceAll(":" + i_PlaceHolder , i_Value);
     * 
     * 只填充，不替换特殊字符。主要用于 “条件DBConditions” ，条件中的数值交由开发者来决定
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-10-11
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    public String onlyFillAll(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType);
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符（前后带单引号的替换）
     * 
     * 替换公式：i_Info.replaceAll("':" + i_PlaceHolder + "'", i_Value.replaceAll("'" ,"''"));
     * 
     * 只填充，不替换特殊字符。主要用于 “条件DBConditions” ，条件中的数值交由开发者来决定
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-10-11
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    public String onlyFillAllMark(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType);
    
    
    
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
class DBSQLFillDefault implements DBSQLFill ,Serializable
{
    private static final long serialVersionUID = -8568480897505758512L;
    
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
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String fillFirst(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        try
        {
            return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,i_Value);
        }
        catch (Exception exce)
        {
            return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(i_Value));
        }
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换首个占位符
     * 
     * 只填充，不替换特殊字符。主要用于 “条件DBConditions” ，条件中的数值交由开发者来决定
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-10-11
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String onlyFillFirst(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        return fillFirst(i_Info ,i_PlaceHolder ,i_Value ,i_DBType);
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符
     * 
     * 替换公式：i_Info.replaceAll(":" + i_PlaceHolder , i_Value.replaceAll("'" ,"''"));
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String fillAll(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        try
        {
            return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,i_Value);
        }
        catch (Exception exce)
        {
            return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(i_Value));
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
     * @createDate  2019-10-11
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String onlyFillAll(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        return fillAll(i_Info ,i_PlaceHolder ,i_Value ,i_DBType);
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符（前后带单引号的替换）
     * 
     * 替换公式：i_Info.replaceAll("':" + i_PlaceHolder + "'", i_Value.replaceAll("'" ,"''"));
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-10
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String fillAllMark(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        try
        {
            return StringHelp.replaceAll(i_Info ,"':" + i_PlaceHolder + "'" ,i_Value);
        }
        catch (Exception exce)
        {
            return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(i_Value));
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
     * @createDate  2019-10-11
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String onlyFillAllMark(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        return fillAllMark(i_Info ,i_PlaceHolder ,i_Value ,i_DBType);
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
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
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
 *              v2.0  2019-05-08  添加：对MySQL数据库添加\号的转义。\号本身就是MySQL数据库的转义符。
 *                                      但写入文本信息时，\号多数时是想被直接当普通符号写入到数据库中。
 *              v3.0  2019-08-23  添加：是否允许替换字符串。防止如：'A' ,'B' ,'C' ... ,'Z'  这样格式的字符串被替换
 *                                      一般用于由外界动态生成的在 IN 语法中，如 IN ('A' ,'B' ,'C' ... ,'Z')，此时这里的单引号就不应被替换。
 */
class DBSQLFillKeyReplace implements DBSQLFill ,Serializable
{
    private static final long serialVersionUID = 3135504177775635847L;

    public  static final String    $FillReplace         = "'";
    
    public  static final String    $FillReplaceBy       = "''";
    
    public  static final String [] $FillReplace_MySQL   = {"'"  ,"\\"};    // MySQL 数据库中的 \ 号是转义符
    
    public  static final String [] $FillReplaceBy_MySQL = {"''" ,"\\\\"};
    
    private static       DBSQLFill $MySelf;
    
    
    /** 表示个别不替换数据库关键字的占位符。前缀无须冒号 */
    private Set<String> notKeyReplace;
    
    
    
    public synchronized static DBSQLFill getInstance(Set<String> i_NotKeyReplace)
    {
        if ( !Help.isNull(i_NotKeyReplace) )
        {
            return new DBSQLFillKeyReplace(i_NotKeyReplace);
        }
        
        if ( $MySelf == null )
        {
            $MySelf = new DBSQLFillKeyReplace();
        }
        
        return $MySelf;
    }
    
    
    private DBSQLFillKeyReplace()
    {
        this(null);
    }
    
    
    private DBSQLFillKeyReplace(Set<String> i_NotKeyReplace)
    {
        this.notKeyReplace = i_NotKeyReplace;
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
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String fillFirst(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        try
        {
            if ( (this.notKeyReplace == null || !this.notKeyReplace.contains(i_PlaceHolder)) && this.isAllowReplace(i_Value) )
            {
                if ( DataSourceGroup.$DBType_MySQL.equals(i_DBType) )
                {
                    return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,StringHelp.replaceAll(i_Value ,$FillReplace_MySQL ,$FillReplaceBy_MySQL));
                }
                else
                {
                    return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,StringHelp.replaceAll(i_Value ,$FillReplace ,$FillReplaceBy));
                }
            }
            else
            {
                return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,i_Value);
            }
        }
        catch (Exception exce)
        {
            if ( (this.notKeyReplace == null || !this.notKeyReplace.contains(i_PlaceHolder)) && this.isAllowReplace(i_Value) )
            {
                if ( DataSourceGroup.$DBType_MySQL.equals(i_DBType) )
                {
                    return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(StringHelp.replaceAll(i_Value ,$FillReplace_MySQL ,$FillReplaceBy_MySQL)));
                }
                else
                {
                    return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(StringHelp.replaceAll(i_Value ,$FillReplace ,$FillReplaceBy)));
                }
            }
            else
            {
                return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(i_Value));
            }
        }
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换首个占位符
     * 
     * 只填充，不替换特殊字符。主要用于 “条件DBConditions” ，条件中的数值交由开发者来决定
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-10-11
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String onlyFillFirst(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        try
        {
            return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,i_Value);
        }
        catch (Exception exce)
        {
            return StringHelp.replaceFirst(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(i_Value));
        }
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符
     * 
     * 替换公式：i_Info.replaceAll(":" + i_PlaceHolder , i_Value.replaceAll("'" ,"''"));
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-08-09
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String fillAll(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        try
        {
            if ( (this.notKeyReplace == null || !this.notKeyReplace.contains(i_PlaceHolder)) && this.isAllowReplace(i_Value) )
            {
                if ( DataSourceGroup.$DBType_MySQL.equals(i_DBType) )
                {
                    return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,StringHelp.replaceAll(i_Value ,$FillReplace_MySQL ,$FillReplaceBy_MySQL));
                }
                else
                {
                    return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,StringHelp.replaceAll(i_Value ,$FillReplace ,$FillReplaceBy));
                }
            }
            else
            {
                return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,i_Value);
            }
        }
        catch (Exception exce)
        {
            if ( (this.notKeyReplace == null || !this.notKeyReplace.contains(i_PlaceHolder)) && this.isAllowReplace(i_Value) )
            {
                if ( DataSourceGroup.$DBType_MySQL.equals(i_DBType) )
                {
                    return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(StringHelp.replaceAll(i_Value ,$FillReplace_MySQL ,$FillReplaceBy_MySQL)));
                }
                else
                {
                    return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(StringHelp.replaceAll(i_Value ,$FillReplace ,$FillReplaceBy)));
                }
            }
            else
            {
                return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(i_Value));
            }
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
     * @createDate  2019-10-11
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String onlyFillAll(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        try
        {
            return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,i_Value);
        }
        catch (Exception exce)
        {
            return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,Matcher.quoteReplacement(i_Value));
        }
    }
    
    
    
    /**
     * 将数值(i_Value)中的单引号替换成两个单引号后，再替换所有相同的占位符（前后带单引号的替换）
     * 
     * 替换公式：i_Info.replaceAll("':" + i_PlaceHolder + "'", i_Value.replaceAll("'" ,"''"));
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-10
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String fillAllMark(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        String v_PH = "':" + i_PlaceHolder + "'";
        
        try
        {
            if ( (this.notKeyReplace == null || !this.notKeyReplace.contains(i_PlaceHolder)) && this.isAllowReplace(i_Value) )
            {
                if ( DataSourceGroup.$DBType_MySQL.equals(i_DBType) )
                {
                    return StringHelp.replaceAll(i_Info ,v_PH ,StringHelp.replaceAll(i_Value ,$FillReplace_MySQL ,$FillReplaceBy_MySQL));
                }
                else
                {
                    return StringHelp.replaceAll(i_Info ,v_PH ,StringHelp.replaceAll(i_Value ,$FillReplace ,$FillReplaceBy));
                }
            }
            else
            {
                return StringHelp.replaceAll(i_Info ,v_PH ,i_Value);
            }
        }
        catch (Exception exce)
        {
            if ( (this.notKeyReplace == null || !this.notKeyReplace.contains(i_PlaceHolder)) && this.isAllowReplace(i_Value) )
            {
                if ( DataSourceGroup.$DBType_MySQL.equals(i_DBType) )
                {
                    return StringHelp.replaceAll(i_Info ,v_PH ,Matcher.quoteReplacement(StringHelp.replaceAll(i_Value ,$FillReplace_MySQL ,$FillReplaceBy_MySQL)));
                }
                else
                {
                    return StringHelp.replaceAll(i_Info ,v_PH ,Matcher.quoteReplacement(StringHelp.replaceAll(i_Value ,$FillReplace ,$FillReplaceBy)));
                }
            }
            else
            {
                return StringHelp.replaceAll(i_Info ,v_PH ,Matcher.quoteReplacement(i_Value));
            }
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
     * @createDate  2019-10-11
     * @version     v1.0
     *
     * @param i_Info
     * @param i_PlaceHolder
     * @param i_Value
     * @param i_DBType       数据库类型。见DataSourceGroup.$DBType_ 前缀的系列常量
     * @return
     */
    @Override
    public String onlyFillAllMark(String i_Info ,String i_PlaceHolder ,String i_Value ,String i_DBType)
    {
        String v_PH = "':" + i_PlaceHolder + "'";
        
        try
        {
            return StringHelp.replaceAll(i_Info ,v_PH ,i_Value);
        }
        catch (Exception exce)
        {
            return StringHelp.replaceAll(i_Info ,v_PH ,Matcher.quoteReplacement(i_Value));
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
    @Override
    public String fillSpace(String i_Info ,String i_PlaceHolder)
    {
        return StringHelp.replaceAll(i_Info ,":" + i_PlaceHolder ,"");
    }
    
    
    
    /**
     * 是否允许替换字符串。防止如：'A' ,'B' ,'C' ... ,'Z'  这样格式的字符串被替换
     * 
     * 一般用于由外界动态生成的在 IN 语法中，如 IN ('A' ,'B' ,'C' ... ,'Z')，此时这里的单引号就不应被替换。
     * 
     * @author      ZhengWei(HY)
     * @createDate  2019-08-23
     * @version     v1.0
     *
     * @param i_Value
     * @return
     */
    private boolean isAllowReplace(String i_Value)
    {
        int v_ACount = StringHelp.getCount(i_Value ,"'");
        
        // 当单引号成对出现时
        if ( v_ACount % 2 == 0 )
        {
            boolean v_StartW = i_Value.trim().startsWith("'");
            boolean v_EndW   = i_Value.trim().endsWith("'");
            
            if ( v_StartW && v_EndW )
            {
                int v_BCount = StringHelp.getCount(i_Value ,",");
                if ( v_ACount / 2 == v_BCount + 1 )
                {
                    // 当单引号成对的个数 = 分号的个数时，不允许作替换动作
                    return false;
                }
            }
            else if ( !v_StartW && !v_EndW )
            {
                int v_BCount = StringHelp.getCount(i_Value ,",");
                if ( v_ACount / 2 == v_BCount )
                {
                    // 当单引号成对的个数 = 分号的个数时，不允许作替换动作
                    return false;
                }
            }
        }
        
        return true;
    }
    
}
