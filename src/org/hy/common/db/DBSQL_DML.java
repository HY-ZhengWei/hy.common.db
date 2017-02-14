package org.hy.common.db;

import java.util.Map;





public interface DBSQL_DML 
{

	/**
	 * 获取可执行的SQL语句，并按 i_Obj 填充有数值。
	 * 
	 * @param i_Obj
	 * @return
	 */
	public String getSQL(Object i_Obj);
	
	
	
	/**
	 * 获取可执行的SQL语句，并按 Map<String ,Object> 填充有数值。
	 * 
	 * Map.key  即为占位符。区分大小写的。
	 * 
	 * @param i_Obj
	 * @return
	 */
	public String getSQL(Map<String ,?> i_Values);
	
}
