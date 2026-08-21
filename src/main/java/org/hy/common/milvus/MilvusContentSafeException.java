package org.hy.common.milvus;





/**
 * Content注入攻击的异常信息 
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-19
 * @version     v1.0
 */
public class MilvusContentSafeException extends Exception
{
    
    private static final long serialVersionUID = 1496142804131518057L;



    public MilvusContentSafeException(String i_Message) 
    {
        super(i_Message);
    }



    public MilvusContentSafeException(String i_Message, Throwable i_Cause) 
    {
        super(i_Message ,i_Cause);
    }
    
}
