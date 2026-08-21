package org.hy.common.milvus;

import org.hy.common.Date;





/**
 * 防止Content注入攻击
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-19
 * @version     v1.0
 */
public final class MilvusContentSafe
{
    
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
        return true;
    }
    
    
    
    /**
     * 判定SQL是否安全，只判定关系关键字(AND、OR)的情况
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Value  设定字符串均以转为大写
     * @return
     */
    public final static boolean isSafe_Relations(String i_Value ,String i_Key)
    {
        // Nothing. 待未来有需要时再实现
        return true;
    }
    
    
    
    /**
     *判定SQL是否安全，只判定SQL注解符 --
     * 
     * 原先是通过正则表达式来判定。因为能耗而再次改良。
     * 
     * 注：入参为完整的SQL
     * 
     * @author      ZhengWei(HY)
     * @createDate  2026-08-19
     * @version     v1.0
     *
     * @param i_Value
     * @return
     */
    public final static boolean isSafe_SQLComment(String i_Value)
    {
        // Nothing. 待未来有需要时再实现
        return true;
    }
    
    
    
    /**
     * 生成攻击日志
     */
    public final static String sqlAttackLog(String i_Value)
    {
        return "\n\n" + Date.getNowTime().getFull() + " Milvus Content attack: " + i_Value + "\n\n";
    }
    
    
    
    private MilvusContentSafe()
    {
        // Nothing.
    }
    
}
