package org.hy.common.configfile;





/**
 * 常量类
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2011-04-19
 */
public class ConfigFile_I18N 
{
	
	private ConfigFile_I18N()
	{
		
	}
	
	
	/**
	 * 获取操作系统的文件路径的分割符
	 * 
	 * @return
	 */
	public static String getSysPathSeparator()
	{
		return System.getProperty("file.separator");
	}
	
	
	/**
	 * 获取操作系统的当前目录
	 * 
	 * @return
	 */
	public static String getSysCurrentPath()
	{
		return System.getProperty("user.dir");
	}
	
	
	/**
	 * 获取操作系统的行分隔符
	 * 
	 * @return
	 */
	public static String getSysLineSeparator()
	{
		return System.getProperty("line.separator");
	}
	
	
	/**
	 * 获取SQL语句间的分隔符
	 * 
	 * @return
	 */
	public static String getSQLSeparator()
	{
		return ";";
	}
	
	
	public static String getHintFileNotExist()
	{
		return "请确认读取目标是否存在:";
	}
	
	
	public static String getHintIsNotFile()
	{
		return "读取目标不是一个文件:";
	}
	
	
	public static String getHintFileNotRead()
	{
		return "读取目标不能被读取:";
	}	
	
	
	public static String getHintRigthConfig()
	{
		return "-- 请正确配置，格式如：key=value。错误所在行：";
	}
	
	
	public static String getHintRigthOSConfig()
	{
		return "-- 请正确配置，格式如：name.key=value。错误所在行：";
	}
	
	
	/**
	 * 将double转化String的格式
	 * 
	 * @return
	 */
	public static String getDecimalFormat()
	{
		return "0.00";
	}
		
}
