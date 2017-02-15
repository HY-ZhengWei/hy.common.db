package org.hy.common.configfile.junit;

import org.hy.common.Date;
import org.hy.common.configfile.ConfigFileObject;





/**
 * 测试：配置文件转为对象实例的集合
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2012-05-28
 */
public class JU20120528_ConfigFileObject 
{
	private String name;
	
	private int    age;

	
	
	public JU20120528_ConfigFileObject()
	{
		
	}
	
	
	
	public int getAge() 
	{
		return age;
	}

	
	
	public void setAge(int age) 
	{
		this.age = age;
	}

	
	
	public String getName() 
	{
		return name;
	}

	
	
	public void setName(String name) 
	{
		this.name = name;
	}
	
	
	
	public String toString()
	{
		return this.name + "_" + this.age;
	}
	
	
	
	public static void main(String [] args)
	{
		ConfigFileObject v_ConfigFileObject = new ConfigFileObject(JU20120528_ConfigFileObject.class.getResource("JU20120528_ConfigFileObject.ini").getPath() ,JU20120528_ConfigFileObject.class);
		
		try 
		{
			v_ConfigFileObject.read();
			v_ConfigFileObject.setValue("JU02" ,"name" ,"ZhengWei_" + (new Date()).getFull());
			v_ConfigFileObject.write();
			
			System.out.println(v_ConfigFileObject.getObject(0));
			System.out.println(v_ConfigFileObject.getObject(1));
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
}
