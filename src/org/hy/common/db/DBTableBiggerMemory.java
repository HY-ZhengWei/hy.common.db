package org.hy.common.db;





/**
 * 超级大结果集的存储器接口
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-04-10
 */
public interface DBTableBiggerMemory 
{

	/**
	 * 设置每一行的数据
	 * 
	 * @param i_DBRowInfo
	 */
	public void setRowInfo(DBRowInfo i_DBRowInfo);
	
	
	
	/**
	 * 获取行数据存储的表对象
	 * 
	 * @return
	 */
	public Object getTableInfo();
	
}
