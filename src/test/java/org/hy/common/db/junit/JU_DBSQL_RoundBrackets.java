package org.hy.common.db.junit;

import java.util.List;

import org.hy.common.db.DBSQL_RoundBrackets;
import org.hy.common.xml.log.Logger;
import org.junit.Test;





/**
 * 测试单元：圆括号的解释
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-05-11
 * @version     v1.0
 */
public class JU_DBSQL_RoundBrackets
{
    private static final Logger $Logger = new Logger(JU_DBSQL_RoundBrackets.class ,true);
    
    
    
    @Test
    public void test_RoundBrackets()
    {
        StringBuilder v_SQL = new StringBuilder();
        
        v_SQL.append("SELECT  * From (");
        v_SQL.append("SELECT  * ");
        v_SQL.append("  FROM  Dual");
        v_SQL.append(" WHERE  BeginTime = TO_DATE(':BeginTime' ,'YYYY-MM-DD HH24:MI:SS')");
        v_SQL.append("   AND  EndTime   = TO_DATE(':EndTime'   ,'YYYY-MM-DD HH24:MI:SS')");
        v_SQL.append("   AND  Statue    = :Statue").append("\n");
        v_SQL.append("   AND  UserName  = ':UserName'");
        v_SQL.append("   AND  CardNo    = ':CardNo'");
        v_SQL.append("   AND  CityCode  = 0910");
        v_SQL.append(") HY");
        
        List<DBSQL_RoundBrackets> v_RBList = DBSQL_RoundBrackets.parse(v_SQL.toString());
        
        String v_ReverseSQL = DBSQL_RoundBrackets.reverse(v_SQL.toString() ,v_RBList);
        
        $Logger.info(v_ReverseSQL);
    }
    
}
