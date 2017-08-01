package org.hy.common.db;





/**
 * SQL注入攻击的异常信息 
 *
 * @author      ZhengWei(HY)
 * @createDate  2017-08-01
 * @version     v1.0
 */
public class DBSQLSafeException extends Exception
{
    
    private static final long serialVersionUID = 5044188023025292836L;



    public DBSQLSafeException(String i_Message) 
    {
        super(i_Message);
    }



    public DBSQLSafeException(String i_Message, Throwable i_Cause) 
    {
        super(i_Message ,i_Cause);
    }
    
}
