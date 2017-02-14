package org.hy.common.configfile;

import java.util.Hashtable;
import java.util.Map;

import org.hy.common.Help;





/**
 * 默认访问配置文件读操作的通用实现类
 * 
 * 1. 有回写功能
 * 2. #号注释行
 * 3. 使用=号分隔key与value
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2011-04-17
 */
public class DefaultConfigFile extends ConfigFileReadBase
{
	private Map<String ,String> content;
	
	
	
	public DefaultConfigFile(String i_FileName)
	{
		super(i_FileName);
		this.content = new Hashtable<String ,String>();
	}
	
	
	/**
	 * 读取一行数据
	 * 
	 * @param i_LineNo      行号
	 * @param i_LineString  一行数据
	 */
	public void readLineString(int i_LineNo ,String i_LineString) 
	{		
		if ( !Help.isNull(i_LineString) )
		{
			String v_LineStr       = i_LineString.trim();
			String [] v_LineStrArr = v_LineStr.split("=");
			
			if ( v_LineStrArr.length != 2 )
			{
				this.setStopRead(true);
				throw new ExceptionInInitializerError(ConfigFile_I18N.getHintRigthConfig() + i_LineNo + " = " + i_LineString);
			}
			
			
			try
			{
				this.content.put(v_LineStrArr[0].trim(), v_LineStrArr[1].trim());
			}
			catch (Exception exce)
			{
				this.setStopRead(true);
				throw new ExceptionInInitializerError(ConfigFile_I18N.getHintRigthConfig() + i_LineNo + " = " + i_LineString);
			}
		}
		
	}
	
	
	public Map<? ,?> getContent()
	{
		return this.content;
	}
	
	
	public String getValue(String i_Key)
	{
		if ( Help.isNull(this.content) || Help.isNull(i_Key) )
		{
			return null;
		}
		
		if ( this.content.containsKey(i_Key) )
		{
			return this.content.get(i_Key);
		}
		else
		{
			return null;
		}
	}
	
}
