package org.hy.common.db;





/**
 * 数据库安全接口。
 * 主要用于数据库用户密码的加密
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-04-27
 */
public interface DBSecurity 
{
	
	/**
	 * 加密
	 * 
	 * @param i_Str  要被加密的字符串
	 * @param i_Key  加密Key
	 * @return
	 */
    public String getEncrypt(String i_Str ,String i_Key);
    
    
    
    /**
     * 解密
     * 
     * @param i_Str  要被解密的字符串
     * @param i_Key  加密Key
     * @return
     */
    public String getDecrypt(String i_Str ,String i_Key);
    
    
    
    /**
     * 获取加密长度
     *  
     * @return
     */
    public int getSecurityLen();
    
}
