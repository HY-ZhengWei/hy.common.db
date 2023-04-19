package org.hy.common.db;

import java.util.List;
import java.util.Map;





public class DBSQL_DML_Select implements DBSQL_DML
{
    private List<DBSQL_SelectSegment>  ssList;
    
    
    
    public DBSQL_DML_Select(String i_SQLText)
    {
        this.ssList = DBSQL_SelectSegment.parse(i_SQLText);
    }
    
    
    
    /**
     * 获取可执行的SQL语句，并按 i_Obj 填充有数值。
     * 
     * @param i_Obj
     * @return
     */
    @Override
    public String getSQL(Object i_Obj)
    {
        return null;
    }
    
    
    
    /**
     * 获取可执行的SQL语句，并按 Map<String ,Object> 填充有数值。
     * 
     * Map.key  即为占位符。区分大小写的。
     * 
     * @param i_Obj
     * @return
     */
    @Override
    public String getSQL(Map<String ,?> i_Values)
    {
        return null;
    }
    
    
    
    public List<DBSQL_SelectSegment> getSsList()
    {
        return this.ssList;
    }
    
}
