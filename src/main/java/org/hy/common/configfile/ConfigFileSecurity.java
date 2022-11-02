package org.hy.common.configfile;





/**
 * 配置文件转为对象的安全接口。
 * 
 * 主要用于数据库用户密码的加密。
 * 
 * 使用此接口后，还有一个好处：
 *    即，可以将加密算法与解密算法独立分开。
 *       加密算法，在此接口中实现；
 *       解密算法，在生成的对象对应的 getter() 方法中实现。
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0
 * @createDate  2012-10-27
 */
public interface ConfigFileSecurity
{
    
    /**
     * 创建配置文件转换出的对象实例
     * 
     * @return
     */
    public Object newObject();
    
    

    /**
     * 配置文件处理每一行时，在配置数据转为对象之后，将调用此方法。
     * 
     * 将一些特殊的处理动交给使用者自行定义。
     * 
     * @param i_CFO          配置文件转为对象的功能类
     * @param io_Object      配置文件转换出的对象实例
     * @param io_CFORowInfo  基本行级配置信息(输入输出型)
     */
    public void dispose(ConfigFileObject i_CFO ,Object io_Object ,ConfigFileObjectRowInfo io_CFORowInfo);
    
}
