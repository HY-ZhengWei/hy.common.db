package org.hy.common.db.junit;

import java.util.List;

import org.hy.common.Date;
import org.hy.common.Help;
import org.hy.common.xml.XJava;
import org.hy.common.xml.XSQL;
import org.hy.common.xml.annotation.XType;
import org.hy.common.xml.annotation.Xjava;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;





/**
 * 测试单元：无数据库连接池概念的数据源。 
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-03-02
 * @version     v1.0
 */
@Xjava(XType.XML) 
@FixMethodOrder(MethodSorters.NAME_ASCENDING) 
public class JU_DataSourceNoPool
{
    private static boolean           $IsInit  = false;
    
    
    
    public JU_DataSourceNoPool() throws Exception
    {
        if ( !$IsInit )
        {
            $IsInit = true;
            XJava.parserAnnotation(this.getClass().getName());
        }
    }
    
    
    
    /**
     * （单线程）测试无连接池概念的情况下，每次访问数据库的速度
     * 
     * @author      ZhengWei(HY)
     * @createDate  2016-03-02
     * @version     v1.0
     *
     * 成功访问 1次，    总用时为：0 00:00:00.966。平均时长为：966.0000毫秒
     * 成功访问 10次，   总用时为：0 00:00:01.925。平均时长为：192.5000毫秒
     * 成功访问 100次，  总用时为：0 00:00:05.821。平均时长为： 58.2100毫秒
     * 成功访问 1000次， 总用时为：0 00:00:36.335。平均时长为： 36.3350毫秒
     * 成功访问 10000次，总用时为：0 00:05:20.725。平均时长为： 32.0725毫秒
     */
    @Test
    @SuppressWarnings("unchecked")
    public void test_001()
    {
        XSQL v_XSQL = (XSQL)XJava.getObject("XSQL_SQLite");
        
        for (int i=0; i<1; i++)
        {
            Help.print((List<List<String>>)v_XSQL.query());
        }
        
        System.out.println("-- 成功访问 " + v_XSQL.getSuccessCount() + "次，总用时为："
                         + Date.toTimeLen((long)v_XSQL.getSuccessTimeLen())
                         + "。平均时长为：" + Help.division(v_XSQL.getSuccessTimeLen() ,(double)v_XSQL.getSuccessCount()) + "毫秒");
    }
    
}
