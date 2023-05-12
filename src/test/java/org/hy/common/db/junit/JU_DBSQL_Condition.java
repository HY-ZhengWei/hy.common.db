package org.hy.common.db.junit;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.db.DBCondition;
import org.hy.common.db.DBSQL;
import org.hy.common.xml.log.Logger;
import org.junit.Test;





/**
 * 测试单元：
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-08-10
 * @version     v1.0
 */
public class JU_DBSQL_Condition
{
    private static final Logger $Logger = new Logger(JU_DBSQL_Condition.class ,true);
    
    
    
    public DBSQL createDBSQL()
    {
        DBSQL v_DBSQL = new DBSQL();
        
        v_DBSQL.setSqlText("SELECT * FROM 表名称 WHERE name = ':name' AND age = :age AND city = ':city'");
        v_DBSQL.addCondition(new DBCondition("name" ,":age >= 18"));
        v_DBSQL.addCondition(new DBCondition("age"  ,":age >= 18" ,"100" ,DBCondition.$DefautlTrueValue));
        v_DBSQL.addCondition(new DBCondition("city" ,":age >= 21" ,":SchoolCity" ,":WorkCity"));
        
        return v_DBSQL;
    }
    
    
    
    /**
     * 条件满足测试
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-10
     * @version     v1.0
     *
     */
    @Test
    public void test01()
    {
        Map<String ,Object> v_Params = new HashMap<String ,Object>();
        v_Params.put("nAmE"       ,"ZhengWei");
        v_Params.put("AgE"        ,21);
        v_Params.put("SchoolCity" ,"Xi''An");
        v_Params.put("WORKCITY"   ,"XianYang");
        
        DBSQL v_DBSQL = createDBSQL();
        
        String v_SQL = v_DBSQL.getSQL(v_Params ,null);
        
        $Logger.info("查询参数条件：");
        Help.print(v_Params);
        $Logger.info("占位符取值条件：");
        Help.print(v_DBSQL.getConditions());
        $Logger.info("SQL模板：" + v_DBSQL.getSqlText());
        $Logger.info("运行SQL：" + v_SQL);
        
        assertTrue(v_SQL.indexOf("ZhengWei") >= 0 && v_SQL.indexOf("100") >= 0);
    }
    
    
    
    /**
     * 条件不满足时的NULL测试
     * 
     * @author      ZhengWei(HY)
     * @createDate  2018-08-10
     * @version     v1.0
     *
     */
    @Test
    public void test02()
    {
        Map<String ,Object> v_Params = new HashMap<String ,Object>();
        v_Params.put("nAmE"       ,"ZhengWei");
        v_Params.put("AgE"        ,17);
        v_Params.put("SchoolCity" ,"Xi''An");
        v_Params.put("WORKCITY"   ,"XianYang");
        
        DBSQL v_DBSQL = createDBSQL();
        
        String v_SQL = v_DBSQL.getSQL(v_Params ,null);
        
        $Logger.info("查询参数条件：");
        Help.print(v_Params);
        $Logger.info("占位符取值条件：");
        Help.print(v_DBSQL.getConditions());
        $Logger.info("SQL模板：" + v_DBSQL.getSqlText());
        $Logger.info("运行SQL：" + v_SQL);
        
        assertTrue(v_SQL.indexOf("NULL") >= 0 && v_SQL.indexOf("17") >= 0);
    }
  
}
