package org.hy.common.db.junit;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.db.DBPreparedSQL;
import org.hy.common.db.DBSQL;
import org.hy.common.xml.log.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;





@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JU_DBSQL
{
    private static final Logger $Logger = new Logger(JU_DBSQL.class ,true);
    
    
    
    private String    id;
    
    private DBSQLTest inner;
    
    
    
    public String getId()
    {
        return id;
    }


    
    public void setId(String id)
    {
        this.id = id;
    }



    public DBSQLTest getInner()
    {
        return inner;
    }

    

    public void setInner(DBSQLTest inner)
    {
        this.inner = inner;
    }



    @Test
    public void test_XX_YY_WW() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        JU_DBSQL v_Data = new JU_DBSQL();
        
        v_Data.setId("123456");
        v_Data.inner = new DBSQLTest();
        v_Data.inner.setUserName("ZhengWei");
        v_Data.inner.setBeginTime(new Date());
        
        String v_SQL = "UPDATE HY SET Name = ':inner.beginTime' WHERE ID = ':id'";
        DBSQL v_DBSQL = new DBSQL(v_SQL);
        
        $Logger.info(v_DBSQL.getSQL(v_Data ,null));
        $Logger.info(v_DBSQL.getSQL(Help.toMap(v_Data) ,null));
        
        DBPreparedSQL v_PreparedSQL = v_DBSQL.getPreparedSQL();
        $Logger.info(v_PreparedSQL.getSQL());
        Help.print(v_PreparedSQL.getPlaceholders());
    }
    
    
    
    @Test
    public void test_XX01_XX02() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        JU_DBSQL v_Data = new JU_DBSQL();
        
        v_Data.setId("123456");
        v_Data.inner = new DBSQLTest();
        v_Data.inner.setUserName("ZhengWei");
        v_Data.inner.setBeginTime(new Date());
        
        String v_SQL = "UPDATE HY SET Name = ':inner.beginTime' WHERE ID = ':id' AND Name = ':inner.userName' AND Name = '<[:inner.userName]>01' AND Name = '<[:inner.userName]>02'";
        DBSQL v_DBSQL = new DBSQL(v_SQL);
        
        $Logger.info(v_DBSQL.getSQL(v_Data ,null));
        $Logger.info(v_DBSQL.getSQL(Help.toMap(v_Data) ,null));
        
        DBPreparedSQL v_PreparedSQL = v_DBSQL.getPreparedSQL();
        $Logger.info(v_PreparedSQL.getSQL());
        Help.print(v_PreparedSQL.getPlaceholders());
    }
    
    
    
    @Test
    public void test_WhereIn()
    {
        Map<String ,Object> v_Data = new HashMap<String ,Object>();
        
        v_Data.put("tableName"    ,"表名称");
        v_Data.put("assetIDs"     ,"'123' ,'456'");
        v_Data.put("assetState"   ,1);
        v_Data.put("updateUserID" ,"ZhengWei(HY)");
        
        String v_SQL = """
                        UPDATE  :tableName
                           SET  updateTime   = NOW()
                               ,updateUserID = ':updateUserID'
                               ,assetState   = :assetState
                         WHERE  assetID     IN ( :assetIDs )
                       """;
        DBSQL v_DBSQL = new DBSQL(v_SQL);
        $Logger.info(v_DBSQL.getSQL(v_Data ,null));
    }
    
}
