package org.hy.common.configfile;

import org.hy.common.Help;





/**
 * 配置文件转为对象实例的集合--基本行级配置信息
 * 
 * @author   ZhengWei(HY)
 * @version  V1.0  2012-10-23
 */
public class ConfigFileObjectRowInfo
{
	/** 所在配置文件中的行号(只读) */
	private int lineNo;
	
	/** 配置文件存放格式为：name.key=value 。 此为 name  (只读) */
	private String name;
	
	/** 配置文件存放格式为：name.key=value 。 此为 key   (只读) */
	private String key;
	
	/** 配置文件存放格式为：name.key=value 。 此为 value */
	private String value;
	
	/** value值是否变动过 */
	private boolean valueIsChange;
	
	
	
	public ConfigFileObjectRowInfo(int i_LineNo ,String i_Name ,String i_Key ,String i_Value)
	{
		this.lineNo = i_LineNo;
		this.setName( i_Name);
		this.setKey(  i_Key);
		this.setValue(i_Value);
		this.valueChangeInit();
	}
	
	
	
	public String getKey() 
	{
		return key;
	}

	
	
	private void setKey(String i_Key) 
	{
		if ( Help.isNull(i_Key) )
		{
			throw new NullPointerException("Key is null of Config file object row info.");
		}
		
		this.key = i_Key;
	}

	
	
	public int getLineNo() 
	{
		return lineNo;
	}

	
	
	public String getName() 
	{
		return name;
	}

	
	
	private void setName(String i_Name) 
	{
		if ( Help.isNull(i_Name) )
		{
			throw new NullPointerException("Name is null of Config file object row info.");
		}
		
		this.name = i_Name;
	}

	
	
	public String getValue() 
	{
		return value;
	}

	
	
	public void setValue(String i_Value) 
	{
		if ( i_Value == null )
		{
			this.value = "";
		}
		else
		{
			String v_Value = (i_Value.replace('\n' ,' ')).replace('\r' ,' ');
			
			if ( !v_Value.equals(this.value) )
			{
				this.value         = v_Value;
				this.valueIsChange = true;
			}
		}
	}
	
	
	
	public void valueChangeInit()
	{
		this.valueIsChange = false;
	}
	
	
	
	public boolean isValueChange()
	{
		return this.valueIsChange;
	}
	
	
	
	@Override
	public String toString() 
	{
	    StringBuilder v_Buffer = new StringBuilder();
		
		v_Buffer.append(this.getName());
		v_Buffer.append(".");
		v_Buffer.append(this.getKey());
		v_Buffer.append("=");
		v_Buffer.append(this.getValue());
		
		return v_Buffer.toString();
	}
	
}
