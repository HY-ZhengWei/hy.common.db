package org.hy.common.db;

import java.lang.reflect.Method;

import org.hy.common.configfile.ConfigFileObjectRowInfo;
import org.hy.common.configfile.ConfigFileSecurity;

import org.hy.common.configfile.ConfigFileObject;





/**
 * 数据库访问的配置文件的相关操作
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2011-04-17
 */
public class DBConfig extends ConfigFileObject
{
	// 数据库配置文件名称
	public static String DBCONFIGFILE = "dbconfig.ini";
	
	/** 是否启用默认的安全接口 */
	private boolean isDefaultSecurity;
	
	
	
	public DBConfig(String i_FileName)
	{
		super(i_FileName ,DatabaseInfo.class);
		
		this.isDefaultSecurity = false;
		
		this.setCfSecurity(new DBConfigSecurity());
	}
	
	
	
	public boolean isEnableSecurity() 
	{
		return isDefaultSecurity;
	}



	public void setEnableSecurity(boolean i_IsEnable) 
	{
		this.isDefaultSecurity = i_IsEnable;
	}
	
	
	
	public DatabaseInfo getDatabaseInfo()
	{
		return (DatabaseInfo)this.getObject(0);
	}

	
	
	/**
	 * 配置文件中密码安全处理
	 *
	 * @author   ZhengWei(HY)
	 * @version  V1.0  2012-10-29
	 */
	class DBConfigSecurity implements ConfigFileSecurity
	{
		
		public Object newObject() 
		{
			if ( isDefaultSecurity )
			{
				Object v_Security = null;
				
				try
				{ 
					Class<?> v_ClassSecurity  = Class.forName("org.hy.common.security.Security");
					Method   v_MethodSecurity = v_ClassSecurity.getDeclaredMethod("getInstance");
					
					v_Security = v_MethodSecurity.invoke(null);
				}
				catch (Exception exce)
				{
					throw new NullPointerException("");
				}
				
				return new DatabaseInfo((DBSecurity)v_Security);
			}
			else
			{
				return new DatabaseInfo();
			}
		}
		
		
		
		public void dispose(ConfigFileObject i_CFO ,Object io_Object ,ConfigFileObjectRowInfo io_CFORowInfo) 
		{
			if ( "password".equals(io_CFORowInfo.getKey()) )
			{
				DatabaseInfo v_DatabaseInfo = (DatabaseInfo)io_Object;
				
				io_CFORowInfo.setValue(v_DatabaseInfo.getPasswordValue());
			}
		}
		
	}
	
}
