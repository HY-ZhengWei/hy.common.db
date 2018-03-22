package org.hy.common.db;

import java.util.regex.Pattern;

import org.hy.common.Date;
import org.hy.common.StringHelp;

import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;





/**
 * 防止SQL注入攻击
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-07-31
 * @version     v1.0
 *              v2.0  2018-03-22  1.优化：李浩发现，当SQL占位符的实际填充字符串超级长(如字符串长度为8000)时，
 *                                     正则表达式在匹配时，性能损耗十分严重，CPU使用率可高达100%，并持续5秒左右。
 *                                2.优化：更加精准的判定AND、OR情况一下的SQL注入攻击。
 */
public final class DBSQLSafe
{
    
    private static final String []   $Relation     = {" AND " ," OR "};
    
    private static final String []   $Compares     = {"="  ,"<" ,"<=" ,">=" ,">"};
    
    private static final String []   $Compares_Fel = {"==" ,"<" ,"<=" ,">=" ,">"};
    
    private static final String [][] $SQLKeys      = {{" UNION "   ,"SELECT " ," FROM "} 
                                                     ,{"EXEC "} 
                                                     ,{"SELECT "   ," FROM "} 
                                                     ,{"INSERT "   ,"INTO"}
                                                     ,{"UPDATE "   ," SET "}
                                                     ,{"DELETE "}
                                                     ,{"TRUNCATE " ,"TABLE "}};
    
    private static final Pattern []  $Patterns     = new Pattern[$SQLKeys.length + 1];
    
    private static final FelEngine   $Fel      = new FelEngineImpl();;
    
    
    
    static
    {
        int i = 0;
        
        for (; i<$SQLKeys.length; i++)
        {
            $Patterns[i] = Pattern.compile("(.+)\\s" + $SQLKeys[i][0].trim() + "\\s(.+)");
        }
        
        $Patterns[i] = Pattern.compile("=[^\n]*((')|(--)|(;))");  // 防止注释--、分号
    }
    
    
    
    /**
     * 判定SQL是否安全
     * 
     * @author      ZhengWei(HY)
     * @createDate  2017-07-31
     * @version     v1.0
     *
     * @param i_Value
     * @return
     */
    public final static boolean isSafe(String i_Value)
    {
        boolean v_CheckRet = false;
        int     v_Index    = 0;
        String  v_Value    = i_Value.toUpperCase();
        
        // AND、OR关系关键字的SQL注入攻击判定
        for (String v_Relation : $Relation)
        {
            if ( !isSafe_Relations(v_Value ,v_Relation) )
            {
                System.err.println(Date.getNowTime().getFull() + " SQL attack: " + i_Value);
                return false;
            }
        }
        
        for (; v_Index<$SQLKeys.length; v_Index++)
        {
            // 先采用最高性能的indexOf()方法简单过滤一次，提高匹配性能。
            if ( StringHelp.isContains(v_Value ,true ,true ,$SQLKeys[v_Index]) )
            {
                v_CheckRet = $Patterns[v_Index].matcher(v_Value).find();
                
                if ( v_CheckRet )
                {
                    System.err.println(Date.getNowTime().getFull() + " SQL attack: " + i_Value);
                    return false;
                }
            }
        }
        
        for (; v_Index<$Patterns.length; v_Index++)
        {
            v_CheckRet = $Patterns[v_Index].matcher(v_Value).find();
            
            if ( v_CheckRet )
            {
                System.err.println(Date.getNowTime().getFull() + " SQL attack: " + i_Value);
                return false;
            }
        }
        
        return true;
    }
    
    
    
    /**
     * 判定SQL是否安全，只判定关系关键字(AND、OR)的情况
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-03-22
     * @version     v1.0
     *
     * @param i_Value  设定字符串均以转为大写
     * @return
     */
    public final static boolean isSafe_Relations(String i_Value ,String i_Key)
    {
        int v_IndexOf = i_Value.indexOf(i_Key);
        if ( v_IndexOf < 0 )
        {
            return true;
        }
        
        String v_SubValue = i_Value.substring(v_IndexOf + i_Key.length());
        for (int i=0; i<$Compares.length; i++)
        {
            String [] v_CompareValues = v_SubValue.split($Compares[i]);
            if ( v_CompareValues.length < 2 )
            {
                continue;
            }
            
            String [] v_CVLefts  = v_CompareValues[0].trim().split(" ");
            String    v_CVLeft   = v_CVLefts[v_CVLefts.length - 1];
            String [] v_CVRights = v_CompareValues[1].trim().split(" ");
            String    v_CVRight  = v_CVRights[0];
            
            try
            {
                Object v_FelRet = $Fel.eval(v_CVLeft + " " + $Compares_Fel[i] + " " + v_CVRight);
                if ( (Boolean)v_FelRet )
                {
                    return false;
                }
            }
            catch (Exception exce)
            {
                exce.printStackTrace();
                return true;
            }
        }
        
        return true;
    }
    
    
    
    private DBSQLSafe()
    {
        // Nothing.
    }
    
}
