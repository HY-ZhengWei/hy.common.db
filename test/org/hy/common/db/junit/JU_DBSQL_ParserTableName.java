package org.hy.common.db.junit;

import static org.junit.Assert.*;

import org.hy.common.db.DBSQL;
import org.junit.Test;





/**
 * 测试单元：解析表名称
 *
 * @author      ZhengWei(HY)
 * @createDate  2018-07-18
 * @version     v1.0
 */
public class JU_DBSQL_ParserTableName
{
    
    @Test
    public void testInsertTableName01()
    {
        String v_TableName = "";
        
        v_TableName = DBSQL.parserTableNameByInsert("INSERT  INTO  表名称   (A,B,C)  VALUES  (值1,值2,值3)");
        
        System.out.println("Insert Table1：" + v_TableName);
        assertTrue("表名称".equals(v_TableName));
    }
    
    
    
    @Test
    public void testInsertTableName02()
    {
        String v_TableName = "";
        
        v_TableName = DBSQL.parserTableNameByInsert("INSERT  INTO  表名称    VALUES  (值1,值2,值3)");
        
        System.out.println("Insert Table2：" + v_TableName);
        assertTrue("表名称".equals(v_TableName));
    }
    
    
    
    @Test
    public void testUpdateTableName01()
    {
        String v_TableName = "";
        
        v_TableName = DBSQL.parserTableNameByUpdate("UPDATE  表名称   SET  Col1 = 值1 ,Col2 = 值2   WHERE EXISTS (SELECT 1 FROM 表名称2))");
        
        System.out.println("Update Table1：" + v_TableName);
        assertTrue("表名称".equals(v_TableName));
    }
    
    
    
    @Test
    public void testUpdateTableName02()
    {
        String v_TableName = "";
        
        v_TableName = DBSQL.parserTableNameByUpdate("UPDATE  别名   SET  Col1 = 值1 ,Col2 = 值2  FROM  表名称   WHERE EXISTS (SELECT 1 FROM 表名称2)");
        
        System.out.println("Update Table1：" + v_TableName);
        assertTrue("表名称".equals(v_TableName));
    }
    
}
