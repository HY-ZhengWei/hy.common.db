package org.hy.common.db.junit;

import java.util.List;

import org.hy.common.db.DBSQL_SelectSegment;
import org.hy.common.xml.log.Logger;
import org.junit.Test;





/**
 * 测试单元：将SQL语句中的所有Select语句及层次解释出来
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-05-12
 * @version     v1.0
 */
public class JU_DBSQL_SelectSegment
{
    private static final Logger $Logger = new Logger(JU_DBSQL_SelectSegment.class ,true);
    
    
    
    @Test
    public void testSelectSegment()
    {
        StringBuilder v_SQL = new StringBuilder();
        
        v_SQL.append("SELECT  * From (");
        v_SQL.append("SELECT  AcceptTime ,Logic_No ,UserName");
        v_SQL.append("  FROM  Dual");
        v_SQL.append(" WHERE  BeginTime = TO_DATE(':BeginTime' ,'YYYY-MM-DD HH24:MI:SS')");
        v_SQL.append("   AND  EndTime   = TO_DATE(':EndTime'   ,'YYYY-MM-DD HH24:MI:SS')");
        v_SQL.append("   AND  Statue    = :Statue").append("\n");
        v_SQL.append("   AND  UserName  = ':UserName'");
        v_SQL.append("   AND  CardNo    = ':CardNo'");
        v_SQL.append("   AND  Logic_No  = (Select Logic_No From T_C_PUB_Product A Where RowNum = 1)");
        v_SQL.append("   AND  CityCode  = 0910");
        v_SQL.append(") HY");
        
        List<DBSQL_SelectSegment> v_SSList = DBSQL_SelectSegment.parse(v_SQL.toString());
        
        String v_ReverseSQL = DBSQL_SelectSegment.reverse(v_SQL.toString() ,v_SSList);
        
        $Logger.info(v_ReverseSQL);
    }
    
}
