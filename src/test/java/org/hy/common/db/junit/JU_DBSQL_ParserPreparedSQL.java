package org.hy.common.db.junit;

import org.hy.common.db.DBSQL;
import org.junit.Test;





/**
 * 测试单元：对预解析SQL中占位符顺序的测试。如占位符 :x 在前，那么后面的点位符 :x123 如何能正确填充数值的测试。
 *
 * @author      ZhengWei(HY)
 * @createDate  2020-06-08
 * @version     v1.0
 */
public class JU_DBSQL_ParserPreparedSQL
{
    
    @Test
    public void test_01()
    {
        StringBuilder v_SQL = new StringBuilder();
        
        v_SQL.append("UPDATE  TActivityInfo"); 
        v_SQL.append("   SET  x            = :x"); 
        v_SQL.append("       ,y            = :y"); 
        v_SQL.append("       ,xrouteConfig = :xrouteConfig"); 
        v_SQL.append("       ,xValue       = :x"); 
        v_SQL.append(" WHERE  activityID   = :activityID"); 
        
        
        DBSQL v_DBSQL = new DBSQL(); 
        
        v_DBSQL.setSqlText(v_SQL.toString());
        v_DBSQL.getPreparedSQL();
        
    }
    
}
