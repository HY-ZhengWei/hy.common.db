package org.hy.common.db.junit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hy.common.xml.log.Logger;
import org.junit.Test;





/**
 * 测试单元：字符串相关的测试
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-05-11
 * @version     v1.0
 */
public class JU_String
{
    
    private static final Logger $Logger = new Logger(JU_String.class ,true);
    
    
    
    @Test
    public void test_String()
    {
        String v_A = "ABCDEFG";
        String v_B = v_A;
        
        v_B = v_B.substring(2);
        $Logger.info(v_B);
        $Logger.info(v_A);
        
        
        String v_W = "SELET * FROM HY WHERE";
        $Logger.info(v_W.substring(0 ,v_W.length() - 6));
    }
    
    
    
    @Test
    public void test_Replace()
    {
        String  v_SQL     = "INSERT  INTO TCalcQiTaoLog                       (                        order_no                       ,dataTableName                       ,dataCreateTime                       )                SELECT  DISTINCT                         A.order_no                       ,'PU_PURCHASING_SCHEDULE'                       ,SYSDATE                  FROM  Order_Shop_Bom_QiTao  A                 WHERE                  AND  A.item_code = UPPER(REPLACE(REPLACE('2401112.4660.02400' ,CHR(13) ,'') ,CHR(10),'')) ";
        Pattern v_Pattern = null;
        Matcher v_Matcher = null;
        
        v_Pattern = Pattern.compile("( )*[Ww][Hh][Ee][Rr][Ee][ ]+[Aa][Nn][Dd][ ]+");
        v_Matcher = v_Pattern.matcher(v_SQL);
        if ( v_Matcher.find() )
        {
            v_SQL = v_Matcher.replaceAll(" WHERE ");
        }
        
        $Logger.info(v_SQL);
    }
    
    
    
    @Test
    public void test_SubString()
    {
        String v_Phone = "19912345678";
        System.out.println(v_Phone.substring(0 ,3) + "****" + v_Phone.substring(7));
    }
    
}
