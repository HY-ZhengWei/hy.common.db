package org.hy.common.db.junit;

import org.hy.common.db.DBConfig;





/**
 * 测试：数据库访问的配置文件的相关操作
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2012-10-29
 */
public class DBConfigTest 
{

	public static void main(String[] args) 
	{
		String   v_DBConfigFileName = DBConfig.class.getResource(DBConfig.DBCONFIGFILE).getPath();
		
		DBConfig v_DBConfig         = new DBConfig(v_DBConfigFileName);
		
		try 
		{
			v_DBConfig.setEnableSecurity(true);
			v_DBConfig.read();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return;
		}
		
		v_DBConfig = null;
	}

}
