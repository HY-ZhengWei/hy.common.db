package org.hy.common.db;

import java.util.regex.Pattern;

import org.hy.common.Date;
import org.hy.common.StringHelp;





/**
 * 防止SQL注入攻击
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-07-31
 * @version     v1.0
 */
public final class DBSQLSafe
{
    
    private static final String  [] $SQLKeys  = {"AND" 
                                                ,"OR" 
                                                ,"UNION" 
                                                ,"EXEC" 
                                                ,"SELECT" 
                                                ,"INSERT"
                                                ,"UPDATE"
                                                ,"DELETE"
                                                ,"TRUNCATE"};
    
    private static final Pattern [] $Patterns = new Pattern[$SQLKeys.length + 1];
    
    
    
    static
    {
        int i = 0;
        
        for (; i<$SQLKeys.length; i++)
        {
            $Patterns[i] = Pattern.compile("(.+)\\s" + StringHelp.toPatternUL($SQLKeys[i]) + "\\s(.+)");
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
        
        for (Pattern v_Pattern : $Patterns)
        {
            v_CheckRet = v_Pattern.matcher(i_Value).find();
            
            if ( v_CheckRet )
            {
                System.err.println(Date.getNowTime().getFull() + " SQL attack: " + i_Value);
                return false;
            }
        }
        
        return true;
    }
    
    
    
    private DBSQLSafe()
    {
        // Nothing.
    }
    
}
