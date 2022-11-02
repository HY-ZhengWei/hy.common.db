package org.hy.common.db;

import java.util.HashMap;





/**
 * 全局占位符。
 * 
 * 作用范围：可作用于所有XSQL对象、DBSQL对象。
 * 
 * 作用功能：用于拼接SQL语句时所用到的全局占位符映射值的集合，与常规占位符一同参与SQL语句的拼接。
 *  
 *  
 * 用自己定义自己类型的单例（懒汉模式），来保证全局对象不为被外界设置成NULL。
 * 
 * 
 * 约定1：DBSQLStaticParams.key的值不包含前缀符:冒号。
 * 
 * 约定2：局部占位符的定义：用户通过方法参数传入的参数，即局部占位符（可以是对象或集合）。
 * 
 * 约定3：局部占位符和全局占位符可以有相同的占位符字符。
 * 
 * 约定4：全局占位符的名称可以不区分大小的配对，但保留区分大小的能力。
 *        即先将区分大小配对，未配对成功时再按不区分大小配对。
 *        举例说明，":ABC"="123"、":abc"="456"，当外界在XML中写:ABC时，优先取"123"拼接SQL。
 *        建议：为了性能，建议开发者保持自己定义的占位符名称，在多地编码的一致性、统一性。
 * 
 * 约定5：相同名称的占位符，其局部占位符优先级高于全局占位符。即，局部占位符 > 全局占位符。
 *        先取局部占位符映射的值，当为NULL时才取全局占位符映射的值。
 *        
 * 约定6：全局占位符相对固定不变，所以它不受DBCondition占位符取值条件的约束。
 *                             
 *
 * @author      ZhengWei(HY)
 * @createDate  2019-03-06
 * @version     v1.0
 */
public class DBSQLStaticParams extends HashMap<String ,Object>
{
    
    private static final long serialVersionUID = -2500848111441877866L;
    
    
    private static DBSQLStaticParams $SQLParams = new DBSQLStaticParams();
    
    
    
    public static DBSQLStaticParams getInstance()
    {
        return $SQLParams;
    }
    
    
    
    private DBSQLStaticParams()
    {
        
    }
    
}
