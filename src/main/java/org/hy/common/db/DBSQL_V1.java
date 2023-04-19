package org.hy.common.db;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hy.common.Help;
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
 * @version     v1.0
 * @createDate  2012-10-31
 */
@Deprecated
public class DBSQL_V1
{
    public final static int   $DBSQL_TYPE_UNKNOWN   = -1;
    
    public final static int   $DBSQL_TYPE_SELECT    = 1;
    
    public final static int   $DBSQL_TYPE_INSERT    = 2;
    
    public final static int   $DBSQL_TYPE_UPDATE    = 3;
    
    public final static int   $DBSQL_TYPE_DELETE    = 4;
    
    public final static int   $DBSQL_TYPE_CALL      = 5;
    
    public final static int   $DBSQL_TYPE_DDL       = 6;
    
    
    
    /** 占位符SQL */
    private String            sqlText;
    
    /** SQL类型 */
    private int               sqlType;
    
    /** 通过分析后的分段SQL信息 */
    private List<DBSQL_Split_V1> sqlSplitList;
    
    /** 分拆SQL的最后一段的SQL文本 */
    private String            lastPartSQL;
    
    
    
    /**
     * 构造器
     */
    public DBSQL_V1()
    {
        this.sqlText      = "";
        this.sqlType      = $DBSQL_TYPE_UNKNOWN;
        this.sqlSplitList = new ArrayList<DBSQL_Split_V1>();
        this.lastPartSQL  = "";
    }
    
    
    
    /**
     * 构造器
     * 
     * @param i_SQLText  完整的原始SQL文本
     */
    public DBSQL_V1(String i_SQLText)
    {
        this.sqlType = $DBSQL_TYPE_UNKNOWN;
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
        
        
        // 占位符的识别
        Pattern v_Pattern        = Pattern.compile("[ (,='%_\\n]:\\w+[ ),='%_\\n]");
        Matcher v_Matcher        = v_Pattern.matcher(this.sqlText);
        int     v_SQLIndex       = 0;
        int     v_MatcheEndIndex = 0;
        
        while ( v_Matcher.find() )
        {
            int    v_StartIndex = v_Matcher.start() + 1;
            int    v_EndIndex   = v_Matcher.end()   - 1;
            String v_MetcheStr  = this.sqlText.substring(v_StartIndex ,v_EndIndex);
            String v_SQLSplit   = this.sqlText.substring(v_MatcheEndIndex ,v_Matcher.end() - v_Matcher.group().length() + 1);
            
            //System.out.println("Match [" + v_MetcheStr + "] at positions " + v_StartIndex + "-" + v_EndIndex);
            
            this.put(new DBSQL_Split_V1(v_SQLIndex ,v_SQLSplit ,v_MetcheStr.substring(1) ,v_StartIndex ,v_EndIndex));
            
            v_MatcheEndIndex = v_EndIndex;
            v_SQLIndex++;
        }
        
        
        if ( v_MatcheEndIndex < this.sqlText.length() )
        {
            this.setLastPartSQL(this.sqlText.substring(v_MatcheEndIndex));
        }
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
     * 添加SQL分段（私有）
     * 
     * @param i_DBSQL_Split
     */
    private void put(DBSQL_Split_V1 i_DBSQL_Split)
    {
        this.sqlSplitList.add(i_DBSQL_Split);
    }
    
    
    
    /**
     * 分拆SQL的最后一段的SQL文本（私有）
     * 
     * @param i_DBSQL_Split
     */
    private void setLastPartSQL(String i_LastPartSQL)
    {
        this.lastPartSQL = i_LastPartSQL;
    }
    
    
    
    /**
     * 填充或设置占位符SQL
     * 
     * @param sqlText
     */
    public synchronized void setSqlText(String i_SQLText)
    {
        this.sqlText     = Help.NVL(i_SQLText).trim();
        this.lastPartSQL = "";
        
        if ( this.sqlSplitList == null )
        {
            this.sqlSplitList = new ArrayList<DBSQL_Split_V1>();
        }
        else
        {
            this.sqlSplitList.clear();
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
        
        
        if ( this.sqlSplitList.size() == 0 )
        {
            return this.lastPartSQL;
        }
        
        
        if ( i_Obj instanceof Map )
        {
            return this.getSQL((Map<String ,?>)i_Obj);
        }
        
        
        StringBuilder            v_SQL      = new StringBuilder();
        Iterator<DBSQL_Split_V1> v_Ierator  = this.sqlSplitList.iterator();
        boolean               v_UpIsExist   = false;         // 上一个输入性参数是否存在，并有效
        boolean               v_UpUpIsExist = false;         // 上上一个输入性参数是否存在，并有效
        int                   v_SQLIndex    = 0;
        
        
        while ( v_Ierator.hasNext() )
        {
            DBSQL_Split_V1 v_DBSQL_Split = v_Ierator.next();
            
            Method v_Method = MethodReflect.getGetMethod(i_Obj.getClass() ,v_DBSQL_Split.getMatcheInfo() ,true);
            
            if ( v_Method != null )
            {
                try
                {
                    Object v_GetterValue = v_Method.invoke(i_Obj);
                    
                    // getter 方法有返回值时
                    if ( v_GetterValue != null )
                    {
                        if ( v_UpIsExist || v_SQLIndex == 0 )
                        {
                            v_SQL.append(v_DBSQL_Split.getSqlSplit());
                        }
                        else
                        {
                            v_SQL.append(v_DBSQL_Split.getSqlSplit_Small());
                        }
                        v_SQL.append(v_GetterValue);
                        
                        v_UpIsExist = true;
                    }
                    else
                    {
                        if ( v_DBSQL_Split.getSqlIndex() == 0 )
                        {
                            v_SQL.append(v_DBSQL_Split.getSqlSplit_Small());
                        }
                        
                        v_UpIsExist = false;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            // 没有匹配的 getter 方法
            else
            {
                if ( v_DBSQL_Split.getSqlIndex() == 0 )
                {
                    v_SQL.append(v_DBSQL_Split.getSqlSplit_Small());
                }
                
                v_UpIsExist = false;
            }
            
            
            if ( v_UpUpIsExist && !v_UpIsExist )
            {
                v_SQL.append(v_DBSQL_Split.getSqlSplit().substring(0 ,1));
            }
            
            
            v_SQLIndex++;
        }
        
        
        if ( v_UpIsExist )
        {
            v_SQL.append(this.lastPartSQL);
        }
        
        
        return v_SQL.toString();
    }
    
    
    
    /**
     * 获取可执行的SQL语句，并按 Map<String ,Object> 填充有数值。
     * 
     * Map.key  即为占位符。区分大小写的。
     * 
     * @param i_Obj
     * @return
     */
    public String getSQL(Map<String ,?> i_Values)
    {
        if ( i_Values == null || i_Values.size() == 0 )
        {
            return null;
        }
        
        
        if ( this.sqlSplitList.size() >= 0 )
        {
            // Add 2014-01-27 使用简单替换方式
            //                暂时停用IF语句之后复杂替换方式，因为它还不够完美
            String           v_SQLStr  = this.sqlText;
            Iterator<String> v_MapKeys = i_Values.keySet().iterator();
            while ( v_MapKeys.hasNext() )
            {
                String v_Key = v_MapKeys.next();
                
                v_SQLStr = v_SQLStr.replaceAll(":" + v_Key ,i_Values.get(v_Key).toString());
            }
            
            return v_SQLStr;
        }
        
        
        StringBuilder         v_SQL         = new StringBuilder();
        Iterator<DBSQL_Split_V1> v_Ierator  = this.sqlSplitList.iterator();
        boolean               v_UpIsExist   = false;         // 上一个输入性参数是否存在，并有效
        boolean               v_UpUpIsExist = false;         // 上上一个输入性参数是否存在，并有效
        int                   v_SQLIndex    = 0;
        
        
        while ( v_Ierator.hasNext() )
        {
            DBSQL_Split_V1 v_DBSQL_Split = v_Ierator.next();
            
            v_UpUpIsExist = v_UpIsExist;
            
            if ( i_Values.containsKey(v_DBSQL_Split.getMatcheInfo()) )
            {
                try
                {
                    Object v_Value = i_Values.get(v_DBSQL_Split.getMatcheInfo());
                    
                    // Map.value 非空时
                    if ( v_Value != null )
                    {
                        if ( v_UpIsExist || v_SQLIndex == 0 )
                        {
                            v_SQL.append(v_DBSQL_Split.getSqlSplit());
                        }
                        else
                        {
                            v_SQL.append(v_DBSQL_Split.getSqlSplit_Small());
                        }
                        v_SQL.append(v_Value);
                        
                        v_UpIsExist = true;
                    }
                    else
                    {
                        if ( v_DBSQL_Split.getSqlIndex() == 0 )
                        {
                            v_SQL.append(v_DBSQL_Split.getSqlSplit_Small());
                        }
                        
                        v_UpIsExist = false;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            // 没有匹配的 getter 方法
            else
            {
                if ( v_DBSQL_Split.getSqlIndex() == 0 )
                {
                    v_SQL.append(v_DBSQL_Split.getSqlSplit_Small());
                }
                
                v_UpIsExist = false;
            }
            
            
            if ( v_UpUpIsExist && !v_UpIsExist )
            {
                v_SQL.append(v_DBSQL_Split.getSqlSplit_UpEnd());
            }
            
            
            v_SQLIndex++;
        }
        
        
        if ( v_UpIsExist )
        {
            v_SQL.append(this.lastPartSQL);
        }
        
        
        return v_SQL.toString();
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
        this.sqlSplitList.clear();
        this.sqlSplitList = null;
        
        super.finalize();
    }
    */
    
}
